package com.leclowndu93150.thaumaturge.data.model;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.client.data.models.model.TextureMapping;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.color.*;
import com.leclowndu93150.thaumaturge.client.model.*;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.content.decor.CandleHolderMaterial;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotem;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.content.device.BlockVisBattery;
import com.leclowndu93150.thaumaturge.content.device.grate.BlockItemGrate;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.golem.BlockGolemFetter;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.ResearchTablePart;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSporeStalk;
import com.leclowndu93150.thaumaturge.content.warding.BlockArcanePressurePlate;
import com.leclowndu93150.thaumaturge.data.model.crystal.CrystalBlockstateGenerator;
import com.leclowndu93150.thaumaturge.data.model.crystal.CrystalItemModelGenerator;
import com.leclowndu93150.thaumaturge.data.model.crystal.EssentiaCrystalModelGenerator;
import com.leclowndu93150.thaumaturge.data.model.warding.WardedGlassModelGenerator;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.RootTransformsBuilder;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class TTModelProvider extends ModelProvider {
    private static final int ROBES_UNDYED_ARGB = 0xFF6A3880;

    private static final TextureSlot LEGACY_MESH_SLOT = TextureSlot.create("legacy");
    private static final Identifier DEEPSLATE_TEXTURE = Identifier.withDefaultNamespace("block/deepslate");
    private static final Identifier BLOCK_PARENT = Identifier.withDefaultNamespace("block/block");
    private static final TextureSlot GRINDSTONE_PIVOT_SLOT = TextureSlot.create("pivot");
    private static final TextureSlot GRINDSTONE_ROUND_SLOT = TextureSlot.create("round");
    private static final TextureSlot GRINDSTONE_LEG_SLOT = TextureSlot.create("leg");
    private static final ModelTemplate ARCANE_GRINDSTONE = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("block/grindstone")), Optional.empty(), GRINDSTONE_PIVOT_SLOT,
            GRINDSTONE_ROUND_SLOT, TextureSlot.SIDE, TextureSlot.PARTICLE, GRINDSTONE_LEG_SLOT);
    private static final TextureSlot GRATE_HATCH_SLOT = TextureSlot.create("hatch");
    private static final float GRATE_MIN_Y = 14.0F;
    private static final float GRATE_SIDE_V_MIN = 15.0F;
    private static final ModelTemplate BLOCK_PARTICLE = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("block/block")), Optional.empty(), TextureSlot.PARTICLE);
    private static final ModelTemplate THREE_LAYERED_ITEM = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/generated")), Optional.empty(), TextureSlot.LAYER0, TextureSlot.LAYER1,
            TextureSlot.LAYER2);
    private static final ModelTemplate CONDENSER_RETEXTURED = new ModelTemplate(Optional.of(TTIds.rl("block/condenser")), Optional.empty(), TextureSlot.SIDE, TextureSlot.PARTICLE);
    private static final int FOLIAGE_DEFAULT_COLOR = 0x48B518;
    private static final int INSET_DEPTH = 2;
    private static final int INSET_ALL_EXPOSED = 63;
    private static final float INSET_GUI_SCALE = 0.8F;
    private static final float INSET_HELD_SCALE = 0.5F;
    private static final float INSET_GROUND_SCALE = 0.35F;
    private static final float SPEAR_SWAP_ANIMATION_SCALE = 1.95F;
    private static final float BLOCK_CENTER = 0.5F;
    private static final float QUARTER_TURN_DEGREES = 90.0F;
    private static final int HALF_TURN_QUARTERS = 2;
    private static final int FULL_TURN_QUARTERS = 4;
    private static final float RESEARCH_TABLE_ITEM_SCALE = 0.5F;

    public TTModelProvider(PackOutput output) {
        super(output, TTIds.MODID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return TTBlocks.BLOCKS.getEntries().stream();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return TTItems.ITEMS.getEntries().stream();
    }

    private static void registerSpear(ItemModelGenerators itemModels, Item item) {
        ItemModel.Unbaked flat = ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), itemModels.modelOutput));
        ItemModel.Unbaked inHand = ItemModelUtils.plainModel(ModelTemplates.SPEAR_IN_HAND.create(item, TextureMapping.layer0(TextureMapping.getItemTexture(item, "_in_hand")), itemModels.modelOutput));
        ItemModel.Unbaked dispatch = ItemModelUtils.select(new DisplayContext(), inHand,
                ItemModelUtils.when(List.of(ItemDisplayContext.GUI, ItemDisplayContext.GROUND, ItemDisplayContext.FIXED, ItemDisplayContext.ON_SHELF), flat));
        itemModels.itemModelOutput.accept(item, dispatch, new ClientItem.Properties(true, false, SPEAR_SWAP_ANIMATION_SCALE));
    }

    private static void registerVoidRobePiece(ItemModelGenerators itemModels, Item item, String name) {
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/" + name);
        Material clothTex = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/" + name + "_over"));
        Material metalTex = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/" + name));
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModelId, TextureMapping.layered(clothTex, metalTex), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(itemModelId, new Dye(ROBES_UNDYED_ARGB)));
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

    private static void registerInvisibleBlock(BlockModelGenerators blockModels, Block block) {
        Identifier empty = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/empty");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(empty)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private static void registerNitor(BlockModelGenerators blockModels, ItemModelGenerators itemModels, DyeColor dye) {
        var block = TTBlocks.NITORS.get(dye).get();
        Identifier empty = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/empty");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(empty)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));

        var item = TTItems.NITORS.get(dye).get();
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/nitor");
        int rgb = dye.getTextureDiffuseColor() & 0xFFFFFF;
        ItemModel.Unbaked flat = ItemModelUtils.tintedModel(itemModelId, new Constant(rgb));
        ItemModel.Unbaked inHand = new SpecialModelWrapper.Unbaked(Identifier.withDefaultNamespace("block/block"), Optional.empty(), new NitorItemSpecialRenderer.Unbaked(block.dyeColor()));
        itemModels.itemModelOutput.accept(item, ItemModelUtils.select(new DisplayContext(), flat, ItemModelUtils.when(
                List.of(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
                inHand)));
    }

    private static void registerInfusionAltar(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        PropertyDispatch<VariantMutator> facing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        registerPillar(blockModels, itemModels, TTBlocks.PILLAR_ARCANE.get(), "pillar_arcane", facing);
        registerPillar(blockModels, itemModels, TTBlocks.PILLAR_ANCIENT.get(), "pillar_ancient", facing);
        registerPillar(blockModels, itemModels, TTBlocks.PILLAR_ELDRITCH.get(), "pillar_eldritch", facing);
        registerSimpleWithItem(blockModels, itemModels, TTBlocks.PEDESTAL_ARCANE.get(), "pedestal_arcane");
        registerSimpleWithItem(blockModels, itemModels, TTBlocks.RECHARGE_PEDESTAL.get(), "recharge_pedestal");
        registerSimpleWithItem(blockModels, itemModels, TTBlocks.PEDESTAL_ANCIENT.get(), "pedestal_ancient");
        registerSimpleWithItem(blockModels, itemModels, TTBlocks.PEDESTAL_ELDRITCH.get(), "pedestal_eldritch");
        registerSimpleWithItem(blockModels, itemModels, TTBlocks.INFUSION_MATRIX.get(), "infusion_matrix");
        itemModels.itemModelOutput.accept(TTBlocks.INFUSION_MATRIX.asItem(), ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/infusion_matrix")));
    }

    private static void registerPillar(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName, PropertyDispatch<VariantMutator> facing) {
        Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant).with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model), new ClientItem.Properties(true, true, 1));
    }

    private static void registerSimpleWithItem(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
        if (block != TTBlocks.INFUSION_MATRIX.get()) {
            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
        }
    }

    private static void registerSpa(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier spaModel = ModelTemplates.CUBE_BOTTOM_TOP.create(ModelLocationUtils.getModelLocation(TTBlocks.SPA.get()),
                new TextureMapping().put(TextureSlot.SIDE, new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/spa_side")))
                        .put(TextureSlot.TOP, new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/spa_top")))
                        .put(TextureSlot.BOTTOM, new Material(Identifier.withDefaultNamespace("block/furnace_top"))),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.SPA.get(), new MultiVariant(WeightedList.of(new Variant(spaModel)))));
        itemModels.itemModelOutput.accept(TTItems.SPA.get(), ItemModelUtils.plainModel(spaModel));
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(TTBlocks.PURIFYING_FLUID.get(), new MultiVariant(WeightedList.of(new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/purifying_fluid"))))));
        itemModels.generateFlatItem(TTItems.BUCKET_LIQUID_DEATH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.BUCKET_PURIFYING.get(), ModelTemplates.FLAT_ITEM);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(TTBlocks.LIQUID_DEATH.get(), new MultiVariant(WeightedList.of(new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/liquid_death"))))));
    }

    private static void registerGolemancy(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TTItems.MIND_CLOCKWORK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MIND_BIOTHAUMIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MODULE_VISION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MODULE_AGGRESSION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GOLEM_BELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GOLEM_TOP_HAT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GOLEM_FEZ.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GOLEM_GLASSES.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GOLEM_BOWTIE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GOLEM_VISOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_BLANK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_PICKUP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_PICKUP_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_FILL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_FILL_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_EMPTY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_EMPTY_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_HARVEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_BUTCHER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_GUARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_GUARD_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_LUMBER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_BREAKER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_BREAKER_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_USE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_PROVIDER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SEAL_STOCK.get(), ModelTemplates.FLAT_ITEM);

        itemModels.itemModelOutput.accept(TTItems.GOLEM_PLACER.get(), new SpecialModelWrapper.Unbaked(TTIds.rl("item/golem_base"), Optional.empty(), new GolemItemSpecialRenderer.Unbaked()));

        Identifier inlayDot = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/inlay_dot");
        Identifier inlaySide = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/inlay_side");
        MultiPartGenerator inlayGenerator = MultiPartGenerator.multiPart(TTBlocks.INLAY.get()).with(new MultiVariant(WeightedList.of(new Variant(inlayDot))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.NORTH, true), new MultiVariant(WeightedList.of(new Variant(inlaySide))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.EAST, true), new MultiVariant(WeightedList.of(BlockModelGenerators.Y_ROT_90.apply(new Variant(inlaySide)))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.SOUTH, true), new MultiVariant(WeightedList.of(BlockModelGenerators.Y_ROT_180.apply(new Variant(inlaySide)))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.WEST, true), new MultiVariant(WeightedList.of(BlockModelGenerators.Y_ROT_270.apply(new Variant(inlaySide)))));
        blockModels.blockStateOutput.accept(inlayGenerator);
        Identifier inlayItemModel = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(TTItems.INLAY.get()), TextureMapping
                .layered(new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/inlay_connect_under")), new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/inlay_connect1"))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TTItems.INLAY.get(), ItemModelUtils.plainModel(inlayItemModel));

        Identifier patternCrafterModel = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/pattern_crafter");
        PropertyDispatch<VariantMutator> patternCrafterFacing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270).select(Direction.EAST, BlockModelGenerators.Y_ROT_90);
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(TTBlocks.PATTERN_CRAFTER.get(), new MultiVariant(WeightedList.of(new Variant(patternCrafterModel)))).with(patternCrafterFacing));
        itemModels.itemModelOutput.accept(TTItems.PATTERN_CRAFTER.get(), ItemModelUtils.plainModel(patternCrafterModel));

        Identifier sprayerModel = ModelTemplates.CUBE_BOTTOM_TOP.create(TTBlocks.POTION_SPRAYER.get(),
                new TextureMapping().put(TextureSlot.TOP, new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/potion_sprayer_top")))
                        .put(TextureSlot.BOTTOM, new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/potion_sprayer_bottom")))
                        .put(TextureSlot.SIDE, new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/potion_sprayer_side"))),
                blockModels.modelOutput);
        PropertyDispatch<VariantMutator> sprayerFacing = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.POTION_SPRAYER.get(), new MultiVariant(WeightedList.of(new Variant(sprayerModel)))).with(sprayerFacing));
        itemModels.itemModelOutput.accept(TTItems.POTION_SPRAYER.get(), ItemModelUtils.plainModel(sprayerModel));

        PropertyDispatch<VariantMutator> levitatorFacing = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        Identifier levitatorOn = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/levitator_on");
        Identifier levitatorOff = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/levitator_off");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.LEVITATOR.get()).with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(levitatorOn)))).select(false, new MultiVariant(WeightedList.of(new Variant(levitatorOff))))).with(levitatorFacing));
        itemModels.itemModelOutput.accept(TTItems.LEVITATOR.get(), ItemModelUtils.plainModel(levitatorOff));
        registerItemGrate(blockModels, itemModels);
        registerGolemFetter(blockModels, itemModels);
        registerTallowBlock(blockModels, itemModels);
        registerArcaneLocks(blockModels, itemModels);

        registerInvisibleBlock(blockModels, TTBlocks.GOLEM_BUILDER.get());
        itemModels.itemModelOutput.accept(TTItems.GOLEM_BUILDER.get(),
                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/golem_builder_base"), Optional.empty(), new GolemBuilderItemSpecialRenderer.Unbaked()));
        registerInvisibleBlock(blockModels, TTBlocks.PLACEHOLDER_IRON_BARS.get());
        registerInvisibleBlock(blockModels, TTBlocks.PLACEHOLDER_CAULDRON.get());
        registerInvisibleBlock(blockModels, TTBlocks.PLACEHOLDER_ANVIL.get());
        registerInvisibleBlock(blockModels, TTBlocks.PLACEHOLDER_TABLE.get());
    }

    private static void registerConstructs(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TTItems.TURRET_BASIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TURRET_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ARCANE_BORE.get(), ModelTemplates.FLAT_ITEM);
        registerInvisibleBlock(blockModels, TTBlocks.ARCANE_BORE.get());
        itemModels.generateFlatItem(TTItems.GRAPPLE_GUN_TIP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GRAPPLE_GUN_SPOOL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ELDRITCH_EYE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.RUNED_TABLET.get(), ModelTemplates.FLAT_ITEM);
        ItemModel.Unbaked unloaded = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/grapple_gun_1"));
        ItemModel.Unbaked loaded = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/grapple_gun_2"));
        itemModels.itemModelOutput.accept(TTItems.GRAPPLE_GUN.get(), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TTDataComponents.GRAPPLE_LOADED.get()), loaded, unloaded));
        registerActivatorRail(blockModels);
    }

    private static void registerActivatorRail(BlockModelGenerators blockModels) {
        Block block = TTBlocks.ACTIVATOR_RAIL.get();
        MultiVariant flat = BlockModelGenerators.plainVariant(ModelTemplates.RAIL_FLAT.create(block, TextureMapping.rail(block), blockModels.modelOutput));
        MultiVariant risingNE = BlockModelGenerators.plainVariant(ModelTemplates.RAIL_RAISED_NE.create(block, TextureMapping.rail(block), blockModels.modelOutput));
        MultiVariant risingSW = BlockModelGenerators.plainVariant(ModelTemplates.RAIL_RAISED_SW.create(block, TextureMapping.rail(block), blockModels.modelOutput));

        MultiVariant flatOn = BlockModelGenerators
                .plainVariant(ModelTemplates.RAIL_FLAT.createWithSuffix(block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), blockModels.modelOutput));
        MultiVariant risingNEOn = BlockModelGenerators
                .plainVariant(ModelTemplates.RAIL_RAISED_NE.createWithSuffix(block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), blockModels.modelOutput));
        MultiVariant risingSWOn = BlockModelGenerators
                .plainVariant(ModelTemplates.RAIL_RAISED_SW.createWithSuffix(block, "_on", TextureMapping.rail(TextureMapping.getBlockTexture(block, "_on")), blockModels.modelOutput));
        blockModels.registerSimpleFlatItemModel(block);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT).generate((powered, railShape) -> switch (railShape) {
                    case NORTH_SOUTH -> powered ? flatOn : flat;
                    case EAST_WEST -> (powered ? flatOn : flat).with(BlockModelGenerators.Y_ROT_90);
                    case ASCENDING_EAST -> (powered ? risingNEOn : risingNE).with(BlockModelGenerators.Y_ROT_90);
                    case ASCENDING_WEST -> (powered ? risingSWOn : risingSW).with(BlockModelGenerators.Y_ROT_90);
                    case ASCENDING_NORTH -> powered ? risingNEOn : risingNE;
                    case ASCENDING_SOUTH -> powered ? risingSWOn : risingSW;
                    default -> throw new UnsupportedOperationException();
                })));
    }

    private static void registerCasters(ItemModelGenerators itemModels) {
        registerFocusItem(itemModels, TTItems.FOCUS_1.get());
        registerFocusItem(itemModels, TTItems.FOCUS_2.get());
        registerFocusItem(itemModels, TTItems.FOCUS_3.get());
    }

    private static void registerFocusItem(ItemModelGenerators itemModels, Item item) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(model, new FocusColorTint()));
    }

    private static void registerRobeItem(ItemModelGenerators itemModels, Item item, String name) {
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/" + name);
        Material baseTex = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/" + name));
        Material overTex = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/" + name + "_over"));
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModelId, TextureMapping.layered(baseTex, overTex), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(itemModelId, new Dye(ROBES_UNDYED_ARGB)));
    }

    private static void registerMirrorItem(ItemModelGenerators itemModels, Item item, String frameTexture) {
        Material frame = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + frameTexture));
        Identifier model = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(item),
                TextureMapping.layered(frame, new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/mirrorpane"))), itemModels.modelOutput);
        Identifier linkedModel = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(item, "_on"),
                TextureMapping.layered(frame, new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/mirrorpaneopen"))), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item,
                ItemModelUtils.conditional(new HasComponent(TTDataComponents.MIRROR_LINK.get(), false), ItemModelUtils.plainModel(linkedModel), ItemModelUtils.plainModel(model)));
    }

    private static void mirrorBlockState(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.PARTICLE_ONLY.createWithSuffix(block, "_state", TextureMapping.particle(new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/mirrorframe"))),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private static void registerItemGrate(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block grate = TTBlocks.ITEM_GRATE.get();
        TextureMapping textures = new TextureMapping().put(TextureSlot.ALL, TextureMapping.getBlockTexture(grate)).put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(grate))
                .put(GRATE_HATCH_SLOT, TextureMapping.getBlockTexture(grate, "_closed"));
        Identifier open = itemGrateTemplate(false).create(grate, textures, blockModels.modelOutput);
        Identifier closed = itemGrateTemplate(true).create(grate, textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(grate)
                .with(PropertyDispatch.initial(BlockItemGrate.OPEN).select(true, BlockModelGenerators.plainVariant(open)).select(false, BlockModelGenerators.plainVariant(closed))));
        itemModels.itemModelOutput.accept(TTItems.ITEM_GRATE.get(), ItemModelUtils.plainModel(open));
    }

    private static ModelTemplate itemGrateTemplate(boolean closed) {
        ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block")).requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE).element(element -> element.from(0.0F, GRATE_MIN_Y, 0.0F).to(16.0F, 16.0F, 16.0F).allFaces((direction, face) -> {
                    face.texture(TextureSlot.ALL);
                    if (direction == Direction.UP) {
                        face.cullface(Direction.UP);
                    } else if (direction != Direction.DOWN) {
                        face.cullface(direction).uvs(0.0F, GRATE_SIDE_V_MIN, 16.0F, 16.0F);
                    }
                }));
        if (closed) {
            builder.suffix("_closed").requiredTextureSlot(GRATE_HATCH_SLOT).element(element -> element.from(0.0F, GRATE_MIN_Y, 0.0F).to(16.0F, 16.0F, 16.0F)
                    .face(Direction.UP, face -> face.texture(GRATE_HATCH_SLOT).cullface(Direction.UP)).face(Direction.DOWN, face -> face.texture(GRATE_HATCH_SLOT)));
        }
        return builder.build();
    }

    private static void registerArcaneLocks(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createDoor(TTBlocks.ARCANE_DOOR.get());
        Block plate = TTBlocks.ARCANE_PRESSURE_PLATE.get();
        PropertyDispatch.C2<MultiVariant, Integer, Boolean> plateDispatch = PropertyDispatch.initial(BlockArcanePressurePlate.MODE, BlockStateProperties.POWERED);
        Identifier itemModel = null;
        for (int mode = BlockArcanePressurePlate.MODE_EVERYONE; mode <= BlockArcanePressurePlate.MODE_ALL_BUT_ACCESS; mode++) {
            String suffix = "_" + mode;
            TextureMapping texture = TextureMapping.defaultTexture(TextureMapping.getBlockTexture(plate, suffix));
            Identifier up = ModelTemplates.PRESSURE_PLATE_UP.createWithSuffix(plate, suffix, texture, blockModels.modelOutput);
            Identifier down = ModelTemplates.PRESSURE_PLATE_DOWN.createWithSuffix(plate, suffix, texture, blockModels.modelOutput);
            plateDispatch.select(mode, false, BlockModelGenerators.plainVariant(up)).select(mode, true, BlockModelGenerators.plainVariant(down));
            if (mode == BlockArcanePressurePlate.MODE_EVERYONE) {
                itemModel = up;
            }
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(plate).with(plateDispatch));
        itemModels.itemModelOutput.accept(TTItems.ARCANE_PRESSURE_PLATE.get(), ItemModelUtils.plainModel(itemModel));
        itemModels.generateFlatItem(TTItems.ARCANE_KEY_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ARCANE_KEY_GOLD.get(), ModelTemplates.FLAT_ITEM);
    }

    private static void registerGolemFetter(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block fetter = TTBlocks.GOLEM_FETTER.get();
        Material side = TextureMapping.getBlockTexture(fetter, "_side");
        Identifier off = ModelTemplates.CUBE_BOTTOM_TOP.create(fetter,
                new TextureMapping().put(TextureSlot.BOTTOM, side).put(TextureSlot.SIDE, side).put(TextureSlot.TOP, TextureMapping.getBlockTexture(fetter)), blockModels.modelOutput);
        Identifier powered = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(fetter, "_powered",
                new TextureMapping().put(TextureSlot.BOTTOM, side).put(TextureSlot.SIDE, side).put(TextureSlot.TOP, TextureMapping.getBlockTexture(fetter, "_active")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(fetter)
                .with(PropertyDispatch.initial(BlockGolemFetter.POWERED).select(false, BlockModelGenerators.plainVariant(off)).select(true, BlockModelGenerators.plainVariant(powered))));
        itemModels.itemModelOutput.accept(TTItems.GOLEM_FETTER.get(), ItemModelUtils.plainModel(off));
    }

    private static void registerTallowBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block block = TTBlocks.TALLOW_BLOCK.get();
        Material side = TextureMapping.getBlockTexture(block);
        Identifier model = ModelTemplates.CUBE_BOTTOM_TOP.create(block,
                new TextureMapping().put(TextureSlot.BOTTOM, side).put(TextureSlot.SIDE, side).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
        itemModels.itemModelOutput.accept(TTItems.TALLOW_BLOCK.get(), ItemModelUtils.plainModel(model));
    }

    private static void cubeAllTexture(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier textureId = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + textureName);
        Material texture = new Material(textureId);
        Identifier modelId = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(texture), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private static void deepslateOre(BlockModelGenerators blockModels, Block block, String overlay) {
        Identifier model = ModelLocationUtils.getModelLocation(block);
        blockModels.modelOutput.accept(model, overlaidCubeModel(DEEPSLATE_TEXTURE, TTIds.rl("block/" + overlay)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private static ModelInstance overlaidCubeModel(Identifier base, Identifier overlay) {
        return () -> {
            JsonObject root = new JsonObject();
            root.addProperty("parent", BLOCK_PARENT.toString());
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", base.toString());
            textures.addProperty("base", base.toString());
            textures.addProperty("overlay", overlay.toString());
            root.add("textures", textures);
            JsonArray elements = new JsonArray();
            elements.add(fullCube("#base"));
            elements.add(fullCube("#overlay"));
            root.add("elements", elements);
            return root;
        };
    }

    private static JsonObject fullCube(String texture) {
        JsonObject element = new JsonObject();
        element.add("from", insetCoords(0, 0, 0));
        element.add("to", insetCoords(16, 16, 16));
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

    private static boolean insetExposed(int mask, Direction dir) {
        return (mask & (1 << dir.get3DDataValue())) != 0;
    }

    private static ModelInstance insetModel(Identifier texture, int mask) {
        return () -> {
            JsonObject root = new JsonObject();
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", texture.toString());
            textures.addProperty("all", texture.toString());
            root.add("textures", textures);
            JsonObject element = new JsonObject();
            element.add("from",
                    insetCoords(insetExposed(mask, Direction.WEST) ? INSET_DEPTH : 0, insetExposed(mask, Direction.DOWN) ? INSET_DEPTH : 0, insetExposed(mask, Direction.NORTH) ? INSET_DEPTH : 0));
            element.add("to", insetCoords(insetExposed(mask, Direction.EAST) ? 16 - INSET_DEPTH : 16, insetExposed(mask, Direction.UP) ? 16 - INSET_DEPTH : 16,
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
            if (mask == INSET_ALL_EXPOSED) {
                root.add("display", insetItemDisplay());
            }
            return root;
        };
    }

    private static JsonObject insetItemDisplay() {
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(30, -135, 0, 0, 0, 0, INSET_GUI_SCALE));
        display.add("thirdperson_righthand", displayTransform(75, 45, 0, 0, 3, 0, INSET_HELD_SCALE));
        display.add("thirdperson_lefthand", displayTransform(75, 45, 0, 0, 3, 0, INSET_HELD_SCALE));
        display.add("firstperson_righthand", displayTransform(0, 45, 0, 0, 0, 0, INSET_HELD_SCALE));
        display.add("firstperson_lefthand", displayTransform(0, -45, 0, 0, 0, 0, INSET_HELD_SCALE));
        display.add("ground", displayTransform(0, 0, 0, 0, 2.5F, 0, INSET_GROUND_SCALE));
        return display;
    }

    private static JsonObject displayTransform(float rotX, float rotY, float rotZ, float moveX, float moveY, float moveZ, float scale) {
        JsonObject transform = new JsonObject();
        transform.add("rotation", floatTriple(rotX, rotY, rotZ));
        transform.add("translation", floatTriple(moveX, moveY, moveZ));
        transform.add("scale", floatTriple(scale, scale, scale));
        return transform;
    }

    private static JsonArray floatTriple(float x, float y, float z) {
        JsonArray values = new JsonArray();
        values.add(x);
        values.add(y);
        values.add(z);
        return values;
    }

    private static JsonArray insetCoords(int x, int y, int z) {
        JsonArray coords = new JsonArray();
        coords.add(x);
        coords.add(y);
        coords.add(z);
        return coords;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerResearchTable(blockModels, itemModels);
        registerDeconstructionTable(blockModels, itemModels);
        registerResearchNote(itemModels);
        registerConstructs(blockModels, itemModels);
        decorModels(blockModels);
        eldritchModels(blockModels);
        translucentCube(blockModels, TTBlocks.AMBER_BRICK.get());
        blockModels.createTrivialCube(TTBlocks.FLESH_BLOCK.get());
        blockModels.registerSimpleItemModel(TTBlocks.FLESH_BLOCK.get().asItem(), ModelLocationUtils.getModelLocation(TTBlocks.FLESH_BLOCK.get()));
        registerInvisibleBlock(blockModels, TTBlocks.EFFECT_SHOCK.get());
        registerInvisibleBlock(blockModels, TTBlocks.BARRIER.get());
        registerInvisibleBlock(blockModels, TTBlocks.NODE.get());
        registerJar(blockModels, itemModels, TTBlocks.JAR_NORMAL.get(), "jar_normal");
        registerJar(blockModels, itemModels, TTBlocks.JAR_VOID.get(), "jar_void");
        registerJarBrain(blockModels, itemModels);
        registerAuraDevices(blockModels, itemModels);
        registerNoiseDevices(blockModels, itemModels);
        TubeModels.register(blockModels);
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.CRUCIBLE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.CRUCIBLE.get()))));
        mirrorBlockState(blockModels, TTBlocks.MIRROR.get());
        mirrorBlockState(blockModels, TTBlocks.MIRROR_ESSENTIA.get());
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.LOOT_URN_COMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_URN_COMMON.get()))));
        itemModels.itemModelOutput.accept(TTItems.LOOT_URN_COMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_URN_COMMON.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TTBlocks.LOOT_URN_UNCOMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_URN_UNCOMMON.get()))));
        itemModels.itemModelOutput.accept(TTItems.LOOT_URN_UNCOMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_URN_UNCOMMON.get())));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.LOOT_URN_RARE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_URN_RARE.get()))));
        itemModels.itemModelOutput.accept(TTItems.LOOT_URN_RARE.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_URN_RARE.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TTBlocks.LOOT_CRATE_COMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_CRATE_COMMON.get()))));
        itemModels.itemModelOutput.accept(TTItems.LOOT_CRATE_COMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_CRATE_COMMON.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TTBlocks.LOOT_CRATE_UNCOMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_CRATE_UNCOMMON.get()))));
        itemModels.itemModelOutput.accept(TTItems.LOOT_CRATE_UNCOMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_CRATE_UNCOMMON.get())));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.LOOT_CRATE_RARE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_CRATE_RARE.get()))));
        itemModels.itemModelOutput.accept(TTItems.LOOT_CRATE_RARE.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TTBlocks.LOOT_CRATE_RARE.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TTBlocks.ARCANE_WORKBENCH.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.ARCANE_WORKBENCH.get()))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.ARCANE_WORKBENCH_CHARGER.get(),
                BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.ARCANE_WORKBENCH_CHARGER.get()))));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.VIS_RELAY.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.VIS_RELAY.get()))));
        itemModels.itemModelOutput.accept(TTItems.VIS_RELAY.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TTBlocks.VIS_RELAY.get())));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.NODE_STABILIZER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.NODE_STABILIZER.get()))));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.NODE_TRANSDUCER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.NODE_TRANSDUCER.get()))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.NODE_STABILIZER_ADVANCED.get(),
                BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.NODE_STABILIZER_ADVANCED.get()))));
        itemModels.itemModelOutput.accept(TTBlocks.NODE_TRANSDUCER.get().asItem(),
                new SpecialModelWrapper.Unbaked(TTIds.rl("item/node_stabilizer_base"), Optional.empty(), new NodeStabilizerItemSpecialRenderer.Unbaked(false, true)));
        itemModels.itemModelOutput.accept(TTBlocks.NODE_STABILIZER.get().asItem(),
                new SpecialModelWrapper.Unbaked(TTIds.rl("item/node_stabilizer_base"), Optional.empty(), new NodeStabilizerItemSpecialRenderer.Unbaked(false)));
        itemModels.itemModelOutput.accept(TTBlocks.NODE_STABILIZER_ADVANCED.get().asItem(),
                new SpecialModelWrapper.Unbaked(TTIds.rl("item/node_stabilizer_base"), Optional.empty(), new NodeStabilizerItemSpecialRenderer.Unbaked(true)));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.JAR_NODE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.JAR_NORMAL.get()))));
        itemModels.itemModelOutput.accept(TTBlocks.JAR_NODE.get().asItem(),
                new CompositeModel.Unbaked(List.of(new CuboidItemModelWrapper.Unbaked(TTIds.rl("block/jar_normal"), Optional.empty(), List.of()),
                        new SpecialModelWrapper.Unbaked(TTIds.rl("block/jar_normal"), Optional.empty(), new JarNodeItemSpecialRenderer.Unbaked())), Optional.empty()));
        horizontalBlock(blockModels, itemModels, TTBlocks.INFERNAL_FURNACE.get(), "infernal_furnace", true);
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.NETHER_BRICKS_PLACEHOLDER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(Blocks.NETHER_BRICKS))));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TTBlocks.OBSIDIAN_PLACEHOLDER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(Blocks.OBSIDIAN))));
        registerAlembic(blockModels, itemModels, TTBlocks.ALEMBIC.get());
        registerBellows(blockModels, itemModels);
        registerSmelter(blockModels, itemModels, TTBlocks.SMELTER_BASIC.get(), "smelter_basic");
        registerSmelter(blockModels, itemModels, TTBlocks.SMELTER_THAUMIUM.get(), "smelter_thaumium");
        registerSmelter(blockModels, itemModels, TTBlocks.SMELTER_VOID.get(), "smelter_void");
        registerAdvancedAlchemicalFurnace(blockModels, itemModels);
        registerEssentiaCrystalizer(blockModels, itemModels);
        registerEssentiaReservoir(blockModels, itemModels);
        registerFluxScrubber(blockModels, itemModels);
        registerFluxGas(blockModels);
        horizontalBlock(blockModels, itemModels, TTBlocks.SMELTER_AUX.get(), "smelter_aux");
        horizontalBlock(blockModels, itemModels, TTBlocks.SMELTER_VENT.get(), "smelter_vent");
        itemModels.generateFlatItem(TTItems.THAUMONOMICON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMONOMICON_CHEAT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMONOMICON_SHARING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMONOMICON_LINKING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CREATIVE_NODE_PLACER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(TTItems.WAND.get(),
                ItemModelUtils.conditional(new WandIsStaffProperty(), new SpecialModelWrapper.Unbaked(TTIds.rl("item/wand_staff_base"), Optional.empty(), new WandItemSpecialRenderer.Unbaked()),
                        new SpecialModelWrapper.Unbaked(TTIds.rl("item/wand_base"), Optional.empty(), new WandItemSpecialRenderer.Unbaked())));
        itemModels.generateFlatItem(TTItems.WAND_CAP_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_COPPER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_GOLD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_SILVER_INERT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_THAUMIUM_INERT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_VOID_INERT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_CAP_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_GREATWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_OBSIDIAN.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_BLAZE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_ICE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_QUARTZ.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_BONE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_REED.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.WAND_ROD_SILVERWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_GREATWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_OBSIDIAN.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_BLAZE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_ICE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_QUARTZ.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_BONE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_REED.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_SILVERWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.STAFF_ROD_PRIMAL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.PRIMAL_CHARM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.FABRIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MIRRORED_GLASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MECHANISM_SIMPLE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MECHANISM_COMPLEX.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MORPHIC_RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.BATH_SALTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SANITY_SOAP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CHUNK_BEEF.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CHUNK_CHICKEN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CHUNK_PORK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CHUNK_FISH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CHUNK_RABBIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CHUNK_MUTTON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TRIPLE_MEAT_TREAT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(TTItems.THAUMOMETER.get(), ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/thaumometer")));
        itemModels.generateFlatItem(TTItems.JAR_BRACE.get(), ModelTemplates.FLAT_ITEM);
        Identifier labelModelId = itemModels.createFlatItemModel(TTItems.LABEL.get(), ModelTemplates.FLAT_ITEM);
        Identifier labelOverlayModelId = itemModels.createFlatItemModel(TTItems.LABEL.get(), "_overlay", ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(TTItems.LABEL.get(),
                new CompositeModel.Unbaked(List.of(ItemModelUtils.plainModel(labelModelId), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TTDataComponents.ASPECT_FILTER.get()),
                        ItemModelUtils.tintedModel(labelOverlayModelId, new AspectFilterTint(0xffffff)), ItemModelUtils.plainModel(labelModelId))), Optional.empty()));
        itemModels.generateFlatItem(TTItems.TAINTED_GOO.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TAINT_TENDRIL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.BOTTLE_TAINT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.VIS_RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIC_SLIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TAINT_CRAWLER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TAINTACLE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TAINT_SWARM_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TAINT_SEED_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TAINT_SEED_PRIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.WISP_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.BRAINY_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.BRAINY_DROWNED_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.BRAINY_HUSK_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.BRAIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.FIREBAT_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.MIND_SPIDER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.PECH_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ELDRITCH_CRAB_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.INHABITED_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ELDRITCH_GUARDIAN_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CULTIST_KNIGHT_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CULTIST_CLERIC_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CULTIST_PORTAL_LESSER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CULTIST_LEADER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CULTIST_PORTAL_GREATER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ELDRITCH_WARDEN_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ELDRITCH_GOLEM_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TAINTACLE_GIANT_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.LOOT_BAG_COMMON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.LOOT_BAG_UNCOMMON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.LOOT_BAG_RARE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.PECH_WAND.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_BLADE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_PLATE_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_PLATE_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_PLATE_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_ROBE_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_ROBE_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_ROBE_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TUBE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TUBE_VALVE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TUBE_RESTRICT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TUBE_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TUBE_ONEWAY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.TUBE_BUFFER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GOGGLES_REVEALING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SCRIBING_TOOLS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.ALUMENTUM.get(), ModelTemplates.FLAT_ITEM);
        registerCelestialNotes(itemModels);
        registerBaubleItems(itemModels);

        // Nitor Models
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/nitor");
        Material baseTex = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/nitor"));
        Material coreTex = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/nitor_core"));
        TextureMapping textures = TextureMapping.layered(baseTex, coreTex);
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModelId, textures, itemModels.modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            registerNitor(blockModels, itemModels, dye);
        }

        // Resources
        blockModels.createTrivialCube(TTBlocks.ORE_AMBER.get());
        blockModels.createTrivialCube(TTBlocks.ORE_CINNABAR.get());
        blockModels.createTrivialCube(TTBlocks.ORE_QUARTZ.get());
        deepslateOre(blockModels, TTBlocks.DEEPSLATE_ORE_AMBER.get(), "ore_amber_overlay");
        deepslateOre(blockModels, TTBlocks.DEEPSLATE_ORE_CINNABAR.get(), "ore_cinnabar_overlay");
        deepslateOre(blockModels, TTBlocks.DEEPSLATE_ORE_QUARTZ.get(), "ore_quartz_overlay");

        blockModels.createTrivialCube(TTBlocks.ALCHEMICAL_CONSTRUCT.get());
        blockModels.createTrivialCube(TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());

        blockModels.createTrivialCube(TTBlocks.METAL_BRASS_BLOCK.get());
        blockModels.createTrivialCube(TTBlocks.METAL_THAUMIUM_BLOCK.get());
        blockModels.createTrivialCube(TTBlocks.METAL_VOID_BLOCK.get());
        translucentCube(blockModels, TTBlocks.AMBER_BLOCK.get());

        itemModels.generateFlatItem(TTItems.INGOT_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.INGOT_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.INGOT_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.AMBER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.QUICKSILVER.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TTItems.RARE_EARTH.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TTItems.NUGGET_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.NUGGET_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.NUGGET_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.NUGGET_QUICKSILVER.get(), ModelTemplates.FLAT_ITEM);
        registerInfusionAltar(blockModels, itemModels);
        registerSimpleWithItem(blockModels, itemModels, TTBlocks.FOCAL_MANIPULATOR.get(), "focal_manipulator");
        registerInvisibleBlock(blockModels, TTBlocks.HOLE.get());
        registerInvisibleBlock(blockModels, TTBlocks.EFFECT_SAP.get());
        registerInvisibleBlock(blockModels, TTBlocks.EFFECT_GLIMMER.get());

        registerCandles(blockModels, itemModels);
        registerBanners(blockModels, itemModels);
        itemModels.generateFlatItem(TTItems.TALLOW.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        registerSpear(itemModels, TTItems.THAUMIUM_SPEAR.get());
        itemModels.generateFlatItem(TTItems.VOID_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        registerSpear(itemModels, TTItems.VOID_SPEAR.get());
        itemModels.generateFlatItem(TTItems.ELEMENTAL_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.ELEMENTAL_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.ELEMENTAL_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.ELEMENTAL_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.ELEMENTAL_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        registerSpear(itemModels, TTItems.ELEMENTAL_SPEAR.get());
        itemModels.generateFlatItem(TTItems.PRIMAL_CRUSHER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TTItems.TRAVELLER_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMOSTATIC_HARNESS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.THAUMIUM_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.VOID_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        registerRobeItem(itemModels, TTItems.CLOTH_CHEST.get(), "cloth_chest");
        registerRobeItem(itemModels, TTItems.CLOTH_LEGS.get(), "cloth_legs");
        registerRobeItem(itemModels, TTItems.CLOTH_BOOTS.get(), "cloth_boots");
        registerSpa(blockModels, itemModels);
        registerCasters(itemModels);
        registerGolemancy(blockModels, itemModels);

        itemModels.generateFlatItem(TTItems.NUGGET_QUARTZ.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TTItems.VOID_SEED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CAUSALITY_COLLAPSER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_GOLD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_COPPER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_LEAD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_TIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.RAW_CINNABAR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_CINNABAR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLUSTER_QUARTZ.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TTItems.PLATE_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.PLATE_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.PLATE_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.PLATE_VOID.get(), ModelTemplates.FLAT_ITEM);

        CrystalBlockstateGenerator.register(blockModels);
        CrystalItemModelGenerator.register(itemModels);
        WardedGlassModelGenerator.register(blockModels, itemModels);
        EssentiaCrystalModelGenerator.register(itemModels);
        registerManaPod(blockModels, itemModels);
        stoneAndStairModels(blockModels);
        treeModels(blockModels, itemModels);
        plantModels(blockModels, itemModels);
        taintModels(blockModels, itemModels);
        containerItemModels(itemModels);
    }

    private void eldritchLock(BlockModelGenerators blockModels) {
        Identifier model = ModelTemplates.CUBE_ORIENTABLE.create(TTBlocks.ELDRITCH_LOCK.get(),
                new TextureMapping().put(TextureSlot.FRONT, texture("eldritch_lock_face")).put(TextureSlot.SIDE, texture("eldritch_lock_side")).put(TextureSlot.TOP, texture("eldritch_lock_side")),
                blockModels.modelOutput);
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, BlockModelGenerators.X_ROT_90)
                .select(Direction.UP, BlockModelGenerators.X_ROT_270).select(Direction.NORTH, BlockModelGenerators.NOP).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270).select(Direction.EAST, BlockModelGenerators.Y_ROT_90);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.ELDRITCH_LOCK.get(), BlockModelGenerators.plainVariant(model)).with(rotations));
        blockModels.registerSimpleItemModel(TTBlocks.ELDRITCH_LOCK.get().asItem(), model);
    }

    private void horizontalBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        horizontalBlock(blockModels, itemModels, block, modelName, false);
    }

    private void horizontalBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName, boolean oversizedInGui) {
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variantOf(modelName)).with(rotations));
        itemModels.itemModelOutput.accept(block.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName), Optional.empty(), List.of()),
                new ClientItem.Properties(true, oversizedInGui, 1));
    }

    private void translucentCube(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block).forceAllTranslucent(), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private void registerBellows(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, BlockModelGenerators.X_ROT_90)
                .select(Direction.UP, BlockModelGenerators.X_ROT_270).select(Direction.NORTH, BlockModelGenerators.NOP).select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.BELLOWS.get(), variantOf("bellows")).with(rotations));
        itemModels.itemModelOutput.accept(TTBlocks.BELLOWS.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/bellows"), Optional.empty(), List.of()));
    }

    private void registerBanners(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/tc_banner");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        Material stand = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/banner_stand"));
        Material cloth = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/banner_cloth"));
        Material symbol = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/banner_symbol"));
        Identifier dyedItemModel = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/banner_dyed");
        Identifier cultistItemModel = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/banner_cultist");
        THREE_LAYERED_ITEM.create(dyedItemModel, TextureMapping.layered(stand, cloth, symbol), itemModels.modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(cultistItemModel, TextureMapping.layered(stand, new Material(cultistItemModel)), itemModels.modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.BANNERS.get(dye).get(), variant));
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.WALL_BANNERS.get(dye).get(), variant));
            int tint = 0xFF000000 | dye.getMapColor().col;
            itemModels.itemModelOutput.accept(TTItems.BANNERS.get(dye).get(),
                    ItemModelUtils.tintedModel(dyedItemModel, new Constant(0xFFFFFFFF), new Constant(tint), new AspectFilterTint(dye.getMapColor().col)));
        }
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.BANNER_CRIMSON_CULT.get(), variant));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.WALL_BANNER_CRIMSON_CULT.get(), variant));
        itemModels.itemModelOutput.accept(TTItems.BANNER_CRIMSON_CULT.get(), ItemModelUtils.plainModel(cultistItemModel));
    }

    private void registerCandles(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/candle");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        for (DyeColor dye : DyeColor.values()) {
            Block candle = TTBlocks.CANDLES.get(dye).get();
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(candle, variant));
            int tint = 0xFF000000 | dye.getMapColor().col;
            itemModels.itemModelOutput.accept(candle.asItem(), ItemModelUtils.tintedModel(model, new Constant(tint)));
        }
        for (CandleHolderMaterial material : CandleHolderMaterial.values()) {
            String name = "block/candle_holder_" + material.getSerializedName();
            Identifier emptyHolder = Identifier.fromNamespaceAndPath(TTIds.MODID, name);
            MultiVariant empty = new MultiVariant(WeightedList.of(new Variant(emptyHolder)));
            MultiVariant filled = new MultiVariant(WeightedList.of(new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, name + "_filled"))));
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.CANDLE_HOLDERS.get(material).get())
                    .with(PropertyDispatch.initial(BlockCandleHolder.CANDLE).generate(held -> held.isPresent() ? filled : empty)));
            itemModels.itemModelOutput.accept(TTItems.CANDLE_HOLDERS.get(material).get(), ItemModelUtils.plainModel(emptyHolder));
        }
    }

    private void registerBaubleItems(ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TTItems.AMULET_MUNDANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.RING_MUNDANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GIRDLE_MUNDANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.RING_APPRENTICE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.AMULET_FANCY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.RING_FANCY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.GIRDLE_FANCY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.AMULET_VIS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.AMULET_VIS_CRAFTED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CHARM_UNDYING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CLOUD_RING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIOSITY_BAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.VOIDSEER_CHARM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.FOCUS_POUCH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.SANITY_CHECKER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIO_ARCANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIO_PRESERVED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIO_ANCIENT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIO_ELDRITCH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIO_KNOWLEDGE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIO_TWISTED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CURIO_RITES.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CREATIVE_FLUX_SPONGE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.HAND_MIRROR.get(), ModelTemplates.FLAT_ITEM);
        registerMirrorItem(itemModels, TTItems.MIRROR.get(), "mirrorframe");
        registerMirrorItem(itemModels, TTItems.MIRROR_ESSENTIA.get(), "mirrorframe2");
        itemModels.generateFlatItem(TTItems.CRIMSON_PRAETOR_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_PRAETOR_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.CRIMSON_PRAETOR_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.FORTRESS_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.FORTRESS_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TTItems.FORTRESS_LEGS.get(), ModelTemplates.FLAT_ITEM);
        registerVerdantCharm(itemModels);
        registerVoidRobeItems(itemModels);
    }

    private void registerVerdantCharm(ItemModelGenerators itemModels) {
        Material base = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/verdant_charm"));
        List<SelectItemModel.SwitchCase<Integer>> cases = new ArrayList<>();
        Identifier fallback = null;
        for (int type = 0; type <= 2; type++) {
            Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/verdant_charm_" + type);
            Material overlay = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/verdant_charm_over_" + type));
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(base, overlay), itemModels.modelOutput);
            cases.add(ItemModelUtils.when(type, ItemModelUtils.plainModel(model)));
            if (type == 0) {
                fallback = model;
            }
        }
        itemModels.itemModelOutput.accept(TTItems.VERDANT_CHARM.get(), ItemModelUtils.select(new ComponentContents<>(TTDataComponents.VERDANT_TYPE.get()), ItemModelUtils.plainModel(fallback), cases));
    }

    private void registerVoidRobeItems(ItemModelGenerators itemModels) {
        Identifier helmModel = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(TTItems.VOID_ROBE_HELM.get()), TextureMapping.layer0(TTItems.VOID_ROBE_HELM.get()),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TTItems.VOID_ROBE_HELM.get(), ItemModelUtils.tintedModel(helmModel, new Dye(ROBES_UNDYED_ARGB)));
        registerVoidRobePiece(itemModels, TTItems.VOID_ROBE_CHEST.get(), "void_robe_chest");
        registerVoidRobePiece(itemModels, TTItems.VOID_ROBE_LEGS.get(), "void_robe_legs");
    }

    private void registerCelestialNotes(ItemModelGenerators itemModels) {
        Material sheet = new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/celestial_notes_sheet"));
        List<SelectItemModel.SwitchCase<CelestialBody>> cases = new ArrayList<>();
        for (CelestialBody body : CelestialBody.values()) {
            Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/celestial_notes_" + body.getSerializedName());
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(sheet, new Material(model)), itemModels.modelOutput);
            cases.add(ItemModelUtils.when(body, ItemModelUtils.plainModel(model)));
        }
        Identifier fallback = Identifier.fromNamespaceAndPath(TTIds.MODID, "item/celestial_notes_sun");
        itemModels.itemModelOutput.accept(TTItems.CELESTIAL_NOTES.get(),
                ItemModelUtils.select(new ComponentContents<>(TTDataComponents.CELESTIAL_BODY.get()), ItemModelUtils.plainModel(fallback), cases));
    }

    private void registerJar(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(TTIds.rl("block/" + modelName))));
        itemModels.itemModelOutput.accept(block.asItem(),
                new CompositeModel.Unbaked(
                        List.of(new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName), Optional.empty(), List.of()),
                                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName), Optional.empty(), new JarItemSpecialRenderer.Unbaked())),
                        Optional.empty()));
    }

    private void registerAlembic(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block) {
        Identifier pane = TTIds.rl("block/alembic_pane");
        Identifier port = TTIds.rl("block/alembic_port");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(variantOf("alembic"));
        List<ItemModel.Unbaked> itemLayers = new ArrayList<>();
        itemLayers.add(new CuboidItemModelWrapper.Unbaked(TTIds.rl("block/alembic"), Optional.empty(), List.of()));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BooleanProperty property = BlockEssentiaTransport.propertyFor(direction);
            VariantMutator rotation = northYRotation(direction);
            generator = generator.with(new ConditionBuilder().term(property, false), new MultiVariant(WeightedList.of(rotation.apply(new Variant(pane)))));
            generator = generator.with(new ConditionBuilder().term(property, true), new MultiVariant(WeightedList.of(rotation.apply(new Variant(port)))));
            itemLayers.add(new CuboidItemModelWrapper.Unbaked(pane, Optional.of(northYTransformation(direction)), List.of()));
        }
        blockModels.blockStateOutput.accept(generator);
        itemModels.itemModelOutput.accept(block.asItem(), new CompositeModel.Unbaked(itemLayers, Optional.empty()));
    }

    private static VariantMutator northYRotation(Direction direction) {
        return switch (direction) {
            case EAST -> BlockModelGenerators.Y_ROT_90;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        };
    }

    private static Transformation northYTransformation(Direction direction) {
        int quarterTurns = Math.floorMod(direction.get2DDataValue() + HALF_TURN_QUARTERS, FULL_TURN_QUARTERS);
        Matrix4f matrix = new Matrix4f().translate(BLOCK_CENTER, 0.0F, BLOCK_CENTER).rotate(Axis.YP.rotationDegrees(-QUARTER_TURN_DEGREES * quarterTurns)).translate(-BLOCK_CENTER, 0.0F,
                -BLOCK_CENTER);
        return new Transformation(matrix);
    }

    private void registerSmelter(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        MultiVariant off = variantOf(modelName + "_off");
        MultiVariant on = variantOf(modelName + "_on");
        PropertyDispatch<MultiVariant> lit = PropertyDispatch.initial(BlockSmelter.LIT).select(false, off).select(true, on);
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(lit).with(rotations));

        itemModels.itemModelOutput.accept(block.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName + "_off"), Optional.empty(), List.of()));
    }

    private static void registerFluxGas(BlockModelGenerators blockModels) {
        Block gas = TTBlocks.FLUX_GAS.get();
        Identifier model = ModelTemplates.CUBE_ALL.create(gas, TextureMapping.cube(gas).forceAllTranslucent(), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(gas, BlockModelGenerators.plainVariant(model)));
    }

    private static void registerEssentiaCrystalizer(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block block = TTBlocks.ESSENTIA_CRYSTALIZER.get();
        Identifier model = TTIds.rl("block/essentia_crystalizer");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)).with(downBasedFacing()));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
    }

    private static void registerEssentiaReservoir(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block block = TTBlocks.ESSENTIA_RESERVOIR.get();
        Identifier frame = TTIds.rl("block/essentia_reservoir_frame");
        Identifier port = TTIds.rl("block/essentia_reservoir_port");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(BlockModelGenerators.plainVariant(frame));
        for (Direction direction : Direction.values()) {
            generator = generator.with(new ConditionBuilder().term(BlockStateProperties.FACING, direction), new MultiVariant(WeightedList.of(applyRotation(new Variant(port), direction))));
        }
        blockModels.blockStateOutput.accept(generator);
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.composite(ItemModelUtils.plainModel(frame), ItemModelUtils.plainModel(port)));
    }

    private static void registerFluxScrubber(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block block = TTBlocks.FLUX_SCRUBBER.get();
        Identifier mesh = TTIds.rl("models/mesh/flux_scrubber.ttmesh");
        TextureMapping textures = new TextureMapping().put(LEGACY_MESH_SLOT, blockTexture("flux_scrubber")).put(TextureSlot.PARTICLE, blockTexture("al_furnace_side"));
        Identifier model = legacyMeshBuilder(mesh, null, root -> root.translation(0.0F, 0.0F, -0.5F)).partVisibility("Tip", false).build().create(block, textures, blockModels.modelOutput);
        Identifier itemModel = legacyMeshTemplate(mesh, "_item", root -> root.translation(0.0F, 0.0F, -0.5F)).create(block, textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)).with(northBasedFacing()));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(itemModel));
    }

    private static ModelTemplate legacyMeshTemplate(Identifier mesh, @Nullable String suffix, Consumer<RootTransformsBuilder> rootTransform) {
        return legacyMeshBuilder(mesh, suffix, rootTransform).build();
    }

    private static ExtendedModelTemplateBuilder legacyMeshBuilder(Identifier mesh, @Nullable String suffix, Consumer<RootTransformsBuilder> rootTransform) {
        ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block")).requiredTextureSlot(LEGACY_MESH_SLOT)
                .requiredTextureSlot(TextureSlot.PARTICLE).customLoader(TTMeshLoaderBuilder::new, loader -> loader.mesh(mesh).flipV(true)).rootTransforms(rootTransform);
        if (suffix != null) {
            builder.suffix(suffix);
        }
        return builder;
    }

    private static PropertyDispatch<VariantMutator> northBasedFacing() {
        return PropertyDispatch.modify(BlockStateProperties.FACING).generate(TTModelProvider::legacyFacing);
    }

    private static PropertyDispatch<VariantMutator> downBasedFacing() {
        return PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, BlockModelGenerators.NOP).select(Direction.UP, BlockModelGenerators.X_ROT_180)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90).select(Direction.NORTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
    }

    private static VariantMutator legacyFacing(Direction direction) {
        return switch (direction) {
            case NORTH -> BlockModelGenerators.NOP;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            case EAST -> BlockModelGenerators.Y_ROT_90;
            case UP -> BlockModelGenerators.X_ROT_270;
            case DOWN -> BlockModelGenerators.X_ROT_90;
        };
    }

    private static void registerAdvancedAlchemicalFurnace(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerInvisibleBlock(blockModels, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get());
        registerInvisibleBlock(blockModels, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get());
        registerInvisibleBlock(blockModels, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get());
        registerInvisibleBlock(blockModels, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get());
        registerInvisibleBlock(blockModels, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get());
        Identifier base = BLOCK_PARTICLE.create(TTIds.rl("item/advanced_alchemical_furnace_base"), TextureMapping.particle(blockTexture("advanced_alchemical_furnace")), blockModels.modelOutput);
        itemModels.itemModelOutput.accept(TTItems.ADVANCED_ALCHEMICAL_FURNACE.get(),
                new SpecialModelWrapper.Unbaked(base, Optional.empty(), new AdvancedAlchemicalFurnaceItemSpecialRenderer.Unbaked()));
    }

    private static Material blockTexture(String name) {
        return new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + name));
    }

    private MultiVariant variantOf(String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName);
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }

    private void registerDeconstructionTable(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerInvisibleBlock(blockModels, TTBlocks.DECONSTRUCTION_TABLE.get());
        itemModels.itemModelOutput.accept(TTBlocks.DECONSTRUCTION_TABLE.get().asItem(),
                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/deconstruction_table_base"), Optional.empty(), new DeconTableItemSpecialRenderer.Unbaked()));
    }

    private void registerResearchNote(ItemModelGenerators itemModels) {
        Identifier base = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(TTItems.RESEARCH_NOTE.get()), TextureMapping
                .layered(new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/research_note")), new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/research_note_overlay"))),
                itemModels.modelOutput);
        Identifier complete = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(TTItems.RESEARCH_NOTE.get(), "_complete"),
                TextureMapping.layered(new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/research_note_complete")),
                        new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "item/research_note_complete_overlay"))),
                itemModels.modelOutput);
        ItemModel.Unbaked baseModel = ItemModelUtils.tintedModel(base, new Constant(0xFFFFFF), new NoteColorTint(0x999999));
        ItemModel.Unbaked completeModel = ItemModelUtils.tintedModel(complete, new Constant(0xFFFFFF), new NoteColorTint(0x999999));
        itemModels.itemModelOutput.accept(TTItems.RESEARCH_NOTE.get(), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TTDataComponents.NOTE_COMPLETE.get()), completeModel, baseModel));
    }

    private void registerResearchTable(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block block = TTBlocks.RESEARCH_TABLE.get();
        Identifier main = TTIds.rl("block/research_table_main");
        Identifier ext = TTIds.rl("block/research_table_ext");
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(BlockResearchTable.PART).select(ResearchTablePart.MAIN, BlockModelGenerators.plainVariant(main))
                        .select(ResearchTablePart.EXT, BlockModelGenerators.plainVariant(ext))).with(PropertyDispatch.modify(BlockResearchTable.FACING).generate(TTModelProvider::northYRotation)));
        Matrix4f mainItem = new Matrix4f().translate(BLOCK_CENTER, BLOCK_CENTER, BLOCK_CENTER).scale(RESEARCH_TABLE_ITEM_SCALE).translate(-BLOCK_CENTER, -BLOCK_CENTER, 0.0F);
        Matrix4f extItem = new Matrix4f(mainItem).translate(0.0F, 0.0F, -1.0F).mul(northYTransformation(Direction.SOUTH).getMatrixCopy());
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.composite(new CuboidItemModelWrapper.Unbaked(main, Optional.of(new Transformation(mainItem)), List.of()),
                new CuboidItemModelWrapper.Unbaked(ext, Optional.of(new Transformation(extItem)), List.of())));
    }

    private void registerJarBrain(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.JAR_BRAIN.get(), BlockModelGenerators.plainVariant(TTIds.rl("block/jar_normal"))));
        itemModels.itemModelOutput.accept(TTBlocks.JAR_BRAIN.get().asItem(),
                new CompositeModel.Unbaked(
                        List.of(new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/jar_normal"), Optional.empty(), List.of()),
                                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/jar_normal"), Optional.empty(), new JarBrainItemSpecialRenderer.Unbaked())),
                        Optional.empty()));
    }

    private void registerNoiseDevices(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        PropertyDispatch<VariantMutator> wallMount = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        registerEnabledFacingDevice(blockModels, itemModels, TTBlocks.ARCANE_EAR.get(), "arcane_ear_on", "arcane_ear_off", wallMount);
        registerEnabledFacingDevice(blockModels, itemModels, TTBlocks.ARCANE_EAR_TOGGLE.get(), "arcane_ear_toggle_on", "arcane_ear_toggle_off", wallMount);

        PropertyDispatch<VariantMutator> hangMount = downBasedFacing();
        registerEnabledFacingDevice(blockModels, itemModels, TTBlocks.LAMP_ARCANE.get(), "lamp_arcane_on", "lamp_arcane_off", hangMount);
        registerEnabledFacingDevice(blockModels, itemModels, TTBlocks.LAMP_GROWTH.get(), "lamp_growth_on", "lamp_growth_off", hangMount);
        registerEnabledFacingDevice(blockModels, itemModels, TTBlocks.LAMP_FERTILITY.get(), "lamp_fertility_on", "lamp_fertility_off", hangMount);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.EVERFULL_URN.get(), BlockModelGenerators.plainVariant(TTIds.rl("block/everfull_urn"))));
        itemModels.itemModelOutput.accept(TTItems.EVERFULL_URN.get(), ItemModelUtils.plainModel(TTIds.rl("block/everfull_urn")));

        PropertyDispatch<VariantMutator> deviceMount = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        registerEnabledFacingDevice(blockModels, itemModels, TTBlocks.VIS_GENERATOR.get(), "vis_generator", "vis_generator", deviceMount);
        registerFacingDevice(blockModels, itemModels, TTBlocks.ESSENTIA_INPUT.get(), "essentia_input", deviceMount);
        registerFacingDevice(blockModels, itemModels, TTBlocks.ESSENTIA_OUTPUT.get(), "essentia_output", deviceMount);

        registerCondenser(blockModels, itemModels);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.STABILIZER.get(), BlockModelGenerators.plainVariant(TTIds.rl("block/stabilizer"))));
        itemModels.itemModelOutput.accept(TTItems.STABILIZER.get(), ItemModelUtils.plainModel(TTIds.rl("block/stabilizer")));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TTBlocks.VOID_SIPHON.get(), BlockModelGenerators.plainVariant(TTIds.rl("block/void_siphon"))));
        itemModels.itemModelOutput.accept(TTItems.VOID_SIPHON.get(), ItemModelUtils.plainModel(TTIds.rl("block/void_siphon")));
        registerLattice(blockModels, itemModels, TTBlocks.CONDENSER_LATTICE.get(), "condenser_lattice_core");
        registerLattice(blockModels, itemModels, TTBlocks.CONDENSER_LATTICE_DIRTY.get(), "condenser_lattice_core_dirty");
        registerRelay(blockModels, itemModels);

        Identifier thaumatoriumModel = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/thaumatorium");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.THAUMATORIUM.get(), new MultiVariant(WeightedList.of(new Variant(thaumatoriumModel))))
                .with(PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).generate(TTModelProvider::northYRotation)));
        itemModels.itemModelOutput.accept(TTItems.THAUMATORIUM.get(), ItemModelUtils.plainModel(thaumatoriumModel), new ClientItem.Properties(true, true, 1));
        registerInvisibleBlock(blockModels, TTBlocks.THAUMATORIUM_TOP.get());
        registerFacingDevice(blockModels, itemModels, TTBlocks.BRAIN_BOX.get(), "brain_box",
                PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, BlockModelGenerators.NOP).select(Direction.UP, BlockModelGenerators.X_ROT_180)
                        .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90).select(Direction.NORTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270)));

        Identifier centrifugeHousing = TTIds.rl("block/centrifuge");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.CENTRIFUGE.get(), BlockModelGenerators.plainVariant(centrifugeHousing)));
        itemModels.itemModelOutput.accept(TTItems.CENTRIFUGE.get(),
                ItemModelUtils.composite(ItemModelUtils.plainModel(centrifugeHousing), ItemModelUtils.plainModel(TTIds.rl("block/centrifuge_spinner"))));

        registerInvisibleBlock(blockModels, TTBlocks.HUNGRY_CHEST.get());
        itemModels.itemModelOutput.accept(TTItems.HUNGRY_CHEST.get(),
                new SpecialModelWrapper.Unbaked(Identifier.withDefaultNamespace("item/chest"), Optional.empty(), new ChestSpecialRenderer.Unbaked(TTIds.rl("hungry"))));
    }

    private void registerCondenser(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier on = TTIds.rl("block/condenser");
        Material offTexture = new Material(TTIds.rl("block/condenser_off"));
        Identifier off = CONDENSER_RETEXTURED.create(TTIds.rl("block/condenser_off"), new TextureMapping().put(TextureSlot.SIDE, offTexture).put(TextureSlot.PARTICLE, offTexture),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.CONDENSER.get()).with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(on)))).select(false, new MultiVariant(WeightedList.of(new Variant(off))))));
        itemModels.itemModelOutput.accept(TTItems.CONDENSER.get(), ItemModelUtils.plainModel(on));
    }

    private void registerLattice(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String coreModel) {
        Identifier side = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/condenser_lattice_side");
        Identifier core = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + coreModel);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(new MultiVariant(WeightedList.of(new Variant(core))));
        record LatticeFace(BooleanProperty property, VariantMutator mutator) {
        }
        List<LatticeFace> faces = List.of(new LatticeFace(BlockStateProperties.DOWN, BlockModelGenerators.NOP), new LatticeFace(BlockStateProperties.UP, BlockModelGenerators.X_ROT_180),
                new LatticeFace(BlockStateProperties.SOUTH, BlockModelGenerators.X_ROT_90),
                new LatticeFace(BlockStateProperties.NORTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)),
                new LatticeFace(BlockStateProperties.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90)),
                new LatticeFace(BlockStateProperties.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270)));
        for (LatticeFace face : faces) {
            generator = generator.with(BlockModelGenerators.condition().term(face.property(), true), new MultiVariant(WeightedList.of(new Variant(side))).with(face.mutator()));
        }
        blockModels.blockStateOutput.accept(generator);
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(core));
    }

    private void registerRelay(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier on = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/redstone_relay_on");
        Identifier off = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/redstone_relay_off");
        PropertyDispatch<VariantMutator> facing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.SOUTH, BlockModelGenerators.NOP)
                .select(Direction.NORTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_90).select(Direction.EAST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.REDSTONE_RELAY.get()).with(PropertyDispatch.initial(BlockStateProperties.POWERED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(on)))).select(false, new MultiVariant(WeightedList.of(new Variant(off))))).with(facing));
        itemModels.itemModelOutput.accept(TTItems.REDSTONE_RELAY.get(), ItemModelUtils.plainModel(off));
    }

    private void registerFacingDevice(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName, PropertyDispatch<VariantMutator> facing) {
        Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model)))).with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
    }

    private void registerEnabledFacingDevice(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String onModel, String offModel, PropertyDispatch<VariantMutator> facing) {
        Identifier on = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + onModel);
        Identifier off = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + offModel);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(on)))).select(false, new MultiVariant(WeightedList.of(new Variant(off))))).with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(off));
    }

    private void registerAuraDevices(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(TTBlocks.MATRIX_SPEED.get());
        blockModels.createTrivialCube(TTBlocks.MATRIX_COST.get());

        Identifier[] batteryModels = new Identifier[5];
        for (int i = 0; i < 5; i++) {
            Identifier textureId = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/vis_battery_" + i);
            batteryModels[i] = ModelTemplates.CUBE_ALL.create(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/vis_battery_" + i), TextureMapping.cube(new Material(textureId)),
                    blockModels.modelOutput);
        }
        PropertyDispatch<MultiVariant> chargeDispatch = PropertyDispatch.initial(BlockVisBattery.CHARGE).generate(charge -> {
            int tier = charge == 0 ? 0 : charge >= 10 ? 4 : (charge + 2) / 3;
            return new MultiVariant(WeightedList.of(new Variant(batteryModels[tier])));
        });
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.VIS_BATTERY.get()).with(chargeDispatch));
        itemModels.itemModelOutput.accept(TTItems.VIS_BATTERY.get(), ItemModelUtils.plainModel(batteryModels[0]));

        Identifier dioptraOn = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/dioptra_on");
        Identifier dioptraOff = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/dioptra_off");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.DIOPTRA.get()).with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(dioptraOn)))).select(false, new MultiVariant(WeightedList.of(new Variant(dioptraOff))))));
        itemModels.itemModelOutput.accept(TTItems.DIOPTRA.get(), ItemModelUtils.plainModel(dioptraOn));
    }

    private void stoneAndStairModels(BlockModelGenerators blockModels) {
        simpleCube(blockModels, TTBlocks.STONE_ARCANE.get(), "stone_arcane");
        simpleCube(blockModels, TTBlocks.STONE_ARCANE_BRICK.get(), "stone_arcane_brick");
        simpleCube(blockModels, TTBlocks.STONE_ANCIENT.get(), "stone_ancient");
        simpleCube(blockModels, TTBlocks.STONE_ANCIENT_TILE.get(), "stone_ancient_tile");
        simpleCube(blockModels, TTBlocks.STONE_ANCIENT_ROCK.get(), "stone_ancient_rock");
        simpleCube(blockModels, TTBlocks.STONE_ANCIENT_GLYPHED.get(), "stone_ancient_glyphed");
        simpleCube(blockModels, TTBlocks.STONE_ANCIENT_DOORWAY.get(), "stone_ancient_doorway");
        simpleCube(blockModels, TTBlocks.STONE_ELDRITCH_TILE.get(), "stone_eldritch_tile");
        simpleCube(blockModels, TTBlocks.STONE_POROUS.get(), "stone_porous");

        stairsFromModels(blockModels, TTBlocks.STAIRS_ARCANE.get(), "arcane_stairs", "arcane_inner_stairs", "arcane_outer_stairs");
        stairsFromModels(blockModels, TTBlocks.STAIRS_ARCANE_BRICK.get(), "arcane_brick_stairs", "arcane_brick_inner_stairs", "arcane_brick_outer_stairs");
        stairsFromModels(blockModels, TTBlocks.STAIRS_ANCIENT.get(), "ancient_stairs", "ancient_inner_stairs", "ancient_outer_stairs");
    }

    private void simpleCube(BlockModelGenerators blockModels, Block block, String modelName) {
        MultiVariant variant = variantOf(modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private void stairsFromModels(BlockModelGenerators blockModels, Block block, String straightName, String innerName, String outerName) {
        MultiVariant straight = variantOf(straightName);
        MultiVariant inner = variantOf(innerName);
        MultiVariant outer = variantOf(outerName);
        PropertyDispatch<MultiVariant> dispatch = PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, straight)
                .select(Direction.WEST, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer)
                .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outer)
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, inner)
                .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, inner)
                .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.WEST, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.WEST, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.WEST, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.WEST, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.EAST, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.WEST, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                .select(Direction.NORTH, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private void treeModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        simpleCube(blockModels, TTBlocks.SAPLING_GREATWOOD.get(), "sapling_greatwood");
        simpleCube(blockModels, TTBlocks.SAPLING_SILVERWOOD.get(), "sapling_silverwood");
        flatItemFromBlock(itemModels, TTItems.SAPLING_GREATWOOD.get(), TTBlocks.SAPLING_GREATWOOD.get());
        flatItemFromBlock(itemModels, TTItems.SAPLING_SILVERWOOD.get(), TTBlocks.SAPLING_SILVERWOOD.get());
        simpleCube(blockModels, TTBlocks.PLANK_GREATWOOD.get(), "plank_greatwood");
        simpleCube(blockModels, TTBlocks.PLANK_SILVERWOOD.get(), "plank_silverwood");
        simpleCube(blockModels, TTBlocks.LEAVES_GREATWOOD.get(), "leaves_greatwood");
        simpleCube(blockModels, TTBlocks.LEAVES_SILVERWOOD.get(), "leaves_silverwood");
        itemModels.itemModelOutput.accept(TTBlocks.LEAVES_GREATWOOD.get().asItem(),
                ItemModelUtils.tintedModel(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/leaves_greatwood"), new Constant(FOLIAGE_DEFAULT_COLOR)));
        log(blockModels, TTBlocks.LOG_GREATWOOD.get(), TTBlocks.WOOD_GREATWOOD.get());
        log(blockModels, TTBlocks.LOG_SILVERWOOD.get(), TTBlocks.WOOD_SILVERWOOD.get());
        blockModels.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(TTBlocks.SILVERWOOD_NODE_LOG.get(),
                BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOG_SILVERWOOD.get())),
                BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TTBlocks.LOG_SILVERWOOD.get(), "_horizontal"))));
        log(blockModels, TTBlocks.STRIPPED_LOG_GREATWOOD.get(), TTBlocks.STRIPPED_WOOD_GREATWOOD.get());
        log(blockModels, TTBlocks.STRIPPED_LOG_SILVERWOOD.get(), TTBlocks.STRIPPED_WOOD_SILVERWOOD.get());
    }

    private void log(BlockModelGenerators blockModels, Block block, Block wood) {
        blockModels.woodProvider(block).logWithHorizontal(block).wood(wood);
    }

    private void plantModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        pottedPlant(blockModels, TTBlocks.POTTED_SAPLING_GREATWOOD.get(), TTBlocks.SAPLING_GREATWOOD.get());
        pottedPlant(blockModels, TTBlocks.POTTED_SAPLING_SILVERWOOD.get(), TTBlocks.SAPLING_SILVERWOOD.get());
        pottedPlant(blockModels, TTBlocks.POTTED_SHIMMERLEAF.get(), TTBlocks.PLANT_SHIMMERLEAF.get());
        pottedPlant(blockModels, TTBlocks.POTTED_CINDERPEARL.get(), TTBlocks.PLANT_CINDERPEARL.get());
        pottedPlant(blockModels, TTBlocks.POTTED_VISHROOM.get(), TTBlocks.PLANT_VISHROOM.get());

        cross(blockModels, TTBlocks.PLANT_SHIMMERLEAF.get());
        cross(blockModels, TTBlocks.ETHEREAL_BLOOM.get());
        cross(blockModels, TTBlocks.PLANT_CINDERPEARL.get());
        cross(blockModels, TTBlocks.PLANT_VISHROOM.get());

        flatItemFromBlock(itemModels, TTItems.PLANT_SHIMMERLEAF.get(), TTBlocks.PLANT_SHIMMERLEAF.get());
        flatItemFromBlock(itemModels, TTItems.ETHEREAL_BLOOM.get(), TTBlocks.ETHEREAL_BLOOM.get());
        flatItemFromBlock(itemModels, TTItems.PLANT_CINDERPEARL.get(), TTBlocks.PLANT_CINDERPEARL.get());
        flatItemFromBlock(itemModels, TTItems.PLANT_VISHROOM.get(), TTBlocks.PLANT_VISHROOM.get());

        Identifier grassModel = Identifier.withDefaultNamespace("block/grass_block");
        MultiVariant grassVariant = new MultiVariant(WeightedList.of(new Variant(grassModel)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.GRASS_AMBIENT.get(), grassVariant));
        itemModels.itemModelOutput.accept(TTItems.GRASS_AMBIENT.get(), ItemModelUtils.tintedModel(grassModel, new GrassColorSource(0.5F, 1.0F)));
    }

    private void flatItemFromBlock(ItemModelGenerators itemModels, Item item, Block block) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(TextureMapping.getBlockTexture(block)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private void registerManaPod(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block pod = TTBlocks.MANA_POD.get();
        MultiVariant[] stems = new MultiVariant[3];
        for (int i = 0; i < 3; i++) {
            Identifier model = ModelTemplates.CROSS.createWithSuffix(pod, "_stage" + i, TextureMapping.cross(TextureMapping.getBlockTexture(pod, "_stem_" + i)), blockModels.modelOutput);
            stems[i] = new MultiVariant(WeightedList.of(new Variant(model)));
        }
        PropertyDispatch<MultiVariant> ages = PropertyDispatch.initial(BlockManaPod.AGE).select(0, stems[0]).select(1, stems[1]).select(2, stems[2]).select(3, stems[2]).select(4, stems[2])
                .select(5, stems[2]).select(6, stems[2]).select(7, stems[2]);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(pod).with(ages));

        Identifier beanModel = ModelLocationUtils.getModelLocation(TTItems.MANA_BEAN.get());
        ModelTemplates.FLAT_ITEM.create(beanModel, TextureMapping.layer0(new Material(TTIds.rl("item/mana_bean"))), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TTItems.MANA_BEAN.get(), ItemModelUtils.tintedModel(beanModel, new CrystalAspectTint(0xFFFFFF)));
    }

    private void pottedPlant(BlockModelGenerators blockModels, Block pot, Block plant) {
        Identifier model = ModelTemplates.FLOWER_POT_CROSS.create(pot, TextureMapping.plant(plant), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(pot, BlockModelGenerators.plainVariant(model)));
    }

    private void cross(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.CROSS.create(block, TextureMapping.cross(block), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private void taintModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.TAINT_ROCK.get(), rotatedWeighted(new String[]{"taint_rock"}, new int[]{1})));
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(TTBlocks.TAINT_SOIL.get(), rotatedWeighted(new String[]{"taint_soil_0", "taint_soil_1", "taint_soil_2"}, new int[]{16, 1, 1})));
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(TTBlocks.TAINT_CRUST.get(), rotatedWeighted(new String[]{"taint_crust_0", "taint_crust_1", "taint_crust_2"}, new int[]{8, 1, 1})));

        registerFluxGoo(blockModels);
        registerTaintGeyser(blockModels);
        registerTaintLog(blockModels);
        registerTaintFeature(blockModels);
        registerTaintFibre(blockModels);
        registerTaintSporeStalk(blockModels);

        blockItemModel(itemModels, TTBlocks.TAINT_ROCK.asItem(), "taint_rock");
        blockItemModel(itemModels, TTBlocks.TAINT_SOIL.asItem(), "taint_soil_0");
        blockItemModel(itemModels, TTBlocks.TAINT_CRUST.asItem(), "taint_crust_0");
        blockItemModel(itemModels, TTBlocks.TAINT_GEYSER.asItem(), "taint_geyser");
        blockItemModel(itemModels, TTBlocks.TAINT_LOG.asItem(), "taint_log");
        blockItemModel(itemModels, TTBlocks.TAINT_FEATURE.asItem(), "taint_orb_0");
        blockItemModel(itemModels, TTBlocks.TAINT_FIBRE.asItem(), "taint_fibre");
        blockItemModel(itemModels, TTBlocks.TAINT_SPORE_STALK.asItem(), "taint_spore_stalk_immature");
    }

    private void registerTaintSporeStalk(BlockModelGenerators blockModels) {
        Block stalk = TTBlocks.TAINT_SPORE_STALK.get();
        Identifier immature = ModelTemplates.CROSS.createWithSuffix(stalk, "_immature", TextureMapping.cross(texture("taint_spore_stalk_1")), blockModels.modelOutput);
        Identifier mature = ModelTemplates.CROSS.createWithSuffix(stalk, "_mature", TextureMapping.cross(texture("taint_spore_stalk_2")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(stalk)
                .with(PropertyDispatch.initial(BlockTaintSporeStalk.MATURE).select(false, BlockModelGenerators.plainVariant(immature)).select(true, BlockModelGenerators.plainVariant(mature))));
    }

    private void registerFluxGoo(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("flux_goo");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.FLUX_GOO.get(), variant));
    }

    private void registerTaintGeyser(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("taint_geyser");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.TAINT_GEYSER.get(), variant));
    }

    private void registerTaintLog(BlockModelGenerators blockModels) {
        WeightedList.Builder<Variant> entries = WeightedList.builder();
        for (int tex = 1; tex <= 2; tex++) {
            for (String face : new String[]{"north", "south", "east", "west"}) {
                entries.add(new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/taint_log_" + face + tex)), 1);
            }
        }
        MultiVariant barks = new MultiVariant(entries.build());
        PropertyDispatch<VariantMutator> axes = PropertyDispatch.modify(BlockStateProperties.AXIS).select(Direction.Axis.Y, BlockModelGenerators.NOP)
                .select(Direction.Axis.Z, BlockModelGenerators.X_ROT_90).select(Direction.Axis.X, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.TAINT_LOG.get(), barks).with(axes));
    }

    private MultiVariant rotatedWeighted(String[] models, int[] weights) {
        WeightedList.Builder<Variant> entries = WeightedList.builder();
        for (int i = 0; i < models.length; i++) {
            Variant base = new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + models[i]));
            entries.add(base, weights[i]);
            entries.add(BlockModelGenerators.X_ROT_90.apply(base), weights[i]);
            entries.add(BlockModelGenerators.Y_ROT_90.apply(base), weights[i]);
            entries.add(BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90).apply(base), weights[i]);
        }
        return new MultiVariant(entries.build());
    }

    private void registerTaintFeature(BlockModelGenerators blockModels) {
        MultiVariant orbs = orbVariants();
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(DirectionalBlock.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TTBlocks.TAINT_FEATURE.get(), orbs).with(rotations));
    }

    private MultiVariant orbVariants() {
        return new MultiVariant(WeightedList.<Variant>builder().add(new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/taint_orb_0")))
                .add(new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/taint_orb_1"))).add(new Variant(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/taint_orb_2"))).build());
    }

    private void registerTaintFibre(BlockModelGenerators blockModels) {
        MultiVariant fibre = variantOf("taint_fibre");
        MultiVariant growth1 = variantOf("taint_growth_1");
        MultiVariant growth2 = variantOf("taint_growth_2");
        MultiVariant growth3 = variantOf("taint_growth_3");
        MultiVariant growth4 = variantOf("taint_growth_4");
        MultiPartGenerator gen = MultiPartGenerator.multiPart(TTBlocks.TAINT_FIBRE.get());
        gen.with(new ConditionBuilder().term(BlockTaintFibre.NORTH, true), fibre);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.EAST, true), fibre.with(BlockModelGenerators.Y_ROT_90));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.SOUTH, true), fibre.with(BlockModelGenerators.Y_ROT_180));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.WEST, true), fibre.with(BlockModelGenerators.Y_ROT_270));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.UP, true), fibre.with(BlockModelGenerators.X_ROT_270));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.DOWN, true), fibre.with(BlockModelGenerators.X_ROT_90));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH1, true), growth1);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH2, true), growth2);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH3, true), growth3);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH4, true), growth4);
        blockModels.blockStateOutput.accept(gen);
    }

    private void blockItemModel(ItemModelGenerators itemModels, Item item, String modelName) {
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName)));
    }

    private void woodFamily(BlockModelGenerators blockModels, Block planks, Block door, Block trapdoor, Block fence, Block fenceGate, Block button, Block pressurePlate) {
        blockModels.createDoor(door);
        blockModels.createTrapdoor(trapdoor);
        blockModels.new BlockFamilyProvider(TextureMapping.cube(planks)).fence(fence).fenceGate(fenceGate).button(button).pressurePlate(pressurePlate);
    }

    private void arcaneGrindstone(BlockModelGenerators blockModels) {
        Block block = TTBlocks.ARCANE_GRINDSTONE.get();
        Material wheel = texture("arcane_stone_1");
        Material frame = texture("metal_thaumium");
        TextureMapping textures = new TextureMapping().put(GRINDSTONE_PIVOT_SLOT, frame).put(GRINDSTONE_ROUND_SLOT, wheel).put(TextureSlot.SIDE, wheel).put(TextureSlot.PARTICLE, wheel)
                .put(GRINDSTONE_LEG_SLOT, frame);
        MultiVariant model = BlockModelGenerators.plainVariant(ARCANE_GRINDSTONE.create(block, textures, blockModels.modelOutput));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, model).with(PropertyDispatch.modify(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                .select(AttachFace.FLOOR, Direction.NORTH, BlockModelGenerators.NOP).select(AttachFace.FLOOR, Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(AttachFace.FLOOR, Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(AttachFace.FLOOR, Direction.WEST, BlockModelGenerators.Y_ROT_270)
                .select(AttachFace.WALL, Direction.NORTH, BlockModelGenerators.X_ROT_90).select(AttachFace.WALL, Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))
                .select(AttachFace.WALL, Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                .select(AttachFace.WALL, Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270)).select(AttachFace.CEILING, Direction.SOUTH, BlockModelGenerators.X_ROT_180)
                .select(AttachFace.CEILING, Direction.WEST, BlockModelGenerators.X_ROT_180.then(BlockModelGenerators.Y_ROT_90))
                .select(AttachFace.CEILING, Direction.NORTH, BlockModelGenerators.X_ROT_180.then(BlockModelGenerators.Y_ROT_180))
                .select(AttachFace.CEILING, Direction.EAST, BlockModelGenerators.X_ROT_180.then(BlockModelGenerators.Y_ROT_270))));
    }

    private void decorModels(BlockModelGenerators blockModels) {
        slab(blockModels, TTBlocks.SLAB_GREATWOOD.get(), TTBlocks.PLANK_GREATWOOD.get(), texture("plank_greatwood"), texture("plank_greatwood"), texture("plank_greatwood"));
        slab(blockModels, TTBlocks.SLAB_SILVERWOOD.get(), TTBlocks.PLANK_SILVERWOOD.get(), texture("plank_silverwood"), texture("plank_silverwood"), texture("plank_silverwood"));
        slab(blockModels, TTBlocks.SLAB_ARCANE_STONE.get(), TTBlocks.STONE_ARCANE.get(), texture("arcane_stone_1"), texture("arcane_stone_2"), texture("arcane_stone_3"));
        slab(blockModels, TTBlocks.SLAB_ARCANE_BRICK.get(), TTBlocks.STONE_ARCANE_BRICK.get(), texture("arcane_brick_stone"), texture("arcane_brick_stone"), texture("arcane_brick_stone"));
        slab(blockModels, TTBlocks.SLAB_ANCIENT.get(), TTBlocks.STONE_ANCIENT.get(), texture("ancient_stone_1"), texture("ancient_stone_2"), texture("ancient_stone_3"));
        slab(blockModels, TTBlocks.SLAB_ELDRITCH.get(), TTBlocks.STONE_ELDRITCH_TILE.get(), texture("eldritch_stone_1"), texture("eldritch_stone_2"), texture("eldritch_stone_3"));
        stairsFromTexture(blockModels, TTBlocks.STAIRS_GREATWOOD.get(), texture("plank_greatwood"));
        stairsFromTexture(blockModels, TTBlocks.STAIRS_SILVERWOOD.get(), texture("plank_silverwood"));
        arcaneGrindstone(blockModels);
        woodFamily(blockModels, TTBlocks.PLANK_GREATWOOD.get(), TTBlocks.DOOR_GREATWOOD.get(), TTBlocks.TRAPDOOR_GREATWOOD.get(), TTBlocks.FENCE_GREATWOOD.get(), TTBlocks.FENCE_GATE_GREATWOOD.get(),
                TTBlocks.BUTTON_GREATWOOD.get(), TTBlocks.PRESSURE_PLATE_GREATWOOD.get());
        woodFamily(blockModels, TTBlocks.PLANK_SILVERWOOD.get(), TTBlocks.DOOR_SILVERWOOD.get(), TTBlocks.TRAPDOOR_SILVERWOOD.get(), TTBlocks.FENCE_SILVERWOOD.get(),
                TTBlocks.FENCE_GATE_SILVERWOOD.get(), TTBlocks.BUTTON_SILVERWOOD.get(), TTBlocks.PRESSURE_PLATE_SILVERWOOD.get());
        existingModelWithItem(blockModels, TTBlocks.TABLE_WOOD.get(), "table_wood");
        existingModelWithItem(blockModels, TTBlocks.TABLE_STONE.get(), "table_stone");
        paving(blockModels, TTBlocks.PAVING_STONE_TRAVEL.get(), "paving_stone_travel");
        paving(blockModels, TTBlocks.PAVING_STONE_BARRIER.get(), "paving_stone_barrier");
    }

    private Material texture(String name) {
        return new Material(Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + name));
    }

    private void slab(BlockModelGenerators blockModels, Block slab, Block fullBlock, Material bottom, Material top, Material side) {
        TextureMapping mapping = new TextureMapping().put(TextureSlot.BOTTOM, bottom).put(TextureSlot.TOP, top).put(TextureSlot.SIDE, side);
        MultiVariant bottomModel = BlockModelGenerators.plainVariant(ModelTemplates.SLAB_BOTTOM.create(slab, mapping, blockModels.modelOutput));
        MultiVariant topModel = BlockModelGenerators.plainVariant(ModelTemplates.SLAB_TOP.create(slab, mapping, blockModels.modelOutput));
        MultiVariant doubleModel = BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(fullBlock));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(slab)
                .with(PropertyDispatch.initial(BlockStateProperties.SLAB_TYPE).select(SlabType.BOTTOM, bottomModel).select(SlabType.TOP, topModel).select(SlabType.DOUBLE, doubleModel)));
        blockModels.registerSimpleItemModel(slab.asItem(), ModelLocationUtils.getModelLocation(slab));
    }

    private void stairsFromTexture(BlockModelGenerators blockModels, Block block, Material all) {
        TextureMapping mapping = new TextureMapping().put(TextureSlot.BOTTOM, all).put(TextureSlot.TOP, all).put(TextureSlot.SIDE, all);
        MultiVariant straight = BlockModelGenerators.plainVariant(ModelTemplates.STAIRS_STRAIGHT.create(block, mapping, blockModels.modelOutput));
        MultiVariant inner = BlockModelGenerators.plainVariant(ModelTemplates.STAIRS_INNER.create(block, mapping, blockModels.modelOutput));
        MultiVariant outer = BlockModelGenerators.plainVariant(ModelTemplates.STAIRS_OUTER.create(block, mapping, blockModels.modelOutput));
        blockModels.blockStateOutput.accept(createStairsDispatch(block, straight, inner, outer));
        blockModels.registerSimpleItemModel(block.asItem(), ModelLocationUtils.getModelLocation(block));
    }

    private MultiVariantGenerator createStairsDispatch(Block block, MultiVariant straight, MultiVariant inner, MultiVariant outer) {
        return MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, straight)
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer)
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outer)
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, inner)
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, inner)
                        .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.STRAIGHT, straight.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_RIGHT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_LEFT, outer.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.INNER_RIGHT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.WEST, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_180).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_90).with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.NORTH, Half.TOP, StairsShape.INNER_LEFT, inner.with(BlockModelGenerators.X_ROT_180).with(BlockModelGenerators.Y_ROT_270).with(BlockModelGenerators.UV_LOCK)));
    }

    private void existingModelWithItem(BlockModelGenerators blockModels, Block block, String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/" + modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private void paving(BlockModelGenerators blockModels, Block block, String name) {
        TextureMapping mapping = new TextureMapping().put(TextureSlot.DIRT, texture("arcane_brick_stone")).put(TextureSlot.TOP, texture(name)).put(TextureSlot.PARTICLE, texture(name));
        Identifier model = ModelTemplates.FARMLAND.create(block, mapping, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private void eldritchModels(BlockModelGenerators blockModels) {
        cube(blockModels, TTBlocks.OBSIDIAN_TILE.get(), "obsidian_tile", true);
        obsidianTotem(blockModels);
        cube(blockModels, TTBlocks.ELDRITCH_STONE.get(), "eldritch_stone", true);
        cube(blockModels, TTBlocks.ELDRITCH_STONE_INERT.get(), "eldritch_stone", true);
        cube(blockModels, TTBlocks.ELDRITCH_ROCK.get(), "eldritch_rock", true);
        cube(blockModels, TTBlocks.ELDRITCH_CRUST.get(), "eldritch_crust", true);
        insetBlock(blockModels, TTBlocks.ELDRITCH_CRUST_GLOWING.get(), "eldritch_crust_glowing");
        cube(blockModels, TTBlocks.ELDRITCH_DOOR.get(), "eldritch_door", true);
        insetBlock(blockModels, TTBlocks.ELDRITCH_STONE_CRYSTAL.get(), "eldritch_stone_crystal");
        eldritchLock(blockModels);
        crabSpawner(blockModels);
        column(blockModels, TTBlocks.ELDRITCH_PEDESTAL.get(), "eldritch_pedestal_side", "eldritch_stone");
        invisibleWithMeshItem(blockModels, TTBlocks.ELDRITCH_ALTAR.get(), "eldritch_altar", "eldritch_altar_item");
        invisibleWithCubeItem(blockModels, TTBlocks.ELDRITCH_OBELISK.get(), "eldritch_deco");
        invisibleWithCubeItem(blockModels, TTBlocks.ELDRITCH_PILLAR.get(), "eldritch_deco");
        invisibleWithCubeItem(blockModels, TTBlocks.ELDRITCH_CAPSTONE.get(), "eldritch_deco");
        trap(blockModels);
        invisible(blockModels, TTBlocks.ELDRITCH_NOTHING.get());
        invisible(blockModels, TTBlocks.ELDRITCH_PORTAL.get());
        stairsFromTexture(blockModels, TTBlocks.STAIRS_ELDRITCH.get(), texture("eldritch_stone"));
    }

    private void insetBlock(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier texture = TTIds.rl("block/" + textureName);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (int mask = 0; mask <= INSET_ALL_EXPOSED; mask++) {
            Identifier model = TTIds.rl("block/" + textureName + "_inset_" + mask);
            blockModels.modelOutput.accept(model, insetModel(texture, mask));
            ConditionBuilder condition = new ConditionBuilder();
            for (Direction dir : Direction.values()) {
                condition = condition.term(BlockEldritchInset.EXPOSED.get(dir), insetExposed(mask, dir));
            }
            generator = generator.with(condition, new MultiVariant(WeightedList.of(new Variant(model))));
        }
        blockModels.blockStateOutput.accept(generator);
        blockModels.registerSimpleItemModel(block.asItem(), TTIds.rl("block/" + textureName + "_inset_" + INSET_ALL_EXPOSED));
    }

    private void cube(BlockModelGenerators blockModels, Block block, String textureName, boolean item) {
        Identifier model = ModelTemplates.CUBE_ALL.create(block, new TextureMapping().put(TextureSlot.ALL, texture(textureName)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
        if (item) {
            blockModels.registerSimpleItemModel(block.asItem(), model);
        }
    }

    private void column(BlockModelGenerators blockModels, Block block, String side, String end) {
        Identifier model = ModelTemplates.CUBE_COLUMN.create(block, new TextureMapping().put(TextureSlot.SIDE, texture(side)).put(TextureSlot.END, texture(end)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private void obsidianTotem(BlockModelGenerators blockModels) {
        Block block = TTBlocks.OBSIDIAN_TOTEM.get();
        Identifier baseModel = ModelTemplates.CUBE_COLUMN.createWithSuffix(block, "_base",
                new TextureMapping().put(TextureSlot.SIDE, texture("obsidian_totem_base")).put(TextureSlot.END, texture("obsidian_tile")), blockModels.modelOutput);
        Identifier shadedModel = ModelTemplates.CUBE_COLUMN.createWithSuffix(block, "_shaded",
                new TextureMapping().put(TextureSlot.SIDE, texture("obsidian_totem_base_shaded")).put(TextureSlot.END, texture("obsidian_tile")), blockModels.modelOutput);
        WeightedList.Builder<Variant> carvings = WeightedList.builder();
        for (int i = 1; i <= 4; i++) {
            Identifier model = ModelTemplates.CUBE_COLUMN.createWithSuffix(block, "_carved_" + i,
                    new TextureMapping().put(TextureSlot.SIDE, texture("obsidian_totem_" + i)).put(TextureSlot.END, texture("obsidian_tile")), blockModels.modelOutput);
            carvings.add(new Variant(model), 1);
            carvings.add(new Variant(model).with(BlockModelGenerators.Y_ROT_90), 1);
            carvings.add(new Variant(model).with(BlockModelGenerators.Y_ROT_180), 1);
            carvings.add(new Variant(model).with(BlockModelGenerators.Y_ROT_270), 1);
        }
        MultiVariant carved = new MultiVariant(carvings.build());
        MultiVariant base = BlockModelGenerators.plainVariant(baseModel);
        MultiVariant shaded = BlockModelGenerators.plainVariant(shadedModel);
        for (Block totem : List.of(block, TTBlocks.OBSIDIAN_TOTEM_CHARGED.get())) {
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(totem).with(PropertyDispatch.initial(BlockObsidianTotem.UP, BlockObsidianTotem.DOWN).select(true, true, shaded)
                    .select(true, false, shaded).select(false, true, carved).select(false, false, base)));
        }
        blockModels.registerSimpleItemModel(block.asItem(), baseModel);
    }

    private void trap(BlockModelGenerators blockModels) {
        Block block = TTBlocks.ELDRITCH_TRAP.get();
        WeightedList.Builder<Variant> variants = WeightedList.builder();
        for (int i = 0; i < 4; i++) {
            Identifier model = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_" + i, new TextureMapping().put(TextureSlot.ALL, texture("eldritch_trap_" + i)), blockModels.modelOutput);
            variants.add(new Variant(model), 1);
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, new MultiVariant(variants.build())));
        blockModels.registerSimpleItemModel(block.asItem(), ModelLocationUtils.getModelLocation(block, "_0"));
    }

    private void crabSpawner(BlockModelGenerators blockModels) {
        Block block = TTBlocks.ELDRITCH_CRAB_SPAWNER.get();
        Identifier model = ModelLocationUtils.getModelLocation(block);
        MultiVariant base = BlockModelGenerators.plainVariant(model);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockEldritchCrabSpawner.FACING).select(Direction.UP, base).select(Direction.DOWN, base.with(BlockModelGenerators.X_ROT_180))
                        .select(Direction.NORTH, base.with(BlockModelGenerators.X_ROT_90)).select(Direction.EAST, base.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.SOUTH, base.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, base.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_270))));
        blockModels.registerSimpleItemModel(block.asItem(), model);
    }

    private void invisibleWithCubeItem(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier itemModel = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_inventory", new TextureMapping().put(TextureSlot.ALL, texture(textureName)), blockModels.modelOutput);
        blockModels.registerSimpleItemModel(block.asItem(), itemModel);
        Identifier model = ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particle(texture(textureName)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private void invisibleWithMeshItem(BlockModelGenerators blockModels, Block block, String textureName, String itemModelName) {
        blockModels.registerSimpleItemModel(block.asItem(), TTIds.rl("block/" + itemModelName));
        Identifier model = ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particle(texture(textureName)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private void invisible(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particle(texture("eldritch_stone")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private void containerItemModels(ItemModelGenerators itemModels) {
        registerPhial(itemModels);
        registerPrimordialPearl(itemModels);
    }

    private void registerPhial(ItemModelGenerators itemModels) {
        Identifier phialModel = itemModels.createFlatItemModel(TTItems.PHIAL.get(), ModelTemplates.FLAT_ITEM);
        Identifier filledModel = itemModels.generateLayeredItem(ModelLocationUtils.getModelLocation(TTItems.PHIAL.get(), "_filled"), TextureMapping.getItemTexture(TTItems.PHIAL.get()),
                TextureMapping.getItemTexture(TTItems.PHIAL.get(), "_overlay"));
        ItemModel.Unbaked phial = ItemModelUtils.plainModel(phialModel);
        ItemModel.Unbaked filled = ItemModelUtils.tintedModel(filledModel, new Constant(0xFFFFFF), new AspectColorTint(0xFFFFFF));
        itemModels.itemModelOutput.accept(TTItems.PHIAL.get(), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TTDataComponents.ASPECTS.get()), filled, phial));
    }

    private void registerPrimordialPearl(ItemModelGenerators itemModels) {
        Identifier pearlModel = itemModels.createFlatItemModel(TTItems.PRIMORDIAL_PEARL.get(), ModelTemplates.FLAT_ITEM);
        Identifier noduleModel = itemModels.createFlatItemModel(TTItems.PRIMORDIAL_PEARL.get(), "_nodule", ModelTemplates.FLAT_ITEM);
        Identifier moteModel = itemModels.createFlatItemModel(TTItems.PRIMORDIAL_PEARL.get(), "_mote", ModelTemplates.FLAT_ITEM);
        ItemModel.Unbaked pearl = ItemModelUtils.plainModel(pearlModel);
        ItemModel.Unbaked nodule = ItemModelUtils.plainModel(noduleModel);
        ItemModel.Unbaked mote = ItemModelUtils.plainModel(moteModel);
        float noduleThreshold = (float) (PrimordialPearlItem.PEARL_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        float moteThreshold = (float) (PrimordialPearlItem.NODULE_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        itemModels.itemModelOutput.accept(TTItems.PRIMORDIAL_PEARL.get(),
                ItemModelUtils.rangeSelect(new Damage(true), pearl, ItemModelUtils.override(nodule, noduleThreshold), ItemModelUtils.override(mote, moteThreshold)));
    }
}
