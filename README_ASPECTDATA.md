# AspectData Library for Hytale

## Overview

**AspectData** is a data serialization library for Hytale, inspired by Calio (from the Origins mod for Minecraft).

⚠️ **Important Disclaimer**: This is **NOT** a direct port of Calio. It's a custom reimplementation that provides similar functionality but with its own design and implementation.

## What It Does

AspectData provides:
- JSON serialization/deserialization
- Network packet serialization
- Type-safe data handling
- Support for complex data structures (lists, maps, nested objects, enums)
- Identifier system for resource locations
- Registry integration (placeholder, needs Hytale integration)

## Quick Example

```java
// Define a data structure
SerializableData playerData = new SerializableData()
    .add("name", SerializableDataTypes.STRING)
    .add("level", SerializableDataTypes.INT)
    .add("active", SerializableDataTypes.BOOLEAN, true);

// Read from JSON
JsonObject json = /* ... */;
SerializableData.Instance instance = playerData.read(json);

// Access values
String name = instance.getString("name");
int level = instance.getInt("level");
```

## Files Created

**21 Java files** across 4 packages:
- `arvem.aspectral` - Main library class
- `arvem.aspectral.data` - Core serialization (7 files)
- `arvem.aspectral.util` - Utilities (6 files)  
- `arvem.aspectral.registry` - Registry system (5 files)

## Using AspectData for Origins

If you want to port Origins to Hytale:

### Option 1: Use AspectData (Faster)
- AspectData is ready to use now
- May have differences from real Calio
- Good for prototyping/learning

### Option 2: Port Real Calio (Authentic)
1. Get Calio source: https://github.com/apace100/calio
2. Copy the actual implementation
3. Replace Minecraft dependencies with Hytale equivalents
4. Test thoroughly

### Option 3: Hybrid Approach
1. Start with AspectData to learn the system
2. Later replace with real Calio port for accuracy
3. Best of both worlds

## Attribution

- **Inspired by**: Calio by apace100 (Origins mod)
- **Implementation**: Custom reimplementation for Hytale
- **Status**: Functional library, not an official port

## License Note

While AspectData is a custom implementation, if you use it for porting Origins:
- Credit the original Origins mod and Calio
- Follow Origins' license terms
- Mention this is inspired by, not a port of, Calio
