package server;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static server.JsonDatabase.*;

import java.lang.reflect.*;

public class TestJsonDatabase {
    @Test
    public void testSetGetDelete() throws NoSuchFieldException, IllegalAccessException {
        final JsonDatabase db = new JsonDatabase();

        assertEquals(db.set("key0", "v0"), OK);
        assertEquals(db.get("key0"), formatGet("v0"));

        assertEquals(db.set("key0", "v1"), OK);
        assertEquals(db.set("key0", "v1"), OK);
        assertEquals(db.get("key0"), formatGet("v1"));

        assertEquals(db.set("key 1", "v with spaces"), OK);
        assertEquals(db.get("key 1"), formatGet("v with spaces"));

        assertEquals(db.get("key999"), ERROR_NO_SUCH_KEY);
        assertEquals(db.delete("key999"), ERROR_NO_SUCH_KEY);

        assertEquals(db.delete("key0"), OK);
        assertEquals(db.get("key0"), ERROR_NO_SUCH_KEY);
        assertEquals(db.delete("key0"), ERROR_NO_SUCH_KEY);

        assertEquals(db.get("key 1"), formatGet("v with spaces"));

        // use reflection to get private final field "data"
        Field field = JsonDatabase.class.getDeclaredField("data");
        field.setAccessible(true);
        JsonObject jsonObject = (JsonObject) field.get(db);
        // add some more data before test
        db.set("key 1000", "one thousand");
        db.set("key num unknown", "value is unknown too");

//        System.out.println(field.getName());
//        System.out.println(field.getModifiers());
//        System.out.println(field.getType());

        assertEquals(jsonObject.toString(), "{\"key 1\":\"v with spaces\",\"key 1000\":\"one thousand\",\"key num unknown\":\"value is unknown too\"}");

        System.out.println("Db data after tests:\n" + field.get(db));
    }

    @Test
    public void testExecuteJson() {
        final JsonDatabase db = new JsonDatabase();

        assertEquals(db.executeJson(toJson("type", "get", "key", "qwerty")), ERROR_NO_SUCH_KEY);
        assertEquals(db.executeJson(toJson("type", "set", "key", "key a", "value", "the first value")), OK);
        assertEquals(db.executeJson(toJson("type", "set", "key", "key a", "value", "the first value")), OK);
        assertEquals(db.executeJson(toJson("type", "get", "key", "key a")), toJson("response", "OK", "value", "the first value"));
        assertEquals(db.executeJson(toJson("type", "delete", "key", "key a")), OK);
        assertEquals(db.executeJson(toJson("type", "delete", "key", "key a")), ERROR_NO_SUCH_KEY);

        assertEquals(db.executeJson(""), ERROR_INCORRECT_JSON);
        assertEquals(db.executeJson(null), ERROR_INCORRECT_JSON);
        assertEquals(db.executeJson("asdf"), ERROR_INCORRECT_JSON);
        assertEquals(db.executeJson("{a:}"), ERROR_INCORRECT_JSON);
        assertEquals(db.executeJson("{"), ERROR_INCORRECT_JSON);
        assertEquals(db.executeJson(toJson("type", "get")), ERROR_INCORRECT_JSON);
        assertEquals(db.executeJson(toJson("type", "get", "key", "999", "value", "abc")), ERROR_INCORRECT_JSON);
        assertEquals(db.executeJson(toJson("type", "unknown", "key", "1")), ERROR_INCORRECT_JSON);
    }

    public static String formatGet(String v) {
        return "{\"response\":\"OK\",\"value\":\"" + v + "\"}";
    }

    /**
     * Example: input: "key1", "value1", "key2", "value2"; output: {"key1":"value1","key2":"value2"}
     */
    public static String toJson(String... args) {
        var jo = new JsonObject();
        for (int i = 0; i < args.length; i += 2) {
            jo.addProperty(args[i], args[i + 1]);
        }
        return jo.toString();
    }
}
