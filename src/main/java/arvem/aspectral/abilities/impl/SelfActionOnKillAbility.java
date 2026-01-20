package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Action triggered when this entity kills another.
 */
public class SelfActionOnKillAbility extends Ability {

    private final Consumer<LivingEntity> action;
    private final Predicate<LivingEntity> targetCondition;

    public SelfActionOnKillAbility(AbilityType<SelfActionOnKillAbility> type, LivingEntity entity,
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

    public static AbilityFactory<SelfActionOnKillAbility> createFactory() {
        return new AbilityFactory<SelfActionOnKillAbility>(
            AspectAbilities.identifier("self_action_on_kill"),
            new SerializableData()
                .add("entity_action", AspectAbilitiesDataTypes.ENTITY_ACTION)
                .add("target_condition", AspectAbilitiesDataTypes.ENTITY_CONDITION, null),
            data -> (type, entity) -> new SelfActionOnKillAbility(
                type, entity,
                data.get("entity_action"),
                data.get("target_condition")
            )
        ).allowCondition();
    }
}
