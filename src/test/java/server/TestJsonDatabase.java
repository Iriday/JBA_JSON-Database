package server;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.JsonDatabase.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestJsonDatabase {
    public final static String TEST_DB_PATH = "src/main/java/server/data/test_db.json";
    private JsonDatabase db;

    @BeforeEach
    public void reinitializeDb() throws IOException {
        deleteFileIfExists(Path.of(TEST_DB_PATH));
        db = new JsonDatabase(TEST_DB_PATH);
    }

    @AfterAll
    public static void deleteDbFile() throws IOException {
        deleteFileIfExists(Path.of(TEST_DB_PATH));
    }

    @Test
    public void testSetGetDelete_simpleKeys() throws NoSuchFieldException, IllegalAccessException, IOException {

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


        // add some more data before test
        db.set("key 1000", "one thousand");
        db.set("key num unknown", "value is unknown too");
        assertTrue(compareDbFileDbFieldAndExpectedResult(db, Path.of(TEST_DB_PATH), "{\"key 1\":\"v with spaces\",\"key 1000\":\"one thousand\",\"key num unknown\":\"value is unknown too\"}"));
    }

    @Test
    public void testSetGetDelete_complexKeys() throws IOException, NoSuchFieldException, IllegalAccessException {
        assertEquals(db.get("unknown value"), ERROR_NO_SUCH_KEY);
        assertEquals(db.delete("unknown value"), ERROR_NO_SUCH_KEY);
        assertEquals(db.delete("[unknown, value]"), ERROR_NO_SUCH_KEY);

        assertEquals(db.set("[\"one\", \"two\", three]", "3"), OK);
        assertEquals(db.get("[one, two, three]"), formatGet("3"));
        assertEquals(db.set("[1,2,3]", "three"), OK);
        assertEquals(db.get("[1,2,3]"), formatGet("three"));

        assertEquals(db.get("[one]"), formatGet2("{\"two\":{\"three\":\"3\"}}"));

        assertEquals(db.set("[one]", "just one"), OK);
        assertEquals(db.get("one"), formatGet("just one"));

        assertEquals(db.set("[1, 2, 4]", "four"), OK);
        assertEquals(db.get("[1,2,4]"), formatGet("four"));
        assertEquals(db.get("[1]"), formatGet2("{\"2\":{\"3\":\"three\",\"4\":\"four\"}}"));

        assertEquals(db.set("temp", "{v1:1v, v2: 2v}"), OK);
        assertEquals(db.get("temp"), formatGet2("{\"v1\":\"1v\",\"v2\":\"2v\"}"));

        assertEquals(db.set("one", "{\"first\": \"f\",\"second\":\"s\"}"), OK);
        assertEquals(db.get("[\"one\"]"), formatGet2("{\"first\":\"f\",\"second\":\"s\"}"));

        assertEquals(db.set("[temp,v2]", "{\"inner1\":{\"inner2\"={\"item\"=\"2v\"}}}"), OK);
        assertEquals(db.get("[temp, v2,  inner1,inner2,item]"), formatGet("2v"));

        // compare whole db
        assertEquals(Files.readString(Path.of(TEST_DB_PATH)), "{\"one\":{\"first\":\"f\",\"second\":\"s\"},\"1\":{\"2\":{\"3\":\"three\",\"4\":\"four\"}},\"temp\":{\"v1\":\"1v\",\"v2\":{\"inner1\":{\"inner2\":{\"item\":\"2v\"}}}}}");

        assertEquals(db.delete("[1,2,5]"), ERROR_NO_SUCH_KEY);
        assertEquals(db.delete("[1,2,4]"), OK);
        assertEquals(db.get("[1,2]"), formatGet2("{\"3\":\"three\"}"));
        assertEquals(db.delete("[1,2,4]"), ERROR_NO_SUCH_KEY);
        assertEquals(db.delete("[1,2,4]"), ERROR_NO_SUCH_KEY);

        assertEquals(db.delete("[temp,v2]"), OK);
        assertEquals(db.delete("[temp,v2]"), ERROR_NO_SUCH_KEY);
        assertEquals(db.get("temp"), formatGet2("{\"v1\":\"1v\"}"));

        assertEquals(db.delete("temp"), OK);
        assertEquals(db.delete("temp"), ERROR_NO_SUCH_KEY);

        assertTrue(compareDbFileDbFieldAndExpectedResult(db, Path.of(TEST_DB_PATH), "{\"one\":{\"first\":\"f\",\"second\":\"s\"},\"1\":{\"2\":{\"3\":\"three\"}}}"));
    }

    @Test
    public void testExecuteJson_simpleKeys() throws IOException {

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

    @Test
    public void testExecuteJson_complexKeys() throws IOException, NoSuchFieldException, IllegalAccessException {
        assertEquals(db.executeJson(toJson("type", "set", "key", "[1,2,3]", "value", "val")), OK);
        assertEquals(db.executeJson(toJson("type", "get", "key", "[1,\"2\",3]")), toJson("response", "OK", "value", "val"));
        assertEquals(db.executeJson(toJson("type", "get", "key", "1")), formatGet2("{\"2\":{\"3\":\"val\"}}"));

        assertEquals(db.executeJson("{\"type\":\"set\",\"key\":[one,two,three],\"value\":{\"v\":\"2\"}}"), OK);
        assertEquals(db.executeJson(toJson("type", "get", "key", "[one,two,three]")), formatGet2("{\"v\":\"2\"}"));

        assertEquals(db.executeJson("{\"type\":\"set\",\"key\":[one,two,three],\"value\":{\"v\":2}}"), OK);
        assertEquals(db.executeJson(toJson("type", "get", "key", "[one,two,three]")), formatGet2("{\"v\":2}"));

        assertEquals(db.executeJson("{\"type\":\"set\",\"key\":[one,two,three,four,five],\"value\":{\"vm\":\"2m\"}}"), OK);
        assertEquals(db.executeJson("{\"type\":\"get\",\"key\":[one,two,three,four,five, vm]}"), formatGet("2m"));

        assertEquals(db.executeJson("{\"type\":\"set\",\"key\":[one,two,three,v],\"value\":{\"v_moved\":2}}"), OK);
        assertEquals(db.executeJson("{\"type\":\"get\",\"key\":[one,two,three,v,v_moved]}"), formatGet2("2"));

        assertEquals(db.executeJson("{\"type\":\"set\",\"key\":[a,b],\"value\":3}"), OK); //----------------------------------------fix // db contains "3" not 3
        assertEquals(db.executeJson("{\"type\":\"get\",\"key\":[a,b]}"), formatGet("3"));

        assertEquals(db.executeJson("{\"type\":\"set\",\"key\":[a,b],\"value\":{\"c\":\"C\"}}"), OK);
        assertEquals(db.executeJson("{\"type\":\"get\",\"key\":[a,b,c]}"), formatGet("C"));

        assertEquals(db.executeJson("{\"type\":\"set\",\"key\":[a,b,c,d,e],\"value\":{\"f\":\"F\"}}"), OK);
        assertEquals(db.executeJson("{\"type\":\"get\",\"key\":[a,b,c,d,e,f]}"), formatGet("F"));


        assertTrue(compareDbFileDbFieldAndExpectedResult(db, Path.of(TEST_DB_PATH), "{\"1\":{\"2\":{\"3\":\"val\"}},\"one\":{\"two\":{\"three\":{\"v\":{\"v_moved\":2},\"four\":{\"five\":{\"vm\":\"2m\"}}}}},\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":\"F\"}}}}}}"));
    }

    public static boolean compareDbFileDbFieldAndExpectedResult(JsonDatabase db, Path db_path, String expectedResult) throws IOException, NoSuchFieldException, IllegalAccessException {
        if (!Files.readString(db_path).equals(expectedResult)) {
            return false;
        }
        // use reflection to get private final field "db"
        Field db_field = JsonDatabase.class.getDeclaredField("db");
        db_field.setAccessible(true);
        return ((JsonObject) db_field.get(db)).toString().equals(expectedResult);
    }

    public static String formatGet(String v) {
        return "{\"response\":\"OK\",\"value\":\"" + v + "\"}";
    }

    public static String formatGet2(String v) {
        return "{\"response\":\"OK\",\"value\":" + v + "}";
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

    public static void deleteFileIfExists(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
}
