package server;

import com.google.gson.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonDbUtils {
    public static JsonElement readDbFromFile(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        }
    }

    public static void writeDbToFile(JsonObject db, Path path) throws IOException { // synchronize db
        try (Writer writer = Files.newBufferedWriter(path)) {
            new Gson().toJson(db, writer);
        }
    }

    public static void createDbIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.writeString(path, "{}");
        }
    }

    public static String[] keyToKeys(String key) {
        if (key.startsWith("[") && key.endsWith("]")) {
            return new Gson().fromJson(JsonParser.parseString(key).getAsJsonArray(), String[].class);
        }
        if (key.startsWith("\"") && key.endsWith("\"")) {
            key = key.substring(1, key.length() - 1);
        }
        return new String[]{key};
    }
}
