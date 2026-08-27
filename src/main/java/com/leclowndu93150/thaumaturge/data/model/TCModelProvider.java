package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotem;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.content.device.BlockVisBattery;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.data.model.crystal.CrystalBlockstateGenerator;
import com.leclowndu93150.thaumaturge.data.model.crystal.CrystalItemModelGenerator;
import com.leclowndu93150.thaumaturge.data.model.crystal.EssentiaCrystalModelGenerator;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
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
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

public final class TCModelProvider implements DataProvider {

    private static final ModelTemplate THREE_LAYERED_ITEM = new ModelTemplate(
            Optional.of(ResourceLocation.withDefaultNamespace("item/generated")),
            Optional.empty(),
            TextureSlot.LAYER0,
            TextureSlot.LAYER1,
            TextureSlot.LAYER2);

    private static final ResourceLocation GENERATED_PARENT = ResourceLocation.withDefaultNamespace("item/generated");
    private static final ResourceLocation BEWLR_BLOCK_PARENT = TCIds.rl("item/bewlr_block");
    private static final ResourceLocation PROPERTY_LINKED = TCIds.rl("linked");
    private static final ResourceLocation PROPERTY_LOADED = TCIds.rl("loaded");
    private static final ResourceLocation PROPERTY_NOTE_COMPLETE = TCIds.rl("note_complete");
    private static final ResourceLocation PROPERTY_FILLED = TCIds.rl("filled");
    private static final ResourceLocation PROPERTY_VERDANT_TYPE = TCIds.rl("verdant_type");
    private static final ResourceLocation PROPERTY_CELESTIAL_BODY = TCIds.rl("celestial_body");
    private static final ResourceLocation PROPERTY_WAND_IS_STAFF = TCIds.rl("wand_is_staff");
    private static final ResourceLocation PROPERTY_DAMAGE = ResourceLocation.withDefaultNamespace("damage");

    private final PackOutput.PathProvider blockStatePath;
    private final PackOutput.PathProvider modelPath;

    private final Map<Block, BlockStateGenerator> blockStates = new LinkedHashMap<>();
    private final Map<ResourceLocation, Supplier<JsonElement>> models = new LinkedHashMap<>();
    private Consumer<BlockStateGenerator> blockStateOutput;
    private BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;

    public TCModelProvider(PackOutput output) {
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
            "bellows", "leaves_greatwood", "leaves_silverwood", "plank_greatwood", "plank_silverwood", "thaumometer");

