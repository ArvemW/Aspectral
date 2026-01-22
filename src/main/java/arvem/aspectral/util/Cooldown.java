package arvem.aspectral.util;

import com.google.gson.JsonObject;

/**
 * Helper class for tracking power cooldowns.
 * Counts down in ticks (20 ticks = 1 second).
 */
public class Cooldown {

    private final int maxTicks;
    private int remainingTicks;

    /**
     * Create a cooldown with the specified duration.
     * @param ticks Cooldown duration in ticks (20 ticks = 1 second)
     */
    public Cooldown(int ticks) {
        this.maxTicks = ticks;
        this.remainingTicks = 0;
    }

    /**
     * Check if the cooldown is ready (not active).
     */
    public boolean isReady() {
        return remainingTicks <= 0;
    }

    /**
     * Trigger the cooldown (start counting down from max).
     */
    public void trigger() {
        remainingTicks = maxTicks;
    }

    /**
     * Tick the cooldown down by one.
     */
    public void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    /**
     * Get remaining ticks.
     */
    public int getRemainingTicks() {
        return remainingTicks;
    }

    /**
     * Get remaining seconds (rounded up).
     */
    public int getRemainingSeconds() {
        return (remainingTicks + 19) / 20; // Round up
    }

    /**
     * Get max ticks.
     */
    public int getMaxTicks() {
        return maxTicks;
    }

    /**
     * Reset the cooldown (make it ready immediately).
     */
    public void reset() {
        remainingTicks = 0;
    }

    /**
     * Serialize to JSON.
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("max", maxTicks);
        json.addProperty("remaining", remainingTicks);
        return json;
    }

    /**
     * Deserialize from JSON.
     */
    public void fromJson(JsonObject json) {
        if (json.has("remaining")) {
            remainingTicks = json.get("remaining").getAsInt();
        }
    }
}

