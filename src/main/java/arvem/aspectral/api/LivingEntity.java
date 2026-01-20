package arvem.aspectral.api;

/**
 * Placeholder interface representing a server-side living entity in Hytale.
 * This should be replaced with the actual Hytale API class when available.
 * <p>
 * For now, this defines the expected interface for entities that can have abilities.
 */
public interface LivingEntity {

    /**
     * Get the unique runtime ID of this entity.
     */
    long getEntityId();

    /**
     * Get the current health of this entity.
     */
    float getHealth();

    /**
     * Set the health of this entity.
     */
    void setHealth(float health);

    /**
     * Heal this entity by the specified amount.
     */
    void heal(float amount);

    /**
     * Deal damage to this entity.
     */
    void damage(float amount);

    /**
     * Check if this entity is on fire.
     */
    boolean isOnFire();

    /**
     * Set this entity on fire for the specified duration (ticks).
     */
    void setOnFire(int ticks);

    /**
     * Extinguish fire on this entity.
     */
    void extinguish();

    /**
     * Check if this entity is sneaking.
     */
    boolean isSneaking();

    /**
     * Check if this entity is sprinting.
     */
    boolean isSprinting();

    /**
     * Check if this entity is swimming.
     */
    boolean isSwimming();

    /**
     * Check if this entity is on the ground.
     */
    boolean isOnGround();

    /**
     * Check if this entity is in water.
     */
    boolean isInWater();

    /**
     * Check if this entity is alive.
     */
    boolean isAlive();

    /**
     * Kill this entity.
     */
    void kill();

    /**
     * Add velocity to this entity.
     */
    void addVelocity(float x, float y, float z);
}
