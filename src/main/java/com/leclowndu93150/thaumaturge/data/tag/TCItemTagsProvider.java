package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosTags;

public final class TCItemTagsProvider extends ItemTagsProvider {
    public TCItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, TCIds.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
        copy(TCBlockTags.MAGICAL_PLANTS, TCItemTags.MAGICAL_PLANTS);
        copy(TCBlockTags.GREATWOOD_LOGS, TCItemTags.GREATWOOD_LOGS);
        tag(TCItemTags.SILVERWOOD_LOGS)
                .add(TCItems.LOG_SILVERWOOD.get())
                .add(TCItems.WOOD_SILVERWOOD.get())
                .add(TCItems.STRIPPED_LOG_SILVERWOOD.get())
                .add(TCItems.STRIPPED_WOOD_SILVERWOOD.get());
        copy(TCBlockTags.PLANKS_GREATWOOD, TCItemTags.PLANKS_GREATWOOD);
        copy(TCBlockTags.PLANKS_SILVERWOOD, TCItemTags.PLANKS_SILVERWOOD);
        copy(TCBlockTags.PLANKS, TCItemTags.PLANKS);
        copy(Tags.Blocks.STRIPPED_LOGS, Tags.Items.STRIPPED_LOGS);
        copy(Tags.Blocks.STRIPPED_WOODS, Tags.Items.STRIPPED_WOODS);
        for (DyeColor dye : DyeColor.values()) {
            tag(TCItemTags.CANDLES).add(TCItems.CANDLES.get(dye).get());
            tag(TCItemTags.NITORS).add(TCItems.NITORS.get(dye).get());
        }

        tag(TCItemTags.WANDS).add(TCItems.WAND.get(), TCItems.PECH_WAND.get());
        tag(TCItemTags.WAND_RODS)
                .add(
                        TCItems.WAND_ROD_GREATWOOD.get(),
                        TCItems.WAND_ROD_OBSIDIAN.get(),
                        TCItems.WAND_ROD_BLAZE.get(),
                        TCItems.WAND_ROD_ICE.get(),
                        TCItems.WAND_ROD_QUARTZ.get(),
                        TCItems.WAND_ROD_BONE.get(),
                        TCItems.WAND_ROD_REED.get(),
                        TCItems.WAND_ROD_SILVERWOOD.get(),
                        TCItems.STAFF_ROD_GREATWOOD.get(),
                        TCItems.STAFF_ROD_OBSIDIAN.get(),
                        TCItems.STAFF_ROD_BLAZE.get(),
                        TCItems.STAFF_ROD_ICE.get(),
                        TCItems.STAFF_ROD_QUARTZ.get(),
                        TCItems.STAFF_ROD_BONE.get(),
                        TCItems.STAFF_ROD_REED.get(),
                        TCItems.STAFF_ROD_SILVERWOOD.get(),
                        TCItems.STAFF_ROD_PRIMAL.get());
        tag(TCItemTags.WAND_CAPS)
                .add(
                        TCItems.WAND_CAP_IRON.get(),
                        TCItems.WAND_CAP_COPPER.get(),
                        TCItems.WAND_CAP_GOLD.get(),
                        TCItems.WAND_CAP_SILVER_INERT.get(),
                        TCItems.WAND_CAP_SILVER.get(),
                        TCItems.WAND_CAP_THAUMIUM_INERT.get(),
                        TCItems.WAND_CAP_THAUMIUM.get(),
                        TCItems.WAND_CAP_VOID_INERT.get(),
                        TCItems.WAND_CAP_VOID.get());
        tag(TCItemTags.VIS_CRYSTALS).add(TCItems.ESSENTIA_CRYSTAL.get());
        tag(TCItemTags.PLACEABLE_CRYSTALS)
                .add(
                        TCItems.CRYSTAL_AER.get(),
                        TCItems.CRYSTAL_IGNIS.get(),
                        TCItems.CRYSTAL_AQUA.get(),
                        TCItems.CRYSTAL_TERRA.get(),
                        TCItems.CRYSTAL_ORDO.get(),
                        TCItems.CRYSTAL_PERDITIO.get(),
                        TCItems.CRYSTAL_VITIUM.get());
        tag(TCItemTags.GOLEM_SEALS)
                .add(
                        TCItems.SEAL_BLANK.get(),
                        TCItems.SEAL_PICKUP.get(),
                        TCItems.SEAL_PICKUP_ADVANCED.get(),
                        TCItems.SEAL_FILL.get(),
                        TCItems.SEAL_FILL_ADVANCED.get(),
                        TCItems.SEAL_EMPTY.get(),
                        TCItems.SEAL_EMPTY_ADVANCED.get(),
                        TCItems.SEAL_HARVEST.get(),
                        TCItems.SEAL_BUTCHER.get(),
                        TCItems.SEAL_GUARD.get(),
                        TCItems.SEAL_GUARD_ADVANCED.get(),
                        TCItems.SEAL_LUMBER.get(),
                        TCItems.SEAL_BREAKER.get(),
                        TCItems.SEAL_BREAKER_ADVANCED.get(),
                        TCItems.SEAL_USE.get(),
                        TCItems.SEAL_PROVIDER.get(),
                        TCItems.SEAL_STOCK.get());
        tag(TCItemTags.RESEARCH_NOTES).add(TCItems.RESEARCH_NOTE.get(), TCItems.CELESTIAL_NOTES.get());
        tag(TCItemTags.LOOT_CONTAINERS)
                .add(
                        TCItems.LOOT_BAG_COMMON.get(),
                        TCItems.LOOT_BAG_UNCOMMON.get(),
                        TCItems.LOOT_BAG_RARE.get(),
                        TCItems.LOOT_URN_COMMON.get(),
                        TCItems.LOOT_URN_UNCOMMON.get(),
                        TCItems.LOOT_URN_RARE.get(),
                        TCItems.LOOT_CRATE_COMMON.get(),
                        TCItems.LOOT_CRATE_UNCOMMON.get(),
                        TCItems.LOOT_CRATE_RARE.get());

