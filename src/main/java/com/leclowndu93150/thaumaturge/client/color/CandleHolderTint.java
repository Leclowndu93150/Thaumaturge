package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import java.util.Set;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class CandleHolderTint implements BlockTintSource {
    private static final int OPAQUE = 0xFF000000;
    private static final int UNTINTED = 0xFFFFFFFF;

    @Override
    public int color(BlockState state) {
        return state.getValue(BlockCandleHolder.CANDLE).dye().map(dye -> OPAQUE | dye.getMapColor().col).orElse(UNTINTED);
    }

    @Override
    public Set<Property<?>> relevantProperties() {
        return Set.of(BlockCandleHolder.CANDLE);
    }
}
