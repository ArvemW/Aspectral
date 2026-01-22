package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import com.google.gson.JsonObject;

/**
 * An power with a cooldown that must expire before it can be used again.
 */
public class CooldownPower extends Power {

    private final int cooldownDuration;
    private int cooldownRemaining = 0;

    public CooldownPower(PowerType<?> type, LivingEntity entity, int cooldownDuration) {
        super(type, entity);
        this.cooldownDuration = cooldownDuration;
        setTicking();
    }

    /**
     * Check if the power is ready to use (not on cooldown).
     */
    public boolean isReady() {
        return cooldownRemaining <= 0;
    }

    /**
     * Get the remaining cooldown in ticks.
     */
    public int getRemainingCooldown() {
        return cooldownRemaining;
    }

    /**
     * Get the progress of the cooldown (0.0 = just started, 1.0 = ready).
     */
    public float getCooldownProgress() {
        if (cooldownDuration <= 0) return 1.0f;
        return 1.0f - ((float) cooldownRemaining / cooldownDuration);
    }

    /**
     * Trigger the cooldown.
     */
    public void use() {
        cooldownRemaining = cooldownDuration;
    }

    /**
     * Reset the cooldown (make ready immediately).
     */
    public void reset() {
        cooldownRemaining = 0;
    }

    /**
     * Modify the remaining cooldown.
     */
    public void modifyCooldown(int ticks) {
        cooldownRemaining = Math.max(0, cooldownRemaining + ticks);
    }

    @Override
    public void tick() {
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("cooldown_remaining", cooldownRemaining);
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        if (json.has("cooldown_remaining")) {
            cooldownRemaining = json.get("cooldown_remaining").getAsInt();
        }
    }

    public static PowerFactory<CooldownPower> createCooldownFactory() {
        return new PowerFactory<CooldownPower>(
            AspectPowers.identifier("cooldown"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT),
            data -> (type, entity) -> new CooldownPower(type, entity, data.get("cooldown"))
        ).allowCondition();
    }
}


