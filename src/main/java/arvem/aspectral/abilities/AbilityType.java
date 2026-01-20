package arvem.aspectral.abilities;

import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.component.AbilityHolderComponent;
import com.google.gson.JsonObject;

/**
 * Represents a type of ability that can be created for entities.
 * Each AbilityType is a template that produces Ability instances.
 *
 * @param <T> The specific Ability subclass this type creates
 */
public class AbilityType<T extends Ability> {

    private final AbilityFactory<T>.Instance factory;
    private final String identifier;

    private String nameKey;
    private String descriptionKey;

    private String name;
    private String description;

    private boolean hidden = false;
    private boolean subAbility = false;

    public AbilityType(String id, AbilityFactory<T>.Instance factory) {
        this.identifier = id;
        this.factory = factory;
    }

    public String getIdentifier() {
        return identifier;
    }

    public AbilityFactory<T>.Instance getFactory() {
        return factory;
    }

    public AbilityType<T> setHidden() {
        return this.setHidden(true);
    }

    public AbilityType<T> setSubAbility() {
        return this.setSubAbility(true);
    }

    public AbilityType<T> setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public AbilityType<T> setSubAbility(boolean subAbility) {
        this.subAbility = subAbility;
        return this;
    }

    public void setTranslationKeys(String name, String description) {
        this.nameKey = name;
        this.descriptionKey = description;
    }

    public AbilityType<T> setDisplayTexts(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    /**
     * Create an instance of this ability for the given entity.
     */
    public T create(LivingEntity entity) {
        T ability = factory.apply(this, entity);

        ability.setSerializableData(factory.getFactory().getSerializableData());
        ability.setDataInstance(factory.getDataInstance());

        return ability;
    }

    /**
     * Create an instance of this ability for the given entity from JSON data.
     * This allows creating ability instances with custom configuration.
     * @param entity The entity this ability will be attached to
     * @param data JSON data containing ability configuration
     * @return A new ability instance configured with the provided data
     */
    public T create(LivingEntity entity, JsonObject data) {
        // Read the data from JSON using the factory
        AbilityFactory<T>.Instance jsonFactory = (AbilityFactory<T>.Instance) factory.getFactory().read(data);
        T ability = jsonFactory.apply(this, entity);

        ability.setSerializableData(factory.getFactory().getSerializableData());
        ability.setDataInstance(jsonFactory.getDataInstance());

        return ability;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isSubAbility() {
        return this.subAbility;
    }

    /**
     * Check if this ability is active on the given entity.
     */
    public boolean isActive(LivingEntity entity) {
        if (entity != null && identifier != null) {
            AbilityHolderComponent component = AbilityHolderComponent.get(entity);
            if (component != null && component.hasAbility(this)) {
                Ability ability = component.getAbility(this);
                return ability != null && ability.isActive();
            }
        }
        return false;
    }

    /**
     * Get the ability instance from an entity, if present.
     */
    public T get(LivingEntity entity) {
        if (entity != null) {
            AbilityHolderComponent component = AbilityHolderComponent.get(entity);
            if (component != null) {
                return component.getAbility(this);
            }
        }
        return null;
    }

    public String getOrCreateNameKey() {
        if (nameKey == null || nameKey.isEmpty()) {
            String[] parts = identifier.split(":");
            String namespace = parts.length > 1 ? parts[0] : "aspectral";
            String path = parts.length > 1 ? parts[1] : parts[0];
            nameKey = "ability." + namespace + "." + path + ".name";
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
            descriptionKey = "ability." + namespace + "." + path + ".description";
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
        if (!(obj instanceof AbilityType<?> other)) return false;
        return this.identifier.equals(other.identifier);
    }

    @Override
    public String toString() {
        return "AbilityType[" + identifier + "]";
    }
}
