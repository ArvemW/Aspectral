package arvem.aspectral;

import arvem.aspectral.command.AspectCommand;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * Main plugin class for Aspectral mod.
 * Manages plugin lifecycle and core systems.
 */
public class Aspectral extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Aspectral instance;


    public Aspectral(JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Initializing Aspectral v%s", this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        try {
            // Initialize AspectAbilities system
            AspectAbilities.initialize(this);

            // Register commands
            this.getCommandRegistry().registerCommand(new AspectCommand());
            AspectAbilities.registerCommands(this);

            LOGGER.atInfo().log("Aspectral loaded successfully!");
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load Aspectral: %s", e.getMessage());
            throw e;
        }
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down Aspectral...");

        // Save all player abilities before shutdown
        AspectAbilities.shutdown();

        LOGGER.atInfo().log("Aspectral shutdown complete.");
    }

    // Accessors
    public static Aspectral getInstance() {
        return instance;
    }
}
