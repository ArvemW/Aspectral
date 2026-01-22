package arvem.aspectral.powers.impl;

import arvem.aspectral.AspectPowers;
import arvem.aspectral.powers.Power;
import arvem.aspectral.powers.PowerTypeReference;
import arvem.aspectral.powers.PowerType;
import arvem.aspectral.powers.factory.PowerFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectPowersDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * An power that contains multiple sub-powers.
 * Useful for grouping related powers together.
 */
public class MultiplePower extends Power {

    private final List<PowerType<?>> subPowerTypes;
    private final List<Power> subAbilities = new ArrayList<>();

    public MultiplePower(PowerType<?> type, LivingEntity entity,
                         List<PowerTypeReference> subPowerRefs) {
        super(type, entity);
        this.subPowerTypes = new ArrayList<>();

        for (var ref : subPowerRefs) {
            PowerType<?> subType = ref.getReferencedPowerType();
            if (subType != null) {
                subPowerTypes.add(subType);
            }
        }
    }

    @Override
    public void onGained() {
        for (PowerType<?> subType : subPowerTypes) {
            Power subPower = subType.create(entity);
            subAbilities.add(subPower);
            subPower.onGained();
        }
    }

    @Override
    public void onLost() {
        for (Power subPower : subAbilities) {
            subPower.onLost();
        }
        subAbilities.clear();
    }

    @Override
    public void onAdded(boolean onSync) {
        for (Power subPower : subAbilities) {
            subPower.onAdded(onSync);
        }
    }

    @Override
    public void onRemoved(boolean onSync) {
        for (Power subPower : subAbilities) {
            subPower.onRemoved(onSync);
        }
    }

    @Override
    public void onRespawn() {
        for (Power subPower : subAbilities) {
            subPower.onRespawn();
        }
    }

    @Override
    public void tick() {
        for (Power subPower : subAbilities) {
            if (subPower.shouldTick()) {
                if (subPower.shouldTickWhenInactive() || subPower.isActive()) {
                    subPower.tick();
                }
            }
        }
    }

    @Override
    public boolean shouldTick() {
        return subAbilities.stream().anyMatch(Power::shouldTick);
    }

    public List<Power> getSubAbilities() {
        return subAbilities;
    }

    @SuppressWarnings("unchecked")
    public <T extends Power> List<T> getSubAbilities(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Power power : subAbilities) {
            if (clazz.isInstance(power)) {
                result.add((T) power);
            }
        }
        return result;
    }

    public static PowerFactory<MultiplePower> createFactory() {
        return new PowerFactory<MultiplePower>(
            AspectPowers.identifier("multiple"),
            new SerializableData()
                .add("powers", SerializableDataType.list(AspectPowersDataTypes.POWER_TYPE)),
            data -> (type, entity) -> new MultiplePower(
                type, entity,
                data.get("powers")
            )
        ).allowCondition();
    }
}


