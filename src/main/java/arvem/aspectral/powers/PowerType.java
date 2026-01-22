package arvem.aspectral.powers;

import arvem.aspectral.component.PowerHolderComponent;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import com.google.gson.JsonObject;

/**
 * Represents a type of power that can be created for entities.
 * Each PowerType is a template that produces Power instances.
 *
 * @param <T> The specific power subclass this type creates
 */
public class PowerType<T extends Power> {

    private final PowerFactory<T>.Instance factory;
    private final String identifier;

    private String nameKey;
    private String descriptionKey;

    private String name;
    private String description;

    private boolean hidden = false;
    private boolean subPower = false;

    public PowerType(String id, PowerFactory<T>.Instance factory) {
        this.identifier = id;
        this.factory = factory;
    }

    public String getIdentifier() {
        return identifier;
    }

    public PowerFactory<T>.Instance getFactory() {
        return factory;
    }

    public PowerType<T> setHidden() {
        return this.setHidden(true);
    }

    public PowerType<T> setsubPower() {
        return this.setsubPower(true);
    }

    public PowerType<T> setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public PowerType<T> setsubPower(boolean subPower) {
        this.subPower = subPower;
        return this;
    }

    public void setTranslationKeys(String name, String description) {
        this.nameKey = name;
        this.descriptionKey = description;
    }

    public PowerType<T> setDisplayTexts(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    /**
     * Create an instance of this power for the given entity.
     */
    public T create(LivingEntity entity) {
        T power = factory.apply(this, entity);

        power.setSerializableData(factory.getFactory().getSerializableData());
        power.setDataInstance(factory.getDataInstance());

        return power;
    }

    /**
     * Create an instance of this power for the given entity from JSON data.
     * This allows creating power instances with custom configuration.
     * @param entity The entity this power will be attached to
     * @param data JSON data containing power configuration
     * @return A new power instance configured with the provided data
     */
    public T create(LivingEntity entity, JsonObject data) {
        // Read the data from JSON using the factory
        PowerFactory<T>.Instance jsonFactory = (PowerFactory<T>.Instance) factory.getFactory().read(data);
        T power = jsonFactory.apply(this, entity);

        power.setSerializableData(factory.getFactory().getSerializableData());
        power.setDataInstance(jsonFactory.getDataInstance());

        return power;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean issubPower() {
        return this.subPower;
    }

    /**
     * Check if this power is active on the given entity.
     */
    public boolean isActive(LivingEntity entity) {
        if (entity != null && identifier != null) {
            PowerHolderComponent component = PowerHolderComponent.get(entity);
            if (component != null && component.hasPower(this)) {
                Power power = component.getPower(this);
                return power != null && power.isActive();
            }
        }
        return false;
    }

    /**
     * Get the power instance from an entity, if present.
     */
    public T get(LivingEntity entity) {
        if (entity != null) {
            PowerHolderComponent component = PowerHolderComponent.get(entity);
            if (component != null) {
                return component.getPower(this);
            }
        }
        return null;
    }

    public String getOrCreateNameKey() {
        if (nameKey == null || nameKey.isEmpty()) {
            String[] parts = identifier.split(":");
            String namespace = parts.length > 1 ? parts[0] : "aspectral";
            String path = parts.length > 1 ? parts[1] : parts[0];
            nameKey = "power." + namespace + "." + path + ".name";
        }
        return nameKey;
    }

    public String getName() {
        return name != null ? name : getOrCreateNameKey();
    }

    public String getOrCreateDescriptionKey() {
        if (descriptionKey == null || descriptionKey.isEmpty()) {
            String[] parts = identifier.split(":");
            String namespace = parts.length > 1 ? parts[0] : "aspectral";
            String path = parts.length > 1 ? parts[1] : parts[0];
            descriptionKey = "power." + namespace + "." + path + ".description";
        }
        return descriptionKey;
    }

    public String getDescription() {
        return description != null ? description : getOrCreateDescriptionKey();
    }

    public JsonObject toJson() {
        JsonObject jsonObject = factory.toJson();
        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("description", getDescription());
        return jsonObject;
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PowerType<?> other)) return false;
        return this.identifier.equals(other.identifier);
    }

    @Override
    public String toString() {
        return "PowerType[" + identifier + "]";
    }
}


