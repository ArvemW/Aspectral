package arvem.aspectral.aspect;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.registry.AbilityRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Loads Aspect definitions from JSON files.
 * Mirrors Origins' data loading system.
 */
public class AspectLoader {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final AspectRegistry registry;
    private final AbilityRegistry abilityRegistry;

    public AspectLoader(AspectRegistry registry, AbilityRegistry abilityRegistry) {
        this.registry = registry;
        this.abilityRegistry = abilityRegistry;
    }

    /**
     * Load all aspects from a directory.
     * @param aspectsDir Directory containing JSON files
     */
    public void loadFromDirectory(Path aspectsDir) {
        LOGGER.atInfo().log("AspectLoader.loadFromDirectory called");
        LOGGER.atInfo().log("Loading aspects from: %s", aspectsDir.toAbsolutePath());
        LOGGER.atInfo().log("Directory exists: %s", Files.exists(aspectsDir));

        // Debug: List all registered factories
        var registeredFactories = abilityRegistry.getAllAbilityFactoryIds();
        LOGGER.atInfo().log("Registered ability factories: %d", registeredFactories.size());
        for (String factoryId : registeredFactories) {
            LOGGER.atInfo().log("  - %s", factoryId);
        }

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
        LOGGER.atInfo().log("Parsed aspect with %d abilities", aspect.getAbilityCount());
        registry.register(aspect);
        LOGGER.atInfo().log("Aspect registered: %s", aspectId);
    }

    /**
     * Parse an Aspect from JSON.
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

        // Parse powers/abilities
        if (json.has("powers")) {
            JsonArray powersArray = json.getAsJsonArray("powers");
            for (JsonElement powerElem : powersArray) {
                if (powerElem.isJsonPrimitive()) {
                    // Simple string reference: just ability type ID
                    String abilityTypeId = powerElem.getAsString();

                    // Get the factory and create an AbilityType with default data
                    var factory = abilityRegistry.getAbilityFactory(abilityTypeId);
                    if (factory == null) {
                        LOGGER.atWarning().log("Unknown ability factory in aspect %s: %s",
                            identifier, abilityTypeId);
                        continue;
                    }

                    // Create AbilityType from factory with default data
                    var factoryInstance = factory.createDefault();
                    AbilityType<?> abilityType = new AbilityType<>(abilityTypeId, factoryInstance);
                    aspect.addAbility(abilityType, new JsonObject());

                } else if (powerElem.isJsonObject()) {
                    // Inline ability definition with type and data
                    JsonObject powerJson = powerElem.getAsJsonObject();

                    if (!powerJson.has("type")) {
                        LOGGER.atWarning().log("Ability definition missing 'type' in aspect %s", identifier);
                        continue;
                    }

                    String abilityTypeId = powerJson.get("type").getAsString();

                    // Get the factory and create an AbilityType with JSON data
                    var factory = abilityRegistry.getAbilityFactory(abilityTypeId);
                    if (factory == null) {
                        LOGGER.atWarning().log("Unknown ability factory in aspect %s: %s",
                            identifier, abilityTypeId);
                        continue;
                    }

                    // Create AbilityType from factory with JSON data
                    var factoryInstance = factory.read(powerJson);
                    AbilityType<?> abilityType = new AbilityType<>(abilityTypeId, factoryInstance);
                    aspect.addAbility(abilityType, powerJson);
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
