package arvem.aspectral.aspect;

import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerDefinition;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.power.PowerRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an Aspect (like an Origin) that players can choose.
 * Each Aspect references a list of power IDs and metadata.
 * Aspects are defined in JSON and loaded at runtime.
 * <p>
 * Unlike the old system where powers were embedded in aspects,
 * this follows Origins' pattern where aspects reference reusable powers.
 */
public class Aspect {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final String identifier;
    private final List<String> powerIds; // References to powers in PowerTypeRegistry

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
        this.powerIds = new ArrayList<>();
        this.order = Integer.MAX_VALUE;
        this.impact = 0;
        this.unchoosable = false;
    }

    /**
     * Add a power reference to this aspect.
     * @param powerId The power ID (e.g., "aspectral:launch")
     */
    public void addPower(String powerId) {
        powerIds.add(powerId);
    }

    /**
     * Create all power instances for the given entity.
     * This is called when granting an aspect to a player.
     * @param entity The entity to create powers for
     * @return List of power instances
     */
    public List<Power> createAbilities(LivingEntity entity) {
        List<Power> abilities = new ArrayList<>();
        PowerRegistry powerRegistry = PowerRegistry.getInstance();

        for (String powerId : powerIds) {
            PowerDefinition def = powerRegistry.getPower(powerId);
            if (def == null) {
                LOGGER.atWarning().log("Power not found: %s (referenced by aspect %s)", powerId, identifier);
                continue;
            }

            try {
                Power power = def.powerType.create(entity, def.data);
                abilities.add(power);
            } catch (Exception e) {
                LOGGER.atWarning().log("Failed to create power from power %s: %s", powerId, e.getMessage());
            }
        }
        return abilities;
    }

    /**
     * Get the number of powers in this aspect.
     */
    public int getPowerCount() {
        return powerIds.size();
    }

    /**
     * Get a power ID by index.
     * Used for identifying powers as "aspectId:powerIndex".
     */
    public String getPowerId(int index) {
        if (index < 0 || index >= powerIds.size()) {
            return null;
        }
        return powerIds.get(index);
    }

    /**
     * Get a power definition by index.
     */
    public PowerDefinition getPowerDefinition(int index) {
        String powerId = getPowerId(index);
        if (powerId == null) {
            return null;
        }
        return PowerRegistry.getInstance().getPower(powerId);
    }

    /**
     * Get all power IDs (immutable).
     */
    public List<String> getPowerIds() {
        return Collections.unmodifiableList(powerIds);
    }

    /**
     * Get all power definitions for this aspect.
     * This resolves power IDs to their actual definitions.
     */
    public List<PowerDefinition> getPowerDefinitions() {
        PowerRegistry powerRegistry = PowerRegistry.getInstance();
        List<PowerDefinition> definitions = new ArrayList<>();

        for (String powerId : powerIds) {
            PowerDefinition def = powerRegistry.getPower(powerId);
            if (def != null) {
                definitions.add(def);
            }
        }

        return Collections.unmodifiableList(definitions);
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
        for (String powerId : powerIds) {
            powersArray.add(powerId);
        }
        json.add("powers", powersArray);

        return json;
    }

    @Override
    public String toString() {
        return "Aspect[" + identifier + ", powers=" + powerIds.size() + "]";
    }
}

