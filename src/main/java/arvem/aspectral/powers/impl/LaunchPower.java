package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import arvem.aspectral.util.Cooldown;
import com.google.gson.JsonObject;

/**
 * Launch power - propels the player upward when a key is pressed.
 * Supports both JSON and code-defined construction.
 */
public class LaunchPower extends Power {

    private final double strength;
    private final String key;
    private final Cooldown cooldown;

    /**
     * Create from PowerType and entity with parameters.
     */
    public LaunchPower(PowerType<?> type, LivingEntity entity,
                       double strength, int cooldownTicks, String key) {
        super(type, entity);
        this.strength = strength;
        this.key = key;
        this.cooldown = new Cooldown(cooldownTicks);

        setTicking(); // Mark as needing tick updates for cooldown
    }

    @Override
    public boolean shouldTick() {
        return !cooldown.isReady();
    }

    @Override
    public void tick() {
        cooldown.tick();
    }

    @Override
    public void onKeyPressed(String pressedKey) {
        AspectPowers.getLogger().atInfo().log("Key pressed: %s", pressedKey);
        if (!isActive() || !cooldown.isReady()) {
            return;
        }

        if (key.equals(pressedKey)) {
            // Apply upward velocity
            if (entity instanceof HytalePlayerAdapter adapter) {
                try {
                    // Get the player's entity reference and store
                    var ref = adapter.getRef();
                    var store = adapter.getStore();

                    // Get the Velocity component
                    var velocity = store.getComponent(ref,
                        com.hypixel.hytale.server.core.modules.physics.component.Velocity.getComponentType());

                    if (velocity != null) {
                        // Add upward force
                        velocity.addForce(0, strength, 0);

                        // Trigger cooldown
                        cooldown.trigger();

                        AspectPowers.getLogger().atInfo().log(
                            "Player %s launched with strength %.2f (cooldown: %ds)",
                            adapter.getUsername(), strength, cooldown.getMaxTicks() / 20);
                    } else {
                        AspectPowers.getLogger().atWarning().log(
                            "Could not find Velocity component for player %s",
                            adapter.getUsername());
                    }

                } catch (Exception e) {
                    AspectPowers.getLogger().atWarning().log(
                        "Failed to apply launch velocity: %s", e.getMessage());
                }
            }
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("strength", strength);
        json.addProperty("cooldown", cooldown.getMaxTicks());
        json.addProperty("key", key);
        json.add("cooldown_state", cooldown.toJson());
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        super.fromJson(json);
        if (json.has("cooldown_state")) {
            cooldown.fromJson(json.getAsJsonObject("cooldown_state"));
        }
    }

    public double getStrength() {
        return strength;
    }

    public String getKey() {
        return key;
    }

    public Cooldown getCooldown() {
        return cooldown;
    }

    public static PowerFactory<LaunchPower> createFactory() {
        return new PowerFactory<LaunchPower>(
            AspectPowers.identifier("launch"),
            new SerializableData()
                .add("strength", SerializableDataTypes.DOUBLE, 1.0)
                .add("cooldown", SerializableDataTypes.INT, 100) // In ticks
                .add("key", SerializableDataTypes.STRING, "key.jump"),
            data -> (type, entity) -> new LaunchPower(
                type, entity,
                data.get("strength"),
                data.get("cooldown"),
                data.get("key")
            )
        ).allowCondition();
    }
}


