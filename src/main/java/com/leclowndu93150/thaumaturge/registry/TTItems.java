package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.content.spell.item.FocusItem;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DoubleHighBlockItem;
import com.leclowndu93150.thaumaturge.content.warding.ItemArcaneKey;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aura.node.CreativeNodePlacerItem;
import com.leclowndu93150.thaumaturge.content.aura.node.JarNodeItem;
import com.leclowndu93150.thaumaturge.content.casters.FocusPouchItem;
import com.leclowndu93150.thaumaturge.content.decor.CandleHolderMaterial;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreItem;
import com.leclowndu93150.thaumaturge.content.device.mirror.ItemBlockMirror;
import com.leclowndu93150.thaumaturge.content.device.mirror.ItemHandMirror;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructDeployment;
import com.leclowndu93150.thaumaturge.content.entity.construct.TurretPlacerItem;
import com.leclowndu93150.thaumaturge.content.equipment.CrimsonBladeItem;
import com.leclowndu93150.thaumaturge.content.equipment.CultistRobeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalAxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalHoeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalPickaxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalShovelItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalSpearItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalSwordItem;
import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.GrappleGunItem;
import com.leclowndu93150.thaumaturge.content.equipment.PrimalCrusherItem;
import com.leclowndu93150.thaumaturge.content.equipment.RobeArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.content.equipment.TravellerBootsItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidGearItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidHoeItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidRobeArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.AmuletVisItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.TrinketItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VerdantCharmItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VoidseerCharmItem;
import com.leclowndu93150.thaumaturge.content.equipment.hover.ThaumostaticHarnessItem;
import com.leclowndu93150.thaumaturge.content.essentia.ItemResonator;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarBraceItem;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarBrainItem;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarItem;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemBell;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemPlacer;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.golem.press.ItemGolemPress;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.ItemAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.ItemInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.entity.EntityBottleTaint;
import com.leclowndu93150.thaumaturge.content.entity.EntityCausalityCollapser;
import com.leclowndu93150.thaumaturge.content.item.ThrowableItem;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.item.SalisMundusItem;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.item.equipment.GogglesItem;
import com.leclowndu93150.thaumaturge.content.manabean.ItemManaBean;
import com.leclowndu93150.thaumaturge.content.misc.ItemCreativeFluxSponge;
import com.leclowndu93150.thaumaturge.content.misc.ItemCurio;
import com.leclowndu93150.thaumaturge.content.misc.alumentum.ThrownAlumentum;
import com.leclowndu93150.thaumaturge.content.pech.PechWandItem;
import com.leclowndu93150.thaumaturge.content.research.book.CheatThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.LinkingThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.SharingThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.ThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.note.ItemResearchNote;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TTIds.MODID);

    public static final DeferredItem<BlockItem> FOCAL_MANIPULATOR = ITEMS.registerSimpleBlockItem(TTBlocks.FOCAL_MANIPULATOR);
    public static final DeferredItem<BlockItem> RESEARCH_TABLE = ITEMS.registerSimpleBlockItem(TTBlocks.RESEARCH_TABLE);
    public static final DeferredItem<BlockItem> DECONSTRUCTION_TABLE = ITEMS.registerSimpleBlockItem(TTBlocks.DECONSTRUCTION_TABLE);

    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH = ITEMS.registerSimpleBlockItem(TTBlocks.ARCANE_WORKBENCH);

    public static final DeferredItem<BlockItem> CRUCIBLE = ITEMS.registerSimpleBlockItem(TTBlocks.CRUCIBLE);

    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH_CHARGER = ITEMS.registerSimpleBlockItem(TTBlocks.ARCANE_WORKBENCH_CHARGER);

    public static final DeferredItem<BlockItem> ALEMBIC = ITEMS.registerSimpleBlockItem(TTBlocks.ALEMBIC);

    public static final DeferredItem<BlockItem> BELLOWS = ITEMS.registerSimpleBlockItem(TTBlocks.BELLOWS);

    public static final DeferredItem<BlockItem> SMELTER_BASIC = ITEMS.registerSimpleBlockItem(TTBlocks.SMELTER_BASIC);

    public static final DeferredItem<BlockItem> SMELTER_THAUMIUM = ITEMS.registerSimpleBlockItem(TTBlocks.SMELTER_THAUMIUM);

    public static final DeferredItem<BlockItem> SMELTER_VOID = ITEMS.registerSimpleBlockItem(TTBlocks.SMELTER_VOID);
    public static final DeferredItem<BlockItem> ADVANCED_ALCHEMICAL_FURNACE = registerSimpleBlockItem(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE, ItemAdvancedAlchemicalFurnace::new);

    public static final DeferredItem<BlockItem> SMELTER_AUX = ITEMS.registerSimpleBlockItem(TTBlocks.SMELTER_AUX);

    public static final DeferredItem<BlockItem> SMELTER_VENT = ITEMS.registerSimpleBlockItem(TTBlocks.SMELTER_VENT);

    public static final DeferredItem<BlockItem> INFERNAL_FURNACE = registerSimpleBlockItem(TTBlocks.INFERNAL_FURNACE, ItemInfernalFurnace::new);

    public static final DeferredItem<BlockItem> JAR_NORMAL = registerSimpleBlockItem(TTBlocks.JAR_NORMAL, JarItem::new);

    public static final DeferredItem<BlockItem> JAR_VOID = registerSimpleBlockItem(TTBlocks.JAR_VOID, JarItem::new);

    public static final DeferredItem<BlockItem> TUBE = ITEMS.registerSimpleBlockItem(TTBlocks.TUBE);

    public static final DeferredItem<BlockItem> TUBE_VALVE = ITEMS.registerSimpleBlockItem(TTBlocks.TUBE_VALVE);

    public static final DeferredItem<BlockItem> TUBE_RESTRICT = ITEMS.registerSimpleBlockItem(TTBlocks.TUBE_RESTRICT);

    public static final DeferredItem<BlockItem> TUBE_FILTER = ITEMS.registerSimpleBlockItem(TTBlocks.TUBE_FILTER);

    public static final DeferredItem<BlockItem> TUBE_ONEWAY = ITEMS.registerSimpleBlockItem(TTBlocks.TUBE_ONEWAY);

    public static final DeferredItem<BlockItem> TUBE_BUFFER = ITEMS.registerSimpleBlockItem(TTBlocks.TUBE_BUFFER);

    public static final DeferredItem<BlockItem> TAINT_ROCK = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_ROCK);
    public static final DeferredItem<BlockItem> TAINT_SOIL = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_SOIL);
    public static final DeferredItem<BlockItem> TAINT_CRUST = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_CRUST);
    public static final DeferredItem<BlockItem> TAINT_GEYSER = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_GEYSER);
    public static final DeferredItem<BlockItem> TAINT_LOG = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_LOG);
    public static final DeferredItem<BlockItem> TAINT_FEATURE = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_FEATURE);
    public static final DeferredItem<BlockItem> TAINT_FIBRE = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_FIBRE);
    public static final DeferredItem<BlockItem> TAINT_SPORE_STALK = ITEMS.registerSimpleBlockItem(TTBlocks.TAINT_SPORE_STALK);

    public static final DeferredItem<BucketItem> BUCKET_LIQUID_DEATH = ITEMS.registerItem("liquid_death_bucket", props -> new BucketItem(TTFluids.LIQUID_DEATH_SOURCE.get(), props),
            props -> props.craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<BucketItem> BUCKET_PURIFYING = ITEMS.registerItem("purifying_bucket", props -> new BucketItem(TTFluids.PURIFYING_SOURCE.get(), props),
            props -> props.craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<JarBraceItem> JAR_BRACE = ITEMS.registerItem("jar_brace", JarBraceItem::new);

    public static final DeferredItem<LabelItem> LABEL = ITEMS.registerItem("label", LabelItem::new);

    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON = ITEMS.registerItem("thaumonomicon", ThaumonomiconItem::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<CheatThaumonomiconItem> THAUMONOMICON_CHEAT = ITEMS.registerItem("thaumonomicon_cheat", CheatThaumonomiconItem::new,
            props -> props.stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<SharingThaumonomiconItem> THAUMONOMICON_SHARING = ITEMS.registerItem("thaumonomicon_sharing", SharingThaumonomiconItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<LinkingThaumonomiconItem> THAUMONOMICON_LINKING = ITEMS.registerItem("thaumonomicon_linking", LinkingThaumonomiconItem::new,
            props -> props.stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<CreativeNodePlacerItem> CREATIVE_NODE_PLACER = ITEMS.registerItem("creative_node_placer", CreativeNodePlacerItem::new, props -> props.rarity(Rarity.EPIC));

    public static final DeferredItem<SalisMundusItem> SALIS_MUNDUS = ITEMS.registerItem("salis_mundus", SalisMundusItem::new, props -> props.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemEssentiaCrystal> ESSENTIA_CRYSTAL = ITEMS.registerItem("essentia_crystal", ItemEssentiaCrystal::new);

    public static final DeferredItem<Item> TAINTED_GOO = ITEMS.registerSimpleItem("tainted_goo");

    public static final DeferredItem<Item> TAINT_TENDRIL = ITEMS.registerSimpleItem("taint_tendril");

    public static final DeferredItem<ThrowableItem> BOTTLE_TAINT = ITEMS.registerItem("bottle_taint", props -> new ThrowableItem(props, EntityBottleTaint::new, EntityBottleTaint.THROW),
            props -> props.stacksTo(8).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> THAUMATORIUM = ITEMS.registerSimpleBlockItem(TTBlocks.THAUMATORIUM);
    public static final DeferredItem<BlockItem> BRAIN_BOX = ITEMS.registerSimpleBlockItem(TTBlocks.BRAIN_BOX);
    public static final DeferredItem<BlockItem> CONDENSER = ITEMS.registerSimpleBlockItem(TTBlocks.CONDENSER);
    public static final DeferredItem<BlockItem> CONDENSER_LATTICE = ITEMS.registerSimpleBlockItem(TTBlocks.CONDENSER_LATTICE);
    public static final DeferredItem<BlockItem> CONDENSER_LATTICE_DIRTY = ITEMS.registerSimpleBlockItem(TTBlocks.CONDENSER_LATTICE_DIRTY);
    public static final DeferredItem<BlockItem> STABILIZER = ITEMS.registerSimpleBlockItem(TTBlocks.STABILIZER);
    public static final DeferredItem<BlockItem> REDSTONE_RELAY = ITEMS.registerSimpleBlockItem(TTBlocks.REDSTONE_RELAY);
    public static final DeferredItem<BlockItem> VOID_SIPHON = ITEMS.registerSimpleBlockItem(TTBlocks.VOID_SIPHON);
    public static final DeferredItem<BlockItem> EVERFULL_URN = ITEMS.registerSimpleBlockItem(TTBlocks.EVERFULL_URN);
    public static final DeferredItem<BlockItem> VIS_GENERATOR = ITEMS.registerSimpleBlockItem(TTBlocks.VIS_GENERATOR);
    public static final DeferredItem<BlockItem> ESSENTIA_INPUT = ITEMS.registerSimpleBlockItem(TTBlocks.ESSENTIA_INPUT);
    public static final DeferredItem<BlockItem> ESSENTIA_OUTPUT = ITEMS.registerSimpleBlockItem(TTBlocks.ESSENTIA_OUTPUT);
    public static final DeferredItem<BlockItem> ARCANE_EAR = ITEMS.registerSimpleBlockItem(TTBlocks.ARCANE_EAR);
    public static final DeferredItem<BlockItem> ARCANE_EAR_TOGGLE = ITEMS.registerSimpleBlockItem(TTBlocks.ARCANE_EAR_TOGGLE);
    public static final DeferredItem<BlockItem> LAMP_ARCANE = ITEMS.registerSimpleBlockItem(TTBlocks.LAMP_ARCANE);
    public static final DeferredItem<BlockItem> LAMP_GROWTH = ITEMS.registerSimpleBlockItem(TTBlocks.LAMP_GROWTH);
    public static final DeferredItem<BlockItem> LAMP_FERTILITY = ITEMS.registerSimpleBlockItem(TTBlocks.LAMP_FERTILITY);
    public static final DeferredItem<BlockItem> CENTRIFUGE = ITEMS.registerSimpleBlockItem(TTBlocks.CENTRIFUGE);
    public static final DeferredItem<BlockItem> ESSENTIA_CRYSTALIZER = ITEMS.registerSimpleBlockItem(TTBlocks.ESSENTIA_CRYSTALIZER);
    public static final DeferredItem<BlockItem> ESSENTIA_RESERVOIR = ITEMS.registerSimpleBlockItem(TTBlocks.ESSENTIA_RESERVOIR);
    public static final DeferredItem<BlockItem> FLUX_SCRUBBER = ITEMS.registerSimpleBlockItem(TTBlocks.FLUX_SCRUBBER);
    public static final DeferredItem<BlockItem> HUNGRY_CHEST = ITEMS.registerSimpleBlockItem(TTBlocks.HUNGRY_CHEST);
    public static final DeferredItem<BlockItem> MATRIX_SPEED = ITEMS.registerSimpleBlockItem(TTBlocks.MATRIX_SPEED);
    public static final DeferredItem<BlockItem> MATRIX_COST = ITEMS.registerSimpleBlockItem(TTBlocks.MATRIX_COST);
    public static final DeferredItem<BlockItem> VIS_BATTERY = ITEMS.registerSimpleBlockItem(TTBlocks.VIS_BATTERY);
    public static final DeferredItem<BlockItem> DIOPTRA = ITEMS.registerSimpleBlockItem(TTBlocks.DIOPTRA);
    public static final DeferredItem<JarBrainItem> JAR_BRAIN = ITEMS.registerItem("jar_brain", props -> new JarBrainItem(TTBlocks.JAR_BRAIN.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> VIS_RESONATOR = ITEMS.registerSimpleItem("vis_resonator");

    public static final DeferredItem<Item> THAUMIUM_SWORD = ITEMS.registerItem("thaumium_sword", Item::new, props -> props.sword(TTMaterials.TOOL_THAUMIUM, 3.0F, -2.4F));
    public static final DeferredItem<Item> THAUMIUM_PICKAXE = ITEMS.registerItem("thaumium_pickaxe", Item::new, props -> props.pickaxe(TTMaterials.TOOL_THAUMIUM, 1.0F, -2.8F));
    public static final DeferredItem<Item> THAUMIUM_AXE = ITEMS.registerItem("thaumium_axe", Item::new, props -> props.axe(TTMaterials.TOOL_THAUMIUM, 4.5F, -3.0F));
    public static final DeferredItem<Item> THAUMIUM_SHOVEL = ITEMS.registerItem("thaumium_shovel", Item::new, props -> props.shovel(TTMaterials.TOOL_THAUMIUM, 1.5F, -3.0F));
    public static final DeferredItem<HoeItem> THAUMIUM_HOE = ITEMS.registerItem("thaumium_hoe", props -> new HoeItem(TTMaterials.TOOL_THAUMIUM, -3.0F, 0.0F, props));
    public static final DeferredItem<Item> THAUMIUM_SPEAR = ITEMS.registerItem("thaumium_spear", Item::new,
            props -> props.spear(TTMaterials.TOOL_THAUMIUM, 1.0F, 1.0F, 0.55F, 3.0F, 10.5F, 6.5F, 5.1F, 10.5F, 4.6F));
    public static final DeferredItem<Item> THAUMIUM_HELM = ITEMS.registerItem("thaumium_helm", Item::new, props -> props.humanoidArmor(TTMaterials.ARMOR_THAUMIUM, ArmorType.HELMET));
    public static final DeferredItem<Item> THAUMIUM_CHEST = ITEMS.registerItem("thaumium_chest", Item::new, props -> props.humanoidArmor(TTMaterials.ARMOR_THAUMIUM, ArmorType.CHESTPLATE));
    public static final DeferredItem<Item> THAUMIUM_LEGS = ITEMS.registerItem("thaumium_legs", Item::new, props -> props.humanoidArmor(TTMaterials.ARMOR_THAUMIUM, ArmorType.LEGGINGS));
    public static final DeferredItem<Item> THAUMIUM_BOOTS = ITEMS.registerItem("thaumium_boots", Item::new, props -> props.humanoidArmor(TTMaterials.ARMOR_THAUMIUM, ArmorType.BOOTS));

    public static final DeferredItem<VoidGearItem> VOID_SWORD = ITEMS.registerItem("void_sword", VoidGearItem::new, props -> props.sword(TTMaterials.TOOL_VOID, 3.0F, -2.4F));
    public static final DeferredItem<VoidGearItem> VOID_PICKAXE = ITEMS.registerItem("void_pickaxe", VoidGearItem::new, props -> props.pickaxe(TTMaterials.TOOL_VOID, 1.0F, -2.8F));
    public static final DeferredItem<VoidGearItem> VOID_AXE = ITEMS.registerItem("void_axe", VoidGearItem::new, props -> props.axe(TTMaterials.TOOL_VOID, 4.0F, -3.0F));
    public static final DeferredItem<VoidGearItem> VOID_SHOVEL = ITEMS.registerItem("void_shovel", VoidGearItem::new, props -> props.shovel(TTMaterials.TOOL_VOID, 1.5F, -3.0F));
    public static final DeferredItem<VoidHoeItem> VOID_HOE = ITEMS.registerItem("void_hoe", props -> new VoidHoeItem(TTMaterials.TOOL_VOID, -3.0F, 0.0F, props));
    public static final DeferredItem<VoidGearItem> VOID_SPEAR = ITEMS.registerItem("void_spear", VoidGearItem::new,
            props -> props.spear(TTMaterials.TOOL_VOID, 1.05F, 1.075F, 0.5F, 3.0F, 10.0F, 6.5F, 5.1F, 10.0F, 4.6F));
    public static final DeferredItem<VoidGearItem> VOID_HELM = ITEMS.registerItem("void_helm", VoidGearItem::new, props -> props.humanoidArmor(TTMaterials.ARMOR_VOID, ArmorType.HELMET));
    public static final DeferredItem<VoidGearItem> VOID_CHEST = ITEMS.registerItem("void_chest", VoidGearItem::new, props -> props.humanoidArmor(TTMaterials.ARMOR_VOID, ArmorType.CHESTPLATE));
    public static final DeferredItem<VoidGearItem> VOID_LEGS = ITEMS.registerItem("void_legs", VoidGearItem::new, props -> props.humanoidArmor(TTMaterials.ARMOR_VOID, ArmorType.LEGGINGS));
    public static final DeferredItem<VoidGearItem> VOID_BOOTS = ITEMS.registerItem("void_boots", VoidGearItem::new, props -> props.humanoidArmor(TTMaterials.ARMOR_VOID, ArmorType.BOOTS));

    public static final DeferredItem<ElementalSwordItem> ELEMENTAL_SWORD = ITEMS.registerItem("elemental_sword", ElementalSwordItem::new,
            props -> props.sword(TTMaterials.TOOL_ELEMENTAL, 3.0F, -2.4F).rarity(Rarity.RARE));
    public static final DeferredItem<ElementalPickaxeItem> ELEMENTAL_PICKAXE = ITEMS.registerItem("elemental_pickaxe", ElementalPickaxeItem::new,
            props -> props.pickaxe(TTMaterials.TOOL_ELEMENTAL, 1.0F, -2.8F).rarity(Rarity.RARE));
    public static final DeferredItem<ElementalAxeItem> ELEMENTAL_AXE = ITEMS.registerItem("elemental_axe", ElementalAxeItem::new,
            props -> props.axe(TTMaterials.TOOL_ELEMENTAL, 5.0F, -3.0F).rarity(Rarity.RARE));
    public static final DeferredItem<ElementalShovelItem> ELEMENTAL_SHOVEL = ITEMS.registerItem("elemental_shovel", ElementalShovelItem::new,
            props -> props.shovel(TTMaterials.TOOL_ELEMENTAL, 1.5F, -3.0F).rarity(Rarity.RARE));
    public static final DeferredItem<ElementalHoeItem> ELEMENTAL_HOE = ITEMS.registerItem("elemental_hoe", props -> new ElementalHoeItem(TTMaterials.TOOL_ELEMENTAL, -3.0F, 0.0F, props),
            props -> props.rarity(Rarity.RARE));
    public static final DeferredItem<ElementalSpearItem> ELEMENTAL_SPEAR = ITEMS.registerItem("elemental_spear", ElementalSpearItem::new,
            props -> props.spear(TTMaterials.TOOL_ELEMENTAL, 1.1F, 1.15F, 0.45F, 2.75F, 9.5F, 6.0F, 5.1F, 9.5F, 4.6F).rarity(Rarity.RARE));
    public static final DeferredItem<PrimalCrusherItem> PRIMAL_CRUSHER = ITEMS.registerItem("primal_crusher", PrimalCrusherItem::new,
            props -> props.pickaxe(TTMaterials.TOOL_PRIMAL_VOID, 3.5F, -2.8F).rarity(Rarity.RARE));

    public static final DeferredItem<CrimsonBladeItem> CRIMSON_BLADE = ITEMS.registerItem("crimson_blade", CrimsonBladeItem::new,
            props -> props.sword(TTMaterials.TOOL_CRIMSON_VOID, 3.0F, -2.4F).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> CRIMSON_PLATE_HELM = ITEMS.registerItem("crimson_plate_helm", Item::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_PLATE, ArmorType.HELMET).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CRIMSON_PLATE_CHEST = ITEMS.registerItem("crimson_plate_chest", Item::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_PLATE, ArmorType.CHESTPLATE).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CRIMSON_PLATE_LEGS = ITEMS.registerItem("crimson_plate_legs", Item::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_PLATE, ArmorType.LEGGINGS).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CultistRobeItem> CRIMSON_BOOTS = ITEMS.registerItem("crimson_boots", CultistRobeItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_BOOTS, ArmorType.BOOTS).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CultistRobeItem> CRIMSON_ROBE_HELM = ITEMS.registerItem("crimson_robe_helm", CultistRobeItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_ROBE, ArmorType.HELMET).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CultistRobeItem> CRIMSON_ROBE_CHEST = ITEMS.registerItem("crimson_robe_chest", CultistRobeItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_ROBE, ArmorType.CHESTPLATE).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CultistRobeItem> CRIMSON_ROBE_LEGS = ITEMS.registerItem("crimson_robe_legs", CultistRobeItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_ROBE, ArmorType.LEGGINGS).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> RECHARGE_PEDESTAL = ITEMS.registerSimpleBlockItem(TTBlocks.RECHARGE_PEDESTAL);

    public static final DeferredItem<BlockItem> LEVITATOR = ITEMS.registerSimpleBlockItem(TTBlocks.LEVITATOR);
    public static final DeferredItem<BlockItem> ITEM_GRATE = ITEMS.registerSimpleBlockItem(TTBlocks.ITEM_GRATE);
    public static final DeferredItem<BlockItem> GOLEM_FETTER = ITEMS.registerSimpleBlockItem(TTBlocks.GOLEM_FETTER);
    public static final DeferredItem<BlockItem> TALLOW_BLOCK = ITEMS.registerSimpleBlockItem(TTBlocks.TALLOW_BLOCK);
    public static final DeferredItem<BlockItem> WARDED_GLASS = ITEMS.registerSimpleBlockItem(TTBlocks.WARDED_GLASS);
    public static final DeferredItem<DoubleHighBlockItem> ARCANE_DOOR = ITEMS.registerItem("arcane_door",
            props -> new DoubleHighBlockItem(TTBlocks.ARCANE_DOOR.get(), props.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> ARCANE_PRESSURE_PLATE = ITEMS.registerSimpleBlockItem(TTBlocks.ARCANE_PRESSURE_PLATE);
    public static final DeferredItem<ItemArcaneKey> ARCANE_KEY_IRON = ITEMS.registerItem("arcane_key_iron", props -> new ItemArcaneKey(props, false), props -> props.stacksTo(16));
    public static final DeferredItem<ItemArcaneKey> ARCANE_KEY_GOLD = ITEMS.registerItem("arcane_key_gold", props -> new ItemArcaneKey(props, true), props -> props.stacksTo(16));
    public static final DeferredItem<BlockItem> POTION_SPRAYER = ITEMS.registerSimpleBlockItem(TTBlocks.POTION_SPRAYER);
    public static final DeferredItem<BlockItem> PATTERN_CRAFTER = ITEMS.registerSimpleBlockItem(TTBlocks.PATTERN_CRAFTER);
    public static final DeferredItem<BlockItem> INLAY = ITEMS.registerSimpleBlockItem(TTBlocks.INLAY);

    public static final DeferredItem<BlockItem> GOLEM_BUILDER = registerSimpleBlockItem(TTBlocks.GOLEM_BUILDER, ItemGolemPress::new);

    public static final DeferredItem<ItemGolemPlacer> GOLEM_PLACER = ITEMS.registerItem("golem", ItemGolemPlacer::new);
    public static final DeferredItem<ItemGolemBell> GOLEM_BELL = ITEMS.registerItem("golem_bell", ItemGolemBell::new, props -> props.stacksTo(1));

    public static final DeferredItem<Item> GOLEM_TOP_HAT = ITEMS.registerSimpleItem("golem_top_hat");
    public static final DeferredItem<Item> GOLEM_FEZ = ITEMS.registerSimpleItem("golem_fez");
    public static final DeferredItem<Item> GOLEM_GLASSES = ITEMS.registerSimpleItem("golem_glasses");
    public static final DeferredItem<Item> GOLEM_BOWTIE = ITEMS.registerSimpleItem("golem_bowtie");
    public static final DeferredItem<Item> GOLEM_VISOR = ITEMS.registerSimpleItem("golem_visor");

    public static final DeferredItem<ItemSealPlacer> SEAL_BLANK = sealItem("seal_blank", null);
    public static final DeferredItem<ItemSealPlacer> SEAL_PICKUP = sealItem("seal_pickup", "pickup");
    public static final DeferredItem<ItemSealPlacer> SEAL_PICKUP_ADVANCED = sealItem("seal_pickup_advanced", "pickup_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_FILL = sealItem("seal_fill", "fill");
    public static final DeferredItem<ItemSealPlacer> SEAL_FILL_ADVANCED = sealItem("seal_fill_advanced", "fill_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_EMPTY = sealItem("seal_empty", "empty");
    public static final DeferredItem<ItemSealPlacer> SEAL_EMPTY_ADVANCED = sealItem("seal_empty_advanced", "empty_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_HARVEST = sealItem("seal_harvest", "harvest");
    public static final DeferredItem<ItemSealPlacer> SEAL_BUTCHER = sealItem("seal_butcher", "butcher");
    public static final DeferredItem<ItemSealPlacer> SEAL_GUARD = sealItem("seal_guard", "guard");
    public static final DeferredItem<ItemSealPlacer> SEAL_GUARD_ADVANCED = sealItem("seal_guard_advanced", "guard_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_LUMBER = sealItem("seal_lumber", "lumber");
    public static final DeferredItem<ItemSealPlacer> SEAL_BREAKER = sealItem("seal_breaker", "breaker");
    public static final DeferredItem<ItemSealPlacer> SEAL_BREAKER_ADVANCED = sealItem("seal_breaker_advanced", "breaker_advanced");
    public static final DeferredItem<ItemSealPlacer> SEAL_USE = sealItem("seal_use", "use");
    public static final DeferredItem<ItemSealPlacer> SEAL_PROVIDER = sealItem("seal_provider", "provider");
    public static final DeferredItem<ItemSealPlacer> SEAL_STOCK = sealItem("seal_stock", "stock");

    private static DeferredItem<ItemSealPlacer> sealItem(String id, String sealPath) {
        return ITEMS.registerItem(id, props -> new ItemSealPlacer(sealPath == null ? null : TTIds.rl(sealPath), props));
    }

    public static final DeferredItem<TravellerBootsItem> TRAVELLER_BOOTS = ITEMS.registerItem("traveller_boots", TravellerBootsItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_TRAVELLER, ArmorType.BOOTS).durability(350).rarity(Rarity.RARE));

    public static final DeferredItem<ThaumostaticHarnessItem> THAUMOSTATIC_HARNESS = ITEMS.registerItem("thaumostatic_harness", ThaumostaticHarnessItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_THAUMOSTATIC_HARNESS, ArmorType.CHESTPLATE).rarity(Rarity.EPIC));

    public static final DeferredItem<RobeArmorItem> CLOTH_CHEST = ITEMS.registerItem("cloth_chest", props -> new RobeArmorItem(3, props),
            props -> props.humanoidArmor(TTMaterials.ARMOR_ROBES, ArmorType.CHESTPLATE));
    public static final DeferredItem<RobeArmorItem> CLOTH_LEGS = ITEMS.registerItem("cloth_legs", props -> new RobeArmorItem(3, props),
            props -> props.humanoidArmor(TTMaterials.ARMOR_ROBES, ArmorType.LEGGINGS));
    public static final DeferredItem<RobeArmorItem> CLOTH_BOOTS = ITEMS.registerItem("cloth_boots", props -> new RobeArmorItem(2, props),
            props -> props.humanoidArmor(TTMaterials.ARMOR_ROBES, ArmorType.BOOTS));

    public static final DeferredItem<ItemManaBean> MANA_BEAN = ITEMS.registerItem("mana_bean", ItemManaBean::new,
            props -> props.food(new FoodProperties(1, 0.5F, true), Consumables.defaultFood().consumeSeconds(0.5F).build()));

    public static final DeferredItem<Item> BRAIN = ITEMS.registerItem("brain", Item::new, props -> props.food(new FoodProperties(4, 0.2F, true),
            Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8F)).build()));

    public static final DeferredItem<SpawnEggItem> BRAINY_ZOMBIE_SPAWN_EGG = registerSpawnEgg("brainy_zombie_spawn_egg", TTEntities.BRAINY_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> GIANT_BRAINY_ZOMBIE_SPAWN_EGG = registerSpawnEgg("giant_brainy_zombie_spawn_egg", TTEntities.GIANT_BRAINY_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> BRAINY_DROWNED_SPAWN_EGG = registerSpawnEgg("brainy_drowned_spawn_egg", TTEntities.BRAINY_DROWNED);
    public static final DeferredItem<SpawnEggItem> BRAINY_HUSK_SPAWN_EGG = registerSpawnEgg("brainy_husk_spawn_egg", TTEntities.BRAINY_HUSK);
    public static final DeferredItem<SpawnEggItem> FIREBAT_SPAWN_EGG = registerSpawnEgg("firebat_spawn_egg", TTEntities.FIRE_BAT);
    public static final DeferredItem<SpawnEggItem> MIND_SPIDER_SPAWN_EGG = registerSpawnEgg("mind_spider_spawn_egg", TTEntities.MIND_SPIDER);
    public static final DeferredItem<SpawnEggItem> WISP_SPAWN_EGG = registerSpawnEgg("wisp_spawn_egg", TTEntities.WISP);
    public static final DeferredItem<SpawnEggItem> THAUMIC_SLIME_SPAWN_EGG = registerSpawnEgg("thaumic_slime_spawn_egg", TTEntities.THAUMIC_SLIME);
    public static final DeferredItem<SpawnEggItem> TAINT_CRAWLER_SPAWN_EGG = registerSpawnEgg("taint_crawler_spawn_egg", TTEntities.TAINT_CRAWLER);
    public static final DeferredItem<SpawnEggItem> TAINTACLE_SPAWN_EGG = registerSpawnEgg("taintacle_spawn_egg", TTEntities.TAINTACLE);
    public static final DeferredItem<SpawnEggItem> TAINT_SWARM_SPAWN_EGG = registerSpawnEgg("taint_swarm_spawn_egg", TTEntities.TAINT_SWARM);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_WARDEN_SPAWN_EGG = registerSpawnEgg("eldritch_warden_spawn_egg", TTEntities.ELDRITCH_WARDEN);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_GOLEM_SPAWN_EGG = registerSpawnEgg("eldritch_golem_spawn_egg", TTEntities.ELDRITCH_GOLEM);
    public static final DeferredItem<SpawnEggItem> CULTIST_LEADER_SPAWN_EGG = registerSpawnEgg("cultist_leader_spawn_egg", TTEntities.CULTIST_LEADER);
    public static final DeferredItem<SpawnEggItem> CULTIST_PORTAL_GREATER_SPAWN_EGG = registerSpawnEgg("cultist_portal_greater_spawn_egg", TTEntities.CULTIST_PORTAL_GREATER);
    public static final DeferredItem<SpawnEggItem> TAINTACLE_GIANT_SPAWN_EGG = registerSpawnEgg("taintacle_giant_spawn_egg", TTEntities.TAINTACLE_GIANT);
    public static final DeferredItem<SpawnEggItem> TAINT_SEED_SPAWN_EGG = registerSpawnEgg("taint_seed_spawn_egg", TTEntities.TAINT_SEED);
    public static final DeferredItem<SpawnEggItem> TAINT_SEED_PRIME_SPAWN_EGG = registerSpawnEgg("taint_seed_prime_spawn_egg", TTEntities.TAINT_SEED_PRIME);
    public static final DeferredItem<SpawnEggItem> PECH_SPAWN_EGG = registerSpawnEgg("pech_spawn_egg", TTEntities.PECH);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_CRAB_SPAWN_EGG = registerSpawnEgg("eldritch_crab_spawn_egg", TTEntities.ELDRITCH_CRAB);
    public static final DeferredItem<SpawnEggItem> INHABITED_ZOMBIE_SPAWN_EGG = registerSpawnEgg("inhabited_zombie_spawn_egg", TTEntities.INHABITED_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> ELDRITCH_GUARDIAN_SPAWN_EGG = registerSpawnEgg("eldritch_guardian_spawn_egg", TTEntities.ELDRITCH_GUARDIAN);
    public static final DeferredItem<SpawnEggItem> CULTIST_KNIGHT_SPAWN_EGG = registerSpawnEgg("cultist_knight_spawn_egg", TTEntities.CULTIST_KNIGHT);
    public static final DeferredItem<SpawnEggItem> CULTIST_CLERIC_SPAWN_EGG = registerSpawnEgg("cultist_cleric_spawn_egg", TTEntities.CULTIST_CLERIC);
    public static final DeferredItem<SpawnEggItem> CULTIST_PORTAL_LESSER_SPAWN_EGG = registerSpawnEgg("cultist_portal_lesser_spawn_egg", TTEntities.CULTIST_PORTAL_LESSER);

    public static final DeferredItem<PechWandItem> PECH_WAND = ITEMS.registerItem("pech_wand", PechWandItem::new, props -> props.rarity(Rarity.RARE));

    public static final DeferredItem<LootBagItem> LOOT_BAG_COMMON = ITEMS.registerItem("loot_bag_common", props -> new LootBagItem(TTLootTables.LOOT_BAG_COMMON, props.stacksTo(16)));
    public static final DeferredItem<LootBagItem> LOOT_BAG_UNCOMMON = ITEMS.registerItem("loot_bag_uncommon",
            props -> new LootBagItem(TTLootTables.LOOT_BAG_UNCOMMON, props.stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LootBagItem> LOOT_BAG_RARE = ITEMS.registerItem("loot_bag_rare", props -> new LootBagItem(TTLootTables.LOOT_BAG_RARE, props.stacksTo(16).rarity(Rarity.RARE)));

    public static final DeferredItem<BlockItem> LOOT_URN_COMMON = ITEMS.registerSimpleBlockItem(TTBlocks.LOOT_URN_COMMON);
    public static final DeferredItem<BlockItem> LOOT_URN_UNCOMMON = ITEMS.registerSimpleBlockItem(TTBlocks.LOOT_URN_UNCOMMON);
    public static final DeferredItem<BlockItem> LOOT_URN_RARE = ITEMS.registerSimpleBlockItem(TTBlocks.LOOT_URN_RARE);
    public static final DeferredItem<BlockItem> LOOT_CRATE_COMMON = ITEMS.registerSimpleBlockItem(TTBlocks.LOOT_CRATE_COMMON);
    public static final DeferredItem<BlockItem> LOOT_CRATE_UNCOMMON = ITEMS.registerSimpleBlockItem(TTBlocks.LOOT_CRATE_UNCOMMON);
    public static final DeferredItem<BlockItem> LOOT_CRATE_RARE = ITEMS.registerSimpleBlockItem(TTBlocks.LOOT_CRATE_RARE);

    private static DeferredItem<SpawnEggItem> registerSpawnEgg(String name, Supplier<? extends EntityType<?>> type) {
        return ITEMS.registerItem(name, properties -> new SpawnEggItem(properties.spawnEgg(type.get())));
    }

    public static final DeferredItem<Item> TALLOW = ITEMS.registerSimpleItem("tallow");

    public static final Map<DyeColor, DeferredItem<BlockItem>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(dye, ITEMS.registerSimpleBlockItem(TTBlocks.CANDLES.get(dye)));
        }
    }

    public static final Map<CandleHolderMaterial, DeferredItem<BlockItem>> CANDLE_HOLDERS = new EnumMap<>(CandleHolderMaterial.class);

    static {
        for (CandleHolderMaterial material : CandleHolderMaterial.values()) {
            CANDLE_HOLDERS.put(material, ITEMS.registerSimpleBlockItem(TTBlocks.CANDLE_HOLDERS.get(material)));
        }
    }

    public static final Map<DyeColor, DeferredItem<StandingAndWallBlockItem>> BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(dye, ITEMS.registerItem("banner_" + dye.getName(),
                    props -> new StandingAndWallBlockItem(TTBlocks.BANNERS.get(dye).get(), TTBlocks.WALL_BANNERS.get(dye).get(), Direction.DOWN, props.stacksTo(16).useBlockDescriptionPrefix())));
        }
    }

    public static final DeferredItem<StandingAndWallBlockItem> BANNER_CRIMSON_CULT = ITEMS.registerItem("banner_crimson_cult",
            props -> new StandingAndWallBlockItem(TTBlocks.BANNER_CRIMSON_CULT.get(), TTBlocks.WALL_BANNER_CRIMSON_CULT.get(), Direction.DOWN, props.stacksTo(16).useBlockDescriptionPrefix()));

    public static final Map<DyeColor, DeferredItem<BlockItem>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, ITEMS.registerSimpleBlockItem(TTBlocks.NITORS.get(dye)));
        }
    }

    public static final DeferredItem<BlockItem> SPA = ITEMS.registerSimpleBlockItem(TTBlocks.SPA);

    public static final DeferredItem<ThrowableItem> ALUMENTUM = ITEMS.registerItem("alumentum", props -> new ThrowableItem(props, ThrownAlumentum::new, ThrownAlumentum.THROW));

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
    public static final DeferredItem<Item> BATH_SALTS = ITEMS.registerItem("bath_salts", Item::new);
    public static final DeferredItem<ItemSanitySoap> SANITY_SOAP = ITEMS.registerItem("sanity_soap", ItemSanitySoap::new);

    public static final DeferredItem<Item> CHUNK_BEEF = registerChunk("chunk_beef");
    public static final DeferredItem<Item> CHUNK_CHICKEN = registerChunk("chunk_chicken");
    public static final DeferredItem<Item> CHUNK_PORK = registerChunk("chunk_pork");
    public static final DeferredItem<Item> CHUNK_FISH = registerChunk("chunk_fish");
    public static final DeferredItem<Item> CHUNK_RABBIT = registerChunk("chunk_rabbit");
    public static final DeferredItem<Item> CHUNK_MUTTON = registerChunk("chunk_mutton");

    public static final DeferredItem<Item> TRIPLE_MEAT_TREAT = ITEMS.registerItem("triple_meat_treat",
            props -> new Item(props.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).alwaysEdible().build(),
                    Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66F)).build())));

    private static DeferredItem<Item> registerChunk(String id) {
        return ITEMS.registerItem(id,
                props -> new Item(props.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build(), Consumables.defaultFood().consumeSeconds(0.5F).build())));
    }

    public static final DeferredItem<BlockItem> CRYSTAL_AER = ITEMS.registerSimpleBlockItem(TTBlocks.CRYSTAL_AER);
    public static final DeferredItem<BlockItem> CRYSTAL_IGNIS = ITEMS.registerSimpleBlockItem(TTBlocks.CRYSTAL_IGNIS);
    public static final DeferredItem<BlockItem> CRYSTAL_AQUA = ITEMS.registerSimpleBlockItem(TTBlocks.CRYSTAL_AQUA);
    public static final DeferredItem<BlockItem> CRYSTAL_TERRA = ITEMS.registerSimpleBlockItem(TTBlocks.CRYSTAL_TERRA);
    public static final DeferredItem<BlockItem> CRYSTAL_ORDO = ITEMS.registerSimpleBlockItem(TTBlocks.CRYSTAL_ORDO);
    public static final DeferredItem<BlockItem> CRYSTAL_PERDITIO = ITEMS.registerSimpleBlockItem(TTBlocks.CRYSTAL_PERDITIO);
    public static final DeferredItem<BlockItem> CRYSTAL_VITIUM = ITEMS.registerSimpleBlockItem(TTBlocks.CRYSTAL_VITIUM);

    public static final DeferredItem<BlockItem> STONE_ARCANE = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ARCANE);
    public static final DeferredItem<BlockItem> STONE_ARCANE_BRICK = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ANCIENT);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_TILE = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ANCIENT_TILE);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_ROCK = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ANCIENT_ROCK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_GLYPHED = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ANCIENT_GLYPHED);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_DOORWAY = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ANCIENT_DOORWAY);
    public static final DeferredItem<BlockItem> STONE_ELDRITCH_TILE = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_ELDRITCH_TILE);
    public static final DeferredItem<BlockItem> STONE_POROUS = ITEMS.registerSimpleBlockItem(TTBlocks.STONE_POROUS);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE = ITEMS.registerSimpleBlockItem(TTBlocks.STAIRS_ARCANE);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE_BRICK = ITEMS.registerSimpleBlockItem(TTBlocks.STAIRS_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STAIRS_ANCIENT = ITEMS.registerSimpleBlockItem(TTBlocks.STAIRS_ANCIENT);

    public static final DeferredItem<BlockItem> SAPLING_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.SAPLING_GREATWOOD);
    public static final DeferredItem<BlockItem> SAPLING_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.SAPLING_SILVERWOOD);
    public static final DeferredItem<BlockItem> LOG_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.LOG_GREATWOOD);
    public static final DeferredItem<BlockItem> WOOD_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.WOOD_GREATWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_LOG_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.STRIPPED_LOG_GREATWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_WOOD_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.STRIPPED_WOOD_GREATWOOD);
    public static final DeferredItem<BlockItem> LOG_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.LOG_SILVERWOOD);
    public static final DeferredItem<BlockItem> WOOD_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.WOOD_SILVERWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_LOG_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.STRIPPED_LOG_SILVERWOOD);
    public static final DeferredItem<BlockItem> STRIPPED_WOOD_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.STRIPPED_WOOD_SILVERWOOD);
    public static final DeferredItem<BlockItem> LEAVES_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.LEAVES_GREATWOOD);
    public static final DeferredItem<BlockItem> LEAVES_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.LEAVES_SILVERWOOD);
    public static final DeferredItem<BlockItem> PLANK_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.PLANK_GREATWOOD);
    public static final DeferredItem<BlockItem> PLANK_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.PLANK_SILVERWOOD);

    public static final DeferredItem<BlockItem> PLANT_SHIMMERLEAF = ITEMS.registerSimpleBlockItem(TTBlocks.PLANT_SHIMMERLEAF);
    public static final DeferredItem<BlockItem> ETHEREAL_BLOOM = ITEMS.registerSimpleBlockItem(TTBlocks.ETHEREAL_BLOOM);
    public static final DeferredItem<BlockItem> PLANT_CINDERPEARL = ITEMS.registerSimpleBlockItem(TTBlocks.PLANT_CINDERPEARL);
    public static final DeferredItem<BlockItem> PLANT_VISHROOM = ITEMS.registerSimpleBlockItem(TTBlocks.PLANT_VISHROOM);
    public static final DeferredItem<BlockItem> GRASS_AMBIENT = ITEMS.registerSimpleBlockItem(TTBlocks.GRASS_AMBIENT);

    public static final DeferredItem<BlockItem> ORE_AMBER = ITEMS.registerSimpleBlockItem(TTBlocks.ORE_AMBER);
    public static final DeferredItem<BlockItem> ORE_CINNABAR = ITEMS.registerSimpleBlockItem(TTBlocks.ORE_CINNABAR);
    public static final DeferredItem<BlockItem> ORE_QUARTZ = ITEMS.registerSimpleBlockItem(TTBlocks.ORE_QUARTZ);
    public static final DeferredItem<BlockItem> DEEPSLATE_ORE_AMBER = ITEMS.registerSimpleBlockItem(TTBlocks.DEEPSLATE_ORE_AMBER);
    public static final DeferredItem<BlockItem> DEEPSLATE_ORE_CINNABAR = ITEMS.registerSimpleBlockItem(TTBlocks.DEEPSLATE_ORE_CINNABAR);
    public static final DeferredItem<BlockItem> DEEPSLATE_ORE_QUARTZ = ITEMS.registerSimpleBlockItem(TTBlocks.DEEPSLATE_ORE_QUARTZ);

    public static final DeferredItem<BlockItem> ALCHEMICAL_CONSTRUCT = ITEMS.registerSimpleBlockItem(TTBlocks.ALCHEMICAL_CONSTRUCT);
    public static final DeferredItem<BlockItem> ADVANCED_ALCHEMICAL_CONSTRUCT = ITEMS.registerSimpleBlockItem(TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT);

    public static final DeferredItem<BlockItem> METAL_THAUMIUM_BLOCK = ITEMS.registerSimpleBlockItem(TTBlocks.METAL_THAUMIUM_BLOCK);
    public static final DeferredItem<BlockItem> METAL_BRASS_BLOCK = ITEMS.registerSimpleBlockItem(TTBlocks.METAL_BRASS_BLOCK);
    public static final DeferredItem<BlockItem> METAL_VOID_BLOCK = ITEMS.registerSimpleBlockItem(TTBlocks.METAL_VOID_BLOCK);
    public static final DeferredItem<BlockItem> AMBER_BLOCK = ITEMS.registerSimpleBlockItem(TTBlocks.AMBER_BLOCK);

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
    public static final DeferredItem<Item> RAW_CINNABAR = ITEMS.registerSimpleItem("raw_cinnabar");
    public static final DeferredItem<Item> CLUSTER_CINNABAR = ITEMS.registerSimpleItem("cluster_cinnabar");
    public static final DeferredItem<Item> CLUSTER_QUARTZ = ITEMS.registerSimpleItem("cluster_quartz");

    public static final int SCRIBING_TOOLS_DURABILITY = 100;

    public static final DeferredItem<ThaumometerItem> THAUMOMETER = ITEMS.registerItem("thaumometer", ThaumometerItem::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> SCRIBING_TOOLS = ITEMS.registerItem("scribing_tools", Item::new, props -> props.stacksTo(1).durability(SCRIBING_TOOLS_DURABILITY));

    public static final DeferredItem<CelestialNotesItem> CELESTIAL_NOTES = ITEMS.registerItem("celestial_notes", CelestialNotesItem::new,
            props -> props.component(TTDataComponents.CELESTIAL_BODY.get(), CelestialBody.SUN));

    public static final DeferredItem<PhialItem> PHIAL = ITEMS.registerItem("phial", PhialItem::new);

    public static final DeferredItem<ItemResearchNote> RESEARCH_NOTE = ITEMS.registerItem("research_note", ItemResearchNote::new, props -> props.stacksTo(1));

    public static final DeferredItem<BlockItem> ARCANE_GRINDSTONE = ITEMS.registerSimpleBlockItem(TTBlocks.ARCANE_GRINDSTONE);
    public static final DeferredItem<BlockItem> INFUSION_MATRIX = ITEMS.registerSimpleBlockItem(TTBlocks.INFUSION_MATRIX);
    public static final DeferredItem<BlockItem> PEDESTAL_ARCANE = ITEMS.registerSimpleBlockItem(TTBlocks.PEDESTAL_ARCANE);
    public static final DeferredItem<BlockItem> PEDESTAL_ANCIENT = ITEMS.registerSimpleBlockItem(TTBlocks.PEDESTAL_ANCIENT);
    public static final DeferredItem<BlockItem> PEDESTAL_ELDRITCH = ITEMS.registerSimpleBlockItem(TTBlocks.PEDESTAL_ELDRITCH);
    public static final DeferredItem<BlockItem> PILLAR_ARCANE = ITEMS.registerSimpleBlockItem(TTBlocks.PILLAR_ARCANE);
    public static final DeferredItem<BlockItem> PILLAR_ANCIENT = ITEMS.registerSimpleBlockItem(TTBlocks.PILLAR_ANCIENT);
    public static final DeferredItem<BlockItem> PILLAR_ELDRITCH = ITEMS.registerSimpleBlockItem(TTBlocks.PILLAR_ELDRITCH);

    public static final DeferredItem<ItemWand> WAND = ITEMS.registerItem("wand", ItemWand::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemWandCap> WAND_CAP_IRON = ITEMS.registerItem("wand_cap_iron", props -> new ItemWandCap(props, TTWandParts.CAP_IRON));
    public static final DeferredItem<ItemWandCap> WAND_CAP_COPPER = ITEMS.registerItem("wand_cap_copper", props -> new ItemWandCap(props, TTWandParts.CAP_COPPER));
    public static final DeferredItem<ItemWandCap> WAND_CAP_GOLD = ITEMS.registerItem("wand_cap_gold", props -> new ItemWandCap(props, TTWandParts.CAP_GOLD));
    public static final DeferredItem<Item> WAND_CAP_SILVER_INERT = ITEMS.registerSimpleItem("wand_cap_silver_inert");
    public static final DeferredItem<ItemWandCap> WAND_CAP_SILVER = ITEMS.registerItem("wand_cap_silver", props -> new ItemWandCap(props, TTWandParts.CAP_SILVER));
    public static final DeferredItem<Item> WAND_CAP_THAUMIUM_INERT = ITEMS.registerSimpleItem("wand_cap_thaumium_inert");
    public static final DeferredItem<ItemWandCap> WAND_CAP_THAUMIUM = ITEMS.registerItem("wand_cap_thaumium", props -> new ItemWandCap(props, TTWandParts.CAP_THAUMIUM));
    public static final DeferredItem<Item> WAND_CAP_VOID_INERT = ITEMS.registerSimpleItem("wand_cap_void_inert");
    public static final DeferredItem<ItemWandCap> WAND_CAP_VOID = ITEMS.registerItem("wand_cap_void", props -> new ItemWandCap(props, TTWandParts.CAP_VOID));

    public static final DeferredItem<ItemWandRod> WAND_ROD_GREATWOOD = ITEMS.registerItem("wand_rod_greatwood", props -> new ItemWandRod(props, TTWandParts.ROD_GREATWOOD));
    public static final DeferredItem<ItemWandRod> WAND_ROD_OBSIDIAN = ITEMS.registerItem("wand_rod_obsidian", props -> new ItemWandRod(props, TTWandParts.ROD_OBSIDIAN));
    public static final DeferredItem<ItemWandRod> WAND_ROD_BLAZE = ITEMS.registerItem("wand_rod_blaze", props -> new ItemWandRod(props, TTWandParts.ROD_BLAZE));
    public static final DeferredItem<ItemWandRod> WAND_ROD_ICE = ITEMS.registerItem("wand_rod_ice", props -> new ItemWandRod(props, TTWandParts.ROD_ICE));
    public static final DeferredItem<ItemWandRod> WAND_ROD_QUARTZ = ITEMS.registerItem("wand_rod_quartz", props -> new ItemWandRod(props, TTWandParts.ROD_QUARTZ));
    public static final DeferredItem<ItemWandRod> WAND_ROD_BONE = ITEMS.registerItem("wand_rod_bone", props -> new ItemWandRod(props, TTWandParts.ROD_BONE));
    public static final DeferredItem<ItemWandRod> WAND_ROD_REED = ITEMS.registerItem("wand_rod_reed", props -> new ItemWandRod(props, TTWandParts.ROD_REED));
    public static final DeferredItem<ItemWandRod> WAND_ROD_SILVERWOOD = ITEMS.registerItem("wand_rod_silverwood", props -> new ItemWandRod(props, TTWandParts.ROD_SILVERWOOD));

    public static final DeferredItem<ItemWandRod> STAFF_ROD_GREATWOOD = ITEMS.registerItem("staff_rod_greatwood", props -> new ItemWandRod(props, TTWandParts.STAFF_GREATWOOD));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_OBSIDIAN = ITEMS.registerItem("staff_rod_obsidian", props -> new ItemWandRod(props, TTWandParts.STAFF_OBSIDIAN));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_BLAZE = ITEMS.registerItem("staff_rod_blaze", props -> new ItemWandRod(props, TTWandParts.STAFF_BLAZE));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_ICE = ITEMS.registerItem("staff_rod_ice", props -> new ItemWandRod(props, TTWandParts.STAFF_ICE));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_QUARTZ = ITEMS.registerItem("staff_rod_quartz", props -> new ItemWandRod(props, TTWandParts.STAFF_QUARTZ));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_BONE = ITEMS.registerItem("staff_rod_bone", props -> new ItemWandRod(props, TTWandParts.STAFF_BONE));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_REED = ITEMS.registerItem("staff_rod_reed", props -> new ItemWandRod(props, TTWandParts.STAFF_REED));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_SILVERWOOD = ITEMS.registerItem("staff_rod_silverwood", props -> new ItemWandRod(props, TTWandParts.STAFF_SILVERWOOD));
    public static final DeferredItem<ItemWandRod> STAFF_ROD_PRIMAL = ITEMS.registerItem("staff_rod_primal", props -> new ItemWandRod(props, TTWandParts.STAFF_PRIMAL),
            props -> props.rarity(Rarity.RARE));

    public static final DeferredItem<ItemPrimalCharm> PRIMAL_CHARM = ITEMS.registerItem("primal_charm", ItemPrimalCharm::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> VIS_RELAY = ITEMS.registerSimpleBlockItem(TTBlocks.VIS_RELAY);
    public static final DeferredItem<BlockItem> NODE_STABILIZER = ITEMS.registerSimpleBlockItem(TTBlocks.NODE_STABILIZER);
    public static final DeferredItem<BlockItem> NODE_STABILIZER_ADVANCED = ITEMS.registerSimpleBlockItem(TTBlocks.NODE_STABILIZER_ADVANCED);
    public static final DeferredItem<BlockItem> NODE_TRANSDUCER = ITEMS.registerSimpleBlockItem(TTBlocks.NODE_TRANSDUCER);
    public static final DeferredItem<JarNodeItem> JAR_NODE = ITEMS.registerItem("jar_node", props -> new JarNodeItem(TTBlocks.JAR_NODE.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<FocusItem> FOCUS_1 = ITEMS.registerItem("focus_1", FocusItem::new, props -> props.stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<FocusItem> FOCUS_2 = ITEMS.registerItem("focus_2", FocusItem::new, props -> props.stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<FocusItem> FOCUS_3 = ITEMS.registerItem("focus_3", FocusItem::new, props -> props.stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<ThrowableItem> CAUSALITY_COLLAPSER = ITEMS.registerItem("causality_collapser",
            props -> new ThrowableItem(props, EntityCausalityCollapser::new, EntityCausalityCollapser.THROW), props -> props.stacksTo(16));

    public static final DeferredItem<Item> VOID_SEED = ITEMS.registerItem("void_seed", Item::new, props -> props.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<PrimordialPearlItem> PRIMORDIAL_PEARL = ITEMS.registerItem("primordial_pearl", PrimordialPearlItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON).durability(PrimordialPearlItem.MAX_DAMAGE));

    public static final int GOGGLES_DURABILITY = 350;

    public static final int GOGGLES_ENCHANTMENT_VALUE = 25;

    public static final ResourceKey<EquipmentAsset> GOGGLES_REVEALING_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, TTIds.rl("goggles_revealing"));

    public static final DeferredItem<GogglesItem> GOGGLES_REVEALING = ITEMS.registerItem("goggles_revealing", GogglesItem::new,
            props -> props.stacksTo(1).durability(GOGGLES_DURABILITY).enchantable(GOGGLES_ENCHANTMENT_VALUE).rarity(Rarity.RARE)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).setAsset(GOGGLES_REVEALING_ASSET).build())
                    .component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ARMOR, new AttributeModifier(GOGGLES_REVEALING_ASSET.identifier(), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                            .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(GOGGLES_REVEALING_ASSET.identifier(), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD).build()));

    public static final DeferredItem<FortressArmorItem> FORTRESS_HELM = ITEMS.registerItem("fortress_helm", FortressArmorItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_FORTRESS, ArmorType.HELMET).rarity(Rarity.RARE));
    public static final DeferredItem<FortressArmorItem> FORTRESS_CHEST = ITEMS.registerItem("fortress_chest", FortressArmorItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_FORTRESS, ArmorType.CHESTPLATE).rarity(Rarity.RARE));
    public static final DeferredItem<FortressArmorItem> FORTRESS_LEGS = ITEMS.registerItem("fortress_legs", FortressArmorItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_FORTRESS, ArmorType.LEGGINGS).rarity(Rarity.RARE));

    public static final DeferredItem<VoidRobeArmorItem> VOID_ROBE_HELM = ITEMS.registerItem("void_robe_helm", VoidRobeArmorItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_VOID_ROBE, ArmorType.HELMET).rarity(Rarity.EPIC).component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE));
    public static final DeferredItem<VoidRobeArmorItem> VOID_ROBE_CHEST = ITEMS.registerItem("void_robe_chest", VoidRobeArmorItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_VOID_ROBE, ArmorType.CHESTPLATE).rarity(Rarity.EPIC));
    public static final DeferredItem<VoidRobeArmorItem> VOID_ROBE_LEGS = ITEMS.registerItem("void_robe_legs", VoidRobeArmorItem::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_VOID_ROBE, ArmorType.LEGGINGS).rarity(Rarity.EPIC));

    public static final DeferredItem<Item> CRIMSON_PRAETOR_HELM = ITEMS.registerItem("crimson_praetor_helm", Item::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_LEADER, ArmorType.HELMET).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CRIMSON_PRAETOR_CHEST = ITEMS.registerItem("crimson_praetor_chest", Item::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_LEADER, ArmorType.CHESTPLATE).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CRIMSON_PRAETOR_LEGS = ITEMS.registerItem("crimson_praetor_legs", Item::new,
            props -> props.humanoidArmor(TTMaterials.ARMOR_CULTIST_LEADER, ArmorType.LEGGINGS).rarity(Rarity.RARE));

    public static final int TRINKET_APPRENTICE_DISCOUNT = 5;
    private static final int AMULET_VIS_FOUND_INTERVAL = 100;
    private static final int AMULET_VIS_CRAFTED_INTERVAL = 20;

    public static final DeferredItem<TrinketItem> AMULET_MUNDANE = ITEMS.registerItem("amulet_mundane", TrinketItem::new, props -> props.stacksTo(1));
    public static final DeferredItem<TrinketItem> RING_MUNDANE = ITEMS.registerItem("ring_mundane", TrinketItem::new, props -> props.stacksTo(1));
    public static final DeferredItem<TrinketItem> GIRDLE_MUNDANE = ITEMS.registerItem("girdle_mundane", TrinketItem::new, props -> props.stacksTo(1));
    public static final DeferredItem<TrinketItem> RING_APPRENTICE = ITEMS.registerItem("ring_apprentice", props -> new TrinketItem(props, TRINKET_APPRENTICE_DISCOUNT),
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<TrinketItem> AMULET_FANCY = ITEMS.registerItem("amulet_fancy", TrinketItem::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<TrinketItem> RING_FANCY = ITEMS.registerItem("ring_fancy", TrinketItem::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<TrinketItem> GIRDLE_FANCY = ITEMS.registerItem("girdle_fancy", TrinketItem::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<AmuletVisItem> AMULET_VIS = ITEMS.registerItem("amulet_vis", props -> new AmuletVisItem(props, AMULET_VIS_FOUND_INTERVAL),
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<AmuletVisItem> AMULET_VIS_CRAFTED = ITEMS.registerItem("amulet_vis_crafted", props -> new AmuletVisItem(props, AMULET_VIS_CRAFTED_INTERVAL),
            props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CHARM_UNDYING = ITEMS.registerItem("charm_undying", Item::new, props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CLOUD_RING = ITEMS.registerItem("cloud_ring", Item::new, props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> CURIOSITY_BAND = ITEMS.registerItem("curiosity_band", Item::new, props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<VerdantCharmItem> VERDANT_CHARM = ITEMS.registerItem("verdant_charm", VerdantCharmItem::new, props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<VoidseerCharmItem> VOIDSEER_CHARM = ITEMS.registerItem("voidseer_charm", VoidseerCharmItem::new, props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<FocusPouchItem> FOCUS_POUCH = ITEMS.registerItem("focus_pouch", FocusPouchItem::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BlockItem> ACTIVATOR_RAIL = ITEMS.registerSimpleBlockItem(TTBlocks.ACTIVATOR_RAIL);
    public static final DeferredItem<BlockItem> OBSIDIAN_TILE = ITEMS.registerSimpleBlockItem(TTBlocks.OBSIDIAN_TILE);
    public static final DeferredItem<BlockItem> OBSIDIAN_TOTEM = ITEMS.registerSimpleBlockItem(TTBlocks.OBSIDIAN_TOTEM);
    public static final DeferredItem<BlockItem> ELDRITCH_STONE = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_STONE);
    public static final DeferredItem<BlockItem> ELDRITCH_STONE_INERT = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_STONE_INERT);
    public static final DeferredItem<BlockItem> ELDRITCH_ROCK = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_ROCK);
    public static final DeferredItem<BlockItem> ELDRITCH_CRUST = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_CRUST);
    public static final DeferredItem<BlockItem> ELDRITCH_CRUST_GLOWING = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_CRUST_GLOWING);
    public static final DeferredItem<BlockItem> STAIRS_ELDRITCH = ITEMS.registerSimpleBlockItem(TTBlocks.STAIRS_ELDRITCH);
    public static final DeferredItem<BlockItem> ELDRITCH_DOOR = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_DOOR);
    public static final DeferredItem<BlockItem> ELDRITCH_PEDESTAL = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_PEDESTAL);
    public static final DeferredItem<BlockItem> ELDRITCH_STONE_CRYSTAL = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_STONE_CRYSTAL);
    public static final DeferredItem<BlockItem> ELDRITCH_LOCK = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_LOCK);
    public static final DeferredItem<BlockItem> ELDRITCH_CRAB_SPAWNER = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_CRAB_SPAWNER);
    public static final DeferredItem<BlockItem> ELDRITCH_TRAP = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_TRAP);
    public static final DeferredItem<BlockItem> ELDRITCH_ALTAR = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_ALTAR);
    public static final DeferredItem<BlockItem> ELDRITCH_OBELISK = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_OBELISK);
    public static final DeferredItem<BlockItem> ELDRITCH_PILLAR = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_PILLAR);
    public static final DeferredItem<BlockItem> ELDRITCH_CAPSTONE = ITEMS.registerSimpleBlockItem(TTBlocks.ELDRITCH_CAPSTONE);
    public static final DeferredItem<Item> ELDRITCH_EYE = ITEMS.registerItem("eldritch_eye", Item::new, props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RUNED_TABLET = ITEMS.registerItem("runed_tablet", Item::new, props -> props.rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> SLAB_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.SLAB_GREATWOOD);
    public static final DeferredItem<BlockItem> SLAB_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.SLAB_SILVERWOOD);
    public static final DeferredItem<BlockItem> SLAB_ARCANE_STONE = ITEMS.registerSimpleBlockItem(TTBlocks.SLAB_ARCANE_STONE);
    public static final DeferredItem<BlockItem> SLAB_ARCANE_BRICK = ITEMS.registerSimpleBlockItem(TTBlocks.SLAB_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> SLAB_ANCIENT = ITEMS.registerSimpleBlockItem(TTBlocks.SLAB_ANCIENT);
    public static final DeferredItem<BlockItem> SLAB_ELDRITCH = ITEMS.registerSimpleBlockItem(TTBlocks.SLAB_ELDRITCH);
    public static final DeferredItem<BlockItem> STAIRS_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.STAIRS_GREATWOOD);
    public static final DeferredItem<BlockItem> STAIRS_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.STAIRS_SILVERWOOD);
    public static final DeferredItem<BlockItem> DOOR_GREATWOOD = registerSimpleBlockItem(TTBlocks.DOOR_GREATWOOD, DoubleHighBlockItem::new);
    public static final DeferredItem<BlockItem> TRAPDOOR_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.TRAPDOOR_GREATWOOD);
    public static final DeferredItem<BlockItem> FENCE_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.FENCE_GREATWOOD);
    public static final DeferredItem<BlockItem> FENCE_GATE_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.FENCE_GATE_GREATWOOD);
    public static final DeferredItem<BlockItem> BUTTON_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.BUTTON_GREATWOOD);
    public static final DeferredItem<BlockItem> PRESSURE_PLATE_GREATWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.PRESSURE_PLATE_GREATWOOD);
    public static final DeferredItem<BlockItem> DOOR_SILVERWOOD = registerSimpleBlockItem(TTBlocks.DOOR_SILVERWOOD, DoubleHighBlockItem::new);
    public static final DeferredItem<BlockItem> TRAPDOOR_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.TRAPDOOR_SILVERWOOD);
    public static final DeferredItem<BlockItem> FENCE_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.FENCE_SILVERWOOD);
    public static final DeferredItem<BlockItem> FENCE_GATE_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.FENCE_GATE_SILVERWOOD);
    public static final DeferredItem<BlockItem> BUTTON_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.BUTTON_SILVERWOOD);
    public static final DeferredItem<BlockItem> PRESSURE_PLATE_SILVERWOOD = ITEMS.registerSimpleBlockItem(TTBlocks.PRESSURE_PLATE_SILVERWOOD);
    public static final DeferredItem<BlockItem> TABLE_WOOD = ITEMS.registerSimpleBlockItem(TTBlocks.TABLE_WOOD);
    public static final DeferredItem<BlockItem> TABLE_STONE = ITEMS.registerSimpleBlockItem(TTBlocks.TABLE_STONE);
    public static final DeferredItem<BlockItem> PAVING_STONE_TRAVEL = ITEMS.registerSimpleBlockItem(TTBlocks.PAVING_STONE_TRAVEL);
    public static final DeferredItem<BlockItem> PAVING_STONE_BARRIER = ITEMS.registerSimpleBlockItem(TTBlocks.PAVING_STONE_BARRIER);
    public static final DeferredItem<BlockItem> AMBER_BRICK = ITEMS.registerSimpleBlockItem(TTBlocks.AMBER_BRICK);
    public static final DeferredItem<BlockItem> FLESH_BLOCK = ITEMS.registerSimpleBlockItem(TTBlocks.FLESH_BLOCK);
    public static final DeferredItem<TurretPlacerItem> TURRET_BASIC = ITEMS.registerItem("turret_basic", props -> new TurretPlacerItem(props, ConstructDeployment.upright(TTEntities.TURRET_CROSSBOW)),
            props -> props.stacksTo(16));
    public static final DeferredItem<TurretPlacerItem> TURRET_ADVANCED = ITEMS.registerItem("turret_advanced",
            props -> new TurretPlacerItem(props, ConstructDeployment.upright(TTEntities.TURRET_CROSSBOW_ADVANCED)), props -> props.stacksTo(16));
    public static final DeferredItem<ArcaneBoreItem> ARCANE_BORE = ITEMS.registerItem("arcane_bore", props -> new ArcaneBoreItem(TTBlocks.ARCANE_BORE.get(), props),
            props -> props.stacksTo(16).rarity(Rarity.UNCOMMON).useBlockDescriptionPrefix());
    public static final DeferredItem<GrappleGunItem> GRAPPLE_GUN = ITEMS.registerItem("grapple_gun", GrappleGunItem::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> GRAPPLE_GUN_TIP = ITEMS.registerSimpleItem("grapple_gun_tip");
    public static final DeferredItem<Item> GRAPPLE_GUN_SPOOL = ITEMS.registerSimpleItem("grapple_gun_spool");
    public static final DeferredItem<Item> SANITY_CHECKER = ITEMS.registerItem("sanity_checker", Item::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<ItemResonator> RESONATOR = ITEMS.registerItem("resonator", ItemResonator::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemCurio> CURIO_ARCANE = registerCurio("curio_arcane", ItemCurio.Variant.ARCANE);
    public static final DeferredItem<ItemCurio> CURIO_PRESERVED = registerCurio("curio_preserved", ItemCurio.Variant.PRESERVED);
    public static final DeferredItem<ItemCurio> CURIO_ANCIENT = registerCurio("curio_ancient", ItemCurio.Variant.ANCIENT);
    public static final DeferredItem<ItemCurio> CURIO_ELDRITCH = registerCurio("curio_eldritch", ItemCurio.Variant.ELDRITCH);
    public static final DeferredItem<ItemCurio> CURIO_KNOWLEDGE = registerCurio("curio_knowledge", ItemCurio.Variant.KNOWLEDGE);
    public static final DeferredItem<ItemCurio> CURIO_TWISTED = registerCurio("curio_twisted", ItemCurio.Variant.TWISTED);
    public static final DeferredItem<ItemCurio> CURIO_RITES = registerCurio("curio_rites", ItemCurio.Variant.RITES);

    public static final DeferredItem<ItemCreativeFluxSponge> CREATIVE_FLUX_SPONGE = ITEMS.registerItem("creative_flux_sponge", ItemCreativeFluxSponge::new,
            props -> props.stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<ItemBlockMirror> MIRROR = ITEMS.registerItem("mirror", props -> new ItemBlockMirror(TTBlocks.MIRROR.get(), props.useBlockDescriptionPrefix()),
            props -> props.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemBlockMirror> MIRROR_ESSENTIA = ITEMS.registerItem("mirror_essentia",
            props -> new ItemBlockMirror(TTBlocks.MIRROR_ESSENTIA.get(), props.useBlockDescriptionPrefix()), props -> props.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemHandMirror> HAND_MIRROR = ITEMS.registerItem("hand_mirror", ItemHandMirror::new, props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    private static DeferredItem<ItemCurio> registerCurio(String name, ItemCurio.Variant variant) {
        return ITEMS.registerItem(name, props -> new ItemCurio(props, variant), props -> props.rarity(Rarity.UNCOMMON));
    }

    private TTItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static <T extends BlockItem> DeferredItem<BlockItem> registerSimpleBlockItem(Holder<Block> block, BiFunction<Block, Item.Properties, T> constructor) {
        return ITEMS.registerItem(block.unwrapKey().orElseThrow().identifier().getPath(), p -> constructor.apply(block.value(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    }
}
