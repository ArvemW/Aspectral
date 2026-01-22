package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * power that modifies jump height/velocity.
 */
public class ModifyJumpPower extends Power {

    private final float jumpModifier;

    public ModifyJumpPower(PowerType<ModifyJumpPower> type, LivingEntity entity, float jumpModifier) {
        super(type, entity);
        this.jumpModifier = jumpModifier;
    }

    public float getJumpModifier() {
        return isActive() ? jumpModifier : 1.0f;
    }

    public static PowerFactory<ModifyJumpPower> createFactory() {
        return new PowerFactory<ModifyJumpPower>(
            AspectPowers.identifier("modify_jump"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f),
            data -> (type, entity) -> new ModifyJumpPower(type, entity, data.get("modifier"))
        ).allowCondition();
    }
}


