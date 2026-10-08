package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

public record RoomPlacement(Holder<RoomType> room, ResourceLocation template, int transform, int anchorX, int anchorZ) {
    public int width() {
        return Dihedral.width(room.value().width(), room.value().depth(), transform);
    }

    public int depth() {
        return Dihedral.depth(room.value().width(), room.value().depth(), transform);
    }
}
