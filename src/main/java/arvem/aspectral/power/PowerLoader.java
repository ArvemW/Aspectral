package arvem.aspectral.power;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.PowerDefinition;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.registry.PowerTypeRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Loads power (power) definitions from JSON files.
 * <p>
 * Powers are separate from aspects and can be reused across multiple aspects.
 * This follows the Origins model where powers are defined once and referenced by ID.
 */
public class PowerLoader {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final arvem.aspectral.power.PowerRegistry powerRegistry;
    private final PowerTypeRegistry powerTypeRegistry;

    public PowerLoader(arvem.aspectral.power.PowerRegistry powerRegistry, PowerTypeRegistry powerTypeRegistry) {
        this.powerRegistry = powerRegistry;
        this.powerTypeRegistry = powerTypeRegistry;
    }

    /**
     * Load all powers from a directory.
     *
     * @param powersDir Directory containing power JSON files
     */
    public void loadFromDirectory(Path powersDir) {
        LOGGER.atInfo().log("PowerLoader.loadFromDirectory called");
        LOGGER.atInfo().log("Loading powers from: %s", powersDir.toAbsolutePath());
        LOGGER.atInfo().log("Directory exists: %s", Files.exists(powersDir));

        if (!Files.exists(powersDir)) {
            LOGGER.atWarning().log("Powers directory does not exist, creating: %s", powersDir);
            try {
                Files.createDirectories(powersDir);
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Failed to create powers directory");
                return;
            }
        }

        int loadedCount = 0;
        try (Stream<Path> paths = Files.walk(powersDir, 1)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                File file = path.toFile();
                LOGGER.atInfo().log("Found JSON file: %s", file.getName());

                try {
                    loadPower(file);
                    loadedCount++;
                } catch (Exception e) {
                    LOGGER.atSevere().withCause(e).log("Failed to load power from: %s", file.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Failed to walk powers directory");
        }

        LOGGER.atInfo().log("Found %d JSON file(s), loaded %d power(s) from %s",
                loadedCount, loadedCount, powersDir.getFileName());
    }

    /**
     * Load a single power from a JSON file.
     */
    private void loadPower(File file) {
        LOGGER.atInfo().log("Loading power from: %s", file.getName());

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Extract power ID from filename (without .json extension)
            String fileName = file.getName();
            String powerId = AspectPowers.identifier(fileName.substring(0, fileName.length() - 5));

            LOGGER.atInfo().log("Parsed power ID: %s", powerId);

            // Check if the power has a type field
            if (!json.has("type")) {
                LOGGER.atWarning().log("Power %s has no 'type' field, skipping", powerId);
                return;
            }

            String typeId = json.get("type").getAsString();
            var factoryRegistration = powerTypeRegistry.getPowerFactory(typeId);

            if (factoryRegistration == null) {
                LOGGER.atWarning().log("Unknown power factory for power %s: %s", powerId, typeId);
                return;
            }

            // Create the power type from the factory
            var factoryInstance = factoryRegistration.read(json);
            PowerType<?> powerType =
                    new PowerType<>(typeId, factoryInstance);

            // Create the power definition
            PowerDefinition definition = new PowerDefinition(powerType, json);

            // Register the power
            powerRegistry.register(powerId, definition);
            LOGGER.atInfo().log("Power registered: %s (type: %s)", powerId, typeId);

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Failed to parse power JSON: %s", file.getName());
            throw new RuntimeException("Failed to load power: " + file.getName(), e);
        }
    }
}



