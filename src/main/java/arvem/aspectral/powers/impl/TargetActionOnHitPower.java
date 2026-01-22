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
import java.util.function.Predicate;

/**
 * Action triggered on the target when this entity hits them.
 */
public class TargetActionOnHitPower extends Power {

    private final Consumer<LivingEntity> action;
    private final Predicate<LivingEntity> targetCondition;
    private final int cooldown;
    private int cooldownRemaining = 0;

    public TargetActionOnHitPower(PowerType<TargetActionOnHitPower> type, LivingEntity entity,
                                  Consumer<LivingEntity> action,
                                  Predicate<LivingEntity> targetCondition,
                                  int cooldown) {
        super(type, entity);
        this.action = action;
        this.targetCondition = targetCondition;
        this.cooldown = cooldown;
        if (cooldown > 0) setTicking();
    }

    @Override
    public void tick() {
        if (cooldownRemaining > 0) cooldownRemaining--;
    }

    public void onHit(LivingEntity target) {
        if (!isActive()) return;
        if (cooldownRemaining > 0) return;
        if (targetCondition != null && !targetCondition.test(target)) return;

        if (action != null) {
            action.accept(target);
        }
        cooldownRemaining = cooldown;
    }

    public static PowerFactory<TargetActionOnHitPower> createFactory() {
        return new PowerFactory<TargetActionOnHitPower>(
            AspectPowers.identifier("target_action_on_hit"),
            new SerializableData()
                .add("entity_action", AspectPowersDataTypes.ENTITY_ACTION)
                .add("target_condition", AspectPowersDataTypes.ENTITY_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 0),
            data -> (type, entity) -> new TargetActionOnHitPower(
                type, entity,
                data.get("entity_action"),
                data.get("target_condition"),
                data.get("cooldown")
            )
        ).allowCondition();
    }
}


