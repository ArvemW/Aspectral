package arvem.aspectral.component;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.api.LivingEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Component that holds abilities for an entity.
 * Attached to entities that can have abilities (typically players).
 * <p>
 * This replaces Minecraft's Cardinal Components and uses a simpler
 * entity-to-component mapping suitable for Hytale.
 */
public class AbilityHolderComponent {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final LivingEntity entity;

    // Maps ability type identifier to ability instance
    private final Map<String, Ability> abilities = new ConcurrentHashMap<>();

    // Maps ability type identifier to list of sources that granted it
    private final Map<String, Set<String>> abilitySources = new ConcurrentHashMap<>();

    // Cached list of abilities that need ticking
    private final List<Ability> tickingAbilities = new ArrayList<>();
    private boolean tickingDirty = true;

    public AbilityHolderComponent(LivingEntity entity) {
        this.entity = entity;
    }

    /**
     * Add an ability to this entity from a specific source.
     *
     * @param abilityType The type of ability to add
     * @param source The source granting this ability (e.g., "origin:human")
     * @return True if the ability was newly added
     */
    public boolean addAbility(AbilityType<?> abilityType, String source) {
        String id = abilityType.getIdentifier();

        // Track the source
        abilitySources.computeIfAbsent(id, k -> new HashSet<>()).add(source);

        // If ability already exists, just add the source
        if (abilities.containsKey(id)) {
            return false;
        }

        // Create new ability instance
        Ability ability = abilityType.create(entity);
        abilities.put(id, ability);
        tickingDirty = true;

        ability.onAdded(false);
        ability.onGained();

        LOGGER.atFine().log("Added ability %s to entity %s from source %s", id, entity, source);
        return true;
    }

    /**
     * Remove an ability from a specific source.
     * The ability is only fully removed when no sources remain.
     */
    public void removeAbility(AbilityType<?> abilityType, String source) {
        String id = abilityType.getIdentifier();

        Set<String> sources = abilitySources.get(id);
        if (sources == null) {
            return;
        }

        sources.remove(source);

        // If no sources remain, fully remove the ability
        if (sources.isEmpty()) {
            abilitySources.remove(id);
            Ability ability = abilities.remove(id);
            if (ability != null) {
                ability.onLost();
                ability.onRemoved(false);
                tickingDirty = true;
                LOGGER.atFine().log("Removed ability %s from entity %s", id, entity);
            }
        }
    }

