package com.javeriana.sensor;

import com.javeriana.sensor.models.Valores;
import com.javeriana.sensor.services.SensorService;
import com.javeriana.sensor.utils.LecturaArchivos;
import com.javeriana.shared.exceptions.SentObject;
import com.javeriana.shared.models.SensorType;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Sensor {
    static final byte[] IP_SERVER = new byte[]{127, 0, 0, 1};
    static final int PORT = 4980;

    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket;

        String filePath = String.format("src/main/resources/%s", args[2]);
        Valores valores = LecturaArchivos.readConfigFile(filePath);
        SensorType sensorType = SensorType.getByType(args[0]);
        int time = Integer.parseInt(args[1]);
        SensorService service = new SensorService();

        List<Integer> generatedNumbers = service.generateRandomNumbers(valores, sensorType);
        System.out.println(generatedNumbers);

        clientSocket = new DatagramSocket(4981);

        generatedNumbers
                .stream()
                .filter(number -> number > 0)
                .map(number -> SentObject.builder()
                        .number(number)
                        .sensorIp(clientSocket.getLocalAddress().getHostAddress())
                        .type(sensorType.getType())
                        .build())
                .map(SerializationUtils::serialize)
                .forEach(byteArray -> writeObject(byteArray, clientSocket, time));
        clientSocket.close();
    }

    private static void writeObject(byte[] byteArray, DatagramSocket clientSocket, int time) {
        try {
            DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, InetAddress.getByAddress(IP_SERVER), PORT);
            clientSocket.send(packet);
            TimeUnit.SECONDS.sleep(time);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
