package arvem.aspectral.abilities.factory.condition;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.AbilityTypeReference;
import arvem.aspectral.abilities.factory.ConditionFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.api.Player;
import arvem.aspectral.component.AbilityHolderComponent;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.registry.AbilityRegistry;
import arvem.aspectral.util.Comparison;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Registration for built-in entity conditions.
 * Entity conditions test properties of a single entity.
 */
public class EntityConditions {

    public static void register(AbilityRegistry registry) {
        // Always true
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("constant"),
            new SerializableData()
                .add("value", SerializableDataTypes.BOOLEAN, true),
            data -> entity -> data.get("value")
        ));

        // AND condition (all must be true)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("and"),
            new SerializableData()
                .add("conditions", AspectAbilitiesDataTypes.ENTITY_CONDITIONS),
            data -> {
                var conditions = data.<java.util.List<java.util.function.Predicate<LivingEntity>>>get("conditions");
                return entity -> conditions.stream().allMatch(c -> c.test(entity));
            }
        ));

        // OR condition (any must be true)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("or"),
            new SerializableData()
                .add("conditions", AspectAbilitiesDataTypes.ENTITY_CONDITIONS),
            data -> {
                var conditions = data.<java.util.List<java.util.function.Predicate<LivingEntity>>>get("conditions");
                return entity -> conditions.stream().anyMatch(c -> c.test(entity));
            }
        ));

        // NOT condition (invert)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("not"),
            new SerializableData()
                .add("condition", AspectAbilitiesDataTypes.ENTITY_CONDITION),
            data -> {
                var condition = data.<java.util.function.Predicate<LivingEntity>>get("condition");
                return entity -> !condition.test(entity);
            }
        ));

        // Check if entity has an ability
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("has_ability"),
            new SerializableData()
                .add("ability", AspectAbilitiesDataTypes.ABILITY_TYPE),
            data -> entity -> {
                var abilityRef = data.<AbilityTypeReference>get("ability");
                var abilityType = abilityRef.getReferencedAbilityType();
                if (abilityType == null) return false;
                var component = AbilityHolderComponent.get(entity);
                return component != null && component.hasAbility(abilityType);
            }
        ));

        // Check health
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("health"),
            new SerializableData()
                .add("comparison", AspectAbilitiesDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            data -> {
                Comparison comparison = data.get("comparison");
                float compareTo = data.get("compare_to");
                return entity -> comparison.compare(entity.getHealth(), compareTo);
            }
        ));

        // Check if entity is on fire
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("on_fire"),
            new SerializableData(),
            data -> LivingEntity::isOnFire
        ));

        // Check if entity is sneaking
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("sneaking"),
            new SerializableData(),
            data -> LivingEntity::isSneaking
        ));

        // Check if entity is sprinting
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("sprinting"),
            new SerializableData(),
            data -> LivingEntity::isSprinting
        ));

        // Check if entity is swimming
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("swimming"),
            new SerializableData(),
            data -> LivingEntity::isSwimming
        ));

        // Check if entity is on ground
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("on_ground"),
            new SerializableData(),
            data -> LivingEntity::isOnGround
        ));

        // Check if entity is in water
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("in_water"),
            new SerializableData(),
            data -> LivingEntity::isInWater
        ));

        // Check if entity is a player
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("is_player"),
            new SerializableData(),
            data -> entity -> entity instanceof Player
        ));

        // Check if entity is alive
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("alive"),
            new SerializableData(),
            data -> LivingEntity::isAlive
        ));

        // Resource check (for ResourceAbility values)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectAbilities.identifier("resource"),
            new SerializableData()
                .add("resource", AspectAbilitiesDataTypes.ABILITY_TYPE)
                .add("comparison", AspectAbilitiesDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            data -> {
                var resourceRef = data.<AbilityTypeReference>get("resource");
                Comparison comparison = data.get("comparison");
                int compareTo = data.get("compare_to");
                return entity -> {
                    var abilityType = resourceRef.getReferencedAbilityType();
                    if (abilityType == null) return false;
                    var component = AbilityHolderComponent.get(entity);
                    if (component == null) return false;
                    var ability = component.getAbility(abilityType);
                    if (ability instanceof arvem.aspectral.abilities.impl.ResourceAbility resource) {
                        return comparison.compare(resource.getValue(), compareTo);
                    }
                    return false;
                };
            }
        ));
    }
}
