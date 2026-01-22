package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * power that modifies movement speed.
 */
public class ModifyMovementSpeedPower extends Power {

    private final float speedModifier;

    public ModifyMovementSpeedPower(PowerType<ModifyMovementSpeedPower> type, LivingEntity entity, float speedModifier) {
        super(type, entity);
        this.speedModifier = speedModifier;
    }

    public float getSpeedModifier() {
        return isActive() ? speedModifier : 1.0f;
    }

    public static PowerFactory<ModifyMovementSpeedPower> createFactory() {
        return new PowerFactory<ModifyMovementSpeedPower>(
            AspectPowers.identifier("modify_movement_speed"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.FLOAT, 1.0f),
            data -> (type, entity) -> new ModifyMovementSpeedPower(type, entity, data.get("modifier"))
        ).allowCondition();
    }
}


