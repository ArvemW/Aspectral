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
 * A power that can be toggled on/off.
 */
public class TogglePower extends Power {

    private boolean toggled;
    private final boolean defaultValue;

    public TogglePower(PowerType<?> type, LivingEntity entity, boolean defaultValue) {
        super(type, entity);
        this.defaultValue = defaultValue;
        this.toggled = defaultValue;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void toggle() {
        toggled = !toggled;
    }

    public void setToggled(boolean value) {
        toggled = value;
    }

    @Override
    public boolean isActive() {
        return toggled && super.isActive();
    }

    @Override
    public void onRespawn() {
        if (dataInstance != null && dataInstance.isPresent("retain_state")
            && !(boolean) dataInstance.get("retain_state")) {
            toggled = defaultValue;
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("toggled", toggled);
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        if (json.has("toggled")) {
            toggled = json.get("toggled").getAsBoolean();
        }
    }

    public static PowerFactory<TogglePower> createFactory() {
        return new PowerFactory<TogglePower>(
            AspectPowers.identifier("toggle"),
            new SerializableData()
                .add("active_by_default", SerializableDataTypes.BOOLEAN, false)
                .add("retain_state", SerializableDataTypes.BOOLEAN, true),
            data -> (type, entity) -> new TogglePower(type, entity, data.get("active_by_default"))
        ).allowCondition();
    }
}


