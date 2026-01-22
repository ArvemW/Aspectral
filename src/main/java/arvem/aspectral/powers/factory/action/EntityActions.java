package arvem.aspectral.powers.factory.action;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.component.PowerHolderComponent;
import arvem.aspectral.powers.PowerTypeReference;
import arvem.aspectral.powers.factory.ActionFactory;
import arvem.aspectral.powers.impl.CooldownPower;
import arvem.aspectral.powers.impl.ResourcePower;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.api.Player;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.registry.PowerTypeRegistry;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.List;
import java.util.function.Consumer;

/**
 * Registration for built-in entity actions.
 * Entity actions perform operations on a single entity.
 */
public class EntityActions {

    public static void register(PowerTypeRegistry registry) {
        // Do nothing action
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("nothing"),
            new SerializableData(),
            data -> entity -> {}
        ));

        // Execute multiple actions
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("and"),
            new SerializableData()
                .add("actions", AspectPowersDataTypes.ENTITY_ACTIONS),
            data -> {
                List<Consumer<LivingEntity>> actions = data.get("actions");
                return entity -> actions.forEach(action -> action.accept(entity));
            }
        ));

        // Conditional action
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("if_else"),
            new SerializableData()
                .add("condition", AspectPowersDataTypes.ENTITY_CONDITION)
                .add("if_action", AspectPowersDataTypes.ENTITY_ACTION, null)
                .add("else_action", AspectPowersDataTypes.ENTITY_ACTION, null),
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
            AspectPowers.identifier("delay"),
            new SerializableData()
                .add("ticks", SerializableDataTypes.INT)
                .add("action", AspectPowersDataTypes.ENTITY_ACTION),
            data -> {
                int ticks = data.get("ticks");
                Consumer<LivingEntity> action = data.get("action");
                return entity -> AspectPowers.SCHEDULER.schedule(() -> action.accept(entity), ticks);
            }
        ));

        // Heal entity
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("heal"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT),
            data -> {
                float amount = data.get("amount");
                return entity -> entity.heal(amount);
            }
        ));

        // Damage entity
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("damage"),
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
            AspectPowers.identifier("set_on_fire"),
            new SerializableData()
                .add("duration", SerializableDataTypes.INT),
            data -> {
                int duration = data.get("duration");
                return entity -> entity.setOnFire(duration);
            }
        ));

        // Extinguish fire
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("extinguish"),
            new SerializableData(),
            data -> LivingEntity::extinguish
        ));

        // Kill entity
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("kill"),
            new SerializableData(),
            data -> LivingEntity::kill
        ));

        // Add power
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("grant_power"),
            new SerializableData()
                .add("power", AspectPowersDataTypes.POWER_TYPE)
                .add("source", SerializableDataTypes.STRING, "action"),
            data -> {
                var powerRef = data.<PowerTypeReference>get("power");
                String source = data.get("source");
                return entity -> {
                    var powerType = powerRef.getReferencedPowerType();
                    if (powerType != null) {
                        var component = PowerHolderComponent.getOrCreate(entity);
                        component.addPower(powerType, source);
                    }
                };
            }
        ));

        // Remove power
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("revoke_power"),
            new SerializableData()
                .add("power", AspectPowersDataTypes.POWER_TYPE)
                .add("source", SerializableDataTypes.STRING, "action"),
            data -> {
                var powerRef = data.<PowerTypeReference>get("power");
                String source = data.get("source");
                return entity -> {
                    var powerType = powerRef.getReferencedPowerType();
                    if (powerType != null) {
                        var component = PowerHolderComponent.get(entity);
                        if (component != null) {
                            component.removePower(powerType, source);
                        }
                    }
                };
            }
        ));

        // Modify resource
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("change_resource"),
            new SerializableData()
                .add("resource", AspectPowersDataTypes.POWER_TYPE)
                .add("change", SerializableDataTypes.INT),
            data -> {
                var resourceRef = data.<PowerTypeReference>get("resource");
                int change = data.get("change");
                return entity -> {
                    var powerType = resourceRef.getReferencedPowerType();
                    if (powerType != null) {
                        var component = PowerHolderComponent.get(entity);
                        if (component != null) {
                            var power = component.getPower(powerType);
                            if (power instanceof ResourcePower resource) {
                                resource.change(change);
                            }
                        }
                    }
                };
            }
        ));

        // Execute command as player (if entity is player)
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("execute_command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING),
            data -> {
                String command = data.get("command");
                return entity -> {
                    if (entity instanceof Player player) {
                        // Execute command through server
                        // AspectPowers.getServer().getCommandManager().executeCommand(player, command);
                    }
                };
            }
        ));

        // Send message to player
        registry.registerEntityAction(new ActionFactory<>(
            AspectPowers.identifier("send_message"),
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
            AspectPowers.identifier("add_velocity"),
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
            AspectPowers.identifier("trigger_cooldown"),
            new SerializableData()
                .add("power", AspectPowersDataTypes.POWER_TYPE),
            data -> {
                var powerRef = data.<PowerTypeReference>get("power");
                return entity -> {
                    var powerType = powerRef.getReferencedPowerType();
                    if (powerType != null) {
                        var component = PowerHolderComponent.get(entity);
                        if (component != null) {
                            var power = component.getPower(powerType);
                            if (power instanceof CooldownPower cooldown) {
                                cooldown.use();
                            }
                        }
                    }
                };
            }
        ));
    }
}


