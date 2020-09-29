package server;


import java.util.stream.Stream;

public class Database {
    private static final String ERROR = "ERROR";
    private static final String OK = "OK";

    private final String[] tempDb;

    public Database(int numOfCells) {
        this.tempDb = Stream.generate(() -> "").limit(numOfCells).toArray(String[]::new);
    }

    public String set(int cell, String data) {
        if (isOutOfBounds(cell)) return ERROR;

        tempDb[cell - 1] = data;
        return OK;
    }

    public String get(int cell) {
        if (isOutOfBounds(cell)) return ERROR;

        String data = tempDb[cell - 1];
        return data.isEmpty() ? ERROR : data;
    }

    public String delete(int cell) {
        if (isOutOfBounds(cell)) return ERROR;

        tempDb[cell - 1] = "";
        return OK;
    }

    private boolean isOutOfBounds(int cell) {
        return cell < 1 || cell > tempDb.length;
    }
}
