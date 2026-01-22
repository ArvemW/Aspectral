package arvem.aspectral.layer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for aspect layers.
 * <p>
 * Layers allow multiple aspect "slots" - for example:
 * - Layer "origin" = main aspect (Skywalker, Human, etc.)
 * - Layer "class" = combat class (Warrior, Mage, etc.)
 * - Layer "subrace" = additional traits
 * <p>
 * Each layer can have one aspect assigned at a time.
 * This allows plugins to add their own layer systems without conflicts.
 */
public class LayerRegistry {
    private static LayerRegistry instance;

    private final Map<String, Layer> layers = new ConcurrentHashMap<>();
    private final List<Layer> sortedLayers = new ArrayList<>();

    public LayerRegistry() {
    }

    public static LayerRegistry getInstance() {
        if (instance == null) {
            instance = new LayerRegistry();
        }
        return instance;
    }

    /**
     * Register a layer.
     */
    public void register(Layer layer) {
        layers.put(layer.getId(), layer);
        rebuildSortedList();
    }

    /**
     * Get a layer by ID.
     */
    public Layer getLayer(String id) {
        return layers.get(id);
    }

    /**
     * Get all layers sorted by order.
     */
    public List<Layer> getAllLayers() {
        return new ArrayList<>(sortedLayers);
    }

    /**
     * Get all enabled layers sorted by order.
     */
    public List<Layer> getEnabledLayers() {
        return sortedLayers.stream()
                .filter(Layer::isEnabled)
                .filter(layer -> !layer.isHidden())
                .toList();
    }

    /**
     * Check if a layer exists.
     */
    public boolean hasLayer(String id) {
        return layers.containsKey(id);
    }

    /**
     * Get the number of registered layers.
     */
    public int size() {
        return layers.size();
    }

    /**
     * Clear all layers (for testing).
     */
    public void clear() {
        layers.clear();
        sortedLayers.clear();
    }

    private void rebuildSortedList() {
        sortedLayers.clear();
        sortedLayers.addAll(layers.values());
        sortedLayers.sort(Comparator.comparingInt(Layer::getOrder));
    }
}


