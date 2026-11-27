package com.leclowndu93150.thaumaturge.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.junit.jupiter.api.Test;

final class AspectReadoutPlacementTest {
    @Test
    void onlyAirGlassAndGlassPanesCanHostAnAspectReadout() {
        BlockState air = mock(BlockState.class);
        when(air.isAir()).thenReturn(true);

        BlockState glass = mock(BlockState.class);
        when(glass.is(Tags.Blocks.GLASS_BLOCKS)).thenReturn(true);

        BlockState pane = mock(BlockState.class);
        when(pane.is(Tags.Blocks.GLASS_PANES)).thenReturn(true);

        BlockState otherNonSolid = mock(BlockState.class);
        when(otherNonSolid.isAir()).thenReturn(false);
        when(otherNonSolid.is(Tags.Blocks.GLASS_BLOCKS)).thenReturn(false);
        when(otherNonSolid.is(Tags.Blocks.GLASS_PANES)).thenReturn(false);

        assertTrue(AspectReadoutPlacement.canHost(air));
        assertTrue(AspectReadoutPlacement.canHost(glass));
        assertTrue(AspectReadoutPlacement.canHost(pane));
        assertFalse(AspectReadoutPlacement.canHost(otherNonSolid));
    }

    @Test
    void diagonalFallbackChoosesAnAvailableCellTowardTheViewer() {
        BlockPos origin = new BlockPos(10, 20, 30);

        BlockPos chosen = AspectReadoutPlacement.frontDiagonal(origin, new Vec3(4.0, 0.0, -3.0), candidate -> true);

        assertEquals(origin.offset(1, 0, -1), chosen);
    }

    @Test
    void diagonalFallbackNeverPlacesTheReadoutBehindTheTarget() {
        BlockPos origin = BlockPos.ZERO;
        BlockPos behind = origin.offset(-1, 0, -1);

        BlockPos chosen = AspectReadoutPlacement.frontDiagonal(
                origin, new Vec3(1.0, 0.0, 1.0), candidate -> candidate.equals(behind));

        assertNull(chosen);
    }

    @Test
    void cardinalFaceMustPointTowardTheViewer() {
        Vec3 viewerDirection = new Vec3(1.0, 0.0, 1.0);

        assertTrue(AspectReadoutPlacement.isViewerFacing(Direction.EAST, viewerDirection));
        assertTrue(AspectReadoutPlacement.isViewerFacing(Direction.SOUTH, viewerDirection));
        assertFalse(AspectReadoutPlacement.isViewerFacing(Direction.WEST, viewerDirection));
        assertFalse(AspectReadoutPlacement.isViewerFacing(Direction.NORTH, viewerDirection));
        assertFalse(AspectReadoutPlacement.isViewerFacing(Direction.UP, viewerDirection));
    }
}
