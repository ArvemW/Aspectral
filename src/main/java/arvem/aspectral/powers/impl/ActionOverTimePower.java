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
 * An power that executes actions periodically.
 */
public class ActionOverTimePower extends Power {

    private final int interval;
    private final Consumer<LivingEntity> risingAction;
    private final Consumer<LivingEntity> fallingAction;

    private int tickCount = 0;
    private boolean wasActive = false;

    public ActionOverTimePower(PowerType<?> type, LivingEntity entity,
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

    public static PowerFactory<ActionOverTimePower> createFactory() {
        return new PowerFactory<ActionOverTimePower>(
            AspectPowers.identifier("action_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .add("rising_action", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("falling_action", AspectPowersDataTypes.ENTITY_ACTION, null),
            data -> (type, entity) -> new ActionOverTimePower(
                type, entity,
                data.get("interval"),
                data.get("rising_action"),
                data.get("falling_action")
            )
        ).allowCondition();
    }
}


