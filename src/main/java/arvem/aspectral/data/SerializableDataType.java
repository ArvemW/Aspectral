package arvem.aspectral.data;

import arvem.aspectral.util.ArgumentWrapper;
import arvem.aspectral.util.ClassUtil;
import arvem.aspectral.util.DynamicIdentifier;
import arvem.aspectral.util.FilterableWeightedList;
import arvem.aspectral.util.JsonHelper;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Defines a serializable data type for AspectData.
 * Handles serialization to/from JSON and network buffers using Hytale's systems.
 * <p>
 * Unlike Minecraft's Calio, this uses:
 * - Netty's {@link ByteBuf} instead of PacketByteBuf
 * - String keys instead of Identifier/RegistryKey
 * - Simple map-based registries instead of Minecraft's registry system
 * 
 * @param <T> The type of data this SerializableDataType handles
 */
public class SerializableDataType<T> {

    private final Class<T> dataClass;
    private final BiConsumer<ByteBuf, T> send;
    private final Function<ByteBuf, T> receive;
    private final Function<JsonElement, T> read;
    private final Function<T, JsonElement> write;

    /**
     * @deprecated Use the constructor with write function for full serialization support
     */
    @Deprecated
    public SerializableDataType(Class<T> dataClass,
                                BiConsumer<ByteBuf, T> send,
                                Function<ByteBuf, T> receive,
                                Function<JsonElement, T> read) {
        this(dataClass, send, receive, read, (obj) -> {
            throw new UnsupportedOperationException(
                "Could not write serializable data type of class " + dataClass.getName() + 
                " as it does not have a write function set."
            );
        });
    }

    public SerializableDataType(Class<T> dataClass,
                                BiConsumer<ByteBuf, T> send,
                                Function<ByteBuf, T> receive,
                                Function<JsonElement, T> read,
                                Function<T, JsonElement> write) {
        this.dataClass = dataClass;
        this.send = send;
        this.receive = receive;
        this.read = read;
        this.write = write;
    }

    public void send(ByteBuf buffer, Object value) {
        send.accept(buffer, cast(value));
    }

    public T receive(ByteBuf buffer) {
        return receive.apply(buffer);
    }

    public T read(JsonElement jsonElement) {
        return read.apply(jsonElement);
    }

    public JsonElement writeUnsafely(Object value) throws Exception {
        try {
            return write.apply(cast(value));
        } catch (ClassCastException e) {
            throw new Exception(e);
        }
    }

    public JsonElement write(T value) {
        return write.apply(value);
    }

    public T cast(Object data) {
        return dataClass.cast(data);
    }

    public Class<T> getDataClass() {
        return dataClass;
    }

    // ============================================
    // Factory Methods for Common Data Types
    // ============================================

    /**
     * Creates a SerializableDataType for a List of elements.
     * 
     * @param singleDataType The data type for individual elements
     * @param <T> The element type
     * @return A SerializableDataType for List&lt;T&gt;
     */
    public static <T> SerializableDataType<List<T>> list(SerializableDataType<T> singleDataType) {
        return new SerializableDataType<>(ClassUtil.castClass(List.class), (buf, list) -> {
            buf.writeInt(list.size());
            int i = 0;
            for (T elem : list) {
                try {
                    singleDataType.send(buf, elem);
                } catch (DataException e) {
                    throw e.prepend("[" + i + "]");
                } catch (Exception e) {
                    throw new DataException(DataException.Phase.WRITING, "[" + i + "]", e);
                }
                i++;
            }
        }, (buf) -> {
            int count = buf.readInt();
            LinkedList<T> list = new LinkedList<>();
            for (int i = 0; i < count; i++) {
                try {
                    list.add(singleDataType.receive(buf));
                } catch (DataException e) {
                    throw e.prepend("[" + i + "]");
                } catch (Exception e) {
                    throw new DataException(DataException.Phase.RECEIVING, "[" + i + "]", e);
                }
            }
            return list;
        }, (json) -> {
            LinkedList<T> list = new LinkedList<>();
            if (json.isJsonArray()) {
                int i = 0;
                for (JsonElement je : json.getAsJsonArray()) {
                    try {
                        list.add(singleDataType.read(je));
                    } catch (DataException e) {
                        throw e.prepend("[" + i + "]");
                    } catch (Exception e) {
                        throw new DataException(DataException.Phase.READING, "[" + i + "]", e);
                    }
                    i++;
                }
            } else {
                list.add(singleDataType.read(json));
            }
            return list;
        }, (list) -> {
            JsonArray array = new JsonArray();
            for (T value : list) {
                array.add(singleDataType.write(value));
            }
            return array;
        });
    }

