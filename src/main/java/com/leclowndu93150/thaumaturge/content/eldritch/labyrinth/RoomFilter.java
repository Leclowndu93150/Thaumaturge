package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

final class RoomFilter implements Predicate<Holder<RoomType>> {
    private static final String TAG_PREFIX = "#";

    private final Set<ResourceLocation> ids = new HashSet<>();
    private final List<TagKey<RoomType>> tags = new ArrayList<>();

    RoomFilter(List<? extends String> entries) {
        for (String entry : entries) {
            boolean tag = entry.startsWith(TAG_PREFIX);
            ResourceLocation id = ResourceLocation.tryParse(tag ? entry.substring(TAG_PREFIX.length()) : entry);
            if (id == null) {
                continue;
            }
            if (tag) {
                tags.add(TagKey.create(RoomType.REGISTRY_KEY, id));
            } else {
                ids.add(id);
            }
        }
    }

    @Override
    public boolean test(Holder<RoomType> room) {
        if (room.unwrapKey().map(key -> ids.contains(key.location())).orElse(false)) {
            return false;
        }
        for (TagKey<RoomType> tag : tags) {
            if (room.is(tag)) {
                return false;
            }
        }
        return true;
    }
}
