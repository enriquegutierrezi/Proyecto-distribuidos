package com.javeriana.shared.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmDTO implements Serializable {
    private int data;
    private String topic;
    private String ip;

    public static AlarmDTO createAlarmFromTopic(TopicDTO type, int data, String ip) {
        return AlarmDTO.builder()
                .ip(ip)
                .topic(type.getTopic())
                .data(data)
                .build();
    }
}
