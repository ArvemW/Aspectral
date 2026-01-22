package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataType;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.List;

/**
 * A power that modifies entity attributes.
 * Attributes are things like max health, movement speed, attack damage, etc.
 */
public class AttributePower extends Power {

    private final List<AttributeModifier> modifiers;

    public AttributePower(PowerType<?> type, LivingEntity entity,
                          List<AttributeModifier> modifiers) {
        super(type, entity);
        this.modifiers = modifiers;
    }

    @Override
    public void onGained() {
        applyModifiers();
    }

    @Override
    public void onLost() {
        removeModifiers();
    }

    @Override
    public void onAdded(boolean onSync) {
        if (!onSync) {
            applyModifiers();
        }
    }

    @Override
    public void onRemoved(boolean onSync) {
        if (!onSync) {
            removeModifiers();
        }
    }

    private void applyModifiers() {
        // This would use Hytale's attribute API
        // For now, placeholder implementation
        for (AttributeModifier modifier : modifiers) {
            // entity.getAttribute(modifier.attribute).addModifier(
            //     type.getIdentifier(), modifier.value, modifier.operation);
        }
    }

    private void removeModifiers() {
        for (AttributeModifier modifier : modifiers) {
            // entity.getAttribute(modifier.attribute).removeModifier(type.getIdentifier());
        }
    }

    /**
     * Represents a single attribute modification.
     */
    public static class AttributeModifier {
        public final String attribute;
        public final double value;
        public final AspectPowersDataTypes.AttributeOperation operation;

        public AttributeModifier(String attribute, double value,
                                  AspectPowersDataTypes.AttributeOperation operation) {
            this.attribute = attribute;
            this.value = value;
            this.operation = operation;
        }
    }

    public static final SerializableDataType<AttributeModifier> ATTRIBUTE_MODIFIER =
        SerializableDataType.compound(
            AttributeModifier.class,
            new SerializableData()
                .add("attribute", SerializableDataTypes.STRING)
                .add("value", SerializableDataTypes.DOUBLE)
                .add("operation", AspectPowersDataTypes.ATTRIBUTE_OPERATION,
                     AspectPowersDataTypes.AttributeOperation.ADD),
            instance -> new AttributeModifier(
                instance.get("attribute"),
                instance.get("value"),
                instance.get("operation")
            ),
            (data, modifier) -> {
                var instance = data.new Instance();
                instance.set("attribute", modifier.attribute);
                instance.set("value", modifier.value);
                instance.set("operation", modifier.operation);
                return instance;
            }
        );

    public static PowerFactory<AttributePower> createFactory() {
        return new PowerFactory<AttributePower>(
            AspectPowers.identifier("attribute"),
            new SerializableData()
                .add("modifiers", SerializableDataType.list(ATTRIBUTE_MODIFIER)),
            data -> (type, entity) -> new AttributePower(
                type, entity,
                data.get("modifiers")
            )
        ).allowCondition();
    }
}


