package com.leclowndu93150.thaumaturge.content.essentia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.infusion.EssentiaSources;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

class PortAndResonatorCompatibilityTest {
    private ServerLevel level;
    private Holder<IAspect> aspect;
    private IEssentiaConduit tube;
    private long gameTime;

    @BeforeEach
    void setUp() {
        level = mock(ServerLevel.class);
        when(level.hasChunkAt(any())).thenReturn(true);
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        aspect = registry.getHolderOrThrow(TTAspects.ORDO);
        tube = mock(IEssentiaConduit.class);
        when(tube.connectsOn(Direction.EAST)).thenReturn(true);
        when(tube.pull(Direction.EAST)).thenReturn(EssentiaPull.NONE);
        when(level.getGameTime()).thenAnswer(invocation -> gameTime);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.west(), Direction.EAST))
                .thenReturn(tube);
    }

    private MockedConstruction<EssentiaSources> interceptSources() {
        return mockConstruction(
                EssentiaSources.class, withSettings().defaultAnswer(RETURNS_SELF), (sources, context) -> {
                    assertEquals(List.of(BlockPos.ZERO, 16), context.arguments());
                });
    }

    private BlockEntityEssentiaPort port(boolean input) {
        var block = input ? TTBlocks.ESSENTIA_INPUT.get() : TTBlocks.ESSENTIA_OUTPUT.get();
        var state = block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
        var port = new BlockEntityEssentiaPort(BlockPos.ZERO, state);
        port.setLevel(level);
        return port;
    }

    private void tick(BlockEntityEssentiaPort port, int count) {
        for (int i = 0; i < count; i++) {
            gameTime++;
            BlockEntityEssentiaPort.serverTick(level, BlockPos.ZERO, port.getBlockState(), port);
        }
    }

    private void offerInput() {
        when(tube.releasesOn(Direction.EAST)).thenReturn(true);
        when(tube.heldAmount(Direction.EAST)).thenReturn(1);
        when(tube.heldAspect(Direction.EAST)).thenReturn(aspect);
        when(tube.extract(aspect, 1, Direction.EAST, false)).thenReturn(1);
    }

    private void requestOutput() {
        when(tube.receivesOn(Direction.EAST)).thenReturn(true);
        when(tube.pull(Direction.EAST)).thenReturn(EssentiaPull.of(aspect, 64));
        when(tube.insert(aspect, 1, Direction.EAST, true)).thenReturn(1);
    }

    @Test
    void inputWithdrawsExactlyOneBeforeRemoteInsertionOnFiveTickCadence() {
        try (var intercepted = interceptSources()) {
            var port = port(true);
            var sources = intercepted.constructed().getFirst();
            verify(sources).facing(Direction.EAST);
            verify(sources).drainEffectTarget(Vec3.atCenterOf(BlockPos.ZERO));
            offerInput();
            when(sources.insert(level, aspect, 10)).thenReturn(true);
            clearInvocations(tube);
            tick(port, 4);
            verifyNoInteractions(tube);
            tick(port, 1);
            var order = inOrder(tube, sources);
            order.verify(tube).extract(aspect, 1, Direction.EAST, false);
            order.verify(sources).insert(level, aspect, 10);
            verify(tube, never()).extract(any(), anyInt(), any(), eq(true));
        }
    }

    @Test
    void failedInputSearchRefundsAndBacksOffFor200Ticks() {
        try (var intercepted = interceptSources();
                var aura = mockStatic(AuraHelper.class)) {
            var port = port(true);
            var sources = intercepted.constructed().getFirst();
            offerInput();
            when(tube.insert(aspect, 1, Direction.EAST, false)).thenReturn(1);
            tick(port, 5);
            var order = inOrder(tube, sources);
            order.verify(tube).extract(aspect, 1, Direction.EAST, false);
            order.verify(sources).insert(level, aspect, 10);
            order.verify(tube).insert(aspect, 1, Direction.EAST, false);
            aura.verifyNoInteractions();
            clearInvocations(tube, sources, level);
            tick(port, 199);
            verifyNoInteractions(tube, sources);
            verify(level, never()).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.west(), Direction.EAST);
            tick(port, 1);
            verify(sources).insert(level, aspect, 10);
        }
    }

    @Test
    void failedInputRefundBecomesOneFluxAndRefusedExtractionDoesNothing() {
        try (var intercepted = interceptSources();
                var aura = mockStatic(AuraHelper.class)) {
            var port = port(true);
            var sources = intercepted.constructed().getFirst();
            offerInput();
            when(tube.extract(aspect, 1, Direction.EAST, false)).thenReturn(0);
            tick(port, 5);
            verify(sources, never()).insert(any(), any(), anyInt());
            when(tube.extract(aspect, 1, Direction.EAST, false)).thenReturn(1);
            tick(port, 5);
            aura.verify(() -> AuraHelper.addFlux(level, BlockPos.ZERO, 1.0F));
        }
    }

    @Test
    void outputSimulatesBeforeDrainingAndRefundsRejectedDelivery() {
        try (var intercepted = interceptSources();
                var aura = mockStatic(AuraHelper.class)) {
            var port = port(false);
            var sources = intercepted.constructed().getFirst();
            when(tube.receivesOn(Direction.EAST)).thenReturn(true);
            when(tube.pull(Direction.EAST)).thenReturn(EssentiaPull.any(64));
            tick(port, 5);
            verify(sources, never()).drain(any(), any(), anyInt());
            requestOutput();
            when(sources.drain(level, aspect, 10)).thenReturn(true);
            when(sources.insert(level, aspect, 10)).thenReturn(true);
            tick(port, 5);
            var order = inOrder(tube, sources);
            order.verify(tube).insert(aspect, 1, Direction.EAST, true);
            order.verify(sources).drain(level, aspect, 10);
            order.verify(tube).insert(aspect, 1, Direction.EAST, false);
            order.verify(sources).insert(level, aspect, 10);
            aura.verifyNoInteractions();
            clearInvocations(sources);
            when(tube.insert(aspect, 1, Direction.EAST, false)).thenReturn(1);
            tick(port, 5);
            verify(sources, never()).insert(any(), any(), anyInt());
        }
    }

    @Test
    void outputFailedRefundPollutesOnceAndFailedSearchBacksOff() {
        try (var intercepted = interceptSources();
                var aura = mockStatic(AuraHelper.class)) {
            var port = port(false);
            var sources = intercepted.constructed().getFirst();
            requestOutput();
            when(sources.drain(level, aspect, 10)).thenReturn(true);
            tick(port, 5);
            aura.verify(() -> AuraHelper.addFlux(level, BlockPos.ZERO, 1.0F));
            when(sources.drain(level, aspect, 10)).thenReturn(false);
            clearInvocations(tube, sources);
            tick(port, 5);
            verify(tube, never()).insert(aspect, 1, Direction.EAST, false);
            clearInvocations(tube, sources);
            tick(port, 199);
            verifyNoInteractions(tube, sources);
            tick(port, 1);
            verify(sources).drain(level, aspect, 10);
        }
    }

    @Test
    void connectionStockPullThresholdAndSimulationGateTransfers() {
        try (var intercepted = interceptSources()) {
            var input = port(true);
            var sources = intercepted.constructed().getFirst();
            offerInput();
            when(tube.connectsOn(Direction.EAST)).thenReturn(false);
            tick(input, 5);
            when(tube.connectsOn(Direction.EAST)).thenReturn(true);
            when(tube.heldAmount(Direction.EAST)).thenReturn(0);
            tick(input, 5);
            when(tube.heldAmount(Direction.EAST)).thenReturn(1);
            when(tube.pull(Direction.EAST)).thenReturn(EssentiaPull.any(128));
            tick(input, 5);
            when(tube.pull(Direction.EAST)).thenReturn(EssentiaPull.NONE);
            when(tube.pullThreshold()).thenReturn(129);
            tick(input, 5);
            verify(tube, never()).extract(any(), anyInt(), any(), anyBoolean());
            verify(sources, never()).insert(any(), any(), anyInt());
            var output = port(false);
            var outputSources = intercepted.constructed().getLast();
            requestOutput();
            when(tube.insert(aspect, 1, Direction.EAST, true)).thenReturn(0);
            tick(output, 5);
            verify(outputSources, never()).drain(any(), any(), anyInt());
        }
    }

    @Test
    void portFaceCapacitySimulationAndServerContractsMatchFinal() {
        try (var intercepted = interceptSources()) {
            var port = port(true);
            var sources = intercepted.constructed().getFirst();
            when(sources.insert(level, aspect, 10)).thenReturn(true);
            assertEquals(128, port.pull(Direction.WEST).strength());
            assertEquals(EssentiaPull.NONE, port.pull(Direction.EAST));
            assertEquals(EssentiaPull.NONE, port.pull(null));
            assertEquals(0, port.insert(aspect, 2, Direction.WEST, true));
            assertEquals(0, port.insert(aspect, 0, Direction.WEST, true));
            assertEquals(0, port.insert(aspect, 1, Direction.EAST, true));
            assertEquals(1, port.insert(aspect, 1, Direction.WEST, true));
            verify(sources, never()).insert(any(), any(), anyInt());
            assertEquals(1, port.insert(aspect, 1, Direction.WEST, false));
            assertEquals(1, port.roomFor(aspect, Direction.WEST));
            assertEquals(0, port.roomFor(aspect, Direction.EAST));
            assertEquals(0, port.heldAmount(null));
            assertNull(port.heldAspect(null));
            assertEquals(0, port.extract(aspect, 1, Direction.WEST, false));
            assertEquals(0, port.pullThreshold());
            for (Direction side : Direction.values()) {
                assertEquals(side == Direction.WEST, port.connectsOn(side));
                assertEquals(side == Direction.WEST, port.receivesOn(side));
                assertFalse(port.releasesOn(side));
            }
            var output = port(false);
            assertTrue(output.releasesOn(Direction.WEST));
            assertFalse(output.receivesOn(Direction.WEST));
            assertEquals(0, output.roomFor(aspect, Direction.WEST));
            assertEquals(0, output.insert(aspect, 1, Direction.WEST, true));
            var client = mock(Level.class);
            port.setLevel(client);
            assertEquals(1, port.insert(aspect, 1, Direction.WEST, true));
            assertEquals(0, port.insert(aspect, 1, Direction.WEST, false));
            for (int i = 0; i < 5; i++)
                BlockEntityEssentiaPort.serverTick(client, BlockPos.ZERO, port.getBlockState(), port);
            verifyNoInteractions(client);
            verify(sources, times(1)).insert(any(), any(), anyInt());
        }
    }

    @Test
    void unloadedTubeDoesNotTriggerCapabilityOrRemoteSourceQueries() {
        try (var intercepted = interceptSources()) {
            var port = port(true);
            var sources = intercepted.constructed().getFirst();
            clearInvocations(tube, sources);
            when(level.hasChunkAt(BlockPos.ZERO.west())).thenReturn(false);
            tick(port, 5);
            verifyNoInteractions(tube, sources);
            verify(level, never()).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.west(), Direction.EAST);
        }
    }

    private UseOnContext context(Level level, Player player) {
        var context = mock(UseOnContext.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.getLevel()).thenReturn(level);
        when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
        when(context.getClickedFace()).thenReturn(Direction.UP);
        when(context.getClickLocation()).thenReturn(Vec3.atCenterOf(BlockPos.ZERO));
        when(context.getHand()).thenReturn(InteractionHand.MAIN_HAND);
        return context;
    }

    @Test
    void resonatorKeepsTranslatedContentsAndSuctionMessages() {
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO, Direction.UP))
                .thenReturn(tube);
        when(tube.heldAspect(Direction.UP)).thenReturn(aspect);
        when(tube.heldAmount(Direction.UP)).thenReturn(7);
        when(tube.pull(Direction.UP)).thenReturn(EssentiaPull.any(64));
        var player = mock(Player.class);
        var resonator = TTItems.RESONATOR.get();
        assertEquals(InteractionResult.SUCCESS, resonator.onItemUseFirst(ItemStack.EMPTY, context(level, player)));
        verify(level).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO, Direction.UP);
        var messages = ArgumentCaptor.forClass(Component.class);
        verify(player, times(2)).sendSystemMessage(messages.capture());
        var contents = (TranslatableContents) messages.getAllValues().getFirst().getContents();
        var suction = (TranslatableContents) messages.getAllValues().getLast().getContents();
        assertEquals("message.thaumaturge.resonator.contents", contents.getKey());
        assertEquals("7", contents.getArgs()[0]);
        assertEquals("message.thaumaturge.resonator.suction", suction.getKey());
        assertEquals("64", suction.getArgs()[0]);
        assertEquals(
                "message.thaumaturge.resonator.untyped",
                ((TranslatableContents) ((Component) suction.getArgs()[1]).getContents()).getKey());
    }

    @Test
    void resonatorReportsEachBufferCompartmentAndDoesNotDuplicateAggregateStock() {
        when(tube.pull(Direction.UP)).thenReturn(EssentiaPull.NONE);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO, Direction.UP))
                .thenReturn(tube);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(mock(BlockEntityTubeBuffer.class));
        var container = mock(IAspectContainer.class);
        when(container.getAspects()).thenReturn(AspectList.EMPTY.add(aspect, 4));
        when(level.getCapability(AspectCapabilities.CONTAINER, BlockPos.ZERO, Direction.UP))
                .thenReturn(container);
        var player = mock(Player.class);
        TTItems.RESONATOR.get().onItemUseFirst(ItemStack.EMPTY, context(level, player));
        verify(player, times(2)).sendSystemMessage(any());
        verify(tube, never()).heldAmount(any());
    }

    @Test
    void resonatorClientOnlySwingsAndMissingTargetsFailWithoutChat() {
        var player = mock(Player.class);
        var resonator = TTItems.RESONATOR.get();
        assertEquals(InteractionResult.FAIL, resonator.onItemUseFirst(ItemStack.EMPTY, context(level, player)));
        verifyNoInteractions(player);
        var client = mock(Level.class);
        when(client.isClientSide()).thenReturn(true);
        when(client.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO, Direction.UP))
                .thenReturn(tube);
        assertEquals(InteractionResult.SUCCESS, resonator.onItemUseFirst(ItemStack.EMPTY, context(client, player)));
        verify(client).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO, Direction.UP);
        verify(player).swing(InteractionHand.MAIN_HAND);
        verify(player, never()).sendSystemMessage(any());
        verifyNoInteractions(tube);
    }
}
