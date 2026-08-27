package com.leclowndu93150.thaumaturge.data.worldgen.feature;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.gen.ObeliskStructure;
import com.leclowndu93150.thaumaturge.content.world.mound.MoundStructure;
import com.leclowndu93150.thaumaturge.registry.TCBiomeTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public final class TCStructureBootstrap {
    public static final ResourceKey<Structure> MOUND = ResourceKey.create(Registries.STRUCTURE, TCIds.rl("mound"));
    public static final ResourceKey<StructureSet> MOUND_SET =
            ResourceKey.create(Registries.STRUCTURE_SET, TCIds.rl("mounds"));
    public static final ResourceKey<Structure> ELDRITCH_OBELISK =
            ResourceKey.create(Registries.STRUCTURE, TCIds.rl("eldritch_obelisk"));
    public static final ResourceKey<StructureSet> ELDRITCH_OBELISK_SET =
            ResourceKey.create(Registries.STRUCTURE_SET, TCIds.rl("eldritch_obelisks"));

    private static final int MOUND_SPACING = 20;
    private static final int MOUND_SEPARATION = 10;
    private static final int MOUND_SALT = 41626157;
    private static final int OBELISK_SPACING = 32;
    private static final int OBELISK_SEPARATION = 12;
    private static final int OBELISK_SALT = 46186246;

    private TCStructureBootstrap() {}

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(
                MOUND, new MoundStructure(new Structure.StructureSettings(biomes.getOrThrow(TCBiomeTags.HAS_MOUND))));
        context.register(
                ELDRITCH_OBELISK,
                new ObeliskStructure(
                        new Structure.StructureSettings(biomes.getOrThrow(TCBiomeTags.HAS_ELDRITCH_OBELISK))));
    }

    public static void bootstrapSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(
                MOUND_SET,
                new StructureSet(
                        structures.getOrThrow(MOUND),
                        new RandomSpreadStructurePlacement(
                                MOUND_SPACING, MOUND_SEPARATION, RandomSpreadType.LINEAR, MOUND_SALT)));
        context.register(
                ELDRITCH_OBELISK_SET,
                new StructureSet(
                        structures.getOrThrow(ELDRITCH_OBELISK),
                        new RandomSpreadStructurePlacement(
                                OBELISK_SPACING, OBELISK_SEPARATION, RandomSpreadType.LINEAR, OBELISK_SALT)));
    }
}
