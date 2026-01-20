package arvem.aspectral.command;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.component.AbilityHolderComponent;
import arvem.aspectral.registry.AbilityRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for managing abilities on players.
 * Usage:
 *   /ability available - List all available ability types
 *   /ability grant <player> <ability> [source] - Grant an ability to a player
 *   /ability revoke <player> <ability> [source] - Revoke an ability from a player
 *   /ability list <player> - List all abilities on a player
 *   /ability clear <player> [source] - Clear abilities from a player
 */
public class AbilityCommand extends AbstractCommandCollection {

    // Color constants for consistent formatting
    private static final String COLOR_GOLD = "#FFAA00";
    private static final String COLOR_GREEN = "#55FF55";
    private static final String COLOR_RED = "#FF5555";
    private static final String COLOR_YELLOW = "#FFFF55";
    private static final String COLOR_WHITE = "#FFFFFF";
    private static final String COLOR_GRAY = "#AAAAAA";

    public AbilityCommand() {
        super("ability", "Manage player abilities");
        addSubCommand(new ListAvailableCommand("available", "List all available ability types"));
        addSubCommand(new GrantCommand("grant", "Grant an ability to a player"));
        addSubCommand(new RevokeCommand("revoke", "Revoke an ability from a player"));
        addSubCommand(new ListCommand("list", "List all abilities on a player"));
        addSubCommand(new ClearCommand("clear", "Clear abilities from a player"));
        addSubCommand(new InfoCommand("info", "Show info about a specific ability on a player"));
        addSubCommand(new SourcesCommand("sources", "Show sources that granted abilities to a player"));
        this.setPermissionGroup(GameMode.Creative);
    }

    /**
     * Helper to create a HytalePlayerAdapter from command context.
     */
    private static HytalePlayerAdapter createPlayerAdapter(
            PlayerRef targetPlayerRef,
            Store<EntityStore> store) {
        Ref<EntityStore> targetRef = targetPlayerRef.getReference();
        Player targetPlayer = store.getComponent(targetRef, Player.getComponentType());
        return new HytalePlayerAdapter(targetPlayer, targetPlayerRef, targetRef, store);
    }

    // ========================================
    // Subcommands
    // ========================================

    private static class ListAvailableCommand extends AbstractPlayerCommand {
        public ListAvailableCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            AbilityRegistry registry = AspectAbilities.getInstance().getAbilityRegistry();

            // List ability factories (these are the ability types that can be used)
            var factoryIds = registry.getAllAbilityFactoryIds();

            if (factoryIds.isEmpty()) {
                playerRef.sendMessage(Message.raw("No ability types registered.").color(COLOR_RED));
                return;
            }

