package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.function.Predicate;

/**
 * Ability that makes other entities glow for this entity (if player).
 */
public class EntityGlowAbility extends Ability {

    private final Predicate<LivingEntity> entityCondition;
    private final int red, green, blue;

    public EntityGlowAbility(AbilityType<EntityGlowAbility> type, LivingEntity entity,
                              Predicate<LivingEntity> entityCondition,
                              int red, int green, int blue) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Check if a specific entity should glow for this entity.
     */
    public boolean shouldGlow(LivingEntity target) {
        if (!isActive()) return false;
        if (entityCondition != null && !entityCondition.test(target)) return false;
        return true;
    }

    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }

    public static AbilityFactory<EntityGlowAbility> createFactory() {
        return new AbilityFactory<EntityGlowAbility>(
            AspectAbilities.identifier("entity_glow"),
            new SerializableData()
                .add("entity_condition", AspectAbilitiesDataTypes.ENTITY_CONDITION, null)
                .add("red", SerializableDataTypes.INT, 255)
                .add("green", SerializableDataTypes.INT, 255)
                .add("blue", SerializableDataTypes.INT, 255),
            data -> (type, entity) -> new EntityGlowAbility(
                type, entity,
                data.get("entity_condition"),
                data.get("red"),
                data.get("green"),
                data.get("blue")
            )
        ).allowCondition();
    }
}
