package arvem.aspectral.powers.impl;

import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.api.LivingEntity;

/**
 * Swimming power - enhances swimming capabilities.
 */
public class SwimmingPower extends Power {
    public SwimmingPower(PowerType<Power> type, LivingEntity entity) {
        super(type, entity);
    }
}


