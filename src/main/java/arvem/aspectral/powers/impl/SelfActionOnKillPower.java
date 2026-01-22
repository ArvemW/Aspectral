package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Action triggered when this entity kills another.
 */
public class SelfActionOnKillPower extends Power {

    private final Consumer<LivingEntity> action;
    private final Predicate<LivingEntity> targetCondition;

    public SelfActionOnKillPower(PowerType<SelfActionOnKillPower> type, LivingEntity entity,
                                 Consumer<LivingEntity> action,
                                 Predicate<LivingEntity> targetCondition) {
        super(type, entity);
        this.action = action;
        this.targetCondition = targetCondition;
    }

    public void onKill(LivingEntity target) {
        if (!isActive()) return;
        if (targetCondition != null && !targetCondition.test(target)) return;

        if (action != null) {
            action.accept(entity);
        }
    }

    public static PowerFactory<SelfActionOnKillPower> createFactory() {
        return new PowerFactory<SelfActionOnKillPower>(
            AspectPowers.identifier("self_action_on_kill"),
            new SerializableData()
                .add("entity_action", AspectPowersDataTypes.ENTITY_ACTION)
                .add("target_condition", AspectPowersDataTypes.ENTITY_CONDITION, null),
            data -> (type, entity) -> new SelfActionOnKillPower(
                type, entity,
                data.get("entity_action"),
                data.get("target_condition")
            )
        ).allowCondition();
    }
}


