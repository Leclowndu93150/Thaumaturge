package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;

/**
 * The original Alchemical Furnace: a compact, fuel-powered essentia smelter.
 *
 * <p>The native smelter block already implements its historical two-slot input/fuel menu,
 * cooking, bellows, alembic output, and flux rules, so this casing intentionally shares that
 * implementation rather than duplicating or subtly changing it.
 */
public final class BlockAlchemicalFurnace extends BlockSmelter {
    public BlockAlchemicalFurnace(Properties properties) {
        super(properties);
    }
}
