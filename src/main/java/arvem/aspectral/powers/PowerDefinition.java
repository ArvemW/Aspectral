package arvem.aspectral.powers;

import com.google.gson.JsonObject;

/**
 * Holds the definition of a power.
 * This is the template + data, not the actual power instance.
 * <p>
 * PowerDefinitions are created from power JSON files and stored
 * in the PowerTypeRegistry for reuse across multiple aspects.
 */
public class PowerDefinition {
    public final PowerType<?> powerType;
    public final JsonObject data;

    public PowerDefinition(PowerType<?> powerType, JsonObject data) {
        this.powerType = powerType;
        this.data = data;
    }

    public String getTypeId() {
        return powerType.getIdentifier();
    }
}

