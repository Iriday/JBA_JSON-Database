package server;

import java.util.Scanner;

public class ViewDbTest {
    private final Database database;

    public ViewDbTest(Database database) {
        this.database = database;
    }

    public void run() {
        var scn = new Scanner(System.in);

        while (true) {
            String input = scn.nextLine().trim();
            if (input.equalsIgnoreCase("EXIT")) break;

            System.out.println(executeCommand(input.split("\\s+", 3), database));
        }
    }

    public static String executeCommand(String[] command, Database database) {
        switch (command[0].toUpperCase()) {
            case "SET": return database.set(Integer.parseInt(command[1]), command[2]);
            case "GET": return database.get(Integer.parseInt(command[1]));
            case "DELETE": return database.delete(Integer.parseInt(command[1]));
            default: return "Error unknown command";
        }
    }
}
