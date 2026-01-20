package arvem.aspectral.api;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Adapter for Hytale's Player that extends HytaleLivingEntityAdapter
 * and adds player-specific functionality.
 *
 * This properly integrates with Hytale's ECS architecture while providing
 * convenient access to player-specific features.
 */
public class HytalePlayerAdapter extends HytaleLivingEntityAdapter {

    private final Player player;
    private final PlayerRef playerRef;

    /**
     * Create an adapter for a Hytale Player.
     *
     * @param player The Hytale Player component
     * @param playerRef The PlayerRef for this player
     * @param entityRef The entity reference in the ECS store
     * @param store The entity store
     */
    public HytalePlayerAdapter(
            @Nonnull Player player,
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> entityRef,
            @Nonnull Store<EntityStore> store) {
        super(player, entityRef, store);
        this.player = player;
        this.playerRef = playerRef;
    }

    /**
     * Get the underlying Hytale Player.
     */
    @Nonnull
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the PlayerRef for this player.
     */
    @Nonnull
    public PlayerRef getPlayerRef() {
        return playerRef;
    }

    /**
     * Get the player's UUID.
     */
    @Nullable
    public UUID getUuid() {
        return playerRef.getUuid();
    }

    /**
     * Get the player's display name.
     */
    @Nonnull
    public String getDisplayName() {
        return player.getDisplayName();
    }

    /**
     * Get the player's username.
     */
    @Nonnull
    public String getUsername() {
        return playerRef.getUsername();
    }

    /**
     * Send a message to this player.
     */
    public void sendMessage(@Nonnull String message) {
        playerRef.sendMessage(Message.raw(message));
    }

    /**
     * Send a formatted message to this player.
     */
    public void sendMessage(@Nonnull Message message) {
        playerRef.sendMessage(message);
    }

    /**
     * Check if this player has a specific permission.
     */
    public boolean hasPermission(@Nonnull String permission) {
        return player.hasPermission(permission);
    }

    /**
     * Check if this player has a specific permission with a default value.
     */
    public boolean hasPermission(@Nonnull String permission, boolean defaultValue) {
        return player.hasPermission(permission, defaultValue);
    }

    /**
     * Get the player's current game mode.
     */
    @Nullable
    public GameMode getGameMode() {
        return player.getGameMode();
    }

    /**
     * Check if the player is in creative mode.
     */
    public boolean isCreative() {
        return player.getGameMode() == GameMode.Creative;
    }


    /**
     * Check if the player is in adventure mode.
     */
    public boolean isAdventure() {
        return player.getGameMode() == GameMode.Adventure;
    }

    /**
     * Check if the player has spawn protection.
     */
    public boolean hasSpawnProtection() {
        return player.hasSpawnProtection();
    }

    /**
     * Check if the player is waiting for client ready.
     */
    public boolean isWaitingForClientReady() {
        return player.isWaitingForClientReady();
    }

    /**
     * Check if this is the player's first spawn.
     */
    public boolean isFirstSpawn() {
        return player.isFirstSpawn();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HytalePlayerAdapter other) {
            UUID thisUuid = getUuid();
            UUID otherUuid = other.getUuid();
            if (thisUuid != null && otherUuid != null) {
                return thisUuid.equals(otherUuid);
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        UUID uuid = getUuid();
        return uuid != null ? uuid.hashCode() : super.hashCode();
    }

    @Override
    public String toString() {
        return "HytalePlayerAdapter[" + getUsername() + ", uuid=" + getUuid() + "]";
    }
}
