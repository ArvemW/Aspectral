package arvem.aspectral.persistence;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.component.AbilityHolderComponent;
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
 * Manages persistence of player abilities.
 * Saves abilities when players disconnect and loads them when they join.
 */
public class AbilityPersistenceManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path dataDirectory;

    // Cache of player data by UUID for quick access
    private final ConcurrentMap<UUID, JsonObject> playerDataCache = new ConcurrentHashMap<>();

    public AbilityPersistenceManager(JavaPlugin plugin) {
        // Create data directory in the run folder
        this.dataDirectory = Path.of("mods", "Aspectral", "players");
        try {
            Files.createDirectories(dataDirectory);
            LOGGER.atInfo().log("Ability persistence directory: %s", dataDirectory.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to create ability data directory: %s", e.getMessage());
        }
    }

    /**
     * Register event listeners for player join/leave.
     */
    public void registerEvents(JavaPlugin plugin) {
        var eventRegistry = plugin.getEventRegistry();

        // Load abilities when player is ready
        eventRegistry.registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);

        // Save abilities when player disconnects
        eventRegistry.register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        LOGGER.atInfo().log("Registered ability persistence events");
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
            LOGGER.atWarning().log("Could not get UUID for player, skipping ability load");
            return;
        }

        // Load abilities asynchronously
        loadPlayerAbilities(uuid, player, ref);
    }

    /**
     * Called when a player disconnects.
     */
    private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        UUID uuid = playerRef.getUuid();

        if (uuid == null) {
            LOGGER.atWarning().log("Could not get UUID for disconnecting player, skipping ability save");
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
                    // Save abilities
                    savePlayerAbilitiesInternal(uuid, playerRef, ref, store);

                    // Clean up component from memory
                    cleanupPlayerInternal(uuid, playerRef, ref, store);
                }
            });
        }
    }

    /**
     * Internal method to save abilities - must be called on world thread.
     */
    private void savePlayerAbilitiesInternal(UUID uuid, PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());

            if (player == null) {
                LOGGER.atFine().log("No player component for %s, skipping save", uuid);
                return;
            }

            HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);
            AbilityHolderComponent component = AbilityHolderComponent.get(adapter);

            if (component == null || component.getAbilities().isEmpty()) {
                // Delete file if no abilities
                Path playerFile = getPlayerFile(uuid);
                Files.deleteIfExists(playerFile);
                playerDataCache.remove(uuid);
                LOGGER.atFine().log("No abilities to save for player %s", uuid);
                return;
            }

            // Serialize and save
            JsonObject data = component.toJson();
            Path playerFile = getPlayerFile(uuid);
            Files.writeString(playerFile, GSON.toJson(data));

            // Update cache
            playerDataCache.put(uuid, data);

            LOGGER.atInfo().log("Saved %d abilities for player %s",
                component.getAbilities().size(), uuid);

        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to save abilities for player %s: %s", uuid, e.getMessage());
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
                AspectAbilities.getInstance().getComponentManager().remove(adapter);
            }
        } catch (Exception e) {
            LOGGER.atFine().log("Error during cleanup for %s: %s", uuid, e.getMessage());
        }
    }

    /**
     * Load abilities for a player from disk.
     */
    @SuppressWarnings({"deprecation", "removal"})
    private void loadPlayerAbilities(UUID uuid, Player player, Ref<EntityStore> ref) {
        Path playerFile = getPlayerFile(uuid);

        if (!Files.exists(playerFile)) {
            LOGGER.atFine().log("No saved abilities for player %s", uuid);
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
                AbilityHolderComponent component = AbilityHolderComponent.getOrCreate(adapter);
                component.fromJson(data);

                LOGGER.atInfo().log("Loaded %d abilities for player %s",
                    component.getAbilities().size(), uuid);
            } else {
                LOGGER.atWarning().log("Could not get PlayerRef for player %s", uuid);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to load abilities for player %s: %s", uuid, e.getMessage());
        }
    }


    /**
     * Save all online players' abilities (for server shutdown).
     */
    public void saveAllPlayers() {
        LOGGER.atInfo().log("Saving all player abilities...");

        for (AbilityHolderComponent component : AspectAbilities.getInstance().getComponentManager().getAll()) {
            if (component.getEntity() instanceof HytalePlayerAdapter adapter) {
                UUID uuid = adapter.getUuid();
                if (uuid != null) {
                    try {
                        JsonObject data = component.toJson();
                        if (!component.getAbilities().isEmpty()) {
                            Path playerFile = getPlayerFile(uuid);
                            Files.writeString(playerFile, GSON.toJson(data));
                            LOGGER.atFine().log("Saved abilities for %s", uuid);
                        }
                    } catch (Exception e) {
                        LOGGER.atWarning().log("Failed to save abilities for %s: %s", uuid, e.getMessage());
                    }
                }
            }
        }

        LOGGER.atInfo().log("Finished saving player abilities");
    }

    /**
     * Get the file path for a player's ability data.
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
