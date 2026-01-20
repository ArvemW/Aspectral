package arvem.aspectral.util;

import arvem.aspectral.data.SerializableData;

/**
 * Dynamic identifier that supports namespace resolution.
 * Part of AspectData library (inspired by Calio from Origins).
 * Uses String keys compatible with Hytale.
 */
public class DynamicIdentifier {

    public static final String DEFAULT_NAMESPACE = "hytale";

    public static String of(String id, String defaultNamespace) {
        // Handle wildcard namespace replacement
        if (id.startsWith("*:")) {
            String actualNamespace = SerializableData.CURRENT_NAMESPACE != null
                ? SerializableData.CURRENT_NAMESPACE
                : defaultNamespace;
            return actualNamespace + ":" + id.substring(2);
        }

        // Handle default namespace
        if (!id.contains(":")) {
            return defaultNamespace + ":" + id;
        }

        return id;
    }

    public static String of(String id) {
        return of(id, DEFAULT_NAMESPACE);
    }
}
