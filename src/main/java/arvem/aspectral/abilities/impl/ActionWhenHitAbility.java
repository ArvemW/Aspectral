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
 * Action triggered on self when hit by another entity.
 */
public class ActionWhenHitAbility extends Ability {

    private final Consumer<LivingEntity> action;
    private final Predicate<LivingEntity> attackerCondition;
    private final int cooldown;
    private int cooldownRemaining = 0;

    public ActionWhenHitAbility(AbilityType<ActionWhenHitAbility> type, LivingEntity entity,
                                 Consumer<LivingEntity> action,
                                 Predicate<LivingEntity> attackerCondition,
                                 int cooldown) {
        super(type, entity);
        this.action = action;
        this.attackerCondition = attackerCondition;
        this.cooldown = cooldown;
        if (cooldown > 0) setTicking();
    }

    @Override
    public void tick() {
        if (cooldownRemaining > 0) cooldownRemaining--;
    }

    public void onHitBy(LivingEntity attacker) {
        if (!isActive()) return;
        if (cooldownRemaining > 0) return;
        if (attacker != null && attackerCondition != null && !attackerCondition.test(attacker)) return;

        if (action != null) {
            action.accept(entity);
        }
        cooldownRemaining = cooldown;
    }

    public static AbilityFactory<ActionWhenHitAbility> createFactory() {
        return new AbilityFactory<ActionWhenHitAbility>(
            AspectAbilities.identifier("action_when_hit"),
            new SerializableData()
                .add("entity_action", AspectAbilitiesDataTypes.ENTITY_ACTION)
                .add("attacker_condition", AspectAbilitiesDataTypes.ENTITY_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 0),
            data -> (type, entity) -> new ActionWhenHitAbility(
                type, entity,
                data.get("entity_action"),
                data.get("attacker_condition"),
                data.get("cooldown")
            )
        ).allowCondition();
    }
}
