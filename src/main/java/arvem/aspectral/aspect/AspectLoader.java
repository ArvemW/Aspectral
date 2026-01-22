package arvem.aspectral.aspect;

import arvem.aspectral.registry.PowerTypeRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Loads Aspect definitions from JSON files.
 * Mirrors Origins' data loading system.
 * <p>
 * Aspects now reference power IDs instead of embedding power definitions.
 */
public class AspectLoader {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final AspectRegistry registry;

    public AspectLoader(AspectRegistry registry, PowerTypeRegistry powerTypeRegistry) {
        this.registry = registry;
    }

    /**
     * Load all aspects from a directory.
     * @param aspectsDir Directory containing JSON files
     */
    public void loadFromDirectory(Path aspectsDir) {
        LOGGER.atInfo().log("AspectLoader.loadFromDirectory called");
        LOGGER.atInfo().log("Loading aspects from: %s", aspectsDir.toAbsolutePath());
        LOGGER.atInfo().log("Directory exists: %s", Files.exists(aspectsDir));


        if (!Files.exists(aspectsDir)) {
            LOGGER.atInfo().log("Aspects directory does not exist: %s", aspectsDir);
            try {
                Files.createDirectories(aspectsDir);
                LOGGER.atInfo().log("Created aspects directory: %s", aspectsDir);
            } catch (IOException e) {
                LOGGER.atSevere().log("Failed to create aspects directory: %s", e.getMessage());
            }
            return;
        }

        if (!Files.isDirectory(aspectsDir)) {
            LOGGER.atWarning().log("Aspects path is not a directory: %s", aspectsDir);
            return;
        }

        int loaded = 0;
        int filesFound = 0;
        try (Stream<Path> paths = Files.walk(aspectsDir)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                if (Files.isRegularFile(path) && path.toString().endsWith(".json")) {
                    filesFound++;
                    LOGGER.atInfo().log("Found JSON file: %s", path.getFileName());
                    try {
                        loadAspect(path);
                        loaded++;
                    } catch (Exception e) {
                        LOGGER.atWarning().log("Failed to load aspect from %s: %s",
                            path.getFileName(), e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to read aspects directory: %s", e.getMessage());
        }

        LOGGER.atInfo().log("Found %d JSON file(s), loaded %d aspect(s) from %s", filesFound, loaded, aspectsDir);
    }

    /**
     * Load a single aspect from a JSON file.
     */
    public void loadAspect(Path jsonFile) throws IOException {
        LOGGER.atInfo().log("Loading aspect from: %s", jsonFile.getFileName());

        JsonObject json;
        try (FileReader reader = new FileReader(jsonFile.toFile())) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }

        // Derive identifier from filename (e.g., "human.json" -> "aspectral:human")
        String filename = jsonFile.getFileName().toString();
        String aspectId = filename.substring(0, filename.length() - 5); // Remove .json

        // Check if JSON has namespace
        if (json.has("id")) {
            aspectId = json.get("id").getAsString();
        } else {
            // Default namespace
            aspectId = "aspectral:" + aspectId;
        }

        LOGGER.atInfo().log("Parsed aspect ID: %s", aspectId);
        Aspect aspect = parseAspect(aspectId, json);
        LOGGER.atInfo().log("Parsed aspect with %d powers", aspect.getPowerCount());
        registry.register(aspect);
        LOGGER.atInfo().log("Aspect registered: %s", aspectId);
    }

    /**
     * Parse an Aspect from JSON.
     * <p>
     * New format (Origins-style):
     * {
     *   "powers": ["aspectral:launch", "aspectral:double_jump"],
     *   "icon": "...",
     *   "order": 0,
     *   "impact": 2
     * }
     */
    public Aspect parseAspect(String identifier, JsonObject json) {
        Aspect aspect = new Aspect(identifier);

        // Parse metadata
        if (json.has("name")) {
            aspect.setName(json.get("name").getAsString());
        }
        if (json.has("description")) {
            aspect.setDescription(json.get("description").getAsString());
        }
        if (json.has("icon")) {
            aspect.setIcon(json.get("icon").getAsString());
        }
        if (json.has("order")) {
            aspect.setOrder(json.get("order").getAsInt());
        }
        if (json.has("impact")) {
            aspect.setImpact(json.get("impact").getAsInt());
        }
        if (json.has("unchoosable")) {
            aspect.setUnchoosable(json.get("unchoosable").getAsBoolean());
        }

        // Parse powers - now just references to power IDs
        if (json.has("powers")) {
            JsonArray powersArray = json.getAsJsonArray("powers");
            for (JsonElement powerElem : powersArray) {
                if (powerElem.isJsonPrimitive()) {
                    // Power ID reference (e.g., "aspectral:launch")
                    String powerId = powerElem.getAsString();
                    aspect.addPower(powerId);
                } else {
                    LOGGER.atWarning().log("Invalid power reference in aspect %s: must be a string ID", identifier);
                }
            }
        }

        return aspect;
    }

    /**
     * Reload all aspects.
     */
    public void reload(Path aspectsDir) {
        registry.clear();
        loadFromDirectory(aspectsDir);
    }
}


