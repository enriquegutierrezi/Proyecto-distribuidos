package com.javeriana.sistema_calidad;

import com.javeriana.shared.models.AlarmObject;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CompletableFuture;

public class SistemaCalidad {

    static final int MAX_LENGTH_BUFFER = 1024;
    static final int PORT = 4990;

    public static void main(String[] args) {
        DatagramSocket socket;
        DatagramPacket packet;

        try {
            socket = new DatagramSocket(PORT);
            System.out.println("Escuchando por el puerto: " + PORT);
            byte[] buf = new byte[MAX_LENGTH_BUFFER];
            packet = new DatagramPacket(buf, buf.length);

            while (true) {
                socket.receive(packet);
                AlarmObject object = SerializationUtils.deserialize(packet.getData());
                CompletableFuture.runAsync(() -> processData(object));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processData(AlarmObject object) {
        System.out.printf("El sensor de %s con IP %s, ha enviado el siguiente valor erroneo %d%n",
                object.getSensorType(), object.getIp(), object.getData());
    }
}
