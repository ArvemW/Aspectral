package arvem.aspectral.powers.factory;

import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Factory for creating powers from JSON definitions.
 * Each power type has a factory that knows how to create instances
 * from serialized data.
 *
 * @param <A> The type of power this factory creates
 */
public class PowerFactory<A extends Power> implements Factory {

    private final String id;
    protected SerializableData data;
    protected Function<SerializableData.Instance, BiFunction<PowerType<A>, LivingEntity, A>> factoryConstructor;

    private boolean hasConditions = false;

    public PowerFactory(String id, SerializableData data,
                          Function<SerializableData.Instance, BiFunction<PowerType<A>, LivingEntity, A>> factoryConstructor) {
        this.id = id;
        this.data = data;
        this.factoryConstructor = factoryConstructor;
    }

    /**
     * Allow this power to have conditions attached.
     */
    public PowerFactory<A> allowCondition() {
        if (!hasConditions) {
            hasConditions = true;
            data.add("condition", AspectPowersDataTypes.ENTITY_CONDITION, null);
        }
        return this;
    }

    @Override
    public String getSerializerId() {
        return id;
    }

    @Override
    public SerializableData getSerializableData() {
        return data;
    }

    /**
     * An instance of this factory with bound data.
     */
    public class Instance implements BiFunction<PowerType<A>, LivingEntity, A> {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        @Override
        public A apply(PowerType<A> powerType, LivingEntity entity) {
            BiFunction<PowerType<A>, LivingEntity, A> constructor = factoryConstructor.apply(dataInstance);
            A power = constructor.apply(powerType, entity);

            if (hasConditions && dataInstance.isPresent("condition")) {
                power.addCondition(dataInstance.get("condition"));
            }

            return power;
        }

        public void write(ByteBuf buf) {
            // Write the factory ID first
            byte[] idBytes = id.getBytes();
            buf.writeInt(idBytes.length);
            buf.writeBytes(idBytes);
            // Then write the data
            data.write(buf, dataInstance);
        }

        public SerializableData.Instance getDataInstance() {
            return dataInstance;
        }

        public PowerFactory<A> getFactory() {
            return PowerFactory.this;
        }

        public JsonObject toJson() {
            JsonObject jsonObject = data.write(dataInstance);
            jsonObject.addProperty("type", id);
            return jsonObject;
        }
    }

    /**
     * Read an instance from JSON.
     */
    public Instance read(JsonObject json) {
        return new Instance(data.read(json));
    }

    /**
     * Read an instance from a network buffer.
     */
    public Instance read(ByteBuf buffer) {
        return new Instance(data.read(buffer));
    }

    /**
     * Create an instance with default values.
     * Useful for command-based power granting.
     */
    public Instance createDefault() {
        return new Instance(data.createDefault());
    }
}


