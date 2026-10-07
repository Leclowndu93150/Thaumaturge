package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityEverfullUrn;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEntityEssentiaCrystalizer;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEntityEssentiaReservoir;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeOneway;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeRestrict;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.spa.BlockEntitySpa;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;

final class JadeBlockProviderHolder {
    static final List<JadeBlockHandler<?>> HANDLERS = List.of(new JadeBlockHandler<>(BlockEntityJar.class, JadeConfig.JARS, JadeEssentiaDetails::jar),
            new JadeBlockHandler<>(BlockEntityTubeBuffer.class, JadeConfig.BUFFERS, JadeEssentiaDetails::buffer),
            new JadeBlockHandler<>(BlockEntityTubeValve.class, JadeConfig.VALVES, JadeEssentiaDetails::valve),
            new JadeBlockHandler<>(BlockEntityTubeFilter.class, JadeConfig.FILTER_TUBES, JadeEssentiaDetails::filterTube),
            new JadeBlockHandler<>(BlockEntityTubeOneway.class, JadeConfig.ONE_WAY_TUBES, JadeEssentiaDetails::oneway),
            new JadeBlockHandler<>(BlockEntityTubeRestrict.class, JadeConfig.RESTRICTED_TUBES, JadeEssentiaDetails::restricted),
            new JadeBlockHandler<>(BlockEntityTube.class, JadeConfig.TUBES, JadeEssentiaDetails::tube),
            new JadeBlockHandler<>(BlockEntityAlembic.class, JadeConfig.ALEMBICS, JadeEssentiaDetails::alembic),
            new JadeBlockHandler<>(BlockEntityCrucible.class, JadeConfig.CRUCIBLES, JadeEssentiaDetails::crucible),
            new JadeBlockHandler<>(BlockEntityThaumatorium.class, JadeConfig.THAUMATORIUMS, JadeEssentiaDetails::thaumatorium),
            new JadeBlockHandler<>(BlockEntityCentrifuge.class, JadeConfig.CENTRIFUGES, JadeEssentiaDetails::centrifuge),
            new JadeBlockHandler<>(BlockEntityEssentiaCrystalizer.class, JadeConfig.CRYSTALIZERS, JadeEssentiaDetails::crystalizer),
            new JadeBlockHandler<>(BlockEntityEssentiaReservoir.class, JadeConfig.RESERVOIRS, JadeEssentiaDetails::reservoir),
            new JadeBlockHandler<>(BlockEntityFluxScrubber.class, JadeConfig.FLUX_SCRUBBERS, JadeMachineDetails::fluxScrubber),
            new JadeBlockHandler<>(BlockEntityVisRelay.class, JadeConfig.VIS_RELAYS, JadeMachineDetails::relay),
            new JadeBlockHandler<>(BlockEntityNodeTransducer.class, JadeConfig.NODE_TRANSDUCERS, JadeMachineDetails::transducer),
            new JadeBlockHandler<>(BlockEntitySmelter.class, JadeConfig.SMELTERS, JadeMachineDetails::smelter),
            new JadeBlockHandler<>(BlockEntityAdvancedAlchemicalFurnace.class, JadeConfig.ADVANCED_ALCHEMICAL_FURNACES, JadeMachineDetails::advancedFurnace),
            new JadeBlockHandler<>(BlockEntityGolemBuilder.class, JadeConfig.GOLEM_BUILDERS, JadeMachineDetails::golemBuilder),
            new JadeBlockHandler<>(BlockEntityVoidSiphon.class, JadeConfig.VOID_SIPHONS, JadeMachineDetails::siphon),
            new JadeBlockHandler<>(BlockEntityDeconstructionTable.class, JadeConfig.DECONSTRUCTION_TABLES, JadeMachineDetails::deconstruction),
            new JadeBlockHandler<>(BlockEntitySpa.class, JadeConfig.SPAS, JadeMachineDetails::spa),
            new JadeBlockHandler<>(BlockEntityEverfullUrn.class, JadeConfig.EVERFULL_URNS, (urn, data) -> data.hideFluid()),
            new JadeBlockHandler<>(BlockEntityInfernalFurnace.class, JadeConfig.INFERNAL_FURNACES, JadeMachineDetails::furnace),
            new JadeBlockHandler<>(BlockEntityFocalManipulator.class, JadeConfig.FOCAL_MANIPULATORS, JadeMachineDetails::focal));
    private static final Map<Block, Class<? extends BlockEntity>> CONTROLLERS = Map.of(TTBlocks.PLACEHOLDER_IRON_BARS.get(), BlockEntityGolemBuilder.class, TTBlocks.PLACEHOLDER_ANVIL.get(),
            BlockEntityGolemBuilder.class, TTBlocks.PLACEHOLDER_CAULDRON.get(), BlockEntityGolemBuilder.class, TTBlocks.PLACEHOLDER_TABLE.get(), BlockEntityGolemBuilder.class,
            TTBlocks.NETHER_BRICKS_PLACEHOLDER.get(), BlockEntityInfernalFurnace.class, TTBlocks.OBSIDIAN_PLACEHOLDER.get(), BlockEntityInfernalFurnace.class,
            TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get(), BlockEntityAdvancedAlchemicalFurnace.class, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get(),
            BlockEntityAdvancedAlchemicalFurnace.class, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get(), BlockEntityAdvancedAlchemicalFurnace.class,
            TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get(), BlockEntityAdvancedAlchemicalFurnace.class);

    private JadeBlockProviderHolder() {}

    static @Nullable BlockEntity resolve(BlockAccessor accessor) {
        BlockEntity direct = accessor.getBlockEntity();
        if (direct != null)
            return direct;
        Class<? extends BlockEntity> type = CONTROLLERS.get(accessor.getBlockState().getBlock());
        if (type == null)
            return null;
        BlockPos origin = accessor.getPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            if (!accessor.getLevel().hasChunkAt(pos))
                continue;
            BlockEntity entity = accessor.getLevel().getBlockEntity(pos);
            if (type.isInstance(entity))
                return entity;
        }
        return null;
    }
}
