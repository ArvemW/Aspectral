package arvem.aspectral.aspect;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for all Aspects.
 * Aspects are loaded from JSON files and registered here.
 */
public class AspectRegistry {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Map<String, Aspect> aspects = new ConcurrentHashMap<>();

    /**
     * Register an aspect.
     */
    public void register(Aspect aspect) {
        String id = aspect.getIdentifier();
        LOGGER.atInfo().log("AspectRegistry.register called for: %s", id);
        LOGGER.atInfo().log("AspectRegistry instance hash: %d", System.identityHashCode(this));
        if (aspects.containsKey(id)) {
            LOGGER.atWarning().log("Overwriting existing aspect: %s", id);
        }
        aspects.put(id, aspect);
        LOGGER.atInfo().log("Registered aspect: %s with %d powers", id, aspect.getPowerCount());
        LOGGER.atInfo().log("Registry size now: %d", aspects.size());
    }

    /**
     * Get an aspect by identifier.
     */
    public Aspect get(String identifier) {
        return aspects.get(identifier);
    }

    /**
     * Check if an aspect exists.
     */
    public boolean has(String identifier) {
        return aspects.containsKey(identifier);
    }

    /**
     * Get all registered aspects.
     */
    public Collection<Aspect> getAll() {
        return Collections.unmodifiableCollection(aspects.values());
    }

    /**
     * Get all aspect identifiers.
     */
    public Collection<String> getAllIds() {
        return Collections.unmodifiableCollection(aspects.keySet());
    }

    /**
     * Clear all registered aspects.
     */
    public void clear() {
        aspects.clear();
    }

    /**
     * Get the count of registered aspects.
     */
    public int size() {
        return aspects.size();
    }
}


