package com.leclowndu93150.thaumaturge.data.model.warding;

import com.leclowndu93150.thaumaturge.client.warding.WardedGlassUnbakedModel;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class WardedGlassModelGenerator {
    private static final String ITEM_MODEL_SUFFIX = "_item";

    private WardedGlassModelGenerator() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block glass = TTBlocks.WARDED_GLASS.get();
        blockModels.blockStateOutput.accept(new SingleModelDefinition(glass, WardedGlassUnbakedModel.INSTANCE));
        Identifier itemModel = ModelTemplates.CUBE_ALL.createWithSuffix(glass, ITEM_MODEL_SUFFIX, TextureMapping.cube(glass), blockModels.modelOutput);
        itemModels.itemModelOutput.accept(TTItems.WARDED_GLASS.get(), ItemModelUtils.plainModel(itemModel));
    }

    private record SingleModelDefinition(Block block, BlockStateModel.Unbaked model) implements BlockModelDefinitionGenerator {
        @Override
        public BlockStateModelDispatcher create() {
            return new BlockStateModelDispatcher(Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(Map.of("", model))), Optional.empty());
        }
    }
}
