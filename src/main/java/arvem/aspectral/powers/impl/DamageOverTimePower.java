package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * A power that deals damage over time.
 */
public class DamageOverTimePower extends Power {

    private final int interval;
    private final float damage;
    private final int onset;

    private int tickCount = 0;
    private int onsetCount = 0;
    private boolean onsetComplete = false;

    public DamageOverTimePower(PowerType<?> type, LivingEntity entity,
                               int interval, float damage, int onset) {
        super(type, entity);
        this.interval = interval;
        this.damage = damage;
        this.onset = onset;
        setTicking();
    }

    @Override
    public void tick() {
        if (!isActive()) {
            // Reset when inactive
            onsetComplete = false;
            onsetCount = 0;
            tickCount = 0;
            return;
        }

        // Handle onset delay
        if (!onsetComplete) {
            onsetCount++;
            if (onsetCount >= onset) {
                onsetComplete = true;
                tickCount = interval; // Trigger immediately after onset
            }
            return;
        }

        // Deal damage at intervals
        tickCount++;
        if (tickCount >= interval) {
            entity.damage(damage);
            tickCount = 0;
        }
    }

    @Override
    public void onGained() {
        onsetComplete = false;
        onsetCount = 0;
        tickCount = 0;
    }

    public static PowerFactory<DamageOverTimePower> createFactory() {
        return new PowerFactory<DamageOverTimePower>(
            AspectPowers.identifier("damage_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .add("damage", SerializableDataTypes.FLOAT, 1.0f)
                .add("onset", SerializableDataTypes.INT, 0),
            data -> (type, entity) -> new DamageOverTimePower(
                type, entity,
                data.get("interval"),
                data.get("damage"),
                data.get("onset")
            )
        ).allowCondition();
    }
}


