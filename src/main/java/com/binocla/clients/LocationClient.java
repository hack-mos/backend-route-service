package com.binocla.clients;

import com.binocla.models.MinTimePlace;
import com.binocla.models.TaxiRequestDto;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@RegisterRestClient(configKey = "taxi-api")
public interface LocationClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<TaxiRequestDto>> getAllTaxis(
            @RestQuery @DefaultValue("0") int page,
            @RestQuery @DefaultValue("100") int size
    );

    @GET
    @Path("/min")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<MinTimePlace> getMinTimeForPlaces(@RestQuery Integer fromDock, @RestQuery Integer toDock);
}
