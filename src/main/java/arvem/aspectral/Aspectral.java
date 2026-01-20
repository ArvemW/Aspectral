package arvem.aspectral;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
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
            this.getCommandRegistry().registerCommand(new AspectCommand());
            LOGGER.atInfo().log("Aspectral loaded successfully!");
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load Aspectral: %s", e.getMessage());
            throw e;
        }
    }

    // Accessors
    public static Aspectral getInstance() {
        return instance;
    }
}
