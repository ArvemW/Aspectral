package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.Activatable;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.function.Consumer;

/**
 * A power that can be actively triggered and has a cooldown.
 * When triggered, executes an action.
 */
public class ActiveCooldownPower extends CooldownPower implements Activatable {

    private final Consumer<LivingEntity> action;
    private final String keyBinding;

    public ActiveCooldownPower(PowerType<?> type, LivingEntity entity,
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

    public static PowerFactory<ActiveCooldownPower> createFactory() {
        return new PowerFactory<ActiveCooldownPower>(
            AspectPowers.identifier("active_cooldown"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT)
                .add("action", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("key", SerializableDataTypes.STRING, "none"),
            data -> (type, entity) -> new ActiveCooldownPower(
                type, entity,
                data.get("cooldown"),
                data.get("action"),
                data.get("key")
            )
        ).allowCondition();
    }
}


