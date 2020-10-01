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

    public static String formatGet(String v) {
        return "{\"response\":\"OK\",\"value\":\"" + v + "\"}";
    }
}
