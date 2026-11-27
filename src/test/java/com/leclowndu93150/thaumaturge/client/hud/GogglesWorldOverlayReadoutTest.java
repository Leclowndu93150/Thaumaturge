package com.leclowndu93150.thaumaturge.client.hud;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

final class GogglesWorldOverlayReadoutTest {
    @Test
    void containerAndQueryCapabilitiesReceiveTheHitFaceAndKeepTheirAmountSemantics() {
        Level level = mock(Level.class);
        BlockPos pos = new BlockPos(3, 4, 5);
        Direction side = Direction.WEST;
        AspectList stored = mock(AspectList.class);
        IAspectContainer container = mock(IAspectContainer.class);
        when(container.getAspects()).thenReturn(stored);
        when(level.getCapability(AspectCapabilities.CONTAINER, pos, side)).thenReturn(container);

        GogglesWorldOverlay.AspectReadout containerReadout = GogglesWorldOverlay.readAspects(level, null, pos, side);

        assertSame(stored, containerReadout.aspects());
        assertTrue(containerReadout.showAmounts());
        verify(level).getCapability(AspectCapabilities.CONTAINER, pos, side);

        IAspectQuery query = mock(IAspectQuery.class);
        AspectList intent = mock(AspectList.class);
        when(level.getCapability(AspectCapabilities.CONTAINER, pos, side)).thenReturn(null);
        when(level.getCapability(EssentiaCapabilities.ASPECT_QUERY, pos, side)).thenReturn(query);
        when(query.queryAspects()).thenReturn(intent);

        GogglesWorldOverlay.AspectReadout queryReadout = GogglesWorldOverlay.readAspects(level, null, pos, side);

        assertSame(intent, queryReadout.aspects());
        assertFalse(queryReadout.showAmounts());
        verify(level).getCapability(EssentiaCapabilities.ASPECT_QUERY, pos, side);
        verify(query).queryAspects();
    }

    @Test
    void infusionMatrixRetainsItsNativeAmountReadoutWithoutCapabilityLookup() {
        Level level = mock(Level.class);
        BlockPos pos = new BlockPos(1, 2, 3);
        BlockEntityInfusionMatrix matrix = mock(BlockEntityInfusionMatrix.class);
        AspectList remaining = mock(AspectList.class);
        when(matrix.remainingEssentia()).thenReturn(remaining);

        GogglesWorldOverlay.AspectReadout readout = GogglesWorldOverlay.readAspects(level, matrix, pos, Direction.UP);

        assertSame(remaining, readout.aspects());
        assertTrue(readout.showAmounts());
        verifyNoInteractions(level);
    }

    @Test
    void jarredNodesExposeTheirAspectReadoutWithoutANodeScan() {
        Level level = mock(Level.class);
        BlockPos pos = new BlockPos(1, 2, 3);
        BlockEntityJarNode jarredNode = mock(BlockEntityJarNode.class);
        AspectList aspects = mock(AspectList.class);
        when(jarredNode.getAspects()).thenReturn(aspects);

        GogglesWorldOverlay.AspectReadout readout =
                GogglesWorldOverlay.readAspects(level, jarredNode, pos, Direction.UP);

        assertSame(aspects, readout.aspects());
        assertTrue(readout.showAmounts());
        assertFalse(GogglesWorldOverlay.requiresNodeScan(jarredNode));
        assertTrue(GogglesWorldOverlay.requiresNodeScan(mock(BlockEntityNode.class)));
        verifyNoInteractions(level);
    }
}
