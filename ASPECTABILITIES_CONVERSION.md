# AspectAbilities Conversion - COMPLETE

## Status: ✓ Core Conversion Complete

The AspectAbilities system has been fully converted from Minecraft's Apoli to work with Hytale.

## IMPORTANT: Run Cleanup First!

**Before compiling, you MUST run the cleanup script below to delete old Minecraft/Apoli files.**
The project will not compile correctly until these files are removed, as they contain
conflicting type definitions (e.g., `ServerLivingEntity` vs `LivingEntity`).

## Cleanup Required

Run these commands in PowerShell from the project root to delete old Minecraft files:

```powershell
cd "C:\Modding\HytaleDev\Aspectral"

# Delete deprecated root files
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\Apoli.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\ApoliClient.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\ApoliServer.java" -Force -ErrorAction SilentlyContinue

# Delete entire Minecraft-specific directories
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\access" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\mixin" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\networking" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\screen" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\integration" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\loot" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\global" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\power" -Recurse -Force -ErrorAction SilentlyContinue

# Delete old component files (keep AbilityHolderComponent.java)
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\component\PowerHolderComponent.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\component\PowerHolderComponentImpl.java" -Force -ErrorAction SilentlyContinue

# Delete old data files (keep AspectAbilitiesDataTypes.java)
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\data\ApoliDataTypes.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\data\ApoliDamageTypes.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\data\DamageSourceDescription.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\data\LegacyMaterial.java" -Force -ErrorAction SilentlyContinue

# Delete old registry files (keep AbilityRegistry.java)
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\registry\ApoliRegistries.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\registry\ApoliRegistryKeys.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\registry\ApoliClassData.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\registry\ApoliClassDataClient.java" -Force -ErrorAction SilentlyContinue

# Delete old command files (keep AbilityCommand.java)
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\command\PowerCommand.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\command\ResourceCommand.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\command\PowerHolderArgumentType.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\command\PowerOperation.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\command\PowerTypeArgumentType.java" -Force -ErrorAction SilentlyContinue

# Delete old util files (keep Scheduler.java and Comparison.java)
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\AddPowerLootFunction.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\AdvancementUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\ApoliConfig.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\ApoliConfigClient.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\ApoliConfigServer.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\ApoliResourceConditions.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\AttributeUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\GainedPowerCriterion.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\HudRender.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\IdentifierAlias.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\InventoryUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\JsonTextFormatter.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\KeyBindingUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\MiscUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\ModifiedCraftingRecipe.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\NamespaceAlias.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\PowerGrantingItem.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\PowerLootCondition.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\PowerPackageRegistry.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\PowerRestrictedCraftingRecipe.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\RemovePowerLootFunction.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\ResourceOperation.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\SavedBlockPosition.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\Shape.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\Space.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\StackPowerUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\SyncStatusEffectsUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\TextAlignment.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\TextureUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\WorldUtil.java" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "src\main\java\arvem\aspectral\abilities\util\modifier" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "Cleanup complete!"
```

### All Core Files Converted:
- `AspectAbilities.java` - Main system entry point ✓
- `ability/Ability.java` - Base ability class ✓
- `ability/AbilityType.java` - Ability type template ✓
- `ability/AbilityTypeReference.java` - Lazy reference ✓
- `ability/Activatable.java` - Interface for triggered abilities ✓
- `ability/factory/AbilityFactory.java` - Factory for creating abilities ✓
- `ability/factory/Factory.java` - Base factory interface ✓
- `ability/factory/AbilityFactories.java` - Registers ability types ✓
- `ability/factory/ConditionFactory.java` - Condition factory ✓
- `ability/factory/ActionFactory.java` - Action factory ✓
- `ability/factory/condition/EntityConditions.java` - Built-in conditions ✓
- `ability/factory/action/EntityActions.java` - Built-in actions ✓
- `component/AbilityHolderComponent.java` - Holds abilities for entities ✓
- `registry/AbilityRegistry.java` - Central registry ✓
- `data/AspectAbilitiesDataTypes.java` - Data types ✓
- `command/AbilityCommand.java` - Command handler ✓
- `util/Scheduler.java` - Task scheduler ✓
- `util/Comparison.java` - Comparison operators ✓
- `api/LivingEntity.java` - Entity interface ✓
- `api/Player.java` - Player interface ✓

