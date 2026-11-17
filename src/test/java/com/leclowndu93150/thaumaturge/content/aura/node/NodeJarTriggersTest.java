package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.casters.CasterBlockActions;
import com.leclowndu93150.thaumaturge.api.casters.CasterBlockUse;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.junit.jupiter.api.Test;

class NodeJarTriggersTest {
    private static final BlockPos CLICKED = BlockPos.ZERO.north();

    @Test
    void dispatchesWandClicksOnWardedAndOrdinaryGlass() {
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        var wardedGlass = TTBlocks.WARDED_GLASS.get().defaultBlockState();
        var ordinaryGlass = mock(BlockState.class);
        when(ordinaryGlass.is(Tags.Blocks.GLASS_BLOCKS)).thenReturn(true);
        mockEnclosure(level);

        assertTrue(CasterBlockActions.dispatch(
                new CasterBlockUse(level, ItemStack.EMPTY, player, CLICKED, Direction.NORTH, wardedGlass)));
        assertTrue(CasterBlockActions.dispatch(
                new CasterBlockUse(level, ItemStack.EMPTY, player, CLICKED, Direction.NORTH, ordinaryGlass)));
    }

    @Test
    void leavesAnotherPlayersWardInPlaceBeforeSpendingVis() {
        ServerLevel level = mock(ServerLevel.class);
        Player player = mock(Player.class);
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        mockEnclosure(level);
        PlayerResearch research = mock(PlayerResearch.class);
        when(research.hasCompleted(NodeJarRitual.RESEARCH_NODE_JAR)).thenReturn(true);

        try (var researchAccess = mockStatic(PlayerResearchAccess.class);
                var wards = mockStatic(WardHandler.class);
                var vis = mockStatic(WandVisHelper.class)) {
            researchAccess.when(() -> PlayerResearchAccess.of(player)).thenReturn(research);
            wards.when(() -> WardHandler.owner(level, CLICKED)).thenReturn(UUID.randomUUID());

            assertTrue(NodeJarRitual.tryJarNode(level, BlockPos.ZERO, player));
            vis.verifyNoInteractions();
            wards.verify(() -> WardHandler.unward(any(), any(), any()), never());
        }
    }

    private static void mockEnclosure(Level level) {
        BlockState wardedGlass = TTBlocks.WARDED_GLASS.get().defaultBlockState();
        BlockState slab = mock(BlockState.class);
        when(slab.is(BlockTags.WOODEN_SLABS)).thenReturn(true);
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(call -> call.<BlockPos>getArgument(0).getY() == 2 ? slab : wardedGlass);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(mock(BlockEntityNode.class));
    }
}
