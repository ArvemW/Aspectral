package arvem.aspectral.component;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.api.LivingEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Component that holds powers for an entity.
 * Attached to entities that can have powers (typically players).
 * <p>
 * This replaces Minecraft's Cardinal Components and uses a simpler
 * entity-to-component mapping suitable for Hytale.
 */
public class PowerHolderComponent {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final LivingEntity entity;

    // Maps power type identifier to power instance
    private final Map<String, Power> powers = new ConcurrentHashMap<>();

    // Maps power type identifier to list of sources that granted it
    private final Map<String, Set<String>> powerSources = new ConcurrentHashMap<>();

    // Cached list of powers that need ticking
    private final List<Power> tickingPowers = new ArrayList<>();
    private boolean tickingDirty = true;

    public PowerHolderComponent(LivingEntity entity) {
        this.entity = entity;
    }

    /**
     * Add a power to this entity from a specific source.
     *
     * @param powerType The type of power to add
     * @param source The source granting this power (e.g., "origin:human")
     * @return True if the power was newly added
     */
    public boolean addPower(PowerType<?> powerType, String source) {
        String id = powerType.getIdentifier();

        // Track the source
        powerSources.computeIfAbsent(id, k -> new HashSet<>()).add(source);

        // If power already exists, just add the source
        if (powers.containsKey(id)) {
            return false;
        }

        // Create new power instance
        Power power = powerType.create(entity);
        powers.put(id, power);
        tickingDirty = true;

        power.onAdded(false);
        power.onGained();

        LOGGER.atFine().log("Added power %s to entity %s from source %s", id, entity, source);
        return true;
    }

    /**
     * Remove an power from a specific source.
     * The power is only fully removed when no sources remain.
     */
    public void removePower(PowerType<?> powerType, String source) {
        String id = powerType.getIdentifier();

        Set<String> sources = powerSources.get(id);
        if (sources == null) {
            return;
        }

        sources.remove(source);

        // If no sources remain, fully remove the power
        if (sources.isEmpty()) {
            powerSources.remove(id);
            Power power = powers.remove(id);
            if (power != null) {
                power.onLost();
                power.onRemoved(false);
                tickingDirty = true;
                LOGGER.atFine().log("Removed power %s from entity %s", id, entity);
            }
        }
    }

    /**
     * Add an power instance directly (used when creating from aspects).
     */
    public void addPower(Power power, String source) {
        String id = power.getType().getIdentifier();

        // Track the source
        powerSources.computeIfAbsent(id, k -> new HashSet<>()).add(source);

        // If power already exists, just add the source
        if (powers.containsKey(id)) {
            return;
        }

        // Store the power instance
        powers.put(id, power);
        tickingDirty = true;

        power.onAdded(false);
        power.onGained();

        LOGGER.atFine().log("Added power %s to entity %s from source %s", id, entity, source);
    }

    /**
     * Remove a power instance directly.
     */
    public void removePower(Power power, String source) {
        String id = power.getType().getIdentifier();

        Set<String> sources = powerSources.get(id);
        if (sources == null) {
            return;
        }

        sources.remove(source);

        // If no sources remain, fully remove the power
        if (sources.isEmpty()) {
            powerSources.remove(id);
            Power removed = powers.remove(id);
            if (removed != null) {
                removed.onLost();
                removed.onRemoved(false);
                tickingDirty = true;
                LOGGER.atFine().log("Removed power %s from entity %s", id, entity);
            }
        }
    }

    /**
     * Clear all powers.
     */
    public void clearAbilities() {
        for (Power power : new ArrayList<>(powers.values())) {
            power.onLost();
            power.onRemoved(false);
        }
        powers.clear();
        powerSources.clear();
        tickingDirty = true;
        LOGGER.atFine().log("Cleared all powers from entity %s", entity);
    }

    /**
     * Remove all powers granted by a specific source.
     *
     * @return The number of powers fully removed
     */
    public int removeAllAbilitiesFromSource(String source) {
        int removed = 0;

        for (String powerId : new ArrayList<>(powerSources.keySet())) {
            Set<String> sources = powerSources.get(powerId);
            if (sources != null && sources.remove(source)) {
                if (sources.isEmpty()) {
                    powerSources.remove(powerId);
                    Power power = powers.remove(powerId);
                    if (power != null) {
                        power.onLost();
                        power.onRemoved(false);
                        removed++;
                    }
                }
            }
        }

        if (removed > 0) {
            tickingDirty = true;
        }

        return removed;
    }

