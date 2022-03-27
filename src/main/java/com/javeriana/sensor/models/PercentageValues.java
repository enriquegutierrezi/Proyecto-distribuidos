package com.javeriana.sensor.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PercentageValues {
    private Double valid;
    private Double outOfRange;
    private Double errors;
}
