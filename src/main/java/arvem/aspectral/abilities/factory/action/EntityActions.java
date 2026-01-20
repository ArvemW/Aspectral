package arvem.aspectral.abilities.factory.action;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.AbilityTypeReference;
import arvem.aspectral.abilities.factory.ActionFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.api.Player;
import arvem.aspectral.component.AbilityHolderComponent;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.registry.AbilityRegistry;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.List;
import java.util.function.Consumer;

/**
 * Registration for built-in entity actions.
 * Entity actions perform operations on a single entity.
 */
public class EntityActions {

    public static void register(AbilityRegistry registry) {
        // Do nothing action
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("nothing"),
            new SerializableData(),
            data -> entity -> {}
        ));

        // Execute multiple actions
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("and"),
            new SerializableData()
                .add("actions", AspectAbilitiesDataTypes.ENTITY_ACTIONS),
            data -> {
                List<Consumer<LivingEntity>> actions = data.get("actions");
                return entity -> actions.forEach(action -> action.accept(entity));
            }
        ));

        // Conditional action
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("if_else"),
            new SerializableData()
                .add("condition", AspectAbilitiesDataTypes.ENTITY_CONDITION)
                .add("if_action", AspectAbilitiesDataTypes.ENTITY_ACTION, null)
                .add("else_action", AspectAbilitiesDataTypes.ENTITY_ACTION, null),
            data -> {
                var condition = data.<java.util.function.Predicate<LivingEntity>>get("condition");
                Consumer<LivingEntity> ifAction = data.get("if_action");
                Consumer<LivingEntity> elseAction = data.get("else_action");
                return entity -> {
                    if (condition.test(entity)) {
                        if (ifAction != null) ifAction.accept(entity);
                    } else {
                        if (elseAction != null) elseAction.accept(entity);
                    }
                };
            }
        ));

        // Delayed action
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("delay"),
            new SerializableData()
                .add("ticks", SerializableDataTypes.INT)
                .add("action", AspectAbilitiesDataTypes.ENTITY_ACTION),
            data -> {
                int ticks = data.get("ticks");
                Consumer<LivingEntity> action = data.get("action");
                return entity -> AspectAbilities.SCHEDULER.schedule(() -> action.accept(entity), ticks);
            }
        ));

        // Heal entity
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("heal"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT),
            data -> {
                float amount = data.get("amount");
                return entity -> entity.heal(amount);
            }
        ));

        // Damage entity
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("damage"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT)
                .add("source", SerializableDataTypes.STRING, "generic"),
            data -> {
                float amount = data.get("amount");
                String source = data.get("source");
                return entity -> entity.damage(amount);
            }
        ));

        // Set on fire
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("set_on_fire"),
            new SerializableData()
                .add("duration", SerializableDataTypes.INT),
            data -> {
                int duration = data.get("duration");
                return entity -> entity.setOnFire(duration);
            }
        ));

        // Extinguish fire
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("extinguish"),
            new SerializableData(),
            data -> LivingEntity::extinguish
        ));

        // Kill entity
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("kill"),
            new SerializableData(),
            data -> LivingEntity::kill
        ));

        // Add ability
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("grant_ability"),
            new SerializableData()
                .add("ability", AspectAbilitiesDataTypes.ABILITY_TYPE)
                .add("source", SerializableDataTypes.STRING, "action"),
            data -> {
                var abilityRef = data.<AbilityTypeReference>get("ability");
                String source = data.get("source");
                return entity -> {
                    var abilityType = abilityRef.getReferencedAbilityType();
                    if (abilityType != null) {
                        var component = AbilityHolderComponent.getOrCreate(entity);
                        component.addAbility(abilityType, source);
                    }
                };
            }
        ));

        // Remove ability
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("revoke_ability"),
            new SerializableData()
                .add("ability", AspectAbilitiesDataTypes.ABILITY_TYPE)
                .add("source", SerializableDataTypes.STRING, "action"),
            data -> {
                var abilityRef = data.<AbilityTypeReference>get("ability");
                String source = data.get("source");
                return entity -> {
                    var abilityType = abilityRef.getReferencedAbilityType();
                    if (abilityType != null) {
                        var component = AbilityHolderComponent.get(entity);
                        if (component != null) {
                            component.removeAbility(abilityType, source);
                        }
                    }
                };
            }
        ));

        // Modify resource
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("change_resource"),
            new SerializableData()
                .add("resource", AspectAbilitiesDataTypes.ABILITY_TYPE)
                .add("change", SerializableDataTypes.INT),
            data -> {
                var resourceRef = data.<AbilityTypeReference>get("resource");
                int change = data.get("change");
                return entity -> {
                    var abilityType = resourceRef.getReferencedAbilityType();
                    if (abilityType != null) {
                        var component = AbilityHolderComponent.get(entity);
                        if (component != null) {
                            var ability = component.getAbility(abilityType);
                            if (ability instanceof arvem.aspectral.abilities.impl.ResourceAbility resource) {
                                resource.change(change);
                            }
                        }
                    }
                };
            }
        ));

        // Execute command as player (if entity is player)
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("execute_command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING),
            data -> {
                String command = data.get("command");
                return entity -> {
                    if (entity instanceof Player player) {
                        // Execute command through server
                        // AspectAbilities.getServer().getCommandManager().executeCommand(player, command);
                    }
                };
            }
        ));

        // Send message to player
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("send_message"),
            new SerializableData()
                .add("message", SerializableDataTypes.STRING),
            data -> {
                String message = data.get("message");
                return entity -> {
                    if (entity instanceof Player player) {
                        player.sendMessage(message);
                    }
                };
            }
        ));

        // Apply velocity
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("add_velocity"),
            new SerializableData()
                .add("x", SerializableDataTypes.FLOAT, 0f)
                .add("y", SerializableDataTypes.FLOAT, 0f)
                .add("z", SerializableDataTypes.FLOAT, 0f),
            data -> {
                float x = data.get("x");
                float y = data.get("y");
                float z = data.get("z");
                return entity -> entity.addVelocity(x, y, z);
            }
        ));

        // Trigger cooldown
        registry.registerEntityAction(new ActionFactory<>(
            AspectAbilities.identifier("trigger_cooldown"),
            new SerializableData()
                .add("ability", AspectAbilitiesDataTypes.ABILITY_TYPE),
            data -> {
                var abilityRef = data.<AbilityTypeReference>get("ability");
                return entity -> {
                    var abilityType = abilityRef.getReferencedAbilityType();
                    if (abilityType != null) {
                        var component = AbilityHolderComponent.get(entity);
                        if (component != null) {
                            var ability = component.getAbility(abilityType);
                            if (ability instanceof arvem.aspectral.abilities.impl.CooldownAbility cooldown) {
                                cooldown.use();
                            }
                        }
                    }
                };
            }
        ));
    }
}