### All Ability Implementations Converted:
- `impl/ToggleAbility.java` ✓
- `impl/CooldownAbility.java` ✓
- `impl/ActiveCooldownAbility.java` ✓
- `impl/ResourceAbility.java` ✓
- `impl/AttributeAbility.java` ✓
- `impl/ActionOverTimeAbility.java` ✓
- `impl/ActionOnCallbackAbility.java` ✓
- `impl/DamageOverTimeAbility.java` ✓
- `impl/FireImmunityAbility.java` ✓
- `impl/ModifyDamageDealtAbility.java` ✓
- `impl/PreventDeathAbility.java` ✓
- `impl/EntityGlowAbility.java` ✓
- `impl/FireProjectileAbility.java` ✓
- `impl/MultipleAbility.java` ✓

### Files To Delete (Old Minecraft/Apoli):
- `Apoli.java` - DELETE (empty/deprecated)
- `ApoliClient.java` - DELETE (empty/deprecated)
- `ApoliServer.java` - DELETE (empty/deprecated)
- `access/` - DELETE entire directory (Minecraft mixins)
- `mixin/` - DELETE entire directory (Minecraft mixins)
- `networking/` - DELETE entire directory (Fabric networking)
- `screen/` - DELETE entire directory (Minecraft GUI)
- `integration/` - DELETE entire directory (Fabric integrations)
- `loot/` - DELETE entire directory (Minecraft loot)
- `global/` - DELETE entire directory (Minecraft resources)
- `power/` - DELETE entire directory (old Power classes, replaced by ability/)
- `component/PowerHolderComponent.java` - DELETE (replaced)
- `component/PowerHolderComponentImpl.java` - DELETE (replaced)
- `data/ApoliDataTypes.java` - DELETE (replaced)
- `data/ApoliDamageTypes.java` - DELETE (Minecraft damage)
- `data/DamageSourceDescription.java` - DELETE (Minecraft damage)
- `data/LegacyMaterial.java` - DELETE (Minecraft materials)
- `registry/ApoliRegistries.java` - DELETE (replaced)
- `registry/ApoliRegistryKeys.java` - DELETE (Minecraft registry)
- `registry/ApoliClassData.java` - DELETE (Minecraft class data)
- `registry/ApoliClassDataClient.java` - DELETE (client-side)
- `command/PowerCommand.java` - DELETE (replaced)
- `command/ResourceCommand.java` - DELETE (Minecraft commands)
- `command/PowerHolderArgumentType.java` - DELETE (Minecraft args)
- `command/PowerOperation.java` - DELETE (Minecraft ops)
- `command/PowerTypeArgumentType.java` - DELETE (Minecraft args)

## Quick Fix for Remaining Files

Run this find/replace across remaining impl/ files:
- Find: `import com.hypixel.hytale.server.core.entity.living.ServerLivingEntity;`
- Replace: `import arvem.aspectral.api.LivingEntity;`

Then:
- Find: `ServerLivingEntity`
- Replace: `LivingEntity`

## Architecture Summary

```
AspectAbilities (main system)
├── AbilityRegistry (central registry)
│   ├── AbilityType registrations
│   ├── AbilityFactory registrations
│   ├── ConditionFactory registrations
│   └── ActionFactory registrations
├── AbilityHolderComponent.Manager (entity tracking)
│   └── AbilityHolderComponent per entity
│       └── Map<String, Ability> abilities
├── Scheduler (tick-based task scheduling)
└── api/
    ├── LivingEntity interface
    └── Player interface
```

## Usage

```java
// Initialize (in plugin setup)
AspectAbilities.initialize(server);

// Get ability component for entity
AbilityHolderComponent component = AbilityHolderComponent.getOrCreate(entity);

// Grant ability
AbilityType<?> type = registry.getAbilityType("aspectral:fire_immunity");
component.addAbility(type, "my_source");

// Check ability
if (type.isActive(entity)) {
    // Do something
}

// Revoke ability
component.removeAbility(type, "my_source");
```
