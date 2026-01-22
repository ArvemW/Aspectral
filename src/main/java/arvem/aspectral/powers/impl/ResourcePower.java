package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import com.google.gson.JsonObject;

/**
 * A power that provides a numeric resource value that can be modified.
 * Similar to mana, energy, charges, etc.
 */
public class ResourcePower extends Power {

    private final int minValue;
    private final int maxValue;
    private final int startValue;
    private int value;

    public ResourcePower(PowerType<?> type, LivingEntity entity,
                         int minValue, int maxValue, int startValue) {
        super(type, entity);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.startValue = startValue;
        this.value = startValue;
    }

    public int getValue() {
        return value;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setValue(int value) {
        this.value = Math.max(minValue, Math.min(maxValue, value));
    }

    public void change(int amount) {
        setValue(value + amount);
    }

    public void increment() {
        change(1);
    }

    public void decrement() {
        change(-1);
    }

    public boolean isFull() {
        return value >= maxValue;
    }

    public boolean isEmpty() {
        return value <= minValue;
    }

    public float getPercentage() {
        if (maxValue == minValue) return 1.0f;
        return (float) (value - minValue) / (maxValue - minValue);
    }

    @Override
    public void onRespawn() {
        if (dataInstance != null && dataInstance.isPresent("reset_on_respawn")
            && (boolean) dataInstance.get("reset_on_respawn")) {
            value = startValue;
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("value", value);
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        if (json.has("value")) {
            value = json.get("value").getAsInt();
        }
    }

    public static PowerFactory<ResourcePower> createFactory() {
        return new PowerFactory<ResourcePower>(
            AspectPowers.identifier("resource"),
            new SerializableData()
                .add("min", SerializableDataTypes.INT, 0)
                .add("max", SerializableDataTypes.INT, 100)
                .add("start_value", SerializableDataTypes.INT, 0)
                .add("reset_on_respawn", SerializableDataTypes.BOOLEAN, false),
            data -> (type, entity) -> new ResourcePower(
                type, entity,
                data.get("min"),
                data.get("max"),
                data.get("start_value")
            )
        ).allowCondition();
    }
}