        tag(TCItemTags.MEAT_CHUNKS)
                .add(
                        TCItems.CHUNK_BEEF.get(),
                        TCItems.CHUNK_CHICKEN.get(),
                        TCItems.CHUNK_PORK.get(),
                        TCItems.CHUNK_FISH.get(),
                        TCItems.CHUNK_RABBIT.get(),
                        TCItems.CHUNK_MUTTON.get());

        copy(TCBlockTags.ORES_AMBER, TCItemTags.ORES_AMBER);
        copy(TCBlockTags.ORES_CINNABAR, TCItemTags.ORES_CINNABAR);
        tag(TCItemTags.ORES_QUARTZ).add(TCItems.ORE_QUARTZ.get());
        tag(Tags.Items.ORES).addTags(TCItemTags.ORES_AMBER, TCItemTags.ORES_CINNABAR, TCItemTags.ORES_QUARTZ);
        tag(TCItemTags.RAW_MATERIALS_CINNABAR).add(TCItems.RAW_CINNABAR.get());
        tag(Tags.Items.RAW_MATERIALS).addTag(TCItemTags.RAW_MATERIALS_CINNABAR);
        tag(TCItemTags.SCAN_IRON).addTags(Tags.Items.ORES_IRON, Tags.Items.INGOTS_IRON, Tags.Items.STORAGE_BLOCKS_IRON);

        copy(TCBlockTags.STORAGE_BLOCKS_AMBER, TCItemTags.STORAGE_BLOCKS_AMBER);
        copy(TCBlockTags.STORAGE_BLOCKS_BRASS, TCItemTags.STORAGE_BLOCKS_BRASS);
        copy(TCBlockTags.STORAGE_BLOCKS_THAUMIUM, TCItemTags.STORAGE_BLOCKS_THAUMIUM);
        copy(TCBlockTags.STORAGE_BLOCKS_VOID_METAL, TCItemTags.STORAGE_BLOCKS_VOID_METAL);
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTags(
                        TCItemTags.STORAGE_BLOCKS_AMBER,
                        TCItemTags.STORAGE_BLOCKS_BRASS,
                        TCItemTags.STORAGE_BLOCKS_THAUMIUM,
                        TCItemTags.STORAGE_BLOCKS_VOID_METAL);

