package arvem.aspectral.abilities;

/**
 * Interface for abilities that can be actively triggered by the player.
 */
public interface Activatable {

    /**
     * Called when the ability is activated by the player.
     */
    void onActivate();

    /**
     * Get the key binding identifier for this ability.
     * @return The key binding ID, or "none" if no key binding
     */
    default String getKeyBinding() {
        return "none";
    }
}