            playerRef.sendMessage(Message.raw("=== Available Abilities (" + factoryIds.size() + ") ===").color(COLOR_GOLD));
            for (String id : factoryIds) {
                playerRef.sendMessage(Message.raw("  - ").color(COLOR_GRAY).insert(Message.raw(id).color(COLOR_WHITE)));
            }
        }
    }

    private static class GrantCommand extends AbstractPlayerCommand {
        public GrantCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        RequiredArg<String> abilityArg = this.withRequiredArg("ability", "Ability ID", ArgTypes.STRING);
        OptionalArg<String> sourceArg = this.withOptionalArg("source", "Source of ability", ArgTypes.STRING);

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            String abilityId = abilityArg.get(commandContext);
            String source = sourceArg.get(commandContext);
            if (source == null) source = "command";

            AbilityRegistry registry = AspectAbilities.getInstance().getAbilityRegistry();

            // First check if there's a registered AbilityType
            AbilityType abilityType = registry.getAbilityType(abilityId);

            // If not, try to create one from a factory
            if (abilityType == null) {
                var factory = registry.getAbilityFactory(abilityId);
                if (factory == null) {
                    playerRef.sendMessage(Message.raw("Ability not found: ").color(COLOR_RED)
                            .insert(Message.raw(abilityId).color(COLOR_WHITE)));
                    return;
                }

                // Create an AbilityType from the factory with default values
                var factoryInstance = factory.createDefault();
                abilityType = new AbilityType(abilityId, factoryInstance);
            }

            // Create adapter and grant ability
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            AbilityHolderComponent component = AbilityHolderComponent.getOrCreate(adapter);
            boolean added = component.addAbility(abilityType, source);

            if (added) {
                playerRef.sendMessage(Message.raw("Granted ").color(COLOR_GREEN)
                        .insert(Message.raw(abilityId).color(COLOR_WHITE))
                        .insert(Message.raw(" to ").color(COLOR_GREEN))
                        .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)));
            } else {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" already has ").color(COLOR_YELLOW))
                        .insert(Message.raw(abilityId).color(COLOR_WHITE)));
            }
        }
    }

    private static class RevokeCommand extends AbstractPlayerCommand {
        public RevokeCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        RequiredArg<String> abilityArg = this.withRequiredArg("ability", "Ability ID", ArgTypes.STRING);
        OptionalArg<String> sourceArg = this.withOptionalArg("source", "Source of ability", ArgTypes.STRING);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            String abilityId = abilityArg.get(commandContext);
            String source = sourceArg.get(commandContext);
            if (source == null) source = "command";

            // Create adapter and check for ability
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            AbilityHolderComponent component = AbilityHolderComponent.get(adapter);

            if (component == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_RED)
                        .insert(Message.raw(" has no abilities.").color(COLOR_RED)));
                return;
            }

            // Find the ability by ID in the player's abilities
            Ability abilityToRemove = null;
            for (Ability ability : component.getAbilities()) {
                if (ability.getType().getIdentifier().equals(abilityId)) {
                    abilityToRemove = ability;
                    break;
                }
            }

            if (abilityToRemove == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_RED)
                        .insert(Message.raw(" doesn't have ").color(COLOR_RED))
                        .insert(Message.raw(abilityId).color(COLOR_WHITE)));
                return;
            }

            component.removeAbility(abilityToRemove.getType(), source);
            playerRef.sendMessage(Message.raw("Revoked ").color(COLOR_GREEN)
                    .insert(Message.raw(abilityId).color(COLOR_WHITE))
                    .insert(Message.raw(" from ").color(COLOR_GREEN))
                    .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)));
        }
    }

    private static class ListCommand extends AbstractPlayerCommand {
        public ListCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);

            // Create adapter and list abilities
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            AbilityHolderComponent component = AbilityHolderComponent.get(adapter);

            if (component == null || component.getAbilities().isEmpty()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" has no abilities.").color(COLOR_YELLOW)));
                return;
            }

            var abilities = component.getAbilities();
            playerRef.sendMessage(Message.raw("=== Abilities for " + targetPlayerRef.getUsername() + " (" + abilities.size() + ") ===").color(COLOR_GOLD));
            for (Ability ability : abilities) {
                String statusColor = ability.isActive() ? COLOR_GREEN : COLOR_RED;
                String statusSymbol = ability.isActive() ? "●" : "○";
                playerRef.sendMessage(Message.raw("  " + statusSymbol + " ").color(statusColor)
                        .insert(Message.raw(ability.getType().getIdentifier()).color(COLOR_WHITE)));
            }
        }
    }

    private static class ClearCommand extends AbstractPlayerCommand {
        public ClearCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        OptionalArg<String> sourceArg = this.withOptionalArg("source", "Source to clear", ArgTypes.STRING);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            String source = sourceArg.get(commandContext);

            // Create adapter and clear abilities
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            AbilityHolderComponent component = AbilityHolderComponent.get(adapter);

            if (component == null || component.getAbilities().isEmpty()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" has no abilities.").color(COLOR_YELLOW)));
                return;
            }

            if (source != null) {
                // Clear abilities from specific source
                int removed = component.removeAllAbilitiesFromSource(source);
                playerRef.sendMessage(Message.raw("Cleared ").color(COLOR_GREEN)
                        .insert(Message.raw(String.valueOf(removed)).color(COLOR_WHITE))
                        .insert(Message.raw(" abilities from ").color(COLOR_GREEN))
                        .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE))
                        .insert(Message.raw(" with source ").color(COLOR_GREEN))
                        .insert(Message.raw(source).color(COLOR_WHITE)));
            } else {
                // Clear all abilities by removing from all sources
                int count = component.getAbilities().size();

                // Get all abilities and remove them
                for (Ability ability : new ArrayList<>(component.getAbilities())) {
                    AbilityType<?> type = ability.getType();
                    // Remove from all sources
                    for (String src : new ArrayList<>(component.getSources(type))) {
                        component.removeAbility(type, src);
                    }
                }

                playerRef.sendMessage(Message.raw("Cleared all ").color(COLOR_GREEN)
                        .insert(Message.raw(String.valueOf(count)).color(COLOR_WHITE))
                        .insert(Message.raw(" abilities from ").color(COLOR_GREEN))
                        .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)));
            }
        }
    }

    private static class InfoCommand extends AbstractPlayerCommand {
        public InfoCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        RequiredArg<String> abilityArg = this.withRequiredArg("ability", "Ability ID", ArgTypes.STRING);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            String abilityId = abilityArg.get(commandContext);

            // Create adapter and find ability
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            AbilityHolderComponent component = AbilityHolderComponent.get(adapter);

            if (component == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_RED)
                        .insert(Message.raw(" has no abilities.").color(COLOR_RED)));
                return;
            }

            // Find the ability
            Ability ability = null;
            for (Ability a : component.getAbilities()) {
                if (a.getType().getIdentifier().equals(abilityId)) {
                    ability = a;
                    break;
                }
            }

            if (ability == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_RED)
                        .insert(Message.raw(" doesn't have ").color(COLOR_RED))
                        .insert(Message.raw(abilityId).color(COLOR_WHITE)));
                return;
            }

            // Display ability info
            playerRef.sendMessage(Message.raw("=== Ability Info: " + abilityId + " ===").color(COLOR_GOLD));

            // Status
            String statusText = ability.isActive() ? "Active" : "Inactive";
            String statusColor = ability.isActive() ? COLOR_GREEN : COLOR_RED;
            playerRef.sendMessage(Message.raw("  Status: ").color(COLOR_GRAY)
                    .insert(Message.raw(statusText).color(statusColor)));

            // Sources
            List<String> sources = component.getSources(ability.getType());
            playerRef.sendMessage(Message.raw("  Sources: ").color(COLOR_GRAY)
                    .insert(Message.raw(String.join(", ", sources)).color(COLOR_WHITE)));

            // Ticking
            String tickStatus = ability.shouldTick() ? "Yes" : "No";
            playerRef.sendMessage(Message.raw("  Ticking: ").color(COLOR_GRAY)
                    .insert(Message.raw(tickStatus).color(COLOR_WHITE)));

            // Type info
            AbilityType<?> type = ability.getType();
            playerRef.sendMessage(Message.raw("  Type ID: ").color(COLOR_GRAY)
                    .insert(Message.raw(type.getIdentifier()).color(COLOR_WHITE)));
        }
    }

    private static class SourcesCommand extends AbstractPlayerCommand {
        public SourcesCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);

            // Create adapter and list sources
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            AbilityHolderComponent component = AbilityHolderComponent.get(adapter);

            if (component == null || component.getAbilities().isEmpty()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" has no abilities.").color(COLOR_YELLOW)));
                return;
            }

            // Collect all unique sources and their abilities
            java.util.Map<String, List<String>> sourceToAbilities = new java.util.HashMap<>();
            for (Ability ability : component.getAbilities()) {
                List<String> sources = component.getSources(ability.getType());
                for (String source : sources) {
                    sourceToAbilities.computeIfAbsent(source, ignored -> new ArrayList<>())
                            .add(ability.getType().getIdentifier());
                }
            }

            playerRef.sendMessage(Message.raw("=== Ability Sources for " + targetPlayerRef.getUsername() + " ===").color(COLOR_GOLD));

            for (java.util.Map.Entry<String, List<String>> entry : sourceToAbilities.entrySet()) {
                playerRef.sendMessage(Message.raw("  " + entry.getKey() + ": ").color(COLOR_YELLOW)
                        .insert(Message.raw(entry.getValue().size() + " abilities").color(COLOR_WHITE)));
                for (String abilityId : entry.getValue()) {
                    playerRef.sendMessage(Message.raw("    - ").color(COLOR_GRAY)
                            .insert(Message.raw(abilityId).color(COLOR_WHITE)));
                }
            }
        }
    }
}
