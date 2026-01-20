package arvem.aspectral.util;

/**
 * Utility class for class casting operations.
 * Part of AspectData library (inspired by Calio from Origins).
 */
public class ClassUtil {

    @SuppressWarnings("unchecked")
    public static <T> Class<T> castClass(Class<?> clazz) {
        return (Class<T>) clazz;
    }
}
