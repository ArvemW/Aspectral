package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;

/**
 * Invisibility ability.
 */
public class InvisibilityAbility extends Ability {

    public InvisibilityAbility(AbilityType<InvisibilityAbility> type, LivingEntity entity) {
        super(type, entity);
    }

    public static AbilityFactory<InvisibilityAbility> createFactory() {
        return new AbilityFactory<InvisibilityAbility>(
            AspectAbilities.identifier("invisibility"),
            new SerializableData(),
            data -> InvisibilityAbility::new
        ).allowCondition();
    }
}
