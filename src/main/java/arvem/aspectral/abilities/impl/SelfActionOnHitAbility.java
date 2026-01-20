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
import java.util.function.Predicate;

/**
 * Action triggered when this entity hits another.
 */
public class SelfActionOnHitAbility extends Ability {

    private final Consumer<LivingEntity> action;
    private final Predicate<LivingEntity> targetCondition;
    private final int cooldown;
    private int cooldownRemaining = 0;

    public SelfActionOnHitAbility(AbilityType<SelfActionOnHitAbility> type, LivingEntity entity,
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
            action.accept(entity);
        }
        cooldownRemaining = cooldown;
    }

    public static AbilityFactory<SelfActionOnHitAbility> createFactory() {
        return new AbilityFactory<SelfActionOnHitAbility>(
            AspectAbilities.identifier("self_action_on_hit"),
            new SerializableData()
                .add("entity_action", AspectAbilitiesDataTypes.ENTITY_ACTION)
                .add("target_condition", AspectAbilitiesDataTypes.ENTITY_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 0),
            data -> (type, entity) -> new SelfActionOnHitAbility(
                type, entity,
                data.get("entity_action"),
                data.get("target_condition"),
                data.get("cooldown")
            )
        ).allowCondition();
    }
}
