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
 * Power that modifies damage dealt by the entity.
 */
public class ModifyDamageDealtPower extends Power {

    private final float damageModifier;
    private final Predicate<LivingEntity> targetCondition;

    public ModifyDamageDealtPower(PowerType<ModifyDamageDealtPower> type, LivingEntity entity,
                                  float damageModifier,
                                  Predicate<LivingEntity> targetCondition) {
        super(type, entity);
        this.damageModifier = damageModifier;
        this.targetCondition = targetCondition;
    }

    /**
     * Calculate modified damage for an attack.
     *
     * @param target The entity being attacked
     * @param originalDamage The original damage amount
     * @return The modified damage amount
     */
    public float modifyDamage(LivingEntity target, float originalDamage) {
        if (!isActive()) return originalDamage;
        if (targetCondition != null && !targetCondition.test(target)) return originalDamage;
        return originalDamage * damageModifier;
    }

    public static PowerFactory<ModifyDamageDealtPower> createFactory() {
        return new PowerFactory<ModifyDamageDealtPower>(
            AspectPowers.identifier("modify_damage_dealt"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f)
                .add("target_condition", AspectPowersDataTypes.ENTITY_CONDITION, null),
            data -> (type, entity) -> new ModifyDamageDealtPower(
                type, entity,
                data.get("modifier"),
                data.get("target_condition")
            )
        ).allowCondition();
    }
}


