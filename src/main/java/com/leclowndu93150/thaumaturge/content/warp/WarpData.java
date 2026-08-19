package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public final class WarpData implements IPlayerWarp {
    public static final int MAX_WARP = 500;

    public static final MapCodec<WarpData> CODEC = RecordCodecBuilder
            .mapCodec(
                    instance -> instance
                            .group(Codec.INT.fieldOf("permanent").forGetter(d -> d.get(WarpType.PERMANENT)), Codec.INT.fieldOf("normal").forGetter(d -> d.get(WarpType.NORMAL)),
                                    Codec.INT.fieldOf("temporary").forGetter(d -> d.get(WarpType.TEMPORARY)), Codec.INT.fieldOf("counter").forGetter(WarpData::getCounter))
                            .apply(instance, WarpData::of));

    public static final StreamCodec<RegistryFriendlyByteBuf, WarpData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, d -> d.get(WarpType.PERMANENT), ByteBufCodecs.VAR_INT,
            d -> d.get(WarpType.NORMAL), ByteBufCodecs.VAR_INT, d -> d.get(WarpType.TEMPORARY), ByteBufCodecs.VAR_INT, WarpData::getCounter, WarpData::of);

    private final int[] warp = new int[WarpType.values().length];
    private int counter;

    public WarpData() {}

    private static WarpData of(int permanent, int normal, int temporary, int counter) {
        WarpData data = new WarpData();
        data.set(WarpType.PERMANENT, permanent);
        data.set(WarpType.NORMAL, normal);
        data.set(WarpType.TEMPORARY, temporary);
        data.setCounter(counter);
        return data;
    }

    @Override
    public int get(WarpType type) {
        return warp[type.ordinal()];
    }

    @Override
    public void set(WarpType type, int amount) {
        warp[type.ordinal()] = Mth.clamp(amount, 0, MAX_WARP);
    }

    @Override
    public int add(WarpType type, int amount) {
        warp[type.ordinal()] = Mth.clamp(warp[type.ordinal()] + amount, 0, MAX_WARP);
        return warp[type.ordinal()];
    }

    @Override
    public int reduce(WarpType type, int amount) {
        return add(type, -amount);
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public void clear() {
        warp[0] = 0;
        warp[1] = 0;
        warp[2] = 0;
        counter = 0;
    }

    public int total() {
        return warp[0] + warp[1] + warp[2];
    }
}
