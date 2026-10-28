package com.leclowndu93150.thaumaturge.server.command.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import org.junit.jupiter.api.Test;

class LocateSubcommandTest {
    @Test
    void clickableCoordinatesSuggestTeleportCommand() {
        ClickEvent click = LocateSubcommand.coordinates(new BlockPos(12, 64, -8))
                .getStyle()
                .getClickEvent();

        assertNotNull(click);
        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, click.getAction());
        assertEquals("/tp @s 12 64 -8", click.getValue());
    }
}
