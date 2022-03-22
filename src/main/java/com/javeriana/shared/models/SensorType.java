package com.javeriana.shared.models;

import com.javeriana.shared.exceptions.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SensorType {
    PH("PH", 6, 8),
    TEMPERATURA("Temperatura", 68, 89),
    OXIGENO("Oxigeno", 2, 11);

    private final String type;
    private final int lowerBound;
    private final int upperBound;

    public static SensorType getByType(String type) {
        return Arrays.stream(SensorType.values())
                .filter(sensorType -> sensorType.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Sensor de tipo %s no soportado", type));
    }
}
