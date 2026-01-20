package arvem.aspectral.component;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.aspect.Aspect;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Component that stores which Aspect a player has chosen.
 * Only stores the Aspect ID - abilities are recreated from the Aspect on load.
 */
public class PlayerAspectComponent {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final LivingEntity entity;
    private String aspectId;

    public PlayerAspectComponent(LivingEntity entity) {
        this.entity = entity;
        this.aspectId = null;
    }

    /**
     * Set the player's aspect and recreate all abilities.
     * @param aspectId The aspect identifier (e.g., "aspectral:skywalker")
     */
    public void setAspect(String aspectId) {
        // Remove existing abilities
        clearAbilities();

        // Set new aspect ID
        this.aspectId = aspectId;

        // Recreate abilities from the aspect
        if (aspectId != null) {
            applyAspect();
        }
    }

    /**
     * Get the player's current aspect ID.
     */
    public String getAspectId() {
        return aspectId;
    }

    /**
     * Check if the player has an aspect.
     */
    public boolean hasAspect() {
        return aspectId != null;
    }

    /**
     * Apply the current aspect's abilities to the player.
     */
    private void applyAspect() {
        if (aspectId == null) {
            return;
        }

        Aspect aspect = AspectAbilities.getInstance().getAspectRegistry().get(aspectId);
        if (aspect == null) {
            LOGGER.atWarning().log("Player has unknown aspect: %s", aspectId);
            return;
        }

        // Create all ability instances from the aspect
        List<Ability> abilities = aspect.createAbilities(entity);

        // Add them to the ability holder
        AbilityHolderComponent holder = AbilityHolderComponent.getOrCreate(entity);
        for (Ability ability : abilities) {
            holder.addAbility(ability, aspectId);
        }

        LOGGER.atInfo().log("Applied aspect %s with %d abilities to player", aspectId, abilities.size());
    }

    /**
     * Clear all abilities from the player.
     */
    private void clearAbilities() {
        AbilityHolderComponent holder = AbilityHolderComponent.get(entity);
        if (holder != null) {
            holder.clearAbilities();
        }
    }

    /**
     * Clear the player's aspect.
     */
    public void clearAspect() {
        clearAbilities();
        this.aspectId = null;
    }

    /**
     * Called when the player joins - recreates abilities from aspect ID.
     */
    public void onPlayerJoin() {
        if (aspectId != null) {
            applyAspect();
        }
    }

    /**
     * Serialize to JSON for persistence.
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (aspectId != null) {
            json.addProperty("aspect", aspectId);
        }
        return json;
    }

    /**
     * Deserialize from JSON.
     */
    public void fromJson(JsonObject json) {
        if (json.has("aspect")) {
            this.aspectId = json.get("aspect").getAsString();
        }
    }

    // ========================================
    // Static Manager
    // ========================================

    public static class Manager {
        private final ConcurrentMap<UUID, PlayerAspectComponent> components = new ConcurrentHashMap<>();

        public PlayerAspectComponent getOrCreate(LivingEntity entity) {
            UUID uuid = getUUID(entity);
            return components.computeIfAbsent(uuid, k -> new PlayerAspectComponent(entity));
        }

        public PlayerAspectComponent get(LivingEntity entity) {
            UUID uuid = getUUID(entity);
            return components.get(uuid);
        }

        public void remove(LivingEntity entity) {
            UUID uuid = getUUID(entity);
            components.remove(uuid);
        }

        private UUID getUUID(LivingEntity entity) {
            if (entity instanceof HytalePlayerAdapter adapter) {
                return adapter.getUuid();
            }
            throw new IllegalArgumentException("Entity is not a player");
        }
    }

    /**
     * Get the component for an entity.
     */
    public static PlayerAspectComponent get(LivingEntity entity) {
        return AspectAbilities.getInstance().getPlayerAspectManager().get(entity);
    }

    /**
     * Get or create the component for an entity.
     */
    public static PlayerAspectComponent getOrCreate(LivingEntity entity) {
        return AspectAbilities.getInstance().getPlayerAspectManager().getOrCreate(entity);
    }
}
