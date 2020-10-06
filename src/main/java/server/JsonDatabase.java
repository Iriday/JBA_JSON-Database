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

    public String set(String key, String value) throws IOException {
        String[] keys = JsonDbUtils.keyToKeys(key);
        JsonObject temp = db;

        w.lock();
        try {
            for (int i = 0; i < keys.length - 1; i++) {
                if (!temp.has(keys[i]) || !temp.get(keys[i]).isJsonObject()) {
                    temp.add(keys[i], new JsonObject());
                }
                temp = temp.get(keys[i]).getAsJsonObject();
            }
            if (value.startsWith("{") && value.endsWith("}")) {
                temp.add(keys[keys.length - 1], JsonParser.parseString(value));
            } else {
                temp.addProperty(keys[keys.length - 1], value);
            }
            writeDbToFile(db, DB_PATH);
        } finally {
            w.unlock();
        }

        return OK;
    }

    public String get(String key) {
        String[] keys = JsonDbUtils.keyToKeys(key);
        JsonObject temp = db;

        r.lock();
        try {
            for (int i = 0; i < keys.length - 1; i++) {
                JsonElement je = temp.get(keys[i]);
                if (je == null) {
                    return ERROR_NO_SUCH_KEY;
                }
                temp = je.getAsJsonObject();
            }

            JsonElement value = temp.get(keys[keys.length - 1]);
            return value != null ? "{\"response\":\"OK\",\"value\":" + value.toString() + "}" : ERROR_NO_SUCH_KEY;
        } finally {
            r.unlock();
        }
    }

    public String delete(String key) throws IOException {
        String[] keys = JsonDbUtils.keyToKeys(key);
        JsonObject temp = db;

        w.lock();
        try {
            for (int i = 0; i < keys.length - 1; i++) {
                JsonElement je = temp.get(keys[i]);
                if (je == null) {
                    return ERROR_NO_SUCH_KEY;
                }
                temp = je.getAsJsonObject();
            }

            if (temp.remove(keys[keys.length - 1]) != null) {
                writeDbToFile(db, DB_PATH);
                return OK;
            }
        } finally {
            w.unlock();
        }
        return ERROR_NO_SUCH_KEY;
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
            JsonElement k = jo.get("key");
            key = k.isJsonArray() ? k.getAsJsonArray().toString() : k.getAsString();
            if (type.equals("set")) {
                JsonElement v = jo.get("value");
                if (v.isJsonObject()) {
                    value = v.getAsJsonObject().toString();
                } else if (v.isJsonArray()) {
                    value = v.getAsJsonArray().toString();
                } else {
                    value = v.getAsString();
                }
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
