package com.javeriana.monitor;

import com.javeriana.publish_subscribe.models.MonitorDTO;
import com.javeriana.shared.models.TopicDTO;
import com.javeriana.shared.models.AlarmDTO;
import com.javeriana.shared.models.SensorTopic;
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
        List<MonitorDTO> monitors = SharedUtils.initMonitors();
        DatagramSocket socket;
        DatagramPacket packet;

        MonitorDTO monitor = MonitorDTO.findMonitorByTopic(monitors, type);
        int port = monitor.getPort();

        try {
            socket = new DatagramSocket(port);
            System.out.println("Escuchando por el puerto: " + port);
            byte[] buf = new byte[MAX_LENGTH_BUFFER];
            packet = new DatagramPacket(buf, buf.length);

            while (true) {
                socket.receive(packet);
                TopicDTO object = SerializationUtils.deserialize(packet.getData());
                CompletableFuture.runAsync(() -> processData(type, object)).join();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processData(String type, TopicDTO topic) {
        SensorTopic sensorTopic = SensorTopic.getByTopic(type);
        System.out.printf("Recibiendo información de sensor de ip %s de tipo %s%n", topic.getSensorIp(), topic.getTopic());
        System.out.printf("\tEl dato recibido es: %d%n", topic.getNumber());
        checkData(sensorTopic, topic);
    }

    private static void checkData(SensorTopic type, TopicDTO topic) {
        int data = topic.getNumber();
        if (data < type.getLowerBound() || data > type.getUpperBound()) {
            System.out.printf("Hay un fallo en la medición del número: %d%n", data);
            // sendAlarm(topic, topic.getSensorIp());
        }
    }

    private static void sendAlarm(TopicDTO topic, String ip) {
        int data = topic.getNumber();
        try {
            ServerSocket s = new ServerSocket(0);
            DatagramSocket clientSocket = new DatagramSocket(s.getLocalPort());
            AlarmDTO alarmDTO = AlarmDTO.createAlarmFromTopic(topic, data, ip);
            byte[] byteArray = SerializationUtils.serialize(alarmDTO);
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
