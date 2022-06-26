package client;

import com.beust.jcommander.Parameter;
import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

public class Args {

    @Parameter(names = {"--type", "-t"}, description = "type of the request")
    private String t;

    @Parameter(names = {"--key", "-k"}, description = "key of the cell's value")
    private String k;

    @Parameter(names = {"--value", "-v"}, description = "the value to save in the database")
    private String v;

    @Parameter(names = {"--input", "-in"}, description = "the input file's name")
    private String in;

    Gson gson = new Gson();

    public String getCommands() {

        LinkedHashMap<String, String> command = new LinkedHashMap<>();

        if (in != null) {
            return getFromFile(in);
        }

        switch (t) {

            case "exit":
                command.put("type", "exit");
                return gson.toJson(command);

            case "get":
                command.put("type", "get");
                command.put("key", k);
                return gson.toJson(command);

            case "delete":
                command.put("type", "delete");
                command.put("key", k);
                return gson.toJson(command);

            case "set":
                command.put("type", "set");
                command.put("key", k);
                command.put("value", v);
                return gson.toJson(command);

            default:
                command.put("type", "undefined");
                return gson.toJson(command);
        }
    }
    private String getFromFile(String in) {

        File file = new File(
                "C:/Users/Consolomon/IdeaProjects/JSON Database/JSON Database/task/src/client/data",
                in);

        try (FileReader reader = new FileReader(file)) {

            JsonObject msgToSend = JsonParser.parseReader(reader).getAsJsonObject();
            if (msgToSend.get("type").getAsString().equals("set") && !msgToSend.get("value").isJsonPrimitive()) {
                return new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(msgToSend);
            }

            return gson.toJson(msgToSend);

        } catch (IOException e) {

            e.printStackTrace();
            return "File not found";
        }
    }
}