        tag(TCItemTags.INGOTS_BRASS).add(TCItems.INGOT_BRASS.get());
        tag(TCItemTags.INGOTS_THAUMIUM).add(TCItems.INGOT_THAUMIUM.get());
        tag(TCItemTags.INGOTS_VOID_METAL).add(TCItems.INGOT_VOID.get());
        tag(Tags.Items.INGOTS)
                .addTags(TCItemTags.INGOTS_BRASS, TCItemTags.INGOTS_THAUMIUM, TCItemTags.INGOTS_VOID_METAL);
        tag(TCItemTags.GEMS_AMBER).add(TCItems.AMBER.get());
        tag(TCItemTags.GEMS_QUICKSILVER).add(TCItems.QUICKSILVER.get());
        tag(Tags.Items.GEMS).addTags(TCItemTags.GEMS_AMBER, TCItemTags.GEMS_QUICKSILVER);

        tag(TCItemTags.NUGGETS_BRASS).add(TCItems.NUGGET_BRASS.get());
        tag(TCItemTags.NUGGETS_THAUMIUM).add(TCItems.NUGGET_THAUMIUM.get());
        tag(TCItemTags.NUGGETS_VOID_METAL).add(TCItems.NUGGET_VOID.get());
        tag(TCItemTags.NUGGETS_QUARTZ).add(TCItems.NUGGET_QUARTZ.get());
        tag(TCItemTags.NUGGETS_QUICKSILVER).add(TCItems.NUGGET_QUICKSILVER.get());
        tag(Tags.Items.NUGGETS)
                .addTags(
                        TCItemTags.NUGGETS_BRASS,
                        TCItemTags.NUGGETS_THAUMIUM,
                        TCItemTags.NUGGETS_VOID_METAL,
                        TCItemTags.NUGGETS_QUARTZ,
                        TCItemTags.NUGGETS_QUICKSILVER);

        tag(TCItemTags.PLATES_IRON).add(TCItems.PLATE_IRON.get());
        tag(TCItemTags.PLATES_BRASS).add(TCItems.PLATE_BRASS.get());
        tag(TCItemTags.PLATES_THAUMIUM).add(TCItems.PLATE_THAUMIUM.get());
        tag(TCItemTags.PLATES_VOID_METAL).add(TCItems.PLATE_VOID.get());
        tag(TCItemTags.PLATES)
                .addTags(
                        TCItemTags.PLATES_IRON,
                        TCItemTags.PLATES_BRASS,
                        TCItemTags.PLATES_THAUMIUM,
                        TCItemTags.PLATES_VOID_METAL);

        tag(TCItemTags.CLUSTERS)
                .add(
                        TCItems.CLUSTER_IRON.get(),
                        TCItems.CLUSTER_COPPER.get(),
                        TCItems.CLUSTER_GOLD.get(),
                        TCItems.CLUSTER_QUARTZ.get(),
                        TCItems.CLUSTER_CINNABAR.get(),
                        TCItems.CLUSTER_QUARTZ.get(),
                        TCItems.CLUSTER_LEAD.get(),
                        TCItems.CLUSTER_SILVER.get(),
                        TCItems.CLUSTER_TIN.get());
        tag(TCItemTags.RARE_EARTH_CHANCE_HIGH)
                .addTags(
                        Tags.Items.ORES_NETHERITE_SCRAP,
                        Tags.Items.ORES_DIAMOND,
                        Tags.Items.ORES_EMERALD,
                        TCItemTags.ORES_CINNABAR,
                        TCItemTags.RAW_MATERIALS_CINNABAR,
                        TCItemTags.ORES_AMBER);
        tag(TCItemTags.RARE_EARTH_CHANCE_NORMAL)
                .addOptionalTag(TCItemTags.ORES_SILVER)
                .addTags(Tags.Items.ORES_GOLD, Tags.Items.RAW_MATERIALS_GOLD, TCItemTags.CLUSTERS);
        tag(TCItemTags.RARE_EARTH_CHANCE_LOW)
                .addOptionalTags(TCItemTags.ORES_TIN, TCItemTags.ORES_LEAD)
                .addTags(
                        Tags.Items.ORES_IRON,
                        Tags.Items.ORES_COAL,
                        Tags.Items.ORES_COPPER,
                        Tags.Items.ORES_LAPIS,
                        Tags.Items.ORES_REDSTONE,
                        Tags.Items.ORES_QUARTZ,
                        Tags.Items.RAW_MATERIALS_IRON,
                        Tags.Items.RAW_MATERIALS_COPPER);

