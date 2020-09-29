package server;

public class Main {
    public static void main(String[] args) {
        Database database = new Database(100);
        ViewDbTest viewDbTest = new ViewDbTest(database);
        viewDbTest.run();
    }
}
