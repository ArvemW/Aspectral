package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.function.Consumer;

/**
 * Ability that prevents death under certain conditions.
 */
public class PreventDeathAbility extends Ability {

    private final Consumer<LivingEntity> action;
    private final int minHealth;

    public PreventDeathAbility(AbilityType<PreventDeathAbility> type, LivingEntity entity,
                                Consumer<LivingEntity> action, int minHealth) {
        super(type, entity);
        this.action = action;
        this.minHealth = minHealth;
    }

    /**
     * Called when the entity would die.
     * @return true if death should be prevented
     */
    public boolean shouldPreventDeath() {
        if (!isActive()) return false;

        if (action != null) {
            action.accept(entity);
        }

        // Set health to minimum
        if (minHealth > 0) {
            entity.setHealth(minHealth);
        }

        return true;
    }

    public static AbilityFactory<PreventDeathAbility> createFactory() {
        return new AbilityFactory<PreventDeathAbility>(
            AspectAbilities.identifier("prevent_death"),
            new SerializableData()
                .add("entity_action", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("min_health", SerializableDataTypes.INT, 1),
            data -> (type, entity) -> new PreventDeathAbility(
                type, entity,
                data.get("entity_action"),
                data.get("min_health")
            )
        ).allowCondition();
    }
}
