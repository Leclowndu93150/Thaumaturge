package com.leclowndu93150.thaumaturge.data.model.crystal;

import com.leclowndu93150.thaumaturge.client.render.crystal.CrystalUnbakedModel;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;

public final class CrystalBlockstateGenerator {
    private CrystalBlockstateGenerator() {}

    public static void register(BlockModelGenerators blockModels) {
        emit(blockModels, TCBlocks.CRYSTAL_AER.get());
        emit(blockModels, TCBlocks.CRYSTAL_IGNIS.get());
        emit(blockModels, TCBlocks.CRYSTAL_AQUA.get());
        emit(blockModels, TCBlocks.CRYSTAL_TERRA.get());
        emit(blockModels, TCBlocks.CRYSTAL_ORDO.get());
        emit(blockModels, TCBlocks.CRYSTAL_PERDITIO.get());
        emit(blockModels, TCBlocks.CRYSTAL_VITIUM.get());
    }

    private static void emit(BlockModelGenerators blockModels, Block block) {
        BlockStateModelDispatcher dispatcher = new BlockStateModelDispatcher(
                Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(Map.of("", (BlockStateModel.Unbaked) CrystalUnbakedModel.INSTANCE))), Optional.empty());
        blockModels.blockStateOutput.accept(new BlockModelDefinitionGenerator() {
            @Override
            public Block block() {
                return block;
            }

            @Override
            public BlockStateModelDispatcher create() {
                return dispatcher;
            }
        });
    }
}
