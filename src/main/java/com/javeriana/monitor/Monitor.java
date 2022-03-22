package com.javeriana.monitor;

import com.javeriana.publish_subscribe.models.MonitorType;
import com.javeriana.shared.exceptions.SentObject;
import com.javeriana.shared.models.AlarmObject;
import com.javeriana.shared.models.SensorType;
import com.javeriana.shared.utils.SharedUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Monitor {
    static final int MAX_LENGTH_BUFFER = 1024;
    static final byte[] IP_SERVER = new byte[]{127, 0, 0, 1};
    static final int SYSTEM_PORT = 4990;

    public static void main(String[] args) {
        String type = args[0];
        List<MonitorType> monitors = SharedUtils.initMonitors();
        DatagramSocket socket;
        DatagramPacket packet;

        int port = MonitorType.findPortByType(monitors, type);

        try {
            socket = new DatagramSocket(port);
            System.out.println("Escuchando por el puerto: " + port);
            byte[] buf = new byte[MAX_LENGTH_BUFFER];
            packet = new DatagramPacket(buf, buf.length);

            while (true) {
                socket.receive(packet);
                SentObject object = SerializationUtils.deserialize(packet.getData());
                CompletableFuture.runAsync(() -> processData(type, object)).join();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processData(String type, SentObject object) {
        SensorType sensorType = SensorType.getByType(type);
        System.out.printf("Recibiendo información de sensor de ip %s de tipo %s%n", object.getSensorIp(), object.getType());
        System.out.printf("\tEl dato recibido es: %d%n", object.getNumber());
        checkData(object.getNumber(), sensorType, object);
    }

    private static void checkData(int data, SensorType type, SentObject object) {
        if (data < type.getLowerBound() || data > type.getUpperBound()) {
            System.out.printf("Hay un fallo en la medición del número: %d%n", data);
            sendData(object, object.getSensorIp(), data);
        }
    }

    private static void sendData(SentObject object, String ip, int data) {
        try {
            ServerSocket s = new ServerSocket(0);
            DatagramSocket clientSocket = new DatagramSocket(s.getLocalPort());
            AlarmObject alarmObject = AlarmObject.createAlarmObjectFromSentObject(object, data, ip);
            byte[] byteArray = SerializationUtils.serialize(alarmObject);
            DatagramPacket packet =
                    new DatagramPacket(
                            byteArray,
                            byteArray.length,
                            InetAddress.getByAddress(IP_SERVER),
                            SYSTEM_PORT);
            clientSocket.send(packet);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
