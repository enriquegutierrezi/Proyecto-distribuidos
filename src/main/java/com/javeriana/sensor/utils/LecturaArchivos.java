package com.javeriana.sensor.utils;

import com.javeriana.sensor.models.Valores;
import com.javeriana.shared.exceptions.BusinessRuleException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LecturaArchivos {

    public static Valores readConfigFile(String fileName) throws IOException {
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);

        Double validos = Double.valueOf(br.readLine());
        Double fueraDeRango = Double.valueOf(br.readLine());
        Double errores = Double.valueOf(br.readLine());

        if ((validos + errores + fueraDeRango) > 1) {
            throw new BusinessRuleException("La suma de los valores del archivo de configuración es mayor a uno");
        }

        return Valores
                .builder()
                .correctos(validos)
                .fueraDeRango(fueraDeRango)
                .errores(errores)
                .build();
    }

}
