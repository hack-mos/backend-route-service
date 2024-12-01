package com.binocla.mappers;

import com.binocla.entities.OrderEntity;
import com.binocla.models.OrderRequestDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OrderMapper {
    public OrderEntity toEntity(OrderRequestDto orderRequestDto) {
        var orderEntity = new OrderEntity();
        orderEntity.setFromDock(orderRequestDto.getFromDock());
        orderEntity.setToDock(orderRequestDto.getToDock());
        return orderEntity;
    }

    public OrderRequestDto toDto(OrderEntity orderEntity) {
        var orderRequestDto = new OrderRequestDto();
        orderRequestDto.setFromDock(orderEntity.getFromDock());
        orderRequestDto.setToDock(orderEntity.getToDock());
        orderRequestDto.setTaxiId(orderEntity.getTaxiId());
        orderRequestDto.setFromBerthPosition(orderEntity.getFromBerthPosition());
        orderRequestDto.setToBerthPosition(orderEntity.getToBerthPosition());
        orderRequestDto.setPredictedMinutes(orderEntity.getPredictedMinutes());
        return orderRequestDto;
    }

    public List<OrderRequestDto> toDtoList(List<OrderEntity> orderEntityList) {
        var list = new ArrayList<OrderRequestDto>();
        for (var e : orderEntityList) {
            list.add(toDto(e));
        }
        return list;
    }
}
