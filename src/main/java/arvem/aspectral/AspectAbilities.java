package arvem.aspectral;

import arvem.aspectral.abilities.factory.AbilityFactories;
import arvem.aspectral.abilities.factory.action.EntityActions;
import arvem.aspectral.abilities.factory.condition.EntityConditions;
import arvem.aspectral.aspect.AspectLoader;
import arvem.aspectral.aspect.AspectRegistry;
import arvem.aspectral.command.AbilityCommand;
import arvem.aspectral.command.AspectCommand;
import arvem.aspectral.component.AbilityHolderComponent;
import arvem.aspectral.component.PlayerAspectComponent;
import arvem.aspectral.persistence.AbilityPersistenceManager;
import arvem.aspectral.registry.AbilityRegistry;
import arvem.aspectral.util.Scheduler;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.nio.file.Path;

/**
 * AspectAbilities - A power/ability system for Hytale.
 * Inspired by Apoli/Origins but designed specifically for Hytale's architecture.
 * <p>
 * Unlike the Minecraft version, this is server-side only but can still control
 * UI elements and client-visible effects through Hytale's server-authoritative model.
 */
public final class AspectAbilities {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static AspectAbilities instance;
    private static JavaPlugin plugin;

    public static final Scheduler SCHEDULER = new Scheduler();

    // Ability holder component for entity ability management
    private final AbilityHolderComponent.Manager componentManager;

    // Player aspect component for storing player's chosen aspect
    private final PlayerAspectComponent.Manager playerAspectManager;

    // Registry for ability types
    private final AbilityRegistry abilityRegistry;

    // Registry for aspects (origins)
    private final AspectRegistry aspectRegistry;

    // Loader for aspect JSON files
    private final AspectLoader aspectLoader;

    // Persistence manager for saving/loading abilities
    private final AbilityPersistenceManager persistenceManager;

    private AspectAbilities(JavaPlugin pluginInstance) {
        this.componentManager = new AbilityHolderComponent.Manager();
        this.playerAspectManager = new PlayerAspectComponent.Manager();
        this.abilityRegistry = new AbilityRegistry();
        this.aspectRegistry = new AspectRegistry();
        this.aspectLoader = new AspectLoader(aspectRegistry, abilityRegistry);
        this.persistenceManager = new AbilityPersistenceManager(pluginInstance);
    }

    /**
     * Initialize the AspectAbilities system.
     * Call this during plugin setup.
     */
    public static void initialize(JavaPlugin pluginInstance) {
        if (instance != null) {
            LOGGER.atWarning().log("AspectAbilities already initialized!");
            return;
        }

        plugin = pluginInstance;
        instance = new AspectAbilities(pluginInstance);

        LOGGER.atInfo().log("Initializing AspectAbilities v%s", Aspectral.getInstance().getManifest().getVersion().toString());

        // Register core systems
        instance.registerCoreFactories();
        instance.registerConditions();
        instance.registerActions();

        // Load aspects from JSON
        instance.loadAspects(pluginInstance);

        // Register persistence events
        instance.persistenceManager.registerEvents(pluginInstance);

        LOGGER.atInfo().log("AspectAbilities initialized successfully!");
    }

    /**
     * Called when the server is shutting down.
     * Saves all player abilities.
     */
    public static void shutdown() {
        if (instance != null) {
            instance.persistenceManager.saveAllPlayers();
            LOGGER.atInfo().log("AspectAbilities shutdown complete.");
        }
    }

    /**
     * Register command handlers for the ability system.
     */
    public static void registerCommands(Aspectral plugin) {
        plugin.getCommandRegistry().registerCommand(new AbilityCommand());
        plugin.getCommandRegistry().registerCommand(new AspectCommand());
        LOGGER.atInfo().log("AspectAbilities commands registered.");
    }

    private void registerCoreFactories() {
        AbilityFactories.register(abilityRegistry);
        LOGGER.atFine().log("Registered ability factories");
    }

    private void registerConditions() {
        EntityConditions.register(abilityRegistry);
        LOGGER.atFine().log("Registered entity conditions");
    }

    private void registerActions() {
        EntityActions.register(abilityRegistry);
        LOGGER.atFine().log("Registered entity actions");
    }

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

    /**
     * Called each server tick to update abilities.
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

    public static AspectAbilities getInstance() {
        return instance;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static HytaleLogger getLogger() {
        return LOGGER;
    }

    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    public AspectRegistry getAspectRegistry() {
        return aspectRegistry;
    }

    public AspectLoader getAspectLoader() {
        return aspectLoader;
    }

    public AbilityHolderComponent.Manager getComponentManager() {
        return componentManager;
    }

    public PlayerAspectComponent.Manager getPlayerAspectManager() {
        return playerAspectManager;
    }

    public AbilityPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
}
