package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class TemplateBlocks {
    private TemplateBlocks() {}

    public record Entry(BlockPos pos, BlockState state, Optional<CompoundTag> nbt) {}

    public static List<Entry> read(StructureTemplate template, HolderGetter<Block> blocks) {
        CompoundTag tag = template.save(new CompoundTag());
        Vec3i size = template.getSize();
        ListTag paletteTag = tag.contains("palette", Tag.TAG_LIST)
                ? tag.getList("palette", Tag.TAG_COMPOUND)
                : tag.getList("palettes", Tag.TAG_LIST).getList(0);
        List<BlockState> palette = new ArrayList<>(paletteTag.size());
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(NbtUtils.readBlockState(blocks, paletteTag.getCompound(i)));
        }
        ListTag blockTags = tag.getList("blocks", Tag.TAG_COMPOUND);
        List<Entry> entries = new ArrayList<>(blockTags.size());
        for (int i = 0; i < blockTags.size(); i++) {
            CompoundTag block = blockTags.getCompound(i);
            ListTag posTag = block.getList("pos", Tag.TAG_INT);
            BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
            int stateId = block.getInt("state");
            if (stateId < 0
                    || stateId >= palette.size()
                    || pos.getX() < 0
                    || pos.getY() < 0
                    || pos.getZ() < 0
                    || pos.getX() >= size.getX()
                    || pos.getY() >= size.getY()
                    || pos.getZ() >= size.getZ()) {
                continue;
            }
            entries.add(new Entry(
                    pos,
                    palette.get(stateId),
                    block.contains("nbt", Tag.TAG_COMPOUND)
                            ? Optional.of(block.getCompound("nbt"))
                            : Optional.empty()));
        }
        return entries;
    }
}
