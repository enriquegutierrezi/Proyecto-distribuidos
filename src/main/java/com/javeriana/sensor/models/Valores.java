package com.javeriana.sensor.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Valores {
    private Double correctos;
    private Double fueraDeRango;
    private Double errores;
}
