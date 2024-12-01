package com.binocla.services;

import com.binocla.clients.LocationClient;
import com.binocla.entities.OrderEntity;
import com.binocla.mappers.OrderMapper;
import com.binocla.models.OrderRequestDto;
import com.binocla.models.TaxiRequestDto;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestPath;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class OrderService {
    @Inject
    OrderMapper orderMapper;
    @RestClient
    LocationClient locationClient;

    @WithTransaction
    public Uni<List<OrderRequestDto>> createOrder(OrderRequestDto orderRequestDto) {
        var fromDock = orderRequestDto.getFromDock();
        var toDock = orderRequestDto.getToDock();
        var totalAmountOfUsers = orderRequestDto.getAmountOfUsers();
        List<OrderRequestDto> createdOrders = new ArrayList<>();
        var amountOfUsersLeft = new AtomicInteger(totalAmountOfUsers);

        return OrderEntity.<OrderEntity>find("fromDock = ?1 and toDock = ?2 and remainingSeats > 0", fromDock, toDock).list()
                .onItem().transformToUni(existingOrders -> {
                    List<Uni<Void>> uniList = new ArrayList<>();
                    for (OrderEntity existingOrder : existingOrders) {
                        if (amountOfUsersLeft.get() <= 0) {
                            break;
                        }

                        int seatsToAllocate = Math.min(amountOfUsersLeft.get(), existingOrder.getRemainingSeats());
                        existingOrder.setRemainingSeats(existingOrder.getRemainingSeats() - seatsToAllocate);
                        amountOfUsersLeft.addAndGet(-seatsToAllocate);

                        Uni<Void> persistUni = existingOrder.<OrderEntity>persistAndFlush()
                                .onItem().invoke(savedOrder -> {
                                    OrderRequestDto dto = orderMapper.toDto(savedOrder);
                                    dto.setAmountOfUsers(seatsToAllocate);
                                    createdOrders.add(dto);
                                }).replaceWithVoid();

                        uniList.add(persistUni);
                    }

                    Uni<Void> uni;
                    if (!uniList.isEmpty()) {
                        uni = Uni.combine().all().unis(uniList).discardItems();
                    } else {
                        uni = Uni.createFrom().voidItem();
                    }

                    return uni.onItem().transformToUni(ignored -> {
                        if (amountOfUsersLeft.get() > 0) {
                            return allocateNewTaxis(fromDock, toDock, amountOfUsersLeft, createdOrders);
                        } else {
                            return Uni.createFrom().item(createdOrders);
                        }
                    });
                });
    }

    private Uni<List<OrderRequestDto>> allocateNewTaxis(Integer fromDock, Integer toDock, AtomicInteger amountOfUsersLeft, List<OrderRequestDto> createdOrders) {
        return locationClient.getAllTaxis(0, 100)
                .onItem().transformToUni(taxis -> allocateUsersToTaxis(taxis, amountOfUsersLeft, fromDock, toDock, createdOrders));
    }

    private Uni<List<OrderRequestDto>> allocateUsersToTaxis(List<TaxiRequestDto> taxis, AtomicInteger amountOfUsersLeft, Integer fromDock, Integer toDock, List<OrderRequestDto> createdOrders) {
        if (amountOfUsersLeft.get() <= 0) {
            return Uni.createFrom().item(createdOrders);
        }

        TaxiRequestDto taxi = taxis.stream()
                .filter(t -> t.getCapacity() > 0)
                .findFirst()
                .orElse(null);

        if (taxi == null) {
            return Uni.createFrom().failure(new IllegalArgumentException("Not enough taxis available to fulfill the order"));
        }

        int seatsToAllocate = Math.min(amountOfUsersLeft.get(), taxi.getCapacity());
        int remainingSeats = taxi.getCapacity() - seatsToAllocate;
        taxi.setCapacity(remainingSeats);
        amountOfUsersLeft.set(amountOfUsersLeft.get() - seatsToAllocate);

        if (remainingSeats == 0) {
            taxis.remove(taxi);
        }

        return locationClient.getMinTimeForPlaces(fromDock, toDock)
                .onItem().transformToUni(minTimePlace -> {
                    var orderEntity = new OrderEntity();
                    orderEntity.setFromDock(fromDock);
                    orderEntity.setToDock(toDock);
                    orderEntity.setRemainingSeats(remainingSeats);
                    orderEntity.setTaxiId(taxi.getId());
                    orderEntity.setPredictedMinutes(minTimePlace.getMinutes());
                    orderEntity.setFromBerthPosition(minTimePlace.getFromBerthPosition());
                    orderEntity.setToBerthPosition(minTimePlace.getToBerthPosition());

                    return orderEntity.<OrderEntity>persistAndFlush()
                            .onItem().transformToUni(savedOrder -> {
                                OrderRequestDto dto = orderMapper.toDto(savedOrder);
                                dto.setAmountOfUsers(seatsToAllocate);
                                createdOrders.add(dto);
                                return allocateUsersToTaxis(taxis, amountOfUsersLeft, fromDock, toDock, createdOrders);
                            });
                });
    }


    @WithTransaction
    public Uni<OrderRequestDto> updateOrderById(Long id, OrderRequestDto taxiRequestDto) {
        return OrderEntity.<OrderEntity>findById(id)
                .onItem()
                .ifNotNull()
                .transformToUni(x -> orderMapper.toEntity(taxiRequestDto).<OrderEntity>persistAndFlush())
                .onItem()
                .transform(x -> orderMapper.toDto(x));
    }

    @WithTransaction
    public Uni<List<OrderRequestDto>> findAll(int page, int size) {
        return OrderEntity.<OrderEntity>findAll().page(page, size).list()
                .onItem()
                .transform(x -> orderMapper.toDtoList(x));
    }

    @WithTransaction
    public Uni<OrderRequestDto> getOrderById(@RestPath Long id) {
        return OrderEntity.<OrderEntity>findById(id)
                .onItem()
                .transform(x -> orderMapper.toDto(x));
    }

    @WithTransaction
    public Uni<Boolean> deleteOrderById(Long id) {
        return OrderEntity.deleteById(id);
    }
}
