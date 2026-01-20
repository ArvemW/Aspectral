package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * An ability that deals damage over time.
 */
public class DamageOverTimeAbility extends Ability {

    private final int interval;
    private final float damage;
    private final int onset;

    private int tickCount = 0;
    private int onsetCount = 0;
    private boolean onsetComplete = false;

    public DamageOverTimeAbility(AbilityType<?> type, LivingEntity entity,
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

    public static AbilityFactory<DamageOverTimeAbility> createFactory() {
        return new AbilityFactory<DamageOverTimeAbility>(
            AspectAbilities.identifier("damage_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .add("damage", SerializableDataTypes.FLOAT, 1.0f)
                .add("onset", SerializableDataTypes.INT, 0),
            data -> (type, entity) -> new DamageOverTimeAbility(
                type, entity,
                data.get("interval"),
                data.get("damage"),
                data.get("onset")
            )
        ).allowCondition();
    }
}
