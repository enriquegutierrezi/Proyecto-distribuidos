package com.javeriana.shared.models;

import com.javeriana.shared.exceptions.SentObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmObject implements Serializable {
    private int data;
    private String sensorType;
    private String ip;

    public static AlarmObject createAlarmObjectFromSentObject(SentObject type, int data, String ip) {
        return AlarmObject.builder()
                .ip(ip)
                .sensorType(type.getType())
                .data(data)
                .build();
    }
}
