package arvem.aspectral.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.hypixel.hytale.protocol.io.PacketIO;
import io.netty.buffer.ByteBuf;

/**
 * Common serializable data types for AspectData.
 * Uses Hytale's ByteBuf (Netty) for network serialization.
 */
public class SerializableDataTypes {

    public static final SerializableDataType<Boolean> BOOLEAN = new SerializableDataType<>(
        Boolean.class,
        ByteBuf::writeBoolean,
        ByteBuf::readBoolean,
        JsonElement::getAsBoolean,
        JsonPrimitive::new
    );

    public static final SerializableDataType<Integer> INT = new SerializableDataType<>(
        Integer.class,
        ByteBuf::writeInt,
        ByteBuf::readInt,
        JsonElement::getAsInt,
        JsonPrimitive::new
    );

    public static final SerializableDataType<Long> LONG = new SerializableDataType<>(
        Long.class,
        ByteBuf::writeLong,
        ByteBuf::readLong,
        JsonElement::getAsLong,
        JsonPrimitive::new
    );

    public static final SerializableDataType<Float> FLOAT = new SerializableDataType<>(
        Float.class,
        ByteBuf::writeFloat,
        ByteBuf::readFloat,
        JsonElement::getAsFloat,
        JsonPrimitive::new
    );

    public static final SerializableDataType<Double> DOUBLE = new SerializableDataType<>(
        Double.class,
        ByteBuf::writeDouble,
        ByteBuf::readDouble,
        JsonElement::getAsDouble,
        JsonPrimitive::new
    );

    public static final SerializableDataType<String> STRING = new SerializableDataType<>(
        String.class,
        (buf, str) -> PacketIO.writeVarString(buf, str, 32767),
        buf -> PacketIO.readVarString(buf, buf.readerIndex()),
        JsonElement::getAsString,
        JsonPrimitive::new
    );
}
