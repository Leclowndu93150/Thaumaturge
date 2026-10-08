package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotem;
import com.leclowndu93150.thaumaturge.content.decor.CandleHolderMaterial;
import com.leclowndu93150.thaumaturge.content.decor.HeldCandle;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.content.device.BlockVisBattery;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.ResearchTablePart;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSporeStalk;
import com.leclowndu93150.thaumaturge.data.model.crystal.CrystalBlockstateGenerator;
import com.leclowndu93150.thaumaturge.data.model.crystal.CrystalItemModelGenerator;
import com.leclowndu93150.thaumaturge.data.model.crystal.EssentiaCrystalModelGenerator;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

public final class TTModelProvider implements DataProvider {
    private static final ResourceLocation DEEPSLATE_TEXTURE = ResourceLocation.withDefaultNamespace("block/deepslate");
    private static final ResourceLocation BLOCK_PARENT = ResourceLocation.withDefaultNamespace("block/block");
    private static final int FULL_CUBE = 16;
    private static final ResourceLocation VANILLA_GRINDSTONE =
            ResourceLocation.withDefaultNamespace("block/grindstone");
    private static final int QUARTER_TURNS = 4;
    private static final int HALF_TURN = 2;
    private static final int NORTH_TO_SOUTH_TURNS = 2;

    private static final ModelTemplate THREE_LAYERED_ITEM = new ModelTemplate(
            Optional.of(ResourceLocation.withDefaultNamespace("item/generated")),
            Optional.empty(),
            TextureSlot.LAYER0,
            TextureSlot.LAYER1,
            TextureSlot.LAYER2);
    private static final ModelTemplate CONDENSER_RETEXTURED = new ModelTemplate(
            Optional.of(TTIds.rl("block/condenser")), Optional.empty(), TextureSlot.SIDE, TextureSlot.PARTICLE);

    private static final ResourceLocation GENERATED_PARENT = ResourceLocation.withDefaultNamespace("item/generated");
    private static final ResourceLocation BEWLR_BLOCK_PARENT = TTIds.rl("item/bewlr_block");
    private static final ResourceLocation PROPERTY_LINKED = TTIds.rl("linked");
    private static final ResourceLocation PROPERTY_LOADED = TTIds.rl("loaded");
    private static final ResourceLocation PROPERTY_NOTE_COMPLETE = TTIds.rl("note_complete");
    private static final ResourceLocation PROPERTY_FILLED = TTIds.rl("filled");
    private static final ResourceLocation PROPERTY_MARKED = TTIds.rl("marked");
    private static final ResourceLocation PROPERTY_VERDANT_TYPE = TTIds.rl("verdant_type");
    private static final ResourceLocation PROPERTY_CELESTIAL_BODY = TTIds.rl("celestial_body");
    private static final ResourceLocation PROPERTY_WAND_IS_STAFF = TTIds.rl("wand_is_staff");
    private static final ResourceLocation PROPERTY_DAMAGE = ResourceLocation.withDefaultNamespace("damage");

    private final PackOutput.PathProvider blockStatePath;
    private final PackOutput.PathProvider modelPath;

    private final Map<Block, BlockStateGenerator> blockStates = new LinkedHashMap<>();
    private final Map<ResourceLocation, Supplier<JsonElement>> models = new LinkedHashMap<>();
    private Consumer<BlockStateGenerator> blockStateOutput;
    private BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;

