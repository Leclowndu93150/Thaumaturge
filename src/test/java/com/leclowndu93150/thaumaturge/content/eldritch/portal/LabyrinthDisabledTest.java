package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class LabyrinthDisabledTest {
    @Test
    void disabledPortalDoesNotStartTransitAndShowsMessage() throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        when(level.getGameTime()).thenReturn(100L);
        when(player.getData(TTAttachments.LABYRINTH_TRANSIT)).thenReturn(new TransitState());
        try (var messages = mockStatic(TTActionBar.class)) {
            assertFalse(LabyrinthTransit.touch(level, player, Optional.empty(), false));
            messages.verify(() -> TTActionBar.sendPurple(player, "gui.thaumaturge.labyrinth.disabled"));
        }
    }

    @Test
    void disabledEntryDoesNotBlockTheExit() {
        assertEquals(true, ThaumaturgeServerConfig.LABYRINTH.enabled.getDefault());
        var entry = Optional.of(new PortalLink(PortalLink.Kind.TO_LABYRINTH, null));
        var exit = Optional.of(new PortalLink(PortalLink.Kind.TO_ORIGIN, null));
        assertFalse(LabyrinthTransit.entryDisabled(true, entry, Level.OVERWORLD));
        assertTrue(LabyrinthTransit.entryDisabled(false, entry, Level.OVERWORLD));
        assertTrue(LabyrinthTransit.entryDisabled(false, Optional.empty(), Level.OVERWORLD));
        assertFalse(LabyrinthTransit.entryDisabled(false, exit, OuterLands.DIMENSION));
        assertFalse(LabyrinthTransit.entryDisabled(false, Optional.empty(), OuterLands.DIMENSION));
    }
}