        tag(ItemTags.DYEABLE)
                .add(
                        TCItems.CLOTH_CHEST.get(),
                        TCItems.CLOTH_LEGS.get(),
                        TCItems.CLOTH_BOOTS.get(),
                        TCItems.VOID_ROBE_HELM.get(),
                        TCItems.VOID_ROBE_CHEST.get(),
                        TCItems.VOID_ROBE_LEGS.get());

        tag(CuriosTags.HEAD).add(TCItems.GOGGLES_REVEALING.get(), TCItems.CURIOSITY_BAND.get());
        tag(CuriosTags.NECKLACE)
                .add(
                        TCItems.AMULET_MUNDANE.get(),
                        TCItems.AMULET_FANCY.get(),
                        TCItems.AMULET_VIS.get(),
                        TCItems.AMULET_VIS_CRAFTED.get());
        tag(CuriosTags.RING)
                .add(
                        TCItems.RING_MUNDANE.get(),
                        TCItems.RING_APPRENTICE.get(),
                        TCItems.RING_FANCY.get(),
                        TCItems.CLOUD_RING.get());
        tag(CuriosTags.BELT).add(TCItems.GIRDLE_MUNDANE.get(), TCItems.GIRDLE_FANCY.get(), TCItems.FOCUS_POUCH.get());
        tag(CuriosTags.CHARM)
                .add(TCItems.CHARM_UNDYING.get(), TCItems.VERDANT_CHARM.get(), TCItems.VOIDSEER_CHARM.get());

        tag(TCItemTags.RUNIC_SHIELDABLE)
                .addOptionalTags(
                        CuriosTags.HEAD, CuriosTags.NECKLACE, CuriosTags.RING, CuriosTags.BELT, CuriosTags.CHARM);

        tag(ItemTags.SWORDS)
                .add(
                        TCItems.THAUMIUM_SWORD.get(),
                        TCItems.VOID_SWORD.get(),
                        TCItems.ELEMENTAL_SWORD.get(),
                        TCItems.CRIMSON_BLADE.get());
        tag(ItemTags.PICKAXES)
                .add(
                        TCItems.THAUMIUM_PICKAXE.get(),
                        TCItems.VOID_PICKAXE.get(),
                        TCItems.ELEMENTAL_PICKAXE.get(),
                        TCItems.PRIMAL_CRUSHER.get());
        tag(ItemTags.AXES).add(TCItems.THAUMIUM_AXE.get(), TCItems.VOID_AXE.get(), TCItems.ELEMENTAL_AXE.get());
        tag(ItemTags.SHOVELS)
                .add(TCItems.THAUMIUM_SHOVEL.get(), TCItems.VOID_SHOVEL.get(), TCItems.ELEMENTAL_SHOVEL.get());
        tag(ItemTags.HOES).add(TCItems.THAUMIUM_HOE.get(), TCItems.VOID_HOE.get(), TCItems.ELEMENTAL_HOE.get());

