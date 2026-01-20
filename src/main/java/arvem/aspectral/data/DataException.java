package arvem.aspectral.data;

/**
 * Exception thrown when there's an error during data serialization/deserialization.
 * Part of AspectData library (inspired by Calio from Origins).
 */
public class DataException extends RuntimeException {

    private final Phase phase;
    private String path;

    public DataException(Phase phase, String path, Throwable cause) {
        super("Error during " + phase.name().toLowerCase() + " at path: " + path, cause);
        this.phase = phase;
        this.path = path;
    }

    public DataException(Phase phase, String path, String message) {
        super("Error during " + phase.name().toLowerCase() + " at path: " + path + " - " + message);
        this.phase = phase;
        this.path = path;
    }

    public DataException prepend(String prefix) {
        this.path = prefix + "." + this.path;
        return this;
    }

    public Phase getPhase() {
        return phase;
    }

    public String getPath() {
        return path;
    }

    public enum Phase {
        READING,
        WRITING,
        RECEIVING
    }
}
