package arvem.aspectral.api;

/**
 * Placeholder interface representing a server-side player in Hytale.
 * This should be replaced with the actual Hytale API class when available.
 */
public interface Player extends LivingEntity {

    /**
     * Get the player's name.
     */
    String getName();

    /**
     * Send a message to this player.
     */
    void sendMessage(String message);

    /**
     * Check if this player has a specific permission.
     */
    boolean hasPermission(String permission);
}
