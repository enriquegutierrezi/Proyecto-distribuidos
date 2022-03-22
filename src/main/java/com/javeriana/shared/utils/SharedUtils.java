package com.javeriana.shared.utils;

import com.javeriana.publish_subscribe.models.MonitorType;

import java.util.List;

public class SharedUtils {
    public static List<MonitorType> initMonitors() {
        MonitorType ph = MonitorType.builder()
                .pos(0)
                .sensorType("PH")
                .port(4982)
                .ip(new byte[]{127, 0, 0, 1})
                .build();

        MonitorType oxigeno = MonitorType.builder()
                .pos(1)
                .sensorType("Oxigeno")
                .port(4983)
                .ip(new byte[]{127, 0, 0, 1})
                .build();

        MonitorType temperatura = MonitorType.builder()
                .pos(2)
                .sensorType("Temperatura")
                .port(4984)
                .ip(new byte[]{127, 0, 0, 1})
                .build();

        return List.of(ph, oxigeno, temperatura);
    }
}
