package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

final class TaintLegacyData {
    private TaintLegacyData() {}

    static void importChunk(ServerLevel level, LevelChunk chunk) {
        if (chunk.getData(TTAttachments.TAINT_LEGACY_IMPORTED.get())) return;
        TaintEcologyState.get(level).copyTo(chunk);
        TaintBiomeState.get(level).copyTo(chunk);
        chunk.setData(TTAttachments.TAINT_LEGACY_IMPORTED.get(), true);
        chunk.setUnsaved(true);
    }
}
