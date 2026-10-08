package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.TemplateBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

final class ArenaManifestJob {
    private static final Comparator<TemplateBlocks.Entry> ORDER = Comparator.<TemplateBlocks.Entry>comparingInt(
                    entry -> entry.pos().getY())
            .thenComparingInt(entry -> entry.pos().getX())
            .thenComparingInt(entry -> entry.pos().getZ());

    private final List<TemplateBlocks.Entry> blocks;
    private final BlockPos origin;
    private int next;

    private ArenaManifestJob(List<TemplateBlocks.Entry> blocks, BlockPos origin) {
        this.blocks = blocks;
        this.origin = origin;
    }

    static ArenaManifestJob none() {
        return new ArenaManifestJob(List.of(), BlockPos.ZERO);
    }

    static Optional<ArenaManifestJob> create(ServerLevel level, ResourceLocation template, BlockPos anchor) {
        Optional<StructureTemplate> found = level.getStructureManager().get(template);
        if (found.isEmpty()) {
            Thaumaturge.LOGGER.warn(
                    "Labyrinth arena template {} is missing; the encounter starts without it", template);
            return Optional.empty();
        }
        Vec3i size = found.get().getSize();
        List<TemplateBlocks.Entry> entries = new ArrayList<>(
                TemplateBlocks.read(found.get(), level.registryAccess().lookupOrThrow(Registries.BLOCK)));
        entries.removeIf(entry -> entry.state().isAir()
                || entry.state().is(Blocks.STRUCTURE_VOID)
                || entry.state().is(Blocks.STRUCTURE_BLOCK));
        entries.sort(ORDER);
        return Optional.of(new ArenaManifestJob(entries, anchor.offset(-size.getX() / 2, 0, -size.getZ() / 2)));
    }

    boolean done() {
        return next >= blocks.size();
    }

    void step(ServerLevel level, int budget) {
        int placed = 0;
        while (next < blocks.size() && placed < budget) {
            TemplateBlocks.Entry entry = blocks.get(next);
            BlockPos pos = origin.offset(entry.pos());
            if (!level.isLoaded(pos)) {
                return;
            }
            next++;
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            level.setBlock(pos, entry.state(), Block.UPDATE_ALL);
            placed++;
        }
    }
}
