package server;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonDbUtils {

    @Test
    public void testKeyToKeys() {
        String keys = Arrays.toString(new String[]{"1", "2", "3"});

        assertEquals(Arrays.toString(JsonDbUtils.keyToKeys("[1,2,3]")), keys);
        assertEquals(Arrays.toString(JsonDbUtils.keyToKeys("[\"1\",2,3]")), keys);
        assertEquals(Arrays.toString(JsonDbUtils.keyToKeys("[1, 2,  3]")), keys);

        assertEquals(Arrays.toString(JsonDbUtils.keyToKeys("2")), "[2]");
        assertEquals(Arrays.toString(JsonDbUtils.keyToKeys("\"2\"")), "[2]");
    }
}
