package com.leclowndu93150.thaumaturge.data.model;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class TubeModels {
    private TubeModels() {}

    private static final ResourceLocation TUBE_CORE = id("block/tube_core");
    private static final ResourceLocation TUBE_CORE_VALVE = id("block/tube_core_valve");
    private static final ResourceLocation TUBE_FILTER_CORE = id("block/tube_filter_core");
    private static final ResourceLocation TUBE_BUFFER_CORE = id("block/tube_buffer_core");
    private static final ResourceLocation TUBE_SIDE = id("block/tube_side");
    private static final ResourceLocation TUBE_SIDE_RESTRICT = id("block/tube_side_restrict");
    private static final ResourceLocation TUBE_ONEWAY_INDICATOR = id("block/tube_oneway_indicator");

    public static void register(Consumer<BlockStateGenerator> blockStateOutput) {
        registerTube(blockStateOutput, TCBlocks.TUBE.get(), TUBE_CORE, TUBE_SIDE);
        registerTube(blockStateOutput, TCBlocks.TUBE_VALVE.get(), TUBE_CORE_VALVE, TUBE_SIDE);
        registerTube(blockStateOutput, TCBlocks.TUBE_RESTRICT.get(), TUBE_CORE, TUBE_SIDE_RESTRICT);
        registerTube(blockStateOutput, TCBlocks.TUBE_FILTER.get(), TUBE_FILTER_CORE, TUBE_SIDE);
        registerOnewayTube(blockStateOutput);
        registerTube(blockStateOutput, TCBlocks.TUBE_BUFFER.get(), TUBE_BUFFER_CORE, TUBE_SIDE);
    }

    private static void registerOnewayTube(Consumer<BlockStateGenerator> blockStateOutput) {
        Block block = TCBlocks.TUBE_ONEWAY.get();
        MultiPartGenerator generator =
                MultiPartGenerator.multiPart(block).with(Variant.variant().with(VariantProperties.MODEL, TUBE_CORE));
        for (Direction direction : Direction.values()) {
            generator = generator
                    .with(
                            Condition.condition().term(BlockStateProperties.FACING, direction),
                            indicatorVariant(direction))
                    .with(
                            Condition.condition().term(BlockEssentiaTransport.propertyFor(direction), true),
                            sideVariant(TUBE_SIDE, direction));
        }
        blockStateOutput.accept(generator);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TCIds.MODID, path);
    }

    private static void registerTube(
            Consumer<BlockStateGenerator> blockStateOutput,
            Block block,
            ResourceLocation coreModel,
            ResourceLocation sideModel) {
        MultiPartGenerator generator =
                MultiPartGenerator.multiPart(block).with(Variant.variant().with(VariantProperties.MODEL, coreModel));
        for (Direction direction : Direction.values()) {
            BooleanProperty property = BlockEssentiaTransport.propertyFor(direction);
            generator = generator.with(Condition.condition().term(property, true), sideVariant(sideModel, direction));
        }
        blockStateOutput.accept(generator);
    }

    private static Variant sideVariant(ResourceLocation model, Direction direction) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, model);
        return switch (direction) {
            case DOWN -> variant;
            case UP -> variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180);
            case NORTH -> variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
            case SOUTH -> variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case WEST ->
                variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case EAST ->
                variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
        };
    }

    private static Variant indicatorVariant(Direction direction) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, TUBE_ONEWAY_INDICATOR);
        return switch (direction) {
            case NORTH -> variant;
            case SOUTH -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case WEST -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case EAST -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP -> variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case DOWN -> variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
        };
    }
}
