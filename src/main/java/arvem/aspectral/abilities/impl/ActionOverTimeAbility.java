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
 * An ability that executes actions periodically.
 */
public class ActionOverTimeAbility extends Ability {

    private final int interval;
    private final Consumer<LivingEntity> risingAction;
    private final Consumer<LivingEntity> fallingAction;

    private int tickCount = 0;
    private boolean wasActive = false;

    public ActionOverTimeAbility(AbilityType<?> type, LivingEntity entity,
                                  int interval,
                                  Consumer<LivingEntity> risingAction,
                                  Consumer<LivingEntity> fallingAction) {
        super(type, entity);
        this.interval = interval;
        this.risingAction = risingAction;
        this.fallingAction = fallingAction;
        setTicking(true); // Tick even when inactive to detect state changes
    }

    @Override
    public void tick() {
        boolean currentlyActive = isActive();

        // Rising edge - just became active
        if (currentlyActive && !wasActive) {
            if (risingAction != null) {
                risingAction.accept(entity);
            }
            tickCount = 0;
        }

        // Falling edge - just became inactive
        if (!currentlyActive && wasActive) {
            if (fallingAction != null) {
                fallingAction.accept(entity);
            }
        }

        // Periodic action while active
        if (currentlyActive && interval > 0) {
            tickCount++;
            if (tickCount >= interval) {
                if (risingAction != null) {
                    risingAction.accept(entity);
                }
                tickCount = 0;
            }
        }

        wasActive = currentlyActive;
    }

    public static AbilityFactory<ActionOverTimeAbility> createFactory() {
        return new AbilityFactory<ActionOverTimeAbility>(
            AspectAbilities.identifier("action_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .add("rising_action", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("falling_action", AspectAbilitiesDataTypes.ENTITY_ACTION, null),
            data -> (type, entity) -> new ActionOverTimeAbility(
                type, entity,
                data.get("interval"),
                data.get("rising_action"),
                data.get("falling_action")
            )
        ).allowCondition();
    }
}
