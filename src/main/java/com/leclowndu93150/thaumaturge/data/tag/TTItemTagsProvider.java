package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
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

public final class TTItemTagsProvider extends ItemTagsProvider {
    public TTItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, TTIds.MODID, existingFileHelper);
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
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
        copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
        copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
        copy(TTBlockTags.MAGICAL_PLANTS, TTItemTags.MAGICAL_PLANTS);
        copy(TTBlockTags.GREATWOOD_LOGS, TTItemTags.GREATWOOD_LOGS);
        tag(TTItemTags.SILVERWOOD_LOGS)
                .add(TTItems.LOG_SILVERWOOD.get())
                .add(TTItems.WOOD_SILVERWOOD.get())
                .add(TTItems.STRIPPED_LOG_SILVERWOOD.get())
                .add(TTItems.STRIPPED_WOOD_SILVERWOOD.get());
        copy(TTBlockTags.PLANKS_GREATWOOD, TTItemTags.PLANKS_GREATWOOD);
        copy(TTBlockTags.PLANKS_SILVERWOOD, TTItemTags.PLANKS_SILVERWOOD);
        copy(TTBlockTags.PLANKS, TTItemTags.PLANKS);
        copy(Tags.Blocks.STRIPPED_LOGS, Tags.Items.STRIPPED_LOGS);
        copy(Tags.Blocks.STRIPPED_WOODS, Tags.Items.STRIPPED_WOODS);
        for (DyeColor dye : DyeColor.values()) {
            tag(TTItemTags.CANDLES).add(TTItems.CANDLES.get(dye).get());
            tag(TTItemTags.NITORS).add(TTItems.NITORS.get(dye).get());
        }

        tag(TTItemTags.SCRIBING_TOOLS).add(TTItems.SCRIBING_TOOLS.get());
        tag(TTItemTags.WANDS).add(TTItems.WAND.get(), TTItems.PECH_WAND.get());
        tag(TTItemTags.WAND_RODS)
                .add(
                        TTItems.WAND_ROD_GREATWOOD.get(),
                        TTItems.WAND_ROD_OBSIDIAN.get(),
                        TTItems.WAND_ROD_BLAZE.get(),
                        TTItems.WAND_ROD_ICE.get(),
                        TTItems.WAND_ROD_QUARTZ.get(),
                        TTItems.WAND_ROD_BONE.get(),
                        TTItems.WAND_ROD_REED.get(),
                        TTItems.WAND_ROD_SILVERWOOD.get(),
                        TTItems.STAFF_ROD_GREATWOOD.get(),
                        TTItems.STAFF_ROD_OBSIDIAN.get(),
                        TTItems.STAFF_ROD_BLAZE.get(),
                        TTItems.STAFF_ROD_ICE.get(),
                        TTItems.STAFF_ROD_QUARTZ.get(),
                        TTItems.STAFF_ROD_BONE.get(),
                        TTItems.STAFF_ROD_REED.get(),
                        TTItems.STAFF_ROD_SILVERWOOD.get(),
                        TTItems.STAFF_ROD_PRIMAL.get());
        tag(TTItemTags.WAND_CAPS)
                .add(
                        TTItems.WAND_CAP_IRON.get(),
                        TTItems.WAND_CAP_COPPER.get(),
                        TTItems.WAND_CAP_GOLD.get(),
                        TTItems.WAND_CAP_SILVER_INERT.get(),
                        TTItems.WAND_CAP_SILVER.get(),
                        TTItems.WAND_CAP_THAUMIUM_INERT.get(),
                        TTItems.WAND_CAP_THAUMIUM.get(),
                        TTItems.WAND_CAP_VOID_INERT.get(),
                        TTItems.WAND_CAP_VOID.get());
        tag(TTItemTags.VIS_CRYSTALS).add(TTItems.ESSENTIA_CRYSTAL.get());
        tag(TTItemTags.PLACEABLE_CRYSTALS)
                .add(
                        TTItems.CRYSTAL_AER.get(),
                        TTItems.CRYSTAL_IGNIS.get(),
                        TTItems.CRYSTAL_AQUA.get(),
                        TTItems.CRYSTAL_TERRA.get(),
                        TTItems.CRYSTAL_ORDO.get(),
                        TTItems.CRYSTAL_PERDITIO.get(),
                        TTItems.CRYSTAL_VITIUM.get());
        tag(TTItemTags.GOLEM_SEALS)
                .add(
                        TTItems.SEAL_BLANK.get(),
                        TTItems.SEAL_PICKUP.get(),
                        TTItems.SEAL_PICKUP_ADVANCED.get(),
                        TTItems.SEAL_FILL.get(),
                        TTItems.SEAL_FILL_ADVANCED.get(),
                        TTItems.SEAL_EMPTY.get(),
                        TTItems.SEAL_EMPTY_ADVANCED.get(),
                        TTItems.SEAL_HARVEST.get(),
                        TTItems.SEAL_BUTCHER.get(),
                        TTItems.SEAL_GUARD.get(),
                        TTItems.SEAL_GUARD_ADVANCED.get(),
                        TTItems.SEAL_LUMBER.get(),
                        TTItems.SEAL_BREAKER.get(),
                        TTItems.SEAL_BREAKER_ADVANCED.get(),
                        TTItems.SEAL_USE.get(),
                        TTItems.SEAL_PROVIDER.get(),
                        TTItems.SEAL_STOCK.get());
        tag(TTItemTags.RESEARCH_NOTES).add(TTItems.RESEARCH_NOTE.get(), TTItems.CELESTIAL_NOTES.get());
        tag(TTItemTags.LOOT_CONTAINERS)
                .add(
                        TTItems.LOOT_BAG_COMMON.get(),
                        TTItems.LOOT_BAG_UNCOMMON.get(),
                        TTItems.LOOT_BAG_RARE.get(),
                        TTItems.LOOT_URN_COMMON.get(),
                        TTItems.LOOT_URN_UNCOMMON.get(),
                        TTItems.LOOT_URN_RARE.get(),
                        TTItems.LOOT_CRATE_COMMON.get(),
                        TTItems.LOOT_CRATE_UNCOMMON.get(),
                        TTItems.LOOT_CRATE_RARE.get());

        tag(TTItemTags.MEAT_CHUNKS)
                .add(
                        TTItems.CHUNK_BEEF.get(),
                        TTItems.CHUNK_CHICKEN.get(),
                        TTItems.CHUNK_PORK.get(),
                        TTItems.CHUNK_FISH.get(),
                        TTItems.CHUNK_RABBIT.get(),
                        TTItems.CHUNK_MUTTON.get());

        copy(TTBlockTags.ORES_AMBER, TTItemTags.ORES_AMBER);
        copy(TTBlockTags.ORES_CINNABAR, TTItemTags.ORES_CINNABAR);
        tag(TTItemTags.ORES_QUARTZ).add(TTItems.ORE_QUARTZ.get()).add(TTItems.DEEPSLATE_ORE_QUARTZ.get());
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        tag(Tags.Items.ORES).addTags(TTItemTags.ORES_AMBER, TTItemTags.ORES_CINNABAR, TTItemTags.ORES_QUARTZ);
        tag(TTItemTags.RAW_MATERIALS_CINNABAR).add(TTItems.RAW_CINNABAR.get());
        tag(Tags.Items.RAW_MATERIALS).addTag(TTItemTags.RAW_MATERIALS_CINNABAR);
        tag(TTItemTags.SCAN_IRON).addTags(Tags.Items.ORES_IRON, Tags.Items.INGOTS_IRON, Tags.Items.STORAGE_BLOCKS_IRON);

        copy(TTBlockTags.STORAGE_BLOCKS_AMBER, TTItemTags.STORAGE_BLOCKS_AMBER);
        copy(TTBlockTags.STORAGE_BLOCKS_BRASS, TTItemTags.STORAGE_BLOCKS_BRASS);
        copy(TTBlockTags.STORAGE_BLOCKS_THAUMIUM, TTItemTags.STORAGE_BLOCKS_THAUMIUM);
        copy(TTBlockTags.STORAGE_BLOCKS_VOID_METAL, TTItemTags.STORAGE_BLOCKS_VOID_METAL);
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTags(
                        TTItemTags.STORAGE_BLOCKS_AMBER,
                        TTItemTags.STORAGE_BLOCKS_BRASS,
                        TTItemTags.STORAGE_BLOCKS_THAUMIUM,
                        TTItemTags.STORAGE_BLOCKS_VOID_METAL);

        tag(TTItemTags.INGOTS_BRASS).add(TTItems.INGOT_BRASS.get());
        tag(TTItemTags.INGOTS_THAUMIUM).add(TTItems.INGOT_THAUMIUM.get());
        tag(TTItemTags.INGOTS_VOID_METAL).add(TTItems.INGOT_VOID.get());
        tag(Tags.Items.INGOTS)
                .addTags(TTItemTags.INGOTS_BRASS, TTItemTags.INGOTS_THAUMIUM, TTItemTags.INGOTS_VOID_METAL);
        tag(TTItemTags.GEMS_AMBER).add(TTItems.AMBER.get());
        tag(TTItemTags.GEMS_QUICKSILVER).add(TTItems.QUICKSILVER.get());
        tag(Tags.Items.GEMS).addTags(TTItemTags.GEMS_AMBER, TTItemTags.GEMS_QUICKSILVER);

        tag(TTItemTags.NUGGETS_BRASS).add(TTItems.NUGGET_BRASS.get());
        tag(TTItemTags.NUGGETS_THAUMIUM).add(TTItems.NUGGET_THAUMIUM.get());
        tag(TTItemTags.NUGGETS_VOID_METAL).add(TTItems.NUGGET_VOID.get());
        tag(TTItemTags.NUGGETS_QUARTZ).add(TTItems.NUGGET_QUARTZ.get());
        tag(TTItemTags.NUGGETS_QUICKSILVER).add(TTItems.NUGGET_QUICKSILVER.get());
        tag(Tags.Items.NUGGETS)
                .addTags(
                        TTItemTags.NUGGETS_BRASS,
                        TTItemTags.NUGGETS_THAUMIUM,
                        TTItemTags.NUGGETS_VOID_METAL,
                        TTItemTags.NUGGETS_QUARTZ,
                        TTItemTags.NUGGETS_QUICKSILVER);

        tag(TTItemTags.PLATES_IRON).add(TTItems.PLATE_IRON.get());
        tag(TTItemTags.PLATES_BRASS).add(TTItems.PLATE_BRASS.get());
        tag(TTItemTags.PLATES_THAUMIUM).add(TTItems.PLATE_THAUMIUM.get());
        tag(TTItemTags.PLATES_VOID_METAL).add(TTItems.PLATE_VOID.get());
        tag(TTItemTags.PLATES)
                .addTags(
                        TTItemTags.PLATES_IRON,
                        TTItemTags.PLATES_BRASS,
                        TTItemTags.PLATES_THAUMIUM,
                        TTItemTags.PLATES_VOID_METAL);

        tag(TTItemTags.CLUSTERS)
                .add(
                        TTItems.CLUSTER_IRON.get(),
                        TTItems.CLUSTER_COPPER.get(),
                        TTItems.CLUSTER_GOLD.get(),
                        TTItems.CLUSTER_QUARTZ.get(),
                        TTItems.CLUSTER_CINNABAR.get(),
                        TTItems.CLUSTER_QUARTZ.get(),
                        TTItems.CLUSTER_LEAD.get(),
                        TTItems.CLUSTER_SILVER.get(),
                        TTItems.CLUSTER_TIN.get());
        tag(TTItemTags.RARE_EARTH_CHANCE_HIGH)
                .addTags(
                        Tags.Items.ORES_NETHERITE_SCRAP,
                        Tags.Items.ORES_DIAMOND,
                        Tags.Items.ORES_EMERALD,
                        TTItemTags.ORES_CINNABAR,
                        TTItemTags.RAW_MATERIALS_CINNABAR,
                        TTItemTags.ORES_AMBER);
        tag(TTItemTags.RARE_EARTH_CHANCE_NORMAL)
                .addOptionalTag(TTItemTags.ORES_SILVER)
                .addTags(Tags.Items.ORES_GOLD, Tags.Items.RAW_MATERIALS_GOLD, TTItemTags.CLUSTERS);
        tag(TTItemTags.RARE_EARTH_CHANCE_LOW)
                .addOptionalTags(TTItemTags.ORES_TIN, TTItemTags.ORES_LEAD)
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
                        TTItems.CLOTH_CHEST.get(),
                        TTItems.CLOTH_LEGS.get(),
                        TTItems.CLOTH_BOOTS.get(),
                        TTItems.VOID_ROBE_HELM.get(),
                        TTItems.VOID_ROBE_CHEST.get(),
                        TTItems.VOID_ROBE_LEGS.get());

        tag(CuriosTags.HEAD).add(TTItems.GOGGLES_REVEALING.get(), TTItems.CURIOSITY_BAND.get());
        tag(CuriosTags.NECKLACE)
                .add(
                        TTItems.AMULET_MUNDANE.get(),
                        TTItems.AMULET_FANCY.get(),
                        TTItems.AMULET_VIS.get(),
                        TTItems.AMULET_VIS_CRAFTED.get());
        tag(CuriosTags.RING)
                .add(
                        TTItems.RING_MUNDANE.get(),
                        TTItems.RING_APPRENTICE.get(),
                        TTItems.RING_FANCY.get(),
                        TTItems.CLOUD_RING.get());
        tag(CuriosTags.BELT).add(TTItems.GIRDLE_MUNDANE.get(), TTItems.GIRDLE_FANCY.get(), TTItems.FOCUS_POUCH.get());
        tag(CuriosTags.CHARM)
                .add(TTItems.CHARM_UNDYING.get(), TTItems.VERDANT_CHARM.get(), TTItems.VOIDSEER_CHARM.get());

        tag(TTItemTags.RUNIC_SHIELDABLE)
                .addOptionalTags(
                        CuriosTags.HEAD, CuriosTags.NECKLACE, CuriosTags.RING, CuriosTags.BELT, CuriosTags.CHARM);

        tag(ItemTags.SWORDS)
                .add(
                        TTItems.THAUMIUM_SWORD.get(),
                        TTItems.VOID_SWORD.get(),
                        TTItems.ELEMENTAL_SWORD.get(),
                        TTItems.CRIMSON_BLADE.get());
        tag(ItemTags.PICKAXES)
                .add(
                        TTItems.THAUMIUM_PICKAXE.get(),
                        TTItems.VOID_PICKAXE.get(),
                        TTItems.ELEMENTAL_PICKAXE.get(),
                        TTItems.PRIMAL_CRUSHER.get());
        tag(ItemTags.AXES).add(TTItems.THAUMIUM_AXE.get(), TTItems.VOID_AXE.get(), TTItems.ELEMENTAL_AXE.get());
        tag(ItemTags.SHOVELS)
                .add(TTItems.THAUMIUM_SHOVEL.get(), TTItems.VOID_SHOVEL.get(), TTItems.ELEMENTAL_SHOVEL.get());
        tag(ItemTags.HOES).add(TTItems.THAUMIUM_HOE.get(), TTItems.VOID_HOE.get(), TTItems.ELEMENTAL_HOE.get());

        tag(ItemTags.HEAD_ARMOR)
                .add(
                        TTItems.THAUMIUM_HELM.get(),
                        TTItems.VOID_HELM.get(),
                        TTItems.VOID_ROBE_HELM.get(),
                        TTItems.FORTRESS_HELM.get(),
                        TTItems.CRIMSON_PLATE_HELM.get(),
                        TTItems.CRIMSON_ROBE_HELM.get(),
                        TTItems.CRIMSON_PRAETOR_HELM.get(),
                        TTItems.GOGGLES_REVEALING.get());
        tag(ItemTags.CHEST_ARMOR)
                .add(
                        TTItems.THAUMIUM_CHEST.get(),
                        TTItems.VOID_CHEST.get(),
                        TTItems.VOID_ROBE_CHEST.get(),
                        TTItems.FORTRESS_CHEST.get(),
                        TTItems.CLOTH_CHEST.get(),
                        TTItems.CRIMSON_PLATE_CHEST.get(),
                        TTItems.CRIMSON_ROBE_CHEST.get(),
                        TTItems.CRIMSON_PRAETOR_CHEST.get());
        tag(ItemTags.LEG_ARMOR)
                .add(
                        TTItems.THAUMIUM_LEGS.get(),
                        TTItems.VOID_LEGS.get(),
                        TTItems.VOID_ROBE_LEGS.get(),
                        TTItems.FORTRESS_LEGS.get(),
                        TTItems.CLOTH_LEGS.get(),
                        TTItems.CRIMSON_PLATE_LEGS.get(),
                        TTItems.CRIMSON_ROBE_LEGS.get(),
                        TTItems.CRIMSON_PRAETOR_LEGS.get());
        tag(ItemTags.FOOT_ARMOR)
                .add(
                        TTItems.THAUMIUM_BOOTS.get(),
                        TTItems.VOID_BOOTS.get(),
                        TTItems.TRAVELLER_BOOTS.get(),
                        TTItems.CLOTH_BOOTS.get(),
                        TTItems.CRIMSON_BOOTS.get());

        tag(TTItemTags.ARMORS_HELMETS)
                .add(
                        TTItems.THAUMIUM_HELM.get(),
                        TTItems.VOID_HELM.get(),
                        TTItems.VOID_ROBE_HELM.get(),
                        TTItems.FORTRESS_HELM.get(),
                        TTItems.CRIMSON_PLATE_HELM.get(),
                        TTItems.CRIMSON_ROBE_HELM.get(),
                        TTItems.CRIMSON_PRAETOR_HELM.get(),
                        TTItems.GOGGLES_REVEALING.get());
        tag(TTItemTags.ARMORS_CHESTPLATES)
                .add(
                        TTItems.THAUMIUM_CHEST.get(),
                        TTItems.VOID_CHEST.get(),
                        TTItems.VOID_ROBE_CHEST.get(),
                        TTItems.FORTRESS_CHEST.get(),
                        TTItems.CLOTH_CHEST.get(),
                        TTItems.CRIMSON_PLATE_CHEST.get(),
                        TTItems.CRIMSON_ROBE_CHEST.get(),
                        TTItems.CRIMSON_PRAETOR_CHEST.get());
        tag(TTItemTags.ARMORS_LEGGINGS)
                .add(
                        TTItems.THAUMIUM_LEGS.get(),
                        TTItems.VOID_LEGS.get(),
                        TTItems.VOID_ROBE_LEGS.get(),
                        TTItems.FORTRESS_LEGS.get(),
                        TTItems.CLOTH_LEGS.get(),
                        TTItems.CRIMSON_PLATE_LEGS.get(),
                        TTItems.CRIMSON_ROBE_LEGS.get(),
                        TTItems.CRIMSON_PRAETOR_LEGS.get());
        tag(TTItemTags.ARMORS_BOOTS)
                .add(
                        TTItems.THAUMIUM_BOOTS.get(),
                        TTItems.VOID_BOOTS.get(),
                        TTItems.TRAVELLER_BOOTS.get(),
                        TTItems.CLOTH_BOOTS.get(),
                        TTItems.CRIMSON_BOOTS.get());

        tag(Tags.Items.MELEE_WEAPON_TOOLS)
                .add(
                        TTItems.THAUMIUM_SWORD.get(),
                        TTItems.VOID_SWORD.get(),
                        TTItems.ELEMENTAL_SWORD.get(),
                        TTItems.CRIMSON_BLADE.get(),
                        TTItems.THAUMIUM_AXE.get(),
                        TTItems.VOID_AXE.get(),
                        TTItems.ELEMENTAL_AXE.get());
        tag(Tags.Items.MINING_TOOL_TOOLS)
                .add(
                        TTItems.THAUMIUM_PICKAXE.get(),
                        TTItems.VOID_PICKAXE.get(),
                        TTItems.ELEMENTAL_PICKAXE.get(),
                        TTItems.PRIMAL_CRUSHER.get());
    }
}
