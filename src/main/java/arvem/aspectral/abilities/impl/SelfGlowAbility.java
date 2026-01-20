package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Ability that makes this entity glow.
 */
public class SelfGlowAbility extends Ability {

    private final int red, green, blue;

    public SelfGlowAbility(AbilityType<SelfGlowAbility> type, LivingEntity entity,
                           int red, int green, int blue) {
        super(type, entity);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean shouldGlow() {
        return isActive();
    }

    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }

    public static AbilityFactory<SelfGlowAbility> createFactory() {
        return new AbilityFactory<SelfGlowAbility>(
            AspectAbilities.identifier("self_glow"),
            new SerializableData()
                .add("red", SerializableDataTypes.INT, 255)
                .add("green", SerializableDataTypes.INT, 255)
                .add("blue", SerializableDataTypes.INT, 255),
            data -> (type, entity) -> new SelfGlowAbility(
                type, entity,
                data.get("red"),
                data.get("green"),
                data.get("blue")
            )
        ).allowCondition();
    }
}
