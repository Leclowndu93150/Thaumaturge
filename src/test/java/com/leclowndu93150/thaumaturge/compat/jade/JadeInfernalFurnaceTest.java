package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JadeInfernalFurnaceTest {
    @ParameterizedTest
    @CsvSource({"140, 140, 0", "70, 140, 50", "1, 140, 99", "0, 140, 0", "150, 140, 0", "0, 0, -1"})
    void displaysCompletedProgressInsteadOfRemainingTime(int remaining, int total, int expectedPercent) {
        BlockEntityInfernalFurnace furnace = mock(BlockEntityInfernalFurnace.class);
        furnace.furnaceCookTime = remaining;
        furnace.furnaceMaxCookTime = total;
        when(furnace.inventory()).thenReturn(new ItemStackHandler(32));
        JadeDetailBuilder builder = new JadeDetailBuilder(JadeConfig.INFERNAL_FURNACES, mock(Player.class));

        JadeMachineDetails.furnace(furnace, builder);

        assertEquals(
                expectedPercent < 0
                        ? List.of()
                        : List.of(Component.translatable("jade.thaumaturge.machine.progress", expectedPercent)),
                builder.build().details());
    }
}
