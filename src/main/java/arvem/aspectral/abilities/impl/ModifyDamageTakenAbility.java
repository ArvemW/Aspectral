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
 * Ability that modifies damage taken by the entity.
 */
public class ModifyDamageTakenAbility extends Ability {

    private final float damageModifier;
    private final Predicate<LivingEntity> attackerCondition;

    public ModifyDamageTakenAbility(AbilityType<ModifyDamageTakenAbility> type, LivingEntity entity,
                                     float damageModifier,
                                     Predicate<LivingEntity> attackerCondition) {
        super(type, entity);
        this.damageModifier = damageModifier;
        this.attackerCondition = attackerCondition;
    }

    /**
     * Calculate modified damage for incoming attack.
     *
     * @param attacker The attacking entity (may be null)
     * @param originalDamage The original damage amount
     * @return The modified damage amount
     */
    public float modifyDamage(LivingEntity attacker, float originalDamage) {
        if (!isActive()) return originalDamage;
        if (attacker != null && attackerCondition != null && !attackerCondition.test(attacker)) {
            return originalDamage;
        }
        return originalDamage * damageModifier;
    }

    public static AbilityFactory<ModifyDamageTakenAbility> createFactory() {
        return new AbilityFactory<ModifyDamageTakenAbility>(
            AspectAbilities.identifier("modify_damage_taken"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f)
                .add("attacker_condition", AspectAbilitiesDataTypes.ENTITY_CONDITION, null),
            data -> (type, entity) -> new ModifyDamageTakenAbility(
                type, entity,
                data.get("modifier"),
                data.get("attacker_condition")
            )
        ).allowCondition();
    }
}
