package com.leclowndu93150.thaumaturge.client.eldritch;

import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryView;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;

public final class ReliquaryViewHolder {
    private static final Long2ObjectMap<ReliquaryView> VIEWS = new Long2ObjectOpenHashMap<>();

    private ReliquaryViewHolder() {}

    public static void put(BlockPos pos, ReliquaryView view) {
        VIEWS.put(pos.asLong(), view);
    }

    public static ReliquaryView get(BlockPos pos) {
        return VIEWS.getOrDefault(pos.asLong(), ReliquaryView.INELIGIBLE);
    }

    public static void clear() {
        VIEWS.clear();
    }
}
