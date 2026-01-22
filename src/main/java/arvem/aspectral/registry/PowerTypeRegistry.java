package arvem.aspectral.registry;

import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.ConditionFactory;
import arvem.aspectral.powers.factory.ActionFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.powers.factory.PowerFactory;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all power-related objects (Power types, factories, conditions, actions).
 * Replaces Minecraft's Registry system with a simpler map-based approach.
 * <p>
 * Note: "Power" here refers to the implementation classes, not the JSON definitions.
 * JSON power definitions are stored in PowerTypeRegistry.
 */
public class PowerTypeRegistry {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Power type registry
    private final Map<String, PowerType<?>> PowerTypes = new ConcurrentHashMap<>();

    // Power factory registry
    private final Map<String, PowerFactory<?>> powerFactories = new ConcurrentHashMap<>();

    // Entity condition registry
    private final Map<String, ConditionFactory<LivingEntity>> entityConditions = new ConcurrentHashMap<>();

    // Entity action registry
    private final Map<String, ActionFactory<LivingEntity>> entityActions = new ConcurrentHashMap<>();

    // Bi-entity condition registry (actor, target)
    private final Map<String, ConditionFactory<EntityPair>> biEntityConditions = new ConcurrentHashMap<>();

    // Bi-entity action registry
    private final Map<String, ActionFactory<EntityPair>> biEntityActions = new ConcurrentHashMap<>();

    // ========================================
    // Power Types
    // ========================================

    public void registerPowerType(PowerType<?> powerType) {
        String id = powerType.getIdentifier();
        if (PowerTypes.containsKey(id)) {
            LOGGER.atWarning().log("Overwriting existing Power type: %s", id);
        }
        PowerTypes.put(id, powerType);
        LOGGER.atFine().log("Registered Power type: %s", id);
    }

    public PowerType<?> getPowerType(String id) {
        return PowerTypes.get(id);
    }

    public boolean hasPowerType(String id) {
        return PowerTypes.containsKey(id);
    }

    public Collection<PowerType<?>> getAllPowerTypes() {
        return Collections.unmodifiableCollection(PowerTypes.values());
    }

    public Collection<String> getAllPowerTypeIds() {
        return Collections.unmodifiableCollection(PowerTypes.keySet());
    }

    // ========================================
    // Power Factories
    // ========================================

    public void registerPowerFactory(PowerFactory<?> factory) {
        String id = factory.getSerializerId();
        if (powerFactories.containsKey(id)) {
            LOGGER.atWarning().log("Overwriting existing power factory: %s", id);
        }
        powerFactories.put(id, factory);
        LOGGER.atFine().log("Registered power factory: %s", id);
    }

    public PowerFactory<?> getPowerFactory(String id) {
        return powerFactories.get(id);
    }

    public boolean hasPowerFactory(String id) {
        return powerFactories.containsKey(id);
    }

    public Collection<String> getAllPowerFactoryIds() {
        return Collections.unmodifiableCollection(powerFactories.keySet());
    }

    public Collection<PowerFactory<?>> getAllPowerFactories() {
        return Collections.unmodifiableCollection(powerFactories.values());
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

    /**
     * Get the count of registered aspects.
     */
    public int size() {
        return powerFactories.size();
    }
}