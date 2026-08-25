package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TCIds.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THAUMATURGE = CREATIVE_MODE_TABS.register(
            "thaumaturge",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumaturge"))
                    .icon(() -> new ItemStack(TCItems.THAUMONOMICON.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(TCItems.THAUMONOMICON.get());
                        output.accept(TCItems.THAUMONOMICON_CHEAT.get());
                        output.accept(TCItems.THAUMONOMICON_SHARING.get());
                        output.accept(TCItems.THAUMONOMICON_LINKING.get());
                        output.accept(TCItems.CREATIVE_NODE_PLACER.get());
                        output.accept(TCItems.SALIS_MUNDUS.get());
                        output.accept(TCItems.THAUMOMETER.get());
                        output.accept(TCItems.SCRIBING_TOOLS.get());
                        output.accept(TCItems.RESEARCH_NOTE.get());
                        output.accept(TCItems.CELESTIAL_NOTES.get());
                        output.accept(TCItems.MANA_BEAN.get());
                        output.accept(TCItems.VIS_RESONATOR.get());
                        for (CelestialBody body : CelestialBody.values()) {
                            output.accept(CelestialNotesItem.stackOf(body));
                        }
                        output.accept(TCItems.GOGGLES_REVEALING.get());
                        output.accept(chargedWand(TCWandParts.CAP_IRON.get(), TCWandParts.ROD_WOOD.get(), false));
                        output.accept(chargedWand(TCWandParts.CAP_GOLD.get(), TCWandParts.ROD_GREATWOOD.get(), false));
                        output.accept(
                                chargedWand(TCWandParts.CAP_THAUMIUM.get(), TCWandParts.ROD_SILVERWOOD.get(), false));
                        output.accept(
                                chargedWand(TCWandParts.CAP_THAUMIUM.get(), TCWandParts.ROD_SILVERWOOD.get(), true));
                        output.accept(chargedWand(TCWandParts.CAP_VOID.get(), TCWandParts.STAFF_PRIMAL.get(), false));
                        output.accept(TCItems.WAND_CAP_IRON.get());
                        output.accept(TCItems.WAND_CAP_COPPER.get());
                        output.accept(TCItems.WAND_CAP_GOLD.get());
                        output.accept(TCItems.WAND_CAP_SILVER_INERT.get());
                        output.accept(TCItems.WAND_CAP_SILVER.get());
                        output.accept(TCItems.WAND_CAP_THAUMIUM_INERT.get());
                        output.accept(TCItems.WAND_CAP_THAUMIUM.get());
                        output.accept(TCItems.WAND_CAP_VOID_INERT.get());
                        output.accept(TCItems.WAND_CAP_VOID.get());
                        output.accept(TCItems.WAND_ROD_GREATWOOD.get());
                        output.accept(TCItems.WAND_ROD_OBSIDIAN.get());
                        output.accept(TCItems.WAND_ROD_BLAZE.get());
                        output.accept(TCItems.WAND_ROD_ICE.get());
                        output.accept(TCItems.WAND_ROD_QUARTZ.get());
                        output.accept(TCItems.WAND_ROD_BONE.get());
                        output.accept(TCItems.WAND_ROD_REED.get());
                        output.accept(TCItems.WAND_ROD_SILVERWOOD.get());
                        output.accept(TCItems.STAFF_ROD_GREATWOOD.get());
                        output.accept(TCItems.STAFF_ROD_OBSIDIAN.get());
                        output.accept(TCItems.STAFF_ROD_BLAZE.get());
                        output.accept(TCItems.STAFF_ROD_ICE.get());
                        output.accept(TCItems.STAFF_ROD_QUARTZ.get());
                        output.accept(TCItems.STAFF_ROD_BONE.get());
                        output.accept(TCItems.STAFF_ROD_REED.get());
                        output.accept(TCItems.STAFF_ROD_SILVERWOOD.get());
                        output.accept(TCItems.STAFF_ROD_PRIMAL.get());
                        output.accept(TCItems.PRIMAL_CHARM.get());
                        output.accept(TCItems.NODE_STABILIZER.get());
                        output.accept(TCItems.NODE_STABILIZER_ADVANCED.get());
                        output.accept(TCItems.NODE_TRANSDUCER.get());
                        output.accept(TCItems.VIS_RELAY.get());
                        output.accept(TCItems.FOCUS_1.get());
                        output.accept(TCItems.FOCUS_2.get());
                        output.accept(TCItems.FOCUS_3.get());
                        output.accept(TCItems.LABEL.get());
                        output.accept(TCItems.RESEARCH_TABLE.get());
                        output.accept(TCBlocks.DECONSTRUCTION_TABLE.get());
                        output.accept(TCItems.ARCANE_WORKBENCH_CHARGER.get());
                        output.accept(TCItems.FOCAL_MANIPULATOR.get());
                        output.accept(TCItems.ARCANE_WORKBENCH.get());
                        output.accept(TCItems.CRUCIBLE.get());
                        output.accept(TCItems.ALCHEMICAL_CONSTRUCT.get());
                        output.accept(TCItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
                        output.accept(TCItems.CENTRIFUGE.get());

                        output.accept(TCItems.ALEMBIC.get());
                        output.accept(TCItems.BELLOWS.get());
                        output.accept(TCItems.SMELTER_BASIC.get());
                        output.accept(TCItems.SMELTER_THAUMIUM.get());
                        output.accept(TCItems.SMELTER_VOID.get());
                        output.accept(TCItems.SMELTER_AUX.get());
                        output.accept(TCItems.SMELTER_VENT.get());
                        output.accept(TCItems.ESSENTIA_INPUT.get());
                        output.accept(TCItems.ESSENTIA_OUTPUT.get());
                        output.accept(TCItems.BUCKET_LIQUID_DEATH.get());
                        output.accept(TCItems.JAR_BRACE.get());
                        output.accept(TCItems.JAR_NORMAL.get());
                        output.accept(TCItems.JAR_VOID.get());
                        output.accept(TCItems.JAR_BRAIN.get());
                        output.accept(TCItems.JAR_NODE.get());
                        output.accept(TCItems.TUBE.get());
                        output.accept(TCItems.TUBE_VALVE.get());
                        output.accept(TCItems.TUBE_RESTRICT.get());
                        output.accept(TCItems.TUBE_FILTER.get());
                        output.accept(TCItems.TUBE_ONEWAY.get());
                        output.accept(TCItems.TUBE_BUFFER.get());
                        output.accept(TCItems.BRAIN_BOX.get());

                        output.accept(TCItems.CRYSTAL_AER.get());
                        output.accept(TCItems.CRYSTAL_IGNIS.get());
                        output.accept(TCItems.CRYSTAL_AQUA.get());
                        output.accept(TCItems.CRYSTAL_TERRA.get());
                        output.accept(TCItems.CRYSTAL_ORDO.get());
                        output.accept(TCItems.CRYSTAL_PERDITIO.get());
                        output.accept(TCItems.CRYSTAL_VITIUM.get());

                        output.accept(TCItems.ORE_AMBER.get());
                        output.accept(TCItems.ORE_CINNABAR.get());
                        output.accept(TCItems.ORE_QUARTZ.get());
                        output.accept(TCItems.AMBER_BLOCK);
                        output.accept(TCItems.METAL_BRASS_BLOCK);
                        output.accept(TCItems.METAL_THAUMIUM_BLOCK);
                        output.accept(TCItems.METAL_VOID_BLOCK);

                        output.accept(TCItems.QUICKSILVER.get());
                        output.accept(TCItems.AMBER.get());
                        output.accept(TCItems.INGOT_BRASS.get());
                        output.accept(TCItems.INGOT_THAUMIUM.get());
                        output.accept(TCItems.INGOT_VOID.get());

                        output.accept(TCItems.NUGGET_QUARTZ.get());
                        output.accept(TCItems.NUGGET_QUICKSILVER.get());
                        output.accept(TCItems.NUGGET_BRASS.get());
                        output.accept(TCItems.NUGGET_THAUMIUM.get());
                        output.accept(TCItems.NUGGET_VOID.get());

                        output.accept(TCItems.PLATE_IRON.get());
                        output.accept(TCItems.PLATE_BRASS.get());
                        output.accept(TCItems.PLATE_THAUMIUM.get());
                        output.accept(TCItems.PLATE_VOID.get());

                        output.accept(TCItems.CLUSTER_IRON.get());
                        output.accept(TCItems.CLUSTER_GOLD.get());
                        output.accept(TCItems.CLUSTER_COPPER.get());
                        addIfTag(parameters, output, TCItemTags.INGOTS_TIN, TCItems.CLUSTER_TIN.get());
                        addIfTag(parameters, output, TCItemTags.INGOTS_SILVER, TCItems.CLUSTER_SILVER.get());
                        addIfTag(parameters, output, TCItemTags.INGOTS_LEAD, TCItems.CLUSTER_LEAD.get());
                        output.accept(TCItems.CLUSTER_CINNABAR.get());
                        output.accept(TCItems.CLUSTER_QUARTZ.get());

                        output.accept(TCItems.CHUNK_BEEF.get());
                        output.accept(TCItems.CHUNK_CHICKEN.get());
                        output.accept(TCItems.CHUNK_PORK.get());
                        output.accept(TCItems.CHUNK_FISH.get());
                        output.accept(TCItems.CHUNK_RABBIT.get());
                        output.accept(TCItems.CHUNK_MUTTON.get());
                        output.accept(TCItems.TRIPLE_MEAT_TREAT.get());

                        output.accept(TCItems.BRAIN.get());
                        output.accept(TCItems.FABRIC.get());
                        output.accept(TCItems.FILTER.get());
                        output.accept(TCItems.MIRRORED_GLASS.get());
                        output.accept(TCItems.MECHANISM_SIMPLE.get());
                        output.accept(TCItems.MECHANISM_COMPLEX.get());
                        output.accept(TCItems.MORPHIC_RESONATOR.get());
                        output.accept(TCItems.VOID_SEED.get());
                        output.accept(TCItems.CAUSALITY_COLLAPSER.get());
                        output.accept(TCItems.PRIMORDIAL_PEARL.get());

                        output.accept(TCItems.INFERNAL_FURNACE.get());
                        output.accept(TCItems.THAUMATORIUM.get());
                        output.accept(TCItems.INFUSION_MATRIX.get());
                        output.accept(TCItems.PEDESTAL_ARCANE.get());
                        output.accept(TCItems.PEDESTAL_ANCIENT.get());
                        output.accept(TCItems.PEDESTAL_ELDRITCH.get());
                        output.accept(TCItems.PILLAR_ARCANE.get());
                        output.accept(TCItems.PILLAR_ANCIENT.get());
                        output.accept(TCItems.PILLAR_ELDRITCH.get());
                        output.accept(TCItems.MATRIX_SPEED.get());
                        output.accept(TCItems.MATRIX_COST.get());

                        output.accept(TCItems.SPA.get());
                        output.accept(TCItems.BATH_SALTS.get());
                        output.accept(TCItems.SANITY_SOAP.get());

                        output.accept(TCItems.ALUMENTUM.get());
                        for (DyeColor dye : DyeColor.values()) {
                            output.accept(TCItems.NITORS.get(dye).get());
                        }
                        for (DyeColor dye : DyeColor.values()) {
                            output.accept(TCItems.BANNERS.get(dye).get());
                        }
                        output.accept(TCItems.BANNER_CRIMSON_CULT.get());
                        output.accept(TCItems.TALLOW.get());
                        for (DyeColor dye : DyeColor.values()) {
                            output.accept(TCItems.CANDLES.get(dye).get());
                        }

                        output.accept(TCItems.STONE_ARCANE.get());
                        output.accept(TCItems.STONE_ARCANE_BRICK.get());
                        output.accept(TCItems.STAIRS_ARCANE.get());
                        output.accept(TCItems.STAIRS_ARCANE_BRICK.get());
                        output.accept(TCItems.STONE_ANCIENT.get());
                        output.accept(TCItems.STONE_ANCIENT_TILE.get());
                        output.accept(TCItems.STONE_ANCIENT_GLYPHED.get());
                        output.accept(TCItems.STAIRS_ANCIENT.get());
                        output.accept(TCItems.STONE_ELDRITCH_TILE.get());
                        output.accept(TCItems.STONE_POROUS.get());
                        output.accept(TCItems.SAPLING_GREATWOOD.get());
                        output.accept(TCItems.SAPLING_SILVERWOOD.get());
                        output.accept(TCItems.LOG_GREATWOOD.get());
                        output.accept(TCItems.WOOD_GREATWOOD.get());
                        output.accept(TCItems.STRIPPED_LOG_GREATWOOD.get());
                        output.accept(TCItems.STRIPPED_WOOD_GREATWOOD.get());
                        output.accept(TCItems.LOG_SILVERWOOD.get());
                        output.accept(TCItems.WOOD_SILVERWOOD.get());
                        output.accept(TCItems.STRIPPED_LOG_SILVERWOOD.get());
                        output.accept(TCItems.STRIPPED_WOOD_SILVERWOOD.get());
                        output.accept(TCItems.LEAVES_GREATWOOD.get());
                        output.accept(TCItems.LEAVES_SILVERWOOD.get());
                        output.accept(TCItems.PLANK_GREATWOOD.get());
                        output.accept(TCItems.PLANK_SILVERWOOD.get());
                        output.accept(TCItems.PLANT_SHIMMERLEAF.get());
                        output.accept(TCItems.PLANT_CINDERPEARL.get());
                        output.accept(TCItems.PLANT_VISHROOM.get());
                        output.accept(TCItems.GRASS_AMBIENT.get());
                        HolderLookup.RegistryLookup<IAspect> aspectRegistry =
                                parameters.holders().lookupOrThrow(IAspect.REGISTRY_KEY);
                        for (Holder<IAspect> aspect : aspectRegistry
                                .listElements()
                                .sorted(Comparator.comparing(h -> !h.value().isPrimal()))
                                .toList()) {
                            output.accept(EssentiaCrystalFactory.of(aspect));
                        }
                        output.accept(TCItems.PHIAL.get());
                        for (Holder<IAspect> aspect : aspectRegistry
                                .listElements()
                                .sorted(Comparator.comparing(h -> !h.value().isPrimal()))
                                .toList()) {
                            output.accept(PhialItem.makeFilled(aspect));
                        }
                        output.accept(TCItems.THAUMIUM_SWORD.get());
                        output.accept(TCItems.THAUMIUM_PICKAXE.get());
                        output.accept(TCItems.THAUMIUM_AXE.get());
                        output.accept(TCItems.THAUMIUM_SHOVEL.get());
                        output.accept(TCItems.THAUMIUM_HOE.get());
                        output.accept(TCItems.THAUMIUM_HELM.get());
                        output.accept(TCItems.THAUMIUM_CHEST.get());
                        output.accept(TCItems.THAUMIUM_LEGS.get());
                        output.accept(TCItems.THAUMIUM_BOOTS.get());
                        output.accept(TCItems.VOID_SWORD.get());
                        output.accept(TCItems.VOID_PICKAXE.get());
                        output.accept(TCItems.VOID_AXE.get());
                        output.accept(TCItems.VOID_SHOVEL.get());
                        output.accept(TCItems.VOID_HOE.get());
                        output.accept(TCItems.VOID_HELM.get());
                        output.accept(TCItems.VOID_CHEST.get());
                        output.accept(TCItems.VOID_LEGS.get());
                        output.accept(TCItems.VOID_BOOTS.get());
                        ItemStack elementalSword = new ItemStack(TCItems.ELEMENTAL_SWORD.get());
                        InfusionEnchantmentHelper.add(elementalSword, InfusionEnchantment.ARCING, 2);
                        output.accept(elementalSword);
                        ItemStack elementalPickaxe = new ItemStack(TCItems.ELEMENTAL_PICKAXE.get());
                        InfusionEnchantmentHelper.add(elementalPickaxe, InfusionEnchantment.REFINING, 1);
                        InfusionEnchantmentHelper.add(elementalPickaxe, InfusionEnchantment.SOUNDING, 2);
                        output.accept(elementalPickaxe);
                        ItemStack elementalAxe = new ItemStack(TCItems.ELEMENTAL_AXE.get());
                        InfusionEnchantmentHelper.add(elementalAxe, InfusionEnchantment.BURROWING, 1);
                        InfusionEnchantmentHelper.add(elementalAxe, InfusionEnchantment.COLLECTOR, 1);
                        output.accept(elementalAxe);
                        ItemStack elementalShovel = new ItemStack(TCItems.ELEMENTAL_SHOVEL.get());
                        InfusionEnchantmentHelper.add(elementalShovel, InfusionEnchantment.DESTRUCTIVE, 1);
                        output.accept(elementalShovel);
                        output.accept(TCItems.ELEMENTAL_HOE.get());
                        ItemStack primalCrusher = new ItemStack(TCItems.PRIMAL_CRUSHER.get());
                        InfusionEnchantmentHelper.add(primalCrusher, InfusionEnchantment.DESTRUCTIVE, 1);
                        InfusionEnchantmentHelper.add(primalCrusher, InfusionEnchantment.REFINING, 1);
                        output.accept(primalCrusher);
                        output.accept(TCItems.RECHARGE_PEDESTAL.get());
                        output.accept(TCItems.LEVITATOR.get());
                        output.accept(TCItems.POTION_SPRAYER.get());
                        output.accept(TCItems.PATTERN_CRAFTER.get());
                        output.accept(TCItems.INLAY.get());
                        output.accept(TCItems.DIOPTRA.get());
                        output.accept(TCItems.ARCANE_EAR.get());
                        output.accept(TCItems.ARCANE_EAR_TOGGLE.get());
                        output.accept(TCItems.LAMP_ARCANE.get());
                        output.accept(TCItems.LAMP_GROWTH.get());
                        output.accept(TCItems.LAMP_FERTILITY.get());
                        output.accept(TCItems.HUNGRY_CHEST.get());
                        output.accept(TCItems.EVERFULL_URN.get());
                        output.accept(TCItems.VIS_GENERATOR.get());
                        output.accept(TCItems.CONDENSER.get());
                        output.accept(TCItems.CONDENSER_LATTICE.get());
                        output.accept(TCItems.CONDENSER_LATTICE_DIRTY.get());
                        output.accept(TCItems.STABILIZER.get());
                        output.accept(TCItems.REDSTONE_RELAY.get());
                        output.accept(TCItems.VOID_SIPHON.get());
                        output.accept(TCItems.VIS_BATTERY.get());
                        output.accept(TCItems.ACTIVATOR_RAIL.get());
                        output.accept(TCItems.SLAB_GREATWOOD.get());
                        output.accept(TCItems.SLAB_SILVERWOOD.get());
                        output.accept(TCItems.SLAB_ARCANE_STONE.get());
                        output.accept(TCItems.SLAB_ARCANE_BRICK.get());
                        output.accept(TCItems.SLAB_ANCIENT.get());
                        output.accept(TCItems.SLAB_ELDRITCH.get());
                        output.accept(TCItems.STAIRS_GREATWOOD.get());
                        output.accept(TCItems.STAIRS_SILVERWOOD.get());
                        output.accept(TCItems.TABLE_WOOD.get());
                        output.accept(TCItems.TABLE_STONE.get());
                        output.accept(TCItems.PAVING_STONE_TRAVEL.get());
                        output.accept(TCItems.PAVING_STONE_BARRIER.get());
                        output.accept(TCItems.AMBER_BRICK.get());
                        output.accept(TCItems.FLESH_BLOCK.get());
                        output.accept(TCItems.OBSIDIAN_TILE.get());
                        output.accept(TCItems.OBSIDIAN_TOTEM.get());
                        output.accept(TCItems.ELDRITCH_STONE.get());
                        output.accept(TCItems.ELDRITCH_ROCK.get());
                        output.accept(TCItems.ELDRITCH_CRUST.get());
                        output.accept(TCItems.ELDRITCH_CRUST_GLOWING.get());
                        output.accept(TCItems.STAIRS_ELDRITCH.get());
                        output.accept(TCItems.ELDRITCH_PEDESTAL.get());
                        output.accept(TCItems.ELDRITCH_EYE.get());
                        output.accept(TCItems.RUNED_TABLET.get());
                        output.accept(TCItems.TURRET_BASIC.get());
                        output.accept(TCItems.TURRET_ADVANCED.get());
                        output.accept(TCItems.TURRET_BORE.get());
                        output.accept(TCItems.GRAPPLE_GUN.get());
                        output.accept(TCItems.GRAPPLE_GUN_TIP.get());
                        output.accept(TCItems.GRAPPLE_GUN_SPOOL.get());
                        output.accept(TCItems.MIND_CLOCKWORK.get());
                        output.accept(TCItems.MIND_BIOTHAUMIC.get());
                        output.accept(TCItems.MODULE_VISION.get());
                        output.accept(TCItems.MODULE_AGGRESSION.get());
                        output.accept(TCItems.GOLEM_BUILDER.get());
                        output.accept(TCItems.GOLEM_BELL.get());
                        output.accept(TCItems.GOLEM_TOP_HAT.get());
                        output.accept(TCItems.GOLEM_FEZ.get());
                        output.accept(TCItems.GOLEM_GLASSES.get());
                        output.accept(TCItems.GOLEM_BOWTIE.get());
                        output.accept(TCItems.GOLEM_VISOR.get());
                        output.accept(TCItems.SEAL_BLANK.get());
                        output.accept(TCItems.SEAL_PICKUP.get());
                        output.accept(TCItems.SEAL_PICKUP_ADVANCED.get());
                        output.accept(TCItems.SEAL_FILL.get());
                        output.accept(TCItems.SEAL_FILL_ADVANCED.get());
                        output.accept(TCItems.SEAL_EMPTY.get());
                        output.accept(TCItems.SEAL_EMPTY_ADVANCED.get());
                        output.accept(TCItems.SEAL_HARVEST.get());
                        output.accept(TCItems.SEAL_BUTCHER.get());
                        output.accept(TCItems.SEAL_GUARD.get());
                        output.accept(TCItems.SEAL_GUARD_ADVANCED.get());
                        output.accept(TCItems.SEAL_LUMBER.get());
                        output.accept(TCItems.SEAL_BREAKER.get());
                        output.accept(TCItems.SEAL_BREAKER_ADVANCED.get());
                        output.accept(TCItems.SEAL_USE.get());
                        output.accept(TCItems.SEAL_PROVIDER.get());
                        output.accept(TCItems.SEAL_STOCK.get());
                        TCGolemParts.materials().forEach(material -> {
                            GolemProperties properties = GolemProperties.createDefault();
                            properties.setMaterial(material);
                            output.accept(golemPlacer(properties));
                        });
                        output.accept(TCItems.TRAVELLER_BOOTS.get());
                        output.accept(TCItems.CLOTH_CHEST.get());
                        output.accept(TCItems.CLOTH_LEGS.get());
                        output.accept(TCItems.CLOTH_BOOTS.get());
                        output.accept(TCItems.WISP_SPAWN_EGG.get());
                        output.accept(TCItems.BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(TCItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(TCItems.FIREBAT_SPAWN_EGG.get());
                        output.accept(TCItems.MIND_SPIDER_SPAWN_EGG.get());
                        output.accept(TCItems.THAUMIC_SLIME_SPAWN_EGG.get());
                        output.accept(TCItems.BOTTLE_TAINT.get());
                        output.accept(TCItems.TAINT_ROCK.get());
                        output.accept(TCItems.TAINT_SOIL.get());
                        output.accept(TCItems.TAINT_CRUST.get());
                        output.accept(TCItems.TAINT_GEYSER.get());
                        output.accept(TCItems.TAINT_LOG.get());
                        output.accept(TCItems.TAINT_FEATURE.get());
                        output.accept(TCItems.TAINT_FIBRE.get());
                        output.accept(TCItems.TAINT_CRAWLER_SPAWN_EGG.get());
                        output.accept(TCItems.TAINTACLE_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SWARM_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SEED_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SEED_PRIME_SPAWN_EGG.get());
                        output.accept(TCItems.CRIMSON_BLADE.get());
                        output.accept(TCItems.CRIMSON_PLATE_HELM.get());
                        output.accept(TCItems.CRIMSON_PLATE_CHEST.get());
                        output.accept(TCItems.CRIMSON_PLATE_LEGS.get());
                        output.accept(TCItems.CRIMSON_BOOTS.get());
                        output.accept(TCItems.CRIMSON_ROBE_HELM.get());
                        output.accept(TCItems.CRIMSON_ROBE_CHEST.get());
                        output.accept(TCItems.CRIMSON_ROBE_LEGS.get());
                        output.accept(TCItems.FORTRESS_HELM.get());
                        output.accept(TCItems.FORTRESS_CHEST.get());
                        output.accept(TCItems.FORTRESS_LEGS.get());
                        output.accept(TCItems.VOID_ROBE_HELM.get());
                        output.accept(TCItems.VOID_ROBE_CHEST.get());
                        output.accept(TCItems.VOID_ROBE_LEGS.get());
                        output.accept(TCItems.AMULET_MUNDANE.get());
                        output.accept(TCItems.RING_MUNDANE.get());
                        output.accept(TCItems.GIRDLE_MUNDANE.get());
                        output.accept(TCItems.RING_APPRENTICE.get());
                        output.accept(TCItems.AMULET_FANCY.get());
                        output.accept(TCItems.RING_FANCY.get());
                        output.accept(TCItems.GIRDLE_FANCY.get());
                        output.accept(TCItems.AMULET_VIS.get());
                        output.accept(TCItems.AMULET_VIS_CRAFTED.get());
                        output.accept(TCItems.CHARM_UNDYING.get());
                        output.accept(TCItems.CLOUD_RING.get());
                        output.accept(TCItems.CURIOSITY_BAND.get());
                        output.accept(TCItems.VERDANT_CHARM.get());
                        output.accept(verdantVariant(1));
                        output.accept(verdantVariant(2));
                        output.accept(TCItems.VOIDSEER_CHARM.get());
                        output.accept(TCItems.FOCUS_POUCH.get());
                        output.accept(TCItems.SANITY_CHECKER.get());
                        output.accept(TCItems.CURIO_ARCANE.get());
                        output.accept(TCItems.CURIO_PRESERVED.get());
                        output.accept(TCItems.CURIO_ANCIENT.get());
                        output.accept(TCItems.CURIO_ELDRITCH.get());
                        output.accept(TCItems.CURIO_KNOWLEDGE.get());
                        output.accept(TCItems.CURIO_TWISTED.get());
                        output.accept(TCItems.CURIO_RITES.get());
                        output.accept(TCItems.CREATIVE_FLUX_SPONGE.get());
                        output.accept(TCItems.MIRROR.get());
                        output.accept(TCItems.MIRROR_ESSENTIA.get());
                        output.accept(TCItems.HAND_MIRROR.get());
                        output.accept(TCItems.RESONATOR.get());
                        output.accept(TCItems.PECH_WAND.get());
                        output.accept(TCItems.LOOT_BAG_COMMON.get());
                        output.accept(TCItems.LOOT_BAG_UNCOMMON.get());
                        output.accept(TCItems.LOOT_BAG_RARE.get());
                        output.accept(TCItems.LOOT_URN_COMMON.get());
                        output.accept(TCItems.LOOT_URN_UNCOMMON.get());
                        output.accept(TCItems.LOOT_URN_RARE.get());
                        output.accept(TCItems.LOOT_CRATE_COMMON.get());
                        output.accept(TCItems.LOOT_CRATE_UNCOMMON.get());
                        output.accept(TCItems.LOOT_CRATE_RARE.get());
                        output.accept(TCItems.PECH_SPAWN_EGG.get());
                        output.accept(TCItems.ELDRITCH_CRAB_SPAWN_EGG.get());
                        output.accept(TCItems.INHABITED_ZOMBIE_SPAWN_EGG.get());
                        output.accept(TCItems.ELDRITCH_GUARDIAN_SPAWN_EGG.get());
                        output.accept(TCItems.CULTIST_KNIGHT_SPAWN_EGG.get());
                        output.accept(TCItems.CULTIST_CLERIC_SPAWN_EGG.get());
                        output.accept(TCItems.CULTIST_PORTAL_LESSER_SPAWN_EGG.get());
                        output.accept(TCItems.CULTIST_LEADER_SPAWN_EGG.get());
                        output.accept(TCItems.CULTIST_PORTAL_GREATER_SPAWN_EGG.get());
                        output.accept(TCItems.ELDRITCH_WARDEN_SPAWN_EGG.get());
                        output.accept(TCItems.ELDRITCH_GOLEM_SPAWN_EGG.get());
                        output.accept(TCItems.TAINTACLE_GIANT_SPAWN_EGG.get());
                    })
                    .build());

    private static void addIfTag(
            CreativeModeTab.ItemDisplayParameters parameters,
            CreativeModeTab.Output output,
            TagKey<Item> tag,
            Item item) {
        Optional<HolderSet.Named<Item>> tagOpt =
                parameters.holders().lookupOrThrow(Registries.ITEM).get(tag);
        if (tagOpt.isPresent()) {
            if (tagOpt.get().stream().findAny().isPresent()) output.accept(item);
        }
    }

    private static ItemStack golemPlacer(GolemProperties props) {
        ItemStack stack = new ItemStack(TCItems.GOLEM_PLACER.get());
        stack.set(TCDataComponents.GOLEM_PROPERTIES.get(), props);
        return stack;
    }

    private static ItemStack verdantVariant(int type) {
        ItemStack stack = new ItemStack(TCItems.VERDANT_CHARM.get());
        stack.set(TCDataComponents.VERDANT_TYPE.get(), type);
        return stack;
    }

    private TCCreativeTabs() {}

    private static ItemStack chargedWand(WandCap cap, WandRod rod, boolean sceptre) {
        ItemStack stack = ItemWand.create(TCItems.WAND.get(), cap, rod, sceptre);
        WandVisHelper.fill(stack);
        return stack;
    }

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
