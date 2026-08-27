package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aura.node.CreativeNodePlacerItem;
import com.leclowndu93150.thaumaturge.content.aura.node.JarNodeItem;
import com.leclowndu93150.thaumaturge.content.casters.FocusPouchItem;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.content.device.mirror.ItemBlockMirror;
import com.leclowndu93150.thaumaturge.content.device.mirror.ItemHandMirror;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbow;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.leclowndu93150.thaumaturge.content.entity.construct.TurretPlacerItem;
import com.leclowndu93150.thaumaturge.content.equipment.CrimsonBladeItem;
import com.leclowndu93150.thaumaturge.content.equipment.CultistPlateItem;
import com.leclowndu93150.thaumaturge.content.equipment.CultistRobeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalAxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalHoeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalPickaxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalShovelItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalSwordItem;
import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.GrappleGunItem;
import com.leclowndu93150.thaumaturge.content.equipment.PrimalCrusherItem;
import com.leclowndu93150.thaumaturge.content.equipment.RobeArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.TCMaterials;
import com.leclowndu93150.thaumaturge.content.equipment.TravellerBootsItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidAxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidGearItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidHoeItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidPickaxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidRobeArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidShovelItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidSwordItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.AmuletVisItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.TrinketItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VerdantCharmItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VoidseerCharmItem;
import com.leclowndu93150.thaumaturge.content.essentia.ItemResonator;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarBraceItem;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarBrainItem;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarItem;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemAccessory;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemBell;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemPlacer;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.golem.press.ItemGolemPress;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.ItemInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.item.CausalityCollapserItem;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.item.SalisMundusItem;
import com.leclowndu93150.thaumaturge.content.item.ScribingToolsItem;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.item.equipment.GogglesItem;
import com.leclowndu93150.thaumaturge.content.manabean.ItemManaBean;
import com.leclowndu93150.thaumaturge.content.misc.ItemCreativeFluxSponge;
import com.leclowndu93150.thaumaturge.content.misc.ItemCurio;
import com.leclowndu93150.thaumaturge.content.misc.alumentum.ItemAlumentum;
import com.leclowndu93150.thaumaturge.content.pech.PechWandItem;
import com.leclowndu93150.thaumaturge.content.research.book.CheatThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.LinkingThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.SharingThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.ThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.note.ItemResearchNote;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemBottleTaint;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import com.leclowndu93150.thaumaturge.content.wands.ItemPrimalCharm;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import com.leclowndu93150.thaumaturge.content.warp.ItemSanitySoap;
import com.leclowndu93150.thaumaturge.content.world.mound.LootBagItem;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCItems {
    private static final int SPAWN_EGG_BASE = 0x51436C;
    private static final int SPAWN_EGG_HIGHLIGHT = 0xB399CF;
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TCIds.MODID);

    public static final DeferredItem<BlockItem> FOCAL_MANIPULATOR =
            ITEMS.registerSimpleBlockItem(TCBlocks.FOCAL_MANIPULATOR);
    public static final DeferredItem<BlockItem> RESEARCH_TABLE = ITEMS.registerSimpleBlockItem(TCBlocks.RESEARCH_TABLE);
    public static final DeferredItem<BlockItem> DECONSTRUCTION_TABLE =
            ITEMS.registerSimpleBlockItem(TCBlocks.DECONSTRUCTION_TABLE);

    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH =
            ITEMS.registerSimpleBlockItem(TCBlocks.ARCANE_WORKBENCH);

    public static final DeferredItem<BlockItem> CRUCIBLE = ITEMS.registerSimpleBlockItem(TCBlocks.CRUCIBLE);

    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH_CHARGER =
            ITEMS.registerSimpleBlockItem(TCBlocks.ARCANE_WORKBENCH_CHARGER);

    public static final DeferredItem<BlockItem> ALEMBIC = ITEMS.registerSimpleBlockItem(TCBlocks.ALEMBIC);

    public static final DeferredItem<BlockItem> BELLOWS = ITEMS.registerSimpleBlockItem(TCBlocks.BELLOWS);

    public static final DeferredItem<BlockItem> SMELTER_BASIC = ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_BASIC);

    public static final DeferredItem<BlockItem> SMELTER_THAUMIUM =
            ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_THAUMIUM);

    public static final DeferredItem<BlockItem> SMELTER_VOID = ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_VOID);

    public static final DeferredItem<BlockItem> SMELTER_AUX = ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_AUX);

    public static final DeferredItem<BlockItem> SMELTER_VENT = ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_VENT);

    public static final DeferredItem<BlockItem> INFERNAL_FURNACE =
            registerSimpleBlockItem(TCBlocks.INFERNAL_FURNACE, ItemInfernalFurnace::new);

    public static final DeferredItem<BlockItem> JAR_NORMAL = registerSimpleBlockItem(TCBlocks.JAR_NORMAL, JarItem::new);

    public static final DeferredItem<BlockItem> JAR_VOID = registerSimpleBlockItem(TCBlocks.JAR_VOID, JarItem::new);

    public static final DeferredItem<BlockItem> TUBE = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE);

    public static final DeferredItem<BlockItem> TUBE_VALVE = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_VALVE);

    public static final DeferredItem<BlockItem> TUBE_RESTRICT = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_RESTRICT);

    public static final DeferredItem<BlockItem> TUBE_FILTER = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_FILTER);

    public static final DeferredItem<BlockItem> TUBE_ONEWAY = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_ONEWAY);

    public static final DeferredItem<BlockItem> TUBE_BUFFER = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_BUFFER);

    public static final DeferredItem<BlockItem> TAINT_ROCK = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_ROCK);
    public static final DeferredItem<BlockItem> TAINT_SOIL = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_SOIL);
    public static final DeferredItem<BlockItem> TAINT_CRUST = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_CRUST);
    public static final DeferredItem<BlockItem> TAINT_GEYSER = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_GEYSER);
    public static final DeferredItem<BlockItem> TAINT_LOG = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_LOG);
    public static final DeferredItem<BlockItem> TAINT_FEATURE = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_FEATURE);
    public static final DeferredItem<BlockItem> TAINT_FIBRE = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_FIBRE);

    public static final DeferredItem<BucketItem> BUCKET_LIQUID_DEATH = ITEMS.registerItem(
            "liquid_death_bucket",
            props -> new BucketItem(TCFluids.LIQUID_DEATH_SOURCE.get(), props),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<JarBraceItem> JAR_BRACE = ITEMS.registerItem("jar_brace", JarBraceItem::new);

    public static final DeferredItem<LabelItem> LABEL = ITEMS.registerItem("label", LabelItem::new);

    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON = ITEMS.registerItem(
            "thaumonomicon",
            ThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<CheatThaumonomiconItem> THAUMONOMICON_CHEAT = ITEMS.registerItem(
            "thaumonomicon_cheat",
            CheatThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<SharingThaumonomiconItem> THAUMONOMICON_SHARING = ITEMS.registerItem(
            "thaumonomicon_sharing",
            SharingThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<LinkingThaumonomiconItem> THAUMONOMICON_LINKING = ITEMS.registerItem(
            "thaumonomicon_linking",
            LinkingThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<CreativeNodePlacerItem> CREATIVE_NODE_PLACER = ITEMS.registerItem(
            "creative_node_placer", CreativeNodePlacerItem::new, new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<SalisMundusItem> SALIS_MUNDUS =
            ITEMS.registerItem("salis_mundus", SalisMundusItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemEssentiaCrystal> ESSENTIA_CRYSTAL =
            ITEMS.registerItem("essentia_crystal", ItemEssentiaCrystal::new);

    public static final DeferredItem<ItemBottleTaint> BOTTLE_TAINT = ITEMS.registerItem(
            "bottle_taint",
            ItemBottleTaint::new,
            new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> THAUMATORIUM = ITEMS.registerSimpleBlockItem(TCBlocks.THAUMATORIUM);
    public static final DeferredItem<BlockItem> BRAIN_BOX = ITEMS.registerSimpleBlockItem(TCBlocks.BRAIN_BOX);
    public static final DeferredItem<BlockItem> CONDENSER = ITEMS.registerSimpleBlockItem(TCBlocks.CONDENSER);
    public static final DeferredItem<BlockItem> CONDENSER_LATTICE =
            ITEMS.registerSimpleBlockItem(TCBlocks.CONDENSER_LATTICE);
    public static final DeferredItem<BlockItem> CONDENSER_LATTICE_DIRTY =
            ITEMS.registerSimpleBlockItem(TCBlocks.CONDENSER_LATTICE_DIRTY);
    public static final DeferredItem<BlockItem> STABILIZER = ITEMS.registerSimpleBlockItem(TCBlocks.STABILIZER);
    public static final DeferredItem<BlockItem> REDSTONE_RELAY = ITEMS.registerSimpleBlockItem(TCBlocks.REDSTONE_RELAY);
    public static final DeferredItem<BlockItem> VOID_SIPHON = ITEMS.registerSimpleBlockItem(TCBlocks.VOID_SIPHON);
    public static final DeferredItem<BlockItem> EVERFULL_URN = ITEMS.registerSimpleBlockItem(TCBlocks.EVERFULL_URN);
    public static final DeferredItem<BlockItem> VIS_GENERATOR = ITEMS.registerSimpleBlockItem(TCBlocks.VIS_GENERATOR);
    public static final DeferredItem<BlockItem> ESSENTIA_INPUT = ITEMS.registerSimpleBlockItem(TCBlocks.ESSENTIA_INPUT);
    public static final DeferredItem<BlockItem> ESSENTIA_OUTPUT =
            ITEMS.registerSimpleBlockItem(TCBlocks.ESSENTIA_OUTPUT);
    public static final DeferredItem<BlockItem> ARCANE_EAR = ITEMS.registerSimpleBlockItem(TCBlocks.ARCANE_EAR);
    public static final DeferredItem<BlockItem> ARCANE_EAR_TOGGLE =
            ITEMS.registerSimpleBlockItem(TCBlocks.ARCANE_EAR_TOGGLE);
    public static final DeferredItem<BlockItem> LAMP_ARCANE = ITEMS.registerSimpleBlockItem(TCBlocks.LAMP_ARCANE);
    public static final DeferredItem<BlockItem> LAMP_GROWTH = ITEMS.registerSimpleBlockItem(TCBlocks.LAMP_GROWTH);
    public static final DeferredItem<BlockItem> LAMP_FERTILITY = ITEMS.registerSimpleBlockItem(TCBlocks.LAMP_FERTILITY);
    public static final DeferredItem<BlockItem> CENTRIFUGE = ITEMS.registerSimpleBlockItem(TCBlocks.CENTRIFUGE);
    public static final DeferredItem<BlockItem> HUNGRY_CHEST = ITEMS.registerSimpleBlockItem(TCBlocks.HUNGRY_CHEST);
    public static final DeferredItem<BlockItem> MATRIX_SPEED = ITEMS.registerSimpleBlockItem(TCBlocks.MATRIX_SPEED);
    public static final DeferredItem<BlockItem> MATRIX_COST = ITEMS.registerSimpleBlockItem(TCBlocks.MATRIX_COST);
    public static final DeferredItem<BlockItem> VIS_BATTERY = ITEMS.registerSimpleBlockItem(TCBlocks.VIS_BATTERY);
    public static final DeferredItem<BlockItem> DIOPTRA = ITEMS.registerSimpleBlockItem(TCBlocks.DIOPTRA);
    public static final DeferredItem<JarBrainItem> JAR_BRAIN =
            ITEMS.registerItem("jar_brain", props -> new JarBrainItem(TCBlocks.JAR_BRAIN.get(), props));

    public static final DeferredItem<Item> VIS_RESONATOR = ITEMS.registerSimpleItem("vis_resonator");

    public static final DeferredItem<Item> THAUMIUM_SWORD = ITEMS.registerItem(
            "thaumium_sword",
            props -> new SwordItem(
                    TCMaterials.TOOL_THAUMIUM,
                    props.attributes(SwordItem.createAttributes(TCMaterials.TOOL_THAUMIUM, 3.0F, -2.4F))));
    public static final DeferredItem<Item> THAUMIUM_PICKAXE = ITEMS.registerItem(
            "thaumium_pickaxe",
            props -> new PickaxeItem(
                    TCMaterials.TOOL_THAUMIUM,
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_THAUMIUM, 1.0F, -2.8F))));
    public static final DeferredItem<Item> THAUMIUM_AXE = ITEMS.registerItem(
            "thaumium_axe",
            props -> new AxeItem(
                    TCMaterials.TOOL_THAUMIUM,
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_THAUMIUM, 4.5F, -3.0F))));
    public static final DeferredItem<Item> THAUMIUM_SHOVEL = ITEMS.registerItem(
            "thaumium_shovel",
            props -> new ShovelItem(
                    TCMaterials.TOOL_THAUMIUM,
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_THAUMIUM, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> THAUMIUM_HOE = ITEMS.registerItem(
            "thaumium_hoe",
            props -> new HoeItem(
                    TCMaterials.TOOL_THAUMIUM,
                    props.attributes(HoeItem.createAttributes(TCMaterials.TOOL_THAUMIUM, -3.0F, 0.0F))));
    public static final DeferredItem<Item> THAUMIUM_HELM = ITEMS.registerItem(
            "thaumium_helm",
            props -> new ArmorItem(
                    TCMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(TCMaterials.DURABILITY_THAUMIUM))));
    public static final DeferredItem<Item> THAUMIUM_CHEST = ITEMS.registerItem(
            "thaumium_chest",
            props -> new ArmorItem(
                    TCMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_THAUMIUM))));
    public static final DeferredItem<Item> THAUMIUM_LEGS = ITEMS.registerItem(
            "thaumium_legs",
            props -> new ArmorItem(
                    TCMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_THAUMIUM))));
    public static final DeferredItem<Item> THAUMIUM_BOOTS = ITEMS.registerItem(
            "thaumium_boots",
            props -> new ArmorItem(
                    TCMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorItem.Type.BOOTS.getDurability(TCMaterials.DURABILITY_THAUMIUM))));

    public static final DeferredItem<VoidSwordItem> VOID_SWORD = ITEMS.registerItem(
            "void_sword",
            props -> new VoidSwordItem(
                    props.attributes(SwordItem.createAttributes(TCMaterials.TOOL_VOID, 3.0F, -2.4F))));
    public static final DeferredItem<VoidPickaxeItem> VOID_PICKAXE = ITEMS.registerItem(
            "void_pickaxe",
            props -> new VoidPickaxeItem(
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_VOID, 1.0F, -2.8F))));
    public static final DeferredItem<VoidAxeItem> VOID_AXE = ITEMS.registerItem(
            "void_axe",
            props ->
                    new VoidAxeItem(props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_VOID, 4.0F, -3.0F))));
    public static final DeferredItem<VoidShovelItem> VOID_SHOVEL = ITEMS.registerItem(
            "void_shovel",
            props -> new VoidShovelItem(
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_VOID, 1.5F, -3.0F))));
    public static final DeferredItem<VoidHoeItem> VOID_HOE = ITEMS.registerItem(
            "void_hoe",
            props -> new VoidHoeItem(props.attributes(HoeItem.createAttributes(TCMaterials.TOOL_VOID, -3.0F, 0.0F))));
    public static final DeferredItem<VoidGearItem> VOID_HELM = ITEMS.registerItem(
            "void_helm",
            props -> new VoidGearItem(
                    TCMaterials.ARMOR_VOID,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(TCMaterials.DURABILITY_VOID))));
    public static final DeferredItem<VoidGearItem> VOID_CHEST = ITEMS.registerItem(
            "void_chest",
            props -> new VoidGearItem(
                    TCMaterials.ARMOR_VOID,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_VOID))));
    public static final DeferredItem<VoidGearItem> VOID_LEGS = ITEMS.registerItem(
            "void_legs",
            props -> new VoidGearItem(
                    TCMaterials.ARMOR_VOID,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_VOID))));
    public static final DeferredItem<VoidGearItem> VOID_BOOTS = ITEMS.registerItem(
            "void_boots",
            props -> new VoidGearItem(
                    TCMaterials.ARMOR_VOID,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorItem.Type.BOOTS.getDurability(TCMaterials.DURABILITY_VOID))));

    public static final DeferredItem<ElementalSwordItem> ELEMENTAL_SWORD = ITEMS.registerItem(
            "elemental_sword",
            props -> new ElementalSwordItem(
                    props.attributes(SwordItem.createAttributes(TCMaterials.TOOL_ELEMENTAL, 3.0F, -2.4F))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<ElementalPickaxeItem> ELEMENTAL_PICKAXE = ITEMS.registerItem(
            "elemental_pickaxe",
            props -> new ElementalPickaxeItem(
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_ELEMENTAL, 1.0F, -2.8F))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<ElementalAxeItem> ELEMENTAL_AXE = ITEMS.registerItem(
            "elemental_axe",
            props -> new ElementalAxeItem(
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_ELEMENTAL, 5.0F, -3.0F))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<ElementalShovelItem> ELEMENTAL_SHOVEL = ITEMS.registerItem(
            "elemental_shovel",
            props -> new ElementalShovelItem(
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_ELEMENTAL, 1.5F, -3.0F))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<ElementalHoeItem> ELEMENTAL_HOE = ITEMS.registerItem(
            "elemental_hoe",
            props -> new ElementalHoeItem(
                    props.attributes(HoeItem.createAttributes(TCMaterials.TOOL_ELEMENTAL, -3.0F, 0.0F))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<PrimalCrusherItem> PRIMAL_CRUSHER = ITEMS.registerItem(
            "primal_crusher",
            props -> new PrimalCrusherItem(
                    props.attributes(DiggerItem.createAttributes(TCMaterials.TOOL_PRIMAL_VOID, 3.5F, -2.8F))
                            .rarity(Rarity.RARE)));

    public static final DeferredItem<CrimsonBladeItem> CRIMSON_BLADE = ITEMS.registerItem(
            "crimson_blade",
            props -> new CrimsonBladeItem(
                    props.attributes(SwordItem.createAttributes(TCMaterials.TOOL_CRIMSON_VOID, 3.0F, -2.4F))
                            .rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> CRIMSON_PLATE_HELM = ITEMS.registerItem(
            "crimson_plate_helm",
            props -> new CultistPlateItem(
                    TCMaterials.ARMOR_CULTIST_PLATE,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(TCMaterials.DURABILITY_CULTIST_PLATE))
                            .rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> CRIMSON_PLATE_CHEST = ITEMS.registerItem(
            "crimson_plate_chest",
            props -> new CultistPlateItem(
                    TCMaterials.ARMOR_CULTIST_PLATE,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_CULTIST_PLATE))
                            .rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> CRIMSON_PLATE_LEGS = ITEMS.registerItem(
            "crimson_plate_legs",
            props -> new CultistPlateItem(
                    TCMaterials.ARMOR_CULTIST_PLATE,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_CULTIST_PLATE))
                            .rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<CultistRobeItem> CRIMSON_BOOTS = ITEMS.registerItem(
            "crimson_boots",
            props -> new CultistRobeItem(
                    TCMaterials.ARMOR_CULTIST_BOOTS,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorItem.Type.BOOTS.getDurability(TCMaterials.DURABILITY_CULTIST_BOOTS))
                            .rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<CultistRobeItem> CRIMSON_ROBE_HELM = ITEMS.registerItem(
            "crimson_robe_helm",
            props -> new CultistRobeItem(
                    TCMaterials.ARMOR_CULTIST_ROBE,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(TCMaterials.DURABILITY_CULTIST_ROBE))
                            .rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<CultistRobeItem> CRIMSON_ROBE_CHEST = ITEMS.registerItem(
            "crimson_robe_chest",
            props -> new CultistRobeItem(
                    TCMaterials.ARMOR_CULTIST_ROBE,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_CULTIST_ROBE))
                            .rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<CultistRobeItem> CRIMSON_ROBE_LEGS = ITEMS.registerItem(
            "crimson_robe_legs",
            props -> new CultistRobeItem(
                    TCMaterials.ARMOR_CULTIST_ROBE,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_CULTIST_ROBE))
                            .rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<BlockItem> RECHARGE_PEDESTAL =
            ITEMS.registerSimpleBlockItem(TCBlocks.RECHARGE_PEDESTAL);

    public static final DeferredItem<BlockItem> LEVITATOR = ITEMS.registerSimpleBlockItem(TCBlocks.LEVITATOR);
    public static final DeferredItem<BlockItem> POTION_SPRAYER = ITEMS.registerSimpleBlockItem(TCBlocks.POTION_SPRAYER);
    public static final DeferredItem<BlockItem> PATTERN_CRAFTER =
            ITEMS.registerSimpleBlockItem(TCBlocks.PATTERN_CRAFTER);
    public static final DeferredItem<BlockItem> INLAY = ITEMS.registerSimpleBlockItem(TCBlocks.INLAY);

    public static final DeferredItem<BlockItem> GOLEM_BUILDER =
            registerSimpleBlockItem(TCBlocks.GOLEM_BUILDER, ItemGolemPress::new);

    public static final DeferredItem<ItemGolemPlacer> GOLEM_PLACER = ITEMS.registerItem("golem", ItemGolemPlacer::new);
    public static final DeferredItem<ItemGolemBell> GOLEM_BELL =
            ITEMS.registerItem("golem_bell", ItemGolemBell::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<ItemGolemAccessory> GOLEM_TOP_HAT =
            ITEMS.registerItem("golem_top_hat", props -> new ItemGolemAccessory(TCGolemAccessories.TOP_HAT, props));
    public static final DeferredItem<ItemGolemAccessory> GOLEM_FEZ =
            ITEMS.registerItem("golem_fez", props -> new ItemGolemAccessory(TCGolemAccessories.FEZ, props));
    public static final DeferredItem<ItemGolemAccessory> GOLEM_GLASSES =
            ITEMS.registerItem("golem_glasses", props -> new ItemGolemAccessory(TCGolemAccessories.GLASSES, props));
    public static final DeferredItem<ItemGolemAccessory> GOLEM_BOWTIE =
            ITEMS.registerItem("golem_bowtie", props -> new ItemGolemAccessory(TCGolemAccessories.BOWTIE, props));
    public static final DeferredItem<ItemGolemAccessory> GOLEM_VISOR =
            ITEMS.registerItem("golem_visor", props -> new ItemGolemAccessory(TCGolemAccessories.VISOR, props));

    public static final DeferredItem<ItemSealPlacer> SEAL_BLANK = sealItem("seal_blank", null);
    public static final DeferredItem<ItemSealPlacer> SEAL_PICKUP = sealItem("seal_pickup", "pickup");
    public static final DeferredItem<ItemSealPlacer> SEAL_PICKUP_ADVANCED =
            sealItem("seal_pickup_advanced", "pickup_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_FILL = sealItem("seal_fill", "fill");
    public static final DeferredItem<ItemSealPlacer> SEAL_FILL_ADVANCED =
            sealItem("seal_fill_advanced", "fill_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_EMPTY = sealItem("seal_empty", "empty");
    public static final DeferredItem<ItemSealPlacer> SEAL_EMPTY_ADVANCED =
            sealItem("seal_empty_advanced", "empty_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_HARVEST = sealItem("seal_harvest", "harvest");
    public static final DeferredItem<ItemSealPlacer> SEAL_BUTCHER = sealItem("seal_butcher", "butcher");
    public static final DeferredItem<ItemSealPlacer> SEAL_GUARD = sealItem("seal_guard", "guard");
    public static final DeferredItem<ItemSealPlacer> SEAL_GUARD_ADVANCED =
            sealItem("seal_guard_advanced", "guard_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_LUMBER = sealItem("seal_lumber", "lumber");
    public static final DeferredItem<ItemSealPlacer> SEAL_BREAKER = sealItem("seal_breaker", "breaker");
    public static final DeferredItem<ItemSealPlacer> SEAL_BREAKER_ADVANCED =
            sealItem("seal_breaker_advanced", "breaker_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_USE = sealItem("seal_use", "use");
    public static final DeferredItem<ItemSealPlacer> SEAL_PROVIDER = sealItem("seal_provider", "provider");
    public static final DeferredItem<ItemSealPlacer> SEAL_STOCK = sealItem("seal_stock", "stock");

    private static DeferredItem<ItemSealPlacer> sealItem(String id, String sealPath) {
        return ITEMS.registerItem(id, props -> new ItemSealPlacer(sealPath == null ? null : TCIds.rl(sealPath), props));
    }

    public static final DeferredItem<TravellerBootsItem> TRAVELLER_BOOTS = ITEMS.registerItem(
            "traveller_boots",
            props -> new TravellerBootsItem(props.durability(350).rarity(Rarity.RARE)));

    public static final DeferredItem<RobeArmorItem> CLOTH_CHEST = ITEMS.registerItem(
            "cloth_chest",
            props -> new RobeArmorItem(
                    3,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_ROBES))));
    public static final DeferredItem<RobeArmorItem> CLOTH_LEGS = ITEMS.registerItem(
            "cloth_legs",
            props -> new RobeArmorItem(
                    3,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_ROBES))));
    public static final DeferredItem<RobeArmorItem> CLOTH_BOOTS = ITEMS.registerItem(
            "cloth_boots",
            props -> new RobeArmorItem(
                    2,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorItem.Type.BOOTS.getDurability(TCMaterials.DURABILITY_ROBES))));

    public static final DeferredItem<ItemManaBean> MANA_BEAN = ITEMS.registerItem(
            "mana_bean",
            ItemManaBean::new,
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.5F)
                            .alwaysEdible()
                            .fast()
                            .build()));

    public static final DeferredItem<Item> BRAIN = ITEMS.registerItem(
            "brain",
            Item::new,
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.2F)
                            .alwaysEdible()
                            .effect(new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8F)
                            .build()));

    public static final DeferredItem<SpawnEggItem> BRAINY_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("brainy_zombie_spawn_egg", TCEntities.BRAINY_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> BRAINY_DROWNED_SPAWN_EGG =
            registerSpawnEgg("brainy_drowned_spawn_egg", TCEntities.BRAINY_DROWNED);
    public static final DeferredItem<SpawnEggItem> BRAINY_HUSK_SPAWN_EGG =
            registerSpawnEgg("brainy_husk_spawn_egg", TCEntities.BRAINY_HUSK);
    public static final DeferredItem<SpawnEggItem> GIANT_BRAINY_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("giant_brainy_zombie_spawn_egg", TCEntities.GIANT_BRAINY_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> FIREBAT_SPAWN_EGG =
            registerSpawnEgg("firebat_spawn_egg", TCEntities.FIRE_BAT);
    public static final DeferredItem<SpawnEggItem> MIND_SPIDER_SPAWN_EGG =
            registerSpawnEgg("mind_spider_spawn_egg", TCEntities.MIND_SPIDER);
    public static final DeferredItem<SpawnEggItem> WISP_SPAWN_EGG = registerSpawnEgg("wisp_spawn_egg", TCEntities.WISP);
    public static final DeferredItem<SpawnEggItem> THAUMIC_SLIME_SPAWN_EGG =
            registerSpawnEgg("thaumic_slime_spawn_egg", TCEntities.THAUMIC_SLIME);
    public static final DeferredItem<SpawnEggItem> TAINT_CRAWLER_SPAWN_EGG =
            registerSpawnEgg("taint_crawler_spawn_egg", TCEntities.TAINT_CRAWLER);
    public static final DeferredItem<SpawnEggItem> TAINTACLE_SPAWN_EGG =
            registerSpawnEgg("taintacle_spawn_egg", TCEntities.TAINTACLE);
    public static final DeferredItem<SpawnEggItem> TAINT_SWARM_SPAWN_EGG =
            registerSpawnEgg("taint_swarm_spawn_egg", TCEntities.TAINT_SWARM);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_WARDEN_SPAWN_EGG =
            registerSpawnEgg("eldritch_warden_spawn_egg", TCEntities.ELDRITCH_WARDEN);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_GOLEM_SPAWN_EGG =
            registerSpawnEgg("eldritch_golem_spawn_egg", TCEntities.ELDRITCH_GOLEM);
    public static final DeferredItem<SpawnEggItem> CULTIST_LEADER_SPAWN_EGG =
            registerSpawnEgg("cultist_leader_spawn_egg", TCEntities.CULTIST_LEADER);
    public static final DeferredItem<SpawnEggItem> CULTIST_PORTAL_GREATER_SPAWN_EGG =
            registerSpawnEgg("cultist_portal_greater_spawn_egg", TCEntities.CULTIST_PORTAL_GREATER);
    public static final DeferredItem<SpawnEggItem> TAINTACLE_GIANT_SPAWN_EGG =
            registerSpawnEgg("taintacle_giant_spawn_egg", TCEntities.TAINTACLE_GIANT);
    public static final DeferredItem<SpawnEggItem> TAINT_SEED_SPAWN_EGG =
            registerSpawnEgg("taint_seed_spawn_egg", TCEntities.TAINT_SEED);
    public static final DeferredItem<SpawnEggItem> TAINT_SEED_PRIME_SPAWN_EGG =
            registerSpawnEgg("taint_seed_prime_spawn_egg", TCEntities.TAINT_SEED_PRIME);
    public static final DeferredItem<SpawnEggItem> PECH_SPAWN_EGG = registerSpawnEgg("pech_spawn_egg", TCEntities.PECH);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_CRAB_SPAWN_EGG =
            registerSpawnEgg("eldritch_crab_spawn_egg", TCEntities.ELDRITCH_CRAB);
    public static final DeferredItem<SpawnEggItem> INHABITED_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("inhabited_zombie_spawn_egg", TCEntities.INHABITED_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_GUARDIAN_SPAWN_EGG =
            registerSpawnEgg("eldritch_guardian_spawn_egg", TCEntities.ELDRITCH_GUARDIAN);
    public static final DeferredItem<SpawnEggItem> CULTIST_KNIGHT_SPAWN_EGG =
            registerSpawnEgg("cultist_knight_spawn_egg", TCEntities.CULTIST_KNIGHT);
    public static final DeferredItem<SpawnEggItem> CULTIST_CLERIC_SPAWN_EGG =
            registerSpawnEgg("cultist_cleric_spawn_egg", TCEntities.CULTIST_CLERIC);
    public static final DeferredItem<SpawnEggItem> CULTIST_PORTAL_LESSER_SPAWN_EGG =
            registerSpawnEgg("cultist_portal_lesser_spawn_egg", TCEntities.CULTIST_PORTAL_LESSER);

    public static final DeferredItem<PechWandItem> PECH_WAND =
            ITEMS.registerItem("pech_wand", PechWandItem::new, new Item.Properties().rarity(Rarity.RARE));

    public static final DeferredItem<LootBagItem> LOOT_BAG_COMMON = ITEMS.registerItem(
            "loot_bag_common", props -> new LootBagItem(TCLootTables.LOOT_BAG_COMMON, props.stacksTo(16)));
    public static final DeferredItem<LootBagItem> LOOT_BAG_UNCOMMON = ITEMS.registerItem(
            "loot_bag_uncommon",
            props -> new LootBagItem(
                    TCLootTables.LOOT_BAG_UNCOMMON, props.stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LootBagItem> LOOT_BAG_RARE = ITEMS.registerItem(
            "loot_bag_rare",
            props -> new LootBagItem(
                    TCLootTables.LOOT_BAG_RARE, props.stacksTo(16).rarity(Rarity.RARE)));

    public static final DeferredItem<BlockItem> LOOT_URN_COMMON =
            ITEMS.registerSimpleBlockItem(TCBlocks.LOOT_URN_COMMON);
    public static final DeferredItem<BlockItem> LOOT_URN_UNCOMMON =
            ITEMS.registerSimpleBlockItem(TCBlocks.LOOT_URN_UNCOMMON);
    public static final DeferredItem<BlockItem> LOOT_URN_RARE = ITEMS.registerSimpleBlockItem(TCBlocks.LOOT_URN_RARE);
    public static final DeferredItem<BlockItem> LOOT_CRATE_COMMON =
            ITEMS.registerSimpleBlockItem(TCBlocks.LOOT_CRATE_COMMON);
    public static final DeferredItem<BlockItem> LOOT_CRATE_UNCOMMON =
            ITEMS.registerSimpleBlockItem(TCBlocks.LOOT_CRATE_UNCOMMON);
    public static final DeferredItem<BlockItem> LOOT_CRATE_RARE =
            ITEMS.registerSimpleBlockItem(TCBlocks.LOOT_CRATE_RARE);

    private static DeferredItem<SpawnEggItem> registerSpawnEgg(
            String name, Supplier<? extends EntityType<? extends Mob>> type) {
        return ITEMS.registerItem(
                name, properties -> new DeferredSpawnEggItem(type, SPAWN_EGG_BASE, SPAWN_EGG_HIGHLIGHT, properties));
    }

    //
    public static final DeferredItem<Item> TALLOW = ITEMS.registerSimpleItem("tallow");

    public static final Map<DyeColor, DeferredItem<BlockItem>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(dye, ITEMS.registerSimpleBlockItem(TCBlocks.CANDLES.get(dye)));
        }
    }

    public static final Map<DyeColor, DeferredItem<StandingAndWallBlockItem>> BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(
                    dye,
                    ITEMS.registerItem(
                            "banner_" + dye.getName(),
                            props -> new StandingAndWallBlockItem(
                                    TCBlocks.BANNERS.get(dye).get(),
                                    TCBlocks.WALL_BANNERS.get(dye).get(),
                                    props.stacksTo(16),
                                    Direction.DOWN)));
        }
    }

    public static final DeferredItem<StandingAndWallBlockItem> BANNER_CRIMSON_CULT = ITEMS.registerItem(
            "banner_crimson_cult",
            props -> new StandingAndWallBlockItem(
                    TCBlocks.BANNER_CRIMSON_CULT.get(),
                    TCBlocks.WALL_BANNER_CRIMSON_CULT.get(),
                    props.stacksTo(16),
                    Direction.DOWN));

    public static final Map<DyeColor, DeferredItem<BlockItem>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, ITEMS.registerSimpleBlockItem(TCBlocks.NITORS.get(dye)));
        }
    }

    public static final DeferredItem<BlockItem> SPA = ITEMS.registerSimpleBlockItem(TCBlocks.SPA);

    public static final DeferredItem<ItemAlumentum> ALUMENTUM = ITEMS.registerItem("alumentum", ItemAlumentum::new);

    public static final DeferredItem<Item> FABRIC = ITEMS.registerSimpleItem("fabric");
    public static final DeferredItem<Item> MIRRORED_GLASS = ITEMS.registerSimpleItem("mirrored_glass");
    public static final DeferredItem<Item> FILTER = ITEMS.registerSimpleItem("filter");
    public static final DeferredItem<Item> MECHANISM_SIMPLE = ITEMS.registerSimpleItem("mechanism_simple");
    public static final DeferredItem<Item> MECHANISM_COMPLEX = ITEMS.registerSimpleItem("mechanism_complex");
    public static final DeferredItem<Item> MIND_CLOCKWORK = ITEMS.registerSimpleItem("mind_clockwork");
    public static final DeferredItem<Item> MIND_BIOTHAUMIC = ITEMS.registerSimpleItem("mind_biothaumic");
    public static final DeferredItem<Item> MODULE_VISION = ITEMS.registerSimpleItem("module_vision");
    public static final DeferredItem<Item> MODULE_AGGRESSION = ITEMS.registerSimpleItem("module_aggression");
    public static final DeferredItem<Item> MORPHIC_RESONATOR = ITEMS.registerSimpleItem("morphic_resonator");
    public static final DeferredItem<Item> BATH_SALTS = ITEMS.registerSimpleItem("bath_salts");
    public static final DeferredItem<ItemSanitySoap> SANITY_SOAP =
            ITEMS.registerItem("sanity_soap", ItemSanitySoap::new);

    public static final DeferredItem<Item> CHUNK_BEEF = registerChunk("chunk_beef");
    public static final DeferredItem<Item> CHUNK_CHICKEN = registerChunk("chunk_chicken");
    public static final DeferredItem<Item> CHUNK_PORK = registerChunk("chunk_pork");
    public static final DeferredItem<Item> CHUNK_FISH = registerChunk("chunk_fish");
    public static final DeferredItem<Item> CHUNK_RABBIT = registerChunk("chunk_rabbit");
    public static final DeferredItem<Item> CHUNK_MUTTON = registerChunk("chunk_mutton");

    public static final DeferredItem<Item> TRIPLE_MEAT_TREAT = ITEMS.registerItem(
            "triple_meat_treat",
            props -> new Item(props.food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.8F)
                    .alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66F)
                    .build())));

    private static DeferredItem<Item> registerChunk(String id) {
        return ITEMS.registerItem(
                id,
                props -> new Item(props.food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationModifier(0.3F)
                        .fast()
                        .build())));
    }

    public static final DeferredItem<BlockItem> CRYSTAL_AER = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_AER);
    public static final DeferredItem<BlockItem> CRYSTAL_IGNIS = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_IGNIS);
    public static final DeferredItem<BlockItem> CRYSTAL_AQUA = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_AQUA);
    public static final DeferredItem<BlockItem> CRYSTAL_TERRA = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_TERRA);
    public static final DeferredItem<BlockItem> CRYSTAL_ORDO = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_ORDO);
    public static final DeferredItem<BlockItem> CRYSTAL_PERDITIO =
            ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_PERDITIO);
    public static final DeferredItem<BlockItem> CRYSTAL_VITIUM = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_VITIUM);

    //

    public static final DeferredItem<BlockItem> STONE_ARCANE = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ARCANE);
    public static final DeferredItem<BlockItem> STONE_ARCANE_BRICK =
            ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_TILE =
            ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_TILE);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_ROCK =
            ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_ROCK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_GLYPHED =
            ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_GLYPHED);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_DOORWAY =
            ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_DOORWAY);
    public static final DeferredItem<BlockItem> STONE_ELDRITCH_TILE =
            ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ELDRITCH_TILE);
    public static final DeferredItem<BlockItem> STONE_POROUS = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_POROUS);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE = ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_ARCANE);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE_BRICK =
            ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STAIRS_ANCIENT = ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_ANCIENT);

    //

    public static final DeferredItem<BlockItem> SAPLING_GREATWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.SAPLING_GREATWOOD);
    public static final DeferredItem<BlockItem> SAPLING_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.SAPLING_SILVERWOOD);
    public static final DeferredItem<BlockItem> LOG_GREATWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.LOG_GREATWOOD);
    public static final DeferredItem<BlockItem> WOOD_GREATWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.WOOD_GREATWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_LOG_GREATWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.STRIPPED_LOG_GREATWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_WOOD_GREATWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.STRIPPED_WOOD_GREATWOOD);
    public static final DeferredItem<BlockItem> LOG_SILVERWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.LOG_SILVERWOOD);
    public static final DeferredItem<BlockItem> WOOD_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.WOOD_SILVERWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_LOG_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.STRIPPED_LOG_SILVERWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_WOOD_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.STRIPPED_WOOD_SILVERWOOD);
    public static final DeferredItem<BlockItem> LEAVES_GREATWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.LEAVES_GREATWOOD);
    public static final DeferredItem<BlockItem> LEAVES_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.LEAVES_SILVERWOOD);
    public static final DeferredItem<BlockItem> PLANK_GREATWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.PLANK_GREATWOOD);
    public static final DeferredItem<BlockItem> PLANK_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.PLANK_SILVERWOOD);

    //

    public static final DeferredItem<BlockItem> PLANT_SHIMMERLEAF =
            ITEMS.registerSimpleBlockItem(TCBlocks.PLANT_SHIMMERLEAF);
    public static final DeferredItem<BlockItem> PLANT_CINDERPEARL =
            ITEMS.registerSimpleBlockItem(TCBlocks.PLANT_CINDERPEARL);
    public static final DeferredItem<BlockItem> PLANT_VISHROOM = ITEMS.registerSimpleBlockItem(TCBlocks.PLANT_VISHROOM);
    public static final DeferredItem<BlockItem> GRASS_AMBIENT = ITEMS.registerSimpleBlockItem(TCBlocks.GRASS_AMBIENT);

    // RESOURCES | INGOTS

    public static final DeferredItem<BlockItem> ORE_AMBER = ITEMS.registerSimpleBlockItem(TCBlocks.ORE_AMBER);
    public static final DeferredItem<BlockItem> ORE_CINNABAR = ITEMS.registerSimpleBlockItem(TCBlocks.ORE_CINNABAR);
    public static final DeferredItem<BlockItem> ORE_QUARTZ = ITEMS.registerSimpleBlockItem(TCBlocks.ORE_QUARTZ);

    public static final DeferredItem<BlockItem> ALCHEMICAL_CONSTRUCT =
            ITEMS.registerSimpleBlockItem(TCBlocks.ALCHEMICAL_CONSTRUCT);
    public static final DeferredItem<BlockItem> ADVANCED_ALCHEMICAL_CONSTRUCT =
            ITEMS.registerSimpleBlockItem(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT);

    public static final DeferredItem<BlockItem> METAL_THAUMIUM_BLOCK =
            ITEMS.registerSimpleBlockItem(TCBlocks.METAL_THAUMIUM_BLOCK);
    public static final DeferredItem<BlockItem> METAL_BRASS_BLOCK =
            ITEMS.registerSimpleBlockItem(TCBlocks.METAL_BRASS_BLOCK);
    public static final DeferredItem<BlockItem> METAL_VOID_BLOCK =
            ITEMS.registerSimpleBlockItem(TCBlocks.METAL_VOID_BLOCK);
    public static final DeferredItem<BlockItem> AMBER_BLOCK = ITEMS.registerSimpleBlockItem(TCBlocks.AMBER_BLOCK);

    public static final DeferredItem<Item> INGOT_BRASS = ITEMS.registerSimpleItem("ingot_brass");
    public static final DeferredItem<Item> INGOT_THAUMIUM = ITEMS.registerSimpleItem("ingot_thaumium");
    public static final DeferredItem<Item> INGOT_VOID = ITEMS.registerSimpleItem("ingot_void");
    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerSimpleItem("quicksilver");
    public static final DeferredItem<Item> AMBER = ITEMS.registerSimpleItem("amber");

    public static final DeferredItem<Item> RARE_EARTH = ITEMS.registerSimpleItem("rare_earth");

    public static final DeferredItem<Item> NUGGET_BRASS = ITEMS.registerSimpleItem("nugget_brass");
    public static final DeferredItem<Item> NUGGET_THAUMIUM = ITEMS.registerSimpleItem("nugget_thaumium");
    public static final DeferredItem<Item> NUGGET_VOID = ITEMS.registerSimpleItem("nugget_void");
    public static final DeferredItem<Item> NUGGET_QUARTZ = ITEMS.registerSimpleItem("nugget_quartz");
    public static final DeferredItem<Item> NUGGET_QUICKSILVER = ITEMS.registerSimpleItem("nugget_quicksilver");

    public static final DeferredItem<Item> PLATE_IRON = ITEMS.registerSimpleItem("plate_iron");
    public static final DeferredItem<Item> PLATE_BRASS = ITEMS.registerSimpleItem("plate_brass");
    public static final DeferredItem<Item> PLATE_THAUMIUM = ITEMS.registerSimpleItem("plate_thaumium");
    public static final DeferredItem<Item> PLATE_VOID = ITEMS.registerSimpleItem("plate_void");

    public static final DeferredItem<Item> CLUSTER_IRON = ITEMS.registerSimpleItem("cluster_iron");
    public static final DeferredItem<Item> CLUSTER_GOLD = ITEMS.registerSimpleItem("cluster_gold");
    public static final DeferredItem<Item> CLUSTER_COPPER = ITEMS.registerSimpleItem("cluster_copper");
    public static final DeferredItem<Item> CLUSTER_TIN = ITEMS.registerSimpleItem("cluster_tin");
    public static final DeferredItem<Item> CLUSTER_SILVER = ITEMS.registerSimpleItem("cluster_silver");
    public static final DeferredItem<Item> CLUSTER_LEAD = ITEMS.registerSimpleItem("cluster_lead");
    public static final DeferredItem<Item> CLUSTER_CINNABAR = ITEMS.registerSimpleItem("cluster_cinnabar");
    public static final DeferredItem<Item> CLUSTER_QUARTZ = ITEMS.registerSimpleItem("cluster_quartz");

    //

    public static final int SCRIBING_TOOLS_DURABILITY = 100;

    public static final DeferredItem<ThaumometerItem> THAUMOMETER = ITEMS.registerItem(
            "thaumometer",
            ThaumometerItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ScribingToolsItem> SCRIBING_TOOLS = ITEMS.registerItem(
            "scribing_tools",
            ScribingToolsItem::new,
            new Item.Properties().stacksTo(1).durability(SCRIBING_TOOLS_DURABILITY));

    public static final DeferredItem<CelestialNotesItem> CELESTIAL_NOTES = ITEMS.registerItem(
            "celestial_notes",
            properties -> new CelestialNotesItem(
                    properties.component(TCDataComponents.CELESTIAL_BODY.get(), CelestialBody.SUN)),
            new Item.Properties());

    //

    public static final DeferredItem<PhialItem> PHIAL = ITEMS.registerItem("phial", PhialItem::new);

    public static final DeferredItem<ItemResearchNote> RESEARCH_NOTE =
            ITEMS.registerItem("research_note", ItemResearchNote::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<BlockItem> INFUSION_MATRIX =
            ITEMS.registerSimpleBlockItem(TCBlocks.INFUSION_MATRIX);
    public static final DeferredItem<BlockItem> PEDESTAL_ARCANE =
            ITEMS.registerSimpleBlockItem(TCBlocks.PEDESTAL_ARCANE);
    public static final DeferredItem<BlockItem> PEDESTAL_ANCIENT =
            ITEMS.registerSimpleBlockItem(TCBlocks.PEDESTAL_ANCIENT);
    public static final DeferredItem<BlockItem> PEDESTAL_ELDRITCH =
            ITEMS.registerSimpleBlockItem(TCBlocks.PEDESTAL_ELDRITCH);
    public static final DeferredItem<BlockItem> PILLAR_ARCANE = ITEMS.registerSimpleBlockItem(TCBlocks.PILLAR_ARCANE);
    public static final DeferredItem<BlockItem> PILLAR_ANCIENT = ITEMS.registerSimpleBlockItem(TCBlocks.PILLAR_ANCIENT);
    public static final DeferredItem<BlockItem> PILLAR_ELDRITCH =
            ITEMS.registerSimpleBlockItem(TCBlocks.PILLAR_ELDRITCH);

    public static final int FOCUS_LESSER_COMPLEXITY = 15;
    public static final int FOCUS_ADVANCED_COMPLEXITY = 25;
    public static final int FOCUS_GREATER_COMPLEXITY = 50;

    public static final DeferredItem<ItemWand> WAND = ITEMS.registerItem(
            "wand", ItemWand::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemWandCap> WAND_CAP_IRON =
            ITEMS.registerItem("wand_cap_iron", props -> new ItemWandCap(props, TCWandParts.CAP_IRON));
    public static final DeferredItem<ItemWandCap> WAND_CAP_COPPER =
            ITEMS.registerItem("wand_cap_copper", props -> new ItemWandCap(props, TCWandParts.CAP_COPPER));
    public static final DeferredItem<ItemWandCap> WAND_CAP_GOLD =
            ITEMS.registerItem("wand_cap_gold", props -> new ItemWandCap(props, TCWandParts.CAP_GOLD));
    public static final DeferredItem<Item> WAND_CAP_SILVER_INERT = ITEMS.registerSimpleItem("wand_cap_silver_inert");
    public static final DeferredItem<ItemWandCap> WAND_CAP_SILVER =
            ITEMS.registerItem("wand_cap_silver", props -> new ItemWandCap(props, TCWandParts.CAP_SILVER));
    public static final DeferredItem<Item> WAND_CAP_THAUMIUM_INERT =
            ITEMS.registerSimpleItem("wand_cap_thaumium_inert");
    public static final DeferredItem<ItemWandCap> WAND_CAP_THAUMIUM =
            ITEMS.registerItem("wand_cap_thaumium", props -> new ItemWandCap(props, TCWandParts.CAP_THAUMIUM));
    public static final DeferredItem<Item> WAND_CAP_VOID_INERT = ITEMS.registerSimpleItem("wand_cap_void_inert");
    public static final DeferredItem<ItemWandCap> WAND_CAP_VOID =
            ITEMS.registerItem("wand_cap_void", props -> new ItemWandCap(props, TCWandParts.CAP_VOID));

    public static final DeferredItem<ItemWandRod> WAND_ROD_GREATWOOD =
            ITEMS.registerItem("wand_rod_greatwood", props -> new ItemWandRod(props, TCWandParts.ROD_GREATWOOD));
    public static final DeferredItem<ItemWandRod> WAND_ROD_OBSIDIAN =
            ITEMS.registerItem("wand_rod_obsidian", props -> new ItemWandRod(props, TCWandParts.ROD_OBSIDIAN));
    public static final DeferredItem<ItemWandRod> WAND_ROD_BLAZE =
            ITEMS.registerItem("wand_rod_blaze", props -> new ItemWandRod(props, TCWandParts.ROD_BLAZE));
    public static final DeferredItem<ItemWandRod> WAND_ROD_ICE =
            ITEMS.registerItem("wand_rod_ice", props -> new ItemWandRod(props, TCWandParts.ROD_ICE));
    public static final DeferredItem<ItemWandRod> WAND_ROD_QUARTZ =
            ITEMS.registerItem("wand_rod_quartz", props -> new ItemWandRod(props, TCWandParts.ROD_QUARTZ));
    public static final DeferredItem<ItemWandRod> WAND_ROD_BONE =
            ITEMS.registerItem("wand_rod_bone", props -> new ItemWandRod(props, TCWandParts.ROD_BONE));
    public static final DeferredItem<ItemWandRod> WAND_ROD_REED =
            ITEMS.registerItem("wand_rod_reed", props -> new ItemWandRod(props, TCWandParts.ROD_REED));
    public static final DeferredItem<ItemWandRod> WAND_ROD_SILVERWOOD =
            ITEMS.registerItem("wand_rod_silverwood", props -> new ItemWandRod(props, TCWandParts.ROD_SILVERWOOD));

    public static final DeferredItem<ItemWandRod> STAFF_ROD_GREATWOOD =
            ITEMS.registerItem("staff_rod_greatwood", props -> new ItemWandRod(props, TCWandParts.STAFF_GREATWOOD));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_OBSIDIAN =
            ITEMS.registerItem("staff_rod_obsidian", props -> new ItemWandRod(props, TCWandParts.STAFF_OBSIDIAN));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_BLAZE =
            ITEMS.registerItem("staff_rod_blaze", props -> new ItemWandRod(props, TCWandParts.STAFF_BLAZE));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_ICE =
            ITEMS.registerItem("staff_rod_ice", props -> new ItemWandRod(props, TCWandParts.STAFF_ICE));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_QUARTZ =
            ITEMS.registerItem("staff_rod_quartz", props -> new ItemWandRod(props, TCWandParts.STAFF_QUARTZ));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_BONE =
            ITEMS.registerItem("staff_rod_bone", props -> new ItemWandRod(props, TCWandParts.STAFF_BONE));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_REED =
            ITEMS.registerItem("staff_rod_reed", props -> new ItemWandRod(props, TCWandParts.STAFF_REED));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_SILVERWOOD =
            ITEMS.registerItem("staff_rod_silverwood", props -> new ItemWandRod(props, TCWandParts.STAFF_SILVERWOOD));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_PRIMAL = ITEMS.registerItem(
            "staff_rod_primal",
            props -> new ItemWandRod(props, TCWandParts.STAFF_PRIMAL),
            new Item.Properties().rarity(Rarity.RARE));

    public static final DeferredItem<ItemPrimalCharm> PRIMAL_CHARM = ITEMS.registerItem(
            "primal_charm",
            ItemPrimalCharm::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> VIS_RELAY = ITEMS.registerSimpleBlockItem(TCBlocks.VIS_RELAY);
    public static final DeferredItem<BlockItem> NODE_STABILIZER =
            ITEMS.registerSimpleBlockItem(TCBlocks.NODE_STABILIZER);
    public static final DeferredItem<BlockItem> NODE_STABILIZER_ADVANCED =
            ITEMS.registerSimpleBlockItem(TCBlocks.NODE_STABILIZER_ADVANCED);
    public static final DeferredItem<BlockItem> NODE_TRANSDUCER =
            ITEMS.registerSimpleBlockItem(TCBlocks.NODE_TRANSDUCER);
    public static final DeferredItem<JarNodeItem> JAR_NODE =
            ITEMS.registerItem("jar_node", props -> new JarNodeItem(TCBlocks.JAR_NODE.get(), props));

    public static final DeferredItem<ItemFocus> FOCUS_1 = ITEMS.registerItem(
            "focus_1",
            props -> new ItemFocus(props, FOCUS_LESSER_COMPLEXITY),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<ItemFocus> FOCUS_2 = ITEMS.registerItem(
            "focus_2",
            props -> new ItemFocus(props, FOCUS_ADVANCED_COMPLEXITY),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<ItemFocus> FOCUS_3 = ITEMS.registerItem(
            "focus_3",
            props -> new ItemFocus(props, FOCUS_GREATER_COMPLEXITY),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<CausalityCollapserItem> CAUSALITY_COLLAPSER =
            ITEMS.registerItem("causality_collapser", CausalityCollapserItem::new, new Item.Properties().stacksTo(16));

    public static final DeferredItem<Item> VOID_SEED =
            ITEMS.registerItem("void_seed", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<PrimordialPearlItem> PRIMORDIAL_PEARL = ITEMS.registerItem(
            "primordial_pearl",
            PrimordialPearlItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).durability(PrimordialPearlItem.MAX_DAMAGE));

    //

    public static final int GOGGLES_DURABILITY = 350;

    public static final int GOGGLES_ENCHANTMENT_VALUE = 25;

    public static final ResourceLocation GOGGLES_REVEALING_ID = TCIds.rl("goggles_revealing");

    public static final DeferredItem<GogglesItem> GOGGLES_REVEALING = ITEMS.registerItem(
            "goggles_revealing",
            GogglesItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .durability(GOGGLES_DURABILITY)
                    .rarity(Rarity.RARE)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(
                                    Attributes.ARMOR,
                                    new AttributeModifier(
                                            GOGGLES_REVEALING_ID, 1, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.HEAD)
                            .add(
                                    Attributes.ARMOR_TOUGHNESS,
                                    new AttributeModifier(
                                            GOGGLES_REVEALING_ID, 1, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.HEAD)
                            .build()));

    public static final DeferredItem<FortressArmorItem> FORTRESS_HELM = ITEMS.registerItem(
            "fortress_helm",
            props -> new FortressArmorItem(
                    TCMaterials.ARMOR_FORTRESS,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(TCMaterials.DURABILITY_FORTRESS))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<FortressArmorItem> FORTRESS_CHEST = ITEMS.registerItem(
            "fortress_chest",
            props -> new FortressArmorItem(
                    TCMaterials.ARMOR_FORTRESS,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_FORTRESS))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<FortressArmorItem> FORTRESS_LEGS = ITEMS.registerItem(
            "fortress_legs",
            props -> new FortressArmorItem(
                    TCMaterials.ARMOR_FORTRESS,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_FORTRESS))
                            .rarity(Rarity.RARE)));

    public static final DeferredItem<VoidRobeArmorItem> VOID_ROBE_HELM = ITEMS.registerItem(
            "void_robe_helm",
            props -> new VoidRobeArmorItem(
                    TCMaterials.ARMOR_VOID_ROBE,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(TCMaterials.DURABILITY_VOID_ROBE))
                            .rarity(Rarity.EPIC)));
    public static final DeferredItem<VoidRobeArmorItem> VOID_ROBE_CHEST = ITEMS.registerItem(
            "void_robe_chest",
            props -> new VoidRobeArmorItem(
                    TCMaterials.ARMOR_VOID_ROBE,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_VOID_ROBE))
                            .rarity(Rarity.EPIC)));
    public static final DeferredItem<VoidRobeArmorItem> VOID_ROBE_LEGS = ITEMS.registerItem(
            "void_robe_legs",
            props -> new VoidRobeArmorItem(
                    TCMaterials.ARMOR_VOID_ROBE,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_VOID_ROBE))
                            .rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> CRIMSON_PRAETOR_HELM = ITEMS.registerItem(
            "crimson_praetor_helm",
            props -> new ArmorItem(
                    TCMaterials.ARMOR_CULTIST_LEADER,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorItem.Type.HELMET.getDurability(TCMaterials.DURABILITY_CULTIST_LEADER))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<Item> CRIMSON_PRAETOR_CHEST = ITEMS.registerItem(
            "crimson_praetor_chest",
            props -> new ArmorItem(
                    TCMaterials.ARMOR_CULTIST_LEADER,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorItem.Type.CHESTPLATE.getDurability(TCMaterials.DURABILITY_CULTIST_LEADER))
                            .rarity(Rarity.RARE)));
    public static final DeferredItem<Item> CRIMSON_PRAETOR_LEGS = ITEMS.registerItem(
            "crimson_praetor_legs",
            props -> new ArmorItem(
                    TCMaterials.ARMOR_CULTIST_LEADER,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorItem.Type.LEGGINGS.getDurability(TCMaterials.DURABILITY_CULTIST_LEADER))
                            .rarity(Rarity.RARE)));

    public static final int TRINKET_APPRENTICE_DISCOUNT = 5;
    private static final int AMULET_VIS_FOUND_INTERVAL = 100;
    private static final int AMULET_VIS_CRAFTED_INTERVAL = 20;

    public static final DeferredItem<TrinketItem> AMULET_MUNDANE =
            ITEMS.registerItem("amulet_mundane", TrinketItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<TrinketItem> RING_MUNDANE =
            ITEMS.registerItem("ring_mundane", TrinketItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<TrinketItem> GIRDLE_MUNDANE =
            ITEMS.registerItem("girdle_mundane", TrinketItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<TrinketItem> RING_APPRENTICE = ITEMS.registerItem(
            "ring_apprentice",
            props -> new TrinketItem(props, TRINKET_APPRENTICE_DISCOUNT),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<TrinketItem> AMULET_FANCY = ITEMS.registerItem(
            "amulet_fancy", TrinketItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<TrinketItem> RING_FANCY = ITEMS.registerItem(
            "ring_fancy", TrinketItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<TrinketItem> GIRDLE_FANCY = ITEMS.registerItem(
            "girdle_fancy", TrinketItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<AmuletVisItem> AMULET_VIS = ITEMS.registerItem(
            "amulet_vis",
            props -> new AmuletVisItem(props, AMULET_VIS_FOUND_INTERVAL),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<AmuletVisItem> AMULET_VIS_CRAFTED = ITEMS.registerItem(
            "amulet_vis_crafted",
            props -> new AmuletVisItem(props, AMULET_VIS_CRAFTED_INTERVAL),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CHARM_UNDYING = ITEMS.registerItem(
            "charm_undying", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CLOUD_RING = ITEMS.registerItem(
            "cloud_ring", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CURIOSITY_BAND = ITEMS.registerItem(
            "curiosity_band", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<VerdantCharmItem> VERDANT_CHARM = ITEMS.registerItem(
            "verdant_charm",
            VerdantCharmItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<VoidseerCharmItem> VOIDSEER_CHARM = ITEMS.registerItem(
            "voidseer_charm",
            VoidseerCharmItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<FocusPouchItem> FOCUS_POUCH = ITEMS.registerItem(
            "focus_pouch",
            FocusPouchItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BlockItem> ACTIVATOR_RAIL = ITEMS.registerSimpleBlockItem(TCBlocks.ACTIVATOR_RAIL);
    public static final DeferredItem<BlockItem> OBSIDIAN_TILE = ITEMS.registerSimpleBlockItem(TCBlocks.OBSIDIAN_TILE);
    public static final DeferredItem<BlockItem> OBSIDIAN_TOTEM = ITEMS.registerSimpleBlockItem(TCBlocks.OBSIDIAN_TOTEM);
    public static final DeferredItem<BlockItem> ELDRITCH_STONE = ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_STONE);
    public static final DeferredItem<BlockItem> ELDRITCH_STONE_INERT =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_STONE_INERT);
    public static final DeferredItem<BlockItem> ELDRITCH_ROCK = ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_ROCK);
    public static final DeferredItem<BlockItem> ELDRITCH_CRUST = ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_CRUST);
    public static final DeferredItem<BlockItem> ELDRITCH_CRUST_GLOWING =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_CRUST_GLOWING);
    public static final DeferredItem<BlockItem> STAIRS_ELDRITCH =
            ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_ELDRITCH);
    public static final DeferredItem<BlockItem> ELDRITCH_DOOR = ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_DOOR);
    public static final DeferredItem<BlockItem> ELDRITCH_PEDESTAL =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_PEDESTAL);
    public static final DeferredItem<BlockItem> ELDRITCH_STONE_CRYSTAL =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_STONE_CRYSTAL);
    public static final DeferredItem<BlockItem> ELDRITCH_LOCK = ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_LOCK);
    public static final DeferredItem<BlockItem> ELDRITCH_CRAB_SPAWNER =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_CRAB_SPAWNER);
    public static final DeferredItem<BlockItem> ELDRITCH_TRAP = ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_TRAP);
    public static final DeferredItem<BlockItem> ELDRITCH_ALTAR = ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_ALTAR);
    public static final DeferredItem<BlockItem> ELDRITCH_OBELISK =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_OBELISK);
    public static final DeferredItem<BlockItem> ELDRITCH_PILLAR =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_PILLAR);
    public static final DeferredItem<BlockItem> ELDRITCH_CAPSTONE =
            ITEMS.registerSimpleBlockItem(TCBlocks.ELDRITCH_CAPSTONE);
    public static final DeferredItem<Item> ELDRITCH_EYE =
            ITEMS.registerItem("eldritch_eye", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RUNED_TABLET =
            ITEMS.registerItem("runed_tablet", Item::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> SLAB_GREATWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.SLAB_GREATWOOD);
    public static final DeferredItem<BlockItem> SLAB_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.SLAB_SILVERWOOD);
    public static final DeferredItem<BlockItem> SLAB_ARCANE_STONE =
            ITEMS.registerSimpleBlockItem(TCBlocks.SLAB_ARCANE_STONE);
    public static final DeferredItem<BlockItem> SLAB_ARCANE_BRICK =
            ITEMS.registerSimpleBlockItem(TCBlocks.SLAB_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> SLAB_ANCIENT = ITEMS.registerSimpleBlockItem(TCBlocks.SLAB_ANCIENT);
    public static final DeferredItem<BlockItem> SLAB_ELDRITCH = ITEMS.registerSimpleBlockItem(TCBlocks.SLAB_ELDRITCH);
    public static final DeferredItem<BlockItem> STAIRS_GREATWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_GREATWOOD);
    public static final DeferredItem<BlockItem> STAIRS_SILVERWOOD =
            ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_SILVERWOOD);
    public static final DeferredItem<BlockItem> TABLE_WOOD = ITEMS.registerSimpleBlockItem(TCBlocks.TABLE_WOOD);
    public static final DeferredItem<BlockItem> TABLE_STONE = ITEMS.registerSimpleBlockItem(TCBlocks.TABLE_STONE);
    public static final DeferredItem<BlockItem> PAVING_STONE_TRAVEL =
            ITEMS.registerSimpleBlockItem(TCBlocks.PAVING_STONE_TRAVEL);
    public static final DeferredItem<BlockItem> PAVING_STONE_BARRIER =
            ITEMS.registerSimpleBlockItem(TCBlocks.PAVING_STONE_BARRIER);
    public static final DeferredItem<BlockItem> AMBER_BRICK = ITEMS.registerSimpleBlockItem(TCBlocks.AMBER_BRICK);
    public static final DeferredItem<BlockItem> FLESH_BLOCK = ITEMS.registerSimpleBlockItem(TCBlocks.FLESH_BLOCK);
    public static final DeferredItem<TurretPlacerItem> TURRET_BASIC = ITEMS.registerItem(
            "turret_basic",
            props -> new TurretPlacerItem(
                    props, level -> new EntityTurretCrossbow(TCEntities.TURRET_CROSSBOW.get(), level)),
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<TurretPlacerItem> TURRET_ADVANCED = ITEMS.registerItem(
            "turret_advanced",
            props -> new TurretPlacerItem(
                    props, level -> new EntityTurretCrossbowAdvanced(TCEntities.TURRET_CROSSBOW_ADVANCED.get(), level)),
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<TurretPlacerItem> TURRET_BORE = ITEMS.registerItem(
            "turret_bore",
            props -> new TurretPlacerItem(props, level -> new EntityArcaneBore(TCEntities.ARCANE_BORE.get(), level)),
            new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<GrappleGunItem> GRAPPLE_GUN = ITEMS.registerItem(
            "grapple_gun",
            GrappleGunItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> GRAPPLE_GUN_TIP = ITEMS.registerSimpleItem("grapple_gun_tip");
    public static final DeferredItem<Item> GRAPPLE_GUN_SPOOL = ITEMS.registerSimpleItem("grapple_gun_spool");
    public static final DeferredItem<Item> SANITY_CHECKER = ITEMS.registerItem(
            "sanity_checker", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<ItemResonator> RESONATOR = ITEMS.registerItem(
            "resonator", ItemResonator::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemCurio> CURIO_ARCANE = registerCurio("curio_arcane", ItemCurio.Variant.ARCANE);
    public static final DeferredItem<ItemCurio> CURIO_PRESERVED =
            registerCurio("curio_preserved", ItemCurio.Variant.PRESERVED);
    public static final DeferredItem<ItemCurio> CURIO_ANCIENT =
            registerCurio("curio_ancient", ItemCurio.Variant.ANCIENT);
    public static final DeferredItem<ItemCurio> CURIO_ELDRITCH =
            registerCurio("curio_eldritch", ItemCurio.Variant.ELDRITCH);
    public static final DeferredItem<ItemCurio> CURIO_KNOWLEDGE =
            registerCurio("curio_knowledge", ItemCurio.Variant.KNOWLEDGE);
    public static final DeferredItem<ItemCurio> CURIO_TWISTED =
            registerCurio("curio_twisted", ItemCurio.Variant.TWISTED);
    public static final DeferredItem<ItemCurio> CURIO_RITES = registerCurio("curio_rites", ItemCurio.Variant.RITES);

    public static final DeferredItem<ItemCreativeFluxSponge> CREATIVE_FLUX_SPONGE = ITEMS.registerItem(
            "creative_flux_sponge",
            ItemCreativeFluxSponge::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<ItemBlockMirror> MIRROR = ITEMS.registerItem(
            "mirror",
            props -> new ItemBlockMirror(TCBlocks.MIRROR.get(), props),
            new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemBlockMirror> MIRROR_ESSENTIA = ITEMS.registerItem(
            "mirror_essentia",
            props -> new ItemBlockMirror(TCBlocks.MIRROR_ESSENTIA.get(), props),
            new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemHandMirror> HAND_MIRROR = ITEMS.registerItem(
            "hand_mirror",
            ItemHandMirror::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    private static DeferredItem<ItemCurio> registerCurio(String name, ItemCurio.Variant variant) {
        return ITEMS.registerItem(
                name, props -> new ItemCurio(props, variant), new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    private TCItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static <T extends BlockItem> DeferredItem<BlockItem> registerSimpleBlockItem(
            Holder<Block> block, BiFunction<Block, Item.Properties, T> constructor) {
        return ITEMS.registerItem(
                block.unwrapKey().orElseThrow().location().getPath(),
                p -> constructor.apply(block.value(), p),
                new Item.Properties());
    }
}
