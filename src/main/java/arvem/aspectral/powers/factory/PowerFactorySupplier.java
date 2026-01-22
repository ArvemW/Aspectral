package arvem.aspectral.powers.factory;

/**
 * Supplier for power factories.
 * Used for lazy initialization of factories.
 */
@FunctionalInterface
public interface PowerFactorySupplier {
    PowerFactory<?> get();
}


