package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;

import java.util.function.Consumer;

/**
 * An power that executes actions on lifecycle events.
 */
public class ActionOnCallbackPower extends Power {

    private final Consumer<LivingEntity> onGainAction;
    private final Consumer<LivingEntity> onLostAction;
    private final Consumer<LivingEntity> onAddedAction;
    private final Consumer<LivingEntity> onRemovedAction;
    private final Consumer<LivingEntity> onRespawnAction;

    public ActionOnCallbackPower(PowerType<?> type, LivingEntity entity,
                                 Consumer<LivingEntity> onGainAction,
                                 Consumer<LivingEntity> onLostAction,
                                 Consumer<LivingEntity> onAddedAction,
                                 Consumer<LivingEntity> onRemovedAction,
                                 Consumer<LivingEntity> onRespawnAction) {
        super(type, entity);
        this.onGainAction = onGainAction;
        this.onLostAction = onLostAction;
        this.onAddedAction = onAddedAction;
        this.onRemovedAction = onRemovedAction;
        this.onRespawnAction = onRespawnAction;
    }

    @Override
    public void onGained() {
        if (onGainAction != null && isActive()) {
            onGainAction.accept(entity);
        }
    }

    @Override
    public void onLost() {
        if (onLostAction != null) {
            onLostAction.accept(entity);
        }
    }

    @Override
    public void onAdded(boolean onSync) {
        if (!onSync && onAddedAction != null && isActive()) {
            onAddedAction.accept(entity);
        }
    }

    @Override
    public void onRemoved(boolean onSync) {
        if (!onSync && onRemovedAction != null) {
            onRemovedAction.accept(entity);
        }
    }

    @Override
    public void onRespawn() {
        if (onRespawnAction != null && isActive()) {
            onRespawnAction.accept(entity);
        }
    }

    public static PowerFactory<ActionOnCallbackPower> createFactory() {
        return new PowerFactory<ActionOnCallbackPower>(
            AspectPowers.identifier("action_on_callback"),
            new SerializableData()
                .add("entity_action_gained", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("entity_action_lost", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("entity_action_added", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("entity_action_removed", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("entity_action_respawned", AspectPowersDataTypes.ENTITY_ACTION, null),
            data -> (type, entity) -> new ActionOnCallbackPower(
                type, entity,
                data.get("entity_action_gained"),
                data.get("entity_action_lost"),
                data.get("entity_action_added"),
                data.get("entity_action_removed"),
                data.get("entity_action_respawned")
            )
        ).allowCondition();
    }
}


