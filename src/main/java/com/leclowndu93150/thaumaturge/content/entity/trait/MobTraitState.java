package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;

public record MobTraitState(List<Holder<MobTrait>> traits) {
    public static final MobTraitState EMPTY = new MobTraitState(List.of());
    public static final MapCodec<MobTraitState> CODEC = RegistryFixedCodec.create(MobTrait.REGISTRY_KEY)
            .listOf()
            .fieldOf("traits")
            .xmap(MobTraitState::new, MobTraitState::traits);
    public static final StreamCodec<RegistryFriendlyByteBuf, MobTraitState> STREAM_CODEC = ByteBufCodecs.holderRegistry(
                    MobTrait.REGISTRY_KEY)
            .apply(ByteBufCodecs.list())
            .map(MobTraitState::new, MobTraitState::traits);

    public MobTraitState {
        traits = List.copyOf(traits);
    }

    public boolean isEmpty() {
        return traits.isEmpty();
    }

    public boolean contains(Holder<MobTrait> trait) {
        for (Holder<MobTrait> held : traits) {
            if (held.value() == trait.value()) {
                return true;
            }
        }
        return false;
    }

    public MobTraitState with(Holder<MobTrait> trait) {
        List<Holder<MobTrait>> next = new ArrayList<>(traits);
        next.add(trait);
        return new MobTraitState(next);
    }

    public MobTraitState without(Holder<MobTrait> trait) {
        List<Holder<MobTrait>> next = new ArrayList<>(traits);
        next.removeIf(held -> held.value() == trait.value());
        return new MobTraitState(next);
    }
}
