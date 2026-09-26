package com.leclowndu93150.thaumaturge.data.worldgen.scan;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public final class ScanEntryBootstrap {
    private ScanEntryBootstrap() {}

    public static void bootstrap(BootstrapContext<ScanEntry> ctx) {
        HolderGetter<Block> blockReg = ctx.lookup(Registries.BLOCK);
        HolderGetter<Item> itemReg = ctx.lookup(Registries.ITEM);
        HolderGetter<EntityType<?>> entityReg = ctx.lookup(Registries.ENTITY_TYPE);

        HolderSet<Block> crystals = blocks(blockReg, TCBlocks.CRYSTAL_AER.get(), TCBlocks.CRYSTAL_IGNIS.get(), TCBlocks.CRYSTAL_AQUA.get(), TCBlocks.CRYSTAL_TERRA.get(), TCBlocks.CRYSTAL_ORDO.get(),
                TCBlocks.CRYSTAL_PERDITIO.get(), TCBlocks.CRYSTAL_VITIUM.get());
        HolderSet<Block> woods = blocks(blockReg, TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.WOOD_GREATWOOD.get(), TCBlocks.WOOD_SILVERWOOD.get(),
                TCBlocks.STRIPPED_LOG_GREATWOOD.get(), TCBlocks.STRIPPED_LOG_SILVERWOOD.get(), TCBlocks.STRIPPED_WOOD_GREATWOOD.get(), TCBlocks.STRIPPED_WOOD_SILVERWOOD.get(),
                TCBlocks.SAPLING_GREATWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get());

        register(ctx, "ore", "ore", blocks(blockReg, concat(List.of(TCBlocks.ORE_AMBER.get(), TCBlocks.ORE_CINNABAR.get()), List.of(TCBlocks.CRYSTAL_AER.get(), TCBlocks.CRYSTAL_IGNIS.get(),
                TCBlocks.CRYSTAL_AQUA.get(), TCBlocks.CRYSTAL_TERRA.get(), TCBlocks.CRYSTAL_ORDO.get(), TCBlocks.CRYSTAL_PERDITIO.get(), TCBlocks.CRYSTAL_VITIUM.get()))), null, null);
        register(ctx, "orecrystal", "scanned/orecrystal", crystals, null, null);
        register(ctx, "plants", "plants", blocks(blockReg,
                concat(List.of(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.WOOD_GREATWOOD.get(), TCBlocks.WOOD_SILVERWOOD.get(), TCBlocks.STRIPPED_LOG_GREATWOOD.get(),
                        TCBlocks.STRIPPED_LOG_SILVERWOOD.get(), TCBlocks.STRIPPED_WOOD_GREATWOOD.get(), TCBlocks.STRIPPED_WOOD_SILVERWOOD.get(), TCBlocks.SAPLING_GREATWOOD.get(),
                        TCBlocks.SAPLING_SILVERWOOD.get()), List.of(TCBlocks.PLANT_CINDERPEARL.get(), TCBlocks.PLANT_SHIMMERLEAF.get(), TCBlocks.PLANT_VISHROOM.get()))),
                null, null);
        register(ctx, "plantwood", "scanned/plantwood", woods, null, null);

        register(ctx, "f_teleport", "f_teleport", blocks(blockReg, Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME), items(itemReg, Items.ENDER_PEARL),
                entities(entityReg, EntityType.ENDERMAN));
        register(ctx, "f_spider", "f_spider", null, null, entities(entityReg, EntityType.SPIDER, EntityType.CAVE_SPIDER, TCEntities.MIND_SPIDER.get()));
        register(ctx, "f_bat", "f_bat", null, null, entities(entityReg, EntityType.BAT));
        register(ctx, "f_fly", "f_fly", null, null, entities(entityReg, EntityType.BAT, EntityType.PARROT, EntityType.GHAST, EntityType.BLAZE, TCEntities.TAINT_SWARM.get()));
        register(ctx, "f_dispenser", "f_dispenser", blocks(blockReg, Blocks.DISPENSER), null, null);
        register(ctx, "f_matclay", "f_matclay", blockReg.getOrThrow(TCBlockTags.SCAN_CLAY), items(itemReg, Items.CLAY_BALL), null);
        register(ctx, "f_matiron", "f_matiron", null, itemReg.getOrThrow(TCItemTags.SCAN_IRON), null);
        register(ctx, "f_matbrass", "f_matbrass", blocks(blockReg, TCBlocks.METAL_BRASS_BLOCK.get()), items(itemReg, TCItems.INGOT_BRASS.get()), null);
        register(ctx, "f_matthaumium", "f_matthaumium", blocks(blockReg, TCBlocks.METAL_THAUMIUM_BLOCK.get()), items(itemReg, TCItems.INGOT_THAUMIUM.get(), TCItems.PLATE_THAUMIUM.get()), null);
        register(ctx, "f_matvoid", "f_matvoid", blocks(blockReg, TCBlocks.METAL_VOID_BLOCK.get()), items(itemReg, TCItems.INGOT_VOID.get(), TCItems.PLATE_VOID.get()), null);
        register(ctx, "f_brain", "f_brain", null, items(itemReg, TCItems.BRAIN.get()),
                entities(entityReg, TCEntities.BRAINY_ZOMBIE.get(), TCEntities.GIANT_BRAINY_ZOMBIE.get(), TCEntities.BRAINY_DROWNED.get(), TCEntities.BRAINY_HUSK.get()));
        register(ctx, "f_golem", "f_golem", blocks(blockReg, TCBlocks.ARCANE_BORE.get()), null, entities(entityReg, TCEntities.THAUMATURGE_GOLEM.get(), TCEntities.TURRET_CROSSBOW.get(),
                TCEntities.TURRET_CROSSBOW_ADVANCED.get(), TCEntities.ARCANE_BORE.get(), EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.COPPER_GOLEM, EntityType.SHULKER));
        register(ctx, "f_arrow", "f_arrow", null, items(itemReg, Items.ARROW), entities(entityReg, EntityType.ARROW, EntityType.SPECTRAL_ARROW, EntityType.TRIDENT, TCEntities.GOLEM_DART.get()));
        register(ctx, "f_fireball", "f_fireball", null, null,
                entities(entityReg, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL, EntityType.WIND_CHARGE, EntityType.BREEZE_WIND_CHARGE));
        register(ctx, "f_spit", "f_spit", null, null, entities(entityReg, EntityType.LLAMA_SPIT));
        register(ctx, "f_voidseed", "f_voidseed", null, items(itemReg, TCItems.VOID_SEED.get()), null);
        register(ctx, "primordial_pearl", "primordial_pearl", null, items(itemReg, TCItems.PRIMORDIAL_PEARL.get()), null);
        register(ctx, "f_toomuchflux", "f_toomuchflux", null, null, entities(entityReg, TCEntities.FLUX_RIFT.get()));
        register(ctx, "fluxrift", "scanned/fluxrift", null, null, entities(entityReg, TCEntities.FLUX_RIFT.get()));
        register(ctx, "orblock1", "scanned/orblock1", blocks(blockReg, TCBlocks.STONE_ANCIENT.get(), TCBlocks.STONE_ANCIENT_TILE.get()), null, null);
        register(ctx, "orblock2", "scanned/orblock2", blocks(blockReg, TCBlocks.STONE_ELDRITCH_TILE.get()), null, null);
        register(ctx, "outer_revelations", "outer_revelations", blocks(blockReg, TCBlocks.ELDRITCH_STONE_CRYSTAL.get(), TCBlocks.ELDRITCH_CRUST_GLOWING.get()), null, null);
        register(ctx, "dragonbreath", "scanned/dragonbreath", null, items(itemReg, Items.DRAGON_BREATH), null);
        register(ctx, "totemundying", "scanned/totemundying", null, items(itemReg, Items.TOTEM_OF_UNDYING), null);
        register(ctx, "pechwand", "scanned/pechwand", null, items(itemReg, TCItems.PECH_WAND.get()), null);
        register(ctx, "oreamber", "scanned/oreamber", blocks(blockReg, TCBlocks.ORE_AMBER.get()), null, null);
        register(ctx, "orecinnabar", "scanned/orecinnabar", blocks(blockReg, TCBlocks.ORE_CINNABAR.get()), null, null);
        register(ctx, "plantcinderpearl", "scanned/plantcinderpearl", blocks(blockReg, TCBlocks.PLANT_CINDERPEARL.get()), null, null);
        register(ctx, "plantshimmerleaf", "scanned/plantshimmerleaf", blocks(blockReg, TCBlocks.PLANT_SHIMMERLEAF.get()), null, null);
        register(ctx, "plantvishroom", "scanned/plantvishroom", blocks(blockReg, TCBlocks.PLANT_VISHROOM.get()), null, null);
        register(ctx, "manapod", "scanned/manapod", blocks(blockReg, TCBlocks.MANA_POD.get()), items(itemReg, TCItems.MANA_BEAN.get()), null);
        register(ctx, "cultist", "scanned/entity/thaumaturge/cultist", null, null, entities(entityReg, TCEntities.CULTIST_KNIGHT.get(), TCEntities.CULTIST_CLERIC.get()));
        register(ctx, "eldritch_crab", "scanned/entity/thaumaturge/eldritch_crab", null, null, entities(entityReg, TCEntities.INHABITED_ZOMBIE.get()));
    }

    private static void register(BootstrapContext<ScanEntry> ctx, String name, String key, @Nullable HolderSet<Block> blocks, @Nullable HolderSet<Item> items, @Nullable HolderSet<EntityType<?>> entities) {
        ctx.register(ResourceKey.create(ScanEntry.REGISTRY_KEY, TCIds.rl(name)), new ScanEntry(TCIds.rl(key), Optional.ofNullable(blocks), Optional.ofNullable(items), Optional.ofNullable(entities)));
    }

    private static List<Block> concat(List<Block> first, List<Block> second) {
        List<Block> combined = new ArrayList<>(first);
        combined.addAll(second);
        return combined;
    }

    private static HolderSet<Block> blocks(HolderGetter<Block> reg, List<Block> list) {
        return blocks(reg, list.toArray(Block[]::new));
    }

    private static HolderSet<Block> blocks(HolderGetter<Block> reg, Block... values) {
        List<Holder<Block>> holders = new ArrayList<>(values.length);
        for (Block value : values) {
            holders.add(reg.getOrThrow(value.builtInRegistryHolder().key()));
        }
        return HolderSet.direct(holders);
    }

    private static HolderSet<Item> items(HolderGetter<Item> reg, ItemLike... values) {
        List<Holder<Item>> holders = new ArrayList<>(values.length);
        for (ItemLike value : values) {
            holders.add(reg.getOrThrow(value.asItem().builtInRegistryHolder().key()));
        }
        return HolderSet.direct(holders);
    }

    private static HolderSet<EntityType<?>> entities(HolderGetter<EntityType<?>> reg, EntityType<?>... values) {
        List<Holder<EntityType<?>>> holders = new ArrayList<>(values.length);
        for (EntityType<?> value : values) {
            holders.add(reg.getOrThrow(value.builtInRegistryHolder().key()));
        }
        return HolderSet.direct(holders);
    }
}
