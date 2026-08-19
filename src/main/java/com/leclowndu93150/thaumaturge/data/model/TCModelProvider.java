package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.color.*;
import com.leclowndu93150.thaumaturge.client.model.*;
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
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.properties.*;
import org.joml.Matrix4f;

public final class TCModelProvider extends ModelProvider {
    private static final int ROBES_UNDYED_ARGB = 0xFF6A3880;

    private static final ModelTemplate THREE_LAYERED_ITEM = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/generated")), Optional.empty(), TextureSlot.LAYER0, TextureSlot.LAYER1,
            TextureSlot.LAYER2);
    private static final int FOLIAGE_DEFAULT_COLOR = 0x48B518;
    private static final int INSET_DEPTH = 2;
    private static final int INSET_ALL_EXPOSED = 63;

    public TCModelProvider(PackOutput output) {
        super(output, TCIds.MODID);
    }

    private static void registerVoidRobePiece(ItemModelGenerators itemModels, Item item, String name) {
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name);
        Material clothTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name + "_over"));
        Material metalTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name));
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
        Identifier empty = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/empty");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(empty)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private static void registerNitor(BlockModelGenerators blockModels, ItemModelGenerators itemModels, DyeColor dye) {
        var block = TCBlocks.NITORS.get(dye).get();
        Identifier empty = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/empty");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(empty)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));

        var item = TCItems.NITORS.get(dye).get();
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/nitor");
        int rgb = dye.getTextureDiffuseColor() & 0xFFFFFF;
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(itemModelId, new Constant(rgb)));
    }

    private static void registerInfusionAltar(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        PropertyDispatch<VariantMutator> facing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        registerPillar(blockModels, itemModels, TCBlocks.PILLAR_ARCANE.get(), "pillar_arcane", facing);
        registerPillar(blockModels, itemModels, TCBlocks.PILLAR_ANCIENT.get(), "pillar_ancient", facing);
        registerPillar(blockModels, itemModels, TCBlocks.PILLAR_ELDRITCH.get(), "pillar_eldritch", facing);
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.PEDESTAL_ARCANE.get(), "pedestal_arcane");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.RECHARGE_PEDESTAL.get(), "recharge_pedestal");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.PEDESTAL_ANCIENT.get(), "pedestal_ancient");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.PEDESTAL_ELDRITCH.get(), "pedestal_eldritch");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.INFUSION_MATRIX.get(), "infusion_matrix");
        itemModels.itemModelOutput.accept(TCBlocks.INFUSION_MATRIX.asItem(), ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/infusion_matrix")));
    }

    private static void registerPillar(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName, PropertyDispatch<VariantMutator> facing) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant).with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model), new ClientItem.Properties(true, true, 1));
    }

    private static void registerSimpleWithItem(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
        if (block != TCBlocks.INFUSION_MATRIX.get()) {
            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
        }
    }

    private static void registerSpa(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier spaModel = ModelTemplates.CUBE_BOTTOM_TOP.create(ModelLocationUtils.getModelLocation(TCBlocks.SPA.get()),
                new TextureMapping().put(TextureSlot.SIDE, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/spa_side")))
                        .put(TextureSlot.TOP, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/spa_top")))
                        .put(TextureSlot.BOTTOM, new Material(Identifier.withDefaultNamespace("block/furnace_top"))),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.SPA.get(), new MultiVariant(WeightedList.of(new Variant(spaModel)))));
        itemModels.itemModelOutput.accept(TCItems.SPA.get(), ItemModelUtils.plainModel(spaModel));
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(TCBlocks.PURIFYING_FLUID.get(), new MultiVariant(WeightedList.of(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/purifying_fluid"))))));
        itemModels.generateFlatItem(TCItems.BUCKET_LIQUID_DEATH.get(), ModelTemplates.FLAT_ITEM);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(TCBlocks.LIQUID_DEATH.get(), new MultiVariant(WeightedList.of(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/liquid_death"))))));
    }

    private static void registerGolemancy(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TCItems.MIND_CLOCKWORK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MIND_BIOTHAUMIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MODULE_VISION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MODULE_AGGRESSION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOLEM_BELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOLEM_TOP_HAT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOLEM_FEZ.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOLEM_GLASSES.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOLEM_BOWTIE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOLEM_VISOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BLANK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_PICKUP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_PICKUP_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_FILL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_FILL_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_EMPTY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_EMPTY_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_HARVEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BUTCHER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_GUARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_GUARD_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_LUMBER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BREAKER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BREAKER_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_USE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_PROVIDER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_STOCK.get(), ModelTemplates.FLAT_ITEM);

        Identifier golemModel = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(TCItems.GOLEM_PLACER.get()), TextureMapping.layer0(TCItems.GOLEM_PLACER.get()),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TCItems.GOLEM_PLACER.get(), ItemModelUtils.tintedModel(golemModel, new GolemMaterialTint()));

        Identifier inlayDot = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/inlay_dot");
        Identifier inlaySide = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/inlay_side");
        MultiPartGenerator inlayGenerator = MultiPartGenerator.multiPart(TCBlocks.INLAY.get()).with(new MultiVariant(WeightedList.of(new Variant(inlayDot))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.NORTH, true), new MultiVariant(WeightedList.of(new Variant(inlaySide))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.EAST, true), new MultiVariant(WeightedList.of(BlockModelGenerators.Y_ROT_90.apply(new Variant(inlaySide)))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.SOUTH, true), new MultiVariant(WeightedList.of(BlockModelGenerators.Y_ROT_180.apply(new Variant(inlaySide)))));
        inlayGenerator = inlayGenerator.with(new ConditionBuilder().term(BlockInlay.WEST, true), new MultiVariant(WeightedList.of(BlockModelGenerators.Y_ROT_270.apply(new Variant(inlaySide)))));
        blockModels.blockStateOutput.accept(inlayGenerator);
        Identifier inlayItemModel = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(TCItems.INLAY.get()), TextureMapping
                .layered(new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/inlay_connect_under")), new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/inlay_connect1"))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TCItems.INLAY.get(), ItemModelUtils.plainModel(inlayItemModel));

        Identifier patternCrafterModel = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/pattern_crafter");
        PropertyDispatch<VariantMutator> patternCrafterFacing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270).select(Direction.EAST, BlockModelGenerators.Y_ROT_90);
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(TCBlocks.PATTERN_CRAFTER.get(), new MultiVariant(WeightedList.of(new Variant(patternCrafterModel)))).with(patternCrafterFacing));
        itemModels.itemModelOutput.accept(TCItems.PATTERN_CRAFTER.get(), ItemModelUtils.plainModel(patternCrafterModel));

        Identifier sprayerModel = ModelTemplates.CUBE_BOTTOM_TOP.create(TCBlocks.POTION_SPRAYER.get(),
                new TextureMapping().put(TextureSlot.TOP, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/potion_sprayer_top")))
                        .put(TextureSlot.BOTTOM, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/potion_sprayer_bottom")))
                        .put(TextureSlot.SIDE, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/potion_sprayer_side"))),
                blockModels.modelOutput);
        PropertyDispatch<VariantMutator> sprayerFacing = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.POTION_SPRAYER.get(), new MultiVariant(WeightedList.of(new Variant(sprayerModel)))).with(sprayerFacing));
        itemModels.itemModelOutput.accept(TCItems.POTION_SPRAYER.get(), ItemModelUtils.plainModel(sprayerModel));

        PropertyDispatch<VariantMutator> levitatorFacing = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        Identifier levitatorOn = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/levitator_on");
        Identifier levitatorOff = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/levitator_off");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.LEVITATOR.get()).with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(levitatorOn)))).select(false, new MultiVariant(WeightedList.of(new Variant(levitatorOff))))).with(levitatorFacing));
        itemModels.itemModelOutput.accept(TCItems.LEVITATOR.get(), ItemModelUtils.plainModel(levitatorOff));

        registerInvisibleBlock(blockModels, TCBlocks.GOLEM_BUILDER.get());
        itemModels.itemModelOutput.accept(TCItems.GOLEM_BUILDER.get(),
                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/golem_builder_base"), Optional.empty(), new GolemBuilderItemSpecialRenderer.Unbaked()));
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_IRON_BARS.get());
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_CAULDRON.get());
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_ANVIL.get());
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_TABLE.get());
    }

    private static void registerConstructs(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TCItems.TURRET_BASIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TURRET_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TURRET_BORE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GRAPPLE_GUN_TIP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GRAPPLE_GUN_SPOOL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.ELDRITCH_EYE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.RUNED_TABLET.get(), ModelTemplates.FLAT_ITEM);
        ItemModel.Unbaked unloaded = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/grapple_gun_1"));
        ItemModel.Unbaked loaded = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/grapple_gun_2"));
        itemModels.itemModelOutput.accept(TCItems.GRAPPLE_GUN.get(), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TCDataComponents.GRAPPLE_LOADED.get()), loaded, unloaded));
        registerActivatorRail(blockModels);
    }

    private static void registerActivatorRail(BlockModelGenerators blockModels) {
        Block block = TCBlocks.ACTIVATOR_RAIL.get();
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
        registerFocusItem(itemModels, TCItems.FOCUS_1.get());
        registerFocusItem(itemModels, TCItems.FOCUS_2.get());
        registerFocusItem(itemModels, TCItems.FOCUS_3.get());
    }

    private static void registerFocusItem(ItemModelGenerators itemModels, Item item) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(model, new FocusColorTint()));
    }

    private static void registerRobeItem(ItemModelGenerators itemModels, Item item, String name) {
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name);
        Material baseTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name));
        Material overTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name + "_over"));
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModelId, TextureMapping.layered(baseTex, overTex), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(itemModelId, new Dye(ROBES_UNDYED_ARGB)));
    }

    private static void registerMirrorItem(ItemModelGenerators itemModels, Item item, String frameTexture) {
        Material frame = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + frameTexture));
        Identifier model = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(item),
                TextureMapping.layered(frame, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/mirrorpane"))), itemModels.modelOutput);
        Identifier linkedModel = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(item, "_on"),
                TextureMapping.layered(frame, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/mirrorpaneopen"))), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item,
                ItemModelUtils.conditional(new HasComponent(TCDataComponents.MIRROR_LINK.get(), false), ItemModelUtils.plainModel(linkedModel), ItemModelUtils.plainModel(model)));
    }

    private static void mirrorBlockState(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.PARTICLE_ONLY.createWithSuffix(block, "_state", TextureMapping.particle(new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/mirrorframe"))),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private static void cubeAllTexture(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier textureId = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + textureName);
        Material texture = new Material(textureId);
        Identifier modelId = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(texture), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
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
            return root;
        };
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
        registerResearchTable(blockModels);
        registerDeconstructionTable(blockModels, itemModels);
        registerResearchNote(itemModels);
        registerConstructs(blockModels, itemModels);
        decorModels(blockModels);
        eldritchModels(blockModels);
        translucentCube(blockModels, TCBlocks.AMBER_BRICK.get());
        blockModels.createTrivialCube(TCBlocks.FLESH_BLOCK.get());
        blockModels.registerSimpleItemModel(TCBlocks.FLESH_BLOCK.get().asItem(), ModelLocationUtils.getModelLocation(TCBlocks.FLESH_BLOCK.get()));
        registerInvisibleBlock(blockModels, TCBlocks.EFFECT_SHOCK.get());
        registerInvisibleBlock(blockModels, TCBlocks.BARRIER.get());
        registerInvisibleBlock(blockModels, TCBlocks.NODE.get());
        registerJar(blockModels, itemModels, TCBlocks.JAR_NORMAL.get(), "jar_normal");
        registerJar(blockModels, itemModels, TCBlocks.JAR_VOID.get(), "jar_void");
        registerJarBrain(blockModels, itemModels);
        registerAuraDevices(blockModels, itemModels);
        registerNoiseDevices(blockModels, itemModels);
        TubeModels.register(blockModels);
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.CRUCIBLE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.CRUCIBLE.get()))));
        mirrorBlockState(blockModels, TCBlocks.MIRROR.get());
        mirrorBlockState(blockModels, TCBlocks.MIRROR_ESSENTIA.get());
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.LOOT_URN_COMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_URN_COMMON.get()))));
        itemModels.itemModelOutput.accept(TCItems.LOOT_URN_COMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_URN_COMMON.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TCBlocks.LOOT_URN_UNCOMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_URN_UNCOMMON.get()))));
        itemModels.itemModelOutput.accept(TCItems.LOOT_URN_UNCOMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_URN_UNCOMMON.get())));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.LOOT_URN_RARE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_URN_RARE.get()))));
        itemModels.itemModelOutput.accept(TCItems.LOOT_URN_RARE.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_URN_RARE.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TCBlocks.LOOT_CRATE_COMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_CRATE_COMMON.get()))));
        itemModels.itemModelOutput.accept(TCItems.LOOT_CRATE_COMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_CRATE_COMMON.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TCBlocks.LOOT_CRATE_UNCOMMON.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_CRATE_UNCOMMON.get()))));
        itemModels.itemModelOutput.accept(TCItems.LOOT_CRATE_UNCOMMON.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_CRATE_UNCOMMON.get())));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.LOOT_CRATE_RARE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_CRATE_RARE.get()))));
        itemModels.itemModelOutput.accept(TCItems.LOOT_CRATE_RARE.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TCBlocks.LOOT_CRATE_RARE.get())));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TCBlocks.ARCANE_WORKBENCH.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.ARCANE_WORKBENCH.get()))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.ARCANE_WORKBENCH_CHARGER.get(),
                BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.ARCANE_WORKBENCH_CHARGER.get()))));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.VIS_RELAY.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.VIS_RELAY.get()))));
        itemModels.itemModelOutput.accept(TCItems.VIS_RELAY.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(TCBlocks.VIS_RELAY.get())));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.NODE_STABILIZER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.NODE_STABILIZER.get()))));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.NODE_TRANSDUCER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.NODE_TRANSDUCER.get()))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.NODE_STABILIZER_ADVANCED.get(),
                BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.NODE_STABILIZER_ADVANCED.get()))));
        itemModels.itemModelOutput.accept(TCBlocks.NODE_TRANSDUCER.get().asItem(),
                new SpecialModelWrapper.Unbaked(TCIds.rl("item/node_stabilizer_base"), Optional.empty(), new NodeStabilizerItemSpecialRenderer.Unbaked(false, true)));
        itemModels.itemModelOutput.accept(TCBlocks.NODE_STABILIZER.get().asItem(),
                new SpecialModelWrapper.Unbaked(TCIds.rl("item/node_stabilizer_base"), Optional.empty(), new NodeStabilizerItemSpecialRenderer.Unbaked(false)));
        itemModels.itemModelOutput.accept(TCBlocks.NODE_STABILIZER_ADVANCED.get().asItem(),
                new SpecialModelWrapper.Unbaked(TCIds.rl("item/node_stabilizer_base"), Optional.empty(), new NodeStabilizerItemSpecialRenderer.Unbaked(true)));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.JAR_NODE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.JAR_NORMAL.get()))));
        itemModels.itemModelOutput.accept(TCBlocks.JAR_NODE.get().asItem(),
                new CompositeModel.Unbaked(List.of(new CuboidItemModelWrapper.Unbaked(TCIds.rl("block/jar_normal"), Optional.empty(), List.of()),
                        new SpecialModelWrapper.Unbaked(TCIds.rl("block/jar_normal"), Optional.empty(), new JarNodeItemSpecialRenderer.Unbaked())), Optional.empty()));
        horizontalBlock(blockModels, itemModels, TCBlocks.INFERNAL_FURNACE.get(), "infernal_furnace", true);
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.NETHER_BRICKS_PLACEHOLDER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(Blocks.NETHER_BRICKS))));
        blockModels.blockStateOutput
                .accept(BlockModelGenerators.createSimpleBlock(TCBlocks.OBSIDIAN_PLACEHOLDER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(Blocks.OBSIDIAN))));
        registerAlembic(blockModels, itemModels, TCBlocks.ALEMBIC.get());
        registerBellows(blockModels, itemModels);
        registerSmelter(blockModels, itemModels, TCBlocks.SMELTER_BASIC.get(), "smelter_basic");
        registerSmelter(blockModels, itemModels, TCBlocks.SMELTER_THAUMIUM.get(), "smelter_thaumium");
        registerSmelter(blockModels, itemModels, TCBlocks.SMELTER_VOID.get(), "smelter_void");
        horizontalBlock(blockModels, itemModels, TCBlocks.SMELTER_AUX.get(), "smelter_aux");
        horizontalBlock(blockModels, itemModels, TCBlocks.SMELTER_VENT.get(), "smelter_vent");
        itemModels.generateFlatItem(TCItems.THAUMONOMICON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMONOMICON_CHEAT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMONOMICON_SHARING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMONOMICON_LINKING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CREATIVE_NODE_PLACER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(TCItems.WAND.get(),
                ItemModelUtils.conditional(new WandIsStaffProperty(), new SpecialModelWrapper.Unbaked(TCIds.rl("item/wand_staff_base"), Optional.empty(), new WandItemSpecialRenderer.Unbaked()),
                        new SpecialModelWrapper.Unbaked(TCIds.rl("item/wand_base"), Optional.empty(), new WandItemSpecialRenderer.Unbaked())));
        itemModels.generateFlatItem(TCItems.WAND_CAP_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_COPPER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_GOLD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_SILVER_INERT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_THAUMIUM_INERT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_VOID_INERT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_CAP_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_GREATWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_OBSIDIAN.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_BLAZE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_ICE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_QUARTZ.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_BONE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_REED.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.WAND_ROD_SILVERWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_GREATWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_OBSIDIAN.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_BLAZE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_ICE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_QUARTZ.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_BONE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_REED.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_SILVERWOOD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.STAFF_ROD_PRIMAL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.PRIMAL_CHARM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FABRIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MIRRORED_GLASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MECHANISM_SIMPLE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MECHANISM_COMPLEX.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MORPHIC_RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BATH_SALTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SANITY_SOAP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_BEEF.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_CHICKEN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_PORK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_FISH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_RABBIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_MUTTON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TRIPLE_MEAT_TREAT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(TCItems.THAUMOMETER.get(), ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/thaumometer")));
        itemModels.generateFlatItem(TCItems.JAR_BRACE.get(), ModelTemplates.FLAT_ITEM);
        Identifier labelModelId = itemModels.createFlatItemModel(TCItems.LABEL.get(), ModelTemplates.FLAT_ITEM);
        Identifier labelOverlayModelId = itemModels.createFlatItemModel(TCItems.LABEL.get(), "_overlay", ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(TCItems.LABEL.get(),
                new CompositeModel.Unbaked(List.of(ItemModelUtils.plainModel(labelModelId), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TCDataComponents.ASPECT_FILTER.get()),
                        ItemModelUtils.tintedModel(labelOverlayModelId, new AspectFilterTint(0xffffff)), ItemModelUtils.plainModel(labelModelId))), Optional.empty()));
        itemModels.generateFlatItem(TCItems.BOTTLE_TAINT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VIS_RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIC_SLIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_CRAWLER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINTACLE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SWARM_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SEED_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SEED_PRIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WISP_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BRAINY_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BRAIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FIREBAT_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MIND_SPIDER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PECH_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.ELDRITCH_CRAB_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INHABITED_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.ELDRITCH_GUARDIAN_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CULTIST_KNIGHT_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CULTIST_CLERIC_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CULTIST_PORTAL_LESSER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CULTIST_LEADER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CULTIST_PORTAL_GREATER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.ELDRITCH_WARDEN_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.ELDRITCH_GOLEM_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINTACLE_GIANT_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.LOOT_BAG_COMMON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.LOOT_BAG_UNCOMMON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.LOOT_BAG_RARE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PECH_WAND.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_BLADE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_PLATE_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_PLATE_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_PLATE_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_ROBE_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_ROBE_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_ROBE_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_VALVE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_RESTRICT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_ONEWAY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_BUFFER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOGGLES_REVEALING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SCRIBING_TOOLS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.ALUMENTUM.get(), ModelTemplates.FLAT_ITEM);
        registerCelestialNotes(itemModels);
        registerBaubleItems(itemModels);

        // Nitor Models
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/nitor");
        Material baseTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/nitor"));
        Material coreTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/nitor_core"));
        TextureMapping textures = TextureMapping.layered(baseTex, coreTex);
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModelId, textures, itemModels.modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            registerNitor(blockModels, itemModels, dye);
        }

        // Resources
        blockModels.createTrivialCube(TCBlocks.ORE_AMBER.get());
        blockModels.createTrivialCube(TCBlocks.ORE_CINNABAR.get());
        blockModels.createTrivialCube(TCBlocks.ORE_QUARTZ.get());

        blockModels.createTrivialCube(TCBlocks.ALCHEMICAL_CONSTRUCT.get());
        blockModels.createTrivialCube(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());

        blockModels.createTrivialCube(TCBlocks.METAL_BRASS_BLOCK.get());
        blockModels.createTrivialCube(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        blockModels.createTrivialCube(TCBlocks.METAL_VOID_BLOCK.get());
        translucentCube(blockModels, TCBlocks.AMBER_BLOCK.get());

        itemModels.generateFlatItem(TCItems.INGOT_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INGOT_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INGOT_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.AMBER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.QUICKSILVER.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.RARE_EARTH.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.NUGGET_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_QUICKSILVER.get(), ModelTemplates.FLAT_ITEM);
        registerInfusionAltar(blockModels, itemModels);
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.FOCAL_MANIPULATOR.get(), "focal_manipulator");
        registerInvisibleBlock(blockModels, TCBlocks.HOLE.get());
        registerInvisibleBlock(blockModels, TCBlocks.EFFECT_SAP.get());
        registerInvisibleBlock(blockModels, TCBlocks.EFFECT_GLIMMER.get());

        registerCandles(blockModels, itemModels);
        registerBanners(blockModels, itemModels);
        itemModels.generateFlatItem(TCItems.TALLOW.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.PRIMAL_CRUSHER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.TRAVELLER_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        registerRobeItem(itemModels, TCItems.CLOTH_CHEST.get(), "cloth_chest");
        registerRobeItem(itemModels, TCItems.CLOTH_LEGS.get(), "cloth_legs");
        registerRobeItem(itemModels, TCItems.CLOTH_BOOTS.get(), "cloth_boots");
        registerSpa(blockModels, itemModels);
        registerCasters(itemModels);
        registerGolemancy(blockModels, itemModels);

        itemModels.generateFlatItem(TCItems.NUGGET_QUARTZ.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.VOID_SEED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CAUSALITY_COLLAPSER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_GOLD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_COPPER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_LEAD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_TIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_CINNABAR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_QUARTZ.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.PLATE_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PLATE_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PLATE_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PLATE_VOID.get(), ModelTemplates.FLAT_ITEM);

        CrystalBlockstateGenerator.register(blockModels);
        CrystalItemModelGenerator.register(itemModels);
        EssentiaCrystalModelGenerator.register(itemModels);
        registerManaPod(blockModels, itemModels);
        stoneAndStairModels(blockModels);
        treeModels(blockModels, itemModels);
        plantModels(blockModels, itemModels);
        taintModels(blockModels, itemModels);
        containerItemModels(itemModels);
    }

    private void eldritchLock(BlockModelGenerators blockModels) {
        Identifier model = ModelTemplates.CUBE_ORIENTABLE.create(TCBlocks.ELDRITCH_LOCK.get(),
                new TextureMapping().put(TextureSlot.FRONT, texture("eldritch_lock_face")).put(TextureSlot.SIDE, texture("eldritch_lock_side")).put(TextureSlot.TOP, texture("eldritch_lock_side")),
                blockModels.modelOutput);
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, BlockModelGenerators.X_ROT_90)
                .select(Direction.UP, BlockModelGenerators.X_ROT_270).select(Direction.NORTH, BlockModelGenerators.NOP).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270).select(Direction.EAST, BlockModelGenerators.Y_ROT_90);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.ELDRITCH_LOCK.get(), BlockModelGenerators.plainVariant(model)).with(rotations));
        blockModels.registerSimpleItemModel(TCBlocks.ELDRITCH_LOCK.get().asItem(), model);
    }

    private void horizontalBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        horizontalBlock(blockModels, itemModels, block, modelName, false);
    }

    private void horizontalBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName, boolean oversizedInGui) {
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variantOf(modelName)).with(rotations));
        itemModels.itemModelOutput.accept(block.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName), Optional.empty(), List.of()),
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
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.BELLOWS.get(), variantOf("bellows")).with(rotations));
        itemModels.itemModelOutput.accept(TCBlocks.BELLOWS.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/bellows"), Optional.empty(), List.of()));
    }

    private void registerBanners(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/tc_banner");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        Material stand = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_stand"));
        Material cloth = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_cloth"));
        Material symbol = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_symbol"));
        Identifier dyedItemModel = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_dyed");
        Identifier cultistItemModel = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_cultist");
        THREE_LAYERED_ITEM.create(dyedItemModel, TextureMapping.layered(stand, cloth, symbol), itemModels.modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(cultistItemModel, TextureMapping.layered(stand, new Material(cultistItemModel)), itemModels.modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.BANNERS.get(dye).get(), variant));
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.WALL_BANNERS.get(dye).get(), variant));
            int tint = 0xFF000000 | dye.getMapColor().col;
            itemModels.itemModelOutput.accept(TCItems.BANNERS.get(dye).get(),
                    ItemModelUtils.tintedModel(dyedItemModel, new Constant(0xFFFFFFFF), new Constant(tint), new AspectFilterTint(dye.getMapColor().col)));
        }
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.BANNER_CRIMSON_CULT.get(), variant));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.WALL_BANNER_CRIMSON_CULT.get(), variant));
        itemModels.itemModelOutput.accept(TCItems.BANNER_CRIMSON_CULT.get(), ItemModelUtils.plainModel(cultistItemModel));
    }

    private void registerCandles(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/candle");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        for (DyeColor dye : DyeColor.values()) {
            Block candle = TCBlocks.CANDLES.get(dye).get();
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(candle, variant));
            int tint = 0xFF000000 | dye.getMapColor().col;
            itemModels.itemModelOutput.accept(candle.asItem(), ItemModelUtils.tintedModel(model, new Constant(tint)));
        }
    }

    private void registerBaubleItems(ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TCItems.AMULET_MUNDANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.RING_MUNDANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GIRDLE_MUNDANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.RING_APPRENTICE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.AMULET_FANCY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.RING_FANCY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GIRDLE_FANCY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.AMULET_VIS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.AMULET_VIS_CRAFTED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHARM_UNDYING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLOUD_RING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIOSITY_BAND.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOIDSEER_CHARM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FOCUS_POUCH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SANITY_CHECKER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIO_ARCANE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIO_PRESERVED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIO_ANCIENT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIO_ELDRITCH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIO_KNOWLEDGE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIO_TWISTED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CURIO_RITES.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CREATIVE_FLUX_SPONGE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.HAND_MIRROR.get(), ModelTemplates.FLAT_ITEM);
        registerMirrorItem(itemModels, TCItems.MIRROR.get(), "mirrorframe");
        registerMirrorItem(itemModels, TCItems.MIRROR_ESSENTIA.get(), "mirrorframe2");
        itemModels.generateFlatItem(TCItems.CRIMSON_PRAETOR_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_PRAETOR_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CRIMSON_PRAETOR_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FORTRESS_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FORTRESS_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FORTRESS_LEGS.get(), ModelTemplates.FLAT_ITEM);
        registerVerdantCharm(itemModels);
        registerVoidRobeItems(itemModels);
    }

    private void registerVerdantCharm(ItemModelGenerators itemModels) {
        Material base = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/verdant_charm"));
        List<SelectItemModel.SwitchCase<Integer>> cases = new ArrayList<>();
        Identifier fallback = null;
        for (int type = 0; type <= 2; type++) {
            Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/verdant_charm_" + type);
            Material overlay = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/verdant_charm_over_" + type));
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(base, overlay), itemModels.modelOutput);
            cases.add(ItemModelUtils.when(type, ItemModelUtils.plainModel(model)));
            if (type == 0) {
                fallback = model;
            }
        }
        itemModels.itemModelOutput.accept(TCItems.VERDANT_CHARM.get(), ItemModelUtils.select(new ComponentContents<>(TCDataComponents.VERDANT_TYPE.get()), ItemModelUtils.plainModel(fallback), cases));
    }

    private void registerVoidRobeItems(ItemModelGenerators itemModels) {
        Identifier helmModel = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(TCItems.VOID_ROBE_HELM.get()), TextureMapping.layer0(TCItems.VOID_ROBE_HELM.get()),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TCItems.VOID_ROBE_HELM.get(), ItemModelUtils.tintedModel(helmModel, new Dye(ROBES_UNDYED_ARGB)));
        registerVoidRobePiece(itemModels, TCItems.VOID_ROBE_CHEST.get(), "void_robe_chest");
        registerVoidRobePiece(itemModels, TCItems.VOID_ROBE_LEGS.get(), "void_robe_legs");
    }

    private void registerCelestialNotes(ItemModelGenerators itemModels) {
        Material sheet = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/celestial_notes_sheet"));
        List<SelectItemModel.SwitchCase<CelestialBody>> cases = new ArrayList<>();
        for (CelestialBody body : CelestialBody.values()) {
            Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/celestial_notes_" + body.getSerializedName());
            ModelTemplates.TWO_LAYERED_ITEM.create(model, TextureMapping.layered(sheet, new Material(model)), itemModels.modelOutput);
            cases.add(ItemModelUtils.when(body, ItemModelUtils.plainModel(model)));
        }
        Identifier fallback = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/celestial_notes_sun");
        itemModels.itemModelOutput.accept(TCItems.CELESTIAL_NOTES.get(),
                ItemModelUtils.select(new ComponentContents<>(TCDataComponents.CELESTIAL_BODY.get()), ItemModelUtils.plainModel(fallback), cases));
    }

    private void registerJar(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(TCIds.rl("block/" + modelName))));
        itemModels.itemModelOutput.accept(block.asItem(),
                new CompositeModel.Unbaked(
                        List.of(new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName), Optional.empty(), List.of()),
                                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName), Optional.empty(), new JarItemSpecialRenderer.Unbaked())),
                        Optional.empty()));
    }

    private void registerAlembic(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block) {

        MultiVariant coreVariant = variantOf("alembic");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(coreVariant);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BooleanProperty property = BlockEssentiaTransport.propertyFor(direction);
            Variant rotated = applyRotation(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/alembic_bore")), direction);
            MultiVariant variant = new MultiVariant(WeightedList.of(rotated));
            generator = generator.with(new ConditionBuilder().term(property, true), variant);
        }
        blockModels.blockStateOutput.accept(generator);

        itemModels.itemModelOutput.accept(block.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/alembic"), Optional.empty(), List.of()));
    }

    private void registerSmelter(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        MultiVariant off = variantOf(modelName + "_off");
        MultiVariant on = variantOf(modelName + "_on");
        PropertyDispatch<MultiVariant> lit = PropertyDispatch.initial(BlockSmelter.LIT).select(false, off).select(true, on);
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90).select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(lit).with(rotations));

        itemModels.itemModelOutput.accept(block.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName + "_off"), Optional.empty(), List.of()));
    }

    private MultiVariant variantOf(String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }

    private void registerDeconstructionTable(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerInvisibleBlock(blockModels, TCBlocks.DECONSTRUCTION_TABLE.get());
        itemModels.itemModelOutput.accept(TCBlocks.DECONSTRUCTION_TABLE.get().asItem(),
                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/deconstruction_table_base"), Optional.empty(), new DeconTableItemSpecialRenderer.Unbaked()));
    }

    private void registerResearchNote(ItemModelGenerators itemModels) {
        Identifier base = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(TCItems.RESEARCH_NOTE.get()), TextureMapping
                .layered(new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/research_note")), new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/research_note_overlay"))),
                itemModels.modelOutput);
        Identifier complete = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(TCItems.RESEARCH_NOTE.get(), "_complete"),
                TextureMapping.layered(new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/research_note_complete")),
                        new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/research_note_complete_overlay"))),
                itemModels.modelOutput);
        ItemModel.Unbaked baseModel = ItemModelUtils.tintedModel(base, new Constant(0xFFFFFF), new NoteColorTint(0x999999));
        ItemModel.Unbaked completeModel = ItemModelUtils.tintedModel(complete, new Constant(0xFFFFFF), new NoteColorTint(0x999999));
        itemModels.itemModelOutput.accept(TCItems.RESEARCH_NOTE.get(), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TCDataComponents.NOTE_COMPLETE.get()), completeModel, baseModel));
    }

    private void registerResearchTable(BlockModelGenerators blockModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/table_wood");
        registerInvisibleBlock(blockModels, TCBlocks.RESEARCH_TABLE.get());
        blockModels.registerSimpleItemModel(TCBlocks.RESEARCH_TABLE.get().asItem(), model);
    }

    private void registerJarBrain(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.JAR_BRAIN.get(), BlockModelGenerators.plainVariant(TCIds.rl("block/jar_normal"))));
        itemModels.itemModelOutput.accept(TCBlocks.JAR_BRAIN.get().asItem(),
                new CompositeModel.Unbaked(
                        List.of(new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/jar_normal"), Optional.empty(), List.of()),
                                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/jar_normal"), Optional.empty(), new JarBrainItemSpecialRenderer.Unbaked())),
                        Optional.empty()));
    }

    private void registerNoiseDevices(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        PropertyDispatch<VariantMutator> wallMount = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.ARCANE_EAR.get(), "arcane_ear_on", "arcane_ear_off", wallMount);
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.ARCANE_EAR_TOGGLE.get(), "arcane_ear_toggle_on", "arcane_ear_toggle_off", wallMount);

        PropertyDispatch<VariantMutator> hangMount = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, BlockModelGenerators.NOP)
                .select(Direction.UP, BlockModelGenerators.X_ROT_180).select(Direction.SOUTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.NORTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.LAMP_ARCANE.get(), "lamp_arcane_on", "lamp_arcane_off", hangMount);
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.LAMP_GROWTH.get(), "lamp_growth_on", "lamp_growth_off", hangMount);
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.LAMP_FERTILITY.get(), "lamp_fertility_on", "lamp_fertility_off", hangMount);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.EVERFULL_URN.get(), BlockModelGenerators.plainVariant(TCIds.rl("block/everfull_urn"))));
        itemModels.itemModelOutput.accept(TCItems.EVERFULL_URN.get(), ItemModelUtils.plainModel(TCIds.rl("block/everfull_urn")));

        PropertyDispatch<VariantMutator> deviceMount = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180)).select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.VIS_GENERATOR.get(), "vis_generator", "vis_generator", deviceMount);
        registerFacingDevice(blockModels, itemModels, TCBlocks.ESSENTIA_INPUT.get(), "essentia_input", deviceMount);
        registerFacingDevice(blockModels, itemModels, TCBlocks.ESSENTIA_OUTPUT.get(), "essentia_output", deviceMount);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.CONDENSER.get(), BlockModelGenerators.plainVariant(TCIds.rl("block/condenser"))));
        itemModels.itemModelOutput.accept(TCItems.CONDENSER.get(), ItemModelUtils.plainModel(TCIds.rl("block/condenser")));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.STABILIZER.get(), BlockModelGenerators.plainVariant(TCIds.rl("block/stabilizer"))));
        itemModels.itemModelOutput.accept(TCItems.STABILIZER.get(), ItemModelUtils.plainModel(TCIds.rl("block/stabilizer")));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.VOID_SIPHON.get(), BlockModelGenerators.plainVariant(TCIds.rl("block/void_siphon"))));
        itemModels.itemModelOutput.accept(TCItems.VOID_SIPHON.get(), ItemModelUtils.plainModel(TCIds.rl("block/void_siphon")));
        registerLattice(blockModels, itemModels, TCBlocks.CONDENSER_LATTICE.get(), "condenser_lattice_core");
        registerLattice(blockModels, itemModels, TCBlocks.CONDENSER_LATTICE_DIRTY.get(), "condenser_lattice_core_dirty");
        registerRelay(blockModels, itemModels);

        Identifier thaumatoriumModel = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/thaumatorium");
        PropertyDispatch<VariantMutator> thaumatoriumFacing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.WEST, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270)).select(Direction.NORTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.THAUMATORIUM.get(), new MultiVariant(WeightedList.of(new Variant(thaumatoriumModel)))).with(thaumatoriumFacing));
        itemModels.itemModelOutput.accept(TCItems.THAUMATORIUM.get(),
                new CuboidItemModelWrapper.Unbaked(thaumatoriumModel, Optional.of(new Transformation(new Matrix4f().translate(0, 0, 1).rotate(Axis.XP.rotationDegrees(270)))), List.of()),
                new ClientItem.Properties(true, true, 1));
        registerInvisibleBlock(blockModels, TCBlocks.THAUMATORIUM_TOP.get());
        registerFacingDevice(blockModels, itemModels, TCBlocks.BRAIN_BOX.get(), "brain_box",
                PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, BlockModelGenerators.NOP).select(Direction.UP, BlockModelGenerators.X_ROT_180)
                        .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90).select(Direction.NORTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270)));

        registerInvisibleBlock(blockModels, TCBlocks.CENTRIFUGE.get());
        itemModels.itemModelOutput.accept(TCItems.CENTRIFUGE.get(),
                new SpecialModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/centrifuge_base"), Optional.empty(), new CentrifugeItemSpecialRenderer.Unbaked()));

        registerInvisibleBlock(blockModels, TCBlocks.HUNGRY_CHEST.get());
        itemModels.itemModelOutput.accept(TCItems.HUNGRY_CHEST.get(),
                new SpecialModelWrapper.Unbaked(Identifier.withDefaultNamespace("item/chest"), Optional.empty(), new ChestSpecialRenderer.Unbaked(TCIds.rl("hungry"))));
    }

    private void registerLattice(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String coreModel) {
        Identifier side = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/condenser_lattice_side");
        Identifier core = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + coreModel);
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
        Identifier on = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/redstone_relay_on");
        Identifier off = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/redstone_relay_off");
        PropertyDispatch<VariantMutator> facing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).select(Direction.SOUTH, BlockModelGenerators.NOP)
                .select(Direction.NORTH, BlockModelGenerators.Y_ROT_180).select(Direction.WEST, BlockModelGenerators.Y_ROT_90).select(Direction.EAST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.REDSTONE_RELAY.get()).with(PropertyDispatch.initial(BlockStateProperties.POWERED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(on)))).select(false, new MultiVariant(WeightedList.of(new Variant(off))))).with(facing));
        itemModels.itemModelOutput.accept(TCItems.REDSTONE_RELAY.get(), ItemModelUtils.plainModel(off));
    }

    private void registerFacingDevice(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName, PropertyDispatch<VariantMutator> facing) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model)))).with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
    }

    private void registerEnabledFacingDevice(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String onModel, String offModel, PropertyDispatch<VariantMutator> facing) {
        Identifier on = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + onModel);
        Identifier off = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + offModel);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(on)))).select(false, new MultiVariant(WeightedList.of(new Variant(off))))).with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(off));
    }

    private void registerAuraDevices(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(TCBlocks.MATRIX_SPEED.get());
        blockModels.createTrivialCube(TCBlocks.MATRIX_COST.get());

        Identifier[] batteryModels = new Identifier[5];
        for (int i = 0; i < 5; i++) {
            Identifier textureId = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/vis_battery_" + i);
            batteryModels[i] = ModelTemplates.CUBE_ALL.create(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/vis_battery_" + i), TextureMapping.cube(new Material(textureId)),
                    blockModels.modelOutput);
        }
        PropertyDispatch<MultiVariant> chargeDispatch = PropertyDispatch.initial(BlockVisBattery.CHARGE).generate(charge -> {
            int tier = charge == 0 ? 0 : charge >= 10 ? 4 : (charge + 2) / 3;
            return new MultiVariant(WeightedList.of(new Variant(batteryModels[tier])));
        });
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.VIS_BATTERY.get()).with(chargeDispatch));
        itemModels.itemModelOutput.accept(TCItems.VIS_BATTERY.get(), ItemModelUtils.plainModel(batteryModels[0]));

        Identifier dioptraOn = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/dioptra_on");
        Identifier dioptraOff = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/dioptra_off");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.DIOPTRA.get()).with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                .select(true, new MultiVariant(WeightedList.of(new Variant(dioptraOn)))).select(false, new MultiVariant(WeightedList.of(new Variant(dioptraOff))))));
        itemModels.itemModelOutput.accept(TCItems.DIOPTRA.get(), ItemModelUtils.plainModel(dioptraOn));
    }

    private void stoneAndStairModels(BlockModelGenerators blockModels) {
        simpleCube(blockModels, TCBlocks.STONE_ARCANE.get(), "stone_arcane");
        simpleCube(blockModels, TCBlocks.STONE_ARCANE_BRICK.get(), "stone_arcane_brick");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT.get(), "stone_ancient");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_TILE.get(), "stone_ancient_tile");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_ROCK.get(), "stone_ancient_rock");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_GLYPHED.get(), "stone_ancient_glyphed");
        simpleCube(blockModels, TCBlocks.STONE_ANCIENT_DOORWAY.get(), "stone_ancient_doorway");
        simpleCube(blockModels, TCBlocks.STONE_ELDRITCH_TILE.get(), "stone_eldritch_tile");
        simpleCube(blockModels, TCBlocks.STONE_POROUS.get(), "stone_porous");

        stairsFromModels(blockModels, TCBlocks.STAIRS_ARCANE.get(), "arcane_stairs", "arcane_inner_stairs", "arcane_outer_stairs");
        stairsFromModels(blockModels, TCBlocks.STAIRS_ARCANE_BRICK.get(), "arcane_brick_stairs", "arcane_brick_inner_stairs", "arcane_brick_outer_stairs");
        stairsFromModels(blockModels, TCBlocks.STAIRS_ANCIENT.get(), "ancient_stairs", "ancient_inner_stairs", "ancient_outer_stairs");
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
        simpleCube(blockModels, TCBlocks.SAPLING_GREATWOOD.get(), "sapling_greatwood");
        simpleCube(blockModels, TCBlocks.SAPLING_SILVERWOOD.get(), "sapling_silverwood");
        flatItemFromBlock(itemModels, TCItems.SAPLING_GREATWOOD.get(), TCBlocks.SAPLING_GREATWOOD.get());
        flatItemFromBlock(itemModels, TCItems.SAPLING_SILVERWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get());
        simpleCube(blockModels, TCBlocks.PLANK_GREATWOOD.get(), "plank_greatwood");
        simpleCube(blockModels, TCBlocks.PLANK_SILVERWOOD.get(), "plank_silverwood");
        simpleCube(blockModels, TCBlocks.LEAVES_GREATWOOD.get(), "leaves_greatwood");
        simpleCube(blockModels, TCBlocks.LEAVES_SILVERWOOD.get(), "leaves_silverwood");
        itemModels.itemModelOutput.accept(TCBlocks.LEAVES_GREATWOOD.get().asItem(),
                ItemModelUtils.tintedModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/leaves_greatwood"), new Constant(FOLIAGE_DEFAULT_COLOR)));
        log(blockModels, TCBlocks.LOG_GREATWOOD.get(), TCBlocks.WOOD_GREATWOOD.get());
        log(blockModels, TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.WOOD_SILVERWOOD.get());
        log(blockModels, TCBlocks.STRIPPED_LOG_GREATWOOD.get(), TCBlocks.STRIPPED_WOOD_GREATWOOD.get());
        log(blockModels, TCBlocks.STRIPPED_LOG_SILVERWOOD.get(), TCBlocks.STRIPPED_WOOD_SILVERWOOD.get());
    }

    private void log(BlockModelGenerators blockModels, Block block, Block wood) {
        blockModels.woodProvider(block).logWithHorizontal(block).wood(wood);
    }

    private void plantModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        cross(blockModels, TCBlocks.PLANT_SHIMMERLEAF.get());
        cross(blockModels, TCBlocks.PLANT_CINDERPEARL.get());
        cross(blockModels, TCBlocks.PLANT_VISHROOM.get());

        flatItemFromBlock(itemModels, TCItems.PLANT_SHIMMERLEAF.get(), TCBlocks.PLANT_SHIMMERLEAF.get());
        flatItemFromBlock(itemModels, TCItems.PLANT_CINDERPEARL.get(), TCBlocks.PLANT_CINDERPEARL.get());
        flatItemFromBlock(itemModels, TCItems.PLANT_VISHROOM.get(), TCBlocks.PLANT_VISHROOM.get());

        Identifier grassModel = Identifier.withDefaultNamespace("block/grass_block");
        MultiVariant grassVariant = new MultiVariant(WeightedList.of(new Variant(grassModel)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.GRASS_AMBIENT.get(), grassVariant));
        itemModels.itemModelOutput.accept(TCItems.GRASS_AMBIENT.get(), ItemModelUtils.tintedModel(grassModel, new GrassColorSource(0.5F, 1.0F)));
    }

    private void flatItemFromBlock(ItemModelGenerators itemModels, Item item, Block block) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(TextureMapping.getBlockTexture(block)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private void registerManaPod(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block pod = TCBlocks.MANA_POD.get();
        MultiVariant[] stems = new MultiVariant[3];
        for (int i = 0; i < 3; i++) {
            Identifier model = ModelTemplates.CROSS.createWithSuffix(pod, "_stage" + i, TextureMapping.cross(TextureMapping.getBlockTexture(pod, "_stem_" + i)), blockModels.modelOutput);
            stems[i] = new MultiVariant(WeightedList.of(new Variant(model)));
        }
        PropertyDispatch<MultiVariant> ages = PropertyDispatch.initial(BlockManaPod.AGE).select(0, stems[0]).select(1, stems[1]).select(2, stems[2]).select(3, stems[2]).select(4, stems[2])
                .select(5, stems[2]).select(6, stems[2]).select(7, stems[2]);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(pod).with(ages));

        Identifier beanModel = ModelLocationUtils.getModelLocation(TCItems.MANA_BEAN.get());
        ModelTemplates.FLAT_ITEM.create(beanModel, TextureMapping.layer0(new Material(TCIds.rl("item/mana_bean"))), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TCItems.MANA_BEAN.get(), ItemModelUtils.tintedModel(beanModel, new CrystalAspectTint(0xFFFFFF)));
    }

    private void cross(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.CROSS.create(block, TextureMapping.cross(block), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private void taintModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_ROCK.get(), rotatedWeighted(new String[]{"taint_rock"}, new int[]{1})));
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_SOIL.get(), rotatedWeighted(new String[]{"taint_soil_0", "taint_soil_1", "taint_soil_2"}, new int[]{16, 1, 1})));
        blockModels.blockStateOutput
                .accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_CRUST.get(), rotatedWeighted(new String[]{"taint_crust_0", "taint_crust_1", "taint_crust_2"}, new int[]{8, 1, 1})));

        registerFluxGoo(blockModels);
        registerTaintGeyser(blockModels);
        registerTaintLog(blockModels);
        registerTaintFeature(blockModels);
        registerTaintFibre(blockModels);

        blockItemModel(itemModels, TCBlocks.TAINT_ROCK.asItem(), "taint_rock");
        blockItemModel(itemModels, TCBlocks.TAINT_SOIL.asItem(), "taint_soil_0");
        blockItemModel(itemModels, TCBlocks.TAINT_CRUST.asItem(), "taint_crust_0");
        blockItemModel(itemModels, TCBlocks.TAINT_GEYSER.asItem(), "taint_geyser");
        blockItemModel(itemModels, TCBlocks.TAINT_LOG.asItem(), "taint_log");
        blockItemModel(itemModels, TCBlocks.TAINT_FEATURE.asItem(), "taint_orb_0");
        blockItemModel(itemModels, TCBlocks.TAINT_FIBRE.asItem(), "taint_fibre");
    }

    private void registerFluxGoo(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("flux_goo");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.FLUX_GOO.get(), variant));
    }

    private void registerTaintGeyser(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("taint_geyser");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_GEYSER.get(), variant));
    }

    private void registerTaintLog(BlockModelGenerators blockModels) {
        WeightedList.Builder<Variant> entries = WeightedList.builder();
        for (int tex = 1; tex <= 2; tex++) {
            for (String face : new String[]{"north", "south", "east", "west"}) {
                entries.add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/taint_log_" + face + tex)), 1);
            }
        }
        MultiVariant barks = new MultiVariant(entries.build());
        PropertyDispatch<VariantMutator> axes = PropertyDispatch.modify(BlockStateProperties.AXIS).select(Direction.Axis.Y, BlockModelGenerators.NOP)
                .select(Direction.Axis.Z, BlockModelGenerators.X_ROT_90).select(Direction.Axis.X, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_LOG.get(), barks).with(axes));
    }

    private MultiVariant rotatedWeighted(String[] models, int[] weights) {
        WeightedList.Builder<Variant> entries = WeightedList.builder();
        for (int i = 0; i < models.length; i++) {
            Variant base = new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + models[i]));
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
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180).select(Direction.NORTH, BlockModelGenerators.X_ROT_270).select(Direction.SOUTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90)).select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_FEATURE.get(), orbs).with(rotations));
    }

    private MultiVariant orbVariants() {
        return new MultiVariant(WeightedList.<Variant>builder().add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/taint_orb_0")))
                .add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/taint_orb_1"))).add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/taint_orb_2"))).build());
    }

    private void registerTaintFibre(BlockModelGenerators blockModels) {
        MultiVariant fibre = variantOf("taint_fibre");
        MultiVariant growth1 = variantOf("taint_growth_1");
        MultiVariant growth2 = variantOf("taint_growth_2");
        MultiVariant growth3 = variantOf("taint_growth_3");
        MultiVariant growth4 = variantOf("taint_growth_4");
        MultiPartGenerator gen = MultiPartGenerator.multiPart(TCBlocks.TAINT_FIBRE.get());
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
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName)));
    }

    private void decorModels(BlockModelGenerators blockModels) {
        slab(blockModels, TCBlocks.SLAB_GREATWOOD.get(), TCBlocks.PLANK_GREATWOOD.get(), texture("plank_greatwood"), texture("plank_greatwood"), texture("plank_greatwood"));
        slab(blockModels, TCBlocks.SLAB_SILVERWOOD.get(), TCBlocks.PLANK_SILVERWOOD.get(), texture("plank_silverwood"), texture("plank_silverwood"), texture("plank_silverwood"));
        slab(blockModels, TCBlocks.SLAB_ARCANE_STONE.get(), TCBlocks.STONE_ARCANE.get(), texture("arcane_stone_1"), texture("arcane_stone_2"), texture("arcane_stone_3"));
        slab(blockModels, TCBlocks.SLAB_ARCANE_BRICK.get(), TCBlocks.STONE_ARCANE_BRICK.get(), texture("arcane_brick_stone"), texture("arcane_brick_stone"), texture("arcane_brick_stone"));
        slab(blockModels, TCBlocks.SLAB_ANCIENT.get(), TCBlocks.STONE_ANCIENT.get(), texture("ancient_stone_1"), texture("ancient_stone_2"), texture("ancient_stone_3"));
        slab(blockModels, TCBlocks.SLAB_ELDRITCH.get(), TCBlocks.STONE_ELDRITCH_TILE.get(), texture("eldritch_stone_1"), texture("eldritch_stone_2"), texture("eldritch_stone_3"));
        stairsFromTexture(blockModels, TCBlocks.STAIRS_GREATWOOD.get(), texture("plank_greatwood"));
        stairsFromTexture(blockModels, TCBlocks.STAIRS_SILVERWOOD.get(), texture("plank_silverwood"));
        existingModelWithItem(blockModels, TCBlocks.TABLE_WOOD.get(), "table_wood");
        existingModelWithItem(blockModels, TCBlocks.TABLE_STONE.get(), "table_stone");
        paving(blockModels, TCBlocks.PAVING_STONE_TRAVEL.get(), "paving_stone_travel");
        paving(blockModels, TCBlocks.PAVING_STONE_BARRIER.get(), "paving_stone_barrier");
    }

    private Material texture(String name) {
        return new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + name));
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
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
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
        cube(blockModels, TCBlocks.OBSIDIAN_TILE.get(), "obsidian_tile", true);
        obsidianTotem(blockModels);
        cube(blockModels, TCBlocks.ELDRITCH_STONE.get(), "eldritch_stone", true);
        cube(blockModels, TCBlocks.ELDRITCH_STONE_INERT.get(), "eldritch_stone", true);
        cube(blockModels, TCBlocks.ELDRITCH_ROCK.get(), "eldritch_rock", true);
        cube(blockModels, TCBlocks.ELDRITCH_CRUST.get(), "eldritch_crust", true);
        insetBlock(blockModels, TCBlocks.ELDRITCH_CRUST_GLOWING.get(), "eldritch_crust_glowing");
        cube(blockModels, TCBlocks.ELDRITCH_DOOR.get(), "eldritch_door", true);
        insetBlock(blockModels, TCBlocks.ELDRITCH_STONE_CRYSTAL.get(), "eldritch_stone_crystal");
        eldritchLock(blockModels);
        crabSpawner(blockModels);
        column(blockModels, TCBlocks.ELDRITCH_PEDESTAL.get(), "eldritch_pedestal_side", "eldritch_stone");
        invisibleWithMeshItem(blockModels, TCBlocks.ELDRITCH_ALTAR.get(), "eldritch_altar", "eldritch_altar_item");
        invisibleWithCubeItem(blockModels, TCBlocks.ELDRITCH_OBELISK.get(), "eldritch_deco");
        invisibleWithCubeItem(blockModels, TCBlocks.ELDRITCH_PILLAR.get(), "eldritch_deco");
        invisibleWithCubeItem(blockModels, TCBlocks.ELDRITCH_CAPSTONE.get(), "eldritch_deco");
        trap(blockModels);
        invisible(blockModels, TCBlocks.ELDRITCH_NOTHING.get());
        invisible(blockModels, TCBlocks.ELDRITCH_PORTAL.get());
        stairsFromTexture(blockModels, TCBlocks.STAIRS_ELDRITCH.get(), texture("eldritch_stone"));
    }

    private void insetBlock(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier texture = TCIds.rl("block/" + textureName);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (int mask = 0; mask <= INSET_ALL_EXPOSED; mask++) {
            Identifier model = TCIds.rl("block/" + textureName + "_inset_" + mask);
            blockModels.modelOutput.accept(model, insetModel(texture, mask));
            ConditionBuilder condition = new ConditionBuilder();
            for (Direction dir : Direction.values()) {
                condition = condition.term(BlockEldritchInset.EXPOSED.get(dir), insetExposed(mask, dir));
            }
            generator = generator.with(condition, new MultiVariant(WeightedList.of(new Variant(model))));
        }
        blockModels.blockStateOutput.accept(generator);
        blockModels.registerSimpleItemModel(block.asItem(), TCIds.rl("block/" + textureName + "_inset_" + INSET_ALL_EXPOSED));
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
        Block block = TCBlocks.OBSIDIAN_TOTEM.get();
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
        for (Block totem : List.of(block, TCBlocks.OBSIDIAN_TOTEM_CHARGED.get())) {
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(totem).with(PropertyDispatch.initial(BlockObsidianTotem.UP, BlockObsidianTotem.DOWN).select(true, true, shaded)
                    .select(true, false, shaded).select(false, true, carved).select(false, false, base)));
        }
        blockModels.registerSimpleItemModel(block.asItem(), baseModel);
    }

    private void trap(BlockModelGenerators blockModels) {
        Block block = TCBlocks.ELDRITCH_TRAP.get();
        WeightedList.Builder<Variant> variants = WeightedList.builder();
        for (int i = 0; i < 4; i++) {
            Identifier model = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_" + i, new TextureMapping().put(TextureSlot.ALL, texture("eldritch_trap_" + i)), blockModels.modelOutput);
            variants.add(new Variant(model), 1);
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, new MultiVariant(variants.build())));
        blockModels.registerSimpleItemModel(block.asItem(), ModelLocationUtils.getModelLocation(block, "_0"));
    }

    private void crabSpawner(BlockModelGenerators blockModels) {
        Block block = TCBlocks.ELDRITCH_CRAB_SPAWNER.get();
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
        blockModels.registerSimpleItemModel(block.asItem(), TCIds.rl("block/" + itemModelName));
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
        Identifier phialModel = itemModels.createFlatItemModel(TCItems.PHIAL.get(), ModelTemplates.FLAT_ITEM);
        Identifier filledModel = itemModels.generateLayeredItem(ModelLocationUtils.getModelLocation(TCItems.PHIAL.get(), "_filled"), TextureMapping.getItemTexture(TCItems.PHIAL.get()),
                TextureMapping.getItemTexture(TCItems.PHIAL.get(), "_overlay"));
        ItemModel.Unbaked phial = ItemModelUtils.plainModel(phialModel);
        ItemModel.Unbaked filled = ItemModelUtils.tintedModel(filledModel, new Constant(0xFFFFFF), new AspectColorTint(0xFFFFFF));
        itemModels.itemModelOutput.accept(TCItems.PHIAL.get(), ItemModelUtils.conditional(ItemModelUtils.hasComponent(TCDataComponents.ASPECTS.get()), filled, phial));
    }

    private void registerPrimordialPearl(ItemModelGenerators itemModels) {
        Identifier pearlModel = itemModels.createFlatItemModel(TCItems.PRIMORDIAL_PEARL.get(), ModelTemplates.FLAT_ITEM);
        Identifier noduleModel = itemModels.createFlatItemModel(TCItems.PRIMORDIAL_PEARL.get(), "_nodule", ModelTemplates.FLAT_ITEM);
        Identifier moteModel = itemModels.createFlatItemModel(TCItems.PRIMORDIAL_PEARL.get(), "_mote", ModelTemplates.FLAT_ITEM);
        ItemModel.Unbaked pearl = ItemModelUtils.plainModel(pearlModel);
        ItemModel.Unbaked nodule = ItemModelUtils.plainModel(noduleModel);
        ItemModel.Unbaked mote = ItemModelUtils.plainModel(moteModel);
        float noduleThreshold = (float) (PrimordialPearlItem.PEARL_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        float moteThreshold = (float) (PrimordialPearlItem.NODULE_MAX_DAMAGE + 1) / (float) PrimordialPearlItem.MAX_DAMAGE;
        itemModels.itemModelOutput.accept(TCItems.PRIMORDIAL_PEARL.get(),
                ItemModelUtils.rangeSelect(new Damage(true), pearl, ItemModelUtils.override(nodule, noduleThreshold), ItemModelUtils.override(mote, moteThreshold)));
    }
}
