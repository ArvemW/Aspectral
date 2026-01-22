package arvem.aspectral.layer;

import arvem.aspectral.AspectPowers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Loads layer definitions from JSON files.
 */
public class LayerLoader {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final LayerRegistry registry;

    public LayerLoader(LayerRegistry registry) {
        this.registry = registry;
    }

    /**
     * Load all layers from a directory.
     *
     * @param layersDir Directory containing layer JSON files
     */
    public void loadFromDirectory(Path layersDir) {
        LOGGER.atInfo().log("LayerLoader.loadFromDirectory called");
        LOGGER.atInfo().log("Loading layers from: %s", layersDir.toAbsolutePath());
        LOGGER.atInfo().log("Directory exists: %s", Files.exists(layersDir));

        if (!Files.exists(layersDir)) {
            LOGGER.atWarning().log("Layers directory does not exist, creating: %s", layersDir);
            try {
                Files.createDirectories(layersDir);
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Failed to create layers directory");
                return;
            }
        }

        int loadedCount = 0;
        try (Stream<Path> paths = Files.walk(layersDir, 1)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                File file = path.toFile();
                LOGGER.atInfo().log("Found JSON file: %s", file.getName());

                try {
                    loadLayer(file);
                    loadedCount++;
                } catch (Exception e) {
                    LOGGER.atSevere().withCause(e).log("Failed to load layer from: %s", file.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Failed to walk layers directory");
        }

        LOGGER.atInfo().log("Found %d JSON file(s), loaded %d layer(s) from %s",
                loadedCount, loadedCount, layersDir.getFileName());
    }

    /**
     * Load a single layer from a JSON file.
     */
    private void loadLayer(File file) {
        LOGGER.atInfo().log("Loading layer from: %s", file.getName());

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Extract layer ID from filename (without .json extension)
            String fileName = file.getName();
            String layerId = AspectPowers.identifier(fileName.substring(0, fileName.length() - 5));

            LOGGER.atInfo().log("Parsed layer ID: %s", layerId);

            // Create layer
            Layer layer = new Layer(layerId, json);

            // Register
            registry.register(layer);
            LOGGER.atInfo().log("Layer registered: %s", layerId);

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Failed to parse layer JSON: %s", file.getName());
            throw new RuntimeException("Failed to load layer: " + file.getName(), e);
        }
    }
}


