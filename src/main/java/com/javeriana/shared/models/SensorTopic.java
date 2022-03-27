package com.javeriana.shared.models;

import com.javeriana.shared.exceptions.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SensorTopic {
    PH("PH", 6, 8),
    TEMPERATURA("Temperatura", 68, 89),
    OXIGENO("Oxigeno", 2, 11);

    private final String type;
    private final int lowerBound;
    private final int upperBound;

    public static SensorTopic getByTopic(String type) {
        return Arrays.stream(SensorTopic.values())
                .filter(sensorTopic -> sensorTopic.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Sensor de tipo %s no soportado", type));
    }
}
