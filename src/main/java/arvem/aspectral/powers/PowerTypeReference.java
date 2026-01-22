package arvem.aspectral.powers;

import arvem.aspectral.AspectPowers;

/**
 * A reference to an PowerType by identifier.
 * Used for lazy loading of power types from JSON.
 */
public class PowerTypeReference {

    private final String identifier;
    private PowerType<?> cachedType;

    public PowerTypeReference(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the referenced power type, loading it from the registry if necessary.
     */
    public PowerType<?> getReferencedPowerType() {
        if (cachedType == null && AspectPowers.getInstance() != null) {
            cachedType = AspectPowers.getInstance().getPowerRegistry().getPowerType(identifier);
        }
        return cachedType;
    }

    /**
     * Check if this reference points to a valid power type.
     */
    public boolean isValid() {
        return getReferencedPowerType() != null;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PowerTypeReference other)) return false;
        return this.identifier.equals(other.identifier);
    }

    @Override
    public String toString() {
        return "PowerTypeReference[" + identifier + "]";
    }
}


