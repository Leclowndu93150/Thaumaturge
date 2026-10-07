package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TTIds.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THAUMATURGE = CREATIVE_MODE_TABS.register("thaumaturge",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.thaumaturge")).icon(() -> new ItemStack(TTItems.THAUMONOMICON.get())).displayItems((parameters, output) -> {
                output.accept(TTItems.THAUMONOMICON.get());
                output.accept(TTItems.THAUMONOMICON_CHEAT.get());
                output.accept(TTItems.THAUMONOMICON_SHARING.get());
                output.accept(TTItems.THAUMONOMICON_LINKING.get());
                output.accept(TTItems.CREATIVE_NODE_PLACER.get());
                output.accept(TTItems.SALIS_MUNDUS.get());
                output.accept(TTItems.THAUMOMETER.get());
                output.accept(TTItems.SCRIBING_TOOLS.get());
                output.accept(TTItems.RESEARCH_NOTE.get());
                output.accept(TTItems.MANA_BEAN.get());
                output.accept(TTItems.VIS_RESONATOR.get());
                for (CelestialBody body : CelestialBody.values()) {
                    output.accept(CelestialNotesItem.stackOf(body));
                }
                output.accept(TTItems.GOGGLES_REVEALING.get());
                output.accept(chargedWand(TTWandParts.CAP_IRON.get(), TTWandParts.ROD_WOOD.get(), false));
                output.accept(chargedWand(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_GREATWOOD.get(), false));
                output.accept(chargedWand(TTWandParts.CAP_THAUMIUM.get(), TTWandParts.ROD_SILVERWOOD.get(), false));
                output.accept(chargedWand(TTWandParts.CAP_THAUMIUM.get(), TTWandParts.ROD_SILVERWOOD.get(), true));
                output.accept(chargedWand(TTWandParts.CAP_VOID.get(), TTWandParts.STAFF_PRIMAL.get(), false));
                output.accept(TTItems.WAND_CAP_IRON.get());
                output.accept(TTItems.WAND_CAP_COPPER.get());
                output.accept(TTItems.WAND_CAP_GOLD.get());
                output.accept(TTItems.WAND_CAP_SILVER_INERT.get());
                output.accept(TTItems.WAND_CAP_SILVER.get());
                output.accept(TTItems.WAND_CAP_THAUMIUM_INERT.get());
                output.accept(TTItems.WAND_CAP_THAUMIUM.get());
                output.accept(TTItems.WAND_CAP_VOID_INERT.get());
                output.accept(TTItems.WAND_CAP_VOID.get());
                output.accept(TTItems.WAND_ROD_GREATWOOD.get());
                output.accept(TTItems.WAND_ROD_OBSIDIAN.get());
                output.accept(TTItems.WAND_ROD_BLAZE.get());
                output.accept(TTItems.WAND_ROD_ICE.get());
                output.accept(TTItems.WAND_ROD_QUARTZ.get());
                output.accept(TTItems.WAND_ROD_BONE.get());
                output.accept(TTItems.WAND_ROD_REED.get());
                output.accept(TTItems.WAND_ROD_SILVERWOOD.get());
                output.accept(TTItems.STAFF_ROD_GREATWOOD.get());
                output.accept(TTItems.STAFF_ROD_OBSIDIAN.get());
                output.accept(TTItems.STAFF_ROD_BLAZE.get());
                output.accept(TTItems.STAFF_ROD_ICE.get());
                output.accept(TTItems.STAFF_ROD_QUARTZ.get());
                output.accept(TTItems.STAFF_ROD_BONE.get());
                output.accept(TTItems.STAFF_ROD_REED.get());
                output.accept(TTItems.STAFF_ROD_SILVERWOOD.get());
                output.accept(TTItems.STAFF_ROD_PRIMAL.get());
                output.accept(TTItems.PRIMAL_CHARM.get());
                output.accept(TTItems.NODE_STABILIZER.get());
                output.accept(TTItems.NODE_STABILIZER_ADVANCED.get());
                output.accept(TTItems.NODE_TRANSDUCER.get());
                output.accept(TTItems.VIS_RELAY.get());
                output.accept(TTItems.FOCUS_1.get());
                output.accept(TTItems.FOCUS_2.get());
                output.accept(TTItems.FOCUS_3.get());
                output.accept(TTItems.LABEL.get());
                output.accept(TTItems.RESEARCH_TABLE.get());
                output.accept(TTBlocks.DECONSTRUCTION_TABLE.get());
                output.accept(TTItems.ARCANE_WORKBENCH_CHARGER.get());
                output.accept(TTItems.FOCAL_MANIPULATOR.get());
                output.accept(TTItems.ARCANE_WORKBENCH.get());
                output.accept(TTItems.CRUCIBLE.get());
                output.accept(TTItems.ALCHEMICAL_CONSTRUCT.get());
                output.accept(TTItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
                output.accept(TTItems.CENTRIFUGE.get());
                output.accept(TTItems.ESSENTIA_CRYSTALIZER.get());

                output.accept(TTItems.ALEMBIC.get());
                output.accept(TTItems.BELLOWS.get());
                output.accept(TTItems.SMELTER_BASIC.get());
                output.accept(TTItems.SMELTER_THAUMIUM.get());
                output.accept(TTItems.SMELTER_VOID.get());
                output.accept(TTItems.ADVANCED_ALCHEMICAL_FURNACE.get());
                output.accept(TTItems.SMELTER_AUX.get());
                output.accept(TTItems.SMELTER_VENT.get());
                output.accept(TTItems.ESSENTIA_INPUT.get());
                output.accept(TTItems.ESSENTIA_OUTPUT.get());
                output.accept(TTItems.BUCKET_LIQUID_DEATH.get());
                output.accept(TTItems.BUCKET_PURIFYING.get());
                output.accept(TTItems.JAR_BRACE.get());
                output.accept(TTItems.JAR_NORMAL.get());
                output.accept(TTItems.JAR_VOID.get());
                output.accept(TTItems.JAR_BRAIN.get());
                output.accept(TTItems.JAR_NODE.get());
                output.accept(TTItems.TUBE.get());
                output.accept(TTItems.TUBE_VALVE.get());
                output.accept(TTItems.TUBE_RESTRICT.get());
                output.accept(TTItems.TUBE_FILTER.get());
                output.accept(TTItems.TUBE_ONEWAY.get());
                output.accept(TTItems.TUBE_BUFFER.get());
                output.accept(TTItems.ESSENTIA_RESERVOIR.get());
                output.accept(TTItems.BRAIN_BOX.get());

                output.accept(TTItems.CRYSTAL_AER.get());
                output.accept(TTItems.CRYSTAL_IGNIS.get());
                output.accept(TTItems.CRYSTAL_AQUA.get());
                output.accept(TTItems.CRYSTAL_TERRA.get());
                output.accept(TTItems.CRYSTAL_ORDO.get());
                output.accept(TTItems.CRYSTAL_PERDITIO.get());
                output.accept(TTItems.CRYSTAL_VITIUM.get());

                output.accept(TTItems.ORE_AMBER.get());
                output.accept(TTItems.DEEPSLATE_ORE_AMBER.get());
                output.accept(TTItems.ORE_CINNABAR.get());
                output.accept(TTItems.DEEPSLATE_ORE_CINNABAR.get());
                output.accept(TTItems.RAW_CINNABAR.get());
                output.accept(TTItems.ORE_QUARTZ.get());
                output.accept(TTItems.DEEPSLATE_ORE_QUARTZ.get());
                output.accept(TTItems.AMBER_BLOCK);
                output.accept(TTItems.METAL_BRASS_BLOCK);
                output.accept(TTItems.METAL_THAUMIUM_BLOCK);
                output.accept(TTItems.METAL_VOID_BLOCK);

                output.accept(TTItems.QUICKSILVER.get());
                output.accept(TTItems.AMBER.get());
                output.accept(TTItems.INGOT_BRASS.get());
                output.accept(TTItems.INGOT_THAUMIUM.get());
                output.accept(TTItems.INGOT_VOID.get());

                output.accept(TTItems.NUGGET_QUARTZ.get());
                output.accept(TTItems.NUGGET_QUICKSILVER.get());
                output.accept(TTItems.NUGGET_BRASS.get());
                output.accept(TTItems.NUGGET_THAUMIUM.get());
                output.accept(TTItems.NUGGET_VOID.get());

                output.accept(TTItems.PLATE_IRON.get());
                output.accept(TTItems.PLATE_BRASS.get());
                output.accept(TTItems.PLATE_THAUMIUM.get());
                output.accept(TTItems.PLATE_VOID.get());

                output.accept(TTItems.CLUSTER_IRON.get());
                output.accept(TTItems.CLUSTER_GOLD.get());
                output.accept(TTItems.CLUSTER_COPPER.get());
                addIfTag(parameters, output, TTItemTags.INGOTS_TIN, TTItems.CLUSTER_TIN.get());
                addIfTag(parameters, output, TTItemTags.INGOTS_SILVER, TTItems.CLUSTER_SILVER.get());
                addIfTag(parameters, output, TTItemTags.INGOTS_LEAD, TTItems.CLUSTER_LEAD.get());
                output.accept(TTItems.CLUSTER_CINNABAR.get());
                output.accept(TTItems.CLUSTER_QUARTZ.get());

                output.accept(TTItems.CHUNK_BEEF.get());
                output.accept(TTItems.CHUNK_CHICKEN.get());
                output.accept(TTItems.CHUNK_PORK.get());
                output.accept(TTItems.CHUNK_FISH.get());
                output.accept(TTItems.CHUNK_RABBIT.get());
                output.accept(TTItems.CHUNK_MUTTON.get());
                output.accept(TTItems.TRIPLE_MEAT_TREAT.get());

                output.accept(TTItems.BRAIN.get());
                output.accept(TTItems.FABRIC.get());
                output.accept(TTItems.FILTER.get());
                output.accept(TTItems.MIRRORED_GLASS.get());
                output.accept(TTItems.MECHANISM_SIMPLE.get());
                output.accept(TTItems.MECHANISM_COMPLEX.get());
                output.accept(TTItems.MORPHIC_RESONATOR.get());
                output.accept(TTItems.VOID_SEED.get());
                output.accept(TTItems.CAUSALITY_COLLAPSER.get());
                output.accept(TTItems.PRIMORDIAL_PEARL.get());

                output.accept(TTItems.INFERNAL_FURNACE.get());
                output.accept(TTItems.THAUMATORIUM.get());
                output.accept(TTItems.INFUSION_MATRIX.get());
                output.accept(TTItems.ARCANE_GRINDSTONE.get());
                output.accept(TTItems.PEDESTAL_ARCANE.get());
                output.accept(TTItems.PEDESTAL_ANCIENT.get());
                output.accept(TTItems.PEDESTAL_ELDRITCH.get());
                output.accept(TTItems.PILLAR_ARCANE.get());
                output.accept(TTItems.PILLAR_ANCIENT.get());
                output.accept(TTItems.PILLAR_ELDRITCH.get());
                output.accept(TTItems.MATRIX_SPEED.get());
                output.accept(TTItems.MATRIX_COST.get());

                output.accept(TTItems.SPA.get());
                output.accept(TTItems.BATH_SALTS.get());
                output.accept(TTItems.SANITY_SOAP.get());

                output.accept(TTItems.ALUMENTUM.get());
                for (DyeColor dye : DyeColor.values()) {
                    output.accept(TTItems.NITORS.get(dye).get());
                }
                for (DyeColor dye : DyeColor.values()) {
                    output.accept(TTItems.BANNERS.get(dye).get());
                }
                output.accept(TTItems.BANNER_CRIMSON_CULT.get());
                output.accept(TTItems.TALLOW.get());
                output.accept(TTItems.TALLOW_BLOCK.get());
                output.accept(TTItems.WARDED_GLASS.get());
                output.accept(TTItems.ARCANE_DOOR.get());
                output.accept(TTItems.ARCANE_PRESSURE_PLATE.get());
                output.accept(TTItems.ARCANE_KEY_IRON.get());
                output.accept(TTItems.ARCANE_KEY_GOLD.get());
                for (DyeColor dye : DyeColor.values()) {
                    output.accept(TTItems.CANDLES.get(dye).get());
                }
                for (DeferredItem<BlockItem> holder : TTItems.CANDLE_HOLDERS.values()) {
                    output.accept(holder.get());
                }

                output.accept(TTItems.STONE_ARCANE.get());
                output.accept(TTItems.STONE_ARCANE_BRICK.get());
                output.accept(TTItems.STAIRS_ARCANE.get());
                output.accept(TTItems.STAIRS_ARCANE_BRICK.get());
                output.accept(TTItems.STONE_ANCIENT.get());
                output.accept(TTItems.STONE_ANCIENT_TILE.get());
                output.accept(TTItems.STONE_ANCIENT_GLYPHED.get());
                output.accept(TTItems.STAIRS_ANCIENT.get());
                output.accept(TTItems.STONE_ELDRITCH_TILE.get());
                output.accept(TTItems.STONE_POROUS.get());
                output.accept(TTItems.SAPLING_GREATWOOD.get());
                output.accept(TTItems.SAPLING_SILVERWOOD.get());
                output.accept(TTItems.LOG_GREATWOOD.get());
                output.accept(TTItems.WOOD_GREATWOOD.get());
                output.accept(TTItems.STRIPPED_LOG_GREATWOOD.get());
                output.accept(TTItems.STRIPPED_WOOD_GREATWOOD.get());
                output.accept(TTItems.LOG_SILVERWOOD.get());
                output.accept(TTItems.WOOD_SILVERWOOD.get());
                output.accept(TTItems.STRIPPED_LOG_SILVERWOOD.get());
                output.accept(TTItems.STRIPPED_WOOD_SILVERWOOD.get());
                output.accept(TTItems.LEAVES_GREATWOOD.get());
                output.accept(TTItems.LEAVES_SILVERWOOD.get());
                output.accept(TTItems.PLANK_GREATWOOD.get());
                output.accept(TTItems.PLANK_SILVERWOOD.get());
                output.accept(TTItems.PLANT_SHIMMERLEAF.get());
                output.accept(TTItems.ETHEREAL_BLOOM.get());
                output.accept(TTItems.PLANT_CINDERPEARL.get());
                output.accept(TTItems.PLANT_VISHROOM.get());
                output.accept(TTItems.GRASS_AMBIENT.get());
                HolderLookup.RegistryLookup<IAspect> aspectRegistry = parameters.holders().lookupOrThrow(IAspect.REGISTRY_KEY);
                for (Holder<IAspect> aspect : aspectRegistry.listElements().sorted(Comparator.comparing(h -> !h.value().isPrimal())).toList()) {
                    output.accept(EssentiaCrystalFactory.of(aspect));
                }
                output.accept(TTItems.PHIAL.get());
                for (Holder<IAspect> aspect : aspectRegistry.listElements().sorted(Comparator.comparing(h -> !h.value().isPrimal())).toList()) {
                    output.accept(PhialItem.makeFilled(aspect));
                }
                output.accept(TTItems.THAUMIUM_SWORD.get());
                output.accept(TTItems.THAUMIUM_SPEAR.get());
                output.accept(TTItems.THAUMIUM_PICKAXE.get());
                output.accept(TTItems.THAUMIUM_AXE.get());
                output.accept(TTItems.THAUMIUM_SHOVEL.get());
                output.accept(TTItems.THAUMIUM_HOE.get());
                output.accept(TTItems.THAUMIUM_HELM.get());
                output.accept(TTItems.THAUMIUM_CHEST.get());
                output.accept(TTItems.THAUMIUM_LEGS.get());
                output.accept(TTItems.THAUMIUM_BOOTS.get());
                output.accept(TTItems.VOID_SWORD.get());
                output.accept(TTItems.VOID_SPEAR.get());
                output.accept(TTItems.VOID_PICKAXE.get());
                output.accept(TTItems.VOID_AXE.get());
                output.accept(TTItems.VOID_SHOVEL.get());
                output.accept(TTItems.VOID_HOE.get());
                output.accept(TTItems.VOID_HELM.get());
                output.accept(TTItems.VOID_CHEST.get());
                output.accept(TTItems.VOID_LEGS.get());
                output.accept(TTItems.VOID_BOOTS.get());
                ItemStack elementalSword = new ItemStack(TTItems.ELEMENTAL_SWORD.get());
                InfusionEnchantmentHelper.add(elementalSword, InfusionEnchantment.ARCING, 2);
                output.accept(elementalSword);
                ItemStack elementalSpear = new ItemStack(TTItems.ELEMENTAL_SPEAR.get());
                InfusionEnchantmentHelper.add(elementalSpear, InfusionEnchantment.ESSENCE, 2);
                output.accept(elementalSpear);
                ItemStack elementalPickaxe = new ItemStack(TTItems.ELEMENTAL_PICKAXE.get());
                InfusionEnchantmentHelper.add(elementalPickaxe, InfusionEnchantment.REFINING, 1);
                InfusionEnchantmentHelper.add(elementalPickaxe, InfusionEnchantment.SOUNDING, 2);
                output.accept(elementalPickaxe);
                ItemStack elementalAxe = new ItemStack(TTItems.ELEMENTAL_AXE.get());
                InfusionEnchantmentHelper.add(elementalAxe, InfusionEnchantment.BURROWING, 1);
                InfusionEnchantmentHelper.add(elementalAxe, InfusionEnchantment.COLLECTOR, 1);
                output.accept(elementalAxe);
                ItemStack elementalShovel = new ItemStack(TTItems.ELEMENTAL_SHOVEL.get());
                InfusionEnchantmentHelper.add(elementalShovel, InfusionEnchantment.DESTRUCTIVE, 1);
                output.accept(elementalShovel);
                output.accept(TTItems.ELEMENTAL_HOE.get());
                ItemStack primalCrusher = new ItemStack(TTItems.PRIMAL_CRUSHER.get());
                InfusionEnchantmentHelper.add(primalCrusher, InfusionEnchantment.DESTRUCTIVE, 1);
                InfusionEnchantmentHelper.add(primalCrusher, InfusionEnchantment.REFINING, 1);
                output.accept(primalCrusher);
                output.accept(TTItems.RECHARGE_PEDESTAL.get());
                output.accept(TTItems.LEVITATOR.get());
                output.accept(TTItems.ITEM_GRATE.get());
                output.accept(TTItems.POTION_SPRAYER.get());
                output.accept(TTItems.PATTERN_CRAFTER.get());
                output.accept(TTItems.INLAY.get());
                output.accept(TTItems.DIOPTRA.get());
                output.accept(TTItems.ARCANE_EAR.get());
                output.accept(TTItems.ARCANE_EAR_TOGGLE.get());
                output.accept(TTItems.LAMP_ARCANE.get());
                output.accept(TTItems.LAMP_GROWTH.get());
                output.accept(TTItems.LAMP_FERTILITY.get());
                output.accept(TTItems.HUNGRY_CHEST.get());
                output.accept(TTItems.EVERFULL_URN.get());
                output.accept(TTItems.VIS_GENERATOR.get());
                output.accept(TTItems.CONDENSER.get());
                output.accept(TTItems.FLUX_SCRUBBER.get());
                output.accept(TTItems.CONDENSER_LATTICE.get());
                output.accept(TTItems.CONDENSER_LATTICE_DIRTY.get());
                output.accept(TTItems.STABILIZER.get());
                output.accept(TTItems.REDSTONE_RELAY.get());
                output.accept(TTItems.VOID_SIPHON.get());
                output.accept(TTItems.VIS_BATTERY.get());
                output.accept(TTItems.ACTIVATOR_RAIL.get());
                output.accept(TTItems.SLAB_GREATWOOD.get());
                output.accept(TTItems.SLAB_SILVERWOOD.get());
                output.accept(TTItems.SLAB_ARCANE_STONE.get());
                output.accept(TTItems.SLAB_ARCANE_BRICK.get());
                output.accept(TTItems.SLAB_ANCIENT.get());
                output.accept(TTItems.SLAB_ELDRITCH.get());
                output.accept(TTItems.STAIRS_GREATWOOD.get());
                output.accept(TTItems.STAIRS_SILVERWOOD.get());
                output.accept(TTItems.DOOR_GREATWOOD.get());
                output.accept(TTItems.TRAPDOOR_GREATWOOD.get());
                output.accept(TTItems.FENCE_GREATWOOD.get());
                output.accept(TTItems.FENCE_GATE_GREATWOOD.get());
                output.accept(TTItems.BUTTON_GREATWOOD.get());
                output.accept(TTItems.PRESSURE_PLATE_GREATWOOD.get());
                output.accept(TTItems.DOOR_SILVERWOOD.get());
                output.accept(TTItems.TRAPDOOR_SILVERWOOD.get());
                output.accept(TTItems.FENCE_SILVERWOOD.get());
                output.accept(TTItems.FENCE_GATE_SILVERWOOD.get());
                output.accept(TTItems.BUTTON_SILVERWOOD.get());
                output.accept(TTItems.PRESSURE_PLATE_SILVERWOOD.get());
                output.accept(TTItems.TABLE_WOOD.get());
                output.accept(TTItems.TABLE_STONE.get());
                output.accept(TTItems.PAVING_STONE_TRAVEL.get());
                output.accept(TTItems.PAVING_STONE_BARRIER.get());
                output.accept(TTItems.AMBER_BRICK.get());
                output.accept(TTItems.FLESH_BLOCK.get());
                output.accept(TTItems.OBSIDIAN_TILE.get());
                output.accept(TTItems.OBSIDIAN_TOTEM.get());
                output.accept(TTItems.ELDRITCH_STONE.get());
                output.accept(TTItems.ELDRITCH_ROCK.get());
                output.accept(TTItems.ELDRITCH_CRUST.get());
                output.accept(TTItems.ELDRITCH_CRUST_GLOWING.get());
                output.accept(TTItems.STAIRS_ELDRITCH.get());
                output.accept(TTItems.ELDRITCH_PEDESTAL.get());
                output.accept(TTItems.ELDRITCH_EYE.get());
                output.accept(TTItems.RUNED_TABLET.get());
                output.accept(TTItems.TURRET_BASIC.get());
                output.accept(TTItems.TURRET_ADVANCED.get());
                output.accept(TTItems.ARCANE_BORE.get());
                output.accept(TTItems.GRAPPLE_GUN.get());
                output.accept(TTItems.GRAPPLE_GUN_TIP.get());
                output.accept(TTItems.GRAPPLE_GUN_SPOOL.get());
                output.accept(TTItems.MIND_CLOCKWORK.get());
                output.accept(TTItems.MIND_BIOTHAUMIC.get());
                output.accept(TTItems.MODULE_VISION.get());
                output.accept(TTItems.MODULE_AGGRESSION.get());
                output.accept(TTItems.GOLEM_BUILDER.get());
                output.accept(TTItems.GOLEM_FETTER.get());
                output.accept(TTItems.GOLEM_BELL.get());
                output.accept(TTItems.GOLEM_TOP_HAT.get());
                output.accept(TTItems.GOLEM_FEZ.get());
                output.accept(TTItems.GOLEM_GLASSES.get());
                output.accept(TTItems.GOLEM_BOWTIE.get());
                output.accept(TTItems.GOLEM_VISOR.get());
                output.accept(TTItems.SEAL_BLANK.get());
                output.accept(TTItems.SEAL_PICKUP.get());
                output.accept(TTItems.SEAL_PICKUP_ADVANCED.get());
                output.accept(TTItems.SEAL_FILL.get());
                output.accept(TTItems.SEAL_FILL_ADVANCED.get());
                output.accept(TTItems.SEAL_EMPTY.get());
                output.accept(TTItems.SEAL_EMPTY_ADVANCED.get());
                output.accept(TTItems.SEAL_HARVEST.get());
                output.accept(TTItems.SEAL_BUTCHER.get());
                output.accept(TTItems.SEAL_GUARD.get());
                output.accept(TTItems.SEAL_GUARD_ADVANCED.get());
                output.accept(TTItems.SEAL_LUMBER.get());
                output.accept(TTItems.SEAL_BREAKER.get());
                output.accept(TTItems.SEAL_BREAKER_ADVANCED.get());
                output.accept(TTItems.SEAL_USE.get());
                output.accept(TTItems.SEAL_PROVIDER.get());
                output.accept(TTItems.SEAL_STOCK.get());
                TTGolemParts.materials().forEach(material -> {
                    output.accept(golemPlacer(GolemProperties.createDefault().withMaterial(material)));
                });
                output.accept(TTItems.TRAVELLER_BOOTS.get());
                output.accept(TTItems.THAUMOSTATIC_HARNESS.get());
                output.accept(TTItems.CLOTH_CHEST.get());
                output.accept(TTItems.CLOTH_LEGS.get());
                output.accept(TTItems.CLOTH_BOOTS.get());
                output.accept(TTItems.WISP_SPAWN_EGG.get());
                output.accept(TTItems.BRAINY_ZOMBIE_SPAWN_EGG.get());
                output.accept(TTItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get());
                output.accept(TTItems.BRAINY_DROWNED_SPAWN_EGG.get());
                output.accept(TTItems.BRAINY_HUSK_SPAWN_EGG.get());
                output.accept(TTItems.FIREBAT_SPAWN_EGG.get());
                output.accept(TTItems.MIND_SPIDER_SPAWN_EGG.get());
                output.accept(TTItems.THAUMIC_SLIME_SPAWN_EGG.get());
                output.accept(TTItems.TAINTED_GOO.get());
                output.accept(TTItems.TAINT_TENDRIL.get());
                output.accept(TTItems.BOTTLE_TAINT.get());
                output.accept(TTItems.TAINT_ROCK.get());
                output.accept(TTItems.TAINT_SOIL.get());
                output.accept(TTItems.TAINT_CRUST.get());
                output.accept(TTItems.TAINT_GEYSER.get());
                output.accept(TTItems.TAINT_LOG.get());
                output.accept(TTItems.TAINT_FEATURE.get());
                output.accept(TTItems.TAINT_FIBRE.get());
                output.accept(TTItems.TAINT_SPORE_STALK.get());
                output.accept(TTItems.TAINT_CRAWLER_SPAWN_EGG.get());
                output.accept(TTItems.TAINTACLE_SPAWN_EGG.get());
                output.accept(TTItems.TAINT_SWARM_SPAWN_EGG.get());
                output.accept(TTItems.TAINT_SEED_SPAWN_EGG.get());
                output.accept(TTItems.TAINT_SEED_PRIME_SPAWN_EGG.get());
                output.accept(TTItems.CRIMSON_BLADE.get());
                output.accept(TTItems.CRIMSON_PLATE_HELM.get());
                output.accept(TTItems.CRIMSON_PLATE_CHEST.get());
                output.accept(TTItems.CRIMSON_PLATE_LEGS.get());
                output.accept(TTItems.CRIMSON_BOOTS.get());
                output.accept(TTItems.CRIMSON_ROBE_HELM.get());
                output.accept(TTItems.CRIMSON_ROBE_CHEST.get());
                output.accept(TTItems.CRIMSON_ROBE_LEGS.get());
                output.accept(TTItems.FORTRESS_HELM.get());
                output.accept(TTItems.FORTRESS_CHEST.get());
                output.accept(TTItems.FORTRESS_LEGS.get());
                output.accept(TTItems.VOID_ROBE_HELM.get());
                output.accept(TTItems.VOID_ROBE_CHEST.get());
                output.accept(TTItems.VOID_ROBE_LEGS.get());
                output.accept(TTItems.AMULET_MUNDANE.get());
                output.accept(TTItems.RING_MUNDANE.get());
                output.accept(TTItems.GIRDLE_MUNDANE.get());
                output.accept(TTItems.RING_APPRENTICE.get());
                output.accept(TTItems.AMULET_FANCY.get());
                output.accept(TTItems.RING_FANCY.get());
                output.accept(TTItems.GIRDLE_FANCY.get());
                output.accept(TTItems.AMULET_VIS.get());
                output.accept(TTItems.AMULET_VIS_CRAFTED.get());
                output.accept(TTItems.CHARM_UNDYING.get());
                output.accept(TTItems.CLOUD_RING.get());
                output.accept(TTItems.CURIOSITY_BAND.get());
                output.accept(TTItems.VERDANT_CHARM.get());
                output.accept(verdantVariant(1));
                output.accept(verdantVariant(2));
                output.accept(TTItems.VOIDSEER_CHARM.get());
                output.accept(TTItems.FOCUS_POUCH.get());
                output.accept(TTItems.SANITY_CHECKER.get());
                output.accept(TTItems.CURIO_ARCANE.get());
                output.accept(TTItems.CURIO_PRESERVED.get());
                output.accept(TTItems.CURIO_ANCIENT.get());
                output.accept(TTItems.CURIO_ELDRITCH.get());
                output.accept(TTItems.CURIO_KNOWLEDGE.get());
                output.accept(TTItems.CURIO_TWISTED.get());
                output.accept(TTItems.CURIO_RITES.get());
                output.accept(TTItems.CREATIVE_FLUX_SPONGE.get());
                output.accept(TTItems.MIRROR.get());
                output.accept(TTItems.MIRROR_ESSENTIA.get());
                output.accept(TTItems.HAND_MIRROR.get());
                output.accept(TTItems.RESONATOR.get());
                output.accept(TTItems.PECH_WAND.get());
                output.accept(TTItems.LOOT_BAG_COMMON.get());
                output.accept(TTItems.LOOT_BAG_UNCOMMON.get());
                output.accept(TTItems.LOOT_BAG_RARE.get());
                output.accept(TTItems.LOOT_URN_COMMON.get());
                output.accept(TTItems.LOOT_URN_UNCOMMON.get());
                output.accept(TTItems.LOOT_URN_RARE.get());
                output.accept(TTItems.LOOT_CRATE_COMMON.get());
                output.accept(TTItems.LOOT_CRATE_UNCOMMON.get());
                output.accept(TTItems.LOOT_CRATE_RARE.get());
                output.accept(TTItems.PECH_SPAWN_EGG.get());
                output.accept(TTItems.ELDRITCH_CRAB_SPAWN_EGG.get());
                output.accept(TTItems.INHABITED_ZOMBIE_SPAWN_EGG.get());
                output.accept(TTItems.ELDRITCH_GUARDIAN_SPAWN_EGG.get());
                output.accept(TTItems.CULTIST_KNIGHT_SPAWN_EGG.get());
                output.accept(TTItems.CULTIST_CLERIC_SPAWN_EGG.get());
                output.accept(TTItems.CULTIST_PORTAL_LESSER_SPAWN_EGG.get());
                output.accept(TTItems.CULTIST_LEADER_SPAWN_EGG.get());
                output.accept(TTItems.CULTIST_PORTAL_GREATER_SPAWN_EGG.get());
                output.accept(TTItems.ELDRITCH_WARDEN_SPAWN_EGG.get());
                output.accept(TTItems.ELDRITCH_GOLEM_SPAWN_EGG.get());
                output.accept(TTItems.TAINTACLE_GIANT_SPAWN_EGG.get());
            }).build());

    private static void addIfTag(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output, TagKey<Item> tag, Item item) {
        Optional<HolderSet.Named<Item>> tagOpt = parameters.holders().get(tag);
        if (tagOpt.isPresent()) {
            if (tagOpt.get().stream().findAny().isPresent())
                output.accept(item);
        }
    }

    private static ItemStack golemPlacer(GolemProperties props) {
        ItemStack stack = new ItemStack(TTItems.GOLEM_PLACER.get());
        stack.set(TTDataComponents.GOLEM_PROPERTIES.get(), props);
        return stack;
    }

    private static ItemStack verdantVariant(int type) {
        ItemStack stack = new ItemStack(TTItems.VERDANT_CHARM.get());
        stack.set(TTDataComponents.VERDANT_TYPE.get(), type);
        return stack;
    }

    private TTCreativeTabs() {}

    private static ItemStack chargedWand(WandCap cap, WandRod rod, boolean sceptre) {
        ItemStack stack = ItemWand.create(TTItems.WAND.get(), cap, rod, sceptre);
        WandVisHelper.fill(stack);
        return stack;
    }

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
