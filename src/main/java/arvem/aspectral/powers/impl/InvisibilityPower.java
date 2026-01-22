package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;

/**
 * Invisibility power.
 */
public class InvisibilityPower extends Power {

    public InvisibilityPower(PowerType<InvisibilityPower> type, LivingEntity entity) {
        super(type, entity);
    }

    public static PowerFactory<InvisibilityPower> createFactory() {
        return new PowerFactory<InvisibilityPower>(
            AspectPowers.identifier("invisibility"),
            new SerializableData(),
            data -> InvisibilityPower::new
        ).allowCondition();
    }
}


