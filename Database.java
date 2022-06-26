package server;

import com.google.gson.*;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

    private  final Gson gson;
    private final File file;
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public Database(File file) {

        this.file = file;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

    }

    protected String get(JsonElement keyList) {

        synchronized (readLock) {

            LinkedHashMap<String, JsonElement> response = new LinkedHashMap<>();

            JsonObject tree = readFromDb();

            try {

                if (keyList.isJsonPrimitive() && tree.has(keyList.getAsString())) {

                    JsonElement subTree = tree.get(keyList.getAsString());
                    response.put("response", new JsonPrimitive("OK"));
                    response.put("value", subTree);
                    return getGson(false).toJson(response);

                } else {

                    JsonElement subTree = recursiveGet(tree, keyList.getAsJsonArray());
                    response.put("response", new JsonPrimitive("OK"));
                    response.put("value", subTree);
                    return getGson(false).toJson(response);
                }

            } catch (NoSuchKeyException e) {

                response.put("response", new JsonPrimitive("ERROR"));
                response.put("reason", new JsonPrimitive("No such key"));
                return getGson(false).toJson(response);
            }
        }
    }


    protected String set(JsonElement keyList, JsonElement value) {

       JsonObject tree = readFromDb();

       if (keyList.isJsonPrimitive()) {

           tree.add(keyList.getAsString(), value);

       } else {

           tree = recursiveSet(tree, keyList.getAsJsonArray(), value);
       }
        synchronized (writeLock) {

            try (FileWriter writer = new FileWriter(file, false)) {

                JsonElement finalTree = gson.toJsonTree(tree);
                gson.toJson(finalTree, writer);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return getGson(false).toJson(Map.of("response", "OK"));
        }
    }

    protected String delete(JsonElement keyList) {

        synchronized (lock) {

            LinkedHashMap<String, String> response = new LinkedHashMap<>();

            JsonObject tree = readFromDb();

            try  {

                if (keyList.isJsonPrimitive() && tree.has(keyList.getAsString())) {
                    tree.remove(keyList.getAsString());
                } else {
                    tree = recursiveDelete(tree, keyList.getAsJsonArray());
                }

                try (FileWriter writer = new FileWriter(file, false)) {

                    if (tree.size() != 0) {
                        gson.toJson(tree, writer);
                    }
                    response.put("response", "OK");

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (NoSuchKeyException e) {
                response.put("response", "ERROR");
                response.put("reason", "No such key");
            }

            return getGson(false).toJson(response);

        }
    }

    protected JsonObject readFromDb() {

        synchronized (readLock) {

            JsonObject rootTree = new JsonObject();

            try (FileReader reader = new FileReader(file)) {

                rootTree = JsonParser.parseReader(reader).getAsJsonObject();

            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
            }

            return rootTree;
        }
    }

    private JsonElement recursiveGet (JsonObject tree, JsonArray keyList) throws NoSuchKeyException {

        if (keyList.size() != 0 && tree.has(keyList.get(0).getAsString())) {

            String key = keyList.remove(0).getAsString();
            if(keyList.size() != 0) {

                return recursiveGet(tree.getAsJsonObject(key), keyList);

            } else {

                if (tree.get(key).isJsonPrimitive()) {
                   return tree.get(key);
                }

                return tree.getAsJsonObject(key);
            }
        }

        throw new NoSuchKeyException();
    }

    private JsonObject recursiveSet(JsonObject tree, JsonArray keyList, JsonElement value) {

        if (tree != null && keyList.size() != 0 && tree.has(keyList.get(0).getAsString())) {

           String key = keyList.remove(0).getAsString();

            if (keyList.size() != 0) {

                JsonObject updatedTree = recursiveSet(tree.getAsJsonObject(key), keyList, value);
                tree.add(key, updatedTree);
                return tree;

            } else {
                tree.add(key, value);
                return tree;
            }
        }
        if (keyList.size() != 0 && ( tree == null || !tree.has(keyList.get(0).getAsString()))) {

            String key = keyList.remove(0).getAsString();
            if (keyList.size() != 0) {

                tree.add(key, recursiveSet(tree, keyList, value));
                return tree;

            } else {
                tree.add(key, value);
                return tree;
            }
        }

        return tree;
    }

    private JsonObject recursiveDelete(JsonObject tree, JsonArray keyList) throws NoSuchKeyException {

        if (keyList.size() != 0 && tree.has(keyList.get(0).getAsString())) {

            String key = keyList.remove(0).getAsString();

            if(keyList.size() != 0) {

                JsonObject updatedTree = recursiveDelete(tree.getAsJsonObject(key), keyList);
                tree.add(key, updatedTree);
                return tree;

            } else {
                tree.remove(key);
                return tree;
            }
        }

        throw  new NoSuchKeyException();
    }

    public Gson getGson(boolean isPretty) {
        return isPretty ? this.gson : new Gson();
    }
}
