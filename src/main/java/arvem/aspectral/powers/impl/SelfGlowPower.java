package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataTypes;

/**
 * power that makes this entity glow.
 */
public class SelfGlowPower extends Power {

    private final int red, green, blue;

    public SelfGlowPower(PowerType<SelfGlowPower> type, LivingEntity entity,
                         int red, int green, int blue) {
        super(type, entity);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean shouldGlow() {
        return isActive();
    }

    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }

    public static PowerFactory<SelfGlowPower> createFactory() {
        return new PowerFactory<SelfGlowPower>(
            AspectPowers.identifier("self_glow"),
            new SerializableData()
                .add("red", SerializableDataTypes.INT, 255)
                .add("green", SerializableDataTypes.INT, 255)
                .add("blue", SerializableDataTypes.INT, 255),
            data -> (type, entity) -> new SelfGlowPower(
                type, entity,
                data.get("red"),
                data.get("green"),
                data.get("blue")
            )
        ).allowCondition();
    }
}


