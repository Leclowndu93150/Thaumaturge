package com.leclowndu93150.thaumaturge.content.eldritch.site;

import com.leclowndu93150.thaumaturge.registry.TTStructures;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class ObeliskSiteStructure extends Structure {
    public static final MapCodec<ObeliskSiteStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    settingsCodec(instance),
                    SimpleWeightedRandomList.wrappedCodec(ResourceKey.codec(ObeliskSite.REGISTRY_KEY))
                            .fieldOf("sites")
                            .forGetter(structure -> structure.sites))
            .apply(instance, ObeliskSiteStructure::new));

    private static final int LEVEL_PROBE = 4;
    private static final int MAX_SLOPE = 2;
    private static final int[][] PROBES = {
        {0, 0},
        {LEVEL_PROBE, LEVEL_PROBE},
        {-LEVEL_PROBE, LEVEL_PROBE},
        {LEVEL_PROBE, -LEVEL_PROBE},
        {-LEVEL_PROBE, -LEVEL_PROBE},
        {0, LEVEL_PROBE},
        {0, -LEVEL_PROBE},
        {LEVEL_PROBE, 0},
        {-LEVEL_PROBE, 0}
    };

    private final SimpleWeightedRandomList<ResourceKey<ObeliskSite>> sites;

    public ObeliskSiteStructure(
            Structure.StructureSettings settings, SimpleWeightedRandomList<ResourceKey<ObeliskSite>> sites) {
        super(settings);
        this.sites = sites;
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int[] probe : PROBES) {
            int surface = context.chunkGenerator()
                    .getFirstOccupiedHeight(
                            x + probe[0],
                            z + probe[1],
                            Heightmap.Types.WORLD_SURFACE_WG,
                            context.heightAccessor(),
                            context.randomState());
            int floor = context.chunkGenerator()
                    .getFirstOccupiedHeight(
                            x + probe[0],
                            z + probe[1],
                            Heightmap.Types.OCEAN_FLOOR_WG,
                            context.heightAccessor(),
                            context.randomState());
            if (surface != floor) {
                return Optional.empty();
            }
            lowest = Math.min(lowest, floor);
            highest = Math.max(highest, floor);
        }
        if (highest - lowest > MAX_SLOPE) {
            return Optional.empty();
        }
        ResourceKey<ObeliskSite> siteKey =
                sites.getRandomValue(context.random()).orElseThrow();
        Optional<ObeliskSite> site = ObeliskSite.lookup(context.registryAccess(), siteKey);
        if (site.isEmpty()) {
            return Optional.empty();
        }
        Rotation rotation = Rotation.getRandom(context.random());
        Vec3i size = context.structureTemplateManager()
                .getOrCreate(site.get().template())
                .getSize();
        BlockPos corner = new BlockPos(x - size.getX() / 2, lowest, z - size.getZ() / 2);
        BlockPos position =
                StructureTemplate.getZeroPositionWithTransform(corner, Mirror.NONE, rotation, size.getX(), size.getZ());
        return Optional.of(new Structure.GenerationStub(
                corner,
                builder -> builder.addPiece(new ObeliskSitePiece(
                        context.structureTemplateManager(), site.get().template(), position, rotation, siteKey))));
    }

    @Override
    public StructureType<?> type() {
        return TTStructures.ELDRITCH_OBELISK.get();
    }
}
