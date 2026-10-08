package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class RoomCompiler {
    private static final String METADATA = "metadata";

    private RoomCompiler() {}

    public static CompiledRoom compile(
            ResourceLocation id, StructureTemplate template, HolderLookup.Provider registries) {
        Vec3i size = template.getSize();
        HolderGetter<Block> blockLookup = registries.lookupOrThrow(Registries.BLOCK);
        byte[] kinds = new byte[size.getX() * size.getY() * size.getZ()];
        List<CompiledRoom.Marker> markers = new ArrayList<>();
        List<BlockPos> barriers = new ArrayList<>();
        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (TemplateBlocks.Entry entry : TemplateBlocks.read(template, blockLookup)) {
            BlockPos pos = entry.pos();
            BlockState state = entry.state();
            RoomVoxels.Kind kind = kindOf(state);
            kinds[CompiledRoom.index(pos.getX(), pos.getY(), pos.getZ(), size)] = (byte) kind.ordinal();
            if (state.is(TTBlockTags.LABYRINTH_BARRIER)) {
                barriers.add(pos);
            }
            if (kind == RoomVoxels.Kind.MARKER) {
                entry.nbt()
                        .filter(nbt -> nbt.contains(METADATA, Tag.TAG_STRING))
                        .map(nbt -> nbt.getString(METADATA))
                        .ifPresent(metadata -> parseMarker(id, pos, metadata, ops, markers));
            }
        }
        return new CompiledRoom(id, template, size, kinds, List.copyOf(markers), List.copyOf(barriers));
    }

    private static RoomVoxels.Kind kindOf(BlockState state) {
        if (state.isAir()) {
            return RoomVoxels.Kind.AIR;
        }
        if (state.is(Blocks.STRUCTURE_BLOCK)) {
            return RoomVoxels.Kind.MARKER;
        }
        return state.is(TTBlockTags.LABYRINTH_PASSABLE) ? RoomVoxels.Kind.PASSABLE : RoomVoxels.Kind.SOLID;
    }

    private static void parseMarker(
            ResourceLocation id,
            BlockPos pos,
            String metadata,
            RegistryOps<JsonElement> ops,
            List<CompiledRoom.Marker> markers) {
        if (metadata.isBlank()) {
            return;
        }
        try {
            LabyrinthMarker.CODEC
                    .parse(ops, JsonParser.parseString(metadata))
                    .resultOrPartial(
                            error -> Thaumaturge.LOGGER.error("Bad labyrinth marker at {} in {}: {}", pos, id, error))
                    .ifPresent(marker -> markers.add(new CompiledRoom.Marker(pos, marker)));
        } catch (JsonParseException e) {
            Thaumaturge.LOGGER.error("Labyrinth marker at {} in {} is not valid JSON: {}", pos, id, metadata, e);
        }
    }
}
