package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public final int PORT;
    private final JsonDatabase database;

    public Server(int PORT) {
        this.PORT = PORT;
        this.database = new JsonDatabase();
    }

    public void run() {
        try (var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started!");
            while (true) {
                try (var socket = serverSocket.accept();
                     var inStream = new DataInputStream(socket.getInputStream());
                     var outStream = new DataOutputStream(socket.getOutputStream())) {

                    String clientRequestJSON = inStream.readUTF();
                    System.out.println("Received: " + clientRequestJSON);
                    String resultFromDb = database.executeJson(clientRequestJSON);
                    outStream.writeUTF(resultFromDb);
                    System.out.println("Sent: " + resultFromDb);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
