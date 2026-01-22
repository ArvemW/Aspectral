package arvem.aspectral.powers.factory;

import arvem.aspectral.data.SerializableData;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Factory for creating actions from JSON definitions.
 * Actions are operations that perform some effect on a given context.
 *
 * @param <T> The type of context this action operates on
 */
public class ActionFactory<T> implements Factory {

    private final String id;
    protected SerializableData data;
    protected Function<SerializableData.Instance, Consumer<T>> actionConstructor;

    public ActionFactory(String id, SerializableData data,
                         Function<SerializableData.Instance, Consumer<T>> actionConstructor) {
        this.id = id;
        this.data = data;
        this.actionConstructor = actionConstructor;
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
     * An instance of this action factory with bound data.
     */
    public class Instance implements Consumer<T> {

        private final SerializableData.Instance dataInstance;
        private final Consumer<T> action;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
            this.action = actionConstructor.apply(data);
        }

        @Override
        public void accept(T t) {
            action.accept(t);
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

        public ActionFactory<T> getFactory() {
            return ActionFactory.this;
        }

        public JsonObject toJson() {
            JsonObject jsonObject = data.write(dataInstance);
            jsonObject.addProperty("type", id);
            return jsonObject;
        }
    }

    /**
     * Read an action instance from JSON.
     */
    public Instance read(JsonObject json) {
        return new Instance(data.read(json));
    }

    /**
     * Read an action instance from a network buffer.
     */
    public Instance read(ByteBuf buffer) {
        return new Instance(data.read(buffer));
    }
}


