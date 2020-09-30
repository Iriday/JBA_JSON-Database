package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public final int PORT;
    private final Database database;

    public Server(int PORT, int dbSize) {
        this.PORT = PORT;
        this.database = new Database(dbSize);
    }

    public void run() {
        try (var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started!");
            while (true) {
                try (var socket = serverSocket.accept();
                     var inStream = new DataInputStream(socket.getInputStream());
                     var outStream = new DataOutputStream(socket.getOutputStream())) {

                    String clientRequest = inStream.readUTF();
                    System.out.println("Received: " + clientRequest);
                    String resultFromDb = executeCommand(clientRequest.split("\\s+", 3), database);
                    outStream.writeUTF(resultFromDb);
                    System.out.println("Sent: " + resultFromDb);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String executeCommand(String[] command, Database database) {
        switch (command[0].toLowerCase()) {
            case "set": return database.set(Integer.parseInt(command[1]), command[2]);
            case "get": return database.get(Integer.parseInt(command[1]));
            case "delete": return database.delete(Integer.parseInt(command[1]));
            default: return "Error: unknown command";
        }
    }
}
