# 🚨 IMPORTANT: Use Hytale's Actual Systems

## The Problem

AspectData was created with **placeholder implementations** of Minecraft systems instead of using Hytale's actual APIs. This needs to be fixed!

## What Hytale Actually Provides

### ✅ Hytale Has These Systems Already:

1. **Asset Registry System**
   - `com.hypixel.hytale.assetstore.AssetRegistry` - Main registry
   - `com.hypixel.hytale.assetstore.AssetStore<K, T, M>` - Typed asset stores
   - Keys are generic (`<K>`) - can be Strings or custom types
   - **Use this instead of**: Custom `Registry`, `RegistryKey`, `RegistryEntry`

2. **Network Serialization**
   - `io.netty.buffer.ByteBuf` - Netty's buffer (industry standard)
   - `com.hypixel.hytale.protocol.io.PacketIO` - Hytale's packet utilities
   - `com.hypixel.hytale.protocol.Packet` - Base packet class
   - **Use this instead of**: Custom `PacketByteBuf`

3. **Plugin System**
   - `com.hypixel.hytale.common.plugin.PluginIdentifier` - For plugin IDs
   - Asset keys are generic (String, enums, custom types)
   - **Use this instead of**: Custom `Identifier` class

4. **Logging**
   - `com.hypixel.hytale.logger.HytaleLogger` - Already being used ✅

## What Needs To Change

### ❌ Remove These Custom Classes:
- `arvem.aspectral.data.Identifier` → Use String keys or `PluginIdentifier`
- `arvem.aspectral.data.PacketByteBuf` → Use `io.netty.buffer.ByteBuf`
- `arvem.aspectral.registry.*` (all 5 files) → Use Hytale's `AssetRegistry` / `AssetStore`
- `arvem.aspectral.data.EntityAttributeModifier` → Use Hytale's actual attribute system (when available)

### ✅ Keep These (They're Useful):
- `SerializableData` - Core data structure handling
- `SerializableDataType` - Type system
- `SerializableDataTypes` - Common types
- `DataException` - Error handling
- Utility classes in `arvem.aspectral.util.*`

## Correct Approach for AspectData

AspectData should be a **lightweight serialization library** that works **with** Hytale's systems:

```java
// WRONG - Creating our own identifier
Identifier id = new Identifier("namespace", "path");

// RIGHT - Use Hytale's systems
String assetKey = "my_asset_key";  // For simple cases
// OR use AssetStore with proper types for complex cases
```

```java
// WRONG - Custom packet buffer
PacketByteBuf buffer = new PacketByteBuf();

// RIGHT - Use Netty's ByteBuf
ByteBuf buffer = Unpooled.buffer();
// Use PacketIO utility methods for common operations
```

```java
// WRONG - Custom registry
Registry<MyType> registry = new Registry<>(key);

// RIGHT - Use Hytale's AssetStore
AssetStore<String, MyAsset, AssetMap<String, MyAsset>> store = 
    AssetRegistry.getAssetStore(MyAsset.class);
```

## Next Steps

1. **Update SerializableDataType** to use `ByteBuf` instead of `PacketByteBuf`
2. **Update SerializableDataTypes** to use String keys or allow generic keys
3. **Remove placeholder registry classes**
4. **Update documentation** to show how to use with Hytale's systems
5. **Create examples** showing integration with `AssetStore`

## Why This Matters

- ✅ **Compatibility**: Works with Hytale's actual systems
- ✅ **Performance**: Uses optimized Netty buffers
- ✅ **Future-proof**: Won't break when Hytale updates
- ✅ **Less code**: Don't maintain duplicates of Hytale's code
- ✅ **Community**: Other modders can understand and use it

## File Impact

**DELETE** (7 files):
- `src/main/java/arvem/aspectral/data/Identifier.java`
- `src/main/java/arvem/aspectral/data/PacketByteBuf.java`
- `src/main/java/arvem/aspectral/data/EntityAttributeModifier.java`
- `src/main/java/arvem/aspectral/registry/Registry.java`
- `src/main/java/arvem/aspectral/registry/RegistryKey.java`
- `src/main/java/arvem/aspectral/registry/RegistryEntry.java`
- `src/main/java/arvem/aspectral/registry/DynamicRegistryManager.java`
- `src/main/java/arvem/aspectral/registry/TagKey.java`

**UPDATE** (3 files):
- `SerializableDataType.java` - Use ByteBuf, remove registry methods
- `SerializableDataTypes.java` - Use String instead of Identifier
- `SerializableData.java` - Use ByteBuf

**KEEP** (9 files):
- All utility classes
- Core serialization logic
- Documentation (updated)

---

**Bottom Line**: AspectData should be a thin serialization layer that works **with** Hytale, not reimplementing Hytale's systems.
