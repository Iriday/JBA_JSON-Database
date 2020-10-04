package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public final int PORT;
    private final JsonDatabase database;

    public Server(int PORT, String dbPath) throws IOException {
        this.PORT = PORT;
        this.database = new JsonDatabase(dbPath);
    }

    public void run() {
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
        try (var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started!");
            while (true) {
                service.submit(new Session(serverSocket.accept(), database));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
