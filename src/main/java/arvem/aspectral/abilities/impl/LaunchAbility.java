package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import com.google.gson.JsonObject;

/**
 * Launch ability - propels the player upward when a key is pressed.
 * Supports both JSON and code-defined construction.
 */
public class LaunchAbility extends Ability {

    private final double strength;
    private final int cooldownTicks;
    private final String key;

    private int currentCooldown = 0;

    /**
     * Create from AbilityType and entity with parameters.
     */
    public LaunchAbility(AbilityType<?> type, LivingEntity entity,
                        double strength, int cooldownTicks, String key) {
        super(type, entity);
        this.strength = strength;
        this.cooldownTicks = cooldownTicks;
        this.key = key;

        setTicking(); // Mark as needing tick updates
    }

    @Override
    public boolean shouldTick() {
        return currentCooldown > 0;
    }

    @Override
    public void tick() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    @Override
    public void onKeyPressed(String pressedKey) {
        if (!isActive() || currentCooldown > 0) {
            return;
        }

        if (key.equals(pressedKey)) {
            // Apply upward velocity
            if (entity instanceof HytalePlayerAdapter player) {
                // TODO: Implement velocity setting using Hytale API
                // player.setVelocity(0, strength, 0);
                currentCooldown = cooldownTicks;
            }
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("strength", strength);
        json.addProperty("cooldown", cooldownTicks / 20);
        json.addProperty("key", key);
        json.addProperty("currentCooldown", currentCooldown);
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        super.fromJson(json);
        if (json.has("currentCooldown")) {
            this.currentCooldown = json.get("currentCooldown").getAsInt();
        }
    }

    public double getStrength() {
        return strength;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public String getKey() {
        return key;
    }

    public int getCurrentCooldown() {
        return currentCooldown;
    }

    public static AbilityFactory<LaunchAbility> createFactory() {
        return new AbilityFactory<LaunchAbility>(
            AspectAbilities.identifier("launch"),
            new SerializableData()
                .add("strength", SerializableDataTypes.DOUBLE, 1.0)
                .add("cooldown", SerializableDataTypes.INT, 100) // In ticks
                .add("key", SerializableDataTypes.STRING, "key.jump"),
            data -> (type, entity) -> new LaunchAbility(
                type, entity,
                data.get("strength"),
                data.get("cooldown"),
                data.get("key")
            )
        ).allowCondition();
    }
}
