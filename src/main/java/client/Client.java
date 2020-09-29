package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public void run(String host, int port) {
        try (var socket = new Socket(InetAddress.getByName(host), port);
             var input = new DataInputStream(socket.getInputStream());
             var output = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Client started!");

            String data = "Give me a record # 999";
            output.writeUTF(data);
            System.out.println("Sent: " + data);
            System.out.println("Received: " + input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
