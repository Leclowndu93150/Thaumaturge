package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.google.common.hash.Hashing;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

final class TemplateNbtWriter {
    private static final String MARKER_MODE = "DATA";
    private static final String EXTENSION = "nbt";
    private static final String STRUCTURE_PATH = "structure";

    private TemplateNbtWriter() {}

    static PackOutput.PathProvider structures(PackOutput output) {
        return output.createPathProvider(PackOutput.Target.DATA_PACK, STRUCTURE_PATH);
    }

    static CompoundTag write(TemplateSource source) {
        Object2IntMap<BlockState> palette = new Object2IntLinkedOpenHashMap<>();
        ListTag blocks = new ListTag();
        for (int x = 0; x < source.sizeX(); x++) {
            for (int y = 0; y < source.sizeY(); y++) {
                for (int z = 0; z < source.sizeZ(); z++) {
                    BlockState state = source.get(x, y, z);
                    if (state == null) {
                        continue;
                    }
                    int id = palette.computeIfAbsent(state, ignored -> palette.size());
                    CompoundTag block = new CompoundTag();
                    block.put("pos", ints(x, y, z));
                    block.putInt("state", id);
                    Optional<String> metadata = source.metadata(x, y, z);
                    metadata.ifPresent(value -> block.put("nbt", markerTag(value)));
                    blocks.add(block);
                }
            }
        }
        ListTag paletteTag = new ListTag();
        for (BlockState state : palette.keySet()) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        CompoundTag root = new CompoundTag();
        root.put("size", ints(source.sizeX(), source.sizeY(), source.sizeZ()));
        root.put("palette", paletteTag);
        root.put("blocks", blocks);
        root.put("entities", new ListTag());
        return NbtUtils.addCurrentDataVersion(root);
    }

    static CompletableFuture<?> save(
            CachedOutput output, PackOutput.PathProvider path, ResourceLocation id, CompoundTag tag) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                NbtIo.writeCompressed(tag, bytes);
                byte[] data = bytes.toByteArray();
                output.writeIfNeeded(
                        path.file(id, EXTENSION), data, Hashing.sha1().hashBytes(data));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write structure template " + id, e);
            }
        });
    }

    private static CompoundTag markerTag(String metadata) {
        CompoundTag tag = new CompoundTag();
        tag.putString(
                "id",
                BuiltInRegistries.BLOCK_ENTITY_TYPE
                        .getKey(BlockEntityType.STRUCTURE_BLOCK)
                        .toString());
        tag.putString("mode", MARKER_MODE);
        tag.putString("metadata", metadata);
        return tag;
    }

    private static ListTag ints(int x, int y, int z) {
        ListTag list = new ListTag();
        list.add(IntTag.valueOf(x));
        list.add(IntTag.valueOf(y));
        list.add(IntTag.valueOf(z));
        return list;
    }
}
