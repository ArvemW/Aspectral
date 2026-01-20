package arvem.aspectral;

import arvem.aspectral.abilities.factory.AbilityFactories;
import arvem.aspectral.abilities.factory.action.EntityActions;
import arvem.aspectral.abilities.factory.condition.EntityConditions;
import arvem.aspectral.command.AbilityCommand;
import arvem.aspectral.component.AbilityHolderComponent;
import arvem.aspectral.persistence.AbilityPersistenceManager;
import arvem.aspectral.registry.AbilityRegistry;
import arvem.aspectral.util.Scheduler;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

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

    // Registry for ability types
    private final AbilityRegistry abilityRegistry;

    // Persistence manager for saving/loading abilities
    private final AbilityPersistenceManager persistenceManager;

    private AspectAbilities(JavaPlugin pluginInstance) {
        this.componentManager = new AbilityHolderComponent.Manager();
        this.abilityRegistry = new AbilityRegistry();
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
     */
    public static String identifier(String path) {
        return Aspectral.getInstance().getManifest().getName() + ":" + path;
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

    public AbilityHolderComponent.Manager getComponentManager() {
        return componentManager;
    }

    public AbilityPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
}
