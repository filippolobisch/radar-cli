package runtime;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import restassured.riskpattern.CloudModel.CloudEnvironment;

public class ExecutionAdaptorVapor extends ExecutionAdapter {

    private final RuntimeModelLogic logic;

    public ExecutionAdaptorVapor(RuntimeModelLogic logic) {
        this.logic = logic;
    }

    @Override
    public boolean execute(CloudEnvironment proposal, ArrayList<Integer> adaptationCaseNumber) {
        String modelToSend = logic.returnentiremodelasjson(proposal);
        try {
            int result = sendAdaptationsToSystem(modelToSend, adaptationCaseNumber);
            return result == 200;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't Connect to Application");
            return false;
        }
    }

    private int sendAdaptationsToSystem(String modelToSend, ArrayList<Integer> adaptations)
            throws IOException {
        String body = "{\"model\": " + modelToSend + ", \"adaptations\": " + adaptations.toString() + "}";
        String fullUrlString = "http://127.0.0.1:8080/execute";
        HttpURLConnection request;
        URL restUrl = new URL(fullUrlString);
        request = (HttpURLConnection) restUrl.openConnection();
        request.setRequestMethod("POST");
        request.setDoOutput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        return request.getResponseCode();
    }

}
