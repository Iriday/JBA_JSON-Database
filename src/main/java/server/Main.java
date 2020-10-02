package server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new Server(9889, "src/main/java/server/data/db.json").run();
    }
}
