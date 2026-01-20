package arvem.aspectral.abilities.factory;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.impl.*;
import arvem.aspectral.registry.AbilityRegistry;
import arvem.aspectral.data.SerializableData;

/**
 * Registration for all built-in ability factories.
 * These are the core ability types available in AspectAbilities.
 * <p>
 * Note: Only abilities that make sense for Hytale are included.
 * Minecraft-specific abilities (elytra, hunger, etc.) are omitted.
 */
public class AbilityFactories {

    /**
     * Register all built-in ability factories.
     */
    public static void register(AbilityRegistry registry) {
        // Simple ability (just conditions, no special behavior)
        registry.registerAbilityFactory(new AbilityFactory<>(
            AspectAbilities.identifier("simple"),
            new SerializableData(),
            data -> Ability::new
        ).allowCondition());

        // Toggle ability (can be activated/deactivated)
        registry.registerAbilityFactory(ToggleAbility.createFactory());

        // Cooldown ability (has a cooldown between uses)
        registry.registerAbilityFactory(CooldownAbility.createCooldownFactory());

        // Active cooldown ability (triggered ability with cooldown)
        registry.registerAbilityFactory(ActiveCooldownAbility.createFactory());

        // Resource ability (numeric resource with min/max)
        registry.registerAbilityFactory(ResourceAbility.createFactory());

        // Attribute ability (modifies entity attributes)
        registry.registerAbilityFactory(AttributeAbility.createFactory());

        // Action over time (periodic action execution)
        registry.registerAbilityFactory(ActionOverTimeAbility.createFactory());

        // Action on callback (action on specific events)
        registry.registerAbilityFactory(ActionOnCallbackAbility.createFactory());

        // Damage over time
        registry.registerAbilityFactory(DamageOverTimeAbility.createFactory());

        // Fire immunity
        registry.registerAbilityFactory(Ability.createSimpleFactory(
            FireImmunityAbility::new, AspectAbilities.identifier("fire_immunity")));

        // Invisibility
        registry.registerAbilityFactory(InvisibilityAbility.createFactory());

        // Invulnerability
        registry.registerAbilityFactory(InvulnerabilityAbility.createFactory());

        // Climbing (wall climbing)
        registry.registerAbilityFactory(ClimbingAbility.createFactory());

        // Swimming (enhanced swimming)
        registry.registerAbilityFactory(Ability.createSimpleFactory(
            SwimmingAbility::new, AspectAbilities.identifier("swimming")));

        // Modify damage dealt
        registry.registerAbilityFactory(ModifyDamageDealtAbility.createFactory());

        // Modify damage taken
        registry.registerAbilityFactory(ModifyDamageTakenAbility.createFactory());

        // Modify jump
        registry.registerAbilityFactory(ModifyJumpAbility.createFactory());

        // Modify movement speed
        registry.registerAbilityFactory(ModifyMovementSpeedAbility.createFactory());

        // Prevent death
        registry.registerAbilityFactory(PreventDeathAbility.createFactory());

        // Self action on hit
        registry.registerAbilityFactory(SelfActionOnHitAbility.createFactory());

        // Self action on kill
        registry.registerAbilityFactory(SelfActionOnKillAbility.createFactory());

        // Target action on hit
        registry.registerAbilityFactory(TargetActionOnHitAbility.createFactory());

        // Action when hit
        registry.registerAbilityFactory(ActionWhenHitAbility.createFactory());

        // Attacker action when hit
        registry.registerAbilityFactory(AttackerActionWhenHitAbility.createFactory());

        // Entity glow (make entities glow for this player)
        registry.registerAbilityFactory(EntityGlowAbility.createFactory());

        // Self glow (make self glow)
        registry.registerAbilityFactory(SelfGlowAbility.createFactory());

        // Fire projectile ability
        registry.registerAbilityFactory(FireProjectileAbility.createFactory());

        // Multiple abilities container
        registry.registerAbilityFactory(MultipleAbility.createFactory());
    }
}
