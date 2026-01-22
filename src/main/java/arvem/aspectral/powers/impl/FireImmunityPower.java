package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;

/**
 * Fire immunity power - prevents fire damage.
 * Hooks into damage events to cancel fire damage.
 */
public class FireImmunityPower extends Power {

    public FireImmunityPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    @Override
    public boolean onDamage(Object event) {
        if (!isActive()) {
            return false;
        }

        // TODO: Check if damage source is fire/lava using Hytale API
        // For now, we assume the event contains damage source information
        // Example:
        // if (event instanceof PlayerDamageEvent damageEvent) {
        //     DamageSource source = damageEvent.getSource();
        //     if (source.isFire() || source.isLava()) {
        //         damageEvent.setCancelled(true);
        //         return true; // Cancelled the damage
        //     }
        // }

        AspectPowers.getLogger().atFine().log(
            "Fire immunity active - checking damage source");

        // Return false for now (don't cancel) until we have proper damage event API
        return false;
    }

    /**
     * Check if the entity should be immune to fire damage.
     * Can be called by other systems.
     */
    public boolean shouldPreventFireDamage() {
        return isActive();
    }

    public static PowerFactory<FireImmunityPower> createFactory() {
        return new PowerFactory<FireImmunityPower>(
            AspectPowers.identifier("fire_immunity"),
            new SerializableData(),
            data -> FireImmunityPower::new
        ).allowCondition();
    }
}


