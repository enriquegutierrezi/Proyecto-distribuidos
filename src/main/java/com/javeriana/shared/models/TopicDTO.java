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
public class TopicDTO implements Serializable {
    private String sensorIp;
    private int number;
    private String topic;
}
