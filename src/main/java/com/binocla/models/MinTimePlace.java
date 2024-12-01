package com.binocla.models;

import lombok.Data;

@Data
public class MinTimePlace {
    private Long fromDock;
    private Long toDock;
    private Double minutes;
    private String fromBerthPosition;
    private Long fromUsageCount;
    private String toBerthPosition;
    private Long toUsageCount;
}
