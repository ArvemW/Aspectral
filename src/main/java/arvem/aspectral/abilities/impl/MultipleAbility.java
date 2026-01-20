package arvem.aspectral.abilities.impl;

import arvem.aspectral.AspectAbilities;
import arvem.aspectral.abilities.Ability;
import arvem.aspectral.abilities.AbilityTypeReference;
import arvem.aspectral.abilities.AbilityType;
import arvem.aspectral.abilities.factory.AbilityFactory;
import arvem.aspectral.api.LivingEntity;
import arvem.aspectral.data.AspectAbilitiesDataTypes;
import arvem.aspectral.data.SerializableData;
import arvem.aspectral.data.SerializableDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * An ability that contains multiple sub-abilities.
 * Useful for grouping related abilities together.
 */
public class MultipleAbility extends Ability {

    private final List<AbilityType<?>> subAbilityTypes;
    private final List<Ability> subAbilities = new ArrayList<>();

    public MultipleAbility(AbilityType<?> type, LivingEntity entity,
                           List<AbilityTypeReference> subAbilityRefs) {
        super(type, entity);
        this.subAbilityTypes = new ArrayList<>();

        for (var ref : subAbilityRefs) {
            AbilityType<?> subType = ref.getReferencedAbilityType();
            if (subType != null) {
                subAbilityTypes.add(subType);
            }
        }
    }

    @Override
    public void onGained() {
        for (AbilityType<?> subType : subAbilityTypes) {
            Ability subAbility = subType.create(entity);
            subAbilities.add(subAbility);
            subAbility.onGained();
        }
    }

    @Override
    public void onLost() {
        for (Ability subAbility : subAbilities) {
            subAbility.onLost();
        }
        subAbilities.clear();
    }

    @Override
    public void onAdded(boolean onSync) {
        for (Ability subAbility : subAbilities) {
            subAbility.onAdded(onSync);
        }
    }

    @Override
    public void onRemoved(boolean onSync) {
        for (Ability subAbility : subAbilities) {
            subAbility.onRemoved(onSync);
        }
    }

    @Override
    public void onRespawn() {
        for (Ability subAbility : subAbilities) {
            subAbility.onRespawn();
        }
    }

    @Override
    public void tick() {
        for (Ability subAbility : subAbilities) {
            if (subAbility.shouldTick()) {
                if (subAbility.shouldTickWhenInactive() || subAbility.isActive()) {
                    subAbility.tick();
                }
            }
        }
    }

    @Override
    public boolean shouldTick() {
        return subAbilities.stream().anyMatch(Ability::shouldTick);
    }

    public List<Ability> getSubAbilities() {
        return subAbilities;
    }

    @SuppressWarnings("unchecked")
    public <T extends Ability> List<T> getSubAbilities(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Ability ability : subAbilities) {
            if (clazz.isInstance(ability)) {
                result.add((T) ability);
            }
        }
        return result;
    }

    public static AbilityFactory<MultipleAbility> createFactory() {
        return new AbilityFactory<MultipleAbility>(
            AspectAbilities.identifier("multiple"),
            new SerializableData()
                .add("abilities", SerializableDataType.list(AspectAbilitiesDataTypes.ABILITY_TYPE)),
            data -> (type, entity) -> new MultipleAbility(
                type, entity,
                data.get("abilities")
            )
        ).allowCondition();
    }
}
