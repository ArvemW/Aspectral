package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.function.Consumer;

/**
 * Power that prevents death under certain conditions.
 */
public class PreventDeathPower extends Power {

    private final Consumer<LivingEntity> action;
    private final int minHealth;

    public PreventDeathPower(PowerType<PreventDeathPower> type, LivingEntity entity,
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

    public static PowerFactory<PreventDeathPower> createFactory() {
        return new PowerFactory<PreventDeathPower>(
            AspectPowers.identifier("prevent_death"),
            new SerializableData()
                .add("entity_action", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("min_health", SerializableDataTypes.INT, 1),
            data -> (type, entity) -> new PreventDeathPower(
                type, entity,
                data.get("entity_action"),
                data.get("min_health")
            )
        ).allowCondition();
    }
}


