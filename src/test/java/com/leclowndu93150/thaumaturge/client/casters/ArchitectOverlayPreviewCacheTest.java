package com.leclowndu93150.thaumaturge.client.casters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class ArchitectOverlayPreviewCacheTest {
    @Test
    void refreshesOnDistinctAnchorsWithCollidingConcatenatedCoordinates() {
        ArchitectOverlayRenderer.PreviewCache cache = new ArchitectOverlayRenderer.PreviewCache();
        BlockPos firstAnchor = new BlockPos(1, 23, 4);
        BlockPos secondAnchor = new BlockPos(12, 3, 4);

        assertTrue(cache.update(null, WorldFrame.INSTANCE, firstAnchor, Direction.UP, 7));
        assertTrue(cache.update(null, WorldFrame.INSTANCE, secondAnchor, Direction.UP, 7));
        assertFalse(cache.update(null, WorldFrame.INSTANCE, secondAnchor, Direction.UP, 7));
    }

    @Test
    void refreshesWhenFaceOrTimeBucketChanges() {
        ArchitectOverlayRenderer.PreviewCache cache = new ArchitectOverlayRenderer.PreviewCache();
        BlockPos anchor = new BlockPos(3, 5, 8);

        assertTrue(cache.update(null, WorldFrame.INSTANCE, anchor, Direction.UP, 7));
        assertTrue(cache.update(null, WorldFrame.INSTANCE, anchor, Direction.NORTH, 7));
        assertTrue(cache.update(null, WorldFrame.INSTANCE, anchor, Direction.NORTH, 8));
        assertFalse(cache.update(null, WorldFrame.INSTANCE, anchor, Direction.NORTH, 8));
    }

    @Test
    void refreshesWhenLevelOrFrameChangesAtTheSameCoordinates() {
        ArchitectOverlayRenderer.PreviewCache cache = new ArchitectOverlayRenderer.PreviewCache();
        Level firstLevel = mock(Level.class);
        Level secondLevel = mock(Level.class);
        SpaceFrame firstFrame = mock(SpaceFrame.class);
        SpaceFrame secondFrame = mock(SpaceFrame.class);
        BlockPos anchor = new BlockPos(20_480_000, 64, 20_480_000);

        assertTrue(cache.update(firstLevel, firstFrame, anchor, Direction.UP, 7));
        assertFalse(cache.update(firstLevel, firstFrame, anchor, Direction.UP, 7));
        assertTrue(cache.update(firstLevel, secondFrame, anchor, Direction.UP, 7));
        assertTrue(cache.update(secondLevel, secondFrame, anchor, Direction.UP, 7));
        assertFalse(cache.update(secondLevel, secondFrame, anchor, Direction.UP, 7));
    }
}
