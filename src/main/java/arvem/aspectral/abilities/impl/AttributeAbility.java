package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataType;
import arvem.aspectral.data.SerializableDataTypes;

import java.util.List;

/**
 * An ability that modifies entity attributes.
 * Attributes are things like max health, movement speed, attack damage, etc.
 */
public class AttributeAbility extends Ability {

    private final List<AttributeModifier> modifiers;

    public AttributeAbility(AbilityType<?> type, LivingEntity entity,
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
        public final AspectAbilitiesDataTypes.AttributeOperation operation;

        public AttributeModifier(String attribute, double value,
                                  AspectAbilitiesDataTypes.AttributeOperation operation) {
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
                .add("operation", AspectAbilitiesDataTypes.ATTRIBUTE_OPERATION,
                     AspectAbilitiesDataTypes.AttributeOperation.ADD),
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

    public static AbilityFactory<AttributeAbility> createFactory() {
        return new AbilityFactory<AttributeAbility>(
            AspectAbilities.identifier("attribute"),
            new SerializableData()
                .add("modifiers", SerializableDataType.list(ATTRIBUTE_MODIFIER)),
            data -> (type, entity) -> new AttributeAbility(
                type, entity,
                data.get("modifiers")
            )
        ).allowCondition();
    }
}