    private void autoBlockItems() {
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (!key.getNamespace().equals(TCIds.MODID) || CHECKED_IN_ITEM_MODELS.contains(key.getPath())) {
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
        translucentCube(TCBlocks.AMBER_BRICK.get());
        blockModels.createTrivialCube(TCBlocks.FLESH_BLOCK.get());
        registerInvisibleBlock(TCBlocks.EFFECT_SHOCK.get());
        registerInvisibleBlock(TCBlocks.BARRIER.get());
        registerInvisibleBlock(TCBlocks.NODE.get());
        registerJar(TCBlocks.JAR_NORMAL.get(), "jar_normal");
        registerJar(TCBlocks.JAR_VOID.get(), "jar_void");
        registerJarBrain();
        registerAuraDevices(blockModels);
        registerNoiseDevices();
        TubeModels.register(blockStateOutput);
        simpleFromExisting(TCBlocks.CRUCIBLE.get(), "crucible");
        mirrorBlockState(TCBlocks.MIRROR.get());
        mirrorBlockState(TCBlocks.MIRROR_ESSENTIA.get());
        simpleFromExisting(TCBlocks.LOOT_URN_COMMON.get(), "loot_urn_common");
        simpleFromExisting(TCBlocks.LOOT_URN_UNCOMMON.get(), "loot_urn_uncommon");
        simpleFromExisting(TCBlocks.LOOT_URN_RARE.get(), "loot_urn_rare");
        simpleFromExisting(TCBlocks.LOOT_CRATE_COMMON.get(), "loot_crate_common");
        simpleFromExisting(TCBlocks.LOOT_CRATE_UNCOMMON.get(), "loot_crate_uncommon");
        simpleFromExisting(TCBlocks.LOOT_CRATE_RARE.get(), "loot_crate_rare");
        simpleFromExisting(TCBlocks.ARCANE_WORKBENCH.get(), "arcane_workbench");
        simpleFromExisting(TCBlocks.ARCANE_WORKBENCH_CHARGER.get(), "arcane_workbench_charger");
        simpleFromExisting(TCBlocks.NODE_STABILIZER.get(), "node_stabilizer");
        simpleFromExisting(TCBlocks.NODE_STABILIZER_ADVANCED.get(), "node_stabilizer_advanced");
        simpleFromExisting(TCBlocks.NODE_TRANSDUCER.get(), "node_transducer");
        simpleFromExisting(TCBlocks.VIS_RELAY.get(), "vis_relay");
        delegateItem(TCBlocks.VIS_RELAY.get().asItem(), TCIds.rl("block/vis_relay"));
        delegateItem(TCBlocks.NODE_STABILIZER.get().asItem(), TCIds.rl("item/node_stabilizer_base"));
        delegateItem(TCBlocks.NODE_STABILIZER_ADVANCED.get().asItem(), TCIds.rl("item/node_stabilizer_base"));
        delegateItem(TCBlocks.NODE_TRANSDUCER.get().asItem(), TCIds.rl("item/node_stabilizer_base"));
        simpleBlock(TCBlocks.JAR_NODE.get(), TCIds.rl("block/jar_normal"));
        delegateItem(TCBlocks.JAR_NODE.get().asItem(), BEWLR_BLOCK_PARENT);
        horizontalBlock(TCBlocks.INFERNAL_FURNACE.get(), "infernal_furnace");
        simpleBlock(
                TCBlocks.NETHER_BRICKS_PLACEHOLDER.get(), ModelLocationUtils.getModelLocation(Blocks.NETHER_BRICKS));
        simpleBlock(TCBlocks.OBSIDIAN_PLACEHOLDER.get(), ModelLocationUtils.getModelLocation(Blocks.OBSIDIAN));
        registerAlembic(TCBlocks.ALEMBIC.get());
        registerBellows();
        registerSmelter(TCBlocks.SMELTER_BASIC.get(), "smelter_basic");
        registerSmelter(TCBlocks.SMELTER_THAUMIUM.get(), "smelter_thaumium");
        registerSmelter(TCBlocks.SMELTER_VOID.get(), "smelter_void");
        horizontalBlock(TCBlocks.SMELTER_AUX.get(), "smelter_aux");
        horizontalBlock(TCBlocks.SMELTER_VENT.get(), "smelter_vent");
        flatItem(TCItems.THAUMONOMICON.get());
        flatItem(TCItems.THAUMONOMICON_CHEAT.get());
        flatItem(TCItems.THAUMONOMICON_SHARING.get());
        flatItem(TCItems.THAUMONOMICON_LINKING.get());
        flatItem(TCItems.CREATIVE_NODE_PLACER.get());
        flatItem(TCItems.SALIS_MUNDUS.get());
        registerWandItem();
        flatItem(TCItems.WAND_CAP_IRON.get());
        flatItem(TCItems.WAND_CAP_COPPER.get());
        flatItem(TCItems.WAND_CAP_GOLD.get());
        flatItem(TCItems.WAND_CAP_SILVER_INERT.get());
        flatItem(TCItems.WAND_CAP_SILVER.get());
        flatItem(TCItems.WAND_CAP_THAUMIUM_INERT.get());
        flatItem(TCItems.WAND_CAP_THAUMIUM.get());
        flatItem(TCItems.WAND_CAP_VOID_INERT.get());
        flatItem(TCItems.WAND_CAP_VOID.get());
        handheldItem(TCItems.WAND_ROD_GREATWOOD.get());
        handheldItem(TCItems.WAND_ROD_OBSIDIAN.get());
        handheldItem(TCItems.WAND_ROD_BLAZE.get());
        handheldItem(TCItems.WAND_ROD_ICE.get());
        handheldItem(TCItems.WAND_ROD_QUARTZ.get());
        handheldItem(TCItems.WAND_ROD_BONE.get());
        handheldItem(TCItems.WAND_ROD_REED.get());
        handheldItem(TCItems.WAND_ROD_SILVERWOOD.get());
        handheldItem(TCItems.STAFF_ROD_GREATWOOD.get());
        handheldItem(TCItems.STAFF_ROD_OBSIDIAN.get());
        handheldItem(TCItems.STAFF_ROD_BLAZE.get());
        handheldItem(TCItems.STAFF_ROD_ICE.get());
        handheldItem(TCItems.STAFF_ROD_QUARTZ.get());
        handheldItem(TCItems.STAFF_ROD_BONE.get());
        handheldItem(TCItems.STAFF_ROD_REED.get());
        handheldItem(TCItems.STAFF_ROD_SILVERWOOD.get());
        handheldItem(TCItems.STAFF_ROD_PRIMAL.get());
        flatItem(TCItems.PRIMAL_CHARM.get());
        flatItem(TCItems.FABRIC.get());
        flatItem(TCItems.MIRRORED_GLASS.get());
        flatItem(TCItems.FILTER.get());
        flatItem(TCItems.MECHANISM_SIMPLE.get());
        flatItem(TCItems.MECHANISM_COMPLEX.get());
        flatItem(TCItems.MORPHIC_RESONATOR.get());
        flatItem(TCItems.BATH_SALTS.get());
        flatItem(TCItems.SANITY_SOAP.get());
        flatItem(TCItems.CHUNK_BEEF.get());
        flatItem(TCItems.CHUNK_CHICKEN.get());
        flatItem(TCItems.CHUNK_PORK.get());
        flatItem(TCItems.CHUNK_FISH.get());
        flatItem(TCItems.CHUNK_RABBIT.get());
        flatItem(TCItems.CHUNK_MUTTON.get());
        flatItem(TCItems.TRIPLE_MEAT_TREAT.get());
        flatItem(TCItems.JAR_BRACE.get());
        flatItem(TCItems.LABEL.get());
        flatItem(TCItems.BOTTLE_TAINT.get());
        flatItem(TCItems.VIS_RESONATOR.get());
        flatItem(TCItems.THAUMIC_SLIME_SPAWN_EGG.get());
        flatItem(TCItems.TAINT_CRAWLER_SPAWN_EGG.get());
        flatItem(TCItems.TAINTACLE_SPAWN_EGG.get());
        flatItem(TCItems.TAINT_SWARM_SPAWN_EGG.get());
        flatItem(TCItems.TAINT_SEED_SPAWN_EGG.get());
        flatItem(TCItems.TAINT_SEED_PRIME_SPAWN_EGG.get());
        flatItem(TCItems.WISP_SPAWN_EGG.get());
        flatItem(TCItems.BRAINY_ZOMBIE_SPAWN_EGG.get());
        flatItem(TCItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get());
        flatItem(TCItems.BRAIN.get());
        flatItem(TCItems.FIREBAT_SPAWN_EGG.get());
        flatItem(TCItems.MIND_SPIDER_SPAWN_EGG.get());
        flatItem(TCItems.PECH_SPAWN_EGG.get());
        flatItem(TCItems.ELDRITCH_CRAB_SPAWN_EGG.get());
        flatItem(TCItems.INHABITED_ZOMBIE_SPAWN_EGG.get());
        flatItem(TCItems.ELDRITCH_GUARDIAN_SPAWN_EGG.get());
        flatItem(TCItems.CULTIST_KNIGHT_SPAWN_EGG.get());
        flatItem(TCItems.CULTIST_CLERIC_SPAWN_EGG.get());
        flatItem(TCItems.CULTIST_PORTAL_LESSER_SPAWN_EGG.get());
        flatItem(TCItems.CULTIST_LEADER_SPAWN_EGG.get());
        flatItem(TCItems.CULTIST_PORTAL_GREATER_SPAWN_EGG.get());
        flatItem(TCItems.ELDRITCH_WARDEN_SPAWN_EGG.get());
        flatItem(TCItems.ELDRITCH_GOLEM_SPAWN_EGG.get());
        flatItem(TCItems.TAINTACLE_GIANT_SPAWN_EGG.get());
        flatItem(TCItems.LOOT_BAG_COMMON.get());
        flatItem(TCItems.LOOT_BAG_UNCOMMON.get());
        flatItem(TCItems.LOOT_BAG_RARE.get());
        handheldItem(TCItems.PECH_WAND.get());
        handheldItem(TCItems.CRIMSON_BLADE.get());
        flatItem(TCItems.CRIMSON_PLATE_HELM.get());
        flatItem(TCItems.CRIMSON_PLATE_CHEST.get());
        flatItem(TCItems.CRIMSON_PLATE_LEGS.get());
        flatItem(TCItems.CRIMSON_BOOTS.get());
        flatItem(TCItems.CRIMSON_ROBE_HELM.get());
        flatItem(TCItems.CRIMSON_ROBE_CHEST.get());
        flatItem(TCItems.CRIMSON_ROBE_LEGS.get());
        flatItem(TCItems.TUBE.get());
        flatItem(TCItems.TUBE_VALVE.get());
        flatItem(TCItems.TUBE_RESTRICT.get());
        flatItem(TCItems.TUBE_FILTER.get());
        flatItem(TCItems.TUBE_ONEWAY.get());
        flatItem(TCItems.TUBE_BUFFER.get());
        flatItem(TCItems.GOGGLES_REVEALING.get());
        flatItem(TCItems.SCRIBING_TOOLS.get());
        flatItem(TCItems.ALUMENTUM.get());
        registerCelestialNotes();
        registerBaubleItems();

        ModelTemplates.TWO_LAYERED_ITEM.create(
                TCIds.rl("item/nitor"),
                TextureMapping.layered(TCIds.rl("block/nitor"), TCIds.rl("block/nitor_core")),
                modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            registerNitor(dye);
        }

        blockModels.createTrivialCube(TCBlocks.ORE_AMBER.get());
        blockModels.createTrivialCube(TCBlocks.ORE_CINNABAR.get());
        blockModels.createTrivialCube(TCBlocks.ORE_QUARTZ.get());

        blockModels.createTrivialCube(TCBlocks.ALCHEMICAL_CONSTRUCT.get());
        blockModels.createTrivialCube(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());

        blockModels.createTrivialCube(TCBlocks.METAL_BRASS_BLOCK.get());
        blockModels.createTrivialCube(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        blockModels.createTrivialCube(TCBlocks.METAL_VOID_BLOCK.get());
        translucentCube(TCBlocks.AMBER_BLOCK.get());

        flatItem(TCItems.INGOT_THAUMIUM.get());
        flatItem(TCItems.INGOT_BRASS.get());
        flatItem(TCItems.INGOT_VOID.get());
        flatItem(TCItems.AMBER.get());
        flatItem(TCItems.QUICKSILVER.get());

        flatItem(TCItems.RARE_EARTH.get());

        flatItem(TCItems.NUGGET_THAUMIUM.get());
        flatItem(TCItems.NUGGET_BRASS.get());
        flatItem(TCItems.NUGGET_VOID.get());
        flatItem(TCItems.NUGGET_QUICKSILVER.get());
        registerInfusionAltar();
        registerSimpleWithItem(TCBlocks.FOCAL_MANIPULATOR.get(), "focal_manipulator");
        registerInvisibleBlock(TCBlocks.HOLE.get());
        registerInvisibleBlock(TCBlocks.EFFECT_SAP.get());
        registerInvisibleBlock(TCBlocks.EFFECT_GLIMMER.get());

        registerCandles();
        registerBanners();
        flatItem(TCItems.TALLOW.get());
        handheldItem(TCItems.THAUMIUM_SWORD.get());
        handheldItem(TCItems.THAUMIUM_PICKAXE.get());
        handheldItem(TCItems.THAUMIUM_AXE.get());
        handheldItem(TCItems.THAUMIUM_SHOVEL.get());
        handheldItem(TCItems.THAUMIUM_HOE.get());
        handheldItem(TCItems.VOID_SWORD.get());
        handheldItem(TCItems.VOID_PICKAXE.get());
        handheldItem(TCItems.VOID_AXE.get());
        handheldItem(TCItems.VOID_SHOVEL.get());
        handheldItem(TCItems.VOID_HOE.get());
        handheldItem(TCItems.ELEMENTAL_SWORD.get());
        handheldItem(TCItems.ELEMENTAL_PICKAXE.get());
        handheldItem(TCItems.ELEMENTAL_AXE.get());
        handheldItem(TCItems.ELEMENTAL_SHOVEL.get());
        handheldItem(TCItems.ELEMENTAL_HOE.get());
        handheldItem(TCItems.PRIMAL_CRUSHER.get());
        flatItem(TCItems.TRAVELLER_BOOTS.get());
        flatItem(TCItems.THAUMIUM_HELM.get());
        flatItem(TCItems.THAUMIUM_CHEST.get());
        flatItem(TCItems.THAUMIUM_LEGS.get());
        flatItem(TCItems.THAUMIUM_BOOTS.get());
        flatItem(TCItems.VOID_HELM.get());
        flatItem(TCItems.VOID_CHEST.get());
        flatItem(TCItems.VOID_LEGS.get());
        flatItem(TCItems.VOID_BOOTS.get());
        registerRobeItem(TCItems.CLOTH_CHEST.get(), "cloth_chest");
        registerRobeItem(TCItems.CLOTH_LEGS.get(), "cloth_legs");
        registerRobeItem(TCItems.CLOTH_BOOTS.get(), "cloth_boots");
        registerSpa();
        registerCasters();
        registerGolemancy();

        flatItem(TCItems.NUGGET_QUARTZ.get());

        flatItem(TCItems.VOID_SEED.get());
        flatItem(TCItems.CAUSALITY_COLLAPSER.get());
        flatItem(TCItems.CLUSTER_IRON.get());
        flatItem(TCItems.CLUSTER_GOLD.get());
        flatItem(TCItems.CLUSTER_COPPER.get());
        flatItem(TCItems.CLUSTER_SILVER.get());
        flatItem(TCItems.CLUSTER_LEAD.get());
        flatItem(TCItems.CLUSTER_TIN.get());
        flatItem(TCItems.CLUSTER_CINNABAR.get());
        flatItem(TCItems.CLUSTER_QUARTZ.get());

        flatItem(TCItems.PLATE_IRON.get());
        flatItem(TCItems.PLATE_THAUMIUM.get());
        flatItem(TCItems.PLATE_BRASS.get());
        flatItem(TCItems.PLATE_VOID.get());

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
        Block pod = TCBlocks.MANA_POD.get();
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
                ModelLocationUtils.getModelLocation(TCItems.MANA_BEAN.get()),
                TextureMapping.layer0(TCIds.rl("item/mana_bean")),
                modelOutput);
    }

    private static Variant v(ResourceLocation model) {
        return Variant.variant().with(VariantProperties.MODEL, model);
    }

    private static Variant vName(String blockModelName) {
        return v(TCIds.rl("block/" + blockModelName));
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
        simpleBlock(block, TCIds.rl("block/" + modelName));
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
        delegateItem(block.asItem(), TCIds.rl("block/" + modelName));
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
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.BELLOWS.get(), vName("bellows"))
                .with(rotations));
    }

    private void registerBanners() {
        ResourceLocation bannerModel = TCIds.rl("block/tc_banner");
        ResourceLocation stand = TCIds.rl("item/banner_stand");
        ResourceLocation cloth = TCIds.rl("item/banner_cloth");
        ResourceLocation symbol = TCIds.rl("item/banner_symbol");
        ResourceLocation dyedItemModel = TCIds.rl("item/banner_dyed");
        ResourceLocation cultistItemModel = TCIds.rl("item/banner_cultist");
        THREE_LAYERED_ITEM.create(dyedItemModel, TextureMapping.layered(stand, cloth, symbol), modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(
                cultistItemModel, TextureMapping.layered(stand, cultistItemModel), modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            simpleBlock(TCBlocks.BANNERS.get(dye).get(), bannerModel);
            simpleBlock(TCBlocks.WALL_BANNERS.get(dye).get(), bannerModel);
            delegateItem(TCItems.BANNERS.get(dye).get(), dyedItemModel);
        }
        simpleBlock(TCBlocks.BANNER_CRIMSON_CULT.get(), bannerModel);
        simpleBlock(TCBlocks.WALL_BANNER_CRIMSON_CULT.get(), bannerModel);
        delegateItem(TCItems.BANNER_CRIMSON_CULT.get(), cultistItemModel);
    }

    private void registerCandles() {
        ResourceLocation model = TCIds.rl("block/candle");
        for (DyeColor dye : DyeColor.values()) {
            Block candle = TCBlocks.CANDLES.get(dye).get();
            simpleBlock(candle, model);
            delegateItem(candle.asItem(), model);
        }
    }

    private void registerBaubleItems() {
        flatItem(TCItems.AMULET_MUNDANE.get());
        flatItem(TCItems.RING_MUNDANE.get());
        flatItem(TCItems.GIRDLE_MUNDANE.get());
        flatItem(TCItems.RING_APPRENTICE.get());
        flatItem(TCItems.AMULET_FANCY.get());
        flatItem(TCItems.RING_FANCY.get());
        flatItem(TCItems.GIRDLE_FANCY.get());
        flatItem(TCItems.AMULET_VIS.get());
        flatItem(TCItems.AMULET_VIS_CRAFTED.get());
        flatItem(TCItems.CHARM_UNDYING.get());
        flatItem(TCItems.CLOUD_RING.get());
        flatItem(TCItems.CURIOSITY_BAND.get());
        flatItem(TCItems.VOIDSEER_CHARM.get());
        flatItem(TCItems.FOCUS_POUCH.get());
        flatItem(TCItems.SANITY_CHECKER.get());
        flatItem(TCItems.RESONATOR.get());
        flatItem(TCItems.CURIO_ARCANE.get());
        flatItem(TCItems.CURIO_PRESERVED.get());
        flatItem(TCItems.CURIO_ANCIENT.get());
        flatItem(TCItems.CURIO_ELDRITCH.get());
        flatItem(TCItems.CURIO_KNOWLEDGE.get());
        flatItem(TCItems.CURIO_TWISTED.get());
        flatItem(TCItems.CURIO_RITES.get());
        flatItem(TCItems.CREATIVE_FLUX_SPONGE.get());
        flatItem(TCItems.HAND_MIRROR.get());
        registerMirrorItem(TCItems.MIRROR.get(), "mirrorframe");
        registerMirrorItem(TCItems.MIRROR_ESSENTIA.get(), "mirrorframe2");
        flatItem(TCItems.CRIMSON_PRAETOR_HELM.get());
        flatItem(TCItems.CRIMSON_PRAETOR_CHEST.get());
        flatItem(TCItems.CRIMSON_PRAETOR_LEGS.get());
        flatItem(TCItems.FORTRESS_HELM.get());
        flatItem(TCItems.FORTRESS_CHEST.get());
        flatItem(TCItems.FORTRESS_LEGS.get());
        registerVerdantCharm();
        registerVoidRobeItems();
    }

    private void registerVerdantCharm() {
        ResourceLocation base = TCIds.rl("item/verdant_charm");
        List<ItemOverride> overrides = new ArrayList<>();
        for (int type = 0; type <= 2; type++) {
            ResourceLocation model = TCIds.rl("item/verdant_charm_" + type);
            ResourceLocation overlay = TCIds.rl("item/verdant_charm_over_" + type);
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(base, overlay), modelOutput);
            if (type > 0) {
                overrides.add(new ItemOverride(PROPERTY_VERDANT_TYPE, type, model));
            }
        }
        overridesItem(TCItems.VERDANT_CHARM.get(), TCIds.rl("item/verdant_charm_0"), overrides);
    }

    private void registerVoidRobeItems() {
        flatItem(TCItems.VOID_ROBE_HELM.get());
        registerVoidRobePiece(TCItems.VOID_ROBE_CHEST.get(), "void_robe_chest");
        registerVoidRobePiece(TCItems.VOID_ROBE_LEGS.get(), "void_robe_legs");
    }

    private void registerVoidRobePiece(Item item, String name) {
        ModelTemplates.TWO_LAYERED_ITEM.create(
                TCIds.rl("item/" + name),
                TextureMapping.layered(TCIds.rl("item/" + name + "_over"), TCIds.rl("item/" + name)),
                modelOutput);
    }

    private void registerCelestialNotes() {
        ResourceLocation sheet = TCIds.rl("item/celestial_notes_sheet");
        List<ItemOverride> overrides = new ArrayList<>();
        for (CelestialBody body : CelestialBody.values()) {
            ResourceLocation model = TCIds.rl("item/celestial_notes_" + body.getSerializedName());
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(sheet, model), modelOutput);
            if (body.ordinal() > 0) {
                overrides.add(new ItemOverride(PROPERTY_CELESTIAL_BODY, body.ordinal(), model));
            }
        }
        overridesItem(TCItems.CELESTIAL_NOTES.get(), TCIds.rl("item/celestial_notes_sun"), overrides);
    }

    private void registerWandItem() {
        ResourceLocation staffModel = TCIds.rl("item/wand_staff");
        modelOutput.accept(staffModel, new DelegatedModel(TCIds.rl("item/wand_staff_base")));
        overridesItem(
                TCItems.WAND.get(),
                TCIds.rl("item/wand_base"),
                List.of(new ItemOverride(PROPERTY_WAND_IS_STAFF, 1.0F, staffModel)));
    }

    private void registerJar(Block block, String modelName) {
        simpleBlock(block, TCIds.rl("block/" + modelName));
        delegateItem(block.asItem(), BEWLR_BLOCK_PARENT);
    }

    private void registerAlembic(Block block) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(vName("alembic"));
        ResourceLocation bore = TCIds.rl("block/alembic_bore");
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BooleanProperty property = BlockEssentiaTransport.propertyFor(direction);
            generator = generator.with(Condition.condition().term(property, true), sideVariant(bore, direction));
        }
        blockStateOutput.accept(generator);
        delegateItem(block.asItem(), TCIds.rl("block/alembic"));
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
        delegateItem(block.asItem(), TCIds.rl("block/" + modelName + "_off"));
    }

    private void registerDeconstructionTable() {
        registerInvisibleBlock(TCBlocks.DECONSTRUCTION_TABLE.get());
        delegateItem(TCBlocks.DECONSTRUCTION_TABLE.get().asItem(), TCIds.rl("item/deconstruction_table_base"));
    }

    private void registerResearchNote() {
        ResourceLocation complete = ModelLocationUtils.getModelLocation(TCItems.RESEARCH_NOTE.get(), "_complete");
        ModelTemplates.TWO_LAYERED_ITEM.create(
                complete,
                TextureMapping.layered(
                        TCIds.rl("item/research_note_complete"), TCIds.rl("item/research_note_complete_overlay")),
                modelOutput);
        generatedOverridesItem(
                TCItems.RESEARCH_NOTE.get(),
                Map.of("layer0", TCIds.rl("item/research_note"), "layer1", TCIds.rl("item/research_note_overlay")),
                List.of(new ItemOverride(PROPERTY_NOTE_COMPLETE, 1.0F, complete)));
    }

    private void registerResearchTable() {
        registerInvisibleBlock(TCBlocks.RESEARCH_TABLE.get());
        delegateItem(TCBlocks.RESEARCH_TABLE.get().asItem(), TCIds.rl("block/table_wood"));
    }

    private void registerInvisibleBlock(Block block) {
        simpleBlock(block, TCIds.rl("block/empty"));
    }

    private void registerNitor(DyeColor dye) {
        registerInvisibleBlock(TCBlocks.NITORS.get(dye).get());
        delegateItem(TCItems.NITORS.get(dye).get(), TCIds.rl("item/nitor"));
    }

    private void registerInfusionAltar() {
        registerPillar(TCBlocks.PILLAR_ARCANE.get(), "pillar_arcane");
        registerPillar(TCBlocks.PILLAR_ANCIENT.get(), "pillar_ancient");
        registerPillar(TCBlocks.PILLAR_ELDRITCH.get(), "pillar_eldritch");
        registerSimpleWithItem(TCBlocks.PEDESTAL_ARCANE.get(), "pedestal_arcane");
        registerSimpleWithItem(TCBlocks.RECHARGE_PEDESTAL.get(), "recharge_pedestal");
        registerSimpleWithItem(TCBlocks.PEDESTAL_ANCIENT.get(), "pedestal_ancient");
        registerSimpleWithItem(TCBlocks.PEDESTAL_ELDRITCH.get(), "pedestal_eldritch");
        registerSimpleWithItem(TCBlocks.INFUSION_MATRIX.get(), "infusion_matrix");
        delegateItem(TCBlocks.INFUSION_MATRIX.get().asItem(), TCIds.rl("block/infusion_matrix"));
    }

    private void registerPillar(Block block, String modelName) {
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block, vName(modelName)).with(horizontalDispatch()));
        delegateItem(block.asItem(), TCIds.rl("block/" + modelName));
    }

    private void registerSimpleWithItem(Block block, String modelName) {
        simpleBlock(block, TCIds.rl("block/" + modelName));
        if (block != TCBlocks.INFUSION_MATRIX.get()) {
            delegateItem(block.asItem(), TCIds.rl("block/" + modelName));
        }
    }

    private void registerSpa() {
        ResourceLocation spaModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                ModelLocationUtils.getModelLocation(TCBlocks.SPA.get()),
                new TextureMapping()
                        .put(TextureSlot.SIDE, TCIds.rl("block/spa_side"))
                        .put(TextureSlot.TOP, TCIds.rl("block/spa_top"))
                        .put(TextureSlot.BOTTOM, ResourceLocation.withDefaultNamespace("block/furnace_top")),
                modelOutput);
        simpleBlock(TCBlocks.SPA.get(), spaModel);
        delegateItem(TCItems.SPA.get(), spaModel);
        simpleBlock(TCBlocks.PURIFYING_FLUID.get(), TCIds.rl("block/purifying_fluid"));
        simpleBlock(TCBlocks.LIQUID_DEATH.get(), TCIds.rl("block/liquid_death"));
        flatItem(TCItems.BUCKET_LIQUID_DEATH.get());
    }

    private void registerGolemancy() {
        flatItem(TCItems.MIND_CLOCKWORK.get());
        flatItem(TCItems.MIND_BIOTHAUMIC.get());
        flatItem(TCItems.MODULE_VISION.get());
        flatItem(TCItems.MODULE_AGGRESSION.get());
        flatItem(TCItems.GOLEM_BELL.get());
        flatItem(TCItems.GOLEM_TOP_HAT.get());
        flatItem(TCItems.GOLEM_FEZ.get());
        flatItem(TCItems.GOLEM_GLASSES.get());
        flatItem(TCItems.GOLEM_BOWTIE.get());
        flatItem(TCItems.GOLEM_VISOR.get());
        flatItem(TCItems.SEAL_BLANK.get());
        flatItem(TCItems.SEAL_PICKUP.get());
        flatItem(TCItems.SEAL_PICKUP_ADVANCED.get());
        flatItem(TCItems.SEAL_FILL.get());
        flatItem(TCItems.SEAL_FILL_ADVANCED.get());
        flatItem(TCItems.SEAL_EMPTY.get());
        flatItem(TCItems.SEAL_EMPTY_ADVANCED.get());
        flatItem(TCItems.SEAL_HARVEST.get());
        flatItem(TCItems.SEAL_BUTCHER.get());
        flatItem(TCItems.SEAL_GUARD.get());
        flatItem(TCItems.SEAL_GUARD_ADVANCED.get());
        flatItem(TCItems.SEAL_LUMBER.get());
        flatItem(TCItems.SEAL_BREAKER.get());
        flatItem(TCItems.SEAL_BREAKER_ADVANCED.get());
        flatItem(TCItems.SEAL_USE.get());
        flatItem(TCItems.SEAL_PROVIDER.get());
        flatItem(TCItems.SEAL_STOCK.get());

        flatItem(TCItems.GOLEM_PLACER.get());

        ResourceLocation inlayDot = TCIds.rl("block/inlay_dot");
        ResourceLocation inlaySide = TCIds.rl("block/inlay_side");
        MultiPartGenerator inlayGenerator = MultiPartGenerator.multiPart(TCBlocks.INLAY.get())
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
                ModelLocationUtils.getModelLocation(TCItems.INLAY.get()),
                TextureMapping.layered(TCIds.rl("block/inlay_connect_under"), TCIds.rl("block/inlay_connect1")),
                modelOutput);

        ResourceLocation patternCrafterModel = TCIds.rl("block/pattern_crafter");
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(TCBlocks.PATTERN_CRAFTER.get(), v(patternCrafterModel))
                        .with(horizontalDispatch()));
        delegateItem(TCItems.PATTERN_CRAFTER.get(), patternCrafterModel);

        ResourceLocation sprayerModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                TCBlocks.POTION_SPRAYER.get(),
                new TextureMapping()
                        .put(TextureSlot.TOP, TCIds.rl("block/potion_sprayer_top"))
                        .put(TextureSlot.BOTTOM, TCIds.rl("block/potion_sprayer_bottom"))
                        .put(TextureSlot.SIDE, TCIds.rl("block/potion_sprayer_side")),
                modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.POTION_SPRAYER.get(), v(sprayerModel))
                .with(upBaseFacingDispatch()));
        delegateItem(TCItems.POTION_SPRAYER.get(), sprayerModel);

        ResourceLocation levitatorOn = TCIds.rl("block/levitator_on");
        ResourceLocation levitatorOff = TCIds.rl("block/levitator_off");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.LEVITATOR.get())
                .with(PropertyDispatch.properties(BlockStateProperties.ENABLED, BlockStateProperties.FACING)
                        .generate((enabled, facing) ->
                                Variant.merge(v(enabled ? levitatorOn : levitatorOff), facingRotation(facing)))));
        delegateItem(TCItems.LEVITATOR.get(), levitatorOff);

        registerInvisibleBlock(TCBlocks.GOLEM_BUILDER.get());
        delegateItem(TCItems.GOLEM_BUILDER.get(), TCIds.rl("item/golem_builder_base"));
        registerInvisibleBlock(TCBlocks.PLACEHOLDER_IRON_BARS.get());
        registerInvisibleBlock(TCBlocks.PLACEHOLDER_CAULDRON.get());
        registerInvisibleBlock(TCBlocks.PLACEHOLDER_ANVIL.get());
        registerInvisibleBlock(TCBlocks.PLACEHOLDER_TABLE.get());
    }

    private void registerConstructs() {
        flatItem(TCItems.TURRET_BASIC.get());
        flatItem(TCItems.TURRET_ADVANCED.get());
        flatItem(TCItems.ARCANE_BORE.get());
        registerInvisibleBlock(TCBlocks.ARCANE_BORE.get());
        flatItem(TCItems.GRAPPLE_GUN_TIP.get());
        flatItem(TCItems.GRAPPLE_GUN_SPOOL.get());
        flatItem(TCItems.ELDRITCH_EYE.get());
        flatItem(TCItems.RUNED_TABLET.get());
        overridesItem(
                TCItems.GRAPPLE_GUN.get(),
                TCIds.rl("item/grapple_gun_1"),
                List.of(new ItemOverride(PROPERTY_LOADED, 1.0F, TCIds.rl("item/grapple_gun_2"))));
        registerActivatorRail();
    }

    private void registerActivatorRail() {
        Block block = TCBlocks.ACTIVATOR_RAIL.get();
        ResourceLocation flat = ModelTemplates.RAIL_FLAT.create(block, TextureMapping.rail(block), modelOutput);
        ResourceLocation risingNE =
                ModelTemplates.RAIL_RAISED_NE.create(block, TextureMapping.rail(block), modelOutput);
        ResourceLocation risingSW =
                ModelTemplates.RAIL_RAISED_SW.create(block, TextureMapping.rail(block), modelOutput);
        ResourceLocation flatOn = ModelTemplates.RAIL_FLAT.createWithSuffix(
                block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), modelOutput);
        ResourceLocation risingNEOn = ModelTemplates.RAIL_RAISED_NE.createWithSuffix(
                block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), modelOutput);
        ResourceLocation risingSWOn = ModelTemplates.RAIL_RAISED_SW.createWithSuffix(
                block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), modelOutput);
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
        flatItem(TCItems.FOCUS_1.get());
        flatItem(TCItems.FOCUS_2.get());
        flatItem(TCItems.FOCUS_3.get());
    }

    private void registerRobeItem(Item item, String name) {
        ModelTemplates.TWO_LAYERED_ITEM.create(
                TCIds.rl("item/" + name),
                TextureMapping.layered(TCIds.rl("item/" + name), TCIds.rl("item/" + name + "_over")),
                modelOutput);
    }

    private void registerJarBrain() {
        simpleBlock(TCBlocks.JAR_BRAIN.get(), TCIds.rl("block/jar_normal"));
        delegateItem(TCBlocks.JAR_BRAIN.get().asItem(), BEWLR_BLOCK_PARENT);
    }

    private void registerNoiseDevices() {
        registerEnabledFacingDevice(TCBlocks.ARCANE_EAR.get(), "arcane_ear_on", "arcane_ear_off", false);
        registerEnabledFacingDevice(
                TCBlocks.ARCANE_EAR_TOGGLE.get(), "arcane_ear_toggle_on", "arcane_ear_toggle_off", false);

        registerEnabledFacingDevice(TCBlocks.LAMP_ARCANE.get(), "lamp_arcane_on", "lamp_arcane_off", true);
        registerEnabledFacingDevice(TCBlocks.LAMP_GROWTH.get(), "lamp_growth_on", "lamp_growth_off", true);
        registerEnabledFacingDevice(TCBlocks.LAMP_FERTILITY.get(), "lamp_fertility_on", "lamp_fertility_off", true);

        simpleFromExisting(TCBlocks.EVERFULL_URN.get(), "everfull_urn");
        registerEnabledFacingDevice(TCBlocks.VIS_GENERATOR.get(), "vis_generator", "vis_generator", false);
        registerFacingDevice(TCBlocks.ESSENTIA_INPUT.get(), "essentia_input", false);
        registerFacingDevice(TCBlocks.ESSENTIA_OUTPUT.get(), "essentia_output", false);

        simpleFromExisting(TCBlocks.CONDENSER.get(), "condenser");
        simpleFromExisting(TCBlocks.STABILIZER.get(), "stabilizer");
        simpleFromExisting(TCBlocks.VOID_SIPHON.get(), "void_siphon");
        registerLattice(TCBlocks.CONDENSER_LATTICE.get(), "condenser_lattice_core");
        registerLattice(TCBlocks.CONDENSER_LATTICE_DIRTY.get(), "condenser_lattice_core_dirty");
        registerRelay();

        ResourceLocation thaumatoriumModel = TCIds.rl("block/thaumatorium");
        PropertyDispatch thaumatoriumFacing = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.WEST, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.SOUTH,
                        Variant.variant()
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .select(
                        Direction.NORTH,
                        Variant.variant()
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.EAST,
                        Variant.variant()
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.THAUMATORIUM.get(), v(thaumatoriumModel))
                .with(thaumatoriumFacing));
        delegateItem(TCItems.THAUMATORIUM.get(), thaumatoriumModel);
        registerInvisibleBlock(TCBlocks.THAUMATORIUM_TOP.get());
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.BRAIN_BOX.get(), vName("brain_box"))
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
        delegateItem(TCItems.BRAIN_BOX.get(), TCIds.rl("block/brain_box"));

        registerInvisibleBlock(TCBlocks.CENTRIFUGE.get());
        delegateItem(TCItems.CENTRIFUGE.get(), TCIds.rl("item/centrifuge_base"));

        registerInvisibleBlock(TCBlocks.HUNGRY_CHEST.get());
        delegateItem(TCItems.HUNGRY_CHEST.get(), BEWLR_BLOCK_PARENT);
    }

    private void registerLattice(Block block, String coreModel) {
        ResourceLocation side = TCIds.rl("block/condenser_lattice_side");
        ResourceLocation core = TCIds.rl("block/" + coreModel);
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
        ResourceLocation on = TCIds.rl("block/redstone_relay_on");
        ResourceLocation off = TCIds.rl("block/redstone_relay_off");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.REDSTONE_RELAY.get())
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
        delegateItem(TCItems.REDSTONE_RELAY.get(), off);
    }

    private void registerFacingDevice(Block block, String modelName, boolean hanging) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, vName(modelName))
                .with(hanging ? downBaseFacingDispatch() : upBaseFacingDispatch()));
        delegateItem(block.asItem(), TCIds.rl("block/" + modelName));
    }

    private void registerEnabledFacingDevice(Block block, String onModel, String offModel, boolean hanging) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(BlockStateProperties.ENABLED, BlockStateProperties.FACING)
                        .generate((enabled, facing) -> Variant.merge(
                                vName(enabled ? onModel : offModel),
                                hanging ? hangingRotation(facing) : facingRotation(facing)))));
        delegateItem(block.asItem(), TCIds.rl("block/" + offModel));
    }

    private void registerAuraDevices(BlockModelGenerators blockModels) {
        blockModels.createTrivialCube(TCBlocks.MATRIX_SPEED.get());
        blockModels.createTrivialCube(TCBlocks.MATRIX_COST.get());

        ResourceLocation[] batteryModels = new ResourceLocation[5];
        for (int i = 0; i < 5; i++) {
            ResourceLocation textureId = TCIds.rl("block/vis_battery_" + i);
            batteryModels[i] = ModelTemplates.CUBE_ALL.create(textureId, TextureMapping.cube(textureId), modelOutput);
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.VIS_BATTERY.get())
                .with(PropertyDispatch.property(BlockVisBattery.CHARGE).generate(charge -> {
                    int tier = charge == 0 ? 0 : charge >= 10 ? 4 : (charge + 2) / 3;
                    return v(batteryModels[tier]);
                })));
        delegateItem(TCItems.VIS_BATTERY.get(), batteryModels[0]);

        ResourceLocation dioptraOn = TCIds.rl("block/dioptra_on");
        ResourceLocation dioptraOff = TCIds.rl("block/dioptra_off");
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.DIOPTRA.get())
                .with(PropertyDispatch.property(BlockStateProperties.ENABLED)
                        .select(true, v(dioptraOn))
                        .select(false, v(dioptraOff))));
        delegateItem(TCItems.DIOPTRA.get(), dioptraOn);
    }

    private void registerMirrorItem(Item item, String frameTexture) {
        ResourceLocation base = ModelLocationUtils.getModelLocation(item, "_off");
        ResourceLocation linked = ModelLocationUtils.getModelLocation(item, "_on");
        ModelTemplates.TWO_LAYERED_ITEM.create(
                base,
                TextureMapping.layered(TCIds.rl("block/" + frameTexture), TCIds.rl("block/mirrorpane")),
                modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(
                linked,
                TextureMapping.layered(TCIds.rl("block/" + frameTexture), TCIds.rl("block/mirrorpaneopen")),
                modelOutput);
        overridesItem(item, base, List.of(new ItemOverride(PROPERTY_LINKED, 1.0F, linked)));
    }

    private void mirrorBlockState(Block block) {
        ResourceLocation model = ModelTemplates.PARTICLE_ONLY.createWithSuffix(
                block, "_state", TextureMapping.particle(TCIds.rl("block/mirrorframe")), modelOutput);
        simpleBlock(block, model);
    }

    private void stoneAndStairModels() {
        simpleFromExisting(TCBlocks.STONE_ARCANE.get(), "stone_arcane");
        simpleFromExisting(TCBlocks.STONE_ARCANE_BRICK.get(), "stone_arcane_brick");
        simpleFromExisting(TCBlocks.STONE_ANCIENT.get(), "stone_ancient");
        simpleFromExisting(TCBlocks.STONE_ANCIENT_TILE.get(), "stone_ancient_tile");
        simpleFromExisting(TCBlocks.STONE_ANCIENT_ROCK.get(), "stone_ancient_rock");
        simpleFromExisting(TCBlocks.STONE_ANCIENT_GLYPHED.get(), "stone_ancient_glyphed");
        simpleFromExisting(TCBlocks.STONE_ANCIENT_DOORWAY.get(), "stone_ancient_doorway");
        simpleFromExisting(TCBlocks.STONE_ELDRITCH_TILE.get(), "stone_eldritch_tile");
        simpleFromExisting(TCBlocks.STONE_POROUS.get(), "stone_porous");

        stairsFromModels(TCBlocks.STAIRS_ARCANE.get(), "arcane_stairs", "arcane_inner_stairs", "arcane_outer_stairs");
        stairsFromModels(
                TCBlocks.STAIRS_ARCANE_BRICK.get(),
                "arcane_brick_stairs",
                "arcane_brick_inner_stairs",
                "arcane_brick_outer_stairs");
        stairsFromModels(
                TCBlocks.STAIRS_ANCIENT.get(), "ancient_stairs", "ancient_inner_stairs", "ancient_outer_stairs");
    }

    private void stairsFromModels(Block block, String straightName, String innerName, String outerName) {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(stairsDispatch(
                        TCIds.rl("block/" + straightName),
                        TCIds.rl("block/" + innerName),
                        TCIds.rl("block/" + outerName))));
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
        simpleFromExisting(TCBlocks.SAPLING_GREATWOOD.get(), "sapling_greatwood");
        simpleFromExisting(TCBlocks.SAPLING_SILVERWOOD.get(), "sapling_silverwood");
        flatItemFromBlock(TCItems.SAPLING_GREATWOOD.get(), TCBlocks.SAPLING_GREATWOOD.get());
        flatItemFromBlock(TCItems.SAPLING_SILVERWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get());
        simpleFromExisting(TCBlocks.PLANK_GREATWOOD.get(), "plank_greatwood");
        simpleFromExisting(TCBlocks.PLANK_SILVERWOOD.get(), "plank_silverwood");
        simpleFromExisting(TCBlocks.LEAVES_GREATWOOD.get(), "leaves_greatwood");
        simpleFromExisting(TCBlocks.LEAVES_SILVERWOOD.get(), "leaves_silverwood");
        log(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.WOOD_GREATWOOD.get());
        log(TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.WOOD_SILVERWOOD.get());
        log(TCBlocks.STRIPPED_LOG_GREATWOOD.get(), TCBlocks.STRIPPED_WOOD_GREATWOOD.get());
        log(TCBlocks.STRIPPED_LOG_SILVERWOOD.get(), TCBlocks.STRIPPED_WOOD_SILVERWOOD.get());
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
        cross(TCBlocks.PLANT_SHIMMERLEAF.get());
        cross(TCBlocks.PLANT_CINDERPEARL.get());
        cross(TCBlocks.PLANT_VISHROOM.get());

        flatItemFromBlock(TCItems.PLANT_SHIMMERLEAF.get(), TCBlocks.PLANT_SHIMMERLEAF.get());
        flatItemFromBlock(TCItems.PLANT_CINDERPEARL.get(), TCBlocks.PLANT_CINDERPEARL.get());
        flatItemFromBlock(TCItems.PLANT_VISHROOM.get(), TCBlocks.PLANT_VISHROOM.get());

        ResourceLocation grassModel = ResourceLocation.withDefaultNamespace("block/grass_block");
        simpleBlock(TCBlocks.GRASS_AMBIENT.get(), grassModel);
        delegateItem(TCItems.GRASS_AMBIENT.get(), grassModel);
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

    private void taintModels() {
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                TCBlocks.TAINT_ROCK.get(), rotatedWeighted(new String[] {"taint_rock"}, new int[] {1})));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                TCBlocks.TAINT_SOIL.get(),
                rotatedWeighted(new String[] {"taint_soil_0", "taint_soil_1", "taint_soil_2"}, new int[] {16, 1, 1})));
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                TCBlocks.TAINT_CRUST.get(),
                rotatedWeighted(
                        new String[] {"taint_crust_0", "taint_crust_1", "taint_crust_2"}, new int[] {8, 1, 1})));

        simpleFromExisting(TCBlocks.FLUX_GOO.get(), "flux_goo");
        simpleFromExisting(TCBlocks.TAINT_GEYSER.get(), "taint_geyser");
        registerTaintLog();
        registerTaintFeature();
        registerTaintFibre();

        delegateItem(TCBlocks.TAINT_ROCK.asItem(), TCIds.rl("block/taint_rock"));
        delegateItem(TCBlocks.TAINT_SOIL.asItem(), TCIds.rl("block/taint_soil_0"));
        delegateItem(TCBlocks.TAINT_CRUST.asItem(), TCIds.rl("block/taint_crust_0"));
        delegateItem(TCBlocks.TAINT_GEYSER.asItem(), TCIds.rl("block/taint_geyser"));
        delegateItem(TCBlocks.TAINT_LOG.asItem(), TCIds.rl("block/taint_log"));
        delegateItem(TCBlocks.TAINT_FEATURE.asItem(), TCIds.rl("block/taint_orb_0"));
        delegateItem(TCBlocks.TAINT_FIBRE.asItem(), TCIds.rl("block/taint_fibre"));
    }

    private void registerTaintLog() {
        List<Variant> barks = new ArrayList<>();
        for (int tex = 1; tex <= 2; tex++) {
            for (String face : new String[] {"north", "south", "east", "west"}) {
                barks.add(vName("taint_log_" + face + tex));
            }
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                        TCBlocks.TAINT_LOG.get(), barks.toArray(Variant[]::new))
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
            ResourceLocation model = TCIds.rl("block/" + models[i]);
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

    private void registerTaintFeature() {
        Variant[] orbs = new Variant[] {vName("taint_orb_0"), vName("taint_orb_1"), vName("taint_orb_2")};
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.TAINT_FEATURE.get(), orbs)
                .with(PropertyDispatch.property(DirectionalBlock.FACING)
                        .select(Direction.UP, Variant.variant())
                        .select(
                                Direction.DOWN,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                        .select(
                                Direction.NORTH,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                        .select(
                                Direction.SOUTH,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
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
    }

    private void registerTaintFibre() {
        ResourceLocation fibre = TCIds.rl("block/taint_fibre");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(TCBlocks.TAINT_FIBRE.get())
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
                TCBlocks.SLAB_GREATWOOD.get(),
                TCBlocks.PLANK_GREATWOOD.get(),
                blockTexture("plank_greatwood"),
                blockTexture("plank_greatwood"),
                blockTexture("plank_greatwood"));
        slab(
                TCBlocks.SLAB_SILVERWOOD.get(),
                TCBlocks.PLANK_SILVERWOOD.get(),
                blockTexture("plank_silverwood"),
                blockTexture("plank_silverwood"),
                blockTexture("plank_silverwood"));
        slab(
                TCBlocks.SLAB_ARCANE_STONE.get(),
                TCBlocks.STONE_ARCANE.get(),
                blockTexture("arcane_stone_1"),
                blockTexture("arcane_stone_2"),
                blockTexture("arcane_stone_3"));
        slab(
                TCBlocks.SLAB_ARCANE_BRICK.get(),
                TCBlocks.STONE_ARCANE_BRICK.get(),
                blockTexture("arcane_brick_stone"),
                blockTexture("arcane_brick_stone"),
                blockTexture("arcane_brick_stone"));
        slab(
                TCBlocks.SLAB_ANCIENT.get(),
                TCBlocks.STONE_ANCIENT.get(),
                blockTexture("ancient_stone_1"),
                blockTexture("ancient_stone_2"),
                blockTexture("ancient_stone_3"));
        slab(
                TCBlocks.SLAB_ELDRITCH.get(),
                TCBlocks.STONE_ELDRITCH_TILE.get(),
                blockTexture("eldritch_stone_1"),
                blockTexture("eldritch_stone_2"),
                blockTexture("eldritch_stone_3"));
        stairsFromTexture(TCBlocks.STAIRS_GREATWOOD.get(), blockTexture("plank_greatwood"));
        stairsFromTexture(TCBlocks.STAIRS_SILVERWOOD.get(), blockTexture("plank_silverwood"));
        existingModelWithItem(TCBlocks.TABLE_WOOD.get(), "table_wood");
        existingModelWithItem(TCBlocks.TABLE_STONE.get(), "table_stone");
        paving(TCBlocks.PAVING_STONE_TRAVEL.get(), "paving_stone_travel");
        paving(TCBlocks.PAVING_STONE_BARRIER.get(), "paving_stone_barrier");
    }

    private static ResourceLocation blockTexture(String name) {
        return TCIds.rl("block/" + name);
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
        simpleBlock(block, TCIds.rl("block/" + modelName));
        delegateItem(block.asItem(), TCIds.rl("block/" + modelName));
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
        cube(TCBlocks.OBSIDIAN_TILE.get(), "obsidian_tile");
        obsidianTotem();
        cube(TCBlocks.ELDRITCH_STONE.get(), "eldritch_stone");
        cube(TCBlocks.ELDRITCH_STONE_INERT.get(), "eldritch_stone");
        cube(TCBlocks.ELDRITCH_ROCK.get(), "eldritch_rock");
        cube(TCBlocks.ELDRITCH_CRUST.get(), "eldritch_crust");
        insetBlock(TCBlocks.ELDRITCH_CRUST_GLOWING.get(), "eldritch_crust_glowing");
        cube(TCBlocks.ELDRITCH_DOOR.get(), "eldritch_door");
        insetBlock(TCBlocks.ELDRITCH_STONE_CRYSTAL.get(), "eldritch_stone_crystal");
        eldritchLock();
        crabSpawner();
        column(TCBlocks.ELDRITCH_PEDESTAL.get(), "eldritch_pedestal_side", "eldritch_stone");
        invisibleWithMeshItem(TCBlocks.ELDRITCH_ALTAR.get(), "eldritch_altar", "eldritch_altar_item");
        invisibleWithCubeItem(TCBlocks.ELDRITCH_OBELISK.get(), "eldritch_deco");
        invisibleWithCubeItem(TCBlocks.ELDRITCH_PILLAR.get(), "eldritch_deco");
        invisibleWithCubeItem(TCBlocks.ELDRITCH_CAPSTONE.get(), "eldritch_deco");
        trap();
        invisible(TCBlocks.ELDRITCH_NOTHING.get());
        invisible(TCBlocks.ELDRITCH_PORTAL.get());
        stairsFromTexture(TCBlocks.STAIRS_ELDRITCH.get(), blockTexture("eldritch_stone"));
    }

    private void cube(Block block, String textureName) {
        ResourceLocation model = ModelTemplates.CUBE_ALL.create(
                block, new TextureMapping().put(TextureSlot.ALL, blockTexture(textureName)), modelOutput);
        simpleBlock(block, model);
        delegateItem(block.asItem(), model);
    }

    private void eldritchLock() {
        ResourceLocation model = ModelTemplates.CUBE_ORIENTABLE.create(
                TCBlocks.ELDRITCH_LOCK.get(),
                new TextureMapping()
                        .put(TextureSlot.FRONT, blockTexture("eldritch_lock_face"))
                        .put(TextureSlot.SIDE, blockTexture("eldritch_lock_side"))
                        .put(TextureSlot.TOP, blockTexture("eldritch_lock_side")),
                modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(TCBlocks.ELDRITCH_LOCK.get(), v(model))
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
        delegateItem(TCBlocks.ELDRITCH_LOCK.get().asItem(), model);
    }

    private static final int INSET_DEPTH = 2;
    private static final int INSET_ALL_EXPOSED = 63;

    private void insetBlock(Block block, String textureName) {
        ResourceLocation texture = TCIds.rl("block/" + textureName);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (int mask = 0; mask <= INSET_ALL_EXPOSED; mask++) {
            ResourceLocation model = TCIds.rl("block/" + textureName + "_inset_" + mask);
            int finalMask = mask;
            modelOutput.accept(model, () -> insetModel(texture, finalMask));
            Condition.TerminalCondition condition = Condition.condition();
            for (Direction dir : Direction.values()) {
                condition = condition.term(BlockEldritchInset.EXPOSED.get(dir), insetExposed(mask, dir));
            }
            generator = generator.with(condition, v(model));
        }
        blockStateOutput.accept(generator);
        delegateItem(block.asItem(), TCIds.rl("block/" + textureName + "_inset_" + INSET_ALL_EXPOSED));
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
        Block block = TCBlocks.OBSIDIAN_TOTEM.get();
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
        for (Block totem : List.of(block, TCBlocks.OBSIDIAN_TOTEM_CHARGED.get())) {
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
        Block block = TCBlocks.ELDRITCH_TRAP.get();
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
        Block block = TCBlocks.ELDRITCH_CRAB_SPAWNER.get();
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
        delegateItem(block.asItem(), TCIds.rl("block/" + itemModelName));
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
        registerPrimordialPearl();
    }

    private void registerPhial() {
        ResourceLocation filled = ModelLocationUtils.getModelLocation(TCItems.PHIAL.get(), "_filled");
        ModelTemplates.TWO_LAYERED_ITEM.create(
                filled,
                TextureMapping.layered(
                        TextureMapping.getItemTexture(TCItems.PHIAL.get()),
                        TextureMapping.getItemTexture(TCItems.PHIAL.get(), "_overlay")),
                modelOutput);
        generatedOverridesItem(
                TCItems.PHIAL.get(),
                Map.of("layer0", TextureMapping.getItemTexture(TCItems.PHIAL.get())),
                List.of(new ItemOverride(PROPERTY_FILLED, 1.0F, filled)));
    }

    private void registerPrimordialPearl() {
        Item item = TCItems.PRIMORDIAL_PEARL.get();
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
