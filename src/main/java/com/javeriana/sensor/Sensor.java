package com.javeriana.sensor;

import com.javeriana.sensor.models.PercentageValues;
import com.javeriana.sensor.utils.LecturaArchivos;
import com.javeriana.shared.models.SensorTopic;
import com.javeriana.shared.models.TopicDTO;
import com.javeriana.shared.utils.SharedUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Sensor {
    static final byte[] IP_SERVER = new byte[]{127, 0, 0, 1};
    static final String STRING_IP_SERVER = "127.0.0.1";
    static final int PORT = 4980;

    public static void main(String[] args) throws IOException {
        String filePath = String.format("src/main/resources/%s", args[2]);
        PercentageValues percentageValues = LecturaArchivos.readConfigFile(filePath);
        SensorTopic sensorTopic = SensorTopic.getByTopic(args[0]);
        int time = Integer.parseInt(args[1]);

        while (true) {
            List<Integer> generatedNumbers = generateRandomNumbers(percentageValues, sensorTopic);
            System.out.println(generatedNumbers);

            generatedNumbers
                    .stream()
                    .filter(number -> number > 0)
                    .map(number -> TopicDTO.builder()
                            .number(number)
                            .sensorIp(SharedUtils.getLocalIp())
                            .topic(sensorTopic.getType())
                            .build())
                    .map(SerializationUtils::serialize)
                    .forEach(byteArray -> publish(byteArray, time));
        }
    }

    private static void publish(byte[] byteArray, int time) {
        try {
            ZMQ.Context context = ZMQ.context(1);

            System.out.println("Enviando numero\n");
            ZMQ.Socket requester = context.socket(ZMQ.REQ);

            String serverIp = String.format("tcp://%s:%d", STRING_IP_SERVER, PORT);

            requester.connect(serverIp);

            requester.send(byteArray, 0);

            requester.close();
            context.term();
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> generateRandomNumbers(PercentageValues percentageValues, SensorTopic sensorTopic) {
        Random rand = new Random();
        int randomAmount = rand.nextInt(20) + 1;

        List<Integer> numbersList = new ArrayList<>();

        int valid = (int) Math.round(randomAmount * percentageValues.getValid());
        int outOfRange = (int) Math.round(randomAmount * percentageValues.getOutOfRange());
        int errors = (int) Math.round(randomAmount * percentageValues.getErrors());

        IntStream.range(0, valid)
                .mapToObj(pos -> rand.nextInt(sensorTopic.getUpperBound() - sensorTopic.getLowerBound() + 1) + sensorTopic.getLowerBound())
                .forEach(numbersList::add);

        IntStream.range(0, errors)
                .mapToObj(pos -> rand.nextInt(10) * -1)
                .forEach(numbersList::add);

        IntStream.range(0, outOfRange)
                .mapToObj(pos -> {
                    if (pos % 2 == 0)
                        return rand.nextInt(sensorTopic.getLowerBound());
                    return rand.nextInt(sensorTopic.getUpperBound() + 1) + sensorTopic.getUpperBound() + 1;
                })
                .forEach(numbersList::add);

        Collections.shuffle(numbersList);
        return numbersList;
    }
}