        tag(ItemTags.HEAD_ARMOR)
                .add(
                        TCItems.THAUMIUM_HELM.get(),
                        TCItems.VOID_HELM.get(),
                        TCItems.VOID_ROBE_HELM.get(),
                        TCItems.FORTRESS_HELM.get(),
                        TCItems.CRIMSON_PLATE_HELM.get(),
                        TCItems.CRIMSON_ROBE_HELM.get(),
                        TCItems.CRIMSON_PRAETOR_HELM.get(),
                        TCItems.GOGGLES_REVEALING.get());
        tag(ItemTags.CHEST_ARMOR)
                .add(
                        TCItems.THAUMIUM_CHEST.get(),
                        TCItems.VOID_CHEST.get(),
                        TCItems.VOID_ROBE_CHEST.get(),
                        TCItems.FORTRESS_CHEST.get(),
                        TCItems.CLOTH_CHEST.get(),
                        TCItems.CRIMSON_PLATE_CHEST.get(),
                        TCItems.CRIMSON_ROBE_CHEST.get(),
                        TCItems.CRIMSON_PRAETOR_CHEST.get());
        tag(ItemTags.LEG_ARMOR)
                .add(
                        TCItems.THAUMIUM_LEGS.get(),
                        TCItems.VOID_LEGS.get(),
                        TCItems.VOID_ROBE_LEGS.get(),
                        TCItems.FORTRESS_LEGS.get(),
                        TCItems.CLOTH_LEGS.get(),
                        TCItems.CRIMSON_PLATE_LEGS.get(),
                        TCItems.CRIMSON_ROBE_LEGS.get(),
                        TCItems.CRIMSON_PRAETOR_LEGS.get());
        tag(ItemTags.FOOT_ARMOR)
                .add(
                        TCItems.THAUMIUM_BOOTS.get(),
                        TCItems.VOID_BOOTS.get(),
                        TCItems.TRAVELLER_BOOTS.get(),
                        TCItems.CLOTH_BOOTS.get(),
                        TCItems.CRIMSON_BOOTS.get());

        tag(TCItemTags.ARMORS_HELMETS)
                .add(
                        TCItems.THAUMIUM_HELM.get(),
                        TCItems.VOID_HELM.get(),
                        TCItems.VOID_ROBE_HELM.get(),
                        TCItems.FORTRESS_HELM.get(),
                        TCItems.CRIMSON_PLATE_HELM.get(),
                        TCItems.CRIMSON_ROBE_HELM.get(),
                        TCItems.CRIMSON_PRAETOR_HELM.get(),
                        TCItems.GOGGLES_REVEALING.get());
        tag(TCItemTags.ARMORS_CHESTPLATES)
                .add(
                        TCItems.THAUMIUM_CHEST.get(),
                        TCItems.VOID_CHEST.get(),
                        TCItems.VOID_ROBE_CHEST.get(),
                        TCItems.FORTRESS_CHEST.get(),
                        TCItems.CLOTH_CHEST.get(),
                        TCItems.CRIMSON_PLATE_CHEST.get(),
                        TCItems.CRIMSON_ROBE_CHEST.get(),
                        TCItems.CRIMSON_PRAETOR_CHEST.get());
        tag(TCItemTags.ARMORS_LEGGINGS)
                .add(
                        TCItems.THAUMIUM_LEGS.get(),
                        TCItems.VOID_LEGS.get(),
                        TCItems.VOID_ROBE_LEGS.get(),
                        TCItems.FORTRESS_LEGS.get(),
                        TCItems.CLOTH_LEGS.get(),
                        TCItems.CRIMSON_PLATE_LEGS.get(),
                        TCItems.CRIMSON_ROBE_LEGS.get(),
                        TCItems.CRIMSON_PRAETOR_LEGS.get());
        tag(TCItemTags.ARMORS_BOOTS)
                .add(
                        TCItems.THAUMIUM_BOOTS.get(),
                        TCItems.VOID_BOOTS.get(),
                        TCItems.TRAVELLER_BOOTS.get(),
                        TCItems.CLOTH_BOOTS.get(),
                        TCItems.CRIMSON_BOOTS.get());

        tag(Tags.Items.MELEE_WEAPON_TOOLS)
                .add(
                        TCItems.THAUMIUM_SWORD.get(),
                        TCItems.VOID_SWORD.get(),
                        TCItems.ELEMENTAL_SWORD.get(),
                        TCItems.CRIMSON_BLADE.get(),
                        TCItems.THAUMIUM_AXE.get(),
                        TCItems.VOID_AXE.get(),
                        TCItems.ELEMENTAL_AXE.get());
        tag(Tags.Items.MINING_TOOL_TOOLS)
                .add(
                        TCItems.THAUMIUM_PICKAXE.get(),
                        TCItems.VOID_PICKAXE.get(),
                        TCItems.ELEMENTAL_PICKAXE.get(),
                        TCItems.PRIMAL_CRUSHER.get());
    }
}
