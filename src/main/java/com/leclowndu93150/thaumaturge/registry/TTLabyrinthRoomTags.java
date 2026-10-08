package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import net.minecraft.tags.TagKey;

public final class TTLabyrinthRoomTags {
    public static final TagKey<RoomType> PASSAGES = key("labyrinth/passages");
    public static final TagKey<RoomType> FALLBACK = key("labyrinth/fallback");
    public static final TagKey<RoomType> PORTAL = key("labyrinth/portal");
    public static final TagKey<RoomType> KEY = key("labyrinth/key");
    public static final TagKey<RoomType> BOSS_HALLS = key("labyrinth/boss_halls");
    public static final TagKey<RoomType> BOSS_HALLS_OPEN = key("labyrinth/boss_halls/open");
    public static final TagKey<RoomType> NESTS = key("labyrinth/nests");
    public static final TagKey<RoomType> LIBRARIES = key("labyrinth/libraries");
    public static final TagKey<RoomType> RARE = key("labyrinth/rare");

    private TTLabyrinthRoomTags() {}

    private static TagKey<RoomType> key(String path) {
        return TagKey.create(RoomType.REGISTRY_KEY, TTIds.rl(path));
    }
}
