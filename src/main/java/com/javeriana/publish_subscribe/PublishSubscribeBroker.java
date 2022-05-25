package com.javeriana.publish_subscribe;

import com.javeriana.publish_subscribe.models.MonitorDTO;
import com.javeriana.shared.models.TopicDTO;
import com.javeriana.shared.utils.SharedUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PublishSubscribeBroker {
    static final int PORT = 4980;
    static final int MAX_LENGTH_BUFFER = 1024;

    public static void main(String[] args) {
        List<MonitorDTO> monitors = SharedUtils.initMonitors();

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            System.out.println("Imprimiendo socket: " + socket);
            System.out.println("Escuchando por el puerto: " + PORT);

            String localIp = String.format("tcp://*:%d", PORT);

            socket.bind(localIp);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                TopicDTO object = SerializationUtils.deserialize(reply);
                CompletableFuture.runAsync(() -> sendToSubscriber(monitors, object)).join();
            }
        }
    }

    private static void sendToSubscriber(List<MonitorDTO> monitors, TopicDTO object) {
        System.out.printf("Recibiendo dato %d%n", object.getNumber());
        MonitorDTO monitor = MonitorDTO.findMonitorByTopic(monitors, object.getTopic());
        if (SharedUtils.serverListening(monitor.getMainIp(), monitor.getPort())) {
            System.out.printf("Monitor 1 de %s activo%n", monitor.getTopic());
            sendData(monitor, monitor.getMainIp(), object);
        } else {
            if (SharedUtils.serverListening(monitor.getSecondaryIp(), monitor.getPort())) {
                System.out.printf("Monitor 2 de %s activo%n", monitor.getTopic());
                sendData(monitor, monitor.getSecondaryIp(), object);
            } else {
                System.out.printf("No hay monitor de %s activo%n", object.getTopic());
            }
        }
    }

    private static void sendData(MonitorDTO monitor, String monitorIp, TopicDTO object) {

        System.out.printf("Enviando a monitor de tipo %s con ip %s%n", monitor.getTopic(), monitorIp);
        byte[] byteArray = SerializationUtils.serialize(object);
        ZMQ.Context context = ZMQ.context(1);

        System.out.println("Enviando numero\n");
        ZMQ.Socket requester = context.socket(ZMQ.REQ);

        String serverIp = String.format("tcp://%s:%d", monitorIp, monitor.getPort());

        requester.connect(serverIp);

        requester.send(byteArray, 0);

        requester.close();
        context.term();
    }
}
