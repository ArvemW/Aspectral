package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.HytalePlayerAdapter;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;
import com.google.gson.JsonObject;

/**
 * Attribute modifier power - applies persistent attribute modifications.
 * Supports both JSON and code-defined construction.
 */
public class AttributeModifierPower extends Power {

    private final String attribute;
    private final double modifier;
    private final AspectPowersDataTypes.AttributeOperation operation;

    private boolean applied = false;

    /**
     * Create from PowerType and entity with parameters.
     */
    public AttributeModifierPower(PowerType<?> type, LivingEntity entity,
                                  String attribute, double modifier,
                                  AspectPowersDataTypes.AttributeOperation operation) {
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

        if (entity instanceof HytalePlayerAdapter adapter) {
            try {
                var ref = adapter.getRef();
                var store = adapter.getStore();

                // Get the EntityStatMap component
                var statMap = store.getComponent(ref,
                    com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());

                if (statMap != null) {
                    // Get the stat index from attribute name
                    var assetMap = com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType.getAssetMap();
                    int statIndex = assetMap.getIndex(attribute);

                    if (statIndex >= 0) {
                        // Create modifier based on operation
                        var calculationType = switch (operation) {
                            case ADD -> com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier.CalculationType.ADDITIVE;
                            case MULTIPLY_BASE, MULTIPLY_TOTAL -> com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier.CalculationType.MULTIPLICATIVE;
                        };

                        var modifierKey = "aspectral_" + getType().getIdentifier();
                        var staticModifier = new com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier(
                            com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier.ModifierTarget.MAX,
                            calculationType,
                            (float) modifier
                        );

                        // Apply the modifier
                        statMap.putModifier(statIndex, modifierKey, staticModifier);

                        AspectPowers.getLogger().atInfo().log(
                            "Applied attribute modifier: %s %.2f (%s) to player %s",
                            attribute, modifier, operation.name(), adapter.getUsername());

                        applied = true;
                    } else {
                        AspectPowers.getLogger().atWarning().log(
                            "Unknown entity stat: %s", attribute);
                    }
                } else {
                    AspectPowers.getLogger().atWarning().log(
                        "Could not find EntityStatMap component for player %s",
                        adapter.getUsername());
                }

            } catch (Exception e) {
                AspectPowers.getLogger().atWarning().log(
                    "Failed to apply attribute modifier: %s", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the attribute modifier.
     */
    private void removeModifier() {
        if (!applied) {
            return;
        }

        if (entity instanceof HytalePlayerAdapter adapter) {
            try {
                var ref = adapter.getRef();
                var store = adapter.getStore();

                // Get the EntityStatMap component
                var statMap = store.getComponent(ref,
                    com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());

                if (statMap != null) {
                    // Get the stat index
                    var assetMap = com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType.getAssetMap();
                    int statIndex = assetMap.getIndex(attribute);

                    if (statIndex >= 0) {
                        // Remove the modifier
                        var modifierKey = "aspectral_" + getType().getIdentifier();
                        statMap.removeModifier(statIndex, modifierKey);

                        AspectPowers.getLogger().atInfo().log(
                            "Removed attribute modifier: %s from player %s",
                            attribute, adapter.getUsername());
                    }
                }

                applied = false;

            } catch (Exception e) {
                AspectPowers.getLogger().atWarning().log(
                    "Failed to remove attribute modifier: %s", e.getMessage());
                e.printStackTrace();
            }
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

    public AspectPowersDataTypes.AttributeOperation getOperation() {
        return operation;
    }

    public boolean isApplied() {
        return applied;
    }

    public static PowerFactory<AttributeModifierPower> createFactory() {
        return new PowerFactory<AttributeModifierPower>(
            AspectPowers.identifier("attribute_modifier"),
            new SerializableData()
                .add("attribute", SerializableDataTypes.STRING, "generic.max_health")
                .add("modifier", SerializableDataTypes.DOUBLE, 0.0)
                .add("operation", AspectPowersDataTypes.ATTRIBUTE_OPERATION,
                     AspectPowersDataTypes.AttributeOperation.ADD),
            data -> (type, entity) -> new AttributeModifierPower(
                type, entity,
                data.get("attribute"),
                data.get("modifier"),
                data.get("operation")
            )
        ).allowCondition();
    }
}


