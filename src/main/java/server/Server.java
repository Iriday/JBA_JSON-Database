package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public void run(int port) {
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("Server started!");

            try (var socket = serverSocket.accept();
                 var input = new DataInputStream(socket.getInputStream());
                 var output = new DataOutputStream(socket.getOutputStream())) {

                System.out.println("Received: " + input.readUTF());
                String data = "A record # 999 was sent!";
                output.writeUTF(data);
                System.out.println("Sent: " + data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
