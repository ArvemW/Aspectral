package arvem.aspectral.util;

/**
 * Wrapper for command argument values.
 * Part of AspectData library (inspired by Calio from Origins).
 */
public class ArgumentWrapper<T> {

    private final T value;
    private final String rawArgument;

    public ArgumentWrapper(T value, String rawArgument) {
        this.value = value;
        this.rawArgument = rawArgument;
    }

    public T value() {
        return value;
    }

    public String rawArgument() {
        return rawArgument;
    }
}
