package com.javeriana.sensor.services;

import com.javeriana.shared.models.SensorType;
import com.javeriana.sensor.models.Valores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class SensorService {
    public List<Integer> generateRandomNumbers(Valores valores, SensorType sensorType) {
        Random rand = new Random();
        int randomAmount = rand.nextInt(20) + 1;

        List<Integer> numbersList = new ArrayList<>();

        int valid = (int) Math.round(randomAmount * valores.getCorrectos());
        int outOfRange = (int) Math.round(randomAmount * valores.getFueraDeRango());
        int errors = (int) Math.round(randomAmount * valores.getErrores());

        IntStream.range(0, valid)
                .mapToObj(pos -> rand.nextInt(sensorType.getUpperBound() - sensorType.getLowerBound() + 1) + sensorType.getLowerBound())
                .forEach(numbersList::add);

        IntStream.range(0, errors)
                .mapToObj(pos -> rand.nextInt(10) * -1)
                .forEach(numbersList::add);

        IntStream.range(0, outOfRange)
                .mapToObj(pos -> {
                    if (pos % 2 == 0)
                        return rand.nextInt(sensorType.getLowerBound());
                    return rand.nextInt(sensorType.getUpperBound() + 1) + sensorType.getUpperBound() + 1;
                })
                .forEach(numbersList::add);

        Collections.shuffle(numbersList);
        return numbersList;
    }
}
