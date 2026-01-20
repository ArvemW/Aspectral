package arvem.aspectral.abilities.factory;

/**
 * Supplier for ability factories.
 * Used for lazy initialization of factories.
 */
@FunctionalInterface
public interface AbilityFactorySupplier {
    AbilityFactory<?> get();
}
