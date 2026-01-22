package arvem.aspectral.command;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.component.PowerHolderComponent;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.api.HytalePlayerAdapter;
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
 * Command for managing powers on players.
 * Usage:
 *   /power available - List all available power types
 *   /power grant <player> <power> [source] - Grant an power to a player
 *   /power revoke <player> <power> [source] - Revoke an power from a player
 *   /power list <player> - List all powers on a player
 *   /power clear <player> [source] - Clear powers from a player
 */
public class PowerCommand extends AbstractCommandCollection {

    // Color constants for consistent formatting
    private static final String COLOR_GOLD = "#FFAA00";
    private static final String COLOR_GREEN = "#55FF55";
    private static final String COLOR_RED = "#FF5555";
    private static final String COLOR_YELLOW = "#FFFF55";
    private static final String COLOR_WHITE = "#FFFFFF";
    private static final String COLOR_GRAY = "#AAAAAA";

    public PowerCommand() {
        super("power", "Manage player powers");
        addSubCommand(new ListAvailableCommand("available", "List all available power types"));
        addSubCommand(new GrantCommand("grant", "Grant an power to a player"));
        addSubCommand(new RevokeCommand("revoke", "Revoke an power from a player"));
        addSubCommand(new ListCommand("list", "List all powers on a player"));
        addSubCommand(new ClearCommand("clear", "Clear powers from a player"));
        addSubCommand(new InfoCommand("info", "Show info about a specific power on a player"));
        addSubCommand(new SourcesCommand("sources", "Show sources that granted powers to a player"));
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
            var aspectRegistry = AspectPowers.getInstance().getAspectRegistry();

            // Debug logging
            AspectPowers.getLogger().atInfo().log("Command: AspectRegistry instance hash: %d", System.identityHashCode(aspectRegistry));
            AspectPowers.getLogger().atInfo().log("Command: AspectRegistry size: %d", aspectRegistry.size());

            // List all powers defined in aspects
            var aspects = aspectRegistry.getAll();

            if (aspects.isEmpty()) {
                AspectPowers.getLogger().atWarning().log("Command: No aspects found in registry!");
                playerRef.sendMessage(Message.raw("No aspects registered.").color(COLOR_RED));
                return;
            }

            // Count total powers across all aspects
            int totalAbilities = 0;
            for (var aspect : aspects) {
                totalAbilities += aspect.getPowerCount();
            }

            if (totalAbilities == 0) {
                playerRef.sendMessage(Message.raw("No powers defined in aspects.").color(COLOR_RED));
                return;
            }

            playerRef.sendMessage(Message.raw("=== Available powers (" + totalAbilities + ") ===").color(COLOR_GOLD));

            // List powers per aspect
            for (var aspect : aspects) {
                int powerCount = aspect.getPowerCount();
                if (powerCount == 0) {
                    continue; // Skip aspects with no powers
                }

                // Show aspect header
                playerRef.sendMessage(Message.raw("  " + aspect.getIdentifier() + " (" + powerCount + "):").color(COLOR_YELLOW));

                // List each power definition
                for (int i = 0; i < powerCount; i++) {
                    var definition = aspect.getPowerDefinition(i);
                    if (definition == null) {
                        continue;
                    }

                    String powerTypeId = definition.powerType.getIdentifier();
                    String fullPowerId = aspect.getIdentifier() + ":" + i;

                    playerRef.sendMessage(Message.raw("    - ").color(COLOR_GRAY)
                            .insert(Message.raw(fullPowerId).color(COLOR_WHITE))
                            .insert(Message.raw(" (").color(COLOR_GRAY))
                            .insert(Message.raw(powerTypeId).color(COLOR_GREEN))
                            .insert(Message.raw(")").color(COLOR_GRAY)));
                }
            }
        }
    }

    private static class GrantCommand extends AbstractPlayerCommand {
        public GrantCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        RequiredArg<String> powerArg = this.withRequiredArg("power", "Power ID (aspect:index)", ArgTypes.STRING);

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            String powerId = powerArg.get(commandContext);

            // Parse aspect:index format
            String[] parts = powerId.split(":");
            if (parts.length != 3) { // Expected: namespace:name:index
                playerRef.sendMessage(Message.raw("Invalid power format. Use: ").color(COLOR_RED)
                        .insert(Message.raw("aspectId:index").color(COLOR_WHITE))
                        .insert(Message.raw(" (e.g., aspectral:skywalker:0)").color(COLOR_GRAY)));
                return;
            }

            String aspectId = parts[0] + ":" + parts[1]; // Reconstruct aspect ID
            int index;
            try {
                index = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                playerRef.sendMessage(Message.raw("Invalid index: ").color(COLOR_RED)
                        .insert(Message.raw(parts[2]).color(COLOR_WHITE)));
                return;
            }

            // Look up the aspect
            var aspectRegistry = AspectPowers.getInstance().getAspectRegistry();
            var aspect = aspectRegistry.get(aspectId);
            if (aspect == null) {
                playerRef.sendMessage(Message.raw("Unknown aspect: ").color(COLOR_RED)
                        .insert(Message.raw(aspectId).color(COLOR_WHITE)));
                return;
            }

            // Get the power definition
            var definition = aspect.getPowerDefinition(index);
            if (definition == null) {
                playerRef.sendMessage(Message.raw("Invalid power index: ").color(COLOR_RED)
                        .insert(Message.raw(String.valueOf(index)).color(COLOR_WHITE))
                        .insert(Message.raw(" for aspect ").color(COLOR_RED))
                        .insert(Message.raw(aspectId).color(COLOR_WHITE)));
                return;
            }

            // Create adapter
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);

            // Create power instance
            Power power = definition.powerType.create(adapter, definition.data);

            // Add to holder
            PowerHolderComponent component = PowerHolderComponent.getOrCreate(adapter);
            component.addPower(power, "command");

            playerRef.sendMessage(Message.raw("Granted ").color(COLOR_GREEN)
                    .insert(Message.raw(powerId).color(COLOR_WHITE))
                    .insert(Message.raw(" (").color(COLOR_GRAY))
                    .insert(Message.raw(definition.powerType.getIdentifier()).color(COLOR_YELLOW))
                    .insert(Message.raw(") to ").color(COLOR_GREEN))
                    .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)));
        }
    }

    private static class RevokeCommand extends AbstractPlayerCommand {
        public RevokeCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        RequiredArg<Integer> indexArg = this.withRequiredArg("index", "Power index", ArgTypes.INTEGER);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            int index = indexArg.get(commandContext);

            // Create adapter and check for power
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_RED)
                        .insert(Message.raw(" has no powers.").color(COLOR_RED)));
                return;
            }

            // Get powers list
            List<Power> powers = component.getAbilities();
            if (index < 0 || index >= powers.size()) {
                playerRef.sendMessage(Message.raw("Invalid index: ").color(COLOR_RED)
                        .insert(Message.raw(String.valueOf(index)).color(COLOR_WHITE))
                        .insert(Message.raw(". Player has ").color(COLOR_RED))
                        .insert(Message.raw(String.valueOf(powers.size())).color(COLOR_WHITE))
                        .insert(Message.raw(" powers.").color(COLOR_RED)));
                return;
            }

            Power powerToRemove = powers.get(index);
            String powerTypeId = powerToRemove.getType().getIdentifier();

            component.removePower(powerToRemove, "command");

            playerRef.sendMessage(Message.raw("Removed power ").color(COLOR_GREEN)
                    .insert(Message.raw(String.valueOf(index)).color(COLOR_YELLOW))
                    .insert(Message.raw(" (").color(COLOR_GRAY))
                    .insert(Message.raw(powerTypeId).color(COLOR_WHITE))
                    .insert(Message.raw(") from ").color(COLOR_GREEN))
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

            // Create adapter and list powers
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component == null || component.getAbilities().isEmpty()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" has no powers.").color(COLOR_YELLOW)));
                return;
            }

            var powers = component.getAbilities();
            playerRef.sendMessage(Message.raw("=== powers for " + targetPlayerRef.getUsername() + " (" + powers.size() + ") ===").color(COLOR_GOLD));

            for (int i = 0; i < powers.size(); i++) {
                Power power = powers.get(i);
                String statusColor = power.isActive() ? COLOR_GREEN : COLOR_RED;
                String statusSymbol = power.isActive() ? "●" : "○";

                playerRef.sendMessage(Message.raw("  [").color(COLOR_GRAY)
                        .insert(Message.raw(String.valueOf(i)).color(COLOR_YELLOW))
                        .insert(Message.raw("] " + statusSymbol + " ").color(statusColor))
                        .insert(Message.raw(power.getType().getIdentifier()).color(COLOR_WHITE)));
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

            // Create adapter and clear powers
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component == null || component.getAbilities().isEmpty()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" has no powers.").color(COLOR_YELLOW)));
                return;
            }

            if (source != null) {
                // Clear powers from specific source
                int removed = component.removeAllAbilitiesFromSource(source);
                playerRef.sendMessage(Message.raw("Cleared ").color(COLOR_GREEN)
                        .insert(Message.raw(String.valueOf(removed)).color(COLOR_WHITE))
                        .insert(Message.raw(" powers from ").color(COLOR_GREEN))
                        .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE))
                        .insert(Message.raw(" with source ").color(COLOR_GREEN))
                        .insert(Message.raw(source).color(COLOR_WHITE)));
            } else {
                // Clear all powers by removing from all sources
                int count = component.getAbilities().size();

                // Get all powers and remove them
                for (Power power : new ArrayList<>(component.getAbilities())) {
                    PowerType<?> type = power.getType();
                    // Remove from all sources
                    for (String src : new ArrayList<>(component.getSources(type))) {
                        component.removePower(type, src);
                    }
                }

                playerRef.sendMessage(Message.raw("Cleared all ").color(COLOR_GREEN)
                        .insert(Message.raw(String.valueOf(count)).color(COLOR_WHITE))
                        .insert(Message.raw(" powers from ").color(COLOR_GREEN))
                        .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)));
            }
        }
    }

    private static class InfoCommand extends AbstractPlayerCommand {
        public InfoCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        RequiredArg<String> powerArg = this.withRequiredArg("power", "Power ID", ArgTypes.STRING);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            String powerId = powerArg.get(commandContext);

            // Create adapter and find power
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_RED)
                        .insert(Message.raw(" has no powers.").color(COLOR_RED)));
                return;
            }

            // Find the power
            Power power = null;
            for (Power a : component.getAbilities()) {
                if (a.getType().getIdentifier().equals(powerId)) {
                    power = a;
                    break;
                }
            }

            if (power == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_RED)
                        .insert(Message.raw(" doesn't have ").color(COLOR_RED))
                        .insert(Message.raw(powerId).color(COLOR_WHITE)));
                return;
            }

            // Display power info
            playerRef.sendMessage(Message.raw("=== Power Info: " + powerId + " ===").color(COLOR_GOLD));

            // Status
            String statusText = power.isActive() ? "Active" : "Inactive";
            String statusColor = power.isActive() ? COLOR_GREEN : COLOR_RED;
            playerRef.sendMessage(Message.raw("  Status: ").color(COLOR_GRAY)
                    .insert(Message.raw(statusText).color(statusColor)));

            // Sources
            List<String> sources = component.getSources(power.getType());
            playerRef.sendMessage(Message.raw("  Sources: ").color(COLOR_GRAY)
                    .insert(Message.raw(String.join(", ", sources)).color(COLOR_WHITE)));

            // Ticking
            String tickStatus = power.shouldTick() ? "Yes" : "No";
            playerRef.sendMessage(Message.raw("  Ticking: ").color(COLOR_GRAY)
                    .insert(Message.raw(tickStatus).color(COLOR_WHITE)));

            // Type info
            PowerType<?> type = power.getType();
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
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component == null || component.getAbilities().isEmpty()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" has no powers.").color(COLOR_YELLOW)));
                return;
            }

            // Collect all unique sources and their powers
            java.util.Map<String, List<String>> sourceToAbilities = new java.util.HashMap<>();
            for (Power power : component.getAbilities()) {
                List<String> sources = component.getSources(power.getType());
                for (String source : sources) {
                    sourceToAbilities.computeIfAbsent(source, ignored -> new ArrayList<>())
                            .add(power.getType().getIdentifier());
                }
            }

            playerRef.sendMessage(Message.raw("=== Power Sources for " + targetPlayerRef.getUsername() + " ===").color(COLOR_GOLD));

            for (java.util.Map.Entry<String, List<String>> entry : sourceToAbilities.entrySet()) {
                playerRef.sendMessage(Message.raw("  " + entry.getKey() + ": ").color(COLOR_YELLOW)
                        .insert(Message.raw(entry.getValue().size() + " powers").color(COLOR_WHITE)));
                for (String powerId : entry.getValue()) {
                    playerRef.sendMessage(Message.raw("    - ").color(COLOR_GRAY)
                            .insert(Message.raw(powerId).color(COLOR_WHITE)));
                }
            }
        }
    }
}


