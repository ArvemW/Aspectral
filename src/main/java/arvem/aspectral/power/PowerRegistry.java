package arvem.aspectral.power;

import arvem.aspectral.powers.PowerDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for power (power) definitions.
 * <p>
 * Powers are reusable power definitions that can be referenced by multiple aspects.
 * This allows for shared powers without duplication.
 */
public class PowerRegistry {
    private static PowerRegistry instance;

    private final Map<String, PowerDefinition> powers = new ConcurrentHashMap<>();

    private PowerRegistry() {
    }

    public static PowerRegistry getInstance() {
        if (instance == null) {
            instance = new PowerRegistry();
        }
        return instance;
    }

    /**
     * Register a power definition.
     */
    public void register(String powerId, PowerDefinition definition) {
        powers.put(powerId, definition);
    }

    /**
     * Get a power definition by ID.
     */
    public PowerDefinition getPower(String powerId) {
        return powers.get(powerId);
    }

    /**
     * Check if a power exists.
     */
    public boolean hasPower(String powerId) {
        return powers.containsKey(powerId);
    }

    /**
     * Get all registered power IDs.
     */
    public java.util.Set<String> getAllPowerIds() {
        return powers.keySet();
    }

    /**
     * Get the number of registered powers.
     */
    public int size() {
        return powers.size();
    }

    /**
     * Clear all powers (for testing).
     */
    public void clear() {
        powers.clear();
    }
}


