package com.javeriana.publish_subscribe;

import com.javeriana.publish_subscribe.models.MonitorType;
import com.javeriana.shared.exceptions.SentObject;
import com.javeriana.shared.utils.SharedUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PublishSubscribe {
    static final int PORT = 4980;
    static final int LOCAL_PORT = 4985;
    static final int MAX_LENGTH_BUFFER = 1024;

    public static void main(String[] args) {
        List<MonitorType> monitors = SharedUtils.initMonitors();
        DatagramSocket socket;
        DatagramPacket packet;
        try {
            socket = new DatagramSocket(PORT);
            System.out.println("Imprimiendo socket: " + socket);
            System.out.println("Escuchando por el puerto: " + PORT);
            byte[] buf = new byte[MAX_LENGTH_BUFFER];
            packet = new DatagramPacket(buf, buf.length);

            while (true) {
                socket.receive(packet);
                SentObject object = SerializationUtils.deserialize(packet.getData());
                CompletableFuture.runAsync(() -> sendData(monitors, object)).join();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendData(List<MonitorType> monitors, SentObject object) {
        try {
            ServerSocket s = new ServerSocket(0);
            int pos = MonitorType.findPositionByType(monitors, object.getType());
            DatagramSocket clientSocket = new DatagramSocket(s.getLocalPort());
            byte[] byteArray = SerializationUtils.serialize(object);
            DatagramPacket packet =
                    new DatagramPacket(
                            byteArray,
                            byteArray.length,
                            InetAddress.getByAddress(monitors.get(pos).getIp()),
                            monitors.get(pos).getPort());
            clientSocket.send(packet);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
