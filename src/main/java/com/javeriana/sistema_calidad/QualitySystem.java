package com.javeriana.sistema_calidad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javeriana.shared.models.AlarmDTO;
import com.javeriana.sistema_calidad.auth.AuthService;
import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class QualitySystem {
    static final String KEY = "92AE31A79FEEB2A3"; // llave
    static final String INITIALIZATION_VECTOR = "0123456789ABCDEF"; // vector de inicialización
    static final int MAX_LENGTH_BUFFER = 1024;
    static final int PORT = 4990;

    public static void main(String[] args) {
        int n = 0;
        AuthService authService = new AuthService();

        do {
            System.out.println("\t1. Registrarse");
            System.out.println("\t2. Iniciar sesión");

            System.out.println("Qué acción desea realizar? ");
            Scanner s = new Scanner(System.in);
            n = s.nextInt();

            String user, password;

            if (n == 1) {
                System.out.println("Ingrese el usuario a registrar: ");
                user = s.next();
                System.out.println("Ingrese su contraseña: ");
                password = s.next();
                boolean registered = authService.registerUser(user, password, KEY, INITIALIZATION_VECTOR);
                if (registered) {
                    System.out.println("Usuario registrado exitosamente");
                    System.out.printf("Usuario loggeado como %s%n%n", user);
                    startReading();
                } else {
                    System.out.println("Hubo un problema registrando el usuario");
                    n = 0;
                }

            } else if (n == 2) {
                System.out.println("Ingrese su usuario:");
                user = s.next();
                System.out.println("Ingrese su contraseña:");
                password = s.next();

                boolean loggedIn = authService.login(user, password, KEY, INITIALIZATION_VECTOR);

                if (loggedIn) {
                    System.out.printf("Usuario loggeado como %s%n%n", user);
                    startReading();
                } else {
                    System.out.println("Hubo un problema iniciando sesión");
                    n = 0;
                }
            }
        } while (n != 1 && n != 2);
    }

    private static void startReading() {
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
        sendDataToFirebase(object);
    }

    private static void sendDataToFirebase(AlarmDTO alarmDTO) {
        try {
            var values = new HashMap<String, String>() {{
                put("value", String.valueOf(alarmDTO.getData()));
            }};

            var objectMapper = new ObjectMapper();
            String requestBody = objectMapper
                    .writeValueAsString(values);
            String uri = String.format("https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/central/%s.json", alarmDTO.getTopic());
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
}
