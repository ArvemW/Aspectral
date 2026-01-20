package arvem.aspectral.abilities.impl;

import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.api.LivingEntity;

/**
 * Simple ability that grants fire immunity.
 * Game logic should check for this ability to prevent fire damage.
 */
public class FireImmunityAbility extends Ability {

    public FireImmunityAbility(AbilityType<Ability> type, LivingEntity entity) {
        super(type, entity);
    }

    /**
     * Check if the entity should be immune to fire damage.
     * Game logic should check for this ability.
     */
    public boolean shouldPreventFireDamage() {
        return isActive();
    }
}
