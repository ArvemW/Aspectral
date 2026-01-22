package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.Activatable;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * Power that fires a projectile when activated.
 */
public class FireProjectilePower extends Power implements Activatable {

    private final String projectileType;
    private final float speed;
    private final float divergence;
    private final int count;
    private final int cooldown;
    private final String keyBinding;

    private int cooldownRemaining = 0;

    public FireProjectilePower(PowerType<?> type, LivingEntity entity,
                               String projectileType, float speed, float divergence,
                               int count, int cooldown, String keyBinding) {
        super(type, entity);
        this.projectileType = projectileType;
        this.speed = speed;
        this.divergence = divergence;
        this.count = count;
        this.cooldown = cooldown;
        this.keyBinding = keyBinding;

        if (cooldown > 0) setTicking();
    }

    @Override
    public void tick() {
        if (cooldownRemaining > 0) cooldownRemaining--;
    }

    @Override
    public void onActivate() {
        if (!isActive()) return;
        if (cooldownRemaining > 0) return;

        fireProjectiles();
        cooldownRemaining = cooldown;
    }

    private void fireProjectiles() {
        // This would use Hytale's projectile API
        // For now, this is a placeholder that would be implemented
        // using Hytale's actual projectile spawning system

        for (int i = 0; i < count; i++) {
            // entity.getWorld().spawnProjectile(projectileType, entity.getPosition(),
            //     entity.getLookDirection(), speed, divergence, entity);
        }
    }

    @Override
    public String getKeyBinding() {
        return keyBinding;
    }

    public static PowerFactory<FireProjectilePower> createFactory() {
        return new PowerFactory<FireProjectilePower>(
            AspectPowers.identifier("fire_projectile"),
            new SerializableData()
                .add("projectile", SerializableDataTypes.STRING)
                .add("speed", SerializableDataTypes.FLOAT, 1.5f)
                .add("divergence", SerializableDataTypes.FLOAT, 0.0f)
                .add("count", SerializableDataTypes.INT, 1)
                .add("cooldown", SerializableDataTypes.INT, 20)
                .add("key", SerializableDataTypes.STRING, "none"),
            data -> (type, entity) -> new FireProjectilePower(
                type, entity,
                data.get("projectile"),
                data.get("speed"),
                data.get("divergence"),
                data.get("count"),
                data.get("cooldown"),
                data.get("key")
            )
        ).allowCondition();
    }
}


