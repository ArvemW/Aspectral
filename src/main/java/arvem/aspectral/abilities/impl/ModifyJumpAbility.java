package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Ability that modifies jump height/velocity.
 */
public class ModifyJumpAbility extends Ability {

    private final float jumpModifier;

    public ModifyJumpAbility(AbilityType<ModifyJumpAbility> type, LivingEntity entity, float jumpModifier) {
        super(type, entity);
        this.jumpModifier = jumpModifier;
    }

    public float getJumpModifier() {
        return isActive() ? jumpModifier : 1.0f;
    }

    public static AbilityFactory<ModifyJumpAbility> createFactory() {
        return new AbilityFactory<ModifyJumpAbility>(
            AspectAbilities.identifier("modify_jump"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f),
            data -> (type, entity) -> new ModifyJumpAbility(type, entity, data.get("modifier"))
        ).allowCondition();
    }
}
