package com.leclowndu93150.thaumaturge.content.eldritch.site;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.eldritch.altar.BlockEntityEldritchAltar;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.StripMarkersProcessor;
import com.leclowndu93150.thaumaturge.registry.TTStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class ObeliskSitePiece extends TemplateStructurePiece {
    public static final String NODE_MARKER = "thaumaturge:site_node";
    private static final ResourceLocation DEFAULT_TEMPLATE = TTIds.rl("obelisk/site");
    private static final String ROTATION = "Rotation";
    private static final String SITE = "Site";

    private final Rotation rotation;
    private final ResourceKey<ObeliskSite> site;
    private boolean processorsAdded;

    public ObeliskSitePiece(
            StructureTemplateManager manager,
            ResourceLocation template,
            BlockPos position,
            Rotation rotation,
            ResourceKey<ObeliskSite> site) {
        super(
                TTStructures.ELDRITCH_OBELISK_PIECE.get(),
                0,
                manager,
                template,
                template.toString(),
                settings(rotation),
                position);
        this.rotation = rotation;
        this.site = site;
    }

    public ObeliskSitePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(
                TTStructures.ELDRITCH_OBELISK_PIECE.get(),
                tag,
                context.structureTemplateManager(),
                id -> settings(readRotation(tag)));
        this.rotation = readRotation(tag);
        ResourceLocation siteId = ResourceLocation.tryParse(tag.getString(SITE));
        this.site = siteId == null ? ObeliskSite.DORMANT : ResourceKey.create(ObeliskSite.REGISTRY_KEY, siteId);
    }

    private static Rotation readRotation(CompoundTag tag) {
        String name = tag.contains(ROTATION) ? tag.getString(ROTATION) : Rotation.NONE.name();
        for (Rotation value : Rotation.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return Rotation.NONE;
    }

    private static StructurePlaceSettings settings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .addProcessor(StripMarkersProcessor.INSTANCE);
    }

    @Override
    protected ResourceLocation makeTemplateLocation() {
        ResourceLocation parsed = ResourceLocation.tryParse(templateName);
        return parsed == null || templateName.isEmpty() ? DEFAULT_TEMPLATE : parsed;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString(ROTATION, rotation.name());
        tag.putString(SITE, site.location().toString());
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBB,
            ChunkPos chunkPos,
            BlockPos referencePos) {
        if (!processorsAdded) {
            processorsAdded = true;
            definition(level)
                    .flatMap(ObeliskSite::processors)
                    .ifPresent(list -> list.value().list().forEach(placeSettings::addProcessor));
        }
        super.postProcess(level, structureManager, generator, random, chunkBB, chunkPos, referencePos);
    }

    @Override
    protected void handleDataMarker(
            String markerId, BlockPos position, ServerLevelAccessor level, RandomSource random, BoundingBox chunkBB) {
        if (!NODE_MARKER.equals(markerId) || !chunkBB.isInside(position)) {
            return;
        }
        Optional<ObeliskSite> definition = definition(level);
        NodeData data = NodeGenerator.rollRandomNodeData(
                level,
                position,
                random,
                false,
                true,
                false,
                NodeGenerator.DEFAULT_SPECIAL_RARITY,
                NodeGenerator.DEFAULT_BASE_AURA);
        if (data != null) {
            NodeGenerator.createNodeAt(
                    level,
                    position,
                    definition.map(ObeliskSite::node).orElse(data.type()),
                    data.modifier().orElse(null),
                    data.aspects());
        }
        if (chunkBB.isInside(position.below())
                && level.getBlockEntity(position.below()) instanceof BlockEntityEldritchAltar altar) {
            altar.assignSite(site);
        }
    }

    private Optional<ObeliskSite> definition(ServerLevelAccessor level) {
        return ObeliskSite.lookup(level.registryAccess(), site);
    }
}
