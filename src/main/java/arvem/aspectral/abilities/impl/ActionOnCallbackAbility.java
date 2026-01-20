package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;

import java.util.function.Consumer;

/**
 * An ability that executes actions on lifecycle events.
 */
public class ActionOnCallbackAbility extends Ability {

    private final Consumer<LivingEntity> onGainAction;
    private final Consumer<LivingEntity> onLostAction;
    private final Consumer<LivingEntity> onAddedAction;
    private final Consumer<LivingEntity> onRemovedAction;
    private final Consumer<LivingEntity> onRespawnAction;

    public ActionOnCallbackAbility(AbilityType<?> type, LivingEntity entity,
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

    public static AbilityFactory<ActionOnCallbackAbility> createFactory() {
        return new AbilityFactory<ActionOnCallbackAbility>(
            AspectAbilities.identifier("action_on_callback"),
            new SerializableData()
                .add("entity_action_gained", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("entity_action_lost", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("entity_action_added", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("entity_action_removed", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("entity_action_respawned", AspectAbilitiesDataTypes.ENTITY_ACTION, null),
            data -> (type, entity) -> new ActionOnCallbackAbility(
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
