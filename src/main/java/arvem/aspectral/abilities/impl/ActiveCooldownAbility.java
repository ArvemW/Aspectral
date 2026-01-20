package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.Activatable;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.function.Consumer;

/**
 * An ability that can be actively triggered and has a cooldown.
 * When triggered, executes an action.
 */
public class ActiveCooldownAbility extends CooldownAbility implements Activatable {

    private final Consumer<LivingEntity> action;
    private final String keyBinding;

    public ActiveCooldownAbility(AbilityType<?> type, LivingEntity entity,
                                  int cooldownDuration, Consumer<LivingEntity> action,
                                  String keyBinding) {
        super(type, entity, cooldownDuration);
        this.action = action;
        this.keyBinding = keyBinding;
    }

    @Override
    public void onActivate() {
        if (isActive() && isReady()) {
            if (action != null) {
                action.accept(entity);
            }
            use();
        }
    }

    @Override
    public String getKeyBinding() {
        return keyBinding;
    }

    public static AbilityFactory<ActiveCooldownAbility> createFactory() {
        return new AbilityFactory<ActiveCooldownAbility>(
            AspectAbilities.identifier("active_cooldown"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT)
                .add("action", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("key", SerializableDataTypes.STRING, "none"),
            data -> (type, entity) -> new ActiveCooldownAbility(
                type, entity,
                data.get("cooldown"),
                data.get("action"),
                data.get("key")
            )
        ).allowCondition();
    }
}
