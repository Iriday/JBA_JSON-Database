package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final JsonDatabase database;

    public Session(Socket socket, JsonDatabase database) {
        this.socket = socket;
        this.database = database;
    }

    @Override
    public void run() {
        try (var inStream = new DataInputStream(socket.getInputStream());
             var outStream = new DataOutputStream(socket.getOutputStream())) {

            String clientRequestJSON = inStream.readUTF();
            System.out.println("Received: " + clientRequestJSON);
            String resultFromDb = database.executeJson(clientRequestJSON);
            outStream.writeUTF(resultFromDb);
            System.out.println("Sent: " + resultFromDb);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
