package arvem.aspectral.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Utility for working with JSON objects.
 * Ported from Minecraft's JsonHelper for use with Hytale.
 */
public class JsonHelper {

    public static int getInt(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        JsonElement element = json.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Field " + key + " must be a number");
        }
        return element.getAsInt();
    }

    public static int getInt(JsonObject json, String key, int defaultValue) {
        if (!json.has(key)) {
            return defaultValue;
        }
        return getInt(json, key);
    }

    public static String getString(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        JsonElement element = json.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Field " + key + " must be a string");
        }
        return element.getAsString();
    }

    public static String getString(JsonObject json, String key, String defaultValue) {
        if (!json.has(key)) {
            return defaultValue;
        }
        return getString(json, key);
    }

    public static boolean getBoolean(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        JsonElement element = json.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Field " + key + " must be a boolean");
        }
        return element.getAsBoolean();
    }

    public static boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        if (!json.has(key)) {
            return defaultValue;
        }
        return getBoolean(json, key);
    }
}
