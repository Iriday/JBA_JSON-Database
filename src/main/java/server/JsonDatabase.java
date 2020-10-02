package server;

import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Path;

import static server.JsonDbUtils.*;

public class JsonDatabase {
    public static final String ERROR_NO_SUCH_KEY = "{\"response\":\"ERROR\",\"reason\":\"No such key\"}";
    public static final String ERROR_INCORRECT_JSON = "{\"response\":\"ERROR\",\"reason\":\"Incorrect JSON\"}";
    public static final String OK = "{\"response\":\"OK\"}";

    private final Path DB_PATH;
    private final JsonObject db;

    public JsonDatabase(String dbPath) throws IOException {
        this.DB_PATH = Path.of(dbPath);
        createDbIfNotExists(DB_PATH);
        this.db = readDbFromFile(DB_PATH).getAsJsonObject();
    }

    public String set(String kay, String value) throws IOException {
        db.addProperty(kay, value);
        writeDbToFile(db, DB_PATH);
        return OK;
    }

    public String get(String kay) {
        JsonElement value = db.get(kay);
        return value != null ? "{\"response\":\"OK\",\"value\":\"" + value.getAsString() + "\"}" : ERROR_NO_SUCH_KEY;
    }

    public String delete(String kay) throws IOException {
        JsonElement oldElem = db.remove(kay);
        if (oldElem == null) {
            return ERROR_NO_SUCH_KEY;
        }
        writeDbToFile(db, DB_PATH);
        return OK;
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
