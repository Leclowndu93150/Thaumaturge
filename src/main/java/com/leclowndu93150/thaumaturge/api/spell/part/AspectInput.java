package com.leclowndu93150.thaumaturge.api.spell.part;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AffinityReaction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

/**
 * Which aspect a part is bound to. The aspect is the crystal a node costs to inscribe and, for
 * aspect-driven behaviours, the element the node acts with.
 *
 * <p>JSON forms: absent (no aspect), an aspect id (fixed), {@code {"choices": [...], "default": id}}
 * (the player picks from a list), or {@code {"affinity": "strike"|"imbue", "default": id}} (the
 * player picks any aspect whose {@link AspectAffinity} defines that reaction).
 *
 * @since 1.0.0
 */
public sealed interface AspectInput
        permits AspectInput.None, AspectInput.Fixed, AspectInput.Choice, AspectInput.FromAffinities {
    /** The part takes no aspect. */
    AspectInput NONE = new None();

    /** Disk and network codec. */
    Codec<AspectInput> CODEC = Codec.either(ResourceKey.codec(IAspect.REGISTRY_KEY), ObjectForm.CODEC)
            .xmap(
                    either -> either.map(Fixed::new, ObjectForm::toInput),
                    input -> input instanceof Fixed fixed
                            ? Either.left(fixed.aspect())
                            : Either.right(ObjectForm.of(input)));

    /**
     * The aspect a fresh node starts with.
     *
     * @return the default aspect, empty for {@link None}
     */
    Optional<ResourceKey<IAspect>> defaultAspect();

    /**
     * Every aspect a node of this part may carry.
     *
     * @param registries the registry lookup used to read affinities
     * @return the allowed aspects in display order
     */
    List<ResourceKey<IAspect>> options(HolderLookup.Provider registries);

    /**
     * Whether the player picks the aspect.
     *
     * @return true for {@link Choice} and {@link FromAffinities}
     */
    default boolean selectable() {
        return false;
    }

    /**
     * The aspect a node actually uses: the chosen one when allowed, else the default.
     *
     * @param chosen     the node's stored aspect
     * @param registries the registry lookup used to read affinities
     * @return the effective aspect, empty for {@link None}
     */
    default Optional<ResourceKey<IAspect>> resolve(
            Optional<ResourceKey<IAspect>> chosen, HolderLookup.Provider registries) {
        if (chosen.isPresent() && selectable() && options(registries).contains(chosen.get())) {
            return chosen;
        }
        return defaultAspect();
    }

    /**
     * No aspect.
     *
     * @since 1.0.0
     */
    record None() implements AspectInput {
        @Override
        public Optional<ResourceKey<IAspect>> defaultAspect() {
            return Optional.empty();
        }

        @Override
        public List<ResourceKey<IAspect>> options(HolderLookup.Provider registries) {
            return List.of();
        }
    }

    /**
     * One aspect, always.
     *
     * @param aspect the aspect
     * @since 1.0.0
     */
    record Fixed(ResourceKey<IAspect> aspect) implements AspectInput {
        @Override
        public Optional<ResourceKey<IAspect>> defaultAspect() {
            return Optional.of(aspect);
        }

        @Override
        public List<ResourceKey<IAspect>> options(HolderLookup.Provider registries) {
            return List.of(aspect);
        }
    }

    /**
     * A fixed list the player picks from.
     *
     * @param choices    the allowed aspects in display order
     * @param defaultKey the starting aspect, a member of {@code choices}
     * @since 1.0.0
     */
    record Choice(List<ResourceKey<IAspect>> choices, ResourceKey<IAspect> defaultKey) implements AspectInput {
        /**
         * Canonicalizes the list into an immutable copy.
         */
        public Choice {
            choices = List.copyOf(choices);
        }

        @Override
        public Optional<ResourceKey<IAspect>> defaultAspect() {
            return Optional.of(defaultKey);
        }

        @Override
        public List<ResourceKey<IAspect>> options(HolderLookup.Provider registries) {
            return choices;
        }

        @Override
        public boolean selectable() {
            return true;
        }
    }

    /**
     * Every aspect whose affinity defines a reaction.
     *
     * @param reaction   the reaction the part uses
     * @param defaultKey the starting aspect
     * @since 1.0.0
     */
    record FromAffinities(AffinityReaction reaction, ResourceKey<IAspect> defaultKey) implements AspectInput {
        @Override
        public Optional<ResourceKey<IAspect>> defaultAspect() {
            return Optional.of(defaultKey);
        }

        @Override
        public List<ResourceKey<IAspect>> options(HolderLookup.Provider registries) {
            List<ResourceKey<IAspect>> out = new ArrayList<>();
            Optional<? extends HolderLookup.RegistryLookup<AspectAffinity>> lookup =
                    registries.lookup(AspectAffinity.REGISTRY_KEY);
            if (lookup.isEmpty()) {
                return out;
            }
            lookup.get()
                    .listElements()
                    .filter(holder -> reaction.definedBy(holder.value()))
                    .map(Holder.Reference::key)
                    .forEach(key -> out.add(ResourceKey.create(IAspect.REGISTRY_KEY, key.location())));
            return out;
        }

        @Override
        public boolean selectable() {
            return true;
        }
    }

    /**
     * The object form shared by {@link Choice} and {@link FromAffinities}.
     *
     * @param choices    the choice list
     * @param affinity   the affinity reaction
     * @param defaultKey the default aspect
     * @since 1.0.0
     */
    record ObjectForm(
            Optional<List<ResourceKey<IAspect>>> choices,
            Optional<AffinityReaction> affinity,
            Optional<ResourceKey<IAspect>> defaultKey) {
        /** Object codec. */
        public static final Codec<ObjectForm> CODEC = RecordCodecBuilder.create(i -> i.group(
                        ResourceKey.codec(IAspect.REGISTRY_KEY)
                                .listOf()
                                .optionalFieldOf("choices")
                                .forGetter(ObjectForm::choices),
                        AffinityReaction.CODEC.optionalFieldOf("affinity").forGetter(ObjectForm::affinity),
                        ResourceKey.codec(IAspect.REGISTRY_KEY)
                                .optionalFieldOf("default")
                                .forGetter(ObjectForm::defaultKey))
                .apply(i, ObjectForm::new));

        static ObjectForm of(AspectInput input) {
            return switch (input) {
                case Choice choice ->
                    new ObjectForm(Optional.of(choice.choices()), Optional.empty(), Optional.of(choice.defaultKey()));
                case FromAffinities from ->
                    new ObjectForm(Optional.empty(), Optional.of(from.reaction()), Optional.of(from.defaultKey()));
                default -> new ObjectForm(Optional.empty(), Optional.empty(), Optional.empty());
            };
        }

        AspectInput toInput() {
            if (affinity.isPresent() && defaultKey.isPresent()) {
                return new FromAffinities(affinity.get(), defaultKey.get());
            }
            if (choices.isPresent() && !choices.get().isEmpty()) {
                return new Choice(choices.get(), defaultKey.orElse(choices.get().getFirst()));
            }
            return NONE;
        }
    }
}
