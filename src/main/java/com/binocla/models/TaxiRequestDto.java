package com.binocla.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TaxiRequestDto {
    private Long id;
    @NotBlank(message = "Name can't be blank")
    private String name;
    @NotNull
    @Positive
    private Integer capacity;
    @Positive
    private Integer minutesToCharge;
    @Positive
    private Integer battery;
    private String driver;
}