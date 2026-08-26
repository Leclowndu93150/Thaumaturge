package com.leclowndu93150.thaumaturge.data.model;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeOneway;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class TubeModels {
    private TubeModels() {}

    private static final Identifier TUBE_CORE = id("block/tube_core");
    private static final Identifier TUBE_CORE_VALVE = id("block/tube_core_valve");
    private static final Identifier TUBE_FILTER_CORE = id("block/tube_filter_core");
    private static final Identifier TUBE_BUFFER_CORE = id("block/tube_buffer_core");
    private static final Identifier TUBE_SIDE = id("block/tube_side");
    private static final Identifier TUBE_SIDE_RESTRICT = id("block/tube_side_restrict");
    private static final Identifier TUBE_ONEWAY_MARKER = id("block/tube_oneway_marker");

    public static void register(BlockModelGenerators blockModels) {
        registerTube(blockModels, TCBlocks.TUBE.get(), TUBE_CORE, TUBE_SIDE);
        registerTube(blockModels, TCBlocks.TUBE_VALVE.get(), TUBE_CORE_VALVE, TUBE_SIDE);
        registerTube(blockModels, TCBlocks.TUBE_RESTRICT.get(), TUBE_CORE, TUBE_SIDE_RESTRICT);
        registerTube(blockModels, TCBlocks.TUBE_FILTER.get(), TUBE_FILTER_CORE, TUBE_SIDE);
        registerOnewayTube(blockModels);
        registerTube(blockModels, TCBlocks.TUBE_BUFFER.get(), TUBE_BUFFER_CORE, TUBE_SIDE);
    }

    private static void registerOnewayTube(BlockModelGenerators blockModels) {
        Block block = TCBlocks.TUBE_ONEWAY.get();
        MultiPartGenerator generator = tubeParts(block, TUBE_CORE, TUBE_SIDE);
        for (Direction facing : Direction.values()) {
            Direction input = facing.getOpposite();
            Variant marker = applyRotation(new Variant(TUBE_ONEWAY_MARKER), input);
            ConditionBuilder condition = new ConditionBuilder().term(BlockTubeOneway.FACING, facing).term(BlockEssentiaTransport.propertyFor(input), true);
            generator = generator.with(condition, new MultiVariant(WeightedList.of(marker)));
        }
        blockModels.blockStateOutput.accept(generator);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TCIds.MODID, path);
    }

    private static void registerTube(BlockModelGenerators blockModels, Block block, Identifier coreModel, Identifier sideModel) {
        blockModels.blockStateOutput.accept(tubeParts(block, coreModel, sideModel));
    }

    private static MultiPartGenerator tubeParts(Block block, Identifier coreModel, Identifier sideModel) {
        MultiVariant coreVariant = new MultiVariant(WeightedList.of(new Variant(coreModel)));
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(coreVariant);
        for (Direction direction : Direction.values()) {
            BooleanProperty property = BlockEssentiaTransport.propertyFor(direction);
            Variant rotated = applyRotation(new Variant(sideModel), direction);
            MultiVariant variant = new MultiVariant(WeightedList.of(rotated));
            generator = generator.with(new ConditionBuilder().term(property, true), variant);
        }
        return generator;
    }

    private static Variant applyRotation(Variant base, Direction direction) {
        VariantMutator mutator = switch (direction) {
            case DOWN -> BlockModelGenerators.NOP;
            case UP -> BlockModelGenerators.X_ROT_180;
            case NORTH -> BlockModelGenerators.X_ROT_270;
            case SOUTH -> BlockModelGenerators.X_ROT_90;
            case WEST -> BlockModelGenerators.X_ROT_270.then(BlockModelGenerators.Y_ROT_270);
            case EAST -> BlockModelGenerators.X_ROT_270.then(BlockModelGenerators.Y_ROT_90);
        };
        return mutator.apply(base);
    }
}
