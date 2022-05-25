package com.javeriana.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javeriana.publish_subscribe.models.MonitorDTO;
import com.javeriana.shared.models.AlarmDTO;
import com.javeriana.shared.models.SensorTopic;
import com.javeriana.shared.models.TopicDTO;
import com.javeriana.shared.utils.SharedUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Monitor {
    static final int MAX_LENGTH_BUFFER = 1024;
    static final byte[] IP_SERVER = new byte[]{127, 0, 0, 1};
    static final String STRING_IP_SERVER = "192.168.5.106";
    static final int SYSTEM_PORT = 4990;

    public static void main(String[] args) {
        String type = args[0];
        List<MonitorDTO> monitors = SharedUtils.initMonitors();

        MonitorDTO monitor = MonitorDTO.findMonitorByTopic(monitors, type);
        int port = monitor.getPort();

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            System.out.println("Imprimiendo socket: " + socket);
            System.out.println("Escuchando por el puerto: " + port);

            System.out.printf("Monitor de: %s%n", type);

            String localIp = String.format("tcp://*:%d", port);

            socket.bind(localIp);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                TopicDTO object = SerializationUtils.deserialize(reply);
                CompletableFuture.runAsync(() -> processData(type, object)).join();
            }
        }
    }

    private static void processData(String type, TopicDTO topic) {
        SensorTopic sensorTopic = SensorTopic.getByTopic(type);
        System.out.printf("Recibiendo información de sensor de ip %s de tipo %s%n", topic.getSensorIp(), topic.getTopic());
        System.out.printf("\tEl dato recibido es: %d%n", topic.getNumber());
        sendDataToFirebase(topic);
        checkData(sensorTopic, topic);
    }

    private static void checkData(SensorTopic type, TopicDTO topic) {
        int data = topic.getNumber();
        if (data < type.getLowerBound() || data > type.getUpperBound()) {
            System.out.printf("Hay un fallo en la medición del número: %d%n", data);
            if (SharedUtils.serverListening(STRING_IP_SERVER, SYSTEM_PORT))
                sendAlarm(topic, topic.getSensorIp());
            else
                System.out.println("No hay usuario activo en el sistema de calidad");
        }
    }

    private static void sendDataToFirebase(TopicDTO topicDTO) {
        try {
            var values = new HashMap<String, String>() {{
                put("value", String.valueOf(topicDTO.getNumber()));
            }};

            var objectMapper = new ObjectMapper();
            String requestBody = objectMapper
                    .writeValueAsString(values);
            String uri = String.format("https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/monitores/%s.json", topicDTO.getTopic());
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<Void> response = client.send(request,
                    HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendAlarm(TopicDTO topic, String ip) {

        int data = topic.getNumber();
        ZMQ.Context context = ZMQ.context(1);

        System.out.println("Enviando alarma\n");

        AlarmDTO alarmDTO = AlarmDTO.createAlarmFromTopic(topic, data, ip);
        byte[] byteArray = SerializationUtils.serialize(alarmDTO);

        ZMQ.Socket requester = context.socket(ZMQ.REQ);

        String serverIp = String.format("tcp://%s:%d", STRING_IP_SERVER, SYSTEM_PORT);

        requester.connect(serverIp);

        requester.send(byteArray, 0);

        requester.close();
        context.term();
    }
}
