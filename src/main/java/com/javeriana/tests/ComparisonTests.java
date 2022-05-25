package com.javeriana.tests;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComparisonTests {

    public static void main(String[] args) throws IOException, InterruptedException {

        List<Integer> sensorsHeatList, sensorsPHList, sensorsOxygenList, monitorsHeatList, monitorsPHList,
                monitorsOxygenList, systemHeatList, systemPHList, systemOxygenList;

        String sensorsHeatUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/sensores/Temperatura.json";
        String sensorsPHUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/sensores/PH.json";
        String sensorsOxygenUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/sensores/Oxigeno.json";

        String monitorsHeatUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/monitores/Temperatura.json";
        String monitorsPHUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/monitores/PH.json";
        String monitorsOxygenUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/monitores/Oxigeno.json";

        String systemHeatUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/central/Temperatura.json";
        String systemPHUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/central/PH.json";
        String systemOxygenUri = "https://sistema-sensores-75536-default-rtdb.firebaseio.com/sistema/central/Oxigeno.json";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest requestSensorsHeat = getRequest(sensorsHeatUri);
        HttpRequest requestSensorsPH = getRequest(sensorsPHUri);
        HttpRequest requestSensorsOxygen = getRequest(sensorsOxygenUri);

        HttpRequest requestMonitorsHeat = getRequest(monitorsHeatUri);
        HttpRequest requestMonitorsPH = getRequest(monitorsPHUri);
        HttpRequest requestMonitorsOxygen = getRequest(monitorsOxygenUri);

        HttpRequest requestSystemHeat = getRequest(systemHeatUri);
        HttpRequest requestSystemPH = getRequest(systemPHUri);
        HttpRequest requestSystemOxygen = getRequest(systemOxygenUri);

        var responseSensorsHeat = client.send(requestSensorsHeat, HttpResponse.BodyHandlers.ofString());
        var responseSensorsPH = client.send(requestSensorsPH, HttpResponse.BodyHandlers.ofString());
        var responseSensorsOxygen = client.send(requestSensorsOxygen, HttpResponse.BodyHandlers.ofString());

        var responseMonitorHeat = client.send(requestMonitorsHeat, HttpResponse.BodyHandlers.ofString());
        var responseMonitorPH = client.send(requestMonitorsPH, HttpResponse.BodyHandlers.ofString());
        var responseMonitorOxygen = client.send(requestMonitorsOxygen, HttpResponse.BodyHandlers.ofString());

        var responseSystemHeat = client.send(requestSystemHeat, HttpResponse.BodyHandlers.ofString());
        var responseSystemPH = client.send(requestSystemPH, HttpResponse.BodyHandlers.ofString());
        var responseSystemOxygen = client.send(requestSystemOxygen, HttpResponse.BodyHandlers.ofString());

        sensorsHeatList = processJsonObject(responseSensorsHeat);
        sensorsPHList = processJsonObject(responseSensorsPH);
        sensorsOxygenList = processJsonObject(responseSensorsOxygen);

        monitorsHeatList = processJsonObject(responseMonitorHeat);
        monitorsPHList = processJsonObject(responseMonitorPH);
        monitorsOxygenList = processJsonObject(responseMonitorOxygen);

        systemHeatList = processJsonObject(responseSystemHeat);
        systemPHList = processJsonObject(responseSystemPH);
        systemOxygenList = processJsonObject(responseSystemOxygen);

        calculateDifferences(sensorsHeatList, monitorsHeatList, systemHeatList, "Temperatura");
        calculateDifferences(sensorsPHList, monitorsPHList, systemPHList, "PH");
        calculateDifferences(sensorsOxygenList, monitorsOxygenList, systemOxygenList, "Oxigeno");
    }

    private static void calculateDifferences(List<Integer> sensorResults, List<Integer> monitorResults,
                                             List<Integer> systemResults, String topic) {
        System.out.printf("Sensor de %s%n", topic);
        int diffSensorMonitor = sensorResults.size() - monitorResults.size();
        double percentageSensorMonitor = diffSensorMonitor > 0 ? (diffSensorMonitor * 100.0) / sensorResults.size() : 0;

        System.out.printf("La perdida de información entre el sensor y el monitor es de %.2f%%%n", percentageSensorMonitor);

        int diffMonitorSystem = monitorResults.size() - systemResults.size();
        double percentageMonitorSystem = diffMonitorSystem > 0 ? (diffMonitorSystem * 100.0) / monitorResults.size() : 0;

        System.out.printf("El porcentaje de alarmas no enviadas por el monitor al sistema es de %.2f%%%n", percentageMonitorSystem);

        System.out.println("-------------------------------------------------------------------------------------");

    }

    private static HttpRequest getRequest(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
    }

    private static List<Integer> processJsonObject(HttpResponse<String> response) {
        List<Integer> result = new ArrayList<>();
        if (!response.body().equals("null")) {
            JSONObject object = new JSONObject(response.body());

            Iterator<String> keys = object.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject localKey = object.getJSONObject(key);
                int value = localKey.getInt("value");
                result.add(value);
            }
        }

        return result;
    }

    private static int findDifference(List<Integer> first, List<Integer> second) {
        List<Integer> diff = new ArrayList<>(first);
        diff.removeAll(second);
        return diff.size();
    }

}
