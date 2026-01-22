package arvem.aspectral.component;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
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
 * Only stores the Aspect ID - powers are recreated from the Aspect on load.
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
     * Set the player's aspect and recreate all powers.
     * @param aspectId The aspect identifier (e.g., "aspectral:skywalker")
     */
    public void setAspect(String aspectId) {
        // Remove existing powers
        clearAbilities();

        // Set new aspect ID
        this.aspectId = aspectId;

        // Recreate powers from the aspect
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
     * Apply the current aspect's powers to the player.
     */
    private void applyAspect() {
        if (aspectId == null) {
            return;
        }

        Aspect aspect = AspectPowers.getInstance().getAspectRegistry().get(aspectId);
        if (aspect == null) {
            LOGGER.atWarning().log("Player has unknown aspect: %s", aspectId);
            return;
        }

        // Create all power instances from the aspect
        List<Power> powers = aspect.createAbilities(entity);

        // Add them to the power holder
        PowerHolderComponent holder = PowerHolderComponent.getOrCreate(entity);
        for (Power power : powers) {
            holder.addPower(power, aspectId);
        }

        LOGGER.atInfo().log("Applied aspect %s with %d powers to player", aspectId, powers.size());
    }

    /**
     * Clear all powers from the player.
     */
    private void clearAbilities() {
        PowerHolderComponent holder = PowerHolderComponent.get(entity);
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
     * Called when the player joins - recreates powers from aspect ID.
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
        return AspectPowers.getInstance().getPlayerAspectManager().get(entity);
    }

    /**
     * Get or create the component for an entity.
     */
    public static PlayerAspectComponent getOrCreate(LivingEntity entity) {
        return AspectPowers.getInstance().getPlayerAspectManager().getOrCreate(entity);
    }
}


