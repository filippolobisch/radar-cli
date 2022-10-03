package helpers;

import java.io.File;
import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonConverter {
    private JSONObject getJsonObject() {
        JSONParser parser = new JSONParser();
        String filePath = new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator + "src" +
                File.separator + "main" + File.separator + "java" + File.separator + "helpers" + File.separator
                + "model.json";
        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getJSONInStringFormat() {
        JSONObject jsonObject = getJsonObject();
        return (jsonObject != null) ? jsonObject.toJSONString() : "";
    }
}
