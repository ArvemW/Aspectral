package arvem.aspectral.persistence;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.component.PowerHolderComponent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages persistence of player powers.
 * Saves powers when players disconnect and loads them when they join.
 */
public class PowerPersistenceManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path dataDirectory;

    // Cache of player data by UUID for quick access
    private final ConcurrentMap<UUID, JsonObject> playerDataCache = new ConcurrentHashMap<>();

    public PowerPersistenceManager(JavaPlugin plugin) {
        // Create data directory in the run folder
        this.dataDirectory = Path.of("mods", "Aspectral", "players");
        try {
            Files.createDirectories(dataDirectory);
            LOGGER.atInfo().log("Power persistence directory: %s", dataDirectory.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to create power data directory: %s", e.getMessage());
        }
    }

    /**
     * Register event listeners for player join/leave.
     */
    public void registerEvents(JavaPlugin plugin) {
        var eventRegistry = plugin.getEventRegistry();

        // Load powers when player is ready
        eventRegistry.registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);

        // Save powers when player disconnects
        eventRegistry.register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        LOGGER.atInfo().log("Registered power persistence events");
    }

    /**
     * Called when a player is ready (joined and loaded).
     */
    private void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        Player player = event.getPlayer();
        Ref<EntityStore> ref = event.getPlayerRef();

        // Get UUID from player
        UUID uuid = getPlayerUuid(player);
        if (uuid == null) {
            LOGGER.atWarning().log("Could not get UUID for player, skipping power load");
            return;
        }

        // Load powers asynchronously
        loadPlayerAbilities(uuid, player, ref);
    }

    /**
     * Called when a player disconnects.
     */
    private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        UUID uuid = playerRef.getUuid();

        if (uuid == null) {
            LOGGER.atWarning().log("Could not get UUID for disconnecting player, skipping power save");
            return;
        }

        // Get reference and execute on world thread
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            var world = store.getExternalData().getWorld();

            // Execute on the world thread to avoid threading issues
            world.execute(() -> {
                if (ref.isValid()) {
                    // Save powers
                    savePlayerAbilitiesInternal(uuid, playerRef, ref, store);

                    // Clean up component from memory
                    cleanupPlayerInternal(uuid, playerRef, ref, store);
                }
            });
        }
    }

    /**
     * Internal method to save powers - must be called on world thread.
     */
    private void savePlayerAbilitiesInternal(UUID uuid, PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());

            if (player == null) {
                LOGGER.atFine().log("No player component for %s, skipping save", uuid);
                return;
            }

            HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);
            PowerHolderComponent powerComponent = PowerHolderComponent.get(adapter);

            // Also get the aspect component
            var aspectComponent = AspectPowers.getInstance().getPlayerAspectManager().get(adapter);

            // If no powers and no aspect, delete the file
            if ((powerComponent == null || powerComponent.getAbilities().isEmpty()) &&
                (aspectComponent == null || !aspectComponent.hasAspect())) {
                Path playerFile = getPlayerFile(uuid);
                Files.deleteIfExists(playerFile);
                playerDataCache.remove(uuid);
                LOGGER.atFine().log("No powers or aspect to save for player %s", uuid);
                return;
            }

            // Create save data object
            JsonObject data = new JsonObject();

            // Save powers
            if (powerComponent != null) {
                data.add("powers", powerComponent.toJson());
            }

            // Save aspect ID
            if (aspectComponent != null && aspectComponent.hasAspect()) {
                data.add("aspect", aspectComponent.toJson());
            }

            Path playerFile = getPlayerFile(uuid);
            Files.writeString(playerFile, GSON.toJson(data));

            // Update cache
            playerDataCache.put(uuid, data);

            int powerCount = powerComponent != null ? powerComponent.getAbilities().size() : 0;
            String aspectId = aspectComponent != null ? aspectComponent.getAspectId() : "none";
            LOGGER.atInfo().log("Saved %d powers and aspect %s for player %s",
                powerCount, aspectId, uuid);

        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to save powers for player %s: %s", uuid, e.getMessage());
        }
    }

    /**
     * Internal cleanup method - must be called on world thread.
     */
    private void cleanupPlayerInternal(UUID uuid, PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);
                AspectPowers.getInstance().getComponentManager().remove(adapter);
                AspectPowers.getInstance().getPlayerAspectManager().remove(adapter);
            }
        } catch (Exception e) {
            LOGGER.atFine().log("Error during cleanup for %s: %s", uuid, e.getMessage());
        }
    }

    /**
     * Load powers for a player from disk.
     */
    @SuppressWarnings({"deprecation", "removal"})
    private void loadPlayerAbilities(UUID uuid, Player player, Ref<EntityStore> ref) {
        Path playerFile = getPlayerFile(uuid);

        if (!Files.exists(playerFile)) {
            LOGGER.atFine().log("No saved powers for player %s", uuid);
            return;
        }

        try {
            String json = Files.readString(playerFile);
            JsonObject data = JsonParser.parseString(json).getAsJsonObject();

            // Cache the data
            playerDataCache.put(uuid, data);

            // Create adapter and component using player's PlayerRef
            Store<EntityStore> store = ref.getStore();
            PlayerRef playerRef = player.getPlayerRef();

            if (playerRef != null) {
                HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);

                // Load aspect first (if present in new format)
                if (data.has("aspect")) {
                    var aspectComponent = AspectPowers.getInstance().getPlayerAspectManager().getOrCreate(adapter);
                    aspectComponent.fromJson(data.getAsJsonObject("aspect"));

                    // Trigger onPlayerJoin to recreate powers from aspect
                    aspectComponent.onPlayerJoin();

                    LOGGER.atInfo().log("Restored aspect %s for player %s",
                        aspectComponent.getAspectId(), uuid);
                }

                // Then load any additional powers (new format or old format)
                if (data.has("powers")) {
                    // New format: powers are nested under "powers" key
                    PowerHolderComponent powerComponent = PowerHolderComponent.getOrCreate(adapter);
                    powerComponent.fromJson(data.getAsJsonObject("powers"));

                    LOGGER.atInfo().log("Loaded %d powers for player %s",
                        powerComponent.getAbilities().size(), uuid);
                } else if (data.has("abilities_list")) {
                    // Old format: powers are at root level
                    PowerHolderComponent powerComponent = PowerHolderComponent.getOrCreate(adapter);
                    powerComponent.fromJson(data);

                    LOGGER.atInfo().log("Loaded %d powers (old format) for player %s",
                        powerComponent.getAbilities().size(), uuid);
                }
            } else {
                LOGGER.atWarning().log("Could not get PlayerRef for player %s", uuid);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to load powers for player %s: %s", uuid, e.getMessage());
        }
    }


    /**
     * Save all online players' powers (for server shutdown).
     */
    public void saveAllPlayers() {
        LOGGER.atInfo().log("Saving all player powers...");

        for (PowerHolderComponent powerComponent : AspectPowers.getInstance().getComponentManager().getAll()) {
            if (powerComponent.getEntity() instanceof HytalePlayerAdapter adapter) {
                UUID uuid = adapter.getUuid();
                if (uuid != null) {
                    try {
                        // Get aspect component
                        var aspectComponent = AspectPowers.getInstance().getPlayerAspectManager().get(adapter);

                        // Create save data
                        JsonObject data = new JsonObject();
                        boolean hasData = false;

                        // Add powers
                        if (!powerComponent.getAbilities().isEmpty()) {
                            data.add("powers", powerComponent.toJson());
                            hasData = true;
                        }

                        // Add aspect
                        if (aspectComponent != null && aspectComponent.hasAspect()) {
                            data.add("aspect", aspectComponent.toJson());
                            hasData = true;
                        }

                        // Save if we have any data
                        if (hasData) {
                            Path playerFile = getPlayerFile(uuid);
                            Files.writeString(playerFile, GSON.toJson(data));
                            LOGGER.atFine().log("Saved powers and aspect for %s", uuid);
                        }
                    } catch (Exception e) {
                        LOGGER.atWarning().log("Failed to save powers for %s: %s", uuid, e.getMessage());
                    }
                }
            }
        }

        LOGGER.atInfo().log("Finished saving player powers");
    }

    /**
     * Get the file path for a player's power data.
     */
    private Path getPlayerFile(UUID uuid) {
        return dataDirectory.resolve(uuid.toString() + ".json");
    }

    /**
     * Get the UUID from a Player entity.
     */
    @SuppressWarnings({"deprecation", "removal"})
    private UUID getPlayerUuid(Player player) {
        return player.getUuid();
    }
}


