package com.javeriana.sistema_calidad;

import com.javeriana.shared.models.AlarmDTO;
import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.CompletableFuture;

public class QualitySystem {
    static final String KEY = "92AE31A79FEEB2A3"; // llave
    static final String INITIALIZATION_VECTOR = "0123456789ABCDEF"; // vector de inicialización
    static final int MAX_LENGTH_BUFFER = 1024;
    static final int PORT = 4990;

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            System.out.println("Imprimiendo socket: " + socket);
            System.out.println("Escuchando por el puerto: " + PORT);

            String localIp = String.format("tcp://*:%d", PORT);

            socket.bind(localIp);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                AlarmDTO object = SerializationUtils.deserialize(reply);
                CompletableFuture.runAsync(() -> processData(object)).join();
            }
        }
    }

    private static void processData(AlarmDTO object) {
        System.out.printf("El sensor de %s con IP %s, ha enviado el siguiente valor erroneo %d%n",
                object.getTopic(), object.getIp(), object.getData());
    }
}
