package arvem.aspectral.powers;

/**
 * Interface for powers that can be actively triggered by the player.
 */
public interface Activatable {

    /**
     * Called when the power is activated by the player.
     */
    void onActivate();

    /**
     * Get the key binding identifier for this power.
     * @return The key binding ID, or "none" if no key binding
     */
    default String getKeyBinding() {
        return "none";
    }
}


