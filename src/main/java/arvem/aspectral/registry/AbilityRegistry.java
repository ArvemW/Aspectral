package arvem.aspectral.registry;

import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.abilities.factory.ConditionFactory;
import arvem.aspectral.abilities.factory.ActionFactory;
import arvem.aspectral.api.LivingEntity;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all ability-related objects.
 * Replaces Minecraft's Registry system with a simpler map-based approach.
 */
public class AbilityRegistry {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Ability type registry
    private final Map<String, AbilityType<?>> abilityTypes = new ConcurrentHashMap<>();

    // Ability factory registry
    private final Map<String, AbilityFactory<?>> abilityFactories = new ConcurrentHashMap<>();

    // Entity condition registry
    private final Map<String, ConditionFactory<LivingEntity>> entityConditions = new ConcurrentHashMap<>();

    // Entity action registry
    private final Map<String, ActionFactory<LivingEntity>> entityActions = new ConcurrentHashMap<>();

    // Bi-entity condition registry (actor, target)
    private final Map<String, ConditionFactory<EntityPair>> biEntityConditions = new ConcurrentHashMap<>();

    // Bi-entity action registry
    private final Map<String, ActionFactory<EntityPair>> biEntityActions = new ConcurrentHashMap<>();

    // ========================================
    // Ability Types
    // ========================================

    public void registerAbilityType(AbilityType<?> abilityType) {
        String id = abilityType.getIdentifier();
        if (abilityTypes.containsKey(id)) {
            LOGGER.atWarning().log("Overwriting existing ability type: %s", id);
        }
        abilityTypes.put(id, abilityType);
        LOGGER.atFine().log("Registered ability type: %s", id);
    }

    public AbilityType<?> getAbilityType(String id) {
        return abilityTypes.get(id);
    }

    public boolean hasAbilityType(String id) {
        return abilityTypes.containsKey(id);
    }

    public Collection<AbilityType<?>> getAllAbilityTypes() {
        return Collections.unmodifiableCollection(abilityTypes.values());
    }

    public Collection<String> getAllAbilityTypeIds() {
        return Collections.unmodifiableCollection(abilityTypes.keySet());
    }

    // ========================================
    // Ability Factories
    // ========================================

    public void registerAbilityFactory(AbilityFactory<?> factory) {
        String id = factory.getSerializerId();
        if (abilityFactories.containsKey(id)) {
            LOGGER.atWarning().log("Overwriting existing ability factory: %s", id);
        }
        abilityFactories.put(id, factory);
        LOGGER.atFine().log("Registered ability factory: %s", id);
    }

    public AbilityFactory<?> getAbilityFactory(String id) {
        return abilityFactories.get(id);
    }

    public boolean hasAbilityFactory(String id) {
        return abilityFactories.containsKey(id);
    }

    public Collection<String> getAllAbilityFactoryIds() {
        return Collections.unmodifiableCollection(abilityFactories.keySet());
    }

    public Collection<AbilityFactory<?>> getAllAbilityFactories() {
        return Collections.unmodifiableCollection(abilityFactories.values());
    }

    // ========================================
    // Entity Conditions
    // ========================================

    public void registerEntityCondition(ConditionFactory<LivingEntity> condition) {
        String id = condition.getSerializerId();
        entityConditions.put(id, condition);
        LOGGER.atFine().log("Registered entity condition: %s", id);
    }

    public ConditionFactory<LivingEntity> getEntityCondition(String id) {
        return entityConditions.get(id);
    }

    public Collection<String> getAllEntityConditionIds() {
        return Collections.unmodifiableCollection(entityConditions.keySet());
    }

    // ========================================
    // Entity Actions
    // ========================================

    public void registerEntityAction(ActionFactory<LivingEntity> action) {
        String id = action.getSerializerId();
        entityActions.put(id, action);
        LOGGER.atFine().log("Registered entity action: %s", id);
    }

    public ActionFactory<LivingEntity> getEntityAction(String id) {
        return entityActions.get(id);
    }

    public Collection<String> getAllEntityActionIds() {
        return Collections.unmodifiableCollection(entityActions.keySet());
    }

    // ========================================
    // Bi-Entity Conditions
    // ========================================

    public void registerBiEntityCondition(ConditionFactory<EntityPair> condition) {
        String id = condition.getSerializerId();
        biEntityConditions.put(id, condition);
        LOGGER.atFine().log("Registered bi-entity condition: %s", id);
    }

    public ConditionFactory<EntityPair> getBiEntityCondition(String id) {
        return biEntityConditions.get(id);
    }

    // ========================================
    // Bi-Entity Actions
    // ========================================

    public void registerBiEntityAction(ActionFactory<EntityPair> action) {
        String id = action.getSerializerId();
        biEntityActions.put(id, action);
        LOGGER.atFine().log("Registered bi-entity action: %s", id);
    }

    public ActionFactory<EntityPair> getBiEntityAction(String id) {
        return biEntityActions.get(id);
    }

    // ========================================
    // Helper Types
    // ========================================

    /**
     * Represents a pair of entities for bi-entity conditions/actions.
     */
    public static class EntityPair {
        public final LivingEntity actor;
        public final LivingEntity target;

        public EntityPair(LivingEntity actor, LivingEntity target) {
            this.actor = actor;
            this.target = target;
        }
    }
}