    /**
     * Remove all abilities granted by a specific source.
     *
     * @return The number of abilities fully removed
     */
    public int removeAllAbilitiesFromSource(String source) {
        int removed = 0;

        for (String abilityId : new ArrayList<>(abilitySources.keySet())) {
            Set<String> sources = abilitySources.get(abilityId);
            if (sources != null && sources.remove(source)) {
                if (sources.isEmpty()) {
                    abilitySources.remove(abilityId);
                    Ability ability = abilities.remove(abilityId);
                    if (ability != null) {
                        ability.onLost();
                        ability.onRemoved(false);
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
     * Get all ability types granted by a specific source.
     */
    public List<String> getAbilitiesFromSource(String source) {
        return abilitySources.entrySet().stream()
            .filter(e -> e.getValue().contains(source))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Check if this entity has a specific ability type.
     */
    public boolean hasAbility(AbilityType<?> abilityType) {
        return abilities.containsKey(abilityType.getIdentifier());
    }

    /**
     * Check if this entity has a specific ability from a specific source.
     */
    public boolean hasAbility(AbilityType<?> abilityType, String source) {
        Set<String> sources = abilitySources.get(abilityType.getIdentifier());
        return sources != null && sources.contains(source);
    }

    /**
     * Get an ability instance by its type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Ability> T getAbility(AbilityType<T> abilityType) {
        return (T) abilities.get(abilityType.getIdentifier());
    }

    /**
     * Get all abilities on this entity.
     */
    public List<Ability> getAbilities() {
        return new ArrayList<>(abilities.values());
    }

    /**
     * Get all ability type identifiers on this entity.
     */
    public Set<String> getAbilityTypeIds() {
        return new HashSet<>(abilities.keySet());
    }

    /**
     * Get all abilities of a specific class type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Ability> List<T> getAbilities(Class<T> abilityClass) {
        return getAbilities(abilityClass, false);
    }

    /**
     * Get all abilities of a specific class type.
     *
     * @param includeInactive Include abilities that are currently inactive
     */
    @SuppressWarnings("unchecked")
    public <T extends Ability> List<T> getAbilities(Class<T> abilityClass, boolean includeInactive) {
        return abilities.values().stream()
            .filter(abilityClass::isInstance)
            .filter(a -> includeInactive || a.isActive())
            .map(a -> (T) a)
            .collect(Collectors.toList());
    }

    /**
     * Get all sources that granted a specific ability.
     */
    public List<String> getSources(AbilityType<?> abilityType) {
        Set<String> sources = abilitySources.get(abilityType.getIdentifier());
        return sources != null ? new ArrayList<>(sources) : Collections.emptyList();
    }

    /**
     * Called each tick to update abilities.
     */
    public void tick() {
        if (tickingDirty) {
            rebuildTickingList();
        }

        for (Ability ability : tickingAbilities) {
            if (ability.shouldTickWhenInactive() || ability.isActive()) {
                ability.tick();
            }
        }
    }

    private void rebuildTickingList() {
        tickingAbilities.clear();
        for (Ability ability : abilities.values()) {
            if (ability.shouldTick()) {
                tickingAbilities.add(ability);
            }
        }
        tickingDirty = false;
    }

    /**
     * Called when the entity respawns.
     */
    public void onRespawn() {
        for (Ability ability : abilities.values()) {
            ability.onRespawn();
        }
    }

    /**
     * Serialize this component to JSON for persistence.
     */
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonArray abilitiesArray = new JsonArray();

        for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
            JsonObject abilityJson = new JsonObject();
            abilityJson.addProperty("id", entry.getKey());

            JsonArray sourcesArray = new JsonArray();
            Set<String> sources = abilitySources.get(entry.getKey());
            if (sources != null) {
                sources.forEach(sourcesArray::add);
            }
            abilityJson.add("sources", sourcesArray);

            abilityJson.add("data", entry.getValue().toJson());
            abilitiesArray.add(abilityJson);
        }

        root.add("abilities", abilitiesArray);
        return root;
    }

    /**
     * Deserialize this component from JSON.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void fromJson(JsonObject json) {
        // Clear existing
        abilities.clear();
        abilitySources.clear();
        tickingDirty = true;

        if (!json.has("abilities")) {
            return;
        }

        JsonArray abilitiesArray = json.getAsJsonArray("abilities");
        for (JsonElement elem : abilitiesArray) {
            JsonObject abilityJson = elem.getAsJsonObject();
            String id = abilityJson.get("id").getAsString();

            // Restore sources
            JsonArray sourcesArray = abilityJson.getAsJsonArray("sources");
            Set<String> sources = new HashSet<>();
            for (JsonElement sourceElem : sourcesArray) {
                sources.add(sourceElem.getAsString());
            }
            abilitySources.put(id, sources);

            // First try to get a registered AbilityType
            AbilityType<?> type = AspectAbilities.getInstance()
                .getAbilityRegistry()
                .getAbilityType(id);

            // If not found, try to create from factory
            if (type == null) {
                var factory = AspectAbilities.getInstance()
                    .getAbilityRegistry()
                    .getAbilityFactory(id);

                if (factory != null) {
                    // Create AbilityType from factory with default values
                    var factoryInstance = factory.createDefault();
                    type = new AbilityType(id, factoryInstance);
                }
            }

            if (type != null) {
                Ability ability = type.create(entity);
                if (abilityJson.has("data")) {
                    ability.fromJson(abilityJson.getAsJsonObject("data"));
                }
                abilities.put(id, ability);
                ability.onAdded(true);
            } else {
                LOGGER.atWarning().log("Unknown ability type during load: %s", id);
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
     * Get the ability holder component for an entity.
     */
    public static AbilityHolderComponent get(LivingEntity entity) {
        if (AspectAbilities.getInstance() == null) {
            return null;
        }
        return AspectAbilities.getInstance().getComponentManager().get(entity);
    }

    /**
     * Get or create the ability holder component for an entity.
     */
    public static AbilityHolderComponent getOrCreate(LivingEntity entity) {
        return AspectAbilities.getInstance().getComponentManager().getOrCreate(entity);
    }

    // ========================================
    // Manager class for component lifecycle
    // ========================================

    /**
     * Manages ability holder components for all entities.
     * Uses UUID for players to persist across reconnects.
     */
    public static class Manager {

        // Use String keys - UUID string for players, entity ID for non-players
        private final Map<String, AbilityHolderComponent> components = new ConcurrentHashMap<>();

        /**
         * Get the component for an entity, or null if none exists.
         */
        public AbilityHolderComponent get(LivingEntity entity) {
            if (entity == null) return null;
            return components.get(getEntityKey(entity));
        }

        /**
         * Get or create a component for an entity.
         */
        public AbilityHolderComponent getOrCreate(LivingEntity entity) {
            if (entity == null) return null;
            return components.computeIfAbsent(getEntityKey(entity),
                key -> new AbilityHolderComponent(entity));
        }

        /**
         * Get a component by UUID (for loading saved data).
         */
        public AbilityHolderComponent getByUuid(java.util.UUID uuid) {
            if (uuid == null) return null;
            return components.get(uuid.toString());
        }

        /**
         * Store a component with a specific UUID key.
         */
        public void put(java.util.UUID uuid, AbilityHolderComponent component) {
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
            for (AbilityHolderComponent component : components.values()) {
                component.tick();
            }
        }

        /**
         * Get all tracked components.
         */
        public Collection<AbilityHolderComponent> getAll() {
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
