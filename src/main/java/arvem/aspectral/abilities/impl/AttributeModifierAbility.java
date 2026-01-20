package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import com.google.gson.JsonObject;

/**
 * Attribute modifier ability - applies persistent attribute modifications.
 * Supports both JSON and code-defined construction.
 */
public class AttributeModifierAbility extends Ability {

    private final String attribute;
    private final double modifier;
    private final AspectAbilitiesDataTypes.AttributeOperation operation;

    private boolean applied = false;

    /**
     * Create from AbilityType and entity with parameters.
     */
    public AttributeModifierAbility(AbilityType<?> type, LivingEntity entity,
                                   String attribute, double modifier,
                                   AspectAbilitiesDataTypes.AttributeOperation operation) {
        super(type, entity);
        this.attribute = attribute;
        this.modifier = modifier;
        this.operation = operation;
    }

    @Override
    public void onAdded(boolean isSync) {
        super.onAdded(isSync);
        applyModifier();
    }

    @Override
    public void onRemoved(boolean isSync) {
        super.onRemoved(isSync);
        removeModifier();
    }

    /**
     * Apply the attribute modifier.
     */
    private void applyModifier() {
        if (applied || !isActive()) {
            return;
        }

        if (entity instanceof HytalePlayerAdapter player) {
            // TODO: Implement attribute modification using Hytale API
            // player.addAttributeModifier(attribute, modifier, operation);
            applied = true;
        }
    }

    /**
     * Remove the attribute modifier.
     */
    private void removeModifier() {
        if (!applied) {
            return;
        }

        if (entity instanceof HytalePlayerAdapter player) {
            // TODO: Implement attribute removal using Hytale API
            // player.removeAttributeModifier(attribute, modifier, operation);
            applied = false;
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("attribute", attribute);
        json.addProperty("modifier", modifier);
        json.addProperty("operation", operation.name());
        json.addProperty("applied", applied);
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        super.fromJson(json);
        if (json.has("applied")) {
            this.applied = json.get("applied").getAsBoolean();
        }
    }

    public String getAttribute() {
        return attribute;
    }

    public double getModifier() {
        return modifier;
    }

    public AspectAbilitiesDataTypes.AttributeOperation getOperation() {
        return operation;
    }

    public boolean isApplied() {
        return applied;
    }

    public static AbilityFactory<AttributeModifierAbility> createFactory() {
        return new AbilityFactory<AttributeModifierAbility>(
            AspectAbilities.identifier("attribute_modifier"),
            new SerializableData()
                .add("attribute", SerializableDataTypes.STRING, "generic.max_health")
                .add("modifier", SerializableDataTypes.DOUBLE, 0.0)
                .add("operation", AspectAbilitiesDataTypes.ATTRIBUTE_OPERATION,
                     AspectAbilitiesDataTypes.AttributeOperation.ADD),
            data -> (type, entity) -> new AttributeModifierAbility(
                type, entity,
                data.get("attribute"),
                data.get("modifier"),
                data.get("operation")
            )
        ).allowCondition();
    }
}
