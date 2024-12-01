package com.binocla.resources;

import com.binocla.models.OrderRequestDto;
import com.binocla.services.OrderService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/api/v1/orders")
@RolesAllowed({"HACK_ADMIN", "HACK_CLIENT"})
public class OrderResource {
    @Inject
    OrderService orderService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<OrderRequestDto>> getAllOrders(
            @RestQuery @DefaultValue("0") int page,
            @RestQuery @DefaultValue("10") int size
    ) {
        return orderService.findAll(page, size);
    }

    @GET
    @Path(("/{id}"))
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<OrderRequestDto> getOrderById(@RestPath Long id) {
        return orderService.getOrderById(id);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<List<OrderRequestDto>> createOrder(OrderRequestDto orderRequestDto) {
        return orderService.createOrder(orderRequestDto);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<OrderRequestDto> updateOrderById(@RestPath Long id, OrderRequestDto taxiRequestDto) {
        return orderService.updateOrderById(id, taxiRequestDto);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Boolean> deleteOrderById(@RestPath Long id) {
        return orderService.deleteOrderById(id);
    }
}
