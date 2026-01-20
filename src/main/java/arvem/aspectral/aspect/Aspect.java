package arvem.aspectral.aspect;

import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.api.LivingEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an Aspect (like an Origin) that players can choose.
 * Each Aspect contains a list of abilities (powers) and metadata.
 * Aspects are defined in JSON and loaded at runtime.
 */
public class Aspect {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final String identifier;
    private final List<AbilityDefinition> abilityDefinitions;

    private String name;
    private String description;
    private String icon;
    private int order;
    private int impact;
    private boolean unchoosable;

    /**
     * Create an aspect.
     * @param identifier Unique identifier (e.g., "aspectral:human")
     */
    public Aspect(String identifier) {
        this.identifier = identifier;
        this.abilityDefinitions = new ArrayList<>();
        this.order = Integer.MAX_VALUE;
        this.impact = 0;
        this.unchoosable = false;
    }

    /**
     * Add an ability definition to this aspect.
     * @param abilityType The type of ability
     * @param data JSON data for configuring the ability instance
     */
    public void addAbility(AbilityType<?> abilityType, JsonObject data) {
        abilityDefinitions.add(new AbilityDefinition(abilityType, data));
    }

    /**
     * Create all ability instances for the given entity.
     * This is called when granting an aspect to a player.
     * @param entity The entity to create abilities for
     * @return List of ability instances
     */
    public List<Ability> createAbilities(LivingEntity entity) {
        List<Ability> abilities = new ArrayList<>();
        for (AbilityDefinition def : abilityDefinitions) {
            try {
                Ability ability = def.abilityType.create(entity, def.data);
                abilities.add(ability);
            } catch (Exception e) {
                LOGGER.atWarning().log("Failed to create ability from type %s: %s",
                    def.abilityType.getIdentifier(), e.getMessage());
            }
        }
        return abilities;
    }

    /**
     * Get the number of abilities in this aspect.
     */
    public int getAbilityCount() {
        return abilityDefinitions.size();
    }

    /**
     * Get an ability definition by index.
     * Used for identifying abilities as "aspectId:powerIndex".
     */
    public AbilityDefinition getAbilityDefinition(int index) {
        if (index < 0 || index >= abilityDefinitions.size()) {
            return null;
        }
        return abilityDefinitions.get(index);
    }

    /**
     * Get all ability definitions (immutable).
     */
    public List<AbilityDefinition> getAbilityDefinitions() {
        return Collections.unmodifiableList(abilityDefinitions);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name != null ? name : identifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getImpact() {
        return impact;
    }

    public void setImpact(int impact) {
        this.impact = impact;
    }

    public boolean isUnchoosable() {
        return unchoosable;
    }

    public void setUnchoosable(boolean unchoosable) {
        this.unchoosable = unchoosable;
    }

    /**
     * Serialize this aspect to JSON (for debugging/export).
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("icon", icon);
        json.addProperty("order", order);
        json.addProperty("impact", impact);
        json.addProperty("unchoosable", unchoosable);

        JsonArray powersArray = new JsonArray();
        for (AbilityDefinition def : abilityDefinitions) {
            powersArray.add(def.abilityType.getIdentifier());
        }
        json.add("powers", powersArray);

        return json;
    }

    @Override
    public String toString() {
        return "Aspect[" + identifier + ", abilities=" + abilityDefinitions.size() + "]";
    }

    /**
     * Holds the definition of an ability within an aspect.
     * This is the template + data, not the actual ability instance.
     */
    public static class AbilityDefinition {
        public final AbilityType<?> abilityType;
        public final JsonObject data;

        public AbilityDefinition(AbilityType<?> abilityType, JsonObject data) {
            this.abilityType = abilityType;
            this.data = data;
        }
    }
}
