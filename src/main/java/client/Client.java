package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public void run(String host, int port) {
        try (var socket = new Socket(InetAddress.getByName(host), port);
             var inStream = new DataInputStream(socket.getInputStream());
             var outStream = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Client started!");

            String data = "set 999 Hello World!";
            outStream.writeUTF(data);
            System.out.println("Sent: " + data);
            System.out.println("Received: " + inStream.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
