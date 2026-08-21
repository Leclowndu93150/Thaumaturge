package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.alchemy.BlockLiquidDeath;
import com.leclowndu93150.thaumaturge.content.aura.BlockRechargePedestal;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNodeStabilizer;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockVisRelay;
import com.leclowndu93150.thaumaturge.content.casters.BlockFocalManipulator;
import com.leclowndu93150.thaumaturge.content.crucible.BlockCrucible;
import com.leclowndu93150.thaumaturge.content.decor.BlockAmber;
import com.leclowndu93150.thaumaturge.content.decor.BlockBarrier;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandle;
import com.leclowndu93150.thaumaturge.content.decor.BlockEffectShock;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotem;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotemCharged;
import com.leclowndu93150.thaumaturge.content.decor.BlockPavingStone;
import com.leclowndu93150.thaumaturge.content.decor.BlockStairsTC;
import com.leclowndu93150.thaumaturge.content.decor.BlockStonePorous;
import com.leclowndu93150.thaumaturge.content.decor.BlockStoneTC;
import com.leclowndu93150.thaumaturge.content.decor.BlockTable;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerWallBlock;
import com.leclowndu93150.thaumaturge.content.device.BlockArcaneEar;
import com.leclowndu93150.thaumaturge.content.device.BlockCondenser;
import com.leclowndu93150.thaumaturge.content.device.BlockCondenserLattice;
import com.leclowndu93150.thaumaturge.content.device.BlockDioptra;
import com.leclowndu93150.thaumaturge.content.device.BlockEverfullUrn;
import com.leclowndu93150.thaumaturge.content.device.BlockHungryChest;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.content.device.BlockLampArcane;
import com.leclowndu93150.thaumaturge.content.device.BlockLampFertility;
import com.leclowndu93150.thaumaturge.content.device.BlockLampGrowth;
import com.leclowndu93150.thaumaturge.content.device.BlockLevitator;
import com.leclowndu93150.thaumaturge.content.device.BlockRedstoneRelay;
import com.leclowndu93150.thaumaturge.content.device.BlockStabilizer;
import com.leclowndu93150.thaumaturge.content.device.BlockVisBattery;
import com.leclowndu93150.thaumaturge.content.device.BlockVisGenerator;
import com.leclowndu93150.thaumaturge.content.device.BlockVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockMirror;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockPatternCrafter;
import com.leclowndu93150.thaumaturge.content.device.sprayer.BlockPotionSprayer;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchAltar;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCap;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchNothing;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchPortal;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchStructure;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchTrap;
import com.leclowndu93150.thaumaturge.content.equipment.BlockEffectGlimmer;
import com.leclowndu93150.thaumaturge.content.essentia.BlockCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEssentiaPort;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockBellows;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJarBrain;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJarVoid;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelterAux;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelterVent;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockBrainBox;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockThaumatoriumTop;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTube;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeFilter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeOneway;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeRestrict;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeValve;
import com.leclowndu93150.thaumaturge.content.focus.BlockEffectSap;
import com.leclowndu93150.thaumaturge.content.focus.BlockHole;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockPlaceholder;
import com.leclowndu93150.thaumaturge.content.infusion.BlockInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPedestal;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPillar;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.metal.BlockMetalTC;
import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockNitor;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.content.spa.BlockPurifyingFluid;
import com.leclowndu93150.thaumaturge.content.spa.BlockSpa;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintCrust;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFeature;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintGeyser;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintLog;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintRock;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSoil;
import com.leclowndu93150.thaumaturge.content.taint.flux.BlockFluxGoo;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooRefs;
import com.leclowndu93150.thaumaturge.content.workbench.BlockArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.BlockArcaneWorkbenchCharger;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.content.world.mound.BlockLoot;
import com.leclowndu93150.thaumaturge.content.world.plant.BlockGrassAmbient;
import com.leclowndu93150.thaumaturge.content.world.plant.BlockPlantCinderpearl;
import com.leclowndu93150.thaumaturge.content.world.plant.BlockPlantShimmerleaf;
import com.leclowndu93150.thaumaturge.content.world.plant.BlockPlantVishroom;
import com.leclowndu93150.thaumaturge.content.world.tree.BlockSaplingTC;
import com.leclowndu93150.thaumaturge.content.world.tree.TCTreeGrowers;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TCIds.MODID);

    public static final DeferredBlock<BlockFocalManipulator> FOCAL_MANIPULATOR = BLOCKS.registerBlock(
            "focal_manipulator",
            BlockFocalManipulator::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<BlockResearchTable> RESEARCH_TABLE = BLOCKS.registerBlock(
            "research_table",
            BlockResearchTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<BlockDeconstructionTable> DECONSTRUCTION_TABLE = BLOCKS.registerBlock(
            "deconstruction_table",
            BlockDeconstructionTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<BlockArcaneWorkbench> ARCANE_WORKBENCH = BLOCKS.registerBlock(
            "arcane_workbench",
            BlockArcaneWorkbench::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<BlockCrucible> CRUCIBLE = BLOCKS.registerBlock(
            "crucible",
            BlockCrucible::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final DeferredBlock<BlockArcaneWorkbenchCharger> ARCANE_WORKBENCH_CHARGER = BLOCKS.registerBlock(
            "arcane_workbench_charger",
            BlockArcaneWorkbenchCharger::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.25F, 10.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockAlembic> ALEMBIC = BLOCKS.registerBlock(
            "alembic",
            BlockAlembic::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2F, 20.0F)
                    .sound(SoundType.WOOD)
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<BlockBellows> BELLOWS = BLOCKS.registerBlock(
            "bellows",
            BlockBellows::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1F, 20.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<BlockSmelter> SMELTER_BASIC = BLOCKS.registerBlock(
            "smelter_basic",
            BlockSmelter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final DeferredBlock<BlockSmelter> SMELTER_THAUMIUM = BLOCKS.registerBlock(
            "smelter_thaumium",
            BlockSmelter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final DeferredBlock<BlockSmelter> SMELTER_VOID = BLOCKS.registerBlock(
            "smelter_void",
            BlockSmelter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final DeferredBlock<BlockSmelterAux> SMELTER_AUX = BLOCKS.registerBlock(
            "smelter_aux",
            BlockSmelterAux::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockSmelterVent> SMELTER_VENT = BLOCKS.registerBlock(
            "smelter_vent",
            BlockSmelterVent::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockJar> JAR_NORMAL = BLOCKS.registerBlock(
            "jar_normal",
            BlockJar::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TCSoundTypes.JAR)
                    .noOcclusion());

    public static final DeferredBlock<BlockJarVoid> JAR_VOID = BLOCKS.registerBlock(
            "jar_void",
            BlockJarVoid::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TCSoundTypes.JAR)
                    .noOcclusion());

    public static final DeferredBlock<BlockTube> TUBE = BLOCKS.registerBlock("tube", BlockTube::new, tubeProps());

    public static final DeferredBlock<BlockTubeValve> TUBE_VALVE =
            BLOCKS.registerBlock("tube_valve", BlockTubeValve::new, tubeProps());

    public static final DeferredBlock<BlockTubeRestrict> TUBE_RESTRICT =
            BLOCKS.registerBlock("tube_restrict", BlockTubeRestrict::new, tubeProps());

    public static final DeferredBlock<BlockTubeFilter> TUBE_FILTER =
            BLOCKS.registerBlock("tube_filter", BlockTubeFilter::new, tubeProps());

    public static final DeferredBlock<BlockTubeOneway> TUBE_ONEWAY =
            BLOCKS.registerBlock("tube_oneway", BlockTubeOneway::new, tubeProps());

    public static final DeferredBlock<BlockTubeBuffer> TUBE_BUFFER =
            BLOCKS.registerBlock("tube_buffer", BlockTubeBuffer::new, tubeProps());

    public static final DeferredBlock<BlockFluxGoo> FLUX_GOO = BLOCKS.registerBlock(
            "flux_goo",
            props -> new BlockFluxGoo(FluxGooRefs.sourceFluid(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .sound(TCSoundTypes.GORE)
                    .noLootTable()
                    .liquid()
                    .randomTicks());

    public static final DeferredBlock<BlockPurifyingFluid> PURIFYING_FLUID = BLOCKS.registerBlock(
            "purifying_fluid",
            props -> new BlockPurifyingFluid(TCFluids.PURIFYING_SOURCE.get(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> 5)
                    .noLootTable()
                    .liquid());

    public static final DeferredBlock<BlockLiquidDeath> LIQUID_DEATH = BLOCKS.registerBlock(
            "liquid_death",
            props -> new BlockLiquidDeath(TCFluids.LIQUID_DEATH_SOURCE.get(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid());

    public static final DeferredBlock<BlockSpa> SPA = BLOCKS.registerBlock(
            "spa",
            BlockSpa::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockTaintRock> TAINT_ROCK =
            BLOCKS.registerBlock("taint_rock", BlockTaintRock::new, taintBlockProps());

    public static final DeferredBlock<BlockTaintSoil> TAINT_SOIL =
            BLOCKS.registerBlock("taint_soil", BlockTaintSoil::new, taintBlockProps());

    public static final DeferredBlock<BlockTaintCrust> TAINT_CRUST =
            BLOCKS.registerBlock("taint_crust", BlockTaintCrust::new, taintBlockProps());

    public static final DeferredBlock<BlockTaintGeyser> TAINT_GEYSER =
            BLOCKS.registerBlock("taint_geyser", BlockTaintGeyser::new, taintBlockProps());

    public static final DeferredBlock<BlockTaintLog> TAINT_LOG = BLOCKS.registerBlock(
            "taint_log",
            BlockTaintLog::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 100.0F)
                    .sound(TCSoundTypes.GORE)
                    .randomTicks()
                    .ignitedByLava());

    public static final DeferredBlock<BlockTaintFeature> TAINT_FEATURE = BLOCKS.registerBlock(
            "taint_feature",
            BlockTaintFeature::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.1F, 0.1F)
                    .sound(TCSoundTypes.GORE)
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks());

    public static final DeferredBlock<BlockTaintFibre> TAINT_FIBRE = BLOCKS.registerBlock(
            "taint_fibre",
            BlockTaintFibre::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0F)
                    .sound(TCSoundTypes.GORE)
                    .noOcclusion()
                    .noCollission()
                    .replaceable()
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .lightLevel(s -> {
                        if (s.getValue(BlockTaintFibre.GROWTH3)) return 12;
                        if (s.getValue(BlockTaintFibre.GROWTH2) || s.getValue(BlockTaintFibre.GROWTH4)) return 6;
                        return 0;
                    }));

    private static BlockBehaviour.Properties pressPlaceholderProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.5F, 3600000.0F)
                .sound(SoundType.STONE)
                .noLootTable();
    }

    private static BlockBehaviour.Properties pedestalProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 17.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties pillarProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 17.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties amberProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(0.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties taintBlockProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(10.0F, 100.0F)
                .sound(TCSoundTypes.GORE)
                .noOcclusion()
                .randomTicks();
    }

    private static BlockBehaviour.Properties tubeProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 5.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    //

    public static final DeferredBlock<Block> ORE_AMBER = BLOCKS.registerBlock(
            "ore_amber",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ORE_CINNABAR = BLOCKS.registerBlock(
            "ore_cinnabar",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ORE_QUARTZ = BLOCKS.registerBlock(
            "ore_quartz",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final Map<DyeColor, DeferredBlock<BlockNitor>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(
                    dye,
                    BLOCKS.registerBlock(
                            "nitor_" + dye.getName(), props -> new BlockNitor(dye, props), nitorProps(dye)));
        }
    }

    public static final Map<DyeColor, DeferredBlock<BlockCandle>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(
                    dye,
                    BLOCKS.registerBlock(
                            "candle_" + dye.getName(), props -> new BlockCandle(dye, props), candleProps(dye)));
        }
    }

    public static final Map<DyeColor, DeferredBlock<BannerStandingBlock>> BANNERS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<BannerWallBlock>> WALL_BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(
                    dye,
                    BLOCKS.registerBlock(
                            "banner_" + dye.getName(), props -> new BannerStandingBlock(dye, props), bannerProps(dye)));
            WALL_BANNERS.put(
                    dye,
                    BLOCKS.registerBlock(
                            "wall_banner_" + dye.getName(),
                            props -> new BannerWallBlock(dye, props),
                            bannerProps(dye)));
        }
    }

    public static final DeferredBlock<BannerStandingBlock> BANNER_CRIMSON_CULT = BLOCKS.registerBlock(
            "banner_crimson_cult", props -> new BannerStandingBlock(null, props), bannerProps(null));

    public static final DeferredBlock<BannerWallBlock> WALL_BANNER_CRIMSON_CULT = BLOCKS.registerBlock(
            "wall_banner_crimson_cult", props -> new BannerWallBlock(null, props), bannerProps(null));

    private static BlockBehaviour.Properties bannerProps(DyeColor dye) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion();
        return dye == null ? props.mapColor(MapColor.COLOR_RED) : props.mapColor(dye.getMapColor());
    }

    private static BlockBehaviour.Properties candleProps(DyeColor dye) {
        return BlockBehaviour.Properties.of()
                .mapColor(dye.getMapColor())
                .strength(0.1F)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 14)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties nitorProps(DyeColor dye) {
        return BlockBehaviour.Properties.of()
                .mapColor(dye.getMapColor())
                .strength(0.1F)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .noCollission()
                .pushReaction(PushReaction.DESTROY);
    }

    //

    public static final DeferredBlock<BlockCrystal> CRYSTAL_AER = registerCrystal("crystal_aer", TCAspects.AER, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_IGNIS =
            registerCrystal("crystal_ignis", TCAspects.IGNIS, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_AQUA =
            registerCrystal("crystal_aqua", TCAspects.AQUA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_TERRA =
            registerCrystal("crystal_terra", TCAspects.TERRA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_ORDO =
            registerCrystal("crystal_ordo", TCAspects.ORDO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_PERDITIO =
            registerCrystal("crystal_perditio", TCAspects.PERDITIO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_VITIUM =
            registerCrystal("crystal_vitium", TCAspects.VITIUM, true);

    private static DeferredBlock<BlockCrystal> registerCrystal(String name, ResourceKey<IAspect> aspect, boolean flux) {
        return BLOCKS.registerBlock(
                name,
                props -> new BlockCrystal(props, aspect, flux),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.NONE)
                        .strength(0.25F)
                        .sound(TCSoundTypes.CRYSTAL)
                        .lightLevel(state -> 1)
                        .noOcclusion()
                        .randomTicks()
                        .pushReaction(PushReaction.DESTROY));
    }

    //

    public static final DeferredBlock<BlockInfusionMatrix> INFUSION_MATRIX = BLOCKS.registerBlock(
            "infusion_matrix",
            BlockInfusionMatrix::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(s -> 15));

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ARCANE =
            BLOCKS.registerBlock("pedestal_arcane", BlockPedestal::new, pedestalProps());

    public static final DeferredBlock<BlockRechargePedestal> RECHARGE_PEDESTAL =
            BLOCKS.registerBlock("recharge_pedestal", BlockRechargePedestal::new, pedestalProps());

    public static final DeferredBlock<BlockInlay> INLAY = BLOCKS.registerBlock(
            "inlay",
            BlockInlay::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noCollission()
                    .lightLevel(state -> 1));

    public static final DeferredBlock<BlockPatternCrafter> PATTERN_CRAFTER = BLOCKS.registerBlock(
            "pattern_crafter",
            BlockPatternCrafter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false));

    public static final DeferredBlock<BlockPotionSprayer> POTION_SPRAYER = BLOCKS.registerBlock(
            "potion_sprayer",
            BlockPotionSprayer::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.METAL));

    public static final DeferredBlock<BlockLevitator> LEVITATOR = BLOCKS.registerBlock(
            "levitator",
            BlockLevitator::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<BlockGolemBuilder> GOLEM_BUILDER = BLOCKS.registerBlock(
            "golem_builder",
            BlockGolemBuilder::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_IRON_BARS =
            BLOCKS.registerBlock("placeholder_iron_bars", BlockPlaceholder::new, pressPlaceholderProps());

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_CAULDRON =
            BLOCKS.registerBlock("placeholder_cauldron", BlockPlaceholder::new, pressPlaceholderProps());

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_ANVIL =
            BLOCKS.registerBlock("placeholder_anvil", BlockPlaceholder::new, pressPlaceholderProps());

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_TABLE =
            BLOCKS.registerBlock("placeholder_table", BlockPlaceholder::new, pressPlaceholderProps());

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ANCIENT =
            BLOCKS.registerBlock("pedestal_ancient", BlockPedestal::new, pedestalProps());

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ELDRITCH =
            BLOCKS.registerBlock("pedestal_eldritch", BlockPedestal::new, pedestalProps());

    public static final DeferredBlock<BlockPillar> PILLAR_ARCANE =
            BLOCKS.registerBlock("pillar_arcane", BlockPillar::new, pillarProps());

    public static final DeferredBlock<BlockPillar> PILLAR_ANCIENT =
            BLOCKS.registerBlock("pillar_ancient", BlockPillar::new, pillarProps());

    public static final DeferredBlock<BlockPillar> PILLAR_ELDRITCH =
            BLOCKS.registerBlock("pillar_eldritch", BlockPillar::new, pillarProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ARCANE =
            BLOCKS.registerBlock("stone_arcane", props -> new BlockStoneTC(props, false), stoneProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ARCANE_BRICK =
            BLOCKS.registerBlock("stone_arcane_brick", props -> new BlockStoneTC(props, false), stoneProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT =
            BLOCKS.registerBlock("stone_ancient", props -> new BlockStoneTC(props, false), stoneProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_TILE =
            BLOCKS.registerBlock("stone_ancient_tile", props -> new BlockStoneTC(props, false), stoneProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_ROCK =
            BLOCKS.registerBlock("stone_ancient_rock", props -> new BlockStoneTC(props, true), unbreakableProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_GLYPHED =
            BLOCKS.registerBlock("stone_ancient_glyphed", props -> new BlockStoneTC(props, false), stoneProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_DOORWAY =
            BLOCKS.registerBlock("stone_ancient_doorway", props -> new BlockStoneTC(props, true), unbreakableProps());

    public static final DeferredBlock<BlockStoneTC> STONE_ELDRITCH_TILE =
            BLOCKS.registerBlock("stone_eldritch_tile", props -> new BlockStoneTC(props, false), eldritchTileProps());

    public static final DeferredBlock<BlockStonePorous> STONE_POROUS =
            BLOCKS.registerBlock("stone_porous", BlockStonePorous::new, porousProps());

    public static final DeferredBlock<BlockStairsTC> STAIRS_ARCANE = BLOCKS.registerBlock(
            "stairs_arcane", props -> new BlockStairsTC(STONE_ARCANE.get().defaultBlockState(), props), stoneProps());

    public static final DeferredBlock<BlockStairsTC> STAIRS_ARCANE_BRICK = BLOCKS.registerBlock(
            "stairs_arcane_brick",
            props -> new BlockStairsTC(STONE_ARCANE_BRICK.get().defaultBlockState(), props),
            stoneProps());

    public static final DeferredBlock<BlockStairsTC> STAIRS_ANCIENT = BLOCKS.registerBlock(
            "stairs_ancient", props -> new BlockStairsTC(STONE_ANCIENT.get().defaultBlockState(), props), stoneProps());

    public static final DeferredBlock<BlockStoneTC> MATRIX_SPEED =
            BLOCKS.registerBlock("matrix_speed", props -> new BlockStoneTC(props, false), stoneProps());

    public static final DeferredBlock<BlockStoneTC> MATRIX_COST =
            BLOCKS.registerBlock("matrix_cost", props -> new BlockStoneTC(props, false), stoneProps());

    public static final DeferredBlock<BlockVisBattery> VIS_BATTERY = BLOCKS.registerBlock(
            "vis_battery",
            BlockVisBattery::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.5F)
                    .sound(SoundType.STONE)
                    .randomTicks()
                    .lightLevel(state -> state.getValue(BlockVisBattery.CHARGE)));

    public static final DeferredBlock<BlockDioptra> DIOPTRA = BLOCKS.registerBlock(
            "dioptra",
            BlockDioptra::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<BlockJarBrain> JAR_BRAIN = BLOCKS.registerBlock(
            "jar_brain",
            BlockJarBrain::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TCSoundTypes.JAR)
                    .noOcclusion());

    public static final DeferredBlock<BlockArcaneEar> ARCANE_EAR =
            BLOCKS.registerBlock("arcane_ear", props -> new BlockArcaneEar(false, props), earProps());

    public static final DeferredBlock<BlockArcaneEar> ARCANE_EAR_TOGGLE =
            BLOCKS.registerBlock("arcane_ear_toggle", props -> new BlockArcaneEar(true, props), earProps());

    public static final DeferredBlock<BlockLampArcane> LAMP_ARCANE =
            BLOCKS.registerBlock("lamp_arcane", BlockLampArcane::new, lampProps());

    public static final DeferredBlock<BlockLampGrowth> LAMP_GROWTH =
            BLOCKS.registerBlock("lamp_growth", BlockLampGrowth::new, lampProps());

    public static final DeferredBlock<BlockLampFertility> LAMP_FERTILITY =
            BLOCKS.registerBlock("lamp_fertility", BlockLampFertility::new, lampProps());

    public static final DeferredBlock<BlockCentrifuge> CENTRIFUGE = BLOCKS.registerBlock(
            "centrifuge",
            BlockCentrifuge::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final DeferredBlock<BlockHungryChest> HUNGRY_CHEST = BLOCKS.registerBlock(
            "hungry_chest",
            BlockHungryChest::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<BlockEverfullUrn> EVERFULL_URN = BLOCKS.registerBlock(
            "everfull_urn",
            BlockEverfullUrn::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<BlockVisGenerator> VIS_GENERATOR = BLOCKS.registerBlock(
            "vis_generator",
            BlockVisGenerator::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<BlockEssentiaPort> ESSENTIA_INPUT =
            BLOCKS.registerBlock("essentia_input", props -> new BlockEssentiaPort(true, props), portProps());

    public static final DeferredBlock<BlockEssentiaPort> ESSENTIA_OUTPUT =
            BLOCKS.registerBlock("essentia_output", props -> new BlockEssentiaPort(false, props), portProps());

    public static final DeferredBlock<BlockCondenser> CONDENSER = BLOCKS.registerBlock(
            "condenser",
            BlockCondenser::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final DeferredBlock<BlockCondenserLattice> CONDENSER_LATTICE = BLOCKS.registerBlock(
            "condenser_lattice",
            props -> new BlockCondenserLattice(false, props),
            latticeProps().lightLevel(state -> 5));

    public static final DeferredBlock<BlockCondenserLattice> CONDENSER_LATTICE_DIRTY = BLOCKS.registerBlock(
            "condenser_lattice_dirty", props -> new BlockCondenserLattice(true, props), latticeProps());

    public static final DeferredBlock<BlockStabilizer> STABILIZER = BLOCKS.registerBlock(
            "stabilizer", BlockStabilizer::new, stoneProps().noOcclusion());

    public static final DeferredBlock<BlockRedstoneRelay> REDSTONE_RELAY = BLOCKS.registerBlock(
            "redstone_relay",
            BlockRedstoneRelay::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instabreak()
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<BlockVoidSiphon> VOID_SIPHON = BLOCKS.registerBlock(
            "void_siphon",
            BlockVoidSiphon::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final DeferredBlock<BlockThaumatorium> THAUMATORIUM = BLOCKS.registerBlock(
            "thaumatorium",
            BlockThaumatorium::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final DeferredBlock<BlockThaumatoriumTop> THAUMATORIUM_TOP = BLOCKS.registerBlock(
            "thaumatorium_top",
            BlockThaumatoriumTop::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noLootTable());

    public static final DeferredBlock<BlockBrainBox> BRAIN_BOX = BLOCKS.registerBlock(
            "brain_box",
            BlockBrainBox::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    private static BlockBehaviour.Properties latticeProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 5.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties portProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties earProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties lampProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .lightLevel(state -> state.getValue(BlockStateProperties.ENABLED) ? 15 : 0);
    }

    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties unbreakableProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties eldritchTileProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 1000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 12)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties porousProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    //

    public static final DeferredBlock<BlockSaplingTC> SAPLING_GREATWOOD = BLOCKS.registerBlock(
            "sapling_greatwood",
            props -> new BlockSaplingTC(TCTreeGrowers.GREATWOOD, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockSaplingTC> SAPLING_SILVERWOOD = BLOCKS.registerBlock(
            "sapling_silverwood",
            props -> new BlockSaplingTC(TCTreeGrowers.SILVERWOOD, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<RotatedPillarBlock> LOG_GREATWOOD = BLOCKS.registerBlock(
            "log_greatwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> WOOD_GREATWOOD = BLOCKS.registerBlock(
            "greatwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_LOG_GREATWOOD = BLOCKS.registerBlock(
            "stripped_log_greatwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WOOD_GREATWOOD = BLOCKS.registerBlock(
            "stripped_greatwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> LOG_SILVERWOOD = BLOCKS.registerBlock(
            "log_silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> WOOD_SILVERWOOD = BLOCKS.registerBlock(
            "silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_LOG_SILVERWOOD = BLOCKS.registerBlock(
            "stripped_log_silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WOOD_SILVERWOOD = BLOCKS.registerBlock(
            "stripped_silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final DeferredBlock<LeavesBlock> LEAVES_GREATWOOD =
            BLOCKS.registerBlock("leaves_greatwood", LeavesBlock::new, leavesProps());

    public static final DeferredBlock<LeavesBlock> LEAVES_SILVERWOOD = BLOCKS.registerBlock(
            "leaves_silverwood", LeavesBlock::new, leavesProps().mapColor(MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<PoweredRailBlock> ACTIVATOR_RAIL = BLOCKS.registerBlock(
            "activator_rail",
            PoweredRailBlock::new,
            BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));

    public static final DeferredBlock<Block> PLANK_GREATWOOD = BLOCKS.registerBlock(
            "plank_greatwood",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<Block> PLANK_SILVERWOOD = BLOCKS.registerBlock(
            "plank_silverwood",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    private static BlockBehaviour.Properties leavesProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    //

    public static final DeferredBlock<BlockPlantShimmerleaf> PLANT_SHIMMERLEAF = BLOCKS.registerBlock(
            "shimmerleaf",
            BlockPlantShimmerleaf::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion());

    public static final DeferredBlock<BlockPlantCinderpearl> PLANT_CINDERPEARL = BLOCKS.registerBlock(
            "cinderpearl",
            BlockPlantCinderpearl::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 8)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion());

    public static final DeferredBlock<BlockPlantVishroom> PLANT_VISHROOM = BLOCKS.registerBlock(
            "vishroom",
            BlockPlantVishroom::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion());

    public static final DeferredBlock<BlockGrassAmbient> GRASS_AMBIENT = BLOCKS.registerBlock(
            "grass_ambient",
            BlockGrassAmbient::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .randomTicks());

    //

    public static final DeferredBlock<BlockMetalTC> ALCHEMICAL_CONSTRUCT = BLOCKS.registerBlock(
            "alchemical_construct",
            BlockMetalTC::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTC> ADVANCED_ALCHEMICAL_CONSTRUCT = BLOCKS.registerBlock(
            "advanced_alchemical_construct",
            BlockMetalTC::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTC> METAL_THAUMIUM_BLOCK = BLOCKS.registerBlock(
            "metal_thaumium",
            BlockMetalTC::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTC> METAL_BRASS_BLOCK = BLOCKS.registerBlock(
            "metal_brass",
            BlockMetalTC::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTC> METAL_VOID_BLOCK = BLOCKS.registerBlock(
            "metal_void",
            BlockMetalTC::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_GREATWOOD = BLOCKS.registerBlock(
            "slab_greatwood",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.2F, 2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<SlabBlock> SLAB_SILVERWOOD = BLOCKS.registerBlock(
            "slab_silverwood",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(1.0F, 2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<SlabBlock> SLAB_ARCANE_STONE = BLOCKS.registerBlock(
            "slab_arcane_stone",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_ARCANE_BRICK = BLOCKS.registerBlock(
            "slab_arcane_brick",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_ANCIENT = BLOCKS.registerBlock(
            "slab_ancient",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_ELDRITCH = BLOCKS.registerBlock(
            "slab_eldritch",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockStairsTC> STAIRS_GREATWOOD = BLOCKS.registerBlock(
            "stairs_greatwood",
            props -> new BlockStairsTC(PLANK_GREATWOOD.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<BlockStairsTC> STAIRS_SILVERWOOD = BLOCKS.registerBlock(
            "stairs_silverwood",
            props -> new BlockStairsTC(PLANK_SILVERWOOD.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<BlockTable> TABLE_WOOD = BLOCKS.registerBlock(
            "table_wood",
            BlockTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .ignitedByLava());

    public static final DeferredBlock<BlockTable> TABLE_STONE = BLOCKS.registerBlock(
            "table_stone",
            BlockTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<BlockPavingStone> PAVING_STONE_TRAVEL = BLOCKS.registerBlock(
            "paving_stone_travel",
            props -> new BlockPavingStone(false, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<BlockPavingStone> PAVING_STONE_BARRIER = BLOCKS.registerBlock(
            "paving_stone_barrier",
            props -> new BlockPavingStone(true, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<BlockBarrier> BARRIER = BLOCKS.registerBlock(
            "barrier",
            BlockBarrier::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 999.0F)
                    .noOcclusion()
                    .noLootTable()
                    .dynamicShape()
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockManaPod> MANA_POD = BLOCKS.registerBlock(
            "mana_pod",
            BlockManaPod::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.5F)
                    .sound(SoundType.CROP)
                    .noOcclusion()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(BlockManaPod.AGE))
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockNode> NODE = BLOCKS.registerBlock(
            "node",
            BlockNode::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockJarNode> JAR_NODE = BLOCKS.registerBlock(
            "jar_node",
            BlockJarNode::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TCSoundTypes.JAR)
                    .noOcclusion());

    public static final DeferredBlock<BlockNodeStabilizer> NODE_STABILIZER = BLOCKS.registerBlock(
            "node_stabilizer",
            props -> new BlockNodeStabilizer(props, false),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .noOcclusion());

    public static final DeferredBlock<BlockNodeStabilizer> NODE_STABILIZER_ADVANCED = BLOCKS.registerBlock(
            "node_stabilizer_advanced",
            props -> new BlockNodeStabilizer(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .noOcclusion());

    public static final DeferredBlock<BlockVisRelay> VIS_RELAY = BLOCKS.registerBlock(
            "vis_relay",
            BlockVisRelay::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F)
                    .noOcclusion()
                    .sound(SoundType.AMETHYST));

    public static final DeferredBlock<BlockNodeTransducer> NODE_TRANSDUCER = BLOCKS.registerBlock(
            "node_transducer",
            BlockNodeTransducer::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .noOcclusion());

    public static final DeferredBlock<BlockAmber> AMBER_BRICK =
            BLOCKS.registerBlock("amber_brick", BlockAmber::new, amberProps());

    public static final DeferredBlock<Block> FLESH_BLOCK = BLOCKS.registerBlock(
            "flesh_block",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.25F, 2.0F)
                    .sound(TCSoundTypes.GORE));

    public static final DeferredBlock<BlockEffectShock> EFFECT_SHOCK = BLOCKS.registerBlock(
            "effect_shock",
            BlockEffectShock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(0.0F, 999.0F)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 7)
                    .randomTicks()
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> OBSIDIAN_TILE = BLOCKS.registerBlock(
            "obsidian_tile",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockObsidianTotem> OBSIDIAN_TOTEM = BLOCKS.registerBlock(
            "obsidian_totem",
            BlockObsidianTotem::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockObsidianTotemCharged> OBSIDIAN_TOTEM_CHARGED = BLOCKS.registerBlock(
            "obsidian_totem_charged",
            BlockObsidianTotemCharged::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ELDRITCH_STONE = BLOCKS.registerBlock(
            "eldritch_stone",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ELDRITCH_STONE_INERT = BLOCKS.registerBlock(
            "eldritch_stone_inert",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<Block> ELDRITCH_ROCK = BLOCKS.registerBlock(
            "eldritch_rock",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ELDRITCH_CRUST = BLOCKS.registerBlock(
            "eldritch_crust",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(TCSoundTypes.GORE));

    public static final DeferredBlock<BlockEldritchInset> ELDRITCH_CRUST_GLOWING = BLOCKS.registerBlock(
            "eldritch_crust_glowing",
            props -> new BlockEldritchInset(ConstantInt.of(0), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 30.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12)
                    .noOcclusion());

    public static final DeferredBlock<BlockMirror> MIRROR = BLOCKS.registerBlock(
            "mirror",
            props -> new BlockMirror(props, false),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.1F)
                    .sound(TCSoundTypes.JAR)
                    .noOcclusion());

    public static final DeferredBlock<BlockMirror> MIRROR_ESSENTIA = BLOCKS.registerBlock(
            "mirror_essentia",
            props -> new BlockMirror(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.1F)
                    .sound(TCSoundTypes.JAR)
                    .noOcclusion());

    public static final DeferredBlock<BlockStairsTC> STAIRS_ELDRITCH = BLOCKS.registerBlock(
            "stairs_eldritch",
            props -> new BlockStairsTC(ELDRITCH_STONE.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ELDRITCH_DOOR = BLOCKS.registerBlock(
            "eldritch_door",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, Float.MAX_VALUE)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12));

    public static final DeferredBlock<Block> ELDRITCH_PEDESTAL = BLOCKS.registerBlock(
            "eldritch_pedestal",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockEldritchInset> ELDRITCH_STONE_CRYSTAL = BLOCKS.registerBlock(
            "eldritch_stone_crystal",
            props -> new BlockEldritchInset(UniformInt.of(1, 4), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 30.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12)
                    .noOcclusion());

    public static final DeferredBlock<BlockEldritchNothing> ELDRITCH_NOTHING = BLOCKS.registerBlock(
            "eldritch_nothing",
            BlockEldritchNothing::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 6000000.0F)
                    .sound(SoundType.WOOL)
                    .lightLevel(state -> 3)
                    .noOcclusion()
                    .noLootTable()
                    .dynamicShape());

    public static final DeferredBlock<BlockEldritchLock> ELDRITCH_LOCK = BLOCKS.registerBlock(
            "eldritch_lock",
            BlockEldritchLock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, Float.MAX_VALUE)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 5)
                    .noLootTable());

    public static final DeferredBlock<BlockEldritchCrabSpawner> ELDRITCH_CRAB_SPAWNER = BLOCKS.registerBlock(
            "eldritch_crab_spawner",
            BlockEldritchCrabSpawner::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(7.0F, 20.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 4)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockEldritchTrap> ELDRITCH_TRAP = BLOCKS.registerBlock(
            "eldritch_trap",
            BlockEldritchTrap::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(15.0F, 30.0F)
                    .sound(SoundType.STONE)
                    .noLootTable());

    public static final DeferredBlock<BlockEldritchAltar> ELDRITCH_ALTAR = BLOCKS.registerBlock(
            "eldritch_altar",
            BlockEldritchAltar::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12)
                    .noOcclusion()
                    .noLootTable());

    public static final DeferredBlock<BlockEldritchObelisk> ELDRITCH_OBELISK = BLOCKS.registerBlock(
            "eldritch_obelisk",
            BlockEldritchObelisk::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 8)
                    .noOcclusion()
                    .noLootTable());

    public static final DeferredBlock<BlockEldritchStructure> ELDRITCH_PILLAR = BLOCKS.registerBlock(
            "eldritch_pillar",
            BlockEldritchStructure::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 8)
                    .noOcclusion()
                    .noLootTable());

    public static final DeferredBlock<BlockEldritchCap> ELDRITCH_CAPSTONE = BLOCKS.registerBlock(
            "eldritch_capstone",
            BlockEldritchCap::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 8)
                    .noOcclusion()
                    .noLootTable());

    public static final DeferredBlock<BlockEldritchPortal> ELDRITCH_PORTAL = BLOCKS.registerBlock(
            "eldritch_portal",
            BlockEldritchPortal::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 200000.0F)
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .noLootTable()
                    .noCollission());

    public static final DeferredBlock<BlockAmber> AMBER_BLOCK =
            BLOCKS.registerBlock("amber_block", BlockAmber::new, amberProps());

    public static final DeferredBlock<BlockPlaceholder> OBSIDIAN_PLACEHOLDER = BLOCKS.registerBlock(
            "placeholder_obsidian",
            props -> new BlockPlaceholder(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockPlaceholder> NETHER_BRICKS_PLACEHOLDER = BLOCKS.registerBlock(
            "placeholder_nether_bricks",
            props -> new BlockPlaceholder(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockInfernalFurnace> INFERNAL_FURNACE = BLOCKS.registerBlock(
            "infernal_furnace",
            BlockInfernalFurnace::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .noLootTable()
                    .lightLevel(state -> 13));

    public static final DeferredBlock<BlockHole> HOLE = BLOCKS.registerBlock(
            "hole",
            BlockHole::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, 6000000.0F)
                    .sound(SoundType.WOOL)
                    .lightLevel(state -> 10)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<BlockEffectSap> EFFECT_SAP = BLOCKS.registerBlock(
            "effect_sap",
            BlockEffectSap::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.0F, 999.0F)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 7)
                    .randomTicks()
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockEffectGlimmer> EFFECT_GLIMMER = BLOCKS.registerBlock(
            "effect_glimmer",
            BlockEffectGlimmer::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.0F, 999.0F)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 15)
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockLoot> LOOT_URN_COMMON =
            lootBlock("loot_urn_common", BlockLoot.LootType.COMMON, false);
    public static final DeferredBlock<BlockLoot> LOOT_URN_UNCOMMON =
            lootBlock("loot_urn_uncommon", BlockLoot.LootType.UNCOMMON, false);
    public static final DeferredBlock<BlockLoot> LOOT_URN_RARE =
            lootBlock("loot_urn_rare", BlockLoot.LootType.RARE, false);
    public static final DeferredBlock<BlockLoot> LOOT_CRATE_COMMON =
            lootBlock("loot_crate_common", BlockLoot.LootType.COMMON, true);
    public static final DeferredBlock<BlockLoot> LOOT_CRATE_UNCOMMON =
            lootBlock("loot_crate_uncommon", BlockLoot.LootType.UNCOMMON, true);
    public static final DeferredBlock<BlockLoot> LOOT_CRATE_RARE =
            lootBlock("loot_crate_rare", BlockLoot.LootType.RARE, true);

    private static DeferredBlock<BlockLoot> lootBlock(String id, BlockLoot.LootType type, boolean crate) {
        return BLOCKS.registerBlock(
                id,
                props -> new BlockLoot(type, crate, props),
                BlockBehaviour.Properties.of()
                        .mapColor(crate ? MapColor.WOOD : MapColor.STONE)
                        .strength(0.15F, 0.0F)
                        .sound(crate ? SoundType.WOOD : TCSoundTypes.URN)
                        .noOcclusion());
    }

    private TCBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
