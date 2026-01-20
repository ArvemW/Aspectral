package arvem.aspectral.abilities.impl;

import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.api.LivingEntity;

/**
 * Swimming ability - enhances swimming capabilities.
 */
public class SwimmingAbility extends Ability {
    public SwimmingAbility(AbilityType<Ability> type, LivingEntity entity) {
        super(type, entity);
    }
}
