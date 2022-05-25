package com.javeriana.sistema_calidad.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.javeriana.shared.utils.SharedUtils;
import com.javeriana.sistema_calidad.auth.models.User;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

public class AuthService {

    public boolean registerUser(String user, String password, String key, String iv) {
        try {

            if (Objects.isNull(getUser(user))) {

                String encryptedPassword = SharedUtils.encrypt(key, iv, password);

                var values = new HashMap<String, String>() {{
                    put("user", user);
                    put("password", encryptedPassword);
                }};

                var objectMapper = new ObjectMapper();
                String requestBody = objectMapper
                        .writeValueAsString(values);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://sistema-sensores-75536-default-rtdb.firebaseio.com/users.json"))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                var response = client.send(request,
                        HttpResponse.BodyHandlers.discarding());

                return response.statusCode() == 200;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String user, String password, String key, String iv) {
        try {
            String encryptedPassword = SharedUtils.encrypt(key, iv, password);
            User firebaseUser = getUser(user);
            return Objects.nonNull(firebaseUser) && firebaseUser.getPassword().equals(encryptedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static User getUser(String user) {
        try {
            String userEncode = URLEncoder.encode("\"user\"", StandardCharsets.UTF_8);
            String userNameEncode = URLEncoder.encode(String.format("\"%s\"", user), StandardCharsets.UTF_8);
            String uri = String.format(
                    "https://sistema-sensores-75536-default-rtdb.firebaseio.com/users.json?orderBy=%s&equalTo=%s",
                    userEncode,
                    userNameEncode
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject object = new JSONObject(response.body());
            if (object.isEmpty())
                return null;

            String name = String.valueOf(object.names().get(0));
            JSONObject elements = object.getJSONObject(name);

            return new Gson().fromJson(String.valueOf(elements), User.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
