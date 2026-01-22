package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.function.Predicate;

/**
 * Power that makes other entities glow for this entity (if player).
 */
public class EntityGlowPower extends Power {

    private final Predicate<LivingEntity> entityCondition;
    private final int red, green, blue;

    public EntityGlowPower(PowerType<EntityGlowPower> type, LivingEntity entity,
                           Predicate<LivingEntity> entityCondition,
                           int red, int green, int blue) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Check if a specific entity should glow for this entity.
     */
    public boolean shouldGlow(LivingEntity target) {
        if (!isActive()) return false;
        if (entityCondition != null && !entityCondition.test(target)) return false;
        return true;
    }

    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }

    public static PowerFactory<EntityGlowPower> createFactory() {
        return new PowerFactory<EntityGlowPower>(
            AspectPowers.identifier("entity_glow"),
            new SerializableData()
                .add("entity_condition", AspectPowersDataTypes.ENTITY_CONDITION, null)
                .add("red", SerializableDataTypes.INT, 255)
                .add("green", SerializableDataTypes.INT, 255)
                .add("blue", SerializableDataTypes.INT, 255),
            data -> (type, entity) -> new EntityGlowPower(
                type, entity,
                data.get("entity_condition"),
                data.get("red"),
                data.get("green"),
                data.get("blue")
            )
        ).allowCondition();
    }
}


