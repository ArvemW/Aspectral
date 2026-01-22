package arvem.aspectral.powers.factory.condition;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.component.PowerHolderComponent;
import arvem.aspectral.powers.PowerTypeReference;
import arvem.aspectral.powers.factory.ConditionFactory;
import arvem.aspectral.powers.impl.ResourcePower;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.api.Player;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.registry.PowerTypeRegistry;
import arvem.aspectral.util.Comparison;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Registration for built-in entity conditions.
 * Entity conditions test properties of a single entity.
 */
public class EntityConditions {

    public static void register(PowerTypeRegistry registry) {
        // Always true
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("constant"),
            new SerializableData()
                .add("value", SerializableDataTypes.BOOLEAN, true),
            data -> entity -> data.get("value")
        ));

        // AND condition (all must be true)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("and"),
            new SerializableData()
                .add("conditions", AspectPowersDataTypes.ENTITY_CONDITIONS),
            data -> {
                var conditions = data.<java.util.List<java.util.function.Predicate<LivingEntity>>>get("conditions");
                return entity -> conditions.stream().allMatch(c -> c.test(entity));
            }
        ));

        // OR condition (any must be true)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("or"),
            new SerializableData()
                .add("conditions", AspectPowersDataTypes.ENTITY_CONDITIONS),
            data -> {
                var conditions = data.<java.util.List<java.util.function.Predicate<LivingEntity>>>get("conditions");
                return entity -> conditions.stream().anyMatch(c -> c.test(entity));
            }
        ));

        // NOT condition (invert)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("not"),
            new SerializableData()
                .add("condition", AspectPowersDataTypes.ENTITY_CONDITION),
            data -> {
                var condition = data.<java.util.function.Predicate<LivingEntity>>get("condition");
                return entity -> !condition.test(entity);
            }
        ));

        // Check if entity has an power
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("has_power"),
            new SerializableData()
                .add("power", AspectPowersDataTypes.POWER_TYPE),
            data -> entity -> {
                var powerRef = data.<PowerTypeReference>get("power");
                var powerType = powerRef.getReferencedPowerType();
                if (powerType == null) return false;
                var component = PowerHolderComponent.get(entity);
                return component != null && component.hasPower(powerType);
            }
        ));

        // Check health
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("health"),
            new SerializableData()
                .add("comparison", AspectPowersDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            data -> {
                Comparison comparison = data.get("comparison");
                float compareTo = data.get("compare_to");
                return entity -> comparison.compare(entity.getHealth(), compareTo);
            }
        ));

        // Check if entity is on fire
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("on_fire"),
            new SerializableData(),
            data -> LivingEntity::isOnFire
        ));

        // Check if entity is sneaking
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("sneaking"),
            new SerializableData(),
            data -> LivingEntity::isSneaking
        ));

        // Check if entity is sprinting
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("sprinting"),
            new SerializableData(),
            data -> LivingEntity::isSprinting
        ));

        // Check if entity is swimming
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("swimming"),
            new SerializableData(),
            data -> LivingEntity::isSwimming
        ));

        // Check if entity is on ground
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("on_ground"),
            new SerializableData(),
            data -> LivingEntity::isOnGround
        ));

        // Check if entity is in water
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("in_water"),
            new SerializableData(),
            data -> LivingEntity::isInWater
        ));

        // Check if entity is a player
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("is_player"),
            new SerializableData(),
            data -> entity -> entity instanceof Player
        ));

        // Check if entity is alive
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("alive"),
            new SerializableData(),
            data -> LivingEntity::isAlive
        ));

        // Resource check (for ResourcePower values)
        registry.registerEntityCondition(new ConditionFactory<>(
            AspectPowers.identifier("resource"),
            new SerializableData()
                .add("resource", AspectPowersDataTypes.POWER_TYPE)
                .add("comparison", AspectPowersDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            data -> {
                var resourceRef = data.<PowerTypeReference>get("resource");
                Comparison comparison = data.get("comparison");
                int compareTo = data.get("compare_to");
                return entity -> {
                    var powerType = resourceRef.getReferencedPowerType();
                    if (powerType == null) return false;
                    var component = PowerHolderComponent.get(entity);
                    if (component == null) return false;
                    var power = component.getPower(powerType);
                    if (power instanceof ResourcePower resource) {
                        return comparison.compare(resource.getValue(), compareTo);
                    }
                    return false;
                };
            }
        ));
    }
}


