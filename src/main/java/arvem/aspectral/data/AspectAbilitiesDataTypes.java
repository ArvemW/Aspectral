package arvem.aspectral.data;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.AbilityTypeReference;
import arvem.aspectral.abilities.factory.ActionFactory;
import arvem.aspectral.abilities.factory.ConditionFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.util.Comparison;
import com.google.gson.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Data types specific to AspectAbilities.
 * These handle serialization of ability-specific concepts.
 */
public class AspectAbilitiesDataTypes {

    /**
     * Data type for ability type references.
     */
    public static final SerializableDataType<AbilityTypeReference> ABILITY_TYPE = SerializableDataType.wrap(
        AbilityTypeReference.class,
        SerializableDataTypes.STRING,
        AbilityTypeReference::getIdentifier,
        AbilityTypeReference::new
    );

    /**
     * Data type for entity conditions.
     */
    public static final SerializableDataType<Predicate<LivingEntity>> ENTITY_CONDITION =
        createConditionDataType("Entity condition");

    /**
     * Data type for lists of entity conditions.
     */
    public static final SerializableDataType<List<Predicate<LivingEntity>>> ENTITY_CONDITIONS =
        SerializableDataType.list(ENTITY_CONDITION);

    /**
     * Data type for entity actions.
     */
    public static final SerializableDataType<Consumer<LivingEntity>> ENTITY_ACTION =
        createActionDataType("Entity action");

    /**
     * Data type for lists of entity actions.
     */
    public static final SerializableDataType<List<Consumer<LivingEntity>>> ENTITY_ACTIONS =
        SerializableDataType.list(ENTITY_ACTION);

    /**
     * Data type for comparison operators.
     */
    public static final SerializableDataType<Comparison> COMPARISON = SerializableDataType.enumValue(Comparison.class);

    /**
     * Data type for attribute operations.
     */
    public static final SerializableDataType<AttributeOperation> ATTRIBUTE_OPERATION =
        SerializableDataType.enumValue(AttributeOperation.class);

    /**
     * Create a condition data type for a specific context.
     */
    @SuppressWarnings("unchecked")
    private static SerializableDataType<Predicate<LivingEntity>> createConditionDataType(String name) {
        return new SerializableDataType<>(
            (Class<Predicate<LivingEntity>>) (Class<?>) Predicate.class,
            (buf, predicate) -> {
                // Conditions are typically not sent over network, but we support it
                if (predicate instanceof ConditionFactory.Instance instance) {
                    instance.write(buf);
                }
            },
            buf -> {
                // Read condition type ID
                int idLen = buf.readInt();
                byte[] idBytes = new byte[idLen];
                buf.readBytes(idBytes);
                String typeId = new String(idBytes);

                ConditionFactory<LivingEntity> factory =
                    AspectAbilities.getInstance().getAbilityRegistry().getEntityCondition(typeId);
                if (factory != null) {
                    return factory.read(buf);
                }
                return entity -> true; // Default to always true
            },
            json -> {
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    String typeId = obj.get("type").getAsString();

                    ConditionFactory<LivingEntity> factory =
                        AspectAbilities.getInstance().getAbilityRegistry().getEntityCondition(typeId);
                    if (factory != null) {
                        return factory.read(obj);
                    }
                }
                throw new JsonSyntaxException(name + " requires a 'type' field");
            },
            predicate -> {
                if (predicate instanceof ConditionFactory.Instance instance) {
                    return instance.toJson();
                }
                return new JsonObject();
            }
        );
    }

    /**
     * Create an action data type for a specific context.
     */
    @SuppressWarnings("unchecked")
    private static SerializableDataType<Consumer<LivingEntity>> createActionDataType(String name) {
        return new SerializableDataType<>(
            (Class<Consumer<LivingEntity>>) (Class<?>) Consumer.class,
            (buf, consumer) -> {
                if (consumer instanceof ActionFactory.Instance instance) {
                    instance.write(buf);
                }
            },
            buf -> {
                int idLen = buf.readInt();
                byte[] idBytes = new byte[idLen];
                buf.readBytes(idBytes);
                String typeId = new String(idBytes);

                ActionFactory<LivingEntity> factory =
                    AspectAbilities.getInstance().getAbilityRegistry().getEntityAction(typeId);
                if (factory != null) {
                    return factory.read(buf);
                }
                return entity -> {}; // Default to no-op
            },
            json -> {
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    String typeId = obj.get("type").getAsString();

                    ActionFactory<LivingEntity> factory =
                        AspectAbilities.getInstance().getAbilityRegistry().getEntityAction(typeId);
                    if (factory != null) {
                        return factory.read(obj);
                    }
                }
                throw new JsonSyntaxException(name + " requires a 'type' field");
            },
            consumer -> {
                if (consumer instanceof ActionFactory.Instance instance) {
                    return instance.toJson();
                }
                return new JsonObject();
            }
        );
    }

    /**
     * Attribute operation types for modifying attributes.
     */
    public enum AttributeOperation {
        ADD,
        MULTIPLY_BASE,
        MULTIPLY_TOTAL
    }
}
