package com.leclowndu93150.thaumaturge.data.model.crystal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.function.Consumer;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class CrystalBlockstateGenerator {
    private static final ResourceLocation CRYSTAL_MODEL = TTIds.rl("block/crystal");

    private CrystalBlockstateGenerator() {}

    public static void register(Consumer<BlockStateGenerator> blockStateOutput) {
        emit(blockStateOutput, TTBlocks.CRYSTAL_AER.get());
        emit(blockStateOutput, TTBlocks.CRYSTAL_IGNIS.get());
        emit(blockStateOutput, TTBlocks.CRYSTAL_AQUA.get());
        emit(blockStateOutput, TTBlocks.CRYSTAL_TERRA.get());
        emit(blockStateOutput, TTBlocks.CRYSTAL_ORDO.get());
        emit(blockStateOutput, TTBlocks.CRYSTAL_PERDITIO.get());
        emit(blockStateOutput, TTBlocks.CRYSTAL_VITIUM.get());
    }

    private static void emit(Consumer<BlockStateGenerator> blockStateOutput, Block block) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                block, Variant.variant().with(VariantProperties.MODEL, CRYSTAL_MODEL)));
    }
}
