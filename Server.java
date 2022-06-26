package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final static int PORT = 12345;

    public void start() {

        Database db = new Database(new File(
                "C:/Users/Consolomon/IdeaProjects/JSON Database/JSON Database/task/src/server/data",
                "db.json"));
        System.out.println("Server started!");

        try (ServerSocket server = new ServerSocket(PORT)) {

            int poolSize = Runtime.getRuntime().availableProcessors();

            ExecutorService threadEngine = Executors.newFixedThreadPool(poolSize);

            while (true) {

                Session session = new Session(server.accept(), db);
                threadEngine.submit(() -> session.run(threadEngine, server));
            }

        } catch (IOException e) {

            if (e.getMessage().equals("Socket closed")) {
               System.out.println("Server is stopped by client");
            } else {
                e.printStackTrace();
            }
        }
    }
}