    public TTModelProvider(PackOutput output) {
        this.blockStatePath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.modelPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.blockStateOutput = generator -> {
            if (blockStates.put(generator.getBlock(), generator) != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + generator.getBlock());
            }
        };
        this.modelOutput = (id, json) -> {
            if (models.put(id, json) != null) {
                throw new IllegalStateException("Duplicate model definition for " + id);
            }
        };
        BlockModelGenerators blockModels = new BlockModelGenerators(blockStateOutput, modelOutput, item -> {});
        registerModels(blockModels);
        autoBlockItems();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        blockStates.forEach((block, generator) -> futures.add(DataProvider.saveStable(
                cache, generator.get(), blockStatePath.json(BuiltInRegistries.BLOCK.getKey(block)))));
        models.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json.get(), modelPath.json(id))));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Thaumaturge Models";
    }

    private static final Set<String> CHECKED_IN_ITEM_MODELS = Set.of(
            "advanced_alchemical_furnace",
            "bellows",
            "flux_scrubber",
            "leaves_greatwood",
            "leaves_silverwood",
            "plank_greatwood",
            "plank_silverwood",
            "thaumometer");

    private void autoBlockItems() {
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (!key.getNamespace().equals(TTIds.MODID) || CHECKED_IN_ITEM_MODELS.contains(key.getPath())) {
                continue;
            }
            if (!(item instanceof BlockItem blockItem)) {
                continue;
            }
            ResourceLocation itemModel = ModelLocationUtils.getModelLocation(item);
            if (!models.containsKey(itemModel) && blockStates.containsKey(blockItem.getBlock())) {
                models.put(itemModel, new DelegatedModel(ModelLocationUtils.getModelLocation(blockItem.getBlock())));
            }
        }
    }

    private void registerModels(BlockModelGenerators blockModels) {
        registerResearchTable();
        registerDeconstructionTable();
        registerResearchNote();
        registerConstructs();
        decorModels();
        eldritchModels();
        translucentCube(TTBlocks.AMBER_BRICK.get());
        blockModels.createTrivialCube(TTBlocks.FLESH_BLOCK.get());
        registerInvisibleBlock(TTBlocks.EFFECT_SHOCK.get());
        registerInvisibleBlock(TTBlocks.BARRIER.get());
        registerInvisibleBlock(TTBlocks.NODE.get());
        registerJar(TTBlocks.JAR_NORMAL.get(), "jar_normal");
        registerJar(TTBlocks.JAR_VOID.get(), "jar_void");
        registerJarBrain();
        registerAuraDevices(blockModels);
        registerNoiseDevices();
        TubeModels.register(blockStateOutput);
        registerEssentiaReservoir();
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(TTBlocks.ESSENTIA_CRYSTALIZER.get(), vName("essentia_crystalizer"))
                        .with(PropertyDispatch.property(BlockStateProperties.FACING)
                                .generate(TTModelProvider::hangingRotation)));
        delegateItem(TTItems.ESSENTIA_CRYSTALIZER.get(), TTIds.rl("block/essentia_crystalizer"));
        legacyNorthFacingBlock(TTBlocks.FLUX_SCRUBBER.get(), "flux_scrubber");
        simpleFromExisting(TTBlocks.CRUCIBLE.get(), "crucible");
        mirrorBlockState(TTBlocks.MIRROR.get());
        mirrorBlockState(TTBlocks.MIRROR_ESSENTIA.get());
        simpleFromExisting(TTBlocks.LOOT_URN_COMMON.get(), "loot_urn_common");
        simpleFromExisting(TTBlocks.LOOT_URN_UNCOMMON.get(), "loot_urn_uncommon");
        simpleFromExisting(TTBlocks.LOOT_URN_RARE.get(), "loot_urn_rare");
        simpleFromExisting(TTBlocks.LOOT_CRATE_COMMON.get(), "loot_crate_common");
        simpleFromExisting(TTBlocks.LOOT_CRATE_UNCOMMON.get(), "loot_crate_uncommon");
        simpleFromExisting(TTBlocks.LOOT_CRATE_RARE.get(), "loot_crate_rare");
        simpleFromExisting(TTBlocks.ARCANE_WORKBENCH.get(), "arcane_workbench");
        simpleFromExisting(TTBlocks.ARCANE_WORKBENCH_CHARGER.get(), "arcane_workbench_charger");
        simpleFromExisting(TTBlocks.NODE_STABILIZER.get(), "node_stabilizer");
        simpleFromExisting(TTBlocks.NODE_STABILIZER_ADVANCED.get(), "node_stabilizer_advanced");
        simpleFromExisting(TTBlocks.NODE_TRANSDUCER.get(), "node_transducer");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.VIS_RELAY.get())
                .with(PropertyDispatch.property(BlockStateProperties.FACING)
                        .select(
                                Direction.UP,
                                Variant.variant().with(VariantProperties.MODEL, TTIds.rl("block/vis_relay")))
                        .select(
                                Direction.DOWN,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, TTIds.rl("block/vis_relay"))
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.NORTH,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, TTIds.rl("block/vis_relay"))
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.SOUTH,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, TTIds.rl("block/vis_relay"))
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.WEST,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, TTIds.rl("block/vis_relay"))
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                        .select(
                                Direction.EAST,
                                Variant.variant()
                                        .with(VariantProperties.MODEL, TTIds.rl("block/vis_relay"))
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))));
        delegateItem(TTBlocks.VIS_RELAY.get().asItem(), TTIds.rl("block/vis_relay"));
        delegateItem(TTBlocks.NODE_STABILIZER.get().asItem(), TTIds.rl("item/node_stabilizer_base"));
        delegateItem(TTBlocks.NODE_STABILIZER_ADVANCED.get().asItem(), TTIds.rl("item/node_stabilizer_base"));
        delegateItem(TTBlocks.NODE_TRANSDUCER.get().asItem(), TTIds.rl("item/node_stabilizer_base"));
        simpleBlock(TTBlocks.JAR_NODE.get(), TTIds.rl("block/jar_normal"));
        delegateItem(TTBlocks.JAR_NODE.get().asItem(), BEWLR_BLOCK_PARENT);
        horizontalBlock(TTBlocks.INFERNAL_FURNACE.get(), "infernal_furnace");
        simpleBlock(
                TTBlocks.NETHER_BRICKS_PLACEHOLDER.get(), ModelLocationUtils.getModelLocation(Blocks.NETHER_BRICKS));
        simpleBlock(TTBlocks.OBSIDIAN_PLACEHOLDER.get(), ModelLocationUtils.getModelLocation(Blocks.OBSIDIAN));
        registerAlembic(TTBlocks.ALEMBIC.get());
        registerBellows();
        registerSmelter(TTBlocks.SMELTER_BASIC.get(), "smelter_basic");
        registerSmelter(TTBlocks.SMELTER_THAUMIUM.get(), "smelter_thaumium");
        registerSmelter(TTBlocks.SMELTER_VOID.get(), "smelter_void");
        horizontalBlock(TTBlocks.SMELTER_AUX.get(), "smelter_aux");
        horizontalBlock(TTBlocks.SMELTER_VENT.get(), "smelter_vent");
        flatItem(TTItems.THAUMONOMICON.get());
        flatItem(TTItems.THAUMONOMICON_CHEAT.get());
        flatItem(TTItems.THAUMONOMICON_SHARING.get());
        flatItem(TTItems.THAUMONOMICON_LINKING.get());
        flatItem(TTItems.CREATIVE_NODE_PLACER.get());
        flatItem(TTItems.SALIS_MUNDUS.get());
        registerWandItem();
        flatItem(TTItems.WAND_CAP_IRON.get());
        flatItem(TTItems.WAND_CAP_COPPER.get());
        flatItem(TTItems.WAND_CAP_GOLD.get());
        flatItem(TTItems.WAND_CAP_SILVER_INERT.get());
        flatItem(TTItems.WAND_CAP_SILVER.get());
        flatItem(TTItems.WAND_CAP_THAUMIUM_INERT.get());
        flatItem(TTItems.WAND_CAP_THAUMIUM.get());
        flatItem(TTItems.WAND_CAP_VOID_INERT.get());
        flatItem(TTItems.WAND_CAP_VOID.get());
        handheldItem(TTItems.WAND_ROD_GREATWOOD.get());
        handheldItem(TTItems.WAND_ROD_OBSIDIAN.get());
        handheldItem(TTItems.WAND_ROD_BLAZE.get());
        handheldItem(TTItems.WAND_ROD_ICE.get());
        handheldItem(TTItems.WAND_ROD_QUARTZ.get());
        handheldItem(TTItems.WAND_ROD_BONE.get());
        handheldItem(TTItems.WAND_ROD_REED.get());
        handheldItem(TTItems.WAND_ROD_SILVERWOOD.get());
        handheldItem(TTItems.STAFF_ROD_GREATWOOD.get());
        handheldItem(TTItems.STAFF_ROD_OBSIDIAN.get());
        handheldItem(TTItems.STAFF_ROD_BLAZE.get());
        handheldItem(TTItems.STAFF_ROD_ICE.get());
        handheldItem(TTItems.STAFF_ROD_QUARTZ.get());
        handheldItem(TTItems.STAFF_ROD_BONE.get());
        handheldItem(TTItems.STAFF_ROD_REED.get());
        handheldItem(TTItems.STAFF_ROD_SILVERWOOD.get());
        handheldItem(TTItems.STAFF_ROD_PRIMAL.get());
        flatItem(TTItems.PRIMAL_CHARM.get());
        flatItem(TTItems.FABRIC.get());
        flatItem(TTItems.MIRRORED_GLASS.get());
        flatItem(TTItems.FILTER.get());
        flatItem(TTItems.MECHANISM_SIMPLE.get());
        flatItem(TTItems.MECHANISM_COMPLEX.get());
        flatItem(TTItems.MORPHIC_RESONATOR.get());
        flatItem(TTItems.BATH_SALTS.get());
        flatItem(TTItems.SANITY_SOAP.get());
        flatItem(TTItems.CHUNK_BEEF.get());
        flatItem(TTItems.CHUNK_CHICKEN.get());
        flatItem(TTItems.CHUNK_PORK.get());
        flatItem(TTItems.CHUNK_FISH.get());
        flatItem(TTItems.CHUNK_RABBIT.get());
        flatItem(TTItems.CHUNK_MUTTON.get());
        flatItem(TTItems.TRIPLE_MEAT_TREAT.get());
        flatItem(TTItems.JAR_BRACE.get());
        flatItem(TTItems.TAINTED_GOO.get());
        flatItem(TTItems.TAINT_TENDRIL.get());
        flatItem(TTItems.BOTTLE_TAINT.get());
        flatItem(TTItems.VIS_RESONATOR.get());
        flatItem(TTItems.THAUMIC_SLIME_SPAWN_EGG.get());
        flatItem(TTItems.TAINT_CRAWLER_SPAWN_EGG.get());
        flatItem(TTItems.TAINTACLE_SPAWN_EGG.get());
        flatItem(TTItems.TAINT_SWARM_SPAWN_EGG.get());
        flatItem(TTItems.TAINT_SEED_SPAWN_EGG.get());
        flatItem(TTItems.TAINT_SEED_PRIME_SPAWN_EGG.get());
        flatItem(TTItems.WISP_SPAWN_EGG.get());
        flatItem(TTItems.BRAINY_ZOMBIE_SPAWN_EGG.get());
        flatItem(TTItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get());
        flatItem(TTItems.BRAINY_DROWNED_SPAWN_EGG.get());
        flatItem(TTItems.BRAINY_HUSK_SPAWN_EGG.get());
        flatItem(TTItems.BRAIN.get());
        flatItem(TTItems.FIREBAT_SPAWN_EGG.get());
        flatItem(TTItems.MIND_SPIDER_SPAWN_EGG.get());
        flatItem(TTItems.PECH_SPAWN_EGG.get());
        flatItem(TTItems.ELDRITCH_CRAB_SPAWN_EGG.get());
        flatItem(TTItems.INHABITED_ZOMBIE_SPAWN_EGG.get());
        flatItem(TTItems.ELDRITCH_GUARDIAN_SPAWN_EGG.get());
        flatItem(TTItems.CULTIST_KNIGHT_SPAWN_EGG.get());
        flatItem(TTItems.CULTIST_CLERIC_SPAWN_EGG.get());
        flatItem(TTItems.CULTIST_PORTAL_LESSER_SPAWN_EGG.get());
        flatItem(TTItems.CULTIST_LEADER_SPAWN_EGG.get());
        flatItem(TTItems.CULTIST_PORTAL_GREATER_SPAWN_EGG.get());
        flatItem(TTItems.ELDRITCH_WARDEN_SPAWN_EGG.get());
        flatItem(TTItems.ELDRITCH_GOLEM_SPAWN_EGG.get());
        flatItem(TTItems.TAINTACLE_GIANT_SPAWN_EGG.get());
        flatItem(TTItems.LOOT_BAG_COMMON.get());
        flatItem(TTItems.LOOT_BAG_UNCOMMON.get());
        flatItem(TTItems.LOOT_BAG_RARE.get());
        handheldItem(TTItems.PECH_WAND.get());
        handheldItem(TTItems.CRIMSON_BLADE.get());
        flatItem(TTItems.CRIMSON_PLATE_HELM.get());
        flatItem(TTItems.CRIMSON_PLATE_CHEST.get());
        flatItem(TTItems.CRIMSON_PLATE_LEGS.get());
        flatItem(TTItems.CRIMSON_BOOTS.get());
        flatItem(TTItems.CRIMSON_ROBE_HELM.get());
        flatItem(TTItems.CRIMSON_ROBE_CHEST.get());
        flatItem(TTItems.CRIMSON_ROBE_LEGS.get());
        flatItem(TTItems.TUBE.get());
        flatItem(TTItems.TUBE_VALVE.get());
        flatItem(TTItems.TUBE_RESTRICT.get());
        flatItem(TTItems.TUBE_FILTER.get());
        flatItem(TTItems.TUBE_ONEWAY.get());
        flatItem(TTItems.TUBE_BUFFER.get());
        flatItem(TTItems.ARCANE_KEY_IRON.get());
        flatItem(TTItems.ARCANE_KEY_GOLD.get());
        flatItem(TTItems.GOGGLES_REVEALING.get());
        flatItem(TTItems.SCRIBING_TOOLS.get());
        flatItem(TTItems.ALUMENTUM.get());
        registerCelestialNotes();
        registerBaubleItems();

        ModelTemplates.TWO_LAYERED_ITEM.create(
                TTIds.rl("item/nitor"),
                TextureMapping.layered(TTIds.rl("block/nitor"), TTIds.rl("block/nitor_core")),
                modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            registerNitor(dye);
        }

        blockModels.createTrivialCube(TTBlocks.ORE_AMBER.get());
        blockModels.createTrivialCube(TTBlocks.ORE_CINNABAR.get());
        blockModels.createTrivialCube(TTBlocks.ORE_QUARTZ.get());
        deepslateOre(TTBlocks.DEEPSLATE_ORE_AMBER.get(), "ore_amber_overlay");
        deepslateOre(TTBlocks.DEEPSLATE_ORE_CINNABAR.get(), "ore_cinnabar_overlay");
        deepslateOre(TTBlocks.DEEPSLATE_ORE_QUARTZ.get(), "ore_quartz_overlay");

        blockModels.createTrivialCube(TTBlocks.ALCHEMICAL_CONSTRUCT.get());
        blockModels.createTrivialCube(TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
        registerInvisibleBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get());

        blockModels.createTrivialCube(TTBlocks.METAL_BRASS_BLOCK.get());
        blockModels.createTrivialCube(TTBlocks.METAL_THAUMIUM_BLOCK.get());
        blockModels.createTrivialCube(TTBlocks.METAL_VOID_BLOCK.get());
        translucentCube(TTBlocks.AMBER_BLOCK.get());

        flatItem(TTItems.INGOT_THAUMIUM.get());
        flatItem(TTItems.INGOT_BRASS.get());
        flatItem(TTItems.INGOT_VOID.get());
        flatItem(TTItems.AMBER.get());
        flatItem(TTItems.QUICKSILVER.get());

        flatItem(TTItems.RARE_EARTH.get());

        flatItem(TTItems.NUGGET_THAUMIUM.get());
        flatItem(TTItems.NUGGET_BRASS.get());
        flatItem(TTItems.NUGGET_VOID.get());
        flatItem(TTItems.NUGGET_QUICKSILVER.get());
        registerInfusionAltar();
        registerSimpleWithItem(TTBlocks.FOCAL_MANIPULATOR.get(), "focal_manipulator");
        registerInvisibleBlock(TTBlocks.HOLE.get());
        registerInvisibleBlock(TTBlocks.EFFECT_SAP.get());
        registerInvisibleBlock(TTBlocks.EFFECT_GLIMMER.get());

        registerCandles();
        registerBanners();
        flatItem(TTItems.TALLOW.get());
        handheldItem(TTItems.THAUMIUM_SWORD.get());
        handheldItem(TTItems.THAUMIUM_PICKAXE.get());
        handheldItem(TTItems.THAUMIUM_AXE.get());
        handheldItem(TTItems.THAUMIUM_SHOVEL.get());
        handheldItem(TTItems.THAUMIUM_HOE.get());
        handheldItem(TTItems.VOID_SWORD.get());
        handheldItem(TTItems.VOID_PICKAXE.get());
        handheldItem(TTItems.VOID_AXE.get());
        handheldItem(TTItems.VOID_SHOVEL.get());
        handheldItem(TTItems.VOID_HOE.get());
        handheldItem(TTItems.ELEMENTAL_SWORD.get());
        handheldItem(TTItems.ELEMENTAL_PICKAXE.get());
        handheldItem(TTItems.ELEMENTAL_AXE.get());
        handheldItem(TTItems.ELEMENTAL_SHOVEL.get());
        handheldItem(TTItems.ELEMENTAL_HOE.get());
        handheldItem(TTItems.PRIMAL_CRUSHER.get());
        flatItem(TTItems.THAUMOSTATIC_HARNESS.get());
        flatItem(TTItems.TRAVELLER_BOOTS.get());
        flatItem(TTItems.THAUMIUM_HELM.get());
        flatItem(TTItems.THAUMIUM_CHEST.get());
        flatItem(TTItems.THAUMIUM_LEGS.get());
        flatItem(TTItems.THAUMIUM_BOOTS.get());
        flatItem(TTItems.VOID_HELM.get());
        flatItem(TTItems.VOID_CHEST.get());
        flatItem(TTItems.VOID_LEGS.get());
        flatItem(TTItems.VOID_BOOTS.get());
        registerRobeItem(TTItems.CLOTH_CHEST.get(), "cloth_chest");
        registerRobeItem(TTItems.CLOTH_LEGS.get(), "cloth_legs");
        registerRobeItem(TTItems.CLOTH_BOOTS.get(), "cloth_boots");
        registerSpa();
        registerCasters();
        registerGolemancy();

        flatItem(TTItems.NUGGET_QUARTZ.get());

        flatItem(TTItems.VOID_SEED.get());
        flatItem(TTItems.CAUSALITY_COLLAPSER.get());
        flatItem(TTItems.CLUSTER_IRON.get());
        flatItem(TTItems.CLUSTER_GOLD.get());
        flatItem(TTItems.CLUSTER_COPPER.get());
        flatItem(TTItems.CLUSTER_SILVER.get());
        flatItem(TTItems.CLUSTER_LEAD.get());
        flatItem(TTItems.CLUSTER_TIN.get());
        flatItem(TTItems.RAW_CINNABAR.get());
        flatItem(TTItems.CLUSTER_CINNABAR.get());
        flatItem(TTItems.CLUSTER_QUARTZ.get());

        flatItem(TTItems.PLATE_IRON.get());
        flatItem(TTItems.PLATE_THAUMIUM.get());
        flatItem(TTItems.PLATE_BRASS.get());
        flatItem(TTItems.PLATE_VOID.get());

        CrystalBlockstateGenerator.register(blockStateOutput);
        CrystalItemModelGenerator.register(modelOutput);
        EssentiaCrystalModelGenerator.register(modelOutput);
        registerManaPod();
        stoneAndStairModels();
        treeModels();
        plantModels();
        taintModels();
        containerItemModels();
    }

    private void registerManaPod() {
        Block pod = TTBlocks.MANA_POD.get();
        ResourceLocation[] stems = new ResourceLocation[3];
        for (int i = 0; i < 3; i++) {
            stems[i] = ModelTemplates.CROSS.createWithSuffix(
                    pod,
                    "_stage" + i,
                    TextureMapping.cross(TextureMapping.getBlockTexture(pod, "_stem_" + i)),
                    (id, json) -> modelOutput.accept(id, () -> {
                        JsonElement element = json.get();
                        element.getAsJsonObject().addProperty("render_type", "minecraft:cutout");
                        return element;
                    }));
        }
        PropertyDispatch ages = PropertyDispatch.property(BlockManaPod.AGE)
                .select(0, v(stems[0]))
                .select(1, v(stems[1]))
                .select(2, v(stems[2]))
                .select(3, v(stems[2]))
                .select(4, v(stems[2]))
                .select(5, v(stems[2]))
                .select(6, v(stems[2]))
                .select(7, v(stems[2]));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(pod).with(ages));

        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(TTItems.MANA_BEAN.get()),
                TextureMapping.layer0(TTIds.rl("item/mana_bean")),
                modelOutput);
    }

    private static Variant v(ResourceLocation model) {
        return Variant.variant().with(VariantProperties.MODEL, model);
    }

    private static Variant vName(String blockModelName) {
        return v(TTIds.rl("block/" + blockModelName));
    }

    private void deepslateOre(Block block, String overlay) {
        ResourceLocation model = ModelLocationUtils.getModelLocation(block);
        ResourceLocation overlayTexture = blockTexture(overlay);
        modelOutput.accept(model, () -> {
            JsonObject root = new JsonObject();
            root.addProperty("parent", BLOCK_PARENT.toString());
            root.addProperty("render_type", "minecraft:cutout");
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", DEEPSLATE_TEXTURE.toString());
            textures.addProperty("base", DEEPSLATE_TEXTURE.toString());
            textures.addProperty("overlay", overlayTexture.toString());
            root.add("textures", textures);
            JsonArray elements = new JsonArray();
            elements.add(fullCube("#base"));
            elements.add(fullCube("#overlay"));
            root.add("elements", elements);
            return root;
        });
        simpleBlock(block, model);
        delegateItem(block.asItem(), model);
    }

    private static JsonObject fullCube(String texture) {
        JsonObject element = new JsonObject();
        element.add("from", coords(0, 0, 0));
        element.add("to", coords(FULL_CUBE, FULL_CUBE, FULL_CUBE));
        JsonObject faces = new JsonObject();
        for (Direction dir : Direction.values()) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", texture);
            face.addProperty("cullface", dir.getSerializedName());
            faces.add(dir.getSerializedName(), face);
        }
        element.add("faces", faces);
        return element;
    }

    private static JsonArray coords(int x, int y, int z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private void simpleBlock(Block block, ResourceLocation model) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, v(model)));
    }

    private void translucentCube(Block block) {
        ResourceLocation model = ModelTemplates.CUBE_ALL.create(
                block,
                TextureMapping.cube(block),
                (id, json) -> modelOutput.accept(id, () -> {
                    JsonElement element = json.get();
                    element.getAsJsonObject().addProperty("render_type", "minecraft:translucent");
                    return element;
                }));
        simpleBlock(block, model);
    }

    private void simpleFromExisting(Block block, String modelName) {
        simpleBlock(block, TTIds.rl("block/" + modelName));
    }

    private void registerCondenser() {
        Block block = TTBlocks.CONDENSER.get();
        ResourceLocation on = TTIds.rl("block/condenser");
        ResourceLocation offTexture = blockTexture("condenser_off");
        ResourceLocation off = CONDENSER_RETEXTURED.create(
                TTIds.rl("block/condenser_off"),
                new TextureMapping().put(TextureSlot.SIDE, offTexture).put(TextureSlot.PARTICLE, offTexture),
                modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.property(BlockStateProperties.ENABLED)
                        .select(true, v(on))
                        .select(false, v(off))));
        delegateItem(block.asItem(), on);
    }

    private void delegateItem(Item item, ResourceLocation model) {
        modelOutput.accept(ModelLocationUtils.getModelLocation(item), new DelegatedModel(model));
    }

    private void flatItem(Item item) {
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), modelOutput);
    }

    private void handheldItem(Item item) {
        ModelTemplates.FLAT_HANDHELD_ITEM.create(
                ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), modelOutput);
    }

    private record ItemOverride(ResourceLocation predicate, float threshold, ResourceLocation model) {}

    private record OverridesModel(
            Optional<ResourceLocation> parent, Map<String, ResourceLocation> layers, List<ItemOverride> overrides)
            implements Supplier<JsonElement> {
        @Override
        public JsonElement get() {
            JsonObject root = new JsonObject();
            root.addProperty("parent", parent.orElse(GENERATED_PARENT).toString());
            if (!layers.isEmpty()) {
                JsonObject textures = new JsonObject();
                layers.forEach((slot, texture) -> textures.addProperty(slot, texture.toString()));
                root.add("textures", textures);
            }
            JsonArray array = new JsonArray();
            for (ItemOverride override : overrides) {
                JsonObject entry = new JsonObject();
                JsonObject predicate = new JsonObject();
                predicate.addProperty(override.predicate().toString(), override.threshold());
                entry.add("predicate", predicate);
                entry.addProperty("model", override.model().toString());
                array.add(entry);
            }
            root.add("overrides", array);
            return root;
        }
    }

    private void overridesItem(Item item, ResourceLocation parent, List<ItemOverride> overrides) {
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(item),
                new OverridesModel(Optional.of(parent), Map.of(), overrides));
    }

    private void generatedOverridesItem(Item item, Map<String, ResourceLocation> layers, List<ItemOverride> overrides) {
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(item), new OverridesModel(Optional.empty(), layers, overrides));
    }

    private static PropertyDispatch horizontalDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, Variant.variant())
                .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.SOUTH,
                        Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(
                        Direction.WEST,
                        Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static Variant facingRotation(Direction direction) {
        return switch (direction) {
            case UP -> Variant.variant();
            case DOWN -> Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180);
            case NORTH -> Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case SOUTH ->
                Variant.variant()
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case WEST ->
                Variant.variant()
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case EAST ->
                Variant.variant()
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
        };
    }

    private void legacyNorthFacingBlock(Block block, String modelName) {
        PropertyDispatch.C1<Direction> dispatch = PropertyDispatch.property(BlockStateProperties.FACING);
        dispatch = dispatch.select(Direction.NORTH, Variant.variant());
        dispatch = dispatch.select(
                Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
        dispatch = dispatch.select(
                Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
        dispatch = dispatch.select(
                Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
        dispatch = dispatch.select(
                Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270));
        dispatch = dispatch.select(
                Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90));
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block, vName(modelName)).with(dispatch));
    }

    private static PropertyDispatch upBaseFacingDispatch() {
        PropertyDispatch.C1<Direction> dispatch = PropertyDispatch.property(BlockStateProperties.FACING);
        for (Direction direction : Direction.values()) {
            dispatch = dispatch.select(direction, facingRotation(direction));
        }
        return dispatch;
    }

    private static Variant hangingRotation(Direction direction) {
        return switch (direction) {
            case DOWN -> Variant.variant();
            case UP -> Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180);
            case SOUTH -> Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case NORTH ->
                Variant.variant()
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case EAST ->
                Variant.variant()
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case WEST ->
                Variant.variant()
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
        };
    }

    private static PropertyDispatch downBaseFacingDispatch() {
        PropertyDispatch.C1<Direction> dispatch = PropertyDispatch.property(BlockStateProperties.FACING);
        for (Direction direction : Direction.values()) {
            dispatch = dispatch.select(direction, hangingRotation(direction));
        }
        return dispatch;
    }

    private void horizontalBlock(Block block, String modelName) {
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block, vName(modelName)).with(horizontalDispatch()));
        delegateItem(block.asItem(), TTIds.rl("block/" + modelName));
    }

    private void registerBellows() {
        PropertyDispatch rotations = PropertyDispatch.property(BlockStateProperties.FACING)
                .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                .select(Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                .select(Direction.NORTH, Variant.variant())
                .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.SOUTH,
                        Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(
                        Direction.WEST,
                        Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.BELLOWS.get(), vName("bellows"))
                .with(rotations));
    }

    private void registerBanners() {
        ResourceLocation bannerModel = TTIds.rl("block/tt_banner");
        ResourceLocation stand = TTIds.rl("item/banner_stand");
        ResourceLocation cloth = TTIds.rl("item/banner_cloth");
        ResourceLocation symbol = TTIds.rl("item/banner_symbol");
        ResourceLocation dyedItemModel = TTIds.rl("item/banner_dyed");
        ResourceLocation cultistItemModel = TTIds.rl("item/banner_cultist");
        THREE_LAYERED_ITEM.create(dyedItemModel, TextureMapping.layered(stand, cloth, symbol), modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(
                cultistItemModel, TextureMapping.layered(stand, cultistItemModel), modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            simpleBlock(TTBlocks.BANNERS.get(dye).get(), bannerModel);
            simpleBlock(TTBlocks.WALL_BANNERS.get(dye).get(), bannerModel);
            delegateItem(TTItems.BANNERS.get(dye).get(), dyedItemModel);
        }
        simpleBlock(TTBlocks.BANNER_CRIMSON_CULT.get(), bannerModel);
        simpleBlock(TTBlocks.WALL_BANNER_CRIMSON_CULT.get(), bannerModel);
        delegateItem(TTItems.BANNER_CRIMSON_CULT.get(), cultistItemModel);
    }

    private void registerCandles() {
        ResourceLocation model = TTIds.rl("block/candle");
        for (DyeColor dye : DyeColor.values()) {
            Block candle = TTBlocks.CANDLES.get(dye).get();
            simpleBlock(candle, model);
            delegateItem(candle.asItem(), model);
        }
        for (CandleHolderMaterial material : CandleHolderMaterial.values()) {
            BlockCandleHolder holder = TTBlocks.CANDLE_HOLDERS.get(material).get();
            ResourceLocation empty = TTIds.rl("block/candle_holder_" + material.getSerializedName());
            ResourceLocation filled = TTIds.rl("block/candle_holder_" + material.getSerializedName() + "_filled");
            PropertyDispatch.C1<HeldCandle> candles = PropertyDispatch.property(BlockCandleHolder.CANDLE);
            for (HeldCandle held : HeldCandle.values()) {
                candles = candles.select(held, v(held.isPresent() ? filled : empty));
            }
            blockStateOutput.accept(MultiVariantGenerator.multiVariant(holder).with(candles));
            delegateItem(holder.asItem(), empty);
        }
    }

    private void registerBaubleItems() {
        flatItem(TTItems.AMULET_MUNDANE.get());
        flatItem(TTItems.RING_MUNDANE.get());
        flatItem(TTItems.GIRDLE_MUNDANE.get());
        flatItem(TTItems.RING_APPRENTICE.get());
        flatItem(TTItems.AMULET_FANCY.get());
        flatItem(TTItems.RING_FANCY.get());
        flatItem(TTItems.GIRDLE_FANCY.get());
        flatItem(TTItems.AMULET_VIS.get());
        flatItem(TTItems.AMULET_VIS_CRAFTED.get());
        flatItem(TTItems.CHARM_UNDYING.get());
        flatItem(TTItems.CLOUD_RING.get());
        flatItem(TTItems.CURIOSITY_BAND.get());
        flatItem(TTItems.VOIDSEER_CHARM.get());
        flatItem(TTItems.FOCUS_POUCH.get());
        flatItem(TTItems.SANITY_CHECKER.get());
        flatItem(TTItems.RESONATOR.get());
        flatItem(TTItems.CURIO_ARCANE.get());
        flatItem(TTItems.CURIO_PRESERVED.get());
        flatItem(TTItems.CURIO_ANCIENT.get());
        flatItem(TTItems.CURIO_ELDRITCH.get());
        flatItem(TTItems.CURIO_KNOWLEDGE.get());
        flatItem(TTItems.CURIO_TWISTED.get());
        flatItem(TTItems.CURIO_RITES.get());
        flatItem(TTItems.CREATIVE_FLUX_SPONGE.get());
        flatItem(TTItems.HAND_MIRROR.get());
        registerMirrorItem(TTItems.MIRROR.get(), "mirrorframe");
        registerMirrorItem(TTItems.MIRROR_ESSENTIA.get(), "mirrorframe2");
        flatItem(TTItems.CRIMSON_PRAETOR_HELM.get());
        flatItem(TTItems.CRIMSON_PRAETOR_CHEST.get());
        flatItem(TTItems.CRIMSON_PRAETOR_LEGS.get());
        flatItem(TTItems.FORTRESS_HELM.get());
        flatItem(TTItems.FORTRESS_CHEST.get());
        flatItem(TTItems.FORTRESS_LEGS.get());
        registerVerdantCharm();
        registerVoidRobeItems();
    }

    private void registerVerdantCharm() {
        ResourceLocation base = TTIds.rl("item/verdant_charm");
        List<ItemOverride> overrides = new ArrayList<>();
        for (int type = 0; type <= 2; type++) {
            ResourceLocation model = TTIds.rl("item/verdant_charm_" + type);
            ResourceLocation overlay = TTIds.rl("item/verdant_charm_over_" + type);
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(base, overlay), modelOutput);
            if (type > 0) {
                overrides.add(new ItemOverride(PROPERTY_VERDANT_TYPE, type, model));
            }
        }
        overridesItem(TTItems.VERDANT_CHARM.get(), TTIds.rl("item/verdant_charm_0"), overrides);
    }

    private void registerVoidRobeItems() {
        flatItem(TTItems.VOID_ROBE_HELM.get());
        registerVoidRobePiece(TTItems.VOID_ROBE_CHEST.get(), "void_robe_chest");
        registerVoidRobePiece(TTItems.VOID_ROBE_LEGS.get(), "void_robe_legs");
    }

    private void registerVoidRobePiece(Item item, String name) {
        ModelTemplates.TWO_LAYERED_ITEM.create(
                TTIds.rl("item/" + name),
                TextureMapping.layered(TTIds.rl("item/" + name + "_over"), TTIds.rl("item/" + name)),
                modelOutput);
    }

    private void registerCelestialNotes() {
        ResourceLocation sheet = TTIds.rl("item/celestial_notes_sheet");
        List<ItemOverride> overrides = new ArrayList<>();
        for (CelestialBody body : CelestialBody.values()) {
            ResourceLocation model = TTIds.rl("item/celestial_notes_" + body.getSerializedName());
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(sheet, model), modelOutput);
            if (body.ordinal() > 0) {
                overrides.add(new ItemOverride(PROPERTY_CELESTIAL_BODY, body.ordinal(), model));
            }
        }
        overridesItem(TTItems.CELESTIAL_NOTES.get(), TTIds.rl("item/celestial_notes_sun"), overrides);
    }

    private void registerWandItem() {
        ResourceLocation staffModel = TTIds.rl("item/wand_staff");
        modelOutput.accept(staffModel, new DelegatedModel(TTIds.rl("item/wand_staff_base")));
        overridesItem(
                TTItems.WAND.get(),
                TTIds.rl("item/wand_base"),
                List.of(new ItemOverride(PROPERTY_WAND_IS_STAFF, 1.0F, staffModel)));
    }

    private void registerJar(Block block, String modelName) {
        simpleBlock(block, TTIds.rl("block/" + modelName));
        delegateItem(block.asItem(), BEWLR_BLOCK_PARENT);
    }

    private void registerAlembic(Block block) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(vName("alembic"));
        Map<String, Object> children = new LinkedHashMap<>();
        children.put("core", Map.of("parent", "thaumaturge:block/alembic"));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BooleanProperty property = BlockEssentiaTransport.propertyFor(direction);
            Variant pane = vName("alembic_pane");
            Variant port = vName("alembic_port");
            int rotation =
                    switch (direction) {
                        case EAST -> 1;
                        case SOUTH -> 2;
                        case WEST -> 3;
                        default -> 0;
                    };
            VariantProperties.Rotation turns = VariantProperties.Rotation.values()[rotation];
            pane = pane.with(VariantProperties.Y_ROT, turns);
            port = port.with(VariantProperties.Y_ROT, turns);
            generator = generator.with(Condition.condition().term(property, false), pane);
            generator = generator.with(Condition.condition().term(property, true), port);
            children.put(
                    direction.getName(),
                    Map.of(
                            "parent",
                            "thaumaturge:block/alembic_pane",
                            "transform",
                            Map.of("origin", "center", "rotation", List.of(0.0F, -90.0F * rotation, 0.0F))));
        }
        blockStateOutput.accept(generator);
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(block.asItem()),
                () -> new Gson()
                        .toJsonTree(Map.of(
                                "parent",
                                "minecraft:block/block",
                                "loader",
                                "neoforge:composite",
                                "children",
                                children)));
    }

    private void registerEssentiaReservoir() {
        Block block = TTBlocks.ESSENTIA_RESERVOIR.get();
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(vName("essentia_reservoir_frame"));
        for (Direction direction : Direction.values()) {
            generator = generator.with(
                    Condition.condition().term(BlockStateProperties.FACING, direction),
                    sideVariant(TTIds.rl("block/essentia_reservoir_port"), direction));
        }
        blockStateOutput.accept(generator);
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(block.asItem()),
                () -> new Gson()
                        .toJsonTree(Map.of(
                                "parent",
                                "minecraft:block/block",
                                "loader",
                                "neoforge:composite",
                                "children",
                                Map.of(
                                        "frame",
                                        Map.of("parent", "thaumaturge:block/essentia_reservoir_frame"),
                                        "port",
                                        Map.of("parent", "thaumaturge:block/essentia_reservoir_port")))));
    }

    private static Variant sideVariant(ResourceLocation model, Direction direction) {
        Variant variant = v(model);
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

    private void registerSmelter(Block block, String modelName) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(BlockSmelter.LIT, BlockStateProperties.HORIZONTAL_FACING)
                        .generate((lit, facing) -> {
                            Variant variant = vName(lit ? modelName + "_on" : modelName + "_off");
                            return switch (facing) {
                                case EAST -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                                case SOUTH -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
                                case WEST -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
                                default -> variant;
                            };
                        })));
        delegateItem(block.asItem(), TTIds.rl("block/" + modelName + "_off"));
    }

    private void registerDeconstructionTable() {
        registerInvisibleBlock(TTBlocks.DECONSTRUCTION_TABLE.get());
        delegateItem(TTBlocks.DECONSTRUCTION_TABLE.get().asItem(), TTIds.rl("item/deconstruction_table_base"));
    }

    private void registerResearchNote() {
        ResourceLocation complete = ModelLocationUtils.getModelLocation(TTItems.RESEARCH_NOTE.get(), "_complete");
        ModelTemplates.TWO_LAYERED_ITEM.create(
                complete,
                TextureMapping.layered(
                        TTIds.rl("item/research_note_complete"), TTIds.rl("item/research_note_complete_overlay")),
                modelOutput);
        generatedOverridesItem(
                TTItems.RESEARCH_NOTE.get(),
                Map.of("layer0", TTIds.rl("item/research_note"), "layer1", TTIds.rl("item/research_note_overlay")),
                List.of(new ItemOverride(PROPERTY_NOTE_COMPLETE, 1.0F, complete)));
    }

    private void registerResearchTable() {
        Block block = TTBlocks.RESEARCH_TABLE.get();
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(BlockResearchTable.PART, BlockResearchTable.FACING)
                        .generate((part, direction) -> {
                            Variant model = vName(
                                    part == ResearchTablePart.MAIN ? "research_table_main" : "research_table_ext");
                            return switch (direction) {
                                case EAST -> model.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                                case SOUTH -> model.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
                                case WEST -> model.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
                                default -> model;
                            };
                        })));
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(block.asItem()),
                () -> new Gson()
                        .toJsonTree(Map.of(
                                "parent",
                                "minecraft:block/block",
                                "loader",
                                "neoforge:composite",
                                "children",
                                Map.of(
                                        "main",
                                                Map.of(
                                                        "parent",
                                                        "thaumaturge:block/research_table_main",
                                                        "transform",
                                                        Map.of(
                                                                "origin",
                                                                "corner",
                                                                "translation",
                                                                List.of(0.25F, 0.25F, 0.5F),
                                                                "scale",
                                                                0.5F)),
                                        "ext",
                                                Map.of(
                                                        "parent",
                                                        "thaumaturge:block/research_table_ext",
                                                        "transform",
                                                        Map.of(
                                                                "origin",
                                                                "corner",
                                                                "translation",
                                                                List.of(0.75F, 0.25F, 0.5F),
                                                                "rotation",
                                                                List.of(0.0F, -180.0F, 0.0F),
                                                                "scale",
                                                                0.5F))))));
    }

    private void registerInvisibleBlock(Block block) {
        simpleBlock(block, TTIds.rl("block/empty"));
    }

    private void registerNitor(DyeColor dye) {
        registerInvisibleBlock(TTBlocks.NITORS.get(dye).get());
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(TTItems.NITORS.get(dye).get()), () -> {
                    JsonObject model = new JsonObject();
                    model.addProperty("loader", "neoforge:separate_transforms");
                    JsonObject flat = new JsonObject();
                    flat.addProperty("parent", TTIds.rl("item/nitor").toString());
                    model.add("base", flat);
                    JsonObject inHand = new JsonObject();
                    inHand.addProperty("parent", BEWLR_BLOCK_PARENT.toString());
                    JsonObject perspectives = new JsonObject();
                    perspectives.add("firstperson_lefthand", inHand);
                    perspectives.add("firstperson_righthand", inHand);
                    perspectives.add("thirdperson_lefthand", inHand);
                    perspectives.add("thirdperson_righthand", inHand);
                    model.add("perspectives", perspectives);
                    return model;
                });
    }

    private void registerInfusionAltar() {
        registerPillar(TTBlocks.PILLAR_ARCANE.get(), "pillar_arcane");
        registerPillar(TTBlocks.PILLAR_ANCIENT.get(), "pillar_ancient");
        registerPillar(TTBlocks.PILLAR_ELDRITCH.get(), "pillar_eldritch");
        registerSimpleWithItem(TTBlocks.PEDESTAL_ARCANE.get(), "pedestal_arcane");
        registerSimpleWithItem(TTBlocks.RECHARGE_PEDESTAL.get(), "recharge_pedestal");
        registerSimpleWithItem(TTBlocks.PEDESTAL_ANCIENT.get(), "pedestal_ancient");
        registerSimpleWithItem(TTBlocks.PEDESTAL_ELDRITCH.get(), "pedestal_eldritch");
        registerSimpleWithItem(TTBlocks.INFUSION_MATRIX.get(), "infusion_matrix");
        delegateItem(TTBlocks.INFUSION_MATRIX.get().asItem(), TTIds.rl("block/infusion_matrix"));
    }

    private void registerPillar(Block block, String modelName) {
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block, vName(modelName)).with(horizontalDispatch()));
        delegateItem(block.asItem(), TTIds.rl("block/" + modelName));
    }

    private void registerSimpleWithItem(Block block, String modelName) {
        simpleBlock(block, TTIds.rl("block/" + modelName));
        if (block != TTBlocks.INFUSION_MATRIX.get()) {
            delegateItem(block.asItem(), TTIds.rl("block/" + modelName));
        }
    }

    private void registerSpa() {
        ResourceLocation spaModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                ModelLocationUtils.getModelLocation(TTBlocks.SPA.get()),
                new TextureMapping()
                        .put(TextureSlot.SIDE, TTIds.rl("block/spa_side"))
                        .put(TextureSlot.TOP, TTIds.rl("block/spa_top"))
                        .put(TextureSlot.BOTTOM, ResourceLocation.withDefaultNamespace("block/furnace_top")),
                modelOutput);
        simpleBlock(TTBlocks.SPA.get(), spaModel);
        delegateItem(TTItems.SPA.get(), spaModel);
        simpleBlock(TTBlocks.PURIFYING_FLUID.get(), TTIds.rl("block/purifying_fluid"));
        simpleBlock(TTBlocks.LIQUID_DEATH.get(), TTIds.rl("block/liquid_death"));
        flatItem(TTItems.BUCKET_LIQUID_DEATH.get());
        flatItem(TTItems.BUCKET_PURIFYING.get());
    }

    private void registerGolemancy() {
        flatItem(TTItems.MIND_CLOCKWORK.get());
        flatItem(TTItems.MIND_BIOTHAUMIC.get());
        flatItem(TTItems.MODULE_VISION.get());
        flatItem(TTItems.MODULE_AGGRESSION.get());
        flatItem(TTItems.GOLEM_BELL.get());
        flatItem(TTItems.GOLEM_TOP_HAT.get());
        flatItem(TTItems.GOLEM_FEZ.get());
        flatItem(TTItems.GOLEM_GLASSES.get());
        flatItem(TTItems.GOLEM_BOWTIE.get());
        flatItem(TTItems.GOLEM_VISOR.get());
        flatItem(TTItems.SEAL_BLANK.get());
        flatItem(TTItems.SEAL_PICKUP.get());
        flatItem(TTItems.SEAL_PICKUP_ADVANCED.get());
        flatItem(TTItems.SEAL_FILL.get());
        flatItem(TTItems.SEAL_FILL_ADVANCED.get());
        flatItem(TTItems.SEAL_EMPTY.get());
        flatItem(TTItems.SEAL_EMPTY_ADVANCED.get());
        flatItem(TTItems.SEAL_HARVEST.get());
        flatItem(TTItems.SEAL_BUTCHER.get());
        flatItem(TTItems.SEAL_GUARD.get());
        flatItem(TTItems.SEAL_GUARD_ADVANCED.get());
        flatItem(TTItems.SEAL_LUMBER.get());
        flatItem(TTItems.SEAL_BREAKER.get());
        flatItem(TTItems.SEAL_BREAKER_ADVANCED.get());
        flatItem(TTItems.SEAL_USE.get());
        flatItem(TTItems.SEAL_PROVIDER.get());
        flatItem(TTItems.SEAL_STOCK.get());

        delegateItem(TTItems.GOLEM_PLACER.get(), TTIds.rl("item/golem_base"));

        ResourceLocation inlayDot = TTIds.rl("block/inlay_dot");
        ResourceLocation inlaySide = TTIds.rl("block/inlay_side");
        MultiPartGenerator inlayGenerator = MultiPartGenerator.multiPart(TTBlocks.INLAY.get())
                .with(v(inlayDot))
                .with(Condition.condition().term(BlockInlay.NORTH, true), v(inlaySide))
                .with(
                        Condition.condition().term(BlockInlay.EAST, true),
                        v(inlaySide).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .with(
                        Condition.condition().term(BlockInlay.SOUTH, true),
                        v(inlaySide).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(
                        Condition.condition().term(BlockInlay.WEST, true),
                        v(inlaySide).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
        blockStateOutput.accept(inlayGenerator);
        ModelTemplates.TWO_LAYERED_ITEM.create(
                ModelLocationUtils.getModelLocation(TTItems.INLAY.get()),
                TextureMapping.layered(TTIds.rl("block/inlay_connect_under"), TTIds.rl("block/inlay_connect1")),
                modelOutput);

        ResourceLocation patternCrafterModel = TTIds.rl("block/pattern_crafter");
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(TTBlocks.PATTERN_CRAFTER.get(), v(patternCrafterModel))
                        .with(horizontalDispatch()));
        delegateItem(TTItems.PATTERN_CRAFTER.get(), patternCrafterModel);

        ResourceLocation sprayerModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                TTBlocks.POTION_SPRAYER.get(),
                new TextureMapping()
                        .put(TextureSlot.TOP, TTIds.rl("block/potion_sprayer_top"))
                        .put(TextureSlot.BOTTOM, TTIds.rl("block/potion_sprayer_bottom"))
                        .put(TextureSlot.SIDE, TTIds.rl("block/potion_sprayer_side")),
                modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.POTION_SPRAYER.get(), v(sprayerModel))
                .with(upBaseFacingDispatch()));
        delegateItem(TTItems.POTION_SPRAYER.get(), sprayerModel);

        ResourceLocation levitatorOn = TTIds.rl("block/levitator_on");
        ResourceLocation levitatorOff = TTIds.rl("block/levitator_off");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.LEVITATOR.get())
                .with(PropertyDispatch.properties(BlockStateProperties.ENABLED, BlockStateProperties.FACING)
                        .generate((enabled, facing) ->
                                Variant.merge(v(enabled ? levitatorOn : levitatorOff), facingRotation(facing)))));
        delegateItem(TTItems.LEVITATOR.get(), levitatorOff);

        registerInvisibleBlock(TTBlocks.GOLEM_BUILDER.get());
        delegateItem(TTItems.GOLEM_BUILDER.get(), TTIds.rl("item/golem_builder_base"));
        registerInvisibleBlock(TTBlocks.PLACEHOLDER_IRON_BARS.get());
        registerInvisibleBlock(TTBlocks.PLACEHOLDER_CAULDRON.get());
        registerInvisibleBlock(TTBlocks.PLACEHOLDER_ANVIL.get());
        registerInvisibleBlock(TTBlocks.PLACEHOLDER_TABLE.get());
        registerInvisibleBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get());
        registerInvisibleBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get());
        registerInvisibleBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get());
        registerInvisibleBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get());
    }

    private void registerConstructs() {
        flatItem(TTItems.TURRET_BASIC.get());
        flatItem(TTItems.TURRET_ADVANCED.get());
        flatItem(TTItems.ARCANE_BORE.get());
        registerInvisibleBlock(TTBlocks.ARCANE_BORE.get());
        flatItem(TTItems.GRAPPLE_GUN_TIP.get());
        flatItem(TTItems.GRAPPLE_GUN_SPOOL.get());
        flatItem(TTItems.ELDRITCH_EYE.get());
        flatItem(TTItems.RUNED_TABLET.get());
        overridesItem(
                TTItems.GRAPPLE_GUN.get(),
                TTIds.rl("item/grapple_gun_1"),
                List.of(new ItemOverride(PROPERTY_LOADED, 1.0F, TTIds.rl("item/grapple_gun_2"))));
        registerActivatorRail();
    }

    private void registerActivatorRail() {
        Block block = TTBlocks.ACTIVATOR_RAIL.get();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> cutoutOutput =
                (id, json) -> modelOutput.accept(id, () -> cutout(json.get()));
        ResourceLocation flat = ModelTemplates.RAIL_FLAT.create(block, TextureMapping.rail(block), cutoutOutput);
        ResourceLocation risingNE =
                ModelTemplates.RAIL_RAISED_NE.create(block, TextureMapping.rail(block), cutoutOutput);
        ResourceLocation risingSW =
                ModelTemplates.RAIL_RAISED_SW.create(block, TextureMapping.rail(block), cutoutOutput);
        ResourceLocation flatOn = ModelTemplates.RAIL_FLAT.createWithSuffix(
                block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), cutoutOutput);
        ResourceLocation risingNEOn = ModelTemplates.RAIL_RAISED_NE.createWithSuffix(
                block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), cutoutOutput);
        ResourceLocation risingSWOn = ModelTemplates.RAIL_RAISED_SW.createWithSuffix(
                block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), cutoutOutput);
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(block.asItem()),
                TextureMapping.layer0(TextureMapping.getBlockTexture(block)),
                modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(
                                BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
                        .generate((powered, railShape) -> switch (railShape) {
                            case NORTH_SOUTH -> v(powered ? flatOn : flat);
                            case EAST_WEST ->
                                v(powered ? flatOn : flat)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                            case ASCENDING_EAST ->
                                v(powered ? risingNEOn : risingNE)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                            case ASCENDING_WEST ->
                                v(powered ? risingSWOn : risingSW)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                            case ASCENDING_NORTH -> v(powered ? risingNEOn : risingNE);
                            case ASCENDING_SOUTH -> v(powered ? risingSWOn : risingSW);
                            default -> throw new UnsupportedOperationException();
                        })));
    }

    private void registerCasters() {
        flatItem(TTItems.FOCUS_1.get());
        flatItem(TTItems.FOCUS_2.get());
        flatItem(TTItems.FOCUS_3.get());
    }

    private void registerRobeItem(Item item, String name) {
        ModelTemplates.TWO_LAYERED_ITEM.create(
                TTIds.rl("item/" + name),
                TextureMapping.layered(TTIds.rl("item/" + name), TTIds.rl("item/" + name + "_over")),
                modelOutput);
    }

    private void registerJarBrain() {
        simpleBlock(TTBlocks.JAR_BRAIN.get(), TTIds.rl("block/jar_normal"));
        delegateItem(TTBlocks.JAR_BRAIN.get().asItem(), BEWLR_BLOCK_PARENT);
    }

    private void registerNoiseDevices() {
        registerEnabledFacingDevice(TTBlocks.ARCANE_EAR.get(), "arcane_ear_on", "arcane_ear_off", false);
        registerEnabledFacingDevice(
                TTBlocks.ARCANE_EAR_TOGGLE.get(), "arcane_ear_toggle_on", "arcane_ear_toggle_off", false);

        registerEnabledFacingDevice(TTBlocks.LAMP_ARCANE.get(), "lamp_arcane_on", "lamp_arcane_off", true);
        registerEnabledFacingDevice(TTBlocks.LAMP_GROWTH.get(), "lamp_growth_on", "lamp_growth_off", true);
        registerEnabledFacingDevice(TTBlocks.LAMP_FERTILITY.get(), "lamp_fertility_on", "lamp_fertility_off", true);

        simpleFromExisting(TTBlocks.EVERFULL_URN.get(), "everfull_urn");
        registerEnabledFacingDevice(TTBlocks.VIS_GENERATOR.get(), "vis_generator", "vis_generator", false);
        registerFacingDevice(TTBlocks.ESSENTIA_INPUT.get(), "essentia_input", false);
        registerFacingDevice(TTBlocks.ESSENTIA_OUTPUT.get(), "essentia_output", false);

        registerCondenser();
        simpleFromExisting(TTBlocks.STABILIZER.get(), "stabilizer");
        simpleFromExisting(TTBlocks.VOID_SIPHON.get(), "void_siphon");
        registerLattice(TTBlocks.CONDENSER_LATTICE.get(), "condenser_lattice_core");
        registerLattice(TTBlocks.CONDENSER_LATTICE_DIRTY.get(), "condenser_lattice_core_dirty");
        registerRelay();

        ResourceLocation thaumatoriumModel = TTIds.rl("block/thaumatorium");
        PropertyDispatch thaumatoriumFacing = horizontalDispatch();
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.THAUMATORIUM.get(), v(thaumatoriumModel))
                .with(thaumatoriumFacing));
        delegateItem(TTItems.THAUMATORIUM.get(), thaumatoriumModel);
        registerInvisibleBlock(TTBlocks.THAUMATORIUM_TOP.get());
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.BRAIN_BOX.get(), vName("brain_box"))
                .with(PropertyDispatch.property(BlockStateProperties.FACING)
                        .select(Direction.DOWN, Variant.variant())
                        .select(
                                Direction.UP,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.SOUTH,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.NORTH,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.WEST,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.EAST,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))));
        delegateItem(TTItems.BRAIN_BOX.get(), TTIds.rl("block/brain_box"));

        simpleBlock(TTBlocks.CENTRIFUGE.get(), TTIds.rl("block/centrifuge"));
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(TTItems.CENTRIFUGE.get()),
                () -> new Gson()
                        .toJsonTree(Map.of(
                                "parent",
                                "minecraft:block/block",
                                "loader",
                                "neoforge:composite",
                                "children",
                                Map.of(
                                        "housing",
                                        Map.of("parent", "thaumaturge:block/centrifuge"),
                                        "spinner",
                                        Map.of("parent", "thaumaturge:block/centrifuge_spinner")))));

        registerInvisibleBlock(TTBlocks.HUNGRY_CHEST.get());
        delegateItem(TTItems.HUNGRY_CHEST.get(), BEWLR_BLOCK_PARENT);
    }

    private void registerLattice(Block block, String coreModel) {
        ResourceLocation side = TTIds.rl("block/condenser_lattice_side");
        ResourceLocation core = TTIds.rl("block/" + coreModel);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(v(core));
        record LatticeFace(BooleanProperty property, Direction direction) {}
        List<LatticeFace> faces = List.of(
                new LatticeFace(BlockStateProperties.DOWN, Direction.DOWN),
                new LatticeFace(BlockStateProperties.UP, Direction.UP),
                new LatticeFace(BlockStateProperties.SOUTH, Direction.SOUTH),
                new LatticeFace(BlockStateProperties.NORTH, Direction.NORTH),
                new LatticeFace(BlockStateProperties.WEST, Direction.WEST),
                new LatticeFace(BlockStateProperties.EAST, Direction.EAST));
        for (LatticeFace face : faces) {
            generator = generator.with(
                    Condition.condition().term(face.property(), true),
                    Variant.merge(v(side), hangingRotation(face.direction())));
        }
        blockStateOutput.accept(generator);
        delegateItem(block.asItem(), core);
    }

    private void registerRelay() {
        ResourceLocation on = TTIds.rl("block/redstone_relay_on");
        ResourceLocation off = TTIds.rl("block/redstone_relay_off");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.REDSTONE_RELAY.get())
                .with(PropertyDispatch.properties(BlockStateProperties.POWERED, BlockStateProperties.HORIZONTAL_FACING)
                        .generate((powered, facing) -> {
                            Variant variant = v(powered ? on : off);
                            return switch (facing) {
                                case NORTH -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
                                case WEST -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                                case EAST -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
                                default -> variant;
                            };
                        })));
        delegateItem(TTItems.REDSTONE_RELAY.get(), off);
    }

    private void registerFacingDevice(Block block, String modelName, boolean hanging) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, vName(modelName))
                .with(hanging ? downBaseFacingDispatch() : upBaseFacingDispatch()));
        delegateItem(block.asItem(), TTIds.rl("block/" + modelName));
    }

    private void registerEnabledFacingDevice(Block block, String onModel, String offModel, boolean hanging) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(BlockStateProperties.ENABLED, BlockStateProperties.FACING)
                        .generate((enabled, facing) -> Variant.merge(
                                vName(enabled ? onModel : offModel),
                                hanging ? hangingRotation(facing) : facingRotation(facing)))));
        delegateItem(block.asItem(), TTIds.rl("block/" + offModel));
    }

    private void registerAuraDevices(BlockModelGenerators blockModels) {
        blockModels.createTrivialCube(TTBlocks.MATRIX_SPEED.get());
        blockModels.createTrivialCube(TTBlocks.MATRIX_COST.get());

        ResourceLocation[] batteryModels = new ResourceLocation[5];
        for (int i = 0; i < 5; i++) {
            ResourceLocation textureId = TTIds.rl("block/vis_battery_" + i);
            batteryModels[i] = ModelTemplates.CUBE_ALL.create(textureId, TextureMapping.cube(textureId), modelOutput);
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.VIS_BATTERY.get())
                .with(PropertyDispatch.property(BlockVisBattery.CHARGE).generate(charge -> {
                    int tier = charge == 0 ? 0 : charge >= 10 ? 4 : (charge + 2) / 3;
                    return v(batteryModels[tier]);
                })));
        delegateItem(TTItems.VIS_BATTERY.get(), batteryModels[0]);

        ResourceLocation dioptraOn = TTIds.rl("block/dioptra_on");
        ResourceLocation dioptraOff = TTIds.rl("block/dioptra_off");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.DIOPTRA.get())
                .with(PropertyDispatch.property(BlockStateProperties.ENABLED)
                        .select(true, v(dioptraOn))
                        .select(false, v(dioptraOff))));
        delegateItem(TTItems.DIOPTRA.get(), dioptraOn);
    }

    private void registerMirrorItem(Item item, String frameTexture) {
        ResourceLocation base = ModelLocationUtils.getModelLocation(item, "_off");
        ResourceLocation linked = ModelLocationUtils.getModelLocation(item, "_on");
        ModelTemplates.TWO_LAYERED_ITEM.create(
                base,
                TextureMapping.layered(TTIds.rl("block/" + frameTexture), TTIds.rl("block/mirrorpane")),
                modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(
                linked,
                TextureMapping.layered(TTIds.rl("block/" + frameTexture), TTIds.rl("block/mirrorpaneopen")),
                modelOutput);
        overridesItem(item, base, List.of(new ItemOverride(PROPERTY_LINKED, 1.0F, linked)));
    }

    private void mirrorBlockState(Block block) {
        ResourceLocation model = ModelTemplates.PARTICLE_ONLY.createWithSuffix(
                block, "_state", TextureMapping.particle(TTIds.rl("block/mirrorframe")), modelOutput);
        simpleBlock(block, model);
    }

    private void stoneAndStairModels() {
        simpleFromExisting(TTBlocks.STONE_ARCANE.get(), "stone_arcane");
        simpleFromExisting(TTBlocks.STONE_ARCANE_BRICK.get(), "stone_arcane_brick");
        simpleFromExisting(TTBlocks.STONE_ANCIENT.get(), "stone_ancient");
        simpleFromExisting(TTBlocks.STONE_ANCIENT_TILE.get(), "stone_ancient_tile");
        simpleFromExisting(TTBlocks.STONE_ANCIENT_ROCK.get(), "stone_ancient_rock");
        simpleFromExisting(TTBlocks.STONE_ANCIENT_GLYPHED.get(), "stone_ancient_glyphed");
        simpleFromExisting(TTBlocks.STONE_ANCIENT_DOORWAY.get(), "stone_ancient_doorway");
        simpleFromExisting(TTBlocks.STONE_ELDRITCH_TILE.get(), "stone_eldritch_tile");
        simpleFromExisting(TTBlocks.STONE_POROUS.get(), "stone_porous");

        stairsFromModels(TTBlocks.STAIRS_ARCANE.get(), "arcane_stairs", "arcane_inner_stairs", "arcane_outer_stairs");
        stairsFromModels(
                TTBlocks.STAIRS_ARCANE_BRICK.get(),
                "arcane_brick_stairs",
                "arcane_brick_inner_stairs",
                "arcane_brick_outer_stairs");
        stairsFromModels(
                TTBlocks.STAIRS_ANCIENT.get(), "ancient_stairs", "ancient_inner_stairs", "ancient_outer_stairs");
    }

    private void stairsFromModels(Block block, String straightName, String innerName, String outerName) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(stairsDispatch(
                        TTIds.rl("block/" + straightName),
                        TTIds.rl("block/" + innerName),
                        TTIds.rl("block/" + outerName))));
    }

    private static Variant stairVariant(
            ResourceLocation model, VariantProperties.Rotation xRot, VariantProperties.Rotation yRot) {
        Variant variant = v(model);
        if (xRot != VariantProperties.Rotation.R0) {
            variant = variant.with(VariantProperties.X_ROT, xRot);
        }
        if (yRot != VariantProperties.Rotation.R0) {
            variant = variant.with(VariantProperties.Y_ROT, yRot);
        }
        if (xRot != VariantProperties.Rotation.R0 || yRot != VariantProperties.Rotation.R0) {
            variant = variant.with(VariantProperties.UV_LOCK, true);
        }
        return variant;
    }

    private static PropertyDispatch stairsDispatch(
            ResourceLocation straight, ResourceLocation inner, ResourceLocation outer) {
        VariantProperties.Rotation r0 = VariantProperties.Rotation.R0;
        VariantProperties.Rotation r90 = VariantProperties.Rotation.R90;
        VariantProperties.Rotation r180 = VariantProperties.Rotation.R180;
        VariantProperties.Rotation r270 = VariantProperties.Rotation.R270;
        return PropertyDispatch.properties(
                        BlockStateProperties.HORIZONTAL_FACING,
                        BlockStateProperties.HALF,
                        BlockStateProperties.STAIRS_SHAPE)
                .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, stairVariant(straight, r0, r0))
                .select(Direction.WEST, Half.BOTTOM, StairsShape.STRAIGHT, stairVariant(straight, r0, r180))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.STRAIGHT, stairVariant(straight, r0, r90))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.STRAIGHT, stairVariant(straight, r0, r270))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, stairVariant(outer, r0, r0))
                .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_RIGHT, stairVariant(outer, r0, r180))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, stairVariant(outer, r0, r90))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, stairVariant(outer, r0, r270))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_LEFT, stairVariant(outer, r0, r270))
                .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_LEFT, stairVariant(outer, r0, r90))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, stairVariant(outer, r0, r0))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT, stairVariant(outer, r0, r180))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, stairVariant(inner, r0, r0))
                .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_RIGHT, stairVariant(inner, r0, r180))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_RIGHT, stairVariant(inner, r0, r90))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_RIGHT, stairVariant(inner, r0, r270))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_LEFT, stairVariant(inner, r0, r270))
                .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_LEFT, stairVariant(inner, r0, r90))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, stairVariant(inner, r0, r0))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_LEFT, stairVariant(inner, r0, r180))
                .select(Direction.EAST, Half.TOP, StairsShape.STRAIGHT, stairVariant(straight, r180, r0))
                .select(Direction.WEST, Half.TOP, StairsShape.STRAIGHT, stairVariant(straight, r180, r180))
                .select(Direction.SOUTH, Half.TOP, StairsShape.STRAIGHT, stairVariant(straight, r180, r90))
                .select(Direction.NORTH, Half.TOP, StairsShape.STRAIGHT, stairVariant(straight, r180, r270))
                .select(Direction.EAST, Half.TOP, StairsShape.OUTER_RIGHT, stairVariant(outer, r180, r90))
                .select(Direction.WEST, Half.TOP, StairsShape.OUTER_RIGHT, stairVariant(outer, r180, r270))
                .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_RIGHT, stairVariant(outer, r180, r180))
                .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_RIGHT, stairVariant(outer, r180, r0))
                .select(Direction.EAST, Half.TOP, StairsShape.OUTER_LEFT, stairVariant(outer, r180, r0))
                .select(Direction.WEST, Half.TOP, StairsShape.OUTER_LEFT, stairVariant(outer, r180, r180))
                .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_LEFT, stairVariant(outer, r180, r90))
                .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_LEFT, stairVariant(outer, r180, r270))
                .select(Direction.EAST, Half.TOP, StairsShape.INNER_RIGHT, stairVariant(inner, r180, r90))
                .select(Direction.WEST, Half.TOP, StairsShape.INNER_RIGHT, stairVariant(inner, r180, r270))
                .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_RIGHT, stairVariant(inner, r180, r180))
                .select(Direction.NORTH, Half.TOP, StairsShape.INNER_RIGHT, stairVariant(inner, r180, r0))
                .select(Direction.EAST, Half.TOP, StairsShape.INNER_LEFT, stairVariant(inner, r180, r0))
                .select(Direction.WEST, Half.TOP, StairsShape.INNER_LEFT, stairVariant(inner, r180, r180))
                .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_LEFT, stairVariant(inner, r180, r90))
                .select(Direction.NORTH, Half.TOP, StairsShape.INNER_LEFT, stairVariant(inner, r180, r270));
    }

    private void treeModels() {
        simpleFromExisting(TTBlocks.SAPLING_GREATWOOD.get(), "sapling_greatwood");
        simpleFromExisting(TTBlocks.SAPLING_SILVERWOOD.get(), "sapling_silverwood");
        flowerPotCross(TTBlocks.POTTED_SAPLING_GREATWOOD.get(), TTBlocks.SAPLING_GREATWOOD.get());
        flowerPotCross(TTBlocks.POTTED_SAPLING_SILVERWOOD.get(), TTBlocks.SAPLING_SILVERWOOD.get());
        flatItemFromBlock(TTItems.SAPLING_GREATWOOD.get(), TTBlocks.SAPLING_GREATWOOD.get());
        flatItemFromBlock(TTItems.SAPLING_SILVERWOOD.get(), TTBlocks.SAPLING_SILVERWOOD.get());
        simpleFromExisting(TTBlocks.PLANK_GREATWOOD.get(), "plank_greatwood");
        simpleFromExisting(TTBlocks.PLANK_SILVERWOOD.get(), "plank_silverwood");
        simpleFromExisting(TTBlocks.LEAVES_GREATWOOD.get(), "leaves_greatwood");
        simpleFromExisting(TTBlocks.LEAVES_SILVERWOOD.get(), "leaves_silverwood");
        log(TTBlocks.LOG_GREATWOOD.get(), TTBlocks.WOOD_GREATWOOD.get());
        log(TTBlocks.LOG_SILVERWOOD.get(), TTBlocks.WOOD_SILVERWOOD.get());
        axisPillar(
                TTBlocks.SILVERWOOD_NODE_LOG.get(),
                TTIds.rl("block/log_silverwood"),
                TTIds.rl("block/log_silverwood_horizontal"));
        log(TTBlocks.STRIPPED_LOG_GREATWOOD.get(), TTBlocks.STRIPPED_WOOD_GREATWOOD.get());
        log(TTBlocks.STRIPPED_LOG_SILVERWOOD.get(), TTBlocks.STRIPPED_WOOD_SILVERWOOD.get());
    }

    private void log(Block log, Block wood) {
        TextureMapping logMapping = TextureMapping.logColumn(log);
        ResourceLocation vertical = ModelTemplates.CUBE_COLUMN.create(log, logMapping, modelOutput);
        ResourceLocation horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(log, logMapping, modelOutput);
        axisPillar(log, vertical, horizontal);
        delegateItem(log.asItem(), vertical);

        TextureMapping woodMapping = logMapping.copyAndUpdate(TextureSlot.END, logMapping.get(TextureSlot.SIDE));
        ResourceLocation woodModel = ModelTemplates.CUBE_COLUMN.create(wood, woodMapping, modelOutput);
        axisPillar(wood, woodModel, woodModel);
        delegateItem(wood.asItem(), woodModel);
    }

    private void axisPillar(Block block, ResourceLocation vertical, ResourceLocation horizontal) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.property(BlockStateProperties.AXIS)
                        .select(Direction.Axis.Y, v(vertical))
                        .select(
                                Direction.Axis.Z,
                                v(horizontal).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.Axis.X,
                                v(horizontal)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))));
    }

    private void plantModels() {
        cross(TTBlocks.PLANT_SHIMMERLEAF.get());
        cross(TTBlocks.PLANT_CINDERPEARL.get());
        cross(TTBlocks.PLANT_VISHROOM.get());
        flowerPotCross(TTBlocks.POTTED_SHIMMERLEAF.get(), TTBlocks.PLANT_SHIMMERLEAF.get());
        flowerPotCross(TTBlocks.POTTED_CINDERPEARL.get(), TTBlocks.PLANT_CINDERPEARL.get());
        flowerPotCross(TTBlocks.POTTED_VISHROOM.get(), TTBlocks.PLANT_VISHROOM.get());

        flatItemFromBlock(TTItems.PLANT_SHIMMERLEAF.get(), TTBlocks.PLANT_SHIMMERLEAF.get());
        flatItemFromBlock(TTItems.PLANT_CINDERPEARL.get(), TTBlocks.PLANT_CINDERPEARL.get());
        flatItemFromBlock(TTItems.PLANT_VISHROOM.get(), TTBlocks.PLANT_VISHROOM.get());

        ResourceLocation grassModel = TTIds.rl("block/grass_ambient");
        modelOutput.accept(grassModel, () -> {
            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:block/grass_block");
            model.addProperty("render_type", "minecraft:cutout_mipped");
            return model;
        });
        ResourceLocation bloomModel = ModelTemplates.CROSS.create(
                TTBlocks.ETHEREAL_BLOOM.get(),
                TextureMapping.cross(TTBlocks.PLANT_SHIMMERLEAF.get()),
                (id, json) -> modelOutput.accept(id, () -> {
                    JsonElement element = json.get();
                    element.getAsJsonObject().addProperty("render_type", "minecraft:cutout");
                    return element;
                }));
        simpleBlock(TTBlocks.ETHEREAL_BLOOM.get(), bloomModel);
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(TTItems.ETHEREAL_BLOOM.get()),
                TextureMapping.layer0(TextureMapping.getBlockTexture(TTBlocks.PLANT_SHIMMERLEAF.get())),
                modelOutput);

        simpleBlock(TTBlocks.GRASS_AMBIENT.get(), grassModel);
        delegateItem(TTItems.GRASS_AMBIENT.get(), grassModel);
    }

    private void flatItemFromBlock(Item item, Block block) {
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item),
                TextureMapping.layer0(TextureMapping.getBlockTexture(block)),
                modelOutput);
    }

    private void cross(Block block) {
        ResourceLocation model = ModelTemplates.CROSS.create(
                block,
                TextureMapping.cross(block),
                (id, json) -> modelOutput.accept(id, () -> {
                    JsonElement element = json.get();
                    element.getAsJsonObject().addProperty("render_type", "minecraft:cutout");
                    return element;
                }));
        simpleBlock(block, model);
    }

    private void flowerPotCross(Block pot, Block plant) {
        ResourceLocation model = ModelTemplates.FLOWER_POT_CROSS.create(
                pot,
                TextureMapping.plant(plant),
                (id, json) -> modelOutput.accept(id, () -> {
                    JsonElement element = json.get();
                    element.getAsJsonObject().addProperty("render_type", "minecraft:cutout");
                    return element;
                }));
        simpleBlock(pot, model);
    }

    private void taintModels() {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                TTBlocks.TAINT_ROCK.get(), rotatedWeighted(new String[] {"taint_rock"}, new int[] {1})));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                TTBlocks.TAINT_SOIL.get(),
                rotatedWeighted(new String[] {"taint_soil_0", "taint_soil_1", "taint_soil_2"}, new int[] {16, 1, 1})));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                TTBlocks.TAINT_CRUST.get(),
                rotatedWeighted(
                        new String[] {"taint_crust_0", "taint_crust_1", "taint_crust_2"}, new int[] {8, 1, 1})));

        simpleFromExisting(TTBlocks.FLUX_GOO.get(), "flux_goo");
        translucentCube(TTBlocks.FLUX_GAS.get());
        simpleFromExisting(TTBlocks.TAINT_GEYSER.get(), "taint_geyser");
        registerTaintLog();
        registerTaintFibre();
        registerTaintSporeStalk();

        delegateItem(TTBlocks.TAINT_ROCK.asItem(), TTIds.rl("block/taint_rock"));
        delegateItem(TTBlocks.TAINT_SOIL.asItem(), TTIds.rl("block/taint_soil_0"));
        delegateItem(TTBlocks.TAINT_CRUST.asItem(), TTIds.rl("block/taint_crust_0"));
        delegateItem(TTBlocks.TAINT_GEYSER.asItem(), TTIds.rl("block/taint_geyser"));
        delegateItem(TTBlocks.TAINT_LOG.asItem(), TTIds.rl("block/taint_log"));
        delegateItem(TTBlocks.TAINT_FEATURE.asItem(), TTIds.rl("block/taint_orb_0"));
        delegateItem(TTBlocks.TAINT_FIBRE.asItem(), TTIds.rl("block/taint_fibre"));
        delegateItem(TTBlocks.TAINT_SPORE_STALK.asItem(), TTIds.rl("block/taint_spore_stalk_immature"));
    }

    private void registerTaintSporeStalk() {
        Block stalk = TTBlocks.TAINT_SPORE_STALK.get();
        ResourceLocation immature = ModelTemplates.CROSS.createWithSuffix(
                stalk,
                "_immature",
                TextureMapping.cross(TTIds.rl("block/taint_spore_stalk_1")),
                (id, json) -> modelOutput.accept(id, () -> {
                    JsonElement element = json.get();
                    element.getAsJsonObject().addProperty("render_type", "minecraft:cutout");
                    return element;
                }));
        ResourceLocation mature = ModelTemplates.CROSS.createWithSuffix(
                stalk,
                "_mature",
                TextureMapping.cross(TTIds.rl("block/taint_spore_stalk_2")),
                (id, json) -> modelOutput.accept(id, () -> {
                    JsonElement element = json.get();
                    element.getAsJsonObject().addProperty("render_type", "minecraft:cutout");
                    return element;
                }));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(stalk)
                .with(PropertyDispatch.property(BlockTaintSporeStalk.MATURE)
                        .select(false, v(immature))
                        .select(true, v(mature))));
    }

    private void registerTaintLog() {
        List<Variant> barks = new ArrayList<>();
        for (int tex = 1; tex <= 2; tex++) {
            for (String face : new String[] {"north", "south", "east", "west"}) {
                barks.add(vName("taint_log_" + face + tex));
            }
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                        TTBlocks.TAINT_LOG.get(), barks.toArray(Variant[]::new))
                .with(PropertyDispatch.property(BlockStateProperties.AXIS)
                        .select(Direction.Axis.Y, Variant.variant())
                        .select(
                                Direction.Axis.Z,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.Axis.X,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))));
    }

    private Variant[] rotatedWeighted(String[] models, int[] weights) {
        List<Variant> entries = new ArrayList<>();
        for (int i = 0; i < models.length; i++) {
            ResourceLocation model = TTIds.rl("block/" + models[i]);
            entries.add(v(model).with(VariantProperties.WEIGHT, weights[i]));
            entries.add(v(model).with(VariantProperties.WEIGHT, weights[i])
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90));
            entries.add(v(model).with(VariantProperties.WEIGHT, weights[i])
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
            entries.add(v(model).with(VariantProperties.WEIGHT, weights[i])
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
        }
        return entries.toArray(Variant[]::new);
    }

    private void registerTaintFibre() {
        ResourceLocation fibre = TTIds.rl("block/taint_fibre");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(TTBlocks.TAINT_FIBRE.get())
                .with(Condition.condition().term(BlockTaintFibre.NORTH, true), v(fibre))
                .with(
                        Condition.condition().term(BlockTaintFibre.EAST, true),
                        v(fibre).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .with(
                        Condition.condition().term(BlockTaintFibre.SOUTH, true),
                        v(fibre).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(
                        Condition.condition().term(BlockTaintFibre.WEST, true),
                        v(fibre).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .with(
                        Condition.condition().term(BlockTaintFibre.UP, true),
                        v(fibre).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                .with(
                        Condition.condition().term(BlockTaintFibre.DOWN, true),
                        v(fibre).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                .with(Condition.condition().term(BlockTaintFibre.GROWTH1, true), vName("taint_growth_1"))
                .with(Condition.condition().term(BlockTaintFibre.GROWTH2, true), vName("taint_growth_2"))
                .with(Condition.condition().term(BlockTaintFibre.GROWTH3, true), vName("taint_growth_3"))
                .with(Condition.condition().term(BlockTaintFibre.GROWTH4, true), vName("taint_growth_4"));
        blockStateOutput.accept(generator);
    }

    private void decorModels() {
        slab(
                TTBlocks.SLAB_GREATWOOD.get(),
                TTBlocks.PLANK_GREATWOOD.get(),
                blockTexture("plank_greatwood"),
                blockTexture("plank_greatwood"),
                blockTexture("plank_greatwood"));
        slab(
                TTBlocks.SLAB_SILVERWOOD.get(),
                TTBlocks.PLANK_SILVERWOOD.get(),
                blockTexture("plank_silverwood"),
                blockTexture("plank_silverwood"),
                blockTexture("plank_silverwood"));
        slab(
                TTBlocks.SLAB_ARCANE_STONE.get(),
                TTBlocks.STONE_ARCANE.get(),
                blockTexture("arcane_stone_1"),
                blockTexture("arcane_stone_2"),
                blockTexture("arcane_stone_3"));
        slab(
                TTBlocks.SLAB_ARCANE_BRICK.get(),
                TTBlocks.STONE_ARCANE_BRICK.get(),
                blockTexture("arcane_brick_stone"),
                blockTexture("arcane_brick_stone"),
                blockTexture("arcane_brick_stone"));
        slab(
                TTBlocks.SLAB_ANCIENT.get(),
                TTBlocks.STONE_ANCIENT.get(),
                blockTexture("ancient_stone_1"),
                blockTexture("ancient_stone_2"),
                blockTexture("ancient_stone_3"));
        slab(
                TTBlocks.SLAB_ELDRITCH.get(),
                TTBlocks.STONE_ELDRITCH_TILE.get(),
                blockTexture("eldritch_stone_1"),
                blockTexture("eldritch_stone_2"),
                blockTexture("eldritch_stone_3"));
        stairsFromTexture(TTBlocks.STAIRS_GREATWOOD.get(), blockTexture("plank_greatwood"));
        stairsFromTexture(TTBlocks.STAIRS_SILVERWOOD.get(), blockTexture("plank_silverwood"));
        arcaneGrindstone();
        woodFamily(
                blockTexture("plank_greatwood"),
                TTBlocks.DOOR_GREATWOOD.get(),
                TTBlocks.TRAPDOOR_GREATWOOD.get(),
                TTBlocks.FENCE_GREATWOOD.get(),
                TTBlocks.FENCE_GATE_GREATWOOD.get(),
                TTBlocks.BUTTON_GREATWOOD.get(),
                TTBlocks.PRESSURE_PLATE_GREATWOOD.get());
        woodFamily(
                blockTexture("plank_silverwood"),
                TTBlocks.DOOR_SILVERWOOD.get(),
                TTBlocks.TRAPDOOR_SILVERWOOD.get(),
                TTBlocks.FENCE_SILVERWOOD.get(),
                TTBlocks.FENCE_GATE_SILVERWOOD.get(),
                TTBlocks.BUTTON_SILVERWOOD.get(),
                TTBlocks.PRESSURE_PLATE_SILVERWOOD.get());
        existingModelWithItem(TTBlocks.TABLE_WOOD.get(), "table_wood");
        existingModelWithItem(TTBlocks.TABLE_STONE.get(), "table_stone");
        paving(TTBlocks.PAVING_STONE_TRAVEL.get(), "paving_stone_travel");
        paving(TTBlocks.PAVING_STONE_BARRIER.get(), "paving_stone_barrier");
        wardedGlass();
        golemFetter();
        tallowBlock();
        itemGrate();
        arcaneDoor();
        arcanePressurePlate();
    }

    private void arcaneDoor() {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.TOP, blockTexture("arcane_door_top"))
                .put(TextureSlot.BOTTOM, blockTexture("arcane_door_bottom"));
        ResourceLocation bottomLeft =
                ModelTemplates.DOOR_BOTTOM_LEFT.create(TTBlocks.ARCANE_DOOR.get(), textures, modelOutput);
        ResourceLocation bottomLeftOpen =
                ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(TTBlocks.ARCANE_DOOR.get(), textures, modelOutput);
        ResourceLocation bottomRight =
                ModelTemplates.DOOR_BOTTOM_RIGHT.create(TTBlocks.ARCANE_DOOR.get(), textures, modelOutput);
        ResourceLocation bottomRightOpen =
                ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(TTBlocks.ARCANE_DOOR.get(), textures, modelOutput);
        ResourceLocation topLeft = TTIds.rl("block/arcane_door_top_left");
        ResourceLocation topLeftOpen = TTIds.rl("block/arcane_door_top_left_open");
        ResourceLocation topRight = TTIds.rl("block/arcane_door_top_right");
        ResourceLocation topRightOpen = TTIds.rl("block/arcane_door_top_right_open");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.ARCANE_DOOR.get())
                .with(doorHalf(
                        doorHalf(
                                PropertyDispatch.properties(
                                        BlockStateProperties.HORIZONTAL_FACING,
                                        BlockStateProperties.DOUBLE_BLOCK_HALF,
                                        BlockStateProperties.DOOR_HINGE,
                                        BlockStateProperties.OPEN),
                                DoubleBlockHalf.LOWER,
                                bottomLeft,
                                bottomLeftOpen,
                                bottomRight,
                                bottomRightOpen),
                        DoubleBlockHalf.UPPER,
                        topLeft,
                        topLeftOpen,
                        topRight,
                        topRightOpen)));
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(TTBlocks.ARCANE_DOOR.get().asItem()),
                TextureMapping.layer0(TTIds.rl("item/arcane_door")),
                modelOutput);
    }

    private void golemFetter() {
        TextureMapping normal = new TextureMapping()
                .put(TextureSlot.BOTTOM, blockTexture("golem_fetter_side"))
                .put(TextureSlot.SIDE, blockTexture("golem_fetter_side"))
                .put(TextureSlot.TOP, blockTexture("golem_fetter"));
        TextureMapping active = new TextureMapping()
                .put(TextureSlot.BOTTOM, blockTexture("golem_fetter_side"))
                .put(TextureSlot.SIDE, blockTexture("golem_fetter_side"))
                .put(TextureSlot.TOP, blockTexture("golem_fetter_active"));
        ResourceLocation off = ModelTemplates.CUBE_BOTTOM_TOP.create(TTBlocks.GOLEM_FETTER.get(), normal, modelOutput);
        ResourceLocation on = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(
                TTBlocks.GOLEM_FETTER.get(), "_powered", active, modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.GOLEM_FETTER.get())
                .with(PropertyDispatch.property(com.leclowndu93150.thaumaturge.content.golem.BlockGolemFetter.POWERED)
                        .select(false, v(off))
                        .select(true, v(on))));
        delegateItem(TTBlocks.GOLEM_FETTER.get().asItem(), off);
    }

    private void tallowBlock() {
        ResourceLocation model = ModelTemplates.CUBE_BOTTOM_TOP.create(
                TTBlocks.TALLOW_BLOCK.get(),
                new TextureMapping()
                        .put(TextureSlot.BOTTOM, blockTexture("tallow_block"))
                        .put(TextureSlot.SIDE, blockTexture("tallow_block"))
                        .put(TextureSlot.TOP, blockTexture("tallow_block_top")),
                modelOutput);
        simpleBlock(TTBlocks.TALLOW_BLOCK.get(), model);
        delegateItem(TTBlocks.TALLOW_BLOCK.get().asItem(), model);
    }

    private void itemGrate() {
        ResourceLocation open = TTIds.rl("block/item_grate");
        ResourceLocation closed = TTIds.rl("block/item_grate_closed");
        modelOutput.accept(open, () -> itemGrateModel(false));
        modelOutput.accept(closed, () -> itemGrateModel(true));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.ITEM_GRATE.get())
                .with(PropertyDispatch.property(com.leclowndu93150.thaumaturge.content.device.BlockItemGrate.OPEN)
                        .select(true, v(open))
                        .select(false, v(closed))));
        delegateItem(TTBlocks.ITEM_GRATE.get().asItem(), open);
    }

    private static JsonObject itemGrateModel(boolean closed) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");
        root.addProperty("render_type", "minecraft:cutout");
        JsonObject textures = new JsonObject();
        ResourceLocation grateTexture = blockTexture("item_grate");
        textures.addProperty("particle", grateTexture.toString());
        textures.addProperty("all", grateTexture.toString());
        textures.addProperty("hatch", blockTexture("item_grate_closed").toString());
        root.add("textures", textures);

        JsonObject element = new JsonObject();
        element.add("from", insetCoords(0, 14, 0));
        element.add("to", insetCoords(16, 16, 16));
        JsonObject faces = new JsonObject();
        JsonObject top = new JsonObject();
        top.addProperty("texture", "#all");
        top.addProperty("cullface", Direction.UP.getSerializedName());
        faces.add(Direction.UP.getSerializedName(), top);
        JsonObject bottom = new JsonObject();
        bottom.addProperty("texture", "#all");
        bottom.addProperty("cullface", Direction.DOWN.getSerializedName());
        faces.add(Direction.DOWN.getSerializedName(), bottom);
        for (Direction direction : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#all");
            face.addProperty("cullface", direction.getSerializedName());
            JsonArray uv = new JsonArray();
            uv.add(0);
            uv.add(15);
            uv.add(16);
            uv.add(16);
            face.add("uv", uv);
            faces.add(direction.getSerializedName(), face);
        }
        element.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(element);
        if (closed) {
            JsonObject hatch = new JsonObject();
            hatch.add("from", insetCoords(0, 14, 0));
            hatch.add("to", insetCoords(16, 16, 16));
            JsonObject hatchTop = new JsonObject();
            hatchTop.addProperty("texture", "#hatch");
            hatchTop.addProperty("cullface", Direction.UP.getSerializedName());
            JsonObject hatchFaces = new JsonObject();
            hatchFaces.add(Direction.UP.getSerializedName(), hatchTop);
            JsonObject hatchBottom = new JsonObject();
            hatchBottom.addProperty("texture", "#hatch");
            hatchBottom.addProperty("cullface", Direction.DOWN.getSerializedName());
            hatchFaces.add(Direction.DOWN.getSerializedName(), hatchBottom);
            hatch.add("faces", hatchFaces);
            elements.add(hatch);
        }
        root.add("elements", elements);
        return root;
    }

    private void wardedGlass() {
        ResourceLocation model = TTIds.rl("block/warded_glass");
        modelOutput.accept(model, () -> wardedGlassModel());
        simpleBlock(TTBlocks.WARDED_GLASS.get(), model);
        ResourceLocation item = ModelTemplates.CUBE_ALL.createWithSuffix(
                TTBlocks.WARDED_GLASS.get(),
                "_item",
                TextureMapping.cube(TTBlocks.WARDED_GLASS.get()),
                (id, json) -> modelOutput.accept(id, () -> translucent(json.get())));
        delegateItem(TTBlocks.WARDED_GLASS.get().asItem(), item);
    }

    private static JsonElement wardedGlassModel() {
        JsonObject root = new JsonObject();
        root.addProperty("loader", "thaumaturge:warded_glass");
        root.addProperty("render_type", "minecraft:translucent");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "thaumaturge:block/warded_glass");
        for (int i = 1; i <= 47; i++) {
            textures.addProperty("ctm_" + i, "thaumaturge:block/warded_glass_" + i);
        }
        root.add("textures", textures);
        return root;
    }

    private static JsonElement cutout(JsonElement json) {
        JsonObject element = json.getAsJsonObject();
        element.addProperty("render_type", "minecraft:cutout");
        return element;
    }

    private static JsonElement translucent(JsonElement json) {
        JsonObject element = json.getAsJsonObject();
        element.addProperty("render_type", "minecraft:translucent");
        return element;
    }

    private void arcanePressurePlate() {
        ResourceLocation[] up = new ResourceLocation[3];
        ResourceLocation[] down = new ResourceLocation[3];
        for (int mode = 0; mode <= 2; mode++) {
            TextureMapping textures =
                    new TextureMapping().put(TextureSlot.TEXTURE, blockTexture("arcane_pressure_plate_" + mode));
            up[mode] = ModelTemplates.PRESSURE_PLATE_UP.createWithSuffix(
                    TTBlocks.ARCANE_PRESSURE_PLATE.get(), "_" + mode, textures, modelOutput);
            down[mode] = ModelTemplates.PRESSURE_PLATE_DOWN.createWithSuffix(
                    TTBlocks.ARCANE_PRESSURE_PLATE.get(), "_" + mode, textures, modelOutput);
        }
        PropertyDispatch.C2<Boolean, Integer> states = PropertyDispatch.properties(
                BlockStateProperties.POWERED,
                com.leclowndu93150.thaumaturge.content.warding.BlockArcanePressurePlate.MODE);
        for (int mode = 0; mode <= 2; mode++) {
            states = states.select(false, mode, v(up[mode])).select(true, mode, v(down[mode]));
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.ARCANE_PRESSURE_PLATE.get())
                .with(states));
        delegateItem(TTBlocks.ARCANE_PRESSURE_PLATE.get().asItem(), up[0]);
    }

    private static PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> doorHalf(
            PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> properties,
            DoubleBlockHalf half,
            ResourceLocation left,
            ResourceLocation leftOpen,
            ResourceLocation right,
            ResourceLocation rightOpen) {
        return properties
                .select(Direction.EAST, half, DoorHingeSide.LEFT, false, v(left))
                .select(
                        Direction.SOUTH,
                        half,
                        DoorHingeSide.LEFT,
                        false,
                        v(left).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.WEST,
                        half,
                        DoorHingeSide.LEFT,
                        false,
                        v(left).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(
                        Direction.NORTH,
                        half,
                        DoorHingeSide.LEFT,
                        false,
                        v(left).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .select(Direction.EAST, half, DoorHingeSide.RIGHT, false, v(right))
                .select(
                        Direction.SOUTH,
                        half,
                        DoorHingeSide.RIGHT,
                        false,
                        v(right).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.WEST,
                        half,
                        DoorHingeSide.RIGHT,
                        false,
                        v(right).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(
                        Direction.NORTH,
                        half,
                        DoorHingeSide.RIGHT,
                        false,
                        v(right).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .select(
                        Direction.EAST,
                        half,
                        DoorHingeSide.LEFT,
                        true,
                        v(leftOpen).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.SOUTH,
                        half,
                        DoorHingeSide.LEFT,
                        true,
                        v(leftOpen).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(
                        Direction.WEST,
                        half,
                        DoorHingeSide.LEFT,
                        true,
                        v(leftOpen).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .select(Direction.NORTH, half, DoorHingeSide.LEFT, true, v(leftOpen))
                .select(
                        Direction.EAST,
                        half,
                        DoorHingeSide.RIGHT,
                        true,
                        v(rightOpen).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .select(Direction.SOUTH, half, DoorHingeSide.RIGHT, true, v(rightOpen))
                .select(
                        Direction.WEST,
                        half,
                        DoorHingeSide.RIGHT,
                        true,
                        v(rightOpen).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.NORTH,
                        half,
                        DoorHingeSide.RIGHT,
                        true,
                        v(rightOpen).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
    }

    private static ResourceLocation blockTexture(String name) {
        return TTIds.rl("block/" + name);
    }

    private void slab(
            Block slab, Block fullBlock, ResourceLocation bottom, ResourceLocation top, ResourceLocation side) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top)
                .put(TextureSlot.SIDE, side);
        ResourceLocation bottomModel = ModelTemplates.SLAB_BOTTOM.create(slab, mapping, modelOutput);
        ResourceLocation topModel = ModelTemplates.SLAB_TOP.create(slab, mapping, modelOutput);
        ResourceLocation doubleModel = ModelLocationUtils.getModelLocation(fullBlock);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(slab)
                .with(PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
                        .select(SlabType.BOTTOM, v(bottomModel))
                        .select(SlabType.TOP, v(topModel))
                        .select(SlabType.DOUBLE, v(doubleModel))));
        delegateItem(slab.asItem(), bottomModel);
    }

    private void woodFamily(
            ResourceLocation plankTexture,
            Block door,
            Block trapdoor,
            Block fence,
            Block fenceGate,
            Block button,
            Block pressurePlate) {
        BiConsumer<ResourceLocation, Supplier<JsonElement>> cutoutOutput =
                (id, json) -> modelOutput.accept(id, () -> cutout(json.get()));
        TextureMapping doorTextures = TextureMapping.door(door);
        ResourceLocation bottomLeft = ModelTemplates.DOOR_BOTTOM_LEFT.create(door, doorTextures, cutoutOutput);
        ResourceLocation bottomLeftOpen = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(door, doorTextures, cutoutOutput);
        ResourceLocation bottomRight = ModelTemplates.DOOR_BOTTOM_RIGHT.create(door, doorTextures, cutoutOutput);
        ResourceLocation bottomRightOpen =
                ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(door, doorTextures, cutoutOutput);
        ResourceLocation topLeft = ModelTemplates.DOOR_TOP_LEFT.create(door, doorTextures, cutoutOutput);
        ResourceLocation topLeftOpen = ModelTemplates.DOOR_TOP_LEFT_OPEN.create(door, doorTextures, cutoutOutput);
        ResourceLocation topRight = ModelTemplates.DOOR_TOP_RIGHT.create(door, doorTextures, cutoutOutput);
        ResourceLocation topRightOpen = ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(door, doorTextures, cutoutOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(door)
                .with(doorHalf(
                        doorHalf(
                                PropertyDispatch.properties(
                                        BlockStateProperties.HORIZONTAL_FACING,
                                        BlockStateProperties.DOUBLE_BLOCK_HALF,
                                        BlockStateProperties.DOOR_HINGE,
                                        BlockStateProperties.OPEN),
                                DoubleBlockHalf.LOWER,
                                bottomLeft,
                                bottomLeftOpen,
                                bottomRight,
                                bottomRightOpen),
                        DoubleBlockHalf.UPPER,
                        topLeft,
                        topLeftOpen,
                        topRight,
                        topRightOpen)));
        flatItem(door.asItem());

        TextureMapping trapdoorTextures = TextureMapping.defaultTexture(trapdoor);
        ResourceLocation trapdoorTop = ModelTemplates.TRAPDOOR_TOP.create(trapdoor, trapdoorTextures, cutoutOutput);
        ResourceLocation trapdoorBottom =
                ModelTemplates.TRAPDOOR_BOTTOM.create(trapdoor, trapdoorTextures, cutoutOutput);
        ResourceLocation trapdoorOpen = ModelTemplates.TRAPDOOR_OPEN.create(trapdoor, trapdoorTextures, cutoutOutput);
        PropertyDispatch.C3<Direction, Half, Boolean> trapdoorStates = PropertyDispatch.properties(
                BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : Half.values()) {
                trapdoorStates.select(facing, half, false, v(half == Half.TOP ? trapdoorTop : trapdoorBottom));
                trapdoorStates.select(
                        facing, half, true, v(trapdoorOpen).with(VariantProperties.Y_ROT, turnsFromNorth(facing, 0)));
            }
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(trapdoor).with(trapdoorStates));
        delegateItem(trapdoor.asItem(), trapdoorBottom);

        TextureMapping plank = new TextureMapping().put(TextureSlot.TEXTURE, plankTexture);
        ResourceLocation fencePost = ModelTemplates.FENCE_POST.create(fence, plank, modelOutput);
        ResourceLocation fenceSide = ModelTemplates.FENCE_SIDE.create(fence, plank, modelOutput);
        MultiPartGenerator fenceParts = MultiPartGenerator.multiPart(fence).with(v(fencePost));
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            fenceParts.with(
                    Condition.condition().term(PipeBlock.PROPERTY_BY_DIRECTION.get(facing), true),
                    v(fenceSide)
                            .with(VariantProperties.Y_ROT, turnsFromNorth(facing, 0))
                            .with(VariantProperties.UV_LOCK, true));
        }
        blockStateOutput.accept(fenceParts);
        delegateItem(fence.asItem(), ModelTemplates.FENCE_INVENTORY.create(fence, plank, modelOutput));

        ResourceLocation gateOpen = ModelTemplates.FENCE_GATE_OPEN.create(fenceGate, plank, modelOutput);
        ResourceLocation gateClosed = ModelTemplates.FENCE_GATE_CLOSED.create(fenceGate, plank, modelOutput);
        ResourceLocation gateWallOpen = ModelTemplates.FENCE_GATE_WALL_OPEN.create(fenceGate, plank, modelOutput);
        ResourceLocation gateWallClosed = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(fenceGate, plank, modelOutput);
        PropertyDispatch.C1<Direction> gateFacing = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            gateFacing.select(
                    facing, Variant.variant().with(VariantProperties.Y_ROT, turnsFromNorth(facing, HALF_TURN)));
        }
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(fenceGate, Variant.variant().with(VariantProperties.UV_LOCK, true))
                        .with(gateFacing)
                        .with(PropertyDispatch.properties(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
                                .select(false, false, v(gateClosed))
                                .select(true, false, v(gateWallClosed))
                                .select(false, true, v(gateOpen))
                                .select(true, true, v(gateWallOpen))));
        delegateItem(fenceGate.asItem(), gateClosed);

        ResourceLocation buttonUp = ModelTemplates.BUTTON.create(button, plank, modelOutput);
        ResourceLocation buttonDown = ModelTemplates.BUTTON_PRESSED.create(button, plank, modelOutput);
        PropertyDispatch.C2<AttachFace, Direction> buttonFacing =
                PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            buttonFacing.select(
                    AttachFace.FLOOR,
                    facing,
                    Variant.variant().with(VariantProperties.Y_ROT, turnsFromNorth(facing, 0)));
            buttonFacing.select(
                    AttachFace.WALL,
                    facing,
                    Variant.variant()
                            .with(VariantProperties.Y_ROT, turnsFromNorth(facing, 0))
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true));
            buttonFacing.select(
                    AttachFace.CEILING,
                    facing,
                    Variant.variant()
                            .with(VariantProperties.Y_ROT, turnsFromNorth(facing, HALF_TURN))
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180));
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(button)
                .with(PropertyDispatch.property(BlockStateProperties.POWERED)
                        .select(false, v(buttonUp))
                        .select(true, v(buttonDown)))
                .with(buttonFacing));
        delegateItem(button.asItem(), ModelTemplates.BUTTON_INVENTORY.create(button, plank, modelOutput));

        ResourceLocation plateUp = ModelTemplates.PRESSURE_PLATE_UP.create(pressurePlate, plank, modelOutput);
        ResourceLocation plateDown = ModelTemplates.PRESSURE_PLATE_DOWN.create(pressurePlate, plank, modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(pressurePlate)
                .with(PropertyDispatch.property(BlockStateProperties.POWERED)
                        .select(false, v(plateUp))
                        .select(true, v(plateDown))));
        delegateItem(pressurePlate.asItem(), plateUp);
    }

    private static VariantProperties.Rotation turnsFromNorth(Direction facing, int extraTurns) {
        return VariantProperties.Rotation.values()[
                Math.floorMod(facing.get2DDataValue() + NORTH_TO_SOUTH_TURNS + extraTurns, QUARTER_TURNS)];
    }

    private void arcaneGrindstone() {
        Block block = TTBlocks.ARCANE_GRINDSTONE.get();
        ResourceLocation wheel = blockTexture("arcane_stone_1");
        ResourceLocation frame = blockTexture("metal_thaumium");
        ResourceLocation model = ModelLocationUtils.getModelLocation(block);
        modelOutput.accept(model, () -> {
            JsonObject textures = new JsonObject();
            textures.addProperty("pivot", frame.toString());
            textures.addProperty("round", wheel.toString());
            textures.addProperty("side", wheel.toString());
            textures.addProperty("particle", wheel.toString());
            textures.addProperty("leg", frame.toString());
            JsonObject root = new JsonObject();
            root.addProperty("parent", VANILLA_GRINDSTONE.toString());
            root.add("textures", textures);
            return root;
        });
        PropertyDispatch.C2<AttachFace, Direction> placement =
                PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            placement.select(
                    AttachFace.FLOOR, facing, v(model).with(VariantProperties.Y_ROT, turnsFromNorth(facing, 0)));
            placement.select(
                    AttachFace.WALL,
                    facing,
                    v(model).with(VariantProperties.Y_ROT, turnsFromNorth(facing, 0))
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90));
            placement.select(
                    AttachFace.CEILING,
                    facing,
                    v(model).with(VariantProperties.Y_ROT, turnsFromNorth(facing, HALF_TURN))
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180));
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(placement));
        delegateItem(block.asItem(), model);
    }

    private void stairsFromTexture(Block block, ResourceLocation all) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, all)
                .put(TextureSlot.TOP, all)
                .put(TextureSlot.SIDE, all);
        ResourceLocation straight = ModelTemplates.STAIRS_STRAIGHT.create(block, mapping, modelOutput);
        ResourceLocation inner = ModelTemplates.STAIRS_INNER.create(block, mapping, modelOutput);
        ResourceLocation outer = ModelTemplates.STAIRS_OUTER.create(block, mapping, modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(stairsDispatch(straight, inner, outer)));
        delegateItem(block.asItem(), straight);
    }

    private void existingModelWithItem(Block block, String modelName) {
        simpleBlock(block, TTIds.rl("block/" + modelName));
        delegateItem(block.asItem(), TTIds.rl("block/" + modelName));
    }

    private void paving(Block block, String name) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.DIRT, blockTexture("arcane_brick_stone"))
                .put(TextureSlot.TOP, blockTexture(name))
                .put(TextureSlot.PARTICLE, blockTexture(name));
        ResourceLocation model = ModelTemplates.FARMLAND.create(block, mapping, modelOutput);
        simpleBlock(block, model);
        delegateItem(block.asItem(), model);
    }

    private void eldritchModels() {
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(TTBlocks.ELDRITCH_RELIQUARY.get(), vName("eldritch_reliquary"))
                        .with(horizontalDispatch()));
        ResourceLocation dormant = ModelTemplates.CUBE_ALL.create(
                TTBlocks.ELDRITCH_NOTHING_DORMANT.get(),
                new TextureMapping().put(TextureSlot.ALL, blockTexture("eldritch_rock")),
                modelOutput);
        simpleBlock(TTBlocks.ELDRITCH_NOTHING_DORMANT.get(), dormant);
        stairsFromTexture(TTBlocks.STAIRS_ELDRITCH_TILE.get(), blockTexture("stone_eldritch_tile"));
        stairsFromTexture(TTBlocks.STAIRS_ELDRITCH_ROCK.get(), blockTexture("eldritch_rock"));
        Block pillar = TTBlocks.STONE_ELDRITCH_PILLAR.get();
        ResourceLocation pillarModel = ModelTemplates.CUBE_COLUMN.create(
                pillar,
                new TextureMapping()
                        .put(TextureSlot.SIDE, blockTexture("stone_eldritch_pillar_side"))
                        .put(TextureSlot.END, blockTexture("eldritch_stone")),
                modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(pillar, v(pillarModel))
                .with(PropertyDispatch.property(BlockStateProperties.AXIS)
                        .select(Direction.Axis.Y, Variant.variant())
                        .select(
                                Direction.Axis.Z,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.Axis.X,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))));
        delegateItem(pillar.asItem(), pillarModel);
        cube(TTBlocks.OBSIDIAN_TILE.get(), "obsidian_tile");
        obsidianTotem();
        cube(TTBlocks.ELDRITCH_STONE.get(), "eldritch_stone");
        cube(TTBlocks.ELDRITCH_STONE_INERT.get(), "eldritch_stone");
        cube(TTBlocks.ELDRITCH_ROCK.get(), "eldritch_rock");
        cube(TTBlocks.ELDRITCH_CRUST.get(), "eldritch_crust");
        insetBlock(TTBlocks.ELDRITCH_CRUST_GLOWING.get(), "eldritch_crust_glowing", true);
        cube(TTBlocks.ELDRITCH_DOOR.get(), "eldritch_door");
        insetBlock(TTBlocks.ELDRITCH_STONE_CRYSTAL.get(), "eldritch_stone_crystal", false);
        eldritchLock();
        crabSpawner();
        column(TTBlocks.ELDRITCH_PEDESTAL.get(), "eldritch_pedestal_side", "eldritch_stone");
        invisibleWithMeshItem(TTBlocks.ELDRITCH_ALTAR.get(), "eldritch_altar", "eldritch_altar_item");
        invisibleWithCubeItem(TTBlocks.ELDRITCH_OBELISK.get(), "eldritch_deco");
        invisibleWithCubeItem(TTBlocks.ELDRITCH_PILLAR.get(), "eldritch_deco");
        invisibleWithCubeItem(TTBlocks.ELDRITCH_CAPSTONE.get(), "eldritch_deco");
        trap();
        invisible(TTBlocks.ELDRITCH_NOTHING.get());
        invisible(TTBlocks.ELDRITCH_PORTAL.get());
        stairsFromTexture(TTBlocks.STAIRS_ELDRITCH.get(), blockTexture("eldritch_stone"));
    }

    private void cube(Block block, String textureName) {
        ResourceLocation model = ModelTemplates.CUBE_ALL.create(
                block, new TextureMapping().put(TextureSlot.ALL, blockTexture(textureName)), modelOutput);
        simpleBlock(block, model);
        delegateItem(block.asItem(), model);
    }

    private void eldritchLock() {
        ResourceLocation model = ModelTemplates.CUBE_ORIENTABLE.create(
                TTBlocks.ELDRITCH_LOCK.get(),
                new TextureMapping()
                        .put(TextureSlot.FRONT, blockTexture("eldritch_lock_face"))
                        .put(TextureSlot.SIDE, blockTexture("eldritch_lock_side"))
                        .put(TextureSlot.TOP, blockTexture("eldritch_lock_side")),
                modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TTBlocks.ELDRITCH_LOCK.get(), v(model))
                .with(PropertyDispatch.property(BlockStateProperties.FACING)
                        .select(
                                Direction.DOWN,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.UP,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                        .select(Direction.NORTH, Variant.variant())
                        .select(
                                Direction.SOUTH,
                                Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.WEST,
                                Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                        .select(
                                Direction.EAST,
                                Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))));
        delegateItem(TTBlocks.ELDRITCH_LOCK.get().asItem(), model);
    }

    private static final int INSET_DEPTH = 2;
    private static final int INSET_ALL_EXPOSED = 63;

    private void insetBlock(Block block, String textureName, boolean staticAllExposedModel) {
        ResourceLocation texture = TTIds.rl("block/" + textureName);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (int mask = 0; mask <= INSET_ALL_EXPOSED; mask++) {
            ResourceLocation model = TTIds.rl("block/" + textureName + "_inset_" + mask);
            int finalMask = mask;
            if (!staticAllExposedModel || mask != INSET_ALL_EXPOSED) {
                modelOutput.accept(model, () -> insetModel(texture, finalMask));
            }
            Condition.TerminalCondition condition = Condition.condition();
            for (Direction dir : Direction.values()) {
                condition = condition.term(BlockEldritchInset.EXPOSED.get(dir), insetExposed(mask, dir));
            }
            generator = generator.with(condition, v(model));
        }
        blockStateOutput.accept(generator);
        delegateItem(block.asItem(), TTIds.rl("block/" + textureName + "_inset_" + INSET_ALL_EXPOSED));
    }

    private static boolean insetExposed(int mask, Direction dir) {
        return (mask & (1 << dir.get3DDataValue())) != 0;
    }

    private static JsonElement insetModel(ResourceLocation texture, int mask) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", texture.toString());
        textures.addProperty("all", texture.toString());
        root.add("textures", textures);
        JsonObject element = new JsonObject();
        element.add(
                "from",
                insetCoords(
                        insetExposed(mask, Direction.WEST) ? INSET_DEPTH : 0,
                        insetExposed(mask, Direction.DOWN) ? INSET_DEPTH : 0,
                        insetExposed(mask, Direction.NORTH) ? INSET_DEPTH : 0));
        element.add(
                "to",
                insetCoords(
                        insetExposed(mask, Direction.EAST) ? 16 - INSET_DEPTH : 16,
                        insetExposed(mask, Direction.UP) ? 16 - INSET_DEPTH : 16,
                        insetExposed(mask, Direction.SOUTH) ? 16 - INSET_DEPTH : 16));
        JsonObject faces = new JsonObject();
        for (Direction dir : Direction.values()) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#all");
            if (!insetExposed(mask, dir)) {
                face.addProperty("cullface", dir.getSerializedName());
            }
            faces.add(dir.getSerializedName(), face);
        }
        element.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(element);
        root.add("elements", elements);
        return root;
    }

    private static JsonArray insetCoords(int x, int y, int z) {
        JsonArray coords = new JsonArray();
        coords.add(x);
        coords.add(y);
        coords.add(z);
        return coords;
    }

    private void column(Block block, String side, String end) {
        ResourceLocation model = ModelTemplates.CUBE_COLUMN.create(
                block,
                new TextureMapping().put(TextureSlot.SIDE, blockTexture(side)).put(TextureSlot.END, blockTexture(end)),
                modelOutput);
        simpleBlock(block, model);
        delegateItem(block.asItem(), model);
    }

    private void obsidianTotem() {
        Block block = TTBlocks.OBSIDIAN_TOTEM.get();
        ResourceLocation baseModel = ModelTemplates.CUBE_COLUMN.createWithSuffix(
                block,
                "_base",
                new TextureMapping()
                        .put(TextureSlot.SIDE, blockTexture("obsidian_totem_base"))
                        .put(TextureSlot.END, blockTexture("obsidian_tile")),
                modelOutput);
        ResourceLocation shadedModel = ModelTemplates.CUBE_COLUMN.createWithSuffix(
                block,
                "_shaded",
                new TextureMapping()
                        .put(TextureSlot.SIDE, blockTexture("obsidian_totem_base_shaded"))
                        .put(TextureSlot.END, blockTexture("obsidian_tile")),
                modelOutput);
        List<Variant> carved = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            ResourceLocation model = ModelTemplates.CUBE_COLUMN.createWithSuffix(
                    block,
                    "_carved_" + i,
                    new TextureMapping()
                            .put(TextureSlot.SIDE, blockTexture("obsidian_totem_" + i))
                            .put(TextureSlot.END, blockTexture("obsidian_tile")),
                    modelOutput);
            carved.add(v(model));
            carved.add(v(model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
            carved.add(v(model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
            carved.add(v(model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
        }
        for (Block totem : List.of(block, TTBlocks.OBSIDIAN_TOTEM_CHARGED.get())) {
            blockStateOutput.accept(MultiVariantGenerator.multiVariant(totem)
                    .with(PropertyDispatch.properties(BlockObsidianTotem.UP, BlockObsidianTotem.DOWN)
                            .select(true, true, v(shadedModel))
                            .select(true, false, v(shadedModel))
                            .select(false, true, carved)
                            .select(false, false, v(baseModel))));
        }
        delegateItem(block.asItem(), baseModel);
    }

    private void trap() {
        Block block = TTBlocks.ELDRITCH_TRAP.get();
        List<Variant> variants = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ResourceLocation model = ModelTemplates.CUBE_ALL.createWithSuffix(
                    block,
                    "_" + i,
                    new TextureMapping().put(TextureSlot.ALL, blockTexture("eldritch_trap_" + i)),
                    modelOutput);
            variants.add(v(model));
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, variants.toArray(Variant[]::new)));
        delegateItem(block.asItem(), ModelLocationUtils.getModelLocation(block, "_0"));
    }

    private void crabSpawner() {
        Block block = TTBlocks.ELDRITCH_CRAB_SPAWNER.get();
        ResourceLocation model = ModelLocationUtils.getModelLocation(block);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, v(model))
                .with(PropertyDispatch.property(BlockEldritchCrabSpawner.FACING)
                        .select(Direction.UP, Variant.variant())
                        .select(
                                Direction.DOWN,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.NORTH,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.EAST,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.SOUTH,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.WEST,
                                Variant.variant()
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))));
        delegateItem(block.asItem(), model);
    }

    private void invisibleWithCubeItem(Block block, String textureName) {
        ResourceLocation itemModel = ModelTemplates.CUBE_ALL.createWithSuffix(
                block, "_inventory", new TextureMapping().put(TextureSlot.ALL, blockTexture(textureName)), modelOutput);
        delegateItem(block.asItem(), itemModel);
        ResourceLocation model = ModelTemplates.PARTICLE_ONLY.create(
                block, TextureMapping.particle(blockTexture(textureName)), modelOutput);
        simpleBlock(block, model);
    }

    private void invisibleWithMeshItem(Block block, String textureName, String itemModelName) {
        delegateItem(block.asItem(), TTIds.rl("block/" + itemModelName));
        ResourceLocation model = ModelTemplates.PARTICLE_ONLY.create(
                block, TextureMapping.particle(blockTexture(textureName)), modelOutput);
        simpleBlock(block, model);
    }

    private void invisible(Block block) {
        ResourceLocation model = ModelTemplates.PARTICLE_ONLY.create(
                block, TextureMapping.particle(blockTexture("eldritch_stone")), modelOutput);
        simpleBlock(block, model);
    }

    private void containerItemModels() {
        registerPhial();
        registerLabel();
        registerPrimordialPearl();
    }

    private void registerPhial() {
        ResourceLocation filled = ModelLocationUtils.getModelLocation(TTItems.PHIAL.get(), "_filled");
        ModelTemplates.TWO_LAYERED_ITEM.create(
                filled,
                TextureMapping.layered(
                        TextureMapping.getItemTexture(TTItems.PHIAL.get()),
                        TextureMapping.getItemTexture(TTItems.PHIAL.get(), "_overlay")),
                modelOutput);
        generatedOverridesItem(
                TTItems.PHIAL.get(),
                Map.of("layer0", TextureMapping.getItemTexture(TTItems.PHIAL.get())),
                List.of(new ItemOverride(PROPERTY_FILLED, 1.0F, filled)));
    }

    private void registerLabel() {
        ResourceLocation marked = ModelLocationUtils.getModelLocation(TTItems.LABEL.get(), "_marked");
        ModelTemplates.TWO_LAYERED_ITEM.create(
                marked,
                TextureMapping.layered(
                        TextureMapping.getItemTexture(TTItems.LABEL.get()),
                        TextureMapping.getItemTexture(TTItems.LABEL.get(), "_overlay")),
                modelOutput);
        generatedOverridesItem(
                TTItems.LABEL.get(),
                Map.of("layer0", TextureMapping.getItemTexture(TTItems.LABEL.get())),
                List.of(new ItemOverride(PROPERTY_MARKED, 1.0F, marked)));
    }

    private void registerPrimordialPearl() {
        Item item = TTItems.PRIMORDIAL_PEARL.get();
        ResourceLocation nodule = ModelLocationUtils.getModelLocation(item, "_nodule");
        ResourceLocation mote = ModelLocationUtils.getModelLocation(item, "_mote");
        ModelTemplates.FLAT_ITEM.create(
                nodule, TextureMapping.layer0(TextureMapping.getItemTexture(item, "_nodule")), modelOutput);
        ModelTemplates.FLAT_ITEM.create(
                mote, TextureMapping.layer0(TextureMapping.getItemTexture(item, "_mote")), modelOutput);
        float noduleThreshold =
                (float) (PrimordialPearlItem.PEARL_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        float moteThreshold =
                (float) (PrimordialPearlItem.NODULE_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        generatedOverridesItem(
                item,
                Map.of("layer0", TextureMapping.getItemTexture(item)),
                List.of(
                        new ItemOverride(PROPERTY_DAMAGE, noduleThreshold, nodule),
                        new ItemOverride(PROPERTY_DAMAGE, moteThreshold, mote)));
    }
}
