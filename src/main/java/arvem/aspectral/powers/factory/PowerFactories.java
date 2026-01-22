package arvem.aspectral.powers.factory;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.impl.*;
import arvem.aspectral.registry.PowerTypeRegistry;
import arvem.aspectral.data.SerializableData;

/**
 * Registration for all built-in power factories.
 * These are the core power types available in AspectPowers.
 * <p>
 * Note: Only powers that make sense for Hytale are included.
 * Minecraft-specific powers (elytra, hunger, etc.) are omitted.
 */
public class PowerFactories {

    /**
     * Register all built-in power factories.
     */
    public static void register(PowerTypeRegistry registry) {
        // Simple power (just conditions, no special behavior)
        registry.registerPowerFactory(new PowerFactory<>(
            AspectPowers.identifier("simple"),
            new SerializableData(),
            data -> Power::new
        ).allowCondition());

        // Toggle power (can be activated/deactivated)
        registry.registerPowerFactory(TogglePower.createFactory());

        // Cooldown power (has a cooldown between uses)
        registry.registerPowerFactory(CooldownPower.createCooldownFactory());

        // Active cooldown power (triggered power with cooldown)
        registry.registerPowerFactory(ActiveCooldownPower.createFactory());

        // Resource power (numeric resource with min/max)
        registry.registerPowerFactory(ResourcePower.createFactory());

        // Attribute power (modifies entity attributes)
        registry.registerPowerFactory(AttributePower.createFactory());

        // Action over time (periodic action execution)
        registry.registerPowerFactory(ActionOverTimePower.createFactory());

        // Action on callback (action on specific events)
        registry.registerPowerFactory(ActionOnCallbackPower.createFactory());

        // Damage over time
        registry.registerPowerFactory(DamageOverTimePower.createFactory());

        // Fire immunity
        registry.registerPowerFactory(FireImmunityPower.createFactory());

        // Invisibility
        registry.registerPowerFactory(InvisibilityPower.createFactory());

        // Invulnerability
        registry.registerPowerFactory(InvulnerabilityPower.createFactory());

        // Climbing (wall climbing)
        registry.registerPowerFactory(ClimbingPower.createFactory());

        // Swimming (enhanced swimming)
        registry.registerPowerFactory(Power.createSimpleFactory(
            SwimmingPower::new, AspectPowers.identifier("swimming")));

        // Modify damage dealt
        registry.registerPowerFactory(ModifyDamageDealtPower.createFactory());

        // Modify damage taken
        registry.registerPowerFactory(ModifyDamageTakenPower.createFactory());

        // Modify jump
        registry.registerPowerFactory(ModifyJumpPower.createFactory());

        // Modify movement speed
        registry.registerPowerFactory(ModifyMovementSpeedPower.createFactory());

        // Prevent death
        registry.registerPowerFactory(PreventDeathPower.createFactory());

        // Self action on hit
        registry.registerPowerFactory(SelfActionOnHitPower.createFactory());

        // Self action on kill
        registry.registerPowerFactory(SelfActionOnKillPower.createFactory());

        // Target action on hit
        registry.registerPowerFactory(TargetActionOnHitPower.createFactory());

        // Action when hit
        registry.registerPowerFactory(ActionWhenHitPower.createFactory());

        // Attacker action when hit
        registry.registerPowerFactory(AttackerActionWhenHitPower.createFactory());

        // Entity glow (make entities glow for this player)
        registry.registerPowerFactory(EntityGlowPower.createFactory());

        // Self glow (make self glow)
        registry.registerPowerFactory(SelfGlowPower.createFactory());

        // Fire projectile power
        registry.registerPowerFactory(FireProjectilePower.createFactory());

        // Multiple powers container
        registry.registerPowerFactory(MultiplePower.createFactory());

        // Launch power (propel player upward)
        var launchFactory = LaunchPower.createFactory();
        registry.registerPowerFactory(launchFactory);
        System.out.println("[DEBUG] Registered LaunchAbility factory: " + launchFactory.getSerializerId());

        // Attribute modifier power (modify single attribute)
        var attrModFactory = AttributeModifierPower.createFactory();
        registry.registerPowerFactory(attrModFactory);
        System.out.println("[DEBUG] Registered AttributeModifierAbility factory: " + attrModFactory.getSerializerId());
    }
}


