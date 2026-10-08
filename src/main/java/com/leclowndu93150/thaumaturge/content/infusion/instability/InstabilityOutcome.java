package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public record InstabilityOutcome(int weight, InstabilityEffect effect) {
    public static final ResourceKey<Registry<InstabilityOutcome>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("instability_outcome"));
    public static final Codec<InstabilityOutcome> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weight").forGetter(InstabilityOutcome::weight),
                    InstabilityEffect.CODEC.fieldOf("effect").forGetter(InstabilityOutcome::effect))
            .apply(instance, InstabilityOutcome::new));

    public static Optional<InstabilityOutcome> roll(RegistryAccess registries, RandomSource random) {
        List<Holder.Reference<InstabilityOutcome>> outcomes = registries
                .lookupOrThrow(REGISTRY_KEY)
                .listElements()
                .filter(holder -> holder.value().weight() > 0)
                .sorted(Comparator.comparing(holder -> holder.key().location()))
                .toList();
        int total = 0;
        for (Holder.Reference<InstabilityOutcome> outcome : outcomes) {
            total += outcome.value().weight();
        }
        if (total <= 0) {
            return Optional.empty();
        }
        int pick = random.nextInt(total);
        for (Holder.Reference<InstabilityOutcome> outcome : outcomes) {
            pick -= outcome.value().weight();
            if (pick < 0) {
                return Optional.of(outcome.value());
            }
        }
        return Optional.empty();
    }
}
