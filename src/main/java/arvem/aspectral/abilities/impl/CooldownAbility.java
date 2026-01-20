package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import com.google.gson.JsonObject;

/**
 * An ability with a cooldown that must expire before it can be used again.
 */
public class CooldownAbility extends Ability {

    private final int cooldownDuration;
    private int cooldownRemaining = 0;

    public CooldownAbility(AbilityType<?> type, LivingEntity entity, int cooldownDuration) {
        super(type, entity);
        this.cooldownDuration = cooldownDuration;
        setTicking();
    }

    /**
     * Check if the ability is ready to use (not on cooldown).
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

    public static AbilityFactory<CooldownAbility> createCooldownFactory() {
        return new AbilityFactory<CooldownAbility>(
            AspectAbilities.identifier("cooldown"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT),
            data -> (type, entity) -> new CooldownAbility(type, entity, data.get("cooldown"))
        ).allowCondition();
    }
}
