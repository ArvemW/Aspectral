package arvem.aspectral.powers;

import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Base class for all powers in AspectPowers.
 * An power is a runtime instance of a power - a special trait that can be granted to entities.
 * <p>
 * Powers can:
 * - Have conditions that determine when they're active
 * - Tick each game tick for ongoing effects
 * - React to lifecycle events (gained, lost, respawn)
 * - Store persistent data
 * <p>
 * Note: In Origins terminology, this is the "Power instance" attached to an entity,
 * while the JSON definition is called a "Power" (stored as PowerDefinition in our code).
 */
public class Power {

    protected LivingEntity entity;
    protected PowerType<?> type;

    protected SerializableData.Instance dataInstance;
    protected SerializableData serializableData;

    private boolean shouldTick = false;
    private boolean shouldTickWhenInactive = false;

    protected List<Predicate<LivingEntity>> conditions;

    public Power(PowerType<?> type, LivingEntity entity) {
        this.type = type;
        this.entity = entity;
        this.conditions = new LinkedList<>();
    }

    /**
     * Add a condition that must be met for this power to be active.
     */
    public Power addCondition(Predicate<LivingEntity> condition) {
        this.conditions.add(condition);
        return this;
    }

    /**
     * Mark this power as requiring ticking.
     */
    protected void setTicking() {
        this.setTicking(false);
    }

    /**
     * Mark this power as requiring ticking.
     * @param evenWhenInactive If true, ticks even when power conditions are not met
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
     * Called every tick when the power is active (or always if shouldTickWhenInactive is true).
     */
    public void tick() {
    }

    /**
     * Called when the power is first granted to an entity.
     */
    public void onGained() {
    }

    /**
     * Called when the power is removed from an entity.
     */
    public void onLost() {
    }

    /**
     * Called when the power is added (including after sync/reload).
     */
    public void onAdded() {
    }

    /**
     * Called when the power is added.
     * @param onSync True if this is from a sync operation
     */
    public void onAdded(boolean onSync) {
        onAdded();
    }

    /**
     * Called when the power is removed (including before sync/reload).
     */
    public void onRemoved() {
    }

    /**
     * Called when the power is removed.
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

    // ========================================
    // Event hooks (new additions)
    // ========================================

    /**
     * Called when a key is pressed.
     * @param key The key identifier
     */
    public void onKeyPressed(String key) {
    }

    /**
     * Called when the entity takes damage.
     * @param event Damage event (platform-specific wrapper)
     * @return True to cancel the event
     */
    public boolean onDamage(Object event) {
        return false;
    }

    /**
     * Called when the entity deals damage.
     * @param event Damage event (platform-specific wrapper)
     * @return True to cancel the event
     */
    public boolean onAttack(Object event) {
        return false;
    }

    /**
     * Called when the entity moves.
     * @param event Move event (platform-specific wrapper)
     * @return True to cancel the event
     */
    public boolean onMove(Object event) {
        return false;
    }

    /**
     * Check if this power is currently active (all conditions pass).
     */
    public boolean isActive() {
        return conditions.stream().allMatch(condition -> condition.test(entity));
    }

    /**
     * Serialize power state to JSON for persistence.
     */
    public JsonObject toJson() {
        if (serializableData != null && dataInstance != null) {
            return serializableData.write(dataInstance);
        }
        return new JsonObject();
    }

    /**
     * Deserialize power state from JSON.
     */
    public void fromJson(JsonObject json) {
        // Override in subclasses for custom state
    }

    public PowerType<?> getType() {
        return type;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * Create a simple factory for powers that don't need extra data.
     */
    public static PowerFactory<Power> createSimpleFactory(
            BiFunction<PowerType<Power>, LivingEntity, Power> constructor,
            String identifier) {
        return new PowerFactory<>(identifier,
            new SerializableData(),
            data -> (type, entity) -> constructor.apply(type, entity)
        ).allowCondition();
    }
}

