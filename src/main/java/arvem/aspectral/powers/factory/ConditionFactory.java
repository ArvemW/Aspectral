package arvem.aspectral.powers.factory;

import arvem.aspectral.data.SerializableData;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Factory for creating conditions from JSON definitions.
 * Conditions are predicates that test whether something is true for a given context.
 *
 * @param <T> The type of context this condition tests against
 */
public class ConditionFactory<T> implements Factory {

    private final String id;
    protected SerializableData data;
    protected Function<SerializableData.Instance, Predicate<T>> conditionConstructor;

    public ConditionFactory(String id, SerializableData data,
                            Function<SerializableData.Instance, Predicate<T>> conditionConstructor) {
        this.id = id;
        this.data = data;
        this.conditionConstructor = conditionConstructor;
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
     * An instance of this condition factory with bound data.
     */
    public class Instance implements Predicate<T> {

        private final SerializableData.Instance dataInstance;
        private final Predicate<T> condition;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
            this.condition = conditionConstructor.apply(data);
        }

        @Override
        public boolean test(T t) {
            return condition.test(t);
        }

        public void write(ByteBuf buf) {
            byte[] idBytes = id.getBytes();
            buf.writeInt(idBytes.length);
            buf.writeBytes(idBytes);
            data.write(buf, dataInstance);
        }

        public SerializableData.Instance getDataInstance() {
            return dataInstance;
        }

        public ConditionFactory<T> getFactory() {
            return ConditionFactory.this;
        }

        public JsonObject toJson() {
            JsonObject jsonObject = data.write(dataInstance);
            jsonObject.addProperty("type", id);
            return jsonObject;
        }
    }

    /**
     * Read a condition instance from JSON.
     */
    public Instance read(JsonObject json) {
        return new Instance(data.read(json));
    }

    /**
     * Read a condition instance from a network buffer.
     */
    public Instance read(ByteBuf buffer) {
        return new Instance(data.read(buffer));
    }
}


