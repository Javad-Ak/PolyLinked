package org.aut.polylinked_client.utils;

import org.json.JSONObject;

import java.io.*;

public class JsonHandler {
    private JsonHandler() {
    }

    public static JSONObject getObject(InputStream inp) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inp));
        StringBuilder res = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null)
            res.append(line);

        reader.close();
        return new JSONObject(res.toString());
    }

    public static void sendObject(OutputStream out, JSONObject obj) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write(obj.toString());
        writer.flush();
        writer.close();
    }

    public static JSONObject createJson(String... keyValPairs) {
        if (keyValPairs.length % 2 == 1 || keyValPairs.length == 0)
            return null;
        else {
            JSONObject jsonObject = new JSONObject();
            for (int i = 0; i < keyValPairs.length; i += 2) jsonObject.put(keyValPairs[i], keyValPairs[i + 1]);
            return jsonObject;
        }
    }
}
