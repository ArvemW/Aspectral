package arvem.aspectral.api;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper for Hytale's LivingEntity that provides clean access to entity properties
 * through Hytale's ECS (Entity Component System) architecture.
 *
 * This class properly integrates with Hytale's component system rather than using
 * deprecated direct methods. All state access goes through the appropriate components.
 */
public class HytaleLivingEntityAdapter implements LivingEntity {

    private final com.hypixel.hytale.server.core.entity.LivingEntity entity;
    private final Ref<EntityStore> entityRef;
    private final Store<EntityStore> store;

    /**
     * Create an adapter for a Hytale LivingEntity.
     *
     * @param entity The Hytale LivingEntity to wrap
     * @param entityRef The entity reference in the ECS store
     * @param store The entity store
     */
    public HytaleLivingEntityAdapter(
            @Nonnull com.hypixel.hytale.server.core.entity.LivingEntity entity,
            @Nonnull Ref<EntityStore> entityRef,
            @Nonnull Store<EntityStore> store) {
        this.entity = entity;
        this.entityRef = entityRef;
        this.store = store;
    }

    /**
     * Get the underlying Hytale LivingEntity.
     */
    @Nonnull
    public com.hypixel.hytale.server.core.entity.LivingEntity getEntity() {
        return entity;
    }

    /**
     * Get the entity reference for ECS operations.
     */
    @Nonnull
    public Ref<EntityStore> getEntityRef() {
        return entityRef;
    }

    /**
     * Get the entity store.
     */
    @Nonnull
    public Store<EntityStore> getStore() {
        return store;
    }

    // ========================================
    // LivingEntity Interface Implementation
    // ========================================

