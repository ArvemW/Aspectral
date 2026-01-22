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
 * power that modifies damage taken by the entity.
 */
public class ModifyDamageTakenPower extends Power {

    private final float damageModifier;
    private final Predicate<LivingEntity> attackerCondition;

    public ModifyDamageTakenPower(PowerType<ModifyDamageTakenPower> type, LivingEntity entity,
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

    public static PowerFactory<ModifyDamageTakenPower> createFactory() {
        return new PowerFactory<ModifyDamageTakenPower>(
            AspectPowers.identifier("modify_damage_taken"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f)
                .add("attacker_condition", AspectPowersDataTypes.ENTITY_CONDITION, null),
            data -> (type, entity) -> new ModifyDamageTakenPower(
                type, entity,
                data.get("modifier"),
                data.get("attacker_condition")
            )
        ).allowCondition();
    }
}


