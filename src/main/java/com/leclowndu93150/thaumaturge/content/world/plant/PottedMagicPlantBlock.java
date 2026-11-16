package com.leclowndu93150.thaumaturge.content.world.plant;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class PottedMagicPlantBlock extends FlowerPotBlock {
    private final Supplier<MagicPlantBlock> plant;

    public PottedMagicPlantBlock(
            Supplier<FlowerPotBlock> emptyPot, Supplier<MagicPlantBlock> plant, BlockBehaviour.Properties properties) {
        super(emptyPot, plant, properties);
        this.plant = plant;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        plant.get().animatePottedTick(level, pos, random);
    }
}
