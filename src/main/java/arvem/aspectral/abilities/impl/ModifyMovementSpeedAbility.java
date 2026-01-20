package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Ability that modifies movement speed.
 */
public class ModifyMovementSpeedAbility extends Ability {

    private final float speedModifier;

    public ModifyMovementSpeedAbility(AbilityType<ModifyMovementSpeedAbility> type, LivingEntity entity, float speedModifier) {
        super(type, entity);
        this.speedModifier = speedModifier;
    }

    public float getSpeedModifier() {
        return isActive() ? speedModifier : 1.0f;
    }

    public static AbilityFactory<ModifyMovementSpeedAbility> createFactory() {
        return new AbilityFactory<ModifyMovementSpeedAbility>(
            AspectAbilities.identifier("modify_movement_speed"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f),
            data -> (type, entity) -> new ModifyMovementSpeedAbility(type, entity, data.get("modifier"))
        ).allowCondition();
    }
}