    /**
     * Creates a SerializableDataType for a weighted list with filtering capabilities.
     * 
     * @param singleDataType The data type for individual elements
     * @param <T> The element type
     * @return A SerializableDataType for FilterableWeightedList&lt;T&gt;
     */
    public static <T> SerializableDataType<FilterableWeightedList<T>> weightedList(SerializableDataType<T> singleDataType) {
        return new SerializableDataType<>(ClassUtil.castClass(FilterableWeightedList.class), (buf, list) -> {
            buf.writeInt(list.size());
            AtomicInteger i = new AtomicInteger();
            list.entryStream().forEach(entry -> {
                try {
                    singleDataType.send(buf, entry.getElement());
                    buf.writeInt(entry.getWeight());
                } catch (DataException e) {
                    throw e.prepend("[" + i.get() + "]");
                } catch (Exception e) {
                    throw new DataException(DataException.Phase.WRITING, "[" + i.get() + "]", e);
                }
                i.getAndIncrement();
            });
        }, (buf) -> {
            int count = buf.readInt();
            FilterableWeightedList<T> list = new FilterableWeightedList<>();
            for (int i = 0; i < count; i++) {
                try {
                    T t = singleDataType.receive(buf);
                    int weight = buf.readInt();
                    list.add(t, weight);
                } catch (DataException e) {
                    throw e.prepend("[" + i + "]");
                } catch (Exception e) {
                    throw new DataException(DataException.Phase.RECEIVING, "[" + i + "]", e);
                }
            }
            return list;
        }, (json) -> {
            FilterableWeightedList<T> list = new FilterableWeightedList<>();
            if (json.isJsonArray()) {
                int i = 0;
                for (JsonElement je : json.getAsJsonArray()) {
                    try {
                        JsonObject weightedObj = je.getAsJsonObject();
                        T elem = singleDataType.read(weightedObj.get("element"));
                        int weight = JsonHelper.getInt(weightedObj, "weight");
                        list.add(elem, weight);
                    } catch (DataException e) {
                        throw e.prepend("[" + i + "]");
                    } catch (Exception e) {
                        throw new DataException(DataException.Phase.READING, "[" + i + "]", e);
                    }
                    i++;
                }
            }
            return list;
        }, (list) -> {
            JsonArray array = new JsonArray();
            for (FilterableWeightedList.Entry<T> value : list.getEntries()) {
                JsonObject listObject = new JsonObject();
                listObject.add("element", singleDataType.write(value.getElement()));
                listObject.addProperty("weight", value.getWeight());
                array.add(listObject);
            }
            return array;
        });
    }

    /**
     * Creates a SerializableDataType that looks up values from a simple map-based registry.
     * Uses string keys compatible with Hytale's asset system.
     * 
     * @param dataClass The class of the registry entries
     * @param registry A map serving as the registry (key -> value)
     * @param <T> The registry entry type
     * @return A SerializableDataType for registry lookups
     */
    public static <T> SerializableDataType<T> registry(Class<T> dataClass, Map<String, T> registry) {
        return registry(dataClass, registry, false);
    }

    /**
     * Creates a SerializableDataType that looks up values from a simple map-based registry.
     * 
     * @param dataClass The class of the registry entries
     * @param registry A map serving as the registry (key -> value)
     * @param defaultNamespace The default namespace to use when key has no namespace
     * @param <T> The registry entry type
     * @return A SerializableDataType for registry lookups
     */
    public static <T> SerializableDataType<T> registry(Class<T> dataClass, Map<String, T> registry, String defaultNamespace) {
        return registry(dataClass, registry, defaultNamespace, false);
    }

    /**
     * Creates a SerializableDataType that looks up values from a simple map-based registry.
     * 
     * @param dataClass The class of the registry entries
     * @param registry A map serving as the registry (key -> value)
     * @param showPossibleValues Whether to show possible values in error messages
     * @param <T> The registry entry type
     * @return A SerializableDataType for registry lookups
     */
    public static <T> SerializableDataType<T> registry(Class<T> dataClass, Map<String, T> registry, boolean showPossibleValues) {
        return registry(dataClass, registry, DynamicIdentifier.DEFAULT_NAMESPACE, showPossibleValues);
    }

