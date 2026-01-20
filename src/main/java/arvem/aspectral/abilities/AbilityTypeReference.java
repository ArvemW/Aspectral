package arvem.aspectral.abilities;

import arvem.aspectral.AspectAbilities;

/**
 * A reference to an AbilityType by identifier.
 * Used for lazy loading of ability types from JSON.
 */
public class AbilityTypeReference {

    private final String identifier;
    private AbilityType<?> cachedType;

    public AbilityTypeReference(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the referenced ability type, loading it from the registry if necessary.
     */
    public AbilityType<?> getReferencedAbilityType() {
        if (cachedType == null && AspectAbilities.getInstance() != null) {
            cachedType = AspectAbilities.getInstance().getAbilityRegistry().getAbilityType(identifier);
        }
        return cachedType;
    }

    /**
     * Check if this reference points to a valid ability type.
     */
    public boolean isValid() {
        return getReferencedAbilityType() != null;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof AbilityTypeReference other)) return false;
        return this.identifier.equals(other.identifier);
    }

    @Override
    public String toString() {
        return "AbilityTypeReference[" + identifier + "]";
    }
}
