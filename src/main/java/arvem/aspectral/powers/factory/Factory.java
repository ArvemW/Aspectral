package arvem.aspectral.powers.factory;

import arvem.aspectral.data.SerializableData;

/**
 * Base interface for factories that create game objects from serialized data.
 */
public interface Factory {

    /**
     * Get the unique identifier for this factory.
     */
    String getSerializerId();

    /**
     * Get the serializable data schema for this factory.
     */
    SerializableData getSerializableData();
}


