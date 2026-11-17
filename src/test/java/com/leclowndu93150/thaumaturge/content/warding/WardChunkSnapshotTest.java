package com.leclowndu93150.thaumaturge.content.warding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.network.ClientboundWardChunkPayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.Test;

class WardChunkSnapshotTest {
    @Test
    void takesASnapshotOfWardsAtPlotCoordinates() {
        BlockPos pos = new BlockPos(20_480_013, -17, 20_480_002);
        UUID owner = UUID.randomUUID();
        LevelChunk chunk = mock(LevelChunk.class);
        WardChunkData wards = new WardChunkData();
        when(chunk.getPos()).thenReturn(new ChunkPos(pos));
        when(chunk.hasData(TTAttachments.WARDS.get())).thenReturn(true);
        when(chunk.getData(TTAttachments.WARDS.get())).thenReturn(wards);

        assertNull(WardHandler.chunkPayload(chunk));
        wards.put(pos, owner);
        ClientboundWardChunkPayload payload = WardHandler.chunkPayload(chunk);
        assertNotNull(payload);
        var group = payload.groups().getFirst();
        assertEquals(owner, group.owner());
        assertEquals(pos, payload.unpack(group.positions().getFirst()));
    }
}
