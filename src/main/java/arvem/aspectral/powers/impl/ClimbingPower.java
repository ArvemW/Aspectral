package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Climbing power - allows wall climbing.
 */
public class ClimbingPower extends Power {

    private final boolean onlyInWall;

    public ClimbingPower(PowerType<ClimbingPower> type, LivingEntity entity, boolean onlyInWall) {
        super(type, entity);
        this.onlyInWall = onlyInWall;
    }

    public boolean isOnlyInWall() {
        return onlyInWall;
    }

    public static PowerFactory<ClimbingPower> createFactory() {
        return new PowerFactory<ClimbingPower>(
            AspectPowers.identifier("climbing"),
            new SerializableData()
                .add("wall_climbing", SerializableDataTypes.BOOLEAN, true),
            data -> (type, entity) -> new ClimbingPower(type, entity, data.get("wall_climbing"))
        ).allowCondition();
    }
}