    @Override
    public long getEntityId() {
        return entityRef.getIndex();
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getHealth() {
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
        if (statMap != null) {
            // Health is typically the first stat or "Health" stat
            var healthStat = statMap.get("Health");
            if (healthStat != null) {
                return healthStat.get();
            }
        }
        return 0f;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setHealth(float health) {
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
        if (statMap != null) {
            var healthStat = statMap.get("Health");
            if (healthStat != null) {
                statMap.setStatValue(healthStat.getIndex(), health);
            }
        }
    }

    @Override
    public void heal(float amount) {
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
        if (statMap != null) {
            @SuppressWarnings("deprecation")
            var healthStat = statMap.get("Health");
            if (healthStat != null) {
                statMap.addStatValue(healthStat.getIndex(), amount);
            }
        }
    }

    /**
     * Get the maximum health of this entity.
     */
    @SuppressWarnings("deprecation")
    public float getMaxHealth() {
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
        if (statMap != null) {
            var healthStat = statMap.get("Health");
            if (healthStat != null) {
                return healthStat.getMax();
            }
        }
        return 20f; // Default max health
    }

    @Override
    public void damage(float amount) {
        // Use negative amount to reduce health
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
        if (statMap != null) {
            @SuppressWarnings("deprecation")
            var healthStat = statMap.get("Health");
            if (healthStat != null) {
                statMap.addStatValue(healthStat.getIndex(), -amount);
            }
        }
    }

    @Override
    public boolean isOnFire() {
        EffectControllerComponent effectController = store.getComponent(entityRef, EffectControllerComponent.getComponentType());
        if (effectController != null) {
            // Check for fire-related effects
            // This would need the actual fire effect index from Hytale's asset system
            return false; // TODO: Check for fire effect
        }
        return false;
    }

    @Override
    public void setOnFire(int ticks) {
        EffectControllerComponent effectController = store.getComponent(entityRef, EffectControllerComponent.getComponentType());
        if (effectController != null) {
            // Apply fire effect through the effect system
            // TODO: Add fire effect with duration
        }
    }

    @Override
    public void extinguish() {
        EffectControllerComponent effectController = store.getComponent(entityRef, EffectControllerComponent.getComponentType());
        if (effectController != null) {
            // Remove fire effect
            // TODO: Remove fire effect
        }
    }

    @Override
    public boolean isSneaking() {
        MovementStates states = getMovementStates();
        return states != null && states.crouching;
    }

    @Override
    public boolean isSprinting() {
        MovementStates states = getMovementStates();
        return states != null && states.sprinting;
    }

    @Override
    public boolean isSwimming() {
        MovementStates states = getMovementStates();
        return states != null && states.swimming;
    }

    @Override
    public boolean isOnGround() {
        MovementStates states = getMovementStates();
        return states != null && states.onGround;
    }

    @Override
    public boolean isInWater() {
        MovementStates states = getMovementStates();
        return states != null && states.inFluid;
    }

    @Override
    public boolean isAlive() {
        return !entity.wasRemoved() && getHealth() > 0;
    }

    @Override
    public void kill() {
        setHealth(0);
        entity.remove();
    }

    @Override
    public void addVelocity(float x, float y, float z) {
        Velocity velocity = store.getComponent(entityRef, Velocity.getComponentType());
        if (velocity != null) {
            velocity.addForce(x, y, z);
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Get the movement states for this entity.
     */
    @Nullable
    public MovementStates getMovementStates() {
        MovementStatesComponent component = store.getComponent(entityRef, MovementStatesComponent.getComponentType());
        return component != null ? component.getMovementStates() : null;
    }

    /**
     * Get the transform component for position access.
     */
    @Nullable
    public TransformComponent getTransform() {
        return store.getComponent(entityRef, TransformComponent.getComponentType());
    }

    /**
     * Get the effect controller for status effects.
     */
    @Nullable
    public EffectControllerComponent getEffectController() {
        return store.getComponent(entityRef, EffectControllerComponent.getComponentType());
    }

    /**
     * Get the entity stat map for stats like health.
     */
    @Nullable
    public EntityStatMap getStatMap() {
        return store.getComponent(entityRef, EntityStatMap.getComponentType());
    }

    /**
     * Get the velocity component.
     */
    @Nullable
    public Velocity getVelocity() {
        return store.getComponent(entityRef, Velocity.getComponentType());
    }

    /**
     * Check if this entity is flying.
     */
    public boolean isFlying() {
        MovementStates states = getMovementStates();
        return states != null && states.flying;
    }

    /**
     * Check if this entity is gliding.
     */
    public boolean isGliding() {
        MovementStates states = getMovementStates();
        return states != null && states.gliding;
    }

    /**
     * Check if this entity is climbing.
     */
    public boolean isClimbing() {
        MovementStates states = getMovementStates();
        return states != null && states.climbing;
    }

    /**
     * Check if this entity is falling.
     */
    public boolean isFalling() {
        MovementStates states = getMovementStates();
        return states != null && states.falling;
    }

    /**
     * Check if this entity is jumping.
     */
    public boolean isJumping() {
        MovementStates states = getMovementStates();
        return states != null && states.jumping;
    }

    /**
     * Check if this entity is sleeping.
     */
    public boolean isSleeping() {
        MovementStates states = getMovementStates();
        return states != null && states.sleeping;
    }

    /**
     * Check if this entity is rolling.
     */
    public boolean isRolling() {
        MovementStates states = getMovementStates();
        return states != null && states.rolling;
    }

    /**
     * Check if this entity is sitting.
     */
    public boolean isSitting() {
        MovementStates states = getMovementStates();
        return states != null && states.sitting;
    }

    /**
     * Check if this entity is mounted on something.
     */
    public boolean isMounting() {
        MovementStates states = getMovementStates();
        return states != null && states.mounting;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HytaleLivingEntityAdapter other) {
            return entityRef.getIndex() == other.entityRef.getIndex();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return entityRef.getIndex();
    }

    @Override
    public String toString() {
        return "HytaleLivingEntityAdapter[id=" + entityRef.getIndex() + ", entity=" + entity + "]";
    }
}
