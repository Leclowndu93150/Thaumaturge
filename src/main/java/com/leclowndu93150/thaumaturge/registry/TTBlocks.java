package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.content.infusion.grindstone.BlockArcaneGrindstone;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.DoorBlock;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.alchemy.BlockLiquidDeath;
import com.leclowndu93150.thaumaturge.content.aura.BlockRechargePedestal;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNodeStabilizer;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockVisRelay;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockFocalManipulator;
import com.leclowndu93150.thaumaturge.content.crucible.BlockCrucible;
import com.leclowndu93150.thaumaturge.content.decor.BlockAmber;
import com.leclowndu93150.thaumaturge.content.decor.BlockBarrier;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandle;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.content.decor.CandleHolderMaterial;
import com.leclowndu93150.thaumaturge.content.decor.BlockEffectShock;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotem;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotemCharged;
import com.leclowndu93150.thaumaturge.content.decor.BlockPavingStone;
import com.leclowndu93150.thaumaturge.content.decor.BlockStairsTT;
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
import com.leclowndu93150.thaumaturge.content.device.levitator.BlockLevitator;
import com.leclowndu93150.thaumaturge.content.device.BlockRedstoneRelay;
import com.leclowndu93150.thaumaturge.content.device.BlockStabilizer;
import com.leclowndu93150.thaumaturge.content.device.BlockVisBattery;
import com.leclowndu93150.thaumaturge.content.device.BlockVisGenerator;
import com.leclowndu93150.thaumaturge.content.device.BlockVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.bore.BlockArcaneBore;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockFluxScrubber;
import com.leclowndu93150.thaumaturge.content.device.grate.BlockItemGrate;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockMirror;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockPatternCrafter;
import com.leclowndu93150.thaumaturge.content.device.sprayer.BlockPotionSprayer;
import com.leclowndu93150.thaumaturge.content.eldritch.altar.BlockEldritchAltar;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCap;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchNothing;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchNothingDormant;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchStructure;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchTrap;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.BlockEldritchPortal;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.BlockEldritchReliquary;
import com.leclowndu93150.thaumaturge.content.equipment.BlockEffectGlimmer;
import com.leclowndu93150.thaumaturge.content.essentia.BlockCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEssentiaPort;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockBellows;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEssentiaCrystalizer;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJarBrain;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJarVoid;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEssentiaReservoir;
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
import com.leclowndu93150.thaumaturge.content.spell.block.BlockEffectSap;
import com.leclowndu93150.thaumaturge.content.spell.block.BlockHole;
import com.leclowndu93150.thaumaturge.content.golem.BlockGolemFetter;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockPlaceholder;
import com.leclowndu93150.thaumaturge.content.infusion.BlockInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPedestal;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPillar;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.metal.BlockMetalTT;
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
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSporeStalk;
import com.leclowndu93150.thaumaturge.content.taint.ecology.BlockEtherealBloom;
import com.leclowndu93150.thaumaturge.content.taint.flux.BlockFluxGas;
import com.leclowndu93150.thaumaturge.content.taint.flux.BlockFluxGoo;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooRefs;
import com.leclowndu93150.thaumaturge.content.warding.BlockArcaneDoor;
import com.leclowndu93150.thaumaturge.content.warding.BlockArcanePressurePlate;
import com.leclowndu93150.thaumaturge.content.warding.BlockWardedGlass;
import com.leclowndu93150.thaumaturge.content.workbench.BlockArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.BlockArcaneWorkbenchCharger;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.content.world.mound.BlockLoot;
import com.leclowndu93150.thaumaturge.content.world.plant.BlockGrassAmbient;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicPlantBlock;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicPlantTraits;
import com.leclowndu93150.thaumaturge.content.world.tree.BlockSaplingTT;
import com.leclowndu93150.thaumaturge.content.world.tree.BlockSilverwoodNodeLog;
import com.leclowndu93150.thaumaturge.content.world.tree.TTTreeGrowers;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTBlocks {
    private static final int WOODEN_BUTTON_PRESS_TICKS = 30;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TTIds.MODID);

    private static final float LEAF_PARTICLE_CHANCE = 0.01F;
    private static final int SILVERWOOD_LEAF_PARTICLE_COLOR = 0xFF4C72AE;

    public static final DeferredBlock<BlockFocalManipulator> FOCAL_MANIPULATOR = BLOCKS.registerBlock("focal_manipulator", BlockFocalManipulator::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 20.0F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockResearchTable> RESEARCH_TABLE = BLOCKS.registerBlock("research_table", BlockResearchTable::new,
            props -> props.mapColor(MapColor.WOOD).strength(1.5F, 2.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<BlockDeconstructionTable> DECONSTRUCTION_TABLE = BLOCKS.registerBlock("deconstruction_table", BlockDeconstructionTable::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockArcaneWorkbench> ARCANE_WORKBENCH = BLOCKS.registerBlock("arcane_workbench", BlockArcaneWorkbench::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockCrucible> CRUCIBLE = BLOCKS.registerBlock("crucible", BlockCrucible::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F, 20.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockArcaneWorkbenchCharger> ARCANE_WORKBENCH_CHARGER = BLOCKS.registerBlock("arcane_workbench_charger", BlockArcaneWorkbenchCharger::new,
            props -> props.mapColor(MapColor.WOOD).strength(1.25F, 10.0F).sound(SoundType.WOOD).noOcclusion().requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockAlembic> ALEMBIC = BLOCKS.registerBlock("alembic", BlockAlembic::new,
            props -> props.mapColor(MapColor.WOOD).strength(2F, 20.0F).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<BlockBellows> BELLOWS = BLOCKS.registerBlock("bellows", BlockBellows::new,
            props -> props.mapColor(MapColor.WOOD).strength(1F, 20.0F).sound(SoundType.WOOD).noOcclusion().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<BlockSmelter> SMELTER_BASIC = BLOCKS.registerBlock("smelter_basic", BlockSmelter::new, props -> props.mapColor(MapColor.METAL).strength(2F, 20.0F)
            .sound(SoundType.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final DeferredBlock<BlockSmelter> SMELTER_THAUMIUM = BLOCKS.registerBlock("smelter_thaumium", BlockSmelter::new, props -> props.mapColor(MapColor.METAL).strength(2F, 20.0F)
            .sound(SoundType.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final DeferredBlock<BlockSmelter> SMELTER_VOID = BLOCKS.registerBlock("smelter_void", BlockSmelter::new, props -> props.mapColor(MapColor.METAL).strength(2F, 20.0F)
            .sound(SoundType.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final DeferredBlock<BlockAdvancedAlchemicalFurnace> ADVANCED_ALCHEMICAL_FURNACE = BLOCKS.registerBlock("advanced_alchemical_furnace", BlockAdvancedAlchemicalFurnace::new,
            props -> props.mapColor(MapColor.METAL).strength(5.0F, 12.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(BlockAdvancedAlchemicalFurnace.LIT) ? 10 : 0));

    public static final DeferredBlock<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER = BLOCKS.registerBlock("advanced_alchemical_furnace_alembic_placeholder", BlockPlaceholder::new,
            TTBlocks::advancedFurnacePlaceholderProps);

    public static final DeferredBlock<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER = BLOCKS.registerBlock("advanced_alchemical_furnace_construct_placeholder",
            BlockPlaceholder::new, TTBlocks::advancedFurnacePlaceholderProps);

    public static final DeferredBlock<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER = BLOCKS.registerBlock("advanced_alchemical_furnace_advanced_construct_placeholder",
            BlockPlaceholder::new, TTBlocks::advancedFurnacePlaceholderProps);

    public static final DeferredBlock<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_NOZZLE = BLOCKS.registerBlock("advanced_alchemical_furnace_nozzle", BlockPlaceholder::new,
            TTBlocks::advancedFurnacePlaceholderProps);

    public static final DeferredBlock<BlockEssentiaCrystalizer> ESSENTIA_CRYSTALIZER = BLOCKS.registerBlock("essentia_crystalizer", BlockEssentiaCrystalizer::new,
            props -> props.mapColor(MapColor.METAL).strength(1.0F, 10.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockEssentiaReservoir> ESSENTIA_RESERVOIR = BLOCKS.registerBlock("essentia_reservoir", BlockEssentiaReservoir::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F, 17.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockFluxScrubber> FLUX_SCRUBBER = BLOCKS.registerBlock("flux_scrubber", BlockFluxScrubber::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockSmelterAux> SMELTER_AUX = BLOCKS.registerBlock("smelter_aux", BlockSmelterAux::new,
            props -> props.mapColor(MapColor.METAL).strength(1F, 20.0F).sound(SoundType.METAL).instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockSmelterVent> SMELTER_VENT = BLOCKS.registerBlock("smelter_vent", BlockSmelterVent::new,
            props -> props.mapColor(MapColor.METAL).strength(1F, 20.0F).sound(SoundType.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockJar> JAR_NORMAL = BLOCKS.registerBlock("jar_normal", BlockJar::new,
            props -> props.mapColor(MapColor.NONE).strength(0.3F).sound(TTSoundTypes.JAR.get()).noOcclusion());

    public static final DeferredBlock<BlockJarVoid> JAR_VOID = BLOCKS.registerBlock("jar_void", BlockJarVoid::new,
            props -> props.mapColor(MapColor.NONE).strength(0.3F).sound(TTSoundTypes.JAR.get()).noOcclusion());

    public static final DeferredBlock<BlockTube> TUBE = BLOCKS.registerBlock("tube", BlockTube::new, TTBlocks::tubeProps);

    public static final DeferredBlock<BlockTubeValve> TUBE_VALVE = BLOCKS.registerBlock("tube_valve", BlockTubeValve::new, TTBlocks::tubeProps);

    public static final DeferredBlock<BlockTubeRestrict> TUBE_RESTRICT = BLOCKS.registerBlock("tube_restrict", BlockTubeRestrict::new, TTBlocks::tubeProps);

    public static final DeferredBlock<BlockTubeFilter> TUBE_FILTER = BLOCKS.registerBlock("tube_filter", BlockTubeFilter::new, TTBlocks::tubeProps);

    public static final DeferredBlock<BlockTubeOneway> TUBE_ONEWAY = BLOCKS.registerBlock("tube_oneway", BlockTubeOneway::new, TTBlocks::tubeProps);

    public static final DeferredBlock<BlockTubeBuffer> TUBE_BUFFER = BLOCKS.registerBlock("tube_buffer", BlockTubeBuffer::new, TTBlocks::tubeProps);

    public static final DeferredBlock<BlockFluxGoo> FLUX_GOO = BLOCKS.registerBlock("flux_goo", props -> new BlockFluxGoo(FluxGooRefs.sourceFluid(), props), props -> props
            .mapColor(MapColor.COLOR_PINK).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.DESTROY).sound(TTSoundTypes.GORE.get()).noLootTable().liquid().randomTicks());

    public static final DeferredBlock<BlockFluxGas> FLUX_GAS = BLOCKS.registerBlock("flux_gas", BlockFluxGas::new, props -> props.mapColor(MapColor.COLOR_PINK).replaceable().noCollision()
            .noOcclusion().strength(100.0F).pushReaction(PushReaction.DESTROY).lightLevel(state -> 7).sound(TTSoundTypes.GORE.get()).noLootTable().randomTicks());

    public static final DeferredBlock<BlockPurifyingFluid> PURIFYING_FLUID = BLOCKS.registerBlock("purifying_fluid", props -> new BlockPurifyingFluid(TTFluids.PURIFYING_SOURCE.get(), props),
            props -> props.mapColor(MapColor.METAL).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.DESTROY).lightLevel(state -> 5).noLootTable().liquid());

    public static final DeferredBlock<BlockLiquidDeath> LIQUID_DEATH = BLOCKS.registerBlock("liquid_death", props -> new BlockLiquidDeath(TTFluids.LIQUID_DEATH_SOURCE.get(), props),
            props -> props.mapColor(MapColor.COLOR_PURPLE).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.DESTROY).noLootTable().liquid());

    public static final DeferredBlock<BlockSpa> SPA = BLOCKS.registerBlock("spa", BlockSpa::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockTaintRock> TAINT_ROCK = BLOCKS.registerBlock("taint_rock", BlockTaintRock::new, TTBlocks::taintBlockProps);

    public static final DeferredBlock<BlockTaintSoil> TAINT_SOIL = BLOCKS.registerBlock("taint_soil", BlockTaintSoil::new, TTBlocks::taintBlockProps);

    public static final DeferredBlock<BlockTaintCrust> TAINT_CRUST = BLOCKS.registerBlock("taint_crust", BlockTaintCrust::new, TTBlocks::taintBlockProps);

    public static final DeferredBlock<BlockTaintGeyser> TAINT_GEYSER = BLOCKS.registerBlock("taint_geyser", BlockTaintGeyser::new, TTBlocks::taintBlockProps);

    public static final DeferredBlock<BlockTaintLog> TAINT_LOG = BLOCKS.registerBlock("taint_log", BlockTaintLog::new,
            props -> props.mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 100.0F).sound(TTSoundTypes.GORE.get()).randomTicks().ignitedByLava());

    public static final DeferredBlock<BlockTaintFeature> TAINT_FEATURE = BLOCKS.registerBlock("taint_feature", BlockTaintFeature::new,
            props -> props.mapColor(MapColor.COLOR_PURPLE).strength(0.1F, 0.1F).sound(TTSoundTypes.GORE.get()).noOcclusion().lightLevel(s -> 10).pushReaction(PushReaction.DESTROY).randomTicks());

    public static final DeferredBlock<BlockTaintFibre> TAINT_FIBRE = BLOCKS.registerBlock("taint_fibre", BlockTaintFibre::new, props -> props.mapColor(MapColor.COLOR_PURPLE).strength(1.0F)
            .sound(TTSoundTypes.GORE.get()).noOcclusion().noCollision().replaceable().pushReaction(PushReaction.DESTROY).randomTicks().lightLevel(s -> {
                if (s.getValue(BlockTaintFibre.GROWTH3))
                    return 12;
                if (s.getValue(BlockTaintFibre.GROWTH2) || s.getValue(BlockTaintFibre.GROWTH4))
                    return 6;
                return 0;
            }));

    public static final DeferredBlock<BlockTaintSporeStalk> TAINT_SPORE_STALK = BLOCKS.registerBlock("taint_spore_stalk", BlockTaintSporeStalk::new,
            props -> props.mapColor(MapColor.COLOR_PURPLE).strength(0.4F).sound(TTSoundTypes.GORE.get()).noOcclusion().noCollision().pushReaction(PushReaction.DESTROY).randomTicks()
                    .lightLevel(state -> state.getValue(BlockTaintSporeStalk.MATURE) ? 10 : 0));

    private static BlockBehaviour.Properties pressPlaceholderProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.STONE).strength(2.5F, 3600000.0F).sound(SoundType.STONE).noOcclusion().dynamicShape();
    }

    private static BlockBehaviour.Properties advancedFurnacePlaceholderProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.METAL).strength(2.5F, 3600000.0F).sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties pedestalProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.STONE).strength(2.0F, 17.5F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties pillarProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.STONE).strength(2.0F, 17.5F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties amberProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.STONE).noOcclusion().isValidSpawn((state, level, pos, entityType) -> false)
                .isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties taintBlockProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.COLOR_PURPLE).strength(10.0F, 100.0F).sound(TTSoundTypes.GORE.get()).noOcclusion().randomTicks();
    }

    private static BlockBehaviour.Properties tubeProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.METAL).strength(0.5F, 5.0F).sound(SoundType.METAL).noOcclusion();
    }

    public static final DeferredBlock<Block> ORE_AMBER = BLOCKS.registerBlock("ore_amber", Block::new,
            props -> props.mapColor(MapColor.STONE).strength(1.5F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ORE_CINNABAR = BLOCKS.registerBlock("ore_cinnabar", Block::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ORE_QUARTZ = BLOCKS.registerBlock("ore_quartz", Block::new,
            props -> props.mapColor(MapColor.STONE).strength(3.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> DEEPSLATE_ORE_AMBER = BLOCKS.registerBlock("deepslate_ore_amber", Block::new,
            props -> props.mapColor(MapColor.DEEPSLATE).strength(3.0F, 5.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> DEEPSLATE_ORE_CINNABAR = BLOCKS.registerBlock("deepslate_ore_cinnabar", Block::new,
            props -> props.mapColor(MapColor.DEEPSLATE).strength(3.5F, 5.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> DEEPSLATE_ORE_QUARTZ = BLOCKS.registerBlock("deepslate_ore_quartz", Block::new,
            props -> props.mapColor(MapColor.DEEPSLATE).strength(4.5F, 5.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());

    public static final Map<DyeColor, DeferredBlock<BlockNitor>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, BLOCKS.registerBlock("nitor_" + dye.getName(), props -> new BlockNitor(dye, props), () -> nitorProps(dye)));
        }
    }

    public static final Map<DyeColor, DeferredBlock<BlockCandle>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(dye, BLOCKS.registerBlock("candle_" + dye.getName(), props -> new BlockCandle(dye, props), () -> candleProps(dye)));
        }
    }

    public static final Map<CandleHolderMaterial, DeferredBlock<BlockCandleHolder>> CANDLE_HOLDERS = new EnumMap<>(CandleHolderMaterial.class);

    static {
        for (CandleHolderMaterial material : CandleHolderMaterial.values()) {
            CANDLE_HOLDERS.put(material, BLOCKS.registerBlock("candle_holder_" + material.getSerializedName(), props -> new BlockCandleHolder(material, props), () -> candleHolderProps(material)));
        }
    }

    public static final Map<DyeColor, DeferredBlock<BannerStandingBlock>> BANNERS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<BannerWallBlock>> WALL_BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(dye, BLOCKS.registerBlock("banner_" + dye.getName(), props -> new BannerStandingBlock(dye, props), () -> bannerProps(dye)));
            WALL_BANNERS.put(dye, BLOCKS.registerBlock("wall_banner_" + dye.getName(), props -> new BannerWallBlock(dye, props), () -> bannerProps(dye)));
        }
    }

    public static final DeferredBlock<BannerStandingBlock> BANNER_CRIMSON_CULT = BLOCKS.registerBlock("banner_crimson_cult", props -> new BannerStandingBlock(null, props), () -> bannerProps(null));

    public static final DeferredBlock<BannerWallBlock> WALL_BANNER_CRIMSON_CULT = BLOCKS.registerBlock("wall_banner_crimson_cult", props -> new BannerWallBlock(null, props), () -> bannerProps(null));

    private static BlockBehaviour.Properties bannerProps(DyeColor dye) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.WOOD).noOcclusion();
        return dye == null ? props.mapColor(MapColor.COLOR_RED) : props.mapColor(dye.getMapColor());
    }

    private static BlockBehaviour.Properties candleProps(DyeColor dye) {
        return BlockBehaviour.Properties.of().mapColor(dye.getMapColor()).strength(0.1F).sound(SoundType.WOOL).lightLevel(state -> 14).noOcclusion();
    }

    private static BlockBehaviour.Properties candleHolderProps(CandleHolderMaterial material) {
        return BlockBehaviour.Properties.of().mapColor(material.mapColor()).strength(material.strength()).sound(SoundType.METAL).lightLevel(BlockCandleHolder::lightEmission).noOcclusion();
    }

    private static BlockBehaviour.Properties nitorProps(DyeColor dye) {
        return BlockBehaviour.Properties.of().mapColor(dye.getMapColor()).strength(0.1F).sound(SoundType.WOOL).lightLevel(state -> 15).noOcclusion().noCollision().pushReaction(PushReaction.DESTROY);
    }

    public static final DeferredBlock<BlockCrystal> CRYSTAL_AER = registerCrystal("crystal_aer", TTAspects.AER, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_IGNIS = registerCrystal("crystal_ignis", TTAspects.IGNIS, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_AQUA = registerCrystal("crystal_aqua", TTAspects.AQUA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_TERRA = registerCrystal("crystal_terra", TTAspects.TERRA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_ORDO = registerCrystal("crystal_ordo", TTAspects.ORDO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_PERDITIO = registerCrystal("crystal_perditio", TTAspects.PERDITIO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_VITIUM = registerCrystal("crystal_vitium", TTAspects.VITIUM, true);

    private static DeferredBlock<BlockCrystal> registerCrystal(String name, ResourceKey<IAspect> aspect, boolean flux) {
        return BLOCKS.registerBlock(name, props -> new BlockCrystal(props, aspect, flux),
                props -> props.mapColor(MapColor.NONE).strength(0.25F).sound(TTSoundTypes.CRYSTAL.get()).lightLevel(state -> 1).noOcclusion().randomTicks().pushReaction(PushReaction.DESTROY));
    }

    public static final DeferredBlock<BlockArcaneGrindstone> ARCANE_GRINDSTONE = BLOCKS.registerBlock("arcane_grindstone", BlockArcaneGrindstone::new,
            props -> props.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.STONE).pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<BlockInfusionMatrix> INFUSION_MATRIX = BLOCKS.registerBlock("infusion_matrix", BlockInfusionMatrix::new,
            props -> props.mapColor(MapColor.STONE).strength(1.5F, 1.2F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ARCANE = BLOCKS.registerBlock("pedestal_arcane", props -> new BlockPedestal(BlockPedestal.Variant.ARCANE, props),
            TTBlocks::pedestalProps);

    public static final DeferredBlock<BlockRechargePedestal> RECHARGE_PEDESTAL = BLOCKS.registerBlock("recharge_pedestal", BlockRechargePedestal::new, TTBlocks::pedestalProps);

    public static final DeferredBlock<BlockInlay> INLAY = BLOCKS.registerBlock("inlay", BlockInlay::new,
            props -> props.mapColor(MapColor.METAL).strength(0.5F).sound(SoundType.METAL).noOcclusion().noCollision().lightLevel(state -> 1));

    public static final DeferredBlock<BlockPatternCrafter> PATTERN_CRAFTER = BLOCKS.registerBlock("pattern_crafter", BlockPatternCrafter::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F, 20.0F).sound(SoundType.METAL).noOcclusion().isRedstoneConductor((state, level, pos) -> false));

    public static final DeferredBlock<BlockPotionSprayer> POTION_SPRAYER = BLOCKS.registerBlock("potion_sprayer", BlockPotionSprayer::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F, 20.0F).sound(SoundType.METAL));

    public static final DeferredBlock<BlockLevitator> LEVITATOR = BLOCKS.registerBlock("levitator", BlockLevitator::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 20.0F).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockItemGrate> ITEM_GRATE = BLOCKS.registerBlock("item_grate", BlockItemGrate::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<Block> TALLOW_BLOCK = BLOCKS.registerBlock("tallow_block", Block::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockGolemFetter> GOLEM_FETTER = BLOCKS.registerBlock("golem_fetter", BlockGolemFetter::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F).sound(SoundType.STONE));

    public static final DeferredBlock<BlockGolemBuilder> GOLEM_BUILDER = BLOCKS.registerBlock("golem_builder", BlockGolemBuilder::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 20.0F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockArcaneBore> ARCANE_BORE = BLOCKS.registerBlock("arcane_bore", BlockArcaneBore::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F, 20.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_IRON_BARS = BLOCKS.registerBlock("placeholder_iron_bars", BlockPlaceholder::new, TTBlocks::pressPlaceholderProps);

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_CAULDRON = BLOCKS.registerBlock("placeholder_cauldron", BlockPlaceholder::new, TTBlocks::pressPlaceholderProps);

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_ANVIL = BLOCKS.registerBlock("placeholder_anvil", BlockPlaceholder::new, TTBlocks::pressPlaceholderProps);

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_TABLE = BLOCKS.registerBlock("placeholder_table", BlockPlaceholder::new, TTBlocks::pressPlaceholderProps);

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ANCIENT = BLOCKS.registerBlock("pedestal_ancient", props -> new BlockPedestal(BlockPedestal.Variant.ELDRITCH, props),
            TTBlocks::pedestalProps);

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ELDRITCH = BLOCKS.registerBlock("pedestal_eldritch", props -> new BlockPedestal(BlockPedestal.Variant.ELDRITCH, props),
            TTBlocks::pedestalProps);

    public static final DeferredBlock<BlockPillar> PILLAR_ARCANE = BLOCKS.registerBlock("pillar_arcane", BlockPillar::new, TTBlocks::pillarProps);

    public static final DeferredBlock<BlockPillar> PILLAR_ANCIENT = BLOCKS.registerBlock("pillar_ancient", BlockPillar::new, TTBlocks::pillarProps);

    public static final DeferredBlock<BlockPillar> PILLAR_ELDRITCH = BLOCKS.registerBlock("pillar_eldritch", BlockPillar::new, TTBlocks::pillarProps);

    public static final DeferredBlock<Block> STONE_ARCANE = BLOCKS.registerBlock("stone_arcane", Block::new, TTBlocks::stoneProps);

    public static final DeferredBlock<Block> STONE_ARCANE_BRICK = BLOCKS.registerBlock("stone_arcane_brick", Block::new, TTBlocks::stoneProps);

    public static final DeferredBlock<Block> STONE_ANCIENT = BLOCKS.registerBlock("stone_ancient", Block::new, TTBlocks::stoneProps);

    public static final DeferredBlock<Block> STONE_ANCIENT_TILE = BLOCKS.registerBlock("stone_ancient_tile", Block::new, TTBlocks::stoneProps);

    public static final DeferredBlock<Block> STONE_ANCIENT_ROCK = BLOCKS.registerBlock("stone_ancient_rock", Block::new, TTBlocks::unbreakableProps);

    public static final DeferredBlock<Block> STONE_ANCIENT_GLYPHED = BLOCKS.registerBlock("stone_ancient_glyphed", Block::new, TTBlocks::stoneProps);

    public static final DeferredBlock<Block> STONE_ANCIENT_DOORWAY = BLOCKS.registerBlock("stone_ancient_doorway", Block::new, TTBlocks::unbreakableProps);

    public static final DeferredBlock<Block> STONE_ELDRITCH_TILE = BLOCKS.registerBlock("stone_eldritch_tile", Block::new, TTBlocks::eldritchTileProps);

    public static final DeferredBlock<Block> STONE_POROUS = BLOCKS.registerBlock("stone_porous", Block::new, TTBlocks::porousProps);

    public static final DeferredBlock<BlockStairsTT> STAIRS_ARCANE = BLOCKS.registerBlock("stairs_arcane", props -> new BlockStairsTT(STONE_ARCANE.get().defaultBlockState(), props),
            TTBlocks::stoneProps);

    public static final DeferredBlock<BlockStairsTT> STAIRS_ARCANE_BRICK = BLOCKS.registerBlock("stairs_arcane_brick", props -> new BlockStairsTT(STONE_ARCANE_BRICK.get().defaultBlockState(), props),
            TTBlocks::stoneProps);

    public static final DeferredBlock<BlockStairsTT> STAIRS_ANCIENT = BLOCKS.registerBlock("stairs_ancient", props -> new BlockStairsTT(STONE_ANCIENT.get().defaultBlockState(), props),
            TTBlocks::stoneProps);

    public static final DeferredBlock<WallBlock> WALL_ARCANE_STONE = BLOCKS.registerBlock("wall_arcane_stone", WallBlock::new, TTBlocks::stoneWallProps);

    public static final DeferredBlock<WallBlock> WALL_ARCANE_BRICK = BLOCKS.registerBlock("wall_arcane_brick", WallBlock::new, TTBlocks::stoneWallProps);

    public static final DeferredBlock<RotatedPillarBlock> STONE_ARCANE_PILLAR = BLOCKS.registerBlock("stone_arcane_pillar", RotatedPillarBlock::new, TTBlocks::stoneProps);

    public static final DeferredBlock<WallBlock> WALL_ANCIENT = BLOCKS.registerBlock("wall_ancient", WallBlock::new, TTBlocks::stoneWallProps);

    public static final DeferredBlock<BlockStairsTT> STAIRS_ANCIENT_TILE = BLOCKS.registerBlock("stairs_ancient_tile", props -> new BlockStairsTT(STONE_ANCIENT_TILE.get().defaultBlockState(), props),
            TTBlocks::stoneProps);

    public static final DeferredBlock<SlabBlock> SLAB_ANCIENT_TILE = BLOCKS.registerBlock("slab_ancient_tile", SlabBlock::new, TTBlocks::stoneProps);

    public static final DeferredBlock<WallBlock> WALL_ANCIENT_TILE = BLOCKS.registerBlock("wall_ancient_tile", WallBlock::new, TTBlocks::stoneWallProps);

    public static final DeferredBlock<BlockStairsTT> STAIRS_ANCIENT_ROCK = BLOCKS.registerBlock("stairs_ancient_rock", props -> new BlockStairsTT(STONE_ANCIENT_ROCK.get().defaultBlockState(), props),
            TTBlocks::stoneProps);

    public static final DeferredBlock<SlabBlock> SLAB_ANCIENT_ROCK = BLOCKS.registerBlock("slab_ancient_rock", SlabBlock::new, TTBlocks::stoneProps);

    public static final DeferredBlock<WallBlock> WALL_ANCIENT_ROCK = BLOCKS.registerBlock("wall_ancient_rock", WallBlock::new, TTBlocks::stoneWallProps);

    public static final DeferredBlock<RotatedPillarBlock> STONE_ANCIENT_PILLAR = BLOCKS.registerBlock("stone_ancient_pillar", RotatedPillarBlock::new, TTBlocks::stoneProps);

    public static final DeferredBlock<Block> MATRIX_SPEED = BLOCKS.registerBlock("matrix_speed", Block::new, TTBlocks::stoneProps);

    public static final DeferredBlock<Block> MATRIX_COST = BLOCKS.registerBlock("matrix_cost", Block::new, TTBlocks::stoneProps);

    public static final DeferredBlock<BlockVisBattery> VIS_BATTERY = BLOCKS.registerBlock("vis_battery", BlockVisBattery::new,
            props -> props.mapColor(MapColor.STONE).strength(0.5F).sound(SoundType.GLASS).randomTicks().lightLevel(state -> state.getValue(BlockVisBattery.CHARGE)));

    public static final DeferredBlock<BlockDioptra> DIOPTRA = BLOCKS.registerBlock("dioptra", BlockDioptra::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockJarBrain> JAR_BRAIN = BLOCKS.registerBlock("jar_brain", BlockJarBrain::new,
            props -> props.mapColor(MapColor.NONE).strength(0.3F).sound(TTSoundTypes.JAR.get()).noOcclusion());

    public static final DeferredBlock<BlockArcaneEar> ARCANE_EAR = BLOCKS.registerBlock("arcane_ear", props -> new BlockArcaneEar(false, props), TTBlocks::earProps);

    public static final DeferredBlock<BlockArcaneEar> ARCANE_EAR_TOGGLE = BLOCKS.registerBlock("arcane_ear_toggle", props -> new BlockArcaneEar(true, props), TTBlocks::earProps);

    public static final DeferredBlock<BlockLampArcane> LAMP_ARCANE = BLOCKS.registerBlock("lamp_arcane", BlockLampArcane::new, TTBlocks::lampProps);

    public static final DeferredBlock<BlockLampGrowth> LAMP_GROWTH = BLOCKS.registerBlock("lamp_growth", BlockLampGrowth::new, TTBlocks::lampProps);

    public static final DeferredBlock<BlockLampFertility> LAMP_FERTILITY = BLOCKS.registerBlock("lamp_fertility", BlockLampFertility::new, TTBlocks::lampProps);

    public static final DeferredBlock<BlockCentrifuge> CENTRIFUGE = BLOCKS.registerBlock("centrifuge", BlockCentrifuge::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F, 10.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockHungryChest> HUNGRY_CHEST = BLOCKS.registerBlock("hungry_chest", BlockHungryChest::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockEverfullUrn> EVERFULL_URN = BLOCKS.registerBlock("everfull_urn", BlockEverfullUrn::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockVisGenerator> VIS_GENERATOR = BLOCKS.registerBlock("vis_generator", BlockVisGenerator::new,
            props -> props.mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockEssentiaPort> ESSENTIA_INPUT = BLOCKS.registerBlock("essentia_input", props -> new BlockEssentiaPort(true, props), TTBlocks::portProps);

    public static final DeferredBlock<BlockEssentiaPort> ESSENTIA_OUTPUT = BLOCKS.registerBlock("essentia_output", props -> new BlockEssentiaPort(false, props), TTBlocks::portProps);

    public static final DeferredBlock<BlockCondenser> CONDENSER = BLOCKS.registerBlock("condenser", BlockCondenser::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F, 10.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockCondenserLattice> CONDENSER_LATTICE = BLOCKS.registerBlock("condenser_lattice", props -> new BlockCondenserLattice(false, props),
            props -> latticeProps().lightLevel(state -> 5));

    public static final DeferredBlock<BlockCondenserLattice> CONDENSER_LATTICE_DIRTY = BLOCKS.registerBlock("condenser_lattice_dirty", props -> new BlockCondenserLattice(true, props),
            props -> latticeProps());

    public static final DeferredBlock<BlockStabilizer> STABILIZER = BLOCKS.registerBlock("stabilizer", BlockStabilizer::new, props -> stoneProps().noOcclusion());

    public static final DeferredBlock<BlockRedstoneRelay> REDSTONE_RELAY = BLOCKS.registerBlock("redstone_relay", BlockRedstoneRelay::new,
            props -> props.mapColor(MapColor.WOOD).instabreak().sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockVoidSiphon> VOID_SIPHON = BLOCKS.registerBlock("void_siphon", BlockVoidSiphon::new,
            props -> props.mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 20.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockThaumatorium> THAUMATORIUM = BLOCKS.registerBlock("thaumatorium", BlockThaumatorium::new,
            props -> props.mapColor(MapColor.METAL).strength(3.0F, 20.0F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockThaumatoriumTop> THAUMATORIUM_TOP = BLOCKS.registerBlock("thaumatorium_top", BlockThaumatoriumTop::new,
            props -> props.mapColor(MapColor.METAL).strength(3.0F, 20.0F).sound(SoundType.METAL).noOcclusion().noLootTable());

    public static final DeferredBlock<BlockBrainBox> BRAIN_BOX = BLOCKS.registerBlock("brain_box", BlockBrainBox::new,
            props -> props.mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion());

    private static BlockBehaviour.Properties latticeProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5F, 5.0F).sound(SoundType.METAL).noOcclusion();
    }

    private static BlockBehaviour.Properties portProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL).noOcclusion();
    }

    private static BlockBehaviour.Properties earProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noOcclusion();
    }

    private static BlockBehaviour.Properties lampProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.METAL).noOcclusion().lightLevel(state -> state.getValue(BlockStateProperties.ENABLED) ? 15 : 0);
    }

    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties stoneWallProps() {
        return stoneProps().forceSolidOn();
    }

    private static BlockBehaviour.Properties eldritchStoneProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties eldritchWallProps() {
        return eldritchStoneProps().forceSolidOn();
    }

    private static BlockBehaviour.Properties unbreakableProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(-1.0F, 3600000.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties eldritchTileProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(15.0F, 1000.0F).sound(SoundType.STONE).lightLevel(state -> 12).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties porousProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<BlockSaplingTT> SAPLING_GREATWOOD = BLOCKS.registerBlock("sapling_greatwood", props -> new BlockSaplingTT(TTTreeGrowers.GREATWOOD, props),
            props -> props.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockSaplingTT> SAPLING_SILVERWOOD = BLOCKS.registerBlock("sapling_silverwood", props -> new BlockSaplingTT(TTTreeGrowers.SILVERWOOD, props),
            props -> props.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<RotatedPillarBlock> LOG_GREATWOOD = BLOCKS.registerBlock("log_greatwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> WOOD_GREATWOOD = BLOCKS.registerBlock("greatwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_LOG_GREATWOOD = BLOCKS.registerBlock("stripped_log_greatwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WOOD_GREATWOOD = BLOCKS.registerBlock("stripped_greatwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> LOG_SILVERWOOD = BLOCKS.registerBlock("log_silverwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).lightLevel(state -> 5).ignitedByLava());

    public static final DeferredBlock<BlockSilverwoodNodeLog> SILVERWOOD_NODE_LOG = BLOCKS.registerBlock("silverwood_node_log", BlockSilverwoodNodeLog::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).lightLevel(state -> 5).ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> WOOD_SILVERWOOD = BLOCKS.registerBlock("silverwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).lightLevel(state -> 5).ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_LOG_SILVERWOOD = BLOCKS.registerBlock("stripped_log_silverwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).lightLevel(state -> 5).ignitedByLava());

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WOOD_SILVERWOOD = BLOCKS.registerBlock("stripped_silverwood", RotatedPillarBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).lightLevel(state -> 5).ignitedByLava());

    public static final DeferredBlock<TintedParticleLeavesBlock> LEAVES_GREATWOOD = BLOCKS.registerBlock("leaves_greatwood", props -> new TintedParticleLeavesBlock(LEAF_PARTICLE_CHANCE, props),
            TTBlocks::leavesProps);

    public static final DeferredBlock<UntintedParticleLeavesBlock> LEAVES_SILVERWOOD = BLOCKS.registerBlock("leaves_silverwood",
            props -> new UntintedParticleLeavesBlock(LEAF_PARTICLE_CHANCE, ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, SILVERWOOD_LEAF_PARTICLE_COLOR), props),
            props -> leavesProps(props).mapColor(MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<PoweredRailBlock> ACTIVATOR_RAIL = BLOCKS.registerBlock("activator_rail", props -> new PoweredRailBlock(props, true),
            props -> props.noCollision().strength(0.7F).sound(SoundType.METAL));

    public static final DeferredBlock<Block> PLANK_GREATWOOD = BLOCKS.registerBlock("plank_greatwood", Block::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<Block> PLANK_SILVERWOOD = BLOCKS.registerBlock("plank_silverwood", Block::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

    private static BlockBehaviour.Properties leavesProps(BlockBehaviour.Properties props) {
        return props.mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY);
    }

    public static final DeferredBlock<MagicPlantBlock> PLANT_SHIMMERLEAF = BLOCKS.registerBlock("shimmerleaf",
            props -> new MagicPlantBlock(TTBlockTags.SHIMMERLEAF_SOIL, MagicPlantTraits.SHIMMERLEAF_GLINTS, Optional.empty(), props),
            props -> props.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 6).pushReaction(PushReaction.DESTROY).noOcclusion());

    public static final DeferredBlock<MagicPlantBlock> PLANT_CINDERPEARL = BLOCKS.registerBlock("cinderpearl",
            props -> new MagicPlantBlock(TTBlockTags.CINDERPEARL_SOIL, MagicPlantTraits.CINDERPEARL_EMBERS, Optional.empty(), props),
            props -> props.mapColor(MapColor.COLOR_ORANGE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 8).pushReaction(PushReaction.DESTROY).noOcclusion());

    public static final DeferredBlock<MagicPlantBlock> PLANT_VISHROOM = BLOCKS.registerBlock("vishroom",
            props -> new MagicPlantBlock(TTBlockTags.VISHROOM_SOIL, MagicPlantTraits.VISHROOM_SPORES, MagicPlantTraits.VISHROOM_NAUSEA, props),
            props -> props.mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 6).pushReaction(PushReaction.DESTROY).noOcclusion());

    public static final DeferredBlock<FlowerPotBlock> POTTED_SAPLING_GREATWOOD = pottedPlant("potted_sapling_greatwood", SAPLING_GREATWOOD);
    public static final DeferredBlock<FlowerPotBlock> POTTED_SAPLING_SILVERWOOD = pottedPlant("potted_sapling_silverwood", SAPLING_SILVERWOOD);
    public static final DeferredBlock<BlockEtherealBloom> ETHEREAL_BLOOM = BLOCKS.registerBlock("ethereal_bloom", BlockEtherealBloom::new,
            props -> props.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 12).pushReaction(PushReaction.DESTROY).noOcclusion());

    public static final DeferredBlock<FlowerPotBlock> POTTED_SHIMMERLEAF = pottedPlant("potted_shimmerleaf", PLANT_SHIMMERLEAF);
    public static final DeferredBlock<FlowerPotBlock> POTTED_CINDERPEARL = pottedPlant("potted_cinderpearl", PLANT_CINDERPEARL);
    public static final DeferredBlock<FlowerPotBlock> POTTED_VISHROOM = pottedPlant("potted_vishroom", PLANT_VISHROOM);

    public static final DeferredBlock<BlockGrassAmbient> GRASS_AMBIENT = BLOCKS.registerBlock("grass_ambient", BlockGrassAmbient::new,
            props -> props.mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS).randomTicks());

    public static final DeferredBlock<BlockMetalTT> ALCHEMICAL_CONSTRUCT = BLOCKS.registerBlock("alchemical_construct", BlockMetalTT::new,
            props -> props.mapColor(MapColor.METAL).strength(4.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTT> ADVANCED_ALCHEMICAL_CONSTRUCT = BLOCKS.registerBlock("advanced_alchemical_construct", BlockMetalTT::new,
            props -> props.mapColor(MapColor.METAL).strength(4.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTT> METAL_THAUMIUM_BLOCK = BLOCKS.registerBlock("metal_thaumium", BlockMetalTT::new,
            props -> props.mapColor(MapColor.COLOR_ORANGE).strength(4.0F, 10.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTT> METAL_BRASS_BLOCK = BLOCKS.registerBlock("metal_brass", BlockMetalTT::new,
            props -> props.mapColor(MapColor.METAL).strength(4.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockMetalTT> METAL_VOID_BLOCK = BLOCKS.registerBlock("metal_void", BlockMetalTT::new,
            props -> props.mapColor(MapColor.METAL).strength(4.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_GREATWOOD = BLOCKS.registerBlock("slab_greatwood", SlabBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(1.2F, 2.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<SlabBlock> SLAB_SILVERWOOD = BLOCKS.registerBlock("slab_silverwood", SlabBlock::new,
            props -> props.mapColor(MapColor.QUARTZ).strength(1.0F, 2.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<SlabBlock> SLAB_ARCANE_STONE = BLOCKS.registerBlock("slab_arcane_stone", SlabBlock::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_ARCANE_BRICK = BLOCKS.registerBlock("slab_arcane_brick", SlabBlock::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_ANCIENT = BLOCKS.registerBlock("slab_ancient", SlabBlock::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_ELDRITCH = BLOCKS.registerBlock("slab_eldritch", SlabBlock::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockStairsTT> STAIRS_GREATWOOD = BLOCKS.registerBlock("stairs_greatwood", props -> new BlockStairsTT(PLANK_GREATWOOD.get().defaultBlockState(), props),
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<BlockStairsTT> STAIRS_SILVERWOOD = BLOCKS.registerBlock("stairs_silverwood", props -> new BlockStairsTT(PLANK_SILVERWOOD.get().defaultBlockState(), props),
            props -> props.mapColor(MapColor.QUARTZ).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<DoorBlock> DOOR_GREATWOOD = BLOCKS.registerBlock("door_greatwood", props -> new DoorBlock(BlockSetType.OAK, props),
            props -> props.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<TrapDoorBlock> TRAPDOOR_GREATWOOD = BLOCKS.registerBlock("trapdoor_greatwood", props -> new TrapDoorBlock(BlockSetType.OAK, props),
            props -> props.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn((state, level, pos, type) -> false).ignitedByLava());

    public static final DeferredBlock<FenceBlock> FENCE_GREATWOOD = BLOCKS.registerBlock("fence_greatwood", FenceBlock::new,
            props -> props.mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<FenceGateBlock> FENCE_GATE_GREATWOOD = BLOCKS.registerBlock("fence_gate_greatwood", props -> new FenceGateBlock(WoodType.OAK, props),
            props -> props.mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());

    public static final DeferredBlock<ButtonBlock> BUTTON_GREATWOOD = BLOCKS.registerBlock("button_greatwood", props -> new ButtonBlock(BlockSetType.OAK, WOODEN_BUTTON_PRESS_TICKS, props),
            props -> props.noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<PressurePlateBlock> PRESSURE_PLATE_GREATWOOD = BLOCKS.registerBlock("pressure_plate_greatwood", props -> new PressurePlateBlock(BlockSetType.OAK, props),
            props -> props.mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).ignitedByLava().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<DoorBlock> DOOR_SILVERWOOD = BLOCKS.registerBlock("door_silverwood", props -> new DoorBlock(BlockSetType.OAK, props),
            props -> props.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<TrapDoorBlock> TRAPDOOR_SILVERWOOD = BLOCKS.registerBlock("trapdoor_silverwood", props -> new TrapDoorBlock(BlockSetType.OAK, props),
            props -> props.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn((state, level, pos, type) -> false).ignitedByLava());

    public static final DeferredBlock<FenceBlock> FENCE_SILVERWOOD = BLOCKS.registerBlock("fence_silverwood", FenceBlock::new,
            props -> props.mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<FenceGateBlock> FENCE_GATE_SILVERWOOD = BLOCKS.registerBlock("fence_gate_silverwood", props -> new FenceGateBlock(WoodType.OAK, props),
            props -> props.mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava());

    public static final DeferredBlock<ButtonBlock> BUTTON_SILVERWOOD = BLOCKS.registerBlock("button_silverwood", props -> new ButtonBlock(BlockSetType.OAK, WOODEN_BUTTON_PRESS_TICKS, props),
            props -> props.noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<PressurePlateBlock> PRESSURE_PLATE_SILVERWOOD = BLOCKS.registerBlock("pressure_plate_silverwood", props -> new PressurePlateBlock(BlockSetType.OAK, props),
            props -> props.mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).ignitedByLava().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockTable> TABLE_WOOD = BLOCKS.registerBlock("table_wood", BlockTable::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());

    public static final DeferredBlock<BlockTable> TABLE_STONE = BLOCKS.registerBlock("table_stone", BlockTable::new,
            props -> props.mapColor(MapColor.STONE).strength(2.5F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockPavingStone> PAVING_STONE_TRAVEL = BLOCKS.registerBlock("paving_stone_travel", props -> new BlockPavingStone(false, props),
            props -> props.mapColor(MapColor.STONE).strength(2.5F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockPavingStone> PAVING_STONE_BARRIER = BLOCKS.registerBlock("paving_stone_barrier", props -> new BlockPavingStone(true, props),
            props -> props.mapColor(MapColor.STONE).strength(2.5F).sound(SoundType.STONE).noOcclusion());

    public static final DeferredBlock<BlockBarrier> BARRIER = BLOCKS.registerBlock("barrier", BlockBarrier::new,
            props -> props.mapColor(MapColor.NONE).strength(-1.0F, 999.0F).noOcclusion().noLootTable().dynamicShape().isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockManaPod> MANA_POD = BLOCKS.registerBlock("mana_pod", BlockManaPod::new, props -> props.mapColor(MapColor.PLANT).strength(0.5F).sound(SoundType.CROP)
            .noOcclusion().randomTicks().pushReaction(PushReaction.DESTROY).lightLevel(state -> state.getValue(BlockManaPod.AGE)).isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockNode> NODE = BLOCKS.registerBlock("node", BlockNode::new,
            props -> props.mapColor(MapColor.NONE).strength(2.0F, 3600000.0F).noOcclusion().noLootTable().isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockJarNode> JAR_NODE = BLOCKS.registerBlock("jar_node", BlockJarNode::new,
            props -> props.mapColor(MapColor.NONE).strength(0.3F).sound(TTSoundTypes.JAR.get()).noOcclusion());

    public static final DeferredBlock<BlockNodeStabilizer> NODE_STABILIZER = BLOCKS.registerBlock("node_stabilizer", props -> new BlockNodeStabilizer(props, false),
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).noOcclusion());

    public static final DeferredBlock<BlockNodeStabilizer> NODE_STABILIZER_ADVANCED = BLOCKS.registerBlock("node_stabilizer_advanced", props -> new BlockNodeStabilizer(props, true),
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).noOcclusion());

    public static final DeferredBlock<BlockVisRelay> VIS_RELAY = BLOCKS.registerBlock("vis_relay", BlockVisRelay::new,
            props -> props.mapColor(MapColor.COLOR_PURPLE).strength(1.5F).noOcclusion().sound(SoundType.AMETHYST));

    public static final DeferredBlock<BlockNodeTransducer> NODE_TRANSDUCER = BLOCKS.registerBlock("node_transducer", BlockNodeTransducer::new,
            props -> props.mapColor(MapColor.STONE).strength(2.0F, 10.0F).noOcclusion());

    public static final DeferredBlock<BlockAmber> AMBER_BRICK = BLOCKS.registerBlock("amber_brick", BlockAmber::new, TTBlocks::amberProps);

    public static final DeferredBlock<Block> FLESH_BLOCK = BLOCKS.registerBlock("flesh_block", Block::new,
            props -> props.mapColor(MapColor.COLOR_RED).strength(0.25F, 2.0F).sound(TTSoundTypes.GORE.get()));

    public static final DeferredBlock<BlockEffectShock> EFFECT_SHOCK = BLOCKS.registerBlock("effect_shock", BlockEffectShock::new, props -> props.mapColor(MapColor.COLOR_CYAN).strength(0.0F, 999.0F)
            .replaceable().noCollision().noOcclusion().lightLevel(state -> 7).randomTicks().noLootTable().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> OBSIDIAN_TILE = BLOCKS.registerBlock("obsidian_tile", Block::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 1200.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockObsidianTotem> OBSIDIAN_TOTEM = BLOCKS.registerBlock("obsidian_totem", BlockObsidianTotem::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 1200.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockObsidianTotemCharged> OBSIDIAN_TOTEM_CHARGED = BLOCKS.registerBlock("obsidian_totem_charged", BlockObsidianTotemCharged::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 1200.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ELDRITCH_STONE = BLOCKS.registerBlock("eldritch_stone", Block::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ELDRITCH_STONE_INERT = BLOCKS.registerBlock("eldritch_stone_inert", Block::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<Block> ELDRITCH_ROCK = BLOCKS.registerBlock("eldritch_rock", Block::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ELDRITCH_CRUST = BLOCKS.registerBlock("eldritch_crust", Block::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 10.0F).sound(TTSoundTypes.GORE.get()));

    public static final DeferredBlock<BlockEldritchInset> ELDRITCH_CRUST_GLOWING = BLOCKS.registerBlock("eldritch_crust_glowing", props -> new BlockEldritchInset(ConstantInt.of(0), props),
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 30.0F).sound(SoundType.STONE).lightLevel(state -> 12).noOcclusion());

    public static final DeferredBlock<BlockMirror> MIRROR = BLOCKS.registerBlock("mirror", props -> new BlockMirror(props, false),
            props -> props.mapColor(MapColor.METAL).strength(0.1F).sound(TTSoundTypes.JAR.get()).noOcclusion());

    public static final DeferredBlock<BlockMirror> MIRROR_ESSENTIA = BLOCKS.registerBlock("mirror_essentia", props -> new BlockMirror(props, true),
            props -> props.mapColor(MapColor.METAL).strength(0.1F).sound(TTSoundTypes.JAR.get()).noOcclusion());

    public static final DeferredBlock<BlockStairsTT> STAIRS_ELDRITCH = BLOCKS.registerBlock("stairs_eldritch", props -> new BlockStairsTT(ELDRITCH_STONE.get().defaultBlockState(), props),
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<SlabBlock> SLAB_ELDRITCH_STONE = BLOCKS.registerBlock("slab_eldritch_stone", SlabBlock::new, TTBlocks::eldritchStoneProps);

    public static final DeferredBlock<WallBlock> WALL_ELDRITCH_STONE = BLOCKS.registerBlock("wall_eldritch_stone", WallBlock::new, TTBlocks::eldritchWallProps);

    public static final DeferredBlock<BlockStairsTT> STAIRS_ELDRITCH_TILE = BLOCKS.registerBlock("stairs_eldritch_tile",
            props -> new BlockStairsTT(STONE_ELDRITCH_TILE.get().defaultBlockState(), props), TTBlocks::eldritchStoneProps);

    public static final DeferredBlock<WallBlock> WALL_ELDRITCH_TILE = BLOCKS.registerBlock("wall_eldritch_tile", WallBlock::new, TTBlocks::eldritchWallProps);

    public static final DeferredBlock<BlockStairsTT> STAIRS_ELDRITCH_ROCK = BLOCKS.registerBlock("stairs_eldritch_rock", props -> new BlockStairsTT(ELDRITCH_ROCK.get().defaultBlockState(), props),
            TTBlocks::eldritchStoneProps);

    public static final DeferredBlock<SlabBlock> SLAB_ELDRITCH_ROCK = BLOCKS.registerBlock("slab_eldritch_rock", SlabBlock::new, TTBlocks::eldritchStoneProps);

    public static final DeferredBlock<WallBlock> WALL_ELDRITCH_ROCK = BLOCKS.registerBlock("wall_eldritch_rock", WallBlock::new, TTBlocks::eldritchWallProps);

    public static final DeferredBlock<RotatedPillarBlock> STONE_ELDRITCH_PILLAR = BLOCKS.registerBlock("stone_eldritch_pillar", RotatedPillarBlock::new, TTBlocks::eldritchStoneProps);

    public static final DeferredBlock<Block> ELDRITCH_DOOR = BLOCKS.registerBlock("eldritch_door", Block::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, Float.MAX_VALUE).sound(SoundType.STONE).lightLevel(state -> 12));

    public static final DeferredBlock<Block> ELDRITCH_PEDESTAL = BLOCKS.registerBlock("eldritch_pedestal", Block::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockEldritchInset> ELDRITCH_STONE_CRYSTAL = BLOCKS.registerBlock("eldritch_stone_crystal", props -> new BlockEldritchInset(UniformInt.of(1, 4), props),
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(2.0F, 30.0F).sound(SoundType.STONE).lightLevel(state -> 12).noOcclusion());

    public static final DeferredBlock<BlockEldritchNothing> ELDRITCH_NOTHING = BLOCKS.registerBlock("eldritch_nothing", BlockEldritchNothing::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 6000000.0F).sound(SoundType.WOOL).lightLevel(state -> 3).noOcclusion().noLootTable().dynamicShape());

    public static final DeferredBlock<BlockEldritchNothingDormant> ELDRITCH_NOTHING_DORMANT = BLOCKS.registerBlock("eldritch_nothing_dormant", BlockEldritchNothingDormant::new, props -> props
            .mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 6000000.0F).sound(SoundType.WOOL).noLootTable().pushReaction(PushReaction.BLOCK).isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockEldritchReliquary> ELDRITCH_RELIQUARY = BLOCKS.registerBlock("eldritch_reliquary", BlockEldritchReliquary::new, props -> props.mapColor(MapColor.COLOR_BLACK)
            .strength(-1.0F, 6000000.0F).sound(SoundType.STONE).noLootTable().noOcclusion().pushReaction(PushReaction.BLOCK).isValidSpawn((state, level, pos, type) -> false));

    public static final DeferredBlock<BlockEldritchLock> ELDRITCH_LOCK = BLOCKS.registerBlock("eldritch_lock", BlockEldritchLock::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, Float.MAX_VALUE).sound(SoundType.STONE).lightLevel(state -> 5).noLootTable());

    public static final DeferredBlock<BlockEldritchCrabSpawner> ELDRITCH_CRAB_SPAWNER = BLOCKS.registerBlock("eldritch_crab_spawner", BlockEldritchCrabSpawner::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(7.0F, 20.0F).sound(SoundType.STONE).lightLevel(state -> 4).noOcclusion().requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockEldritchTrap> ELDRITCH_TRAP = BLOCKS.registerBlock("eldritch_trap", BlockEldritchTrap::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(15.0F, 30.0F).sound(SoundType.STONE).noLootTable());

    public static final DeferredBlock<BlockEldritchAltar> ELDRITCH_ALTAR = BLOCKS.registerBlock("eldritch_altar", BlockEldritchAltar::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 20000.0F).sound(SoundType.STONE).lightLevel(state -> 12).noOcclusion().noLootTable());

    public static final DeferredBlock<BlockEldritchObelisk> ELDRITCH_OBELISK = BLOCKS.registerBlock("eldritch_obelisk", BlockEldritchObelisk::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 20000.0F).sound(SoundType.STONE).lightLevel(state -> 8).noOcclusion().noLootTable());

    public static final DeferredBlock<BlockEldritchStructure> ELDRITCH_PILLAR = BLOCKS.registerBlock("eldritch_pillar", BlockEldritchStructure::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 20000.0F).sound(SoundType.STONE).lightLevel(state -> 8).noOcclusion().noLootTable());

    public static final DeferredBlock<BlockEldritchCap> ELDRITCH_CAPSTONE = BLOCKS.registerBlock("eldritch_capstone", BlockEldritchCap::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(50.0F, 20000.0F).sound(SoundType.STONE).lightLevel(state -> 8).noOcclusion().noLootTable());

    public static final DeferredBlock<BlockEldritchPortal> ELDRITCH_PORTAL = BLOCKS.registerBlock("eldritch_portal", BlockEldritchPortal::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 200000.0F).lightLevel(state -> 15).noOcclusion().noLootTable().noCollision().pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<BlockAmber> AMBER_BLOCK = BLOCKS.registerBlock("amber_block", BlockAmber::new, TTBlocks::amberProps);

    public static final DeferredBlock<BlockWardedGlass> WARDED_GLASS = BLOCKS.registerBlock("warded_glass", BlockWardedGlass::new,
            props -> props.strength(0.3F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, level, pos, entityType) -> false).isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false));

    public static final DeferredBlock<BlockArcaneDoor> ARCANE_DOOR = BLOCKS.registerBlock("arcane_door", BlockArcaneDoor::new,
            props -> props.mapColor(MapColor.WOOD).strength(3.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockArcanePressurePlate> ARCANE_PRESSURE_PLATE = BLOCKS.registerBlock("arcane_pressure_plate", BlockArcanePressurePlate::new,
            props -> props.mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOD).noCollision().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockPlaceholder> OBSIDIAN_PLACEHOLDER = BLOCKS.registerBlock("placeholder_obsidian", props -> new BlockPlaceholder(props, true),
            props -> props.mapColor(MapColor.STONE).strength(2.5F, 3600000.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockPlaceholder> NETHER_BRICKS_PLACEHOLDER = BLOCKS.registerBlock("placeholder_nether_bricks", props -> new BlockPlaceholder(props, true),
            props -> props.mapColor(MapColor.STONE).strength(2.5F, 3600000.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockInfernalFurnace> INFERNAL_FURNACE = BLOCKS.registerBlock("infernal_furnace", BlockInfernalFurnace::new,
            props -> props.mapColor(MapColor.STONE).strength(2.5F, 3600000.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion().noLootTable().lightLevel(_ -> 13));

    public static final DeferredBlock<BlockHole> HOLE = BLOCKS.registerBlock("hole", BlockHole::new,
            props -> props.mapColor(MapColor.STONE).strength(-1.0F, 6000000.0F).sound(SoundType.WOOL).lightLevel(state -> 10).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<BlockEffectSap> EFFECT_SAP = BLOCKS.registerBlock("effect_sap", BlockEffectSap::new, props -> props.mapColor(MapColor.COLOR_PURPLE).strength(0.0F, 999.0F)
            .replaceable().noCollision().noOcclusion().lightLevel(state -> 7).randomTicks().noLootTable().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockEffectGlimmer> EFFECT_GLIMMER = BLOCKS.registerBlock("effect_glimmer", BlockEffectGlimmer::new,
            props -> props.mapColor(MapColor.NONE).strength(0.0F, 999.0F).replaceable().noCollision().noOcclusion().lightLevel(state -> 15).noLootTable().pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<BlockLoot> LOOT_URN_COMMON = lootBlock("loot_urn_common", BlockLoot.LootType.COMMON, false);
    public static final DeferredBlock<BlockLoot> LOOT_URN_UNCOMMON = lootBlock("loot_urn_uncommon", BlockLoot.LootType.UNCOMMON, false);
    public static final DeferredBlock<BlockLoot> LOOT_URN_RARE = lootBlock("loot_urn_rare", BlockLoot.LootType.RARE, false);
    public static final DeferredBlock<BlockLoot> LOOT_CRATE_COMMON = lootBlock("loot_crate_common", BlockLoot.LootType.COMMON, true);
    public static final DeferredBlock<BlockLoot> LOOT_CRATE_UNCOMMON = lootBlock("loot_crate_uncommon", BlockLoot.LootType.UNCOMMON, true);
    public static final DeferredBlock<BlockLoot> LOOT_CRATE_RARE = lootBlock("loot_crate_rare", BlockLoot.LootType.RARE, true);

    private static DeferredBlock<BlockLoot> lootBlock(String id, BlockLoot.LootType type, boolean crate) {
        return BLOCKS.registerBlock(id, props -> new BlockLoot(type, crate, props),
                props -> props.mapColor(crate ? MapColor.WOOD : MapColor.STONE).strength(0.15F, 0.0F).sound(crate ? SoundType.WOOD : TTSoundTypes.URN.get()).noOcclusion());
    }

    private TTBlocks() {}

    private static DeferredBlock<FlowerPotBlock> pottedPlant(String name, DeferredBlock<? extends Block> plant) {
        return BLOCKS.registerBlock(name, props -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, plant, props),
                props -> props.instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
