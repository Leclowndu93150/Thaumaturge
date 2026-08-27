package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aura.BlockEntityRechargePedestal;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeStabilizer;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.casters.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.decor.BlockEntityBarrierStone;
import com.leclowndu93150.thaumaturge.content.decor.banner.BlockEntityBanner;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityArcaneEar;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityCondenser;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityDioptra;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityEverfullUrn;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityHungryChest;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityLampArcane;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityLampFertility;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityLampGrowth;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityLevitator;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityRedstoneRelay;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVisGenerator;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.bore.BlockEntityArcaneBore;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirror;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirrorEssentia;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockEntityPatternCrafter;
import com.leclowndu93150.thaumaturge.content.device.sprayer.BlockEntityPotionSprayer;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchAltar;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchCap;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchNothing;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchPortal;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchTrap;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityEssentiaPort;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockEntityBellows;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJarBrain;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJarVoid;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatoriumTop;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeOneway;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeRestrict;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
import com.leclowndu93150.thaumaturge.content.focus.BlockEntityHole;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockEntityNitor;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.spa.BlockEntitySpa;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TCIds.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityBanner>> BANNER =
            BLOCK_ENTITIES.register(
                    "banner", () -> new BlockEntityType<>(BlockEntityBanner::new, bannerBlocks(), null));

    private static Set<Block> bannerBlocks() {
        Set<Block> blocks = new HashSet<>();
        for (DyeColor dye : DyeColor.values()) {
            blocks.add(TCBlocks.BANNERS.get(dye).get());
            blocks.add(TCBlocks.WALL_BANNERS.get(dye).get());
        }
        blocks.add(TCBlocks.BANNER_CRIMSON_CULT.get());
        blocks.add(TCBlocks.WALL_BANNER_CRIMSON_CULT.get());
        return blocks;
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityNode>> NODE =
            BLOCK_ENTITIES.register(
                    "node",
                    () -> new BlockEntityType<>(
                            BlockEntityNode::new,
                            Set.of(TCBlocks.NODE.get(), TCBlocks.OBSIDIAN_TOTEM_CHARGED.get()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityNodeStabilizer>> NODE_STABILIZER =
            BLOCK_ENTITIES.register(
                    "node_stabilizer",
                    () -> new BlockEntityType<>(
                            BlockEntityNodeStabilizer::new,
                            Set.of(TCBlocks.NODE_STABILIZER.get(), TCBlocks.NODE_STABILIZER_ADVANCED.get()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityVisRelay>> VIS_RELAY =
            BLOCK_ENTITIES.register(
                    "vis_relay",
                    () -> new BlockEntityType<>(BlockEntityVisRelay::new, Set.of(TCBlocks.VIS_RELAY.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityNodeTransducer>> NODE_TRANSDUCER =
            BLOCK_ENTITIES.register(
                    "node_transducer",
                    () -> new BlockEntityType<>(
                            BlockEntityNodeTransducer::new, Set.of(TCBlocks.NODE_TRANSDUCER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityJarNode>> JAR_NODE =
            BLOCK_ENTITIES.register(
                    "jar_node",
                    () -> new BlockEntityType<>(BlockEntityJarNode::new, Set.of(TCBlocks.JAR_NODE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityFocalManipulator>>
            FOCAL_MANIPULATOR = BLOCK_ENTITIES.register(
                    "focal_manipulator",
                    () -> new BlockEntityType<>(
                            BlockEntityFocalManipulator::new, Set.of(TCBlocks.FOCAL_MANIPULATOR.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityManaPod>> MANA_POD =
            BLOCK_ENTITIES.register(
                    "mana_pod",
                    () -> new BlockEntityType<>(BlockEntityManaPod::new, Set.of(TCBlocks.MANA_POD.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityResearchTable>> RESEARCH_TABLE =
            BLOCK_ENTITIES.register(
                    "research_table",
                    () -> new BlockEntityType<>(
                            BlockEntityResearchTable::new, Set.of(TCBlocks.RESEARCH_TABLE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityDeconstructionTable>>
            DECONSTRUCTION_TABLE = BLOCK_ENTITIES.register(
                    "deconstruction_table",
                    () -> new BlockEntityType<>(
                            BlockEntityDeconstructionTable::new, Set.of(TCBlocks.DECONSTRUCTION_TABLE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityArcaneWorkbench>>
            ARCANE_WORKBENCH = BLOCK_ENTITIES.register(
                    "arcane_workbench",
                    () -> new BlockEntityType<>(
                            BlockEntityArcaneWorkbench::new, Set.of(TCBlocks.ARCANE_WORKBENCH.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityCrucible>> CRUCIBLE =
            BLOCK_ENTITIES.register(
                    "crucible",
                    () -> new BlockEntityType<>(BlockEntityCrucible::new, Set.of(TCBlocks.CRUCIBLE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySpa>> SPA =
            BLOCK_ENTITIES.register(
                    "spa", () -> new BlockEntityType<>(BlockEntitySpa::new, Set.of(TCBlocks.SPA.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySmelter>> SMELTER =
            BLOCK_ENTITIES.register(
                    "smelter",
                    () -> new BlockEntityType<>(
                            BlockEntitySmelter::new,
                            Set.of(
                                    TCBlocks.SMELTER_BASIC.get(),
                                    TCBlocks.SMELTER_THAUMIUM.get(),
                                    TCBlocks.SMELTER_VOID.get()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityAlembic>> ALEMBIC =
            BLOCK_ENTITIES.register(
                    "alembic",
                    () -> new BlockEntityType<>(BlockEntityAlembic::new, Set.of(TCBlocks.ALEMBIC.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityBellows>> BELLOWS =
            BLOCK_ENTITIES.register(
                    "bellows",
                    () -> new BlockEntityType<>(BlockEntityBellows::new, Set.of(TCBlocks.BELLOWS.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityJar>> JAR =
            BLOCK_ENTITIES.register(
                    "jar",
                    () -> new BlockEntityType<>(
                            BlockEntityJar::new,
                            BuiltInRegistries.BLOCK.stream()
                                    .filter(BlockJar.class::isInstance)
                                    .collect(Collectors.toUnmodifiableSet()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityJarBrain>> JAR_BRAIN =
            BLOCK_ENTITIES.register(
                    "jar_brain",
                    () -> new BlockEntityType<>(BlockEntityJarBrain::new, Set.of(TCBlocks.JAR_BRAIN.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityThaumatorium>> THAUMATORIUM =
            BLOCK_ENTITIES.register(
                    "thaumatorium",
                    () -> new BlockEntityType<>(
                            BlockEntityThaumatorium::new, Set.of(TCBlocks.THAUMATORIUM.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityThaumatoriumTop>>
            THAUMATORIUM_TOP = BLOCK_ENTITIES.register(
                    "thaumatorium_top",
                    () -> new BlockEntityType<>(
                            BlockEntityThaumatoriumTop::new, Set.of(TCBlocks.THAUMATORIUM_TOP.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityCondenser>> CONDENSER =
            BLOCK_ENTITIES.register(
                    "condenser",
                    () -> new BlockEntityType<>(BlockEntityCondenser::new, Set.of(TCBlocks.CONDENSER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityStabilizer>> STABILIZER =
            BLOCK_ENTITIES.register(
                    "stabilizer",
                    () -> new BlockEntityType<>(BlockEntityStabilizer::new, Set.of(TCBlocks.STABILIZER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityRedstoneRelay>> REDSTONE_RELAY =
            BLOCK_ENTITIES.register(
                    "redstone_relay",
                    () -> new BlockEntityType<>(
                            BlockEntityRedstoneRelay::new, Set.of(TCBlocks.REDSTONE_RELAY.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityVoidSiphon>> VOID_SIPHON =
            BLOCK_ENTITIES.register(
                    "void_siphon",
                    () -> new BlockEntityType<>(BlockEntityVoidSiphon::new, Set.of(TCBlocks.VOID_SIPHON.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEverfullUrn>> EVERFULL_URN =
            BLOCK_ENTITIES.register(
                    "everfull_urn",
                    () -> new BlockEntityType<>(
                            BlockEntityEverfullUrn::new, Set.of(TCBlocks.EVERFULL_URN.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityVisGenerator>> VIS_GENERATOR =
            BLOCK_ENTITIES.register(
                    "vis_generator",
                    () -> new BlockEntityType<>(
                            BlockEntityVisGenerator::new, Set.of(TCBlocks.VIS_GENERATOR.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEssentiaPort>> ESSENTIA_PORT =
            BLOCK_ENTITIES.register(
                    "essentia_port",
                    () -> new BlockEntityType<>(
                            BlockEntityEssentiaPort::new,
                            Set.of(TCBlocks.ESSENTIA_INPUT.get(), TCBlocks.ESSENTIA_OUTPUT.get()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityArcaneEar>> ARCANE_EAR =
            BLOCK_ENTITIES.register(
                    "arcane_ear",
                    () -> new BlockEntityType<>(
                            BlockEntityArcaneEar::new,
                            Set.of(TCBlocks.ARCANE_EAR.get(), TCBlocks.ARCANE_EAR_TOGGLE.get()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLampArcane>> LAMP_ARCANE =
            BLOCK_ENTITIES.register(
                    "lamp_arcane",
                    () -> new BlockEntityType<>(BlockEntityLampArcane::new, Set.of(TCBlocks.LAMP_ARCANE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLampGrowth>> LAMP_GROWTH =
            BLOCK_ENTITIES.register(
                    "lamp_growth",
                    () -> new BlockEntityType<>(BlockEntityLampGrowth::new, Set.of(TCBlocks.LAMP_GROWTH.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLampFertility>> LAMP_FERTILITY =
            BLOCK_ENTITIES.register(
                    "lamp_fertility",
                    () -> new BlockEntityType<>(
                            BlockEntityLampFertility::new, Set.of(TCBlocks.LAMP_FERTILITY.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityCentrifuge>> CENTRIFUGE =
            BLOCK_ENTITIES.register(
                    "centrifuge",
                    () -> new BlockEntityType<>(BlockEntityCentrifuge::new, Set.of(TCBlocks.CENTRIFUGE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityHungryChest>> HUNGRY_CHEST =
            BLOCK_ENTITIES.register(
                    "hungry_chest",
                    () -> new BlockEntityType<>(
                            BlockEntityHungryChest::new, Set.of(TCBlocks.HUNGRY_CHEST.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchAltar>> ELDRITCH_ALTAR =
            BLOCK_ENTITIES.register(
                    "eldritch_altar",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchAltar::new, Set.of(TCBlocks.ELDRITCH_ALTAR.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchObelisk>>
            ELDRITCH_OBELISK = BLOCK_ENTITIES.register(
                    "eldritch_obelisk",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchObelisk::new, Set.of(TCBlocks.ELDRITCH_OBELISK.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchLock>> ELDRITCH_LOCK =
            BLOCK_ENTITIES.register(
                    "eldritch_lock",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchLock::new, Set.of(TCBlocks.ELDRITCH_LOCK.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchCrabSpawner>>
            ELDRITCH_CRAB_SPAWNER = BLOCK_ENTITIES.register(
                    "eldritch_crab_spawner",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchCrabSpawner::new, Set.of(TCBlocks.ELDRITCH_CRAB_SPAWNER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchTrap>> ELDRITCH_TRAP =
            BLOCK_ENTITIES.register(
                    "eldritch_trap",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchTrap::new, Set.of(TCBlocks.ELDRITCH_TRAP.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchPortal>> ELDRITCH_PORTAL =
            BLOCK_ENTITIES.register(
                    "eldritch_portal",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchPortal::new, Set.of(TCBlocks.ELDRITCH_PORTAL.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchCap>> ELDRITCH_CAP =
            BLOCK_ENTITIES.register(
                    "eldritch_cap",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchCap::new, Set.of(TCBlocks.ELDRITCH_CAPSTONE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEldritchNothing>>
            ELDRITCH_NOTHING = BLOCK_ENTITIES.register(
                    "eldritch_nothing",
                    () -> new BlockEntityType<>(
                            BlockEntityEldritchNothing::new, Set.of(TCBlocks.ELDRITCH_NOTHING.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityMirror>> MIRROR =
            BLOCK_ENTITIES.register(
                    "mirror", () -> new BlockEntityType<>(BlockEntityMirror::new, Set.of(TCBlocks.MIRROR.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityMirrorEssentia>> MIRROR_ESSENTIA =
            BLOCK_ENTITIES.register(
                    "mirror_essentia",
                    () -> new BlockEntityType<>(
                            BlockEntityMirrorEssentia::new, Set.of(TCBlocks.MIRROR_ESSENTIA.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityBarrierStone>> BARRIER_STONE =
            BLOCK_ENTITIES.register(
                    "barrier_stone",
                    () -> new BlockEntityType<>(
                            BlockEntityBarrierStone::new, Set.of(TCBlocks.PAVING_STONE_BARRIER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityDioptra>> DIOPTRA =
            BLOCK_ENTITIES.register(
                    "dioptra",
                    () -> new BlockEntityType<>(BlockEntityDioptra::new, Set.of(TCBlocks.DIOPTRA.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityJarVoid>> JAR_VOID =
            BLOCK_ENTITIES.register(
                    "jar_void",
                    () -> new BlockEntityType<>(BlockEntityJarVoid::new, Set.of(TCBlocks.JAR_VOID.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTube>> TUBE =
            BLOCK_ENTITIES.register(
                    "tube", () -> new BlockEntityType<>(BlockEntityTube::new, Set.of(TCBlocks.TUBE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeValve>> TUBE_VALVE =
            BLOCK_ENTITIES.register(
                    "tube_valve",
                    () -> new BlockEntityType<>(BlockEntityTubeValve::new, Set.of(TCBlocks.TUBE_VALVE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeRestrict>> TUBE_RESTRICT =
            BLOCK_ENTITIES.register(
                    "tube_restrict",
                    () -> new BlockEntityType<>(
                            BlockEntityTubeRestrict::new, Set.of(TCBlocks.TUBE_RESTRICT.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeFilter>> TUBE_FILTER =
            BLOCK_ENTITIES.register(
                    "tube_filter",
                    () -> new BlockEntityType<>(BlockEntityTubeFilter::new, Set.of(TCBlocks.TUBE_FILTER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeOneway>> TUBE_ONEWAY =
            BLOCK_ENTITIES.register(
                    "tube_oneway",
                    () -> new BlockEntityType<>(BlockEntityTubeOneway::new, Set.of(TCBlocks.TUBE_ONEWAY.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeBuffer>> TUBE_BUFFER =
            BLOCK_ENTITIES.register(
                    "tube_buffer",
                    () -> new BlockEntityType<>(BlockEntityTubeBuffer::new, Set.of(TCBlocks.TUBE_BUFFER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityNitor>> NITOR =
            BLOCK_ENTITIES.register(
                    "nitor",
                    () -> new BlockEntityType<>(
                            BlockEntityNitor::new,
                            TCBlocks.NITORS.values().stream()
                                    .map(b -> (Block) b.get())
                                    .collect(Collectors.toSet()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityInfernalFurnace>>
            INFERNAL_FURNACE = BLOCK_ENTITIES.register(
                    "infernal_furnace",
                    () -> new BlockEntityType<>(
                            BlockEntityInfernalFurnace::new, Set.of(TCBlocks.INFERNAL_FURNACE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityInfusionMatrix>> INFUSION_MATRIX =
            BLOCK_ENTITIES.register(
                    "infusion_matrix",
                    () -> new BlockEntityType<>(
                            BlockEntityInfusionMatrix::new, Set.of(TCBlocks.INFUSION_MATRIX.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityPedestal>> PEDESTAL =
            BLOCK_ENTITIES.register(
                    "pedestal",
                    () -> new BlockEntityType<>(
                            BlockEntityPedestal::new,
                            Set.of(
                                    TCBlocks.PEDESTAL_ARCANE.get(),
                                    TCBlocks.PEDESTAL_ANCIENT.get(),
                                    TCBlocks.PEDESTAL_ELDRITCH.get()),
                            null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityRechargePedestal>>
            RECHARGE_PEDESTAL = BLOCK_ENTITIES.register(
                    "recharge_pedestal",
                    () -> new BlockEntityType<>(
                            BlockEntityRechargePedestal::new, Set.of(TCBlocks.RECHARGE_PEDESTAL.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityPatternCrafter>> PATTERN_CRAFTER =
            BLOCK_ENTITIES.register(
                    "pattern_crafter",
                    () -> new BlockEntityType<>(
                            BlockEntityPatternCrafter::new, Set.of(TCBlocks.PATTERN_CRAFTER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityPotionSprayer>> POTION_SPRAYER =
            BLOCK_ENTITIES.register(
                    "potion_sprayer",
                    () -> new BlockEntityType<>(
                            BlockEntityPotionSprayer::new, Set.of(TCBlocks.POTION_SPRAYER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLevitator>> LEVITATOR =
            BLOCK_ENTITIES.register(
                    "levitator",
                    () -> new BlockEntityType<>(BlockEntityLevitator::new, Set.of(TCBlocks.LEVITATOR.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityGolemBuilder>> GOLEM_BUILDER =
            BLOCK_ENTITIES.register(
                    "golem_builder",
                    () -> new BlockEntityType<>(
                            BlockEntityGolemBuilder::new, Set.of(TCBlocks.GOLEM_BUILDER.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityArcaneBore>> ARCANE_BORE =
            BLOCK_ENTITIES.register(
                    "arcane_bore",
                    () -> new BlockEntityType<>(BlockEntityArcaneBore::new, Set.of(TCBlocks.ARCANE_BORE.get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityHole>> HOLE =
            BLOCK_ENTITIES.register(
                    "hole", () -> new BlockEntityType<>(BlockEntityHole::new, Set.of(TCBlocks.HOLE.get()), null));

    private TCBlockEntities() {}

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
