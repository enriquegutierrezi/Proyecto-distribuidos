package com.javeriana.publish_subscribe.models;

import com.javeriana.shared.exceptions.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorType {
    private int pos;
    private String sensorType;
    private byte[] ip;
    private int port;

    public static int findPositionByType(List<MonitorType> monitors, String type) {
        return monitors.stream()
                .filter(monitor -> monitor.getSensorType().equalsIgnoreCase(type))
                .findFirst()
                .map(MonitorType::getPos)
                .orElseThrow(() -> new BusinessRuleException("Sensor de tipo %s no soportado", type));
    }

    public static int findPortByType(List<MonitorType> monitors, String type) {
        return monitors.stream()
                .filter(monitor -> monitor.getSensorType().equalsIgnoreCase(type))
                .findFirst()
                .map(MonitorType::getPort)
                .orElseThrow(() -> new BusinessRuleException("Sensor de tipo %s no soportado", type));
    }
}
