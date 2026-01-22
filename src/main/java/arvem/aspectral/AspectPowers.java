package arvem.aspectral;

import arvem.aspectral.component.PowerHolderComponent;
import arvem.aspectral.powers.factory.PowerFactories;
import arvem.aspectral.powers.factory.action.EntityActions;
import arvem.aspectral.powers.factory.condition.EntityConditions;
import arvem.aspectral.aspect.AspectLoader;
import arvem.aspectral.aspect.AspectRegistry;
import arvem.aspectral.command.PowerCommand;
import arvem.aspectral.command.AspectCommand;
import arvem.aspectral.component.PlayerAspectComponent;
import arvem.aspectral.layer.LayerLoader;
import arvem.aspectral.layer.LayerRegistry;
import arvem.aspectral.persistence.PowerPersistenceManager;
import arvem.aspectral.power.PowerLoader;
import arvem.aspectral.registry.PowerTypeRegistry;
import arvem.aspectral.util.Scheduler;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.nio.file.Path;

/**
 * AspectPowers - A power/power system for Hytale.
 * Inspired by Apoli/Origins but designed specifically for Hytale's architecture.
 * <p>
 * Unlike the Minecraft version, this is server-side only but can still control
 * UI elements and client-visible effects through Hytale's server-authoritative model.
 * <p>
 * Data structure (Origins-style):
 * - Powers are in data/aspectral/powers/*.json (reusable powers)
 * - Aspects are in data/aspectral/aspects/*.json (collections of power references)
 * - Layers are in data/aspectral/layers/*.json (define which aspects are available)
 */
public final class AspectPowers {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static AspectPowers instance;
    private static JavaPlugin plugin;

    public static final Scheduler SCHEDULER = new Scheduler();

    // Power holder component for entity power management
    private final PowerHolderComponent.Manager componentManager;

    // Player aspect component for storing player's chosen aspect
    private final PlayerAspectComponent.Manager playerAspectManager;

    // Registry for power types
    private final PowerTypeRegistry powerTypeRegistry;

    // Registry for power instances
    private final arvem.aspectral.power.PowerRegistry powerRegistry;

    // Registry for aspects (origins)
    private final AspectRegistry aspectRegistry;

    // Registry for layers (groups of aspects)
    private final LayerRegistry layerRegistry;

    // Loaders for data files
    private final PowerLoader powerLoader;
    private final AspectLoader aspectLoader;
    private final LayerLoader layerLoader;

    // Persistence manager for saving/loading powers
    private final PowerPersistenceManager persistenceManager;

    private AspectPowers(JavaPlugin pluginInstance) {
        this.componentManager = new PowerHolderComponent.Manager();
        this.playerAspectManager = new PlayerAspectComponent.Manager();
        this.powerTypeRegistry = new PowerTypeRegistry();
        this.powerRegistry = arvem.aspectral.power.PowerRegistry.getInstance();
        this.aspectRegistry = new AspectRegistry();
        this.layerRegistry = new LayerRegistry();
        this.powerLoader = new PowerLoader(powerRegistry, powerTypeRegistry);
        this.aspectLoader = new AspectLoader(aspectRegistry, powerTypeRegistry);
        this.layerLoader = new LayerLoader(layerRegistry);
        this.persistenceManager = new PowerPersistenceManager(pluginInstance);
    }

    /**
     * Initialize the AspectPowers system.
     * Call this during plugin setup.
     */
    public static void initialize(JavaPlugin pluginInstance) {
        if (instance != null) {
            LOGGER.atWarning().log("AspectPowers already initialized!");
            return;
        }

        plugin = pluginInstance;
        instance = new AspectPowers(pluginInstance);

        LOGGER.atInfo().log("Initializing AspectPowers v%s", Aspectral.getInstance().getManifest().getVersion().toString());

        // Register core systems
        instance.registerCoreFactories();
        instance.registerConditions();
        instance.registerActions();

        // Load data from JSON files (order matters: powers -> aspects -> layers)
        instance.loadPowers(pluginInstance);
        instance.loadAspects(pluginInstance);
        instance.loadLayers(pluginInstance);

        // Register event listeners
        instance.registerEventListeners(pluginInstance);

        // Register persistence events
        instance.persistenceManager.registerEvents(pluginInstance);

        LOGGER.atInfo().log("AspectPowers initialized successfully!");
    }

    /**
     * Called when the server is shutting down.
     * Saves all player powers.
     */
    public static void shutdown() {
        if (instance != null) {
            instance.persistenceManager.saveAllPlayers();
            LOGGER.atInfo().log("AspectPowers shutdown complete.");
        }
    }

    /**
     * Register command handlers for the power system.
     */
    public static void registerCommands(Aspectral plugin) {
        plugin.getCommandRegistry().registerCommand(new PowerCommand());
        plugin.getCommandRegistry().registerCommand(new AspectCommand());
        LOGGER.atInfo().log("AspectPowers commands registered.");
    }

    private void registerCoreFactories() {
        PowerFactories.register(powerTypeRegistry);
        LOGGER.atFine().log("Registered power factories");
    }

    private void registerConditions() {
        EntityConditions.register(powerTypeRegistry);
        LOGGER.atFine().log("Registered entity conditions");
    }

