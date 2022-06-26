package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Session {

    private final Socket socket;
    private final Database db;
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public Session(Socket clientSocket, Database db) {

        this.socket = clientSocket;
        this.db = db;
    }

    public void run(ExecutorService tEngine, ServerSocket server) {

        try (
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ){

            String message = input.readUTF();

            JsonObject cmd = JsonParser.parseString(message).getAsJsonObject();

            String type = cmd.get("type").getAsString();
            JsonElement key = cmd.get("key");

            switch (type) {

                case "get":
                    readLock.lock();
                    String receivedForGet = db.get(key);
                    output.writeUTF(receivedForGet);
                    readLock.unlock();
                    break;

                case "set":
                    writeLock.lock();
                    JsonElement value = cmd.get("value");
                    String receivedForSet = db.set(key, value);
                    output.writeUTF(receivedForSet);
                    writeLock.unlock();
                    break;

                case "delete":
                    writeLock.lock();
                    String receivedForDelete = db.delete(key);
                    output.writeUTF(receivedForDelete);
                    writeLock.unlock();
                    break;

                case "exit":
                    writeLock.lock();
                    tEngine.shutdown();
                    output.writeUTF(db.getGson(false).toJson(Map.of("response", "OK")));
                    server.close();
                    socket.close();
                    System.exit(0);
                    writeLock.unlock();


                default:
                    synchronized (readLock) {
                        output.writeUTF(db.getGson(false).toJson(Map.of("response", "ERROR: not such type")));
                        break;
                    }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
