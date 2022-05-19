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
public class MonitorDTO {
    private int pos;
    private String topic;
    private String mainIp;
    private String secondaryIp;
    private int port;

    public static MonitorDTO findMonitorByTopic(List<MonitorDTO> monitors, String type) {
        return monitors.stream()
                .filter(monitor -> monitor.getTopic().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Sensor de tipo %s no soportado", type));
    }
}