    /**
     * Creates a SerializableDataType that looks up values from a simple map-based registry.
     * 
     * @param dataClass The class of the registry entries
     * @param registry A map serving as the registry (key -> value)
     * @param defaultNamespace The default namespace for keys without namespace
     * @param showPossibleValues Whether to show possible values in error messages
     * @param <T> The registry entry type
     * @return A SerializableDataType for registry lookups
     */
    public static <T> SerializableDataType<T> registry(Class<T> dataClass, Map<String, T> registry, 
                                                       String defaultNamespace, boolean showPossibleValues) {
        return registry(dataClass, registry, defaultNamespace, (reg, key) -> {
            String possibleValues = showPossibleValues 
                ? " Expected value to be any of: " + String.join(", ", reg.keySet()) 
                : "";
            return new RuntimeException("Type \"" + key + "\" is not registered." + possibleValues);
        });
    }

    /**
     * Creates a SerializableDataType that looks up values from a simple map-based registry.
     * 
     * @param dataClass The class of the registry entries
     * @param registry A map serving as the registry (key -> value)
     * @param exception Function to create exception for missing entries
     * @param <T> The registry entry type
     * @return A SerializableDataType for registry lookups
     */
    public static <T> SerializableDataType<T> registry(Class<T> dataClass, Map<String, T> registry,
                                                       BiFunction<Map<String, T>, String, RuntimeException> exception) {
        return registry(dataClass, registry, DynamicIdentifier.DEFAULT_NAMESPACE, exception);
    }

    /**
     * Creates a SerializableDataType that looks up values from a simple map-based registry.
     * 
     * @param dataClass The class of the registry entries
     * @param registry A map serving as the registry (key -> value)
     * @param defaultNamespace The default namespace for keys without namespace
     * @param exception Function to create exception for missing entries
     * @param <T> The registry entry type
     * @return A SerializableDataType for registry lookups
     */
    public static <T> SerializableDataType<T> registry(Class<T> dataClass, Map<String, T> registry,
                                                       String defaultNamespace,
                                                       BiFunction<Map<String, T>, String, RuntimeException> exception) {
        // Create inverse lookup map
        Map<T, String> inverseRegistry = new HashMap<>();
        registry.forEach((key, value) -> inverseRegistry.put(value, key));

        return wrap(
            dataClass,
            SerializableDataTypes.STRING,
            t -> Objects.requireNonNull(inverseRegistry.get(t), "Value not found in registry"),
            keyString -> {
                String key = DynamicIdentifier.of(keyString, defaultNamespace);
                T value = registry.get(key);
                if (value == null) {
                    throw exception.apply(registry, key);
                }
                return value;
            }
        );
    }

