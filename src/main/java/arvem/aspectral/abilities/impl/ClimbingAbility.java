package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Climbing ability - allows wall climbing.
 */
public class ClimbingAbility extends Ability {

    private final boolean onlyInWall;

    public ClimbingAbility(AbilityType<ClimbingAbility> type, LivingEntity entity, boolean onlyInWall) {
        super(type, entity);
        this.onlyInWall = onlyInWall;
    }

    public boolean isOnlyInWall() {
        return onlyInWall;
    }

    public static AbilityFactory<ClimbingAbility> createFactory() {
        return new AbilityFactory<ClimbingAbility>(
            AspectAbilities.identifier("climbing"),
            new SerializableData()
                .add("wall_climbing", SerializableDataTypes.BOOLEAN, true),
            data -> (type, entity) -> new ClimbingAbility(type, entity, data.get("wall_climbing"))
        ).allowCondition();
    }
}
