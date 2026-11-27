package com.leclowndu93150.thaumaturge.content.spell.modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class EchoModifierTest {
    private static final int CHAIN_LENGTH = 4;

    @Test
    void chainedEchoesCreateLinearRepeatsInsteadOfMultiplyingEachOther() {
        List<SpellTarget> targets = List.of(new SpellTarget(
                BlockHitResult.miss(Vec3.ZERO, Direction.NORTH, BlockPos.ZERO), Vec3.ZERO, new Vec3(0.0, 0.0, 1.0)));
        ArrayDeque<PendingEcho> pending = new ArrayDeque<>();
        List<Integer> delays = new ArrayList<>();
        int[] effects = {0};

        runChain(0, SpellState.DEFAULT, targets, pending, delays, effects);
        while (!pending.isEmpty()) {
            PendingEcho echo = pending.removeFirst();
            runChain(echo.nextEcho(), echo.state(), targets, pending, delays, effects);
        }

        assertEquals(CHAIN_LENGTH + 1, effects[0]);
        assertEquals(CHAIN_LENGTH, delays.size());
    }

    private static void runChain(
            int index,
            SpellState state,
            List<SpellTarget> targets,
            ArrayDeque<PendingEcho> pending,
            List<Integer> delays,
            int[] effects) {
        if (index == CHAIN_LENGTH) {
            effects[0]++;
            return;
        }

        CastContext context = mock(CastContext.class);
        when(context.state()).thenReturn(state);
        when(context.setting("repeats")).thenReturn(1);
        when(context.hasNext()).thenReturn(true);
        doAnswer(invocation -> {
                    runChain(index + 1, state, targets, pending, delays, effects);
                    return null;
                })
                .when(context)
                .proceed(anyList());
        doAnswer(invocation -> {
                    delays.add(invocation.getArgument(0));
                    pending.addLast(new PendingEcho(index + 1, invocation.getArgument(2)));
                    return null;
                })
                .when(context)
                .proceedLater(anyInt(), anyList(), any(SpellState.class));

        new EchoModifier(1, 0.5F).execute(context, targets);
    }

    private record PendingEcho(int nextEcho, SpellState state) {}
}
