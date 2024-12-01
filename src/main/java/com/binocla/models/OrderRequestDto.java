package com.binocla.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderRequestDto {
    @NotNull
    @Positive
    private Integer fromDock;
    @NotNull
    @Positive
    private Integer toDock;
    @Positive
    private Integer amountOfUsers;

    private Long taxiId;
    private Double predictedMinutes;
    private String fromBerthPosition;
    private String toBerthPosition;
}
