package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.function.Predicate;

/**
 * Ability that modifies damage dealt by the entity.
 */
public class ModifyDamageDealtAbility extends Ability {

    private final float damageModifier;
    private final Predicate<LivingEntity> targetCondition;

    public ModifyDamageDealtAbility(AbilityType<ModifyDamageDealtAbility> type, LivingEntity entity,
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

    public static AbilityFactory<ModifyDamageDealtAbility> createFactory() {
        return new AbilityFactory<ModifyDamageDealtAbility>(
            AspectAbilities.identifier("modify_damage_dealt"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f)
                .add("target_condition", AspectAbilitiesDataTypes.ENTITY_CONDITION, null),
            data -> (type, entity) -> new ModifyDamageDealtAbility(
                type, entity,
                data.get("modifier"),
                data.get("target_condition")
            )
        ).allowCondition();
    }
}
