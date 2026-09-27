package com.leclowndu93150.thaumaturge.content.wands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ItemWandTest {
    private final ItemWand wand = mock(ItemWand.class, CALLS_REAL_METHODS);
    private final ServerLevel level = mock(ServerLevel.class);
    private final Player player = mock(Player.class);
    private final ItemStack stack = mock(ItemStack.class);
    private final BlockEntityNode node = mock(BlockEntityNode.class);
    private final BlockPos pos = new BlockPos(0, 2, 5);

    private void aimAtNode() {
        when(player.level()).thenReturn(level);
        when(player.blockInteractionRange()).thenReturn(6.0);
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(stack);
        when(player.pick(6.0, 1.0F, false))
                .thenReturn(new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
        when(level.getBlockEntity(pos)).thenReturn(node);
    }

    @Test
    void nodeStartsChannelingBeforeInspectingTheInstalledFocus() {
        aimAtNode();

        assertEquals(
                InteractionResult.CONSUME,
                wand.use(level, player, InteractionHand.MAIN_HAND).getResult());

        verify(player).startUsingItem(InteractionHand.MAIN_HAND);
        verify(wand, never()).getFocusStack(stack);
    }

    @Test
    void channelingUsesCurrentAimAndThePlayersFullBlockReach() {
        aimAtNode();

        wand.onUseTick(level, player, stack, 71995);

        verify(node).drainToWand(level, player, stack, 71995);
        verify(player).pick(6.0, 1.0F, false);
    }

    @Test
    void aMissDoesNotTreatTheEndPositionAsANode() {
        aimAtNode();
        when(player.pick(6.0, 1.0F, false)).thenReturn(BlockHitResult.miss(Vec3.atCenterOf(pos), Direction.NORTH, pos));
        doReturn(ItemStack.EMPTY).when(wand).getFocusStack(stack);

        wand.use(level, player, InteractionHand.MAIN_HAND);

        verify(level, never()).getBlockEntity(pos);
        verify(wand).getFocusStack(stack);
    }
}
