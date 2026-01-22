package arvem.aspectral.event;

import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.component.PowerHolderComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Global event listener that dispatches events to player powers.
 * Forwards Hytale server events to PowerHolderComponent.
 */
public class PowerEventDispatcher {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Called when a player takes damage.
     * Forwards to all active powers.
     *
     * @param player The player taking damage
     * @param playerRef The player reference
     * @param ref The entity reference
     * @param store The entity store
     * @param event The damage event (placeholder for now)
     * @return true if damage should be cancelled
     */
    public static boolean onPlayerDamage(Player player, PlayerRef playerRef,
                                         Ref<EntityStore> ref, Store<EntityStore> store,
                                         Object event) {
        try {
            HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component != null) {
                return component.onDamage(event);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("Error dispatching damage event: %s", e.getMessage());
        }
        return false;
    }

    /**
     * Called when a player deals damage.
     * Forwards to all active powers.
     *
     * @param player The player dealing damage
     * @param playerRef The player reference
     * @param ref The entity reference
     * @param store The entity store
     * @param event The attack event (placeholder for now)
     * @return true if attack should be cancelled
     */
    public static boolean onPlayerAttack(Player player, PlayerRef playerRef,
                                         Ref<EntityStore> ref, Store<EntityStore> store,
                                         Object event) {
        try {
            HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component != null) {
                return component.onAttack(event);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("Error dispatching attack event: %s", e.getMessage());
        }
        return false;
    }

    /**
     * Called when a player moves.
     * Forwards to all active powers.
     *
     * @param player The player moving
     * @param playerRef The player reference
     * @param ref The entity reference
     * @param store The entity store
     * @param event The move event (placeholder for now)
     * @return true if movement should be cancelled
     */
    public static boolean onPlayerMove(Player player, PlayerRef playerRef,
                                       Ref<EntityStore> ref, Store<EntityStore> store,
                                       Object event) {
        try {
            HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component != null) {
                return component.onMove(event);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("Error dispatching move event: %s", e.getMessage());
        }
        return false;
    }

    /**
     * Called when a player presses a key.
     * Forwards to all active powers.
     *
     * @param player The player pressing the key
     * @param playerRef The player reference
     * @param ref The entity reference
     * @param store The entity store
     * @param key The key that was pressed
     */
    public static void onKeyPressed(Player player, PlayerRef playerRef,
                                    Ref<EntityStore> ref, Store<EntityStore> store,
                                    String key) {
        try {
            HytalePlayerAdapter adapter = new HytalePlayerAdapter(player, playerRef, ref, store);
            PowerHolderComponent component = PowerHolderComponent.get(adapter);

            if (component != null) {
                component.onKeyPressed(key);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("Error dispatching key press event: %s", e.getMessage());
        }
    }
}