    /**
     * Get all power types granted by a specific source.
     */
    public List<String> getAbilitiesFromSource(String source) {
        return powerSources.entrySet().stream()
            .filter(e -> e.getValue().contains(source))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Check if this entity has a specific power type.
     */
    public boolean hasPower(PowerType<?> powerType) {
        return powers.containsKey(powerType.getIdentifier());
    }

    /**
     * Check if this entity has a specific power from a specific source.
     */
    public boolean hasPower(PowerType<?> powerType, String source) {
        Set<String> sources = powerSources.get(powerType.getIdentifier());
        return sources != null && sources.contains(source);
    }

    /**
     * Get an power instance by its type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Power> T getPower(PowerType<T> powerType) {
        return (T) powers.get(powerType.getIdentifier());
    }

    /**
     * Get all powers on this entity.
     */
    public List<Power> getAbilities() {
        return new ArrayList<>(powers.values());
    }

    /**
     * Get all power type identifiers on this entity.
     */
    public Set<String> getPowerTypeIds() {
        return new HashSet<>(powers.keySet());
    }

    /**
     * Get all powers of a specific class type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Power> List<T> getAbilities(Class<T> powerClass) {
        return getAbilities(powerClass, false);
    }

    /**
     * Get all powers of a specific class type.
     *
     * @param includeInactive Include powers that are currently inactive
     */
    @SuppressWarnings("unchecked")
    public <T extends Power> List<T> getAbilities(Class<T> powerClass, boolean includeInactive) {
        return powers.values().stream()
            .filter(powerClass::isInstance)
            .filter(a -> includeInactive || a.isActive())
            .map(a -> (T) a)
            .collect(Collectors.toList());
    }

    /**
     * Get all sources that granted a specific power.
     */
    public List<String> getSources(PowerType<?> powerType) {
        Set<String> sources = powerSources.get(powerType.getIdentifier());
        return sources != null ? new ArrayList<>(sources) : Collections.emptyList();
    }

    /**
     * Called each tick to update powers.
     */
    public void tick() {
        if (tickingDirty) {
            rebuildTickingList();
        }

        for (Power power : tickingPowers) {
            if (power.shouldTickWhenInactive() || power.isActive()) {
                power.tick();
            }
        }
    }

    private void rebuildTickingList() {
        tickingPowers.clear();
        for (Power power : powers.values()) {
            if (power.shouldTick()) {
                tickingPowers.add(power);
            }
        }
        tickingDirty = false;
    }

    /**
     * Called when the entity respawns.
     */
    public void onRespawn() {
        for (Power power : powers.values()) {
            power.onRespawn();
        }
    }

    /**
     * Dispatch key press event to all powers.
     */
    public void onKeyPressed(String key) {
        for (Power power : powers.values()) {
            if (power.isActive()) {
                power.onKeyPressed(key);
            }
        }
    }

    /**
     * Dispatch damage event to all powers.
     * @param event The damage event
     * @return true if any power cancelled the event
     */
    public boolean onDamage(Object event) {
        boolean cancelled = false;
        for (Power power : powers.values()) {
            if (power.isActive()) {
                if (power.onDamage(event)) {
                    cancelled = true;
                }
            }
        }
        return cancelled;
    }

    /**
     * Dispatch attack event to all powers.
     * @param event The attack event
     * @return true if any power cancelled the event
     */
    public boolean onAttack(Object event) {
        boolean cancelled = false;
        for (Power power : powers.values()) {
            if (power.isActive()) {
                if (power.onAttack(event)) {
                    cancelled = true;
                }
            }
        }
        return cancelled;
    }

    /**
     * Dispatch move event to all powers.
     * @param event The move event
     * @return true if any power cancelled the event
     */
    public boolean onMove(Object event) {
        boolean cancelled = false;
        for (Power power : powers.values()) {
            if (power.isActive()) {
                if (power.onMove(event)) {
                    cancelled = true;
                }
            }
        }
        return cancelled;
    }

    /**
     * Serialize this component to JSON for persistence.
     */
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonArray abilitiesArray = new JsonArray();

        for (Map.Entry<String, Power> entry : powers.entrySet()) {
            JsonObject powerJson = new JsonObject();
            powerJson.addProperty("id", entry.getKey());

            JsonArray sourcesArray = new JsonArray();
            Set<String> sources = powerSources.get(entry.getKey());
            if (sources != null) {
                sources.forEach(sourcesArray::add);
            }
            powerJson.add("sources", sourcesArray);

            powerJson.add("data", entry.getValue().toJson());
            abilitiesArray.add(powerJson);
        }

        root.add("powers", abilitiesArray);
        return root;
    }

