package arvem.aspectral.abilities;

import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Base class for all abilities in AspectAbilities.
 * An ability is a special power or trait that can be granted to entities.
 * <p>
 * Abilities can:
 * - Have conditions that determine when they're active
 * - Tick each game tick for ongoing effects
 * - React to lifecycle events (gained, lost, respawn)
 * - Store persistent data
 */
public class Ability {

    protected LivingEntity entity;
    protected AbilityType<?> type;

    protected SerializableData.Instance dataInstance;
    protected SerializableData serializableData;

    private boolean shouldTick = false;
    private boolean shouldTickWhenInactive = false;

    protected List<Predicate<LivingEntity>> conditions;

    public Ability(AbilityType<?> type, LivingEntity entity) {
        this.type = type;
        this.entity = entity;
        this.conditions = new LinkedList<>();
    }

    /**
     * Add a condition that must be met for this ability to be active.
     */
    public Ability addCondition(Predicate<LivingEntity> condition) {
        this.conditions.add(condition);
        return this;
    }

    /**
     * Mark this ability as requiring ticking.
     */
    protected void setTicking() {
        this.setTicking(false);
    }

    /**
     * Mark this ability as requiring ticking.
     * @param evenWhenInactive If true, ticks even when ability conditions are not met
     */
    protected void setTicking(boolean evenWhenInactive) {
        this.shouldTick = true;
        this.shouldTickWhenInactive = evenWhenInactive;
    }

    protected final void setDataInstance(SerializableData.Instance dataInstance) {
        this.dataInstance = dataInstance;
    }

    protected final void setSerializableData(SerializableData serializableData) {
        this.serializableData = serializableData;
    }

    public boolean shouldTick() {
        return shouldTick;
    }

    public boolean shouldTickWhenInactive() {
        return shouldTickWhenInactive;
    }

    /**
     * Called every tick when the ability is active (or always if shouldTickWhenInactive is true).
     */
    public void tick() {
    }

    /**
     * Called when the ability is first granted to an entity.
     */
    public void onGained() {
    }

    /**
     * Called when the ability is removed from an entity.
     */
    public void onLost() {
    }

    /**
     * Called when the ability is added (including after sync/reload).
     */
    public void onAdded() {
    }

    /**
     * Called when the ability is added.
     * @param onSync True if this is from a sync operation
     */
    public void onAdded(boolean onSync) {
        onAdded();
    }

    /**
     * Called when the ability is removed (including before sync/reload).
     */
    public void onRemoved() {
    }

    /**
     * Called when the ability is removed.
     * @param onSync True if this is from a sync operation
     */
    public void onRemoved(boolean onSync) {
        onRemoved();
    }

    /**
     * Called when the entity respawns.
     */
    public void onRespawn() {
    }

    /**
     * Check if this ability is currently active (all conditions pass).
     */
    public boolean isActive() {
        return conditions.stream().allMatch(condition -> condition.test(entity));
    }

    /**
     * Serialize ability state to JSON for persistence.
     */
    public JsonObject toJson() {
        if (serializableData != null && dataInstance != null) {
            return serializableData.write(dataInstance);
        }
        return new JsonObject();
    }

    /**
     * Deserialize ability state from JSON.
     */
    public void fromJson(JsonObject json) {
        // Override in subclasses for custom state
    }

    public AbilityType<?> getType() {
        return type;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * Create a simple factory for abilities that don't need extra data.
     */
    public static AbilityFactory<Ability> createSimpleFactory(
            BiFunction<AbilityType<Ability>, LivingEntity, Ability> constructor,
            String identifier) {
        return new AbilityFactory<>(identifier,
            new SerializableData(),
            data -> (type, entity) -> constructor.apply(type, entity)
        ).allowCondition();
    }
}
