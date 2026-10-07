package com.leclowndu93150.thaumaturge.data.worldgen.scan;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
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

        HolderSet<Block> crystals = blocks(
                blockReg,
                TTBlocks.CRYSTAL_AER.get(),
                TTBlocks.CRYSTAL_IGNIS.get(),
                TTBlocks.CRYSTAL_AQUA.get(),
                TTBlocks.CRYSTAL_TERRA.get(),
                TTBlocks.CRYSTAL_ORDO.get(),
                TTBlocks.CRYSTAL_PERDITIO.get(),
                TTBlocks.CRYSTAL_VITIUM.get());
        HolderSet<Block> woods = blocks(
                blockReg,
                TTBlocks.LOG_GREATWOOD.get(),
                TTBlocks.LOG_SILVERWOOD.get(),
                TTBlocks.SILVERWOOD_NODE_LOG.get(),
                TTBlocks.WOOD_GREATWOOD.get(),
                TTBlocks.WOOD_SILVERWOOD.get(),
                TTBlocks.STRIPPED_LOG_GREATWOOD.get(),
                TTBlocks.STRIPPED_LOG_SILVERWOOD.get(),
                TTBlocks.STRIPPED_WOOD_GREATWOOD.get(),
                TTBlocks.STRIPPED_WOOD_SILVERWOOD.get(),
                TTBlocks.SAPLING_GREATWOOD.get(),
                TTBlocks.SAPLING_SILVERWOOD.get());

        register(
                ctx,
                "ore",
                "ore",
                blocks(
                        blockReg,
                        concat(
                                List.of(
                                        TTBlocks.ORE_AMBER.get(),
                                        TTBlocks.ORE_CINNABAR.get(),
                                        TTBlocks.DEEPSLATE_ORE_AMBER.get(),
                                        TTBlocks.DEEPSLATE_ORE_CINNABAR.get()),
                                List.of(
                                        TTBlocks.CRYSTAL_AER.get(),
                                        TTBlocks.CRYSTAL_IGNIS.get(),
                                        TTBlocks.CRYSTAL_AQUA.get(),
                                        TTBlocks.CRYSTAL_TERRA.get(),
                                        TTBlocks.CRYSTAL_ORDO.get(),
                                        TTBlocks.CRYSTAL_PERDITIO.get(),
                                        TTBlocks.CRYSTAL_VITIUM.get()))),
                null,
                null);
        register(ctx, "orecrystal", "scanned/orecrystal", crystals, null, null);
        register(ctx, "plants", "plants", blockReg.getOrThrow(TTBlockTags.MAGICAL_PLANTS), null, null);
        register(ctx, "plantwood", "scanned/plantwood", woods, null, null);

        register(
                ctx,
                "f_teleport",
                "f_teleport",
                blocks(blockReg, Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME),
                items(itemReg, Items.ENDER_PEARL),
                entities(entityReg, EntityType.ENDERMAN));
        register(
                ctx,
                "f_spider",
                "f_spider",
                null,
                null,
                entities(entityReg, EntityType.SPIDER, EntityType.CAVE_SPIDER, TTEntities.MIND_SPIDER.get()));
        register(ctx, "f_bat", "f_bat", null, null, entities(entityReg, EntityType.BAT));
        register(
                ctx,
                "f_fly",
                "f_fly",
                null,
                null,
                entities(
                        entityReg,
                        EntityType.BAT,
                        EntityType.PARROT,
                        EntityType.GHAST,
                        EntityType.BLAZE,
                        TTEntities.TAINT_SWARM.get()));
        register(ctx, "f_dispenser", "f_dispenser", blocks(blockReg, Blocks.DISPENSER), null, null);
        register(
                ctx,
                "f_matclay",
                "f_matclay",
                blockReg.getOrThrow(TTBlockTags.SCAN_CLAY),
                items(itemReg, Items.CLAY_BALL),
                null);
        register(ctx, "f_matiron", "f_matiron", null, itemReg.getOrThrow(TTItemTags.SCAN_IRON), null);
        register(
                ctx,
                "f_matbrass",
                "f_matbrass",
                blocks(blockReg, TTBlocks.METAL_BRASS_BLOCK.get()),
                items(itemReg, TTItems.INGOT_BRASS.get()),
                null);
        register(
                ctx,
                "f_matthaumium",
                "f_matthaumium",
                blocks(blockReg, TTBlocks.METAL_THAUMIUM_BLOCK.get()),
                items(itemReg, TTItems.INGOT_THAUMIUM.get(), TTItems.PLATE_THAUMIUM.get()),
                null);
        register(
                ctx,
                "f_matvoid",
                "f_matvoid",
                blocks(blockReg, TTBlocks.METAL_VOID_BLOCK.get()),
                items(itemReg, TTItems.INGOT_VOID.get(), TTItems.PLATE_VOID.get()),
                null);
        register(
                ctx,
                "f_brain",
                "f_brain",
                null,
                items(itemReg, TTItems.BRAIN.get()),
                entities(
                        entityReg,
                        TTEntities.BRAINY_ZOMBIE.get(),
                        TTEntities.GIANT_BRAINY_ZOMBIE.get(),
                        TTEntities.BRAINY_DROWNED.get(),
                        TTEntities.BRAINY_HUSK.get()));
        register(
                ctx,
                "f_golem",
                "f_golem",
                blocks(blockReg, TTBlocks.ARCANE_BORE.get()),
                null,
                entities(
                        entityReg,
                        TTEntities.THAUMATURGE_GOLEM.get(),
                        TTEntities.TURRET_CROSSBOW.get(),
                        TTEntities.TURRET_CROSSBOW_ADVANCED.get(),
                        TTEntities.ARCANE_BORE.get(),
                        EntityType.IRON_GOLEM,
                        EntityType.SNOW_GOLEM,
                        EntityType.SHULKER));
        register(
                ctx,
                "f_arrow",
                "f_arrow",
                null,
                items(itemReg, Items.ARROW),
                entities(
                        entityReg,
                        EntityType.ARROW,
                        EntityType.SPECTRAL_ARROW,
                        EntityType.TRIDENT,
                        TTEntities.GOLEM_DART.get()));
        register(
                ctx,
                "f_fireball",
                "f_fireball",
                null,
                null,
                entities(
                        entityReg,
                        EntityType.FIREBALL,
                        EntityType.SMALL_FIREBALL,
                        EntityType.DRAGON_FIREBALL,
                        EntityType.WITHER_SKULL,
                        EntityType.WIND_CHARGE,
                        EntityType.BREEZE_WIND_CHARGE));
        register(ctx, "f_spit", "f_spit", null, null, entities(entityReg, EntityType.LLAMA_SPIT));
        register(ctx, "f_voidseed", "f_voidseed", null, items(itemReg, TTItems.VOID_SEED.get()), null);
        register(
                ctx,
                "primordial_pearl",
                "primordial_pearl",
                null,
                items(itemReg, TTItems.PRIMORDIAL_PEARL.get()),
                null);
        register(ctx, "f_toomuchflux", "f_toomuchflux", null, null, entities(entityReg, TTEntities.FLUX_RIFT.get()));
        register(ctx, "fluxrift", "scanned/fluxrift", null, null, entities(entityReg, TTEntities.FLUX_RIFT.get()));
        register(
                ctx,
                "orblock1",
                "scanned/orblock1",
                blocks(blockReg, TTBlocks.STONE_ANCIENT.get(), TTBlocks.STONE_ANCIENT_TILE.get()),
                null,
                null);
        register(ctx, "orblock2", "scanned/orblock2", blocks(blockReg, TTBlocks.STONE_ELDRITCH_TILE.get()), null, null);
        register(
                ctx,
                "orblock3",
                "scanned/orblock3",
                blocks(blockReg, TTBlocks.STONE_ANCIENT_GLYPHED.get()),
                null,
                null);
        register(
                ctx,
                "outer_revelations",
                "outer_revelations",
                blocks(blockReg, TTBlocks.ELDRITCH_STONE_CRYSTAL.get(), TTBlocks.ELDRITCH_CRUST_GLOWING.get()),
                null,
                null);
        register(ctx, "dragonbreath", "scanned/dragonbreath", null, items(itemReg, Items.DRAGON_BREATH), null);
        register(ctx, "totemundying", "scanned/totemundying", null, items(itemReg, Items.TOTEM_OF_UNDYING), null);
        register(ctx, "pechwand", "scanned/pechwand", null, items(itemReg, TTItems.PECH_WAND.get()), null);
        register(
                ctx,
                "oreamber",
                "scanned/oreamber",
                blocks(blockReg, TTBlocks.ORE_AMBER.get(), TTBlocks.DEEPSLATE_ORE_AMBER.get()),
                null,
                null);
        register(
                ctx,
                "orecinnabar",
                "scanned/orecinnabar",
                blocks(blockReg, TTBlocks.ORE_CINNABAR.get(), TTBlocks.DEEPSLATE_ORE_CINNABAR.get()),
                null,
                null);
        register(
                ctx,
                "plantcinderpearl",
                "scanned/plantcinderpearl",
                blocks(blockReg, TTBlocks.PLANT_CINDERPEARL.get()),
                null,
                null);
        register(
                ctx,
                "plantshimmerleaf",
                "scanned/plantshimmerleaf",
                blocks(blockReg, TTBlocks.PLANT_SHIMMERLEAF.get()),
                null,
                null);
        register(
                ctx,
                "plantvishroom",
                "scanned/plantvishroom",
                blocks(blockReg, TTBlocks.PLANT_VISHROOM.get()),
                null,
                null);
        register(
                ctx,
                "manapod",
                "scanned/manapod",
                blocks(blockReg, TTBlocks.MANA_POD.get()),
                items(itemReg, TTItems.MANA_BEAN.get()),
                null);
        register(
                ctx,
                "cultist",
                "scanned/entity/thaumaturge/cultist",
                null,
                null,
                entities(entityReg, TTEntities.CULTIST_KNIGHT.get(), TTEntities.CULTIST_CLERIC.get()));
        register(
                ctx,
                "eldritch_crab",
                "scanned/entity/thaumaturge/eldritch_crab",
                null,
                null,
                entities(entityReg, TTEntities.INHABITED_ZOMBIE.get()));
    }

    private static void register(
            BootstrapContext<ScanEntry> ctx,
            String name,
            String key,
            @Nullable HolderSet<Block> blocks,
            @Nullable HolderSet<Item> items,
            @Nullable HolderSet<EntityType<?>> entities) {
        ctx.register(
                ResourceKey.create(ScanEntry.REGISTRY_KEY, TTIds.rl(name)),
                new ScanEntry(
                        TTIds.rl(key),
                        Optional.ofNullable(blocks),
                        Optional.ofNullable(items),
                        Optional.ofNullable(entities)));
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