    private void registerActions() {
        EntityActions.register(powerTypeRegistry);
        LOGGER.atFine().log("Registered entity actions");
    }

    @SuppressWarnings("unused")
    private void loadPowers(JavaPlugin pluginInstance) {
        // Load powers (power definitions) from data folder
        // Server runs from the 'run' directory, so we use a relative path from there
        Path powersDir = Path.of("plugins", "aspectral", "powers");
        LOGGER.atInfo().log("PowerLoader: starting load from: %s", powersDir.toAbsolutePath());
        LOGGER.atInfo().log("Directory exists: %s", java.nio.file.Files.exists(powersDir));
        powerLoader.loadFromDirectory(powersDir);
        LOGGER.atInfo().log("PowerLoader: finished load. Loaded %d power(s)", powerRegistry.size());
    }

    @SuppressWarnings("unused")
    private void loadAspects(JavaPlugin pluginInstance) {
        // Load aspects from the plugin's data folder
        // Server runs from the 'run' directory, so we use a relative path from there
        Path aspectsDir = Path.of("plugins", "aspectral", "aspects");
        LOGGER.atInfo().log("AspectLoader: starting load from: %s", aspectsDir.toAbsolutePath());
        LOGGER.atInfo().log("Directory exists: %s", java.nio.file.Files.exists(aspectsDir));
        aspectLoader.loadFromDirectory(aspectsDir);
        LOGGER.atInfo().log("AspectLoader: finished load. Loaded %d aspect(s)", aspectRegistry.size());
        LOGGER.atInfo().log("AspectRegistry instance after load, hash: %d", System.identityHashCode(aspectRegistry));
    }

    @SuppressWarnings("unused")
    private void loadLayers(JavaPlugin pluginInstance) {
        // Load layers from data folder
        Path layersDir = Path.of("plugins", "aspectral", "layers");
        LOGGER.atInfo().log("LayerLoader: starting load from: %s", layersDir.toAbsolutePath());
        LOGGER.atInfo().log("Directory exists: %s", java.nio.file.Files.exists(layersDir));
        layerLoader.loadFromDirectory(layersDir);
        LOGGER.atInfo().log("LayerLoader: finished load. Loaded %d layer(s)", layerRegistry.size());
    }

    @SuppressWarnings("unused")
    private void registerEventListeners(JavaPlugin pluginInstance) {
        // ============================================================================
        // IMPORTANT: Player Input Detection in Hytale
        // ============================================================================
        // Hytale servers do NOT receive raw keyboard input or custom keybinds.
        // The client interprets keypresses and sends PACKETS describing actions.
        //
        // According to the official Player Input Guide:
        // https://hytalemodding.dev/en/docs/guides/plugin/player-input-guide
        //
        // To detect player input, you must:
        // 1. Listen to SyncInteractionChains packet (ID: 290)
        // 2. Check the InteractionType enum for the action:
        //    - Primary (0) - left click
        //    - Secondary (1) - right click
        //    - Power1 (2) - power key 1
        //    - Power2 (3) - power key 2
        //    - Power3 (4) - power key 3
        //    - Use (5) - use key (F)
        //    - Other types: Pick, Pickup, CollisionEnter, etc.
        //
        // THERE IS NO WAY TO CREATE CUSTOM KEYBINDS OR DETECT ARBITRARY KEYS.
        // You are limited to the InteractionTypes the client sends.
        //
        // Example packet listener implementation:
        //   pluginInstance.getPacketRegistry().register(
        //       PacketDirection.CLIENT_TO_SERVER,
        //       (handler, packet) -> {
        //           if (packet.getId() != 290) return;
        //           SyncInteractionChains chains = (SyncInteractionChains) packet;
        //           for (SyncInteractionChain chain : chains.updates) {
        //               if (chain.interactionType == InteractionType.Use) {
        //                   // Player pressed F key
        //               }
        //           }
        //       }
        //   );
        //
        // For powers triggered by existing interactions (e.g., left-click attack,
        // right-click use), you can use the built-in interaction system instead.
        // ============================================================================

        LOGGER.atInfo().log("Event listeners registered (Input detection not available in Hytale API)");
    }


    /**
     * Called each server tick to update powers.
     */
    public static void tick() {
        if (instance != null) {
            SCHEDULER.tick();
            instance.componentManager.tickAll();
        }
    }

    /**
     * Create a namespaced identifier string.
     * Uses lowercase namespace to match JSON convention.
     */
    public static String identifier(String path) {
        return Aspectral.getInstance().getManifest().getName().toLowerCase() + ":" + path;
    }

    // Accessors

    public static AspectPowers getInstance() {
        return instance;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static HytaleLogger getLogger() {
        return LOGGER;
    }

    public PowerTypeRegistry getPowerRegistry() {
        return powerTypeRegistry;
    }

    public AspectRegistry getAspectRegistry() {
        return aspectRegistry;
    }

    public AspectLoader getAspectLoader() {
        return aspectLoader;
    }

    public PowerHolderComponent.Manager getComponentManager() {
        return componentManager;
    }

    public PlayerAspectComponent.Manager getPlayerAspectManager() {
        return playerAspectManager;
    }

    public PowerPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
}


