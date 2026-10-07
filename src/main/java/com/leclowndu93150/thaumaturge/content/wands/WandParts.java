package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record WandParts(WandCap cap, WandRod rod, boolean sceptre) {
    public static final Codec<WandParts> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    TTWandParts.caps().byNameCodec().fieldOf("cap").forGetter(WandParts::cap),
                    TTWandParts.rods().byNameCodec().fieldOf("rod").forGetter(WandParts::rod),
                    Codec.BOOL.optionalFieldOf("sceptre", false).forGetter(WandParts::sceptre))
            .apply(instance, WandParts::new));

    public static final StreamCodec<ByteBuf, WandParts> STREAM_CODEC = StreamCodec.composite(
            registryStream(TTWandParts::caps),
            WandParts::cap,
            registryStream(TTWandParts::rods),
            WandParts::rod,
            ByteBufCodecs.BOOL,
            WandParts::sceptre,
            WandParts::new);

    public static WandParts starter() {
        return new WandParts(TTWandParts.CAP_IRON.get(), TTWandParts.ROD_WOOD.get(), false);
    }

    private static <T> StreamCodec<ByteBuf, T> registryStream(Supplier<Registry<T>> registry) {
        return ResourceLocation.STREAM_CODEC.map(
                id -> registry.get().get(id), value -> registry.get().getKey(value));
    }

    public int maxCentivis() {
        return rod.capacity() * (sceptre ? WandEconomy.SCEPTRE_CAPACITY_PER_VIS : WandEconomy.CENTIVIS_PER_VIS);
    }

    public int craftCost() {
        int cost = cap.craftCost() * rod.craftCost();
        return sceptre ? (int) (cost * WandEconomy.SCEPTRE_CRAFT_COST_FACTOR) : cost;
    }
}