    /**
     * Deserialize this component from JSON.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void fromJson(JsonObject json) {
        // Clear existing
        powers.clear();
        powerSources.clear();
        tickingDirty = true;

        if (!json.has("powers")) {
            return;
        }

        JsonArray abilitiesArray = json.getAsJsonArray("powers");
        for (JsonElement elem : abilitiesArray) {
            JsonObject powerJson = elem.getAsJsonObject();
            String id = powerJson.get("id").getAsString();

            // Restore sources
            JsonArray sourcesArray = powerJson.getAsJsonArray("sources");
            Set<String> sources = new HashSet<>();
            for (JsonElement sourceElem : sourcesArray) {
                sources.add(sourceElem.getAsString());
            }
            powerSources.put(id, sources);

            // First try to get a registered PowerType
            PowerType<?> type = AspectPowers.getInstance()
                .getPowerRegistry()
                .getPowerType(id);

            // If not found, try to create from factory
            if (type == null) {
                var factory = AspectPowers.getInstance()
                    .getPowerRegistry()
                    .getPowerFactory(id);

                if (factory != null) {
                    // Create PowerType from factory with default values
                    var factoryInstance = factory.createDefault();
                    type = new PowerType(id, factoryInstance);
                }
            }

            if (type != null) {
                Power power = type.create(entity);
                if (powerJson.has("data")) {
                    power.fromJson(powerJson.getAsJsonObject("data"));
                }
                powers.put(id, power);
                power.onAdded(true);
            } else {
                LOGGER.atWarning().log("Unknown power type during load: %s", id);
            }
        }
    }

    public LivingEntity getEntity() {
        return entity;
    }

    // ========================================
    // Static access methods
    // ========================================

    /**
     * Get the power holder component for an entity.
     */
    public static PowerHolderComponent get(LivingEntity entity) {
        if (AspectPowers.getInstance() == null) {
            return null;
        }
        return AspectPowers.getInstance().getComponentManager().get(entity);
    }

    /**
     * Get or create the power holder component for an entity.
     */
    public static PowerHolderComponent getOrCreate(LivingEntity entity) {
        return AspectPowers.getInstance().getComponentManager().getOrCreate(entity);
    }

    // ========================================
    // Manager class for component lifecycle
    // ========================================

    /**
     * Manages power holder components for all entities.
     * Uses UUID for players to persist across reconnects.
     */
    public static class Manager {

        // Use String keys - UUID string for players, entity ID for non-players
        private final Map<String, PowerHolderComponent> components = new ConcurrentHashMap<>();

        /**
         * Get the component for an entity, or null if none exists.
         */
        public PowerHolderComponent get(LivingEntity entity) {
            if (entity == null) return null;
            return components.get(getEntityKey(entity));
        }

        /**
         * Get or create a component for an entity.
         */
        public PowerHolderComponent getOrCreate(LivingEntity entity) {
            if (entity == null) return null;
            return components.computeIfAbsent(getEntityKey(entity),
                key -> new PowerHolderComponent(entity));
        }

        /**
         * Get a component by UUID (for loading saved data).
         */
        public PowerHolderComponent getByUuid(java.util.UUID uuid) {
            if (uuid == null) return null;
            return components.get(uuid.toString());
        }

        /**
         * Store a component with a specific UUID key.
         */
        public void put(java.util.UUID uuid, PowerHolderComponent component) {
            if (uuid != null && component != null) {
                components.put(uuid.toString(), component);
            }
        }

        /**
         * Remove the component for an entity (e.g., on disconnect).
         */
        public void remove(LivingEntity entity) {
            if (entity == null) return;
            components.remove(getEntityKey(entity));
        }

        /**
         * Remove a component by UUID.
         */
        public void removeByUuid(java.util.UUID uuid) {
            if (uuid != null) {
                components.remove(uuid.toString());
            }
        }

        /**
         * Tick all components.
         */
        public void tickAll() {
            for (PowerHolderComponent component : components.values()) {
                component.tick();
            }
        }

        /**
         * Get all tracked components.
         */
        public Collection<PowerHolderComponent> getAll() {
            return components.values();
        }

        /**
         * Get the key for an entity - UUID for players, entity ID for others.
         */
        private String getEntityKey(LivingEntity entity) {
            if (entity instanceof arvem.aspectral.api.HytalePlayerAdapter playerAdapter) {
                java.util.UUID uuid = playerAdapter.getUuid();
                if (uuid != null) {
                    return uuid.toString();
                }
            }
            // Fallback to entity ID for non-players
            return "entity:" + entity.getEntityId();
        }
    }
}