    /**
     * Creates a SerializableDataType for compound data structures.
     * 
     * @param dataClass The class of the compound type
     * @param data The SerializableData definition
     * @param toInstance Function to create instance from data
     * @param toData Function to convert instance back to data
     * @param <T> The compound type
     * @return A SerializableDataType for the compound structure
     */
    public static <T> SerializableDataType<T> compound(Class<T> dataClass, SerializableData data, 
                                                       Function<SerializableData.Instance, T> toInstance, 
                                                       BiFunction<SerializableData, T, SerializableData.Instance> toData) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> data.write(buf, toData.apply(data, t)),
            (buf) -> toInstance.apply(data.read(buf)),
            (json) -> toInstance.apply(data.read(json.getAsJsonObject())),
            (t) -> data.write(toData.apply(data, t)));
    }

    /**
     * Creates a SerializableDataType for enum values.
     * 
     * @param dataClass The enum class
     * @param <T> The enum type
     * @return A SerializableDataType for the enum
     */
    public static <T extends Enum<T>> SerializableDataType<T> enumValue(Class<T> dataClass) {
        return enumValue(dataClass, null);
    }

    /**
     * Creates a SerializableDataType for enum values with additional name mappings.
     * 
     * @param dataClass The enum class
     * @param additionalMap Additional string-to-enum mappings (for aliases)
     * @param <T> The enum type
     * @return A SerializableDataType for the enum
     */
    public static <T extends Enum<T>> SerializableDataType<T> enumValue(Class<T> dataClass, HashMap<String, T> additionalMap) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> buf.writeInt(t.ordinal()),
            (buf) -> dataClass.getEnumConstants()[buf.readInt()],
            (json) -> {
                if (json.isJsonPrimitive()) {
                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                    if (primitive.isNumber()) {
                        int enumOrdinal = primitive.getAsInt();
                        T[] enumValues = dataClass.getEnumConstants();
                        if (enumOrdinal < 0 || enumOrdinal >= enumValues.length) {
                            throw new JsonSyntaxException("Expected to be in the range of 0 - " + (enumValues.length - 1));
                        }
                        return enumValues[enumOrdinal];
                    } else if (primitive.isString()) {
                        String enumName = primitive.getAsString();
                        try {
                            return Enum.valueOf(dataClass, enumName);
                        } catch (IllegalArgumentException e0) {
                            try {
                                return Enum.valueOf(dataClass, enumName.toUpperCase(Locale.ROOT));
                            } catch (IllegalArgumentException e1) {
                                if (additionalMap != null && additionalMap.containsKey(enumName)) {
                                    return additionalMap.get(enumName);
                                }
                                T[] enumValues = dataClass.getEnumConstants();
                                StringBuilder stringOf = new StringBuilder(enumValues[0].name() + ", " + enumValues[0].name().toLowerCase(Locale.ROOT));
                                for (int i = 1; i < enumValues.length; i++) {
                                    stringOf.append(", ").append(enumValues[i].name()).append(", ").append(enumValues[i].name().toLowerCase(Locale.ROOT));
                                }
                                throw new JsonSyntaxException("Expected value to be a string of: " + stringOf);
                            }
                        }
                    }
                }
                throw new JsonSyntaxException("Expected value to be either an integer or a string.");
            },
            (t) -> new JsonPrimitive(t.name()));
    }

    /**
     * Creates a SerializableDataType for a Map with String keys.
     * 
     * @param valueDataType The data type for map values
     * @param <V> The value type
     * @return A SerializableDataType for Map&lt;String, V&gt;
     */
    public static <V> SerializableDataType<Map<String, V>> map(SerializableDataType<V> valueDataType) {
        return new SerializableDataType<>(
            ClassUtil.castClass(Map.class),
            (buffer, map) -> {
                buffer.writeInt(map.size());
                map.forEach((key, value) -> {
                    writeString(buffer, key);
                    valueDataType.send(buffer, value);
                });
            },
            buffer -> {
                int size = buffer.readInt();
                Map<String, V> map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    String key = readString(buffer);
                    V value = valueDataType.receive(buffer);
                    map.put(key, value);
                }
                return map;
            },
            jsonElement -> {
                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("Expected a JSON object.");
                }

                Map<String, V> map = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    map.put(entry.getKey(), valueDataType.read(entry.getValue()));
                }

                return map;
            },
            map -> {
                JsonObject jsonObject = new JsonObject();
                map.forEach((k, v) -> jsonObject.add(k, valueDataType.write(v)));
                return jsonObject;
            }
        );
    }

    /**
     * Creates a SerializableDataType for a bidirectional map lookup.
     *
     * @param dataClass The class of mapped values
     * @param map A Map for name-to-value lookup (inverse lookup is computed automatically)
     * @param <T> The mapped type
     * @return A SerializableDataType for the mapped type
     */
    public static <T> SerializableDataType<T> mapped(Class<T> dataClass, Map<String, T> map) {
        // Create inverse lookup map
        Map<T, String> inverseMap = new HashMap<>();
        map.forEach((key, value) -> inverseMap.put(value, key));

        return new SerializableDataType<>(dataClass,
            (buf, t) -> writeString(buf, inverseMap.get(t)),
            (buf) -> map.get(readString(buf)),
            (json) -> {
                if (json.isJsonPrimitive()) {
                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                    if (primitive.isString()) {
                        String name = primitive.getAsString();
                        if (map.containsKey(name)) {
                            return map.get(name);
                        }
                        throw new JsonSyntaxException("Expected value to be a string of: " + 
                            map.keySet().stream().reduce((s0, s1) -> s0 + ", " + s1).orElse(""));
                    }
                }
                throw new JsonSyntaxException("Expected value to be a string.");
            },
            (t) -> new JsonPrimitive(inverseMap.get(t)));
    }

    /**
     * Creates a SerializableDataType by wrapping another with conversion functions.
     * 
     * @param dataClass The target class
     * @param base The base SerializableDataType
     * @param toFunction Conversion from T to U
     * @param fromFunction Conversion from U to T
     * @param <T> The target type
     * @param <U> The base type
     * @return A wrapped SerializableDataType
     */
    public static <T, U> SerializableDataType<T> wrap(Class<T> dataClass, SerializableDataType<U> base, 
                                                      Function<T, U> toFunction, Function<U, T> fromFunction) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> base.send(buf, toFunction.apply(t)),
            (buf) -> fromFunction.apply(base.receive(buf)),
            (json) -> fromFunction.apply(base.read(json)),
            (t) -> base.write(toFunction.apply(t)));
    }

    /**
     * Creates a SerializableDataType for an EnumSet.
     * 
     * @param enumClass The enum class
     * @param enumDataType The SerializableDataType for individual enum values
     * @param <T> The enum type
     * @return A SerializableDataType for EnumSet&lt;T&gt;
     */
    public static <T extends Enum<T>> SerializableDataType<EnumSet<T>> enumSet(Class<T> enumClass, SerializableDataType<T> enumDataType) {
        return new SerializableDataType<>(ClassUtil.castClass(EnumSet.class),
            (buf, set) -> {
                buf.writeInt(set.size());
                set.forEach(t -> buf.writeInt(t.ordinal()));
            },
            (buf) -> {
                int size = buf.readInt();
                EnumSet<T> set = EnumSet.noneOf(enumClass);
                T[] allValues = enumClass.getEnumConstants();
                for (int i = 0; i < size; i++) {
                    int ordinal = buf.readInt();
                    set.add(allValues[ordinal]);
                }
                return set;
            },
            (json) -> {
                EnumSet<T> set = EnumSet.noneOf(enumClass);
                if (json.isJsonPrimitive()) {
                    T t = enumDataType.read(json);
                    set.add(t);
                } else if (json.isJsonArray()) {
                    JsonArray array = json.getAsJsonArray();
                    for (JsonElement jsonElement : array) {
                        T t = enumDataType.read(jsonElement);
                        set.add(t);
                    }
                } else {
                    throw new RuntimeException("Expected enum set to be either an array or a primitive.");
                }
                return set;
            },
            (set) -> {
                JsonArray array = new JsonArray();
                for (T value : set) {
                    array.add(enumDataType.write(value));
                }
                return array;
            });
    }

    /**
     * Creates a SerializableDataType for bounded numeric values.
     * 
     * @param numberDataType The base number data type
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @param read Function to apply bounds checking
     * @param <T> The number type
     * @return A bounded SerializableDataType
     */
    public static <T extends Number> SerializableDataType<T> boundNumber(SerializableDataType<T> numberDataType, 
                                                                         T min, T max, 
                                                                         Function<T, BiFunction<T, T, T>> read) {
        return new SerializableDataType<>(
            numberDataType.dataClass,
            numberDataType.send,
            numberDataType.receive,
            jsonElement -> read.apply(numberDataType.read(jsonElement)).apply(min, max),
            numberDataType.write
        );
    }

    /**
     * Creates a SerializableDataType for command argument types.
     * Uses Hytale's command system when available.
     * 
     * @param parser A function that parses the string into the target type
     * @param <T> The argument type
     * @return A SerializableDataType for ArgumentWrapper&lt;T&gt;
     */
    public static <T> SerializableDataType<ArgumentWrapper<T>> argumentType(Function<String, T> parser) {
        return wrap(ClassUtil.castClass(ArgumentWrapper.class), SerializableDataTypes.STRING,
            ArgumentWrapper::rawArgument,
            str -> {
                try {
                    T t = parser.apply(str);
                    return new ArgumentWrapper<>(t, str);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse argument: " + e.getMessage());
                }
            });
    }

    // ============================================
    // Helper Methods for ByteBuf String Operations
    // ============================================

    /**
     * Writes a string to the ByteBuf with length prefix.
     */
    private static void writeString(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    /**
     * Reads a string from the ByteBuf with length prefix.
     */
    private static String readString(ByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}