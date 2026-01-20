package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import com.google.gson.JsonObject;

/**
 * An ability that can be toggled on/off.
 */
public class ToggleAbility extends Ability {

    private boolean toggled;
    private final boolean defaultValue;

    public ToggleAbility(AbilityType<?> type, LivingEntity entity, boolean defaultValue) {
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

    public static AbilityFactory<ToggleAbility> createFactory() {
        return new AbilityFactory<ToggleAbility>(
            AspectAbilities.identifier("toggle"),
            new SerializableData()
                .add("active_by_default", SerializableDataTypes.BOOLEAN, false)
                .add("retain_state", SerializableDataTypes.BOOLEAN, true),
            data -> (type, entity) -> new ToggleAbility(type, entity, data.get("active_by_default"))
        ).allowCondition();
    }
}
