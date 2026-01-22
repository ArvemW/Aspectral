package arvem.aspectral.command;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.component.PowerHolderComponent;
import arvem.aspectral.powers.Power;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.aspect.Aspect;
import arvem.aspectral.component.PlayerAspectComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Command for managing aspects on players.
 * Usage:
 *   /aspect grant <player> <aspectId> - Grant an aspect to a player
 *   /aspect clear <player> - Clear a player's aspect
 *   /aspect list - List all available aspects
 *   /aspect info <player> - Show a player's current aspect
 */
public class AspectCommand extends AbstractCommandCollection {

    // Color constants for consistent formatting
    private static final String COLOR_GOLD = "#FFAA00";
    private static final String COLOR_GREEN = "#55FF55";
    private static final String COLOR_RED = "#FF5555";
    private static final String COLOR_YELLOW = "#FFFF55";
    private static final String COLOR_WHITE = "#FFFFFF";
    private static final String COLOR_GRAY = "#AAAAAA";

    public AspectCommand() {
        super("aspect", "Manage player aspects");
        addSubCommand(new GrantCommand("grant", "Grant an aspect to a player"));
        addSubCommand(new ClearCommand("clear", "Clear a player's aspect"));
        addSubCommand(new ListCommand("list", "List all available aspects"));
        addSubCommand(new InfoCommand("info", "Show a player's current aspect"));
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

    private static class GrantCommand extends AbstractPlayerCommand {
        public GrantCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        RequiredArg<String> aspectIdArg = this.withRequiredArg("aspect", "Aspect ID", ArgTypes.STRING);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);
            String aspectId = aspectIdArg.get(commandContext);

            // Validate aspect exists
            Aspect aspect = AspectPowers.getInstance().getAspectRegistry().get(aspectId);
            if (aspect == null) {
                playerRef.sendMessage(Message.raw("Unknown aspect: ").color(COLOR_RED)
                        .insert(Message.raw(aspectId).color(COLOR_WHITE)));
                return;
            }

            // Create player adapter
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);

            // Get or create component
            PlayerAspectComponent component = PlayerAspectComponent.getOrCreate(adapter);

            // Set the aspect (this clears old powers and creates new ones)
            component.setAspect(aspectId);

            playerRef.sendMessage(Message.raw("Granted aspect ").color(COLOR_GREEN)
                    .insert(Message.raw(aspectId).color(COLOR_WHITE))
                    .insert(Message.raw(" to ").color(COLOR_GREEN))
                    .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE))
                    .insert(Message.raw(" with ").color(COLOR_GREEN))
                    .insert(Message.raw(String.valueOf(aspect.getPowerCount())).color(COLOR_YELLOW))
                    .insert(Message.raw(" powers").color(COLOR_GREEN)));
        }
    }

    private static class ClearCommand extends AbstractPlayerCommand {
        public ClearCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);

            // Create player adapter
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);

            // Get component
            PlayerAspectComponent component = PlayerAspectComponent.get(adapter);
            if (component == null || !component.hasAspect()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_YELLOW)
                        .insert(Message.raw(" has no aspect.").color(COLOR_YELLOW)));
                return;
            }

            String oldAspect = component.getAspectId();
            component.clearAspect();

            playerRef.sendMessage(Message.raw("Cleared aspect ").color(COLOR_GREEN)
                    .insert(Message.raw(oldAspect).color(COLOR_WHITE))
                    .insert(Message.raw(" from ").color(COLOR_GREEN))
                    .insert(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)));
        }
    }

    private static class ListCommand extends AbstractPlayerCommand {
        public ListCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            var aspectRegistry = AspectPowers.getInstance().getAspectRegistry();
            var aspects = aspectRegistry.getAll();

            if (aspects.isEmpty()) {
                playerRef.sendMessage(Message.raw("No aspects registered.").color(COLOR_RED));
                return;
            }

            playerRef.sendMessage(Message.raw("=== Available Aspects (" + aspects.size() + ") ===").color(COLOR_GOLD));

            for (Aspect aspect : aspects) {
                playerRef.sendMessage(Message.raw("  - ").color(COLOR_GRAY)
                        .insert(Message.raw(aspect.getIdentifier()).color(COLOR_WHITE))
                        .insert(Message.raw(" (").color(COLOR_GRAY))
                        .insert(Message.raw(String.valueOf(aspect.getPowerCount())).color(COLOR_YELLOW))
                        .insert(Message.raw(" powers)").color(COLOR_GRAY)));
            }
        }
    }

    private static class InfoCommand extends AbstractPlayerCommand {
        public InfoCommand(@NonNull String name, @NonNull String description) {
            super(name, description);
        }

        RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);

        @Override
        protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store,
                              @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
            PlayerRef targetPlayerRef = targetPlayerArg.get(commandContext);

            // Create player adapter
            HytalePlayerAdapter adapter = createPlayerAdapter(targetPlayerRef, store);

            // Get component
            PlayerAspectComponent component = PlayerAspectComponent.get(adapter);
            if (component == null || !component.hasAspect()) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)
                        .insert(Message.raw(" has no aspect.").color(COLOR_YELLOW)));
                return;
            }

            String aspectId = component.getAspectId();
            Aspect aspect = AspectPowers.getInstance().getAspectRegistry().get(aspectId);

            if (aspect == null) {
                playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)
                        .insert(Message.raw(" has unknown aspect: ").color(COLOR_RED))
                        .insert(Message.raw(aspectId).color(COLOR_WHITE)));
                return;
            }

            playerRef.sendMessage(Message.raw(targetPlayerRef.getUsername()).color(COLOR_WHITE)
                    .insert(Message.raw(" has aspect: ").color(COLOR_GRAY))
                    .insert(Message.raw(aspectId).color(COLOR_YELLOW))
                    .insert(Message.raw(" (").color(COLOR_GRAY))
                    .insert(Message.raw(String.valueOf(aspect.getPowerCount())).color(COLOR_WHITE))
                    .insert(Message.raw(" powers)").color(COLOR_GRAY)));

            // List powers
            PowerHolderComponent holder = PowerHolderComponent.get(adapter);
            if (holder != null) {
                List<Power> powers = holder.getAbilities();
                for (int i = 0; i < powers.size(); i++) {
                    Power power = powers.get(i);
                    playerRef.sendMessage(Message.raw("    ").color(COLOR_GRAY)
                            .insert(Message.raw(String.valueOf(i)).color(COLOR_YELLOW))
                            .insert(Message.raw(": ").color(COLOR_GRAY))
                            .insert(Message.raw(power.getType().getIdentifier()).color(COLOR_WHITE)));
                }
            }
        }
    }
}



