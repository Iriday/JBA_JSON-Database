package server;

import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static server.JsonDbUtils.*;

public class JsonDatabase {
    public static final String ERROR_NO_SUCH_KEY = "{\"response\":\"ERROR\",\"reason\":\"No such key\"}";
    public static final String ERROR_INCORRECT_JSON = "{\"response\":\"ERROR\",\"reason\":\"Incorrect JSON\"}";
    public static final String OK = "{\"response\":\"OK\"}";

    private final Path DB_PATH;
    private final JsonObject db;

    private final ReentrantReadWriteLock RReadWriteLock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock r = RReadWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock w = RReadWriteLock.writeLock();

    public JsonDatabase(String dbPath) throws IOException {
        this.DB_PATH = Path.of(dbPath);
        createDbIfNotExists(DB_PATH);
        this.db = readDbFromFile(DB_PATH).getAsJsonObject();
    }

    public String set(String kay, String value) throws IOException {
        w.lock();
        try {
            db.addProperty(kay, value);
            writeDbToFile(db, DB_PATH);
        } finally {
            w.unlock();
        }

        return OK;
    }

    public String get(String kay) {
        String result = null;

        r.lock();
        try {
            JsonElement value = db.get(kay);
            if (value != null) {
                result = value.getAsString();
            }
        } finally {
            r.unlock();
        }

        return result != null ? "{\"response\":\"OK\",\"value\":\"" + result + "\"}" : ERROR_NO_SUCH_KEY;
    }

    public String delete(String key) throws IOException {
        String result = ERROR_NO_SUCH_KEY;
        w.lock();
        try {
            if (db.remove(key) != null) {
                writeDbToFile(db, DB_PATH);
                result = OK;
            }
        } finally {
            w.unlock();
        }

        return result;
    }

    /**
     * input examples:
     * "{"type":"set","key":"10","value":"some data"}"
     * "{"type":"get","key":"10"}"
     * "{"type":"delete","key":"10"}"
     */
    public String executeJson(String json) throws IOException {
        JsonObject jo;
        String type;
        String key;
        String value = null;
        try {
            jo = JsonParser.parseString(json).getAsJsonObject();
            type = jo.get("type").getAsString();
            key = jo.get("key").getAsString();
            if (type.equals("set")) {
                value = jo.get("value").getAsString();
            }
        } catch (IllegalStateException | NullPointerException | JsonSyntaxException e) {
            return ERROR_INCORRECT_JSON;
        }
        if ((value == null && jo.size() != 2) || (value != null && jo.size() != 3)) {
            return ERROR_INCORRECT_JSON;
        }

        switch (type) {
            case "get": return get(key);
            case "set": return set(key, value);
            case "delete": return delete(key);
            default: return ERROR_INCORRECT_JSON;
        }
    }
}
