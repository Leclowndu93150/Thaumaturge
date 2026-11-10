package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

class EssentiaSourcesCutoffTest {
    private ServerLevel level;
    private Holder<IAspect> aspect;
    private Holder<IAspect> other;
    private final Map<BlockPos, IAspectSource> sources = new HashMap<>();
    private final Set<ChunkPos> unloaded = new HashSet<>();
    private MockedStatic<EffectDispatch> effects;
    private long time;

    @BeforeEach
    void setUp() {
        level = mock(ServerLevel.class);
        aspect = Holder.direct(mock(IAspect.class));
        other = Holder.direct(mock(IAspect.class));
        when(aspect.value().color()).thenReturn(0x123456);
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
        when(level.getGameTime()).thenAnswer(call -> time);
        when(level.hasChunk(anyInt(), anyInt()))
                .thenAnswer(call -> !unloaded.contains(new ChunkPos(call.getArgument(0), call.getArgument(1))));
        when(level.getCapability(eq(AspectCapabilities.CONTAINER), any(BlockPos.class), isNull()))
                .thenAnswer(call -> sources.get(call.getArgument(1)));
        effects = mockStatic(EffectDispatch.class);
    }

    @AfterEach
    void tearDown() {
        effects.close();
    }

    private IAspectSource source(BlockPos pos, int priority, boolean empty, int suction) {
        IAspectSource source = mock(IAspectSource.class);
        when(source.drainEffectOffset()).thenReturn(Vec3.ZERO);
        when(source.getSourcePriority()).thenReturn(priority);
        when(source.getAspects()).thenReturn(empty ? AspectList.EMPTY : AspectList.EMPTY.add(aspect, 1));
        when(source.drain(aspect, 1)).thenReturn(true);
        when(source.accepts(aspect)).thenReturn(true);
        sources.put(pos, source);
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        when(conduit.pull(null)).thenReturn(EssentiaPull.of(aspect, suction));
        when(level.getCapability(EssentiaCapabilities.CONDUIT, pos, null)).thenReturn(conduit);
        return source;
    }

    @Test
    void drainUsesPriorityThenDistanceAndPublishesOneUnitAndDefaultStream() {
        var near = source(BlockPos.ZERO.east(), 0, false, 100);
        var far = source(BlockPos.ZERO.east(2), 1, false, 0);
        var farther = source(BlockPos.ZERO.east(3), 1, false, 0);
        var entity = mock(BlockEntity.class);
        when(level.getBlockEntity(BlockPos.ZERO.east(2))).thenReturn(entity);
        assertTrue(new EssentiaSources(BlockPos.ZERO, 3).drain(level, aspect, 20));
        verify(far).drain(aspect, 1);
        verify(near, never()).drain(any(), anyInt());
        verify(farther, never()).drain(any(), anyInt());
        verify(entity).setChanged();
        effects.verify(() -> EffectDispatch.spawnEssentiaStream(
                level,
                Vec3.atCenterOf(BlockPos.ZERO.east(2)),
                Vec3.atCenterOf(BlockPos.ZERO.below()),
                0x123456,
                0,
                0,
                0.1F,
                20,
                0.0));
        effects.verifyNoMoreInteractions();
    }

    @Test
    void drainUsesSourceVisualOffsetWithoutChangingItsBlockPosition() {
        var source = source(BlockPos.ZERO.east(), 0, false, 0);
        when(source.drainEffectOffset()).thenReturn(new Vec3(0, 5.0 / 16.0, 0));

        assertTrue(new EssentiaSources(BlockPos.ZERO, 2).drain(level, aspect, 10));

        verify(source).drain(aspect, 1);
        effects.verify(() -> EffectDispatch.spawnEssentiaStream(
                level,
                Vec3.atCenterOf(BlockPos.ZERO.east()).add(0, 5.0 / 16.0, 0),
                Vec3.atCenterOf(BlockPos.ZERO.below()),
                0x123456,
                0,
                0,
                0.1F,
                10,
                0.0));
    }

    @Test
    void insertionOrdersPriorityThenSuctionThenFilledThenDistance() {
        var low = source(BlockPos.ZERO.east(), 0, false, 200);
        var weak = source(BlockPos.ZERO.west(), 1, false, 5);
        var empty = source(BlockPos.ZERO.north(), 1, true, 10);
        var filled = source(BlockPos.ZERO.south(), 1, false, 10);
        var farFilled = source(BlockPos.ZERO.south(2), 1, false, 10);
        var search = new EssentiaSources(BlockPos.ZERO, 2);
        assertTrue(search.insert(level, aspect, 10));
        verify(filled).fill(aspect, 1);
        for (var source : new IAspectSource[] {low, weak, empty, farFilled})
            verify(source, never()).fill(any(), anyInt());
        effects.verify(() -> EffectDispatch.spawnEssentiaStream(
                level,
                Vec3.atCenterOf(BlockPos.ZERO),
                Vec3.atCenterOf(BlockPos.ZERO.south()),
                0x123456,
                0,
                0,
                0.1F,
                10,
                0.0));
    }

    @Test
    void nonmatchingPullDoesNotContributeSuction() {
        var first = source(BlockPos.ZERO.west(), 0, false, 0);
        var wrong = source(BlockPos.ZERO.east(), 0, false, 100);
        var conduit = mock(IEssentiaConduit.class);
        when(conduit.pull(null)).thenReturn(EssentiaPull.of(other, 100));
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.east(), null))
                .thenReturn(conduit);
        assertTrue(new EssentiaSources(BlockPos.ZERO, 1).insert(level, aspect, 10));
        verify(first).fill(aspect, 1);
        verify(wrong, never()).fill(any(), anyInt());
    }

    @Test
    void blockedOrRejectingSourcesDoNotReceiveAndFailedFillFallsThrough() {
        var blocked = source(BlockPos.ZERO.north(), 4, false, 0);
        when(blocked.isBlocked()).thenReturn(true);
        var rejects = source(BlockPos.ZERO.south(), 3, false, 0);
        when(rejects.accepts(aspect)).thenReturn(false);
        var fails = source(BlockPos.ZERO.west(), 2, false, 0);
        when(fails.fill(aspect, 1)).thenReturn(1);
        var succeeds = source(BlockPos.ZERO.east(), 1, false, 0);
        assertTrue(new EssentiaSources(BlockPos.ZERO, 1).insert(level, aspect, 10));
        verify(blocked, never()).fill(any(), anyInt());
        verify(rejects, never()).fill(any(), anyInt());
        var order = inOrder(fails, succeeds);
        order.verify(fails).fill(aspect, 1);
        order.verify(succeeds).fill(aspect, 1);
        effects.verify(() -> EffectDispatch.spawnEssentiaStream(
                level,
                Vec3.atCenterOf(BlockPos.ZERO),
                Vec3.atCenterOf(BlockPos.ZERO.east()),
                0x123456,
                0,
                0,
                0.1F,
                10,
                0.0));
        effects.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void directionalScanIncludesOwnPlaneAndRangeMinusOneButNeverBackwards(Direction face) {
        var ownPlanePos = BlockPos.ZERO.relative(face.getAxis() == Direction.Axis.Y ? Direction.EAST : Direction.UP);
        var ownPlane = source(ownPlanePos, 1, false, 0);
        var forward = source(BlockPos.ZERO.relative(face), 0, false, 0);
        var tooFar = source(BlockPos.ZERO.relative(face, 2), 2, false, 0);
        var backwards = source(BlockPos.ZERO.relative(face.getOpposite()), 2, false, 0);
        var owner = source(BlockPos.ZERO, 10, false, 0);
        assertTrue(new EssentiaSources(BlockPos.ZERO, 2).facing(face).drain(level, aspect, 10));
        verify(ownPlane).drain(aspect, 1);
        verify(forward, never()).drain(any(), anyInt());
        verify(tooFar, never()).getSourcePriority();
        verify(backwards, never()).getSourcePriority();
        verify(owner, never()).getSourcePriority();
    }

    @Test
    void cachedCandidatesRefreshPriorityAndSkipUnloadedPositionsBeforeQuerying() {
        var west = source(BlockPos.ZERO.west(), 1, false, 0);
        var east = source(BlockPos.ZERO.east(), 0, false, 0);
        var search = new EssentiaSources(BlockPos.ZERO, 1);
        assertTrue(search.drain(level, aspect, 10));
        when(east.getSourcePriority()).thenReturn(2);
        assertTrue(search.drain(level, aspect, 10));
        verify(east).drain(aspect, 1);
        unloaded.add(new ChunkPos(BlockPos.ZERO.east()));
        clearInvocations(level, west, east);
        assertTrue(search.drain(level, aspect, 10));
        verify(west).drain(aspect, 1);
        verifyNoInteractions(east);
        verify(level, never()).getCapability(AspectCapabilities.CONTAINER, BlockPos.ZERO.east(), null);
    }

    @Test
    void failedSearchBacksOffExactly200TicksAndInvalidationClearsDelay() {
        var search = new EssentiaSources(BlockPos.ZERO, 1);
        time = 10;
        assertFalse(search.drain(level, aspect, 10));
        var found = source(BlockPos.ZERO.east(), 0, false, 0);
        clearInvocations(level);
        time = 209;
        assertFalse(search.drain(level, aspect, 10));
        verify(level, never()).getCapability(eq(AspectCapabilities.CONTAINER), any(), isNull());
        time = 210;
        assertTrue(search.drain(level, aspect, 10));
        when(found.drain(aspect, 1)).thenReturn(false);
        assertFalse(search.drain(level, aspect, 10));
        when(found.drain(aspect, 1)).thenReturn(true);
        search.invalidate();
        assertTrue(search.drain(level, aspect, 10));
    }

    @Test
    void ignoredBlockEntitiesAndUnloadedSourcesAreExcludedFromDiscovery() {
        var ignored = source(BlockPos.ZERO.west(), 2, false, 0);
        var unloadedSource = source(BlockPos.ZERO.north(), 2, false, 0);
        var available = source(BlockPos.ZERO.east(), 0, false, 0);
        var entity = mock(BlockEntity.class);
        when(level.getBlockEntity(BlockPos.ZERO.west())).thenReturn(entity);
        unloaded.add(new ChunkPos(BlockPos.ZERO.north()));
        assertTrue(new EssentiaSources(BlockPos.ZERO, 1)
                .ignoring(be -> be == entity)
                .drain(level, aspect, 10));
        verify(available).drain(aspect, 1);
        verify(ignored, never()).getSourcePriority();
        verify(unloadedSource, never()).getSourcePriority();
    }

    @Test
    void customStreamTargetAndCacheLifecycleMatchCutoff() {
        var west = source(BlockPos.ZERO.west(), 1, false, 0);
        var east = source(BlockPos.ZERO.east(), 0, false, 0);
        Vec3 target = new Vec3(2.5, 4, 3);
        var search = new EssentiaSources(BlockPos.ZERO, 2).drainEffectTarget(target);
        assertTrue(search.drain(level, aspect, 5));
        search.facing(Direction.EAST).ignoring(be -> true);
        assertTrue(search.drain(level, aspect, 5));
        verify(west, times(2)).drain(aspect, 1);
        search.invalidate();
        assertTrue(search.drain(level, aspect, 5));
        verify(east).drain(aspect, 1);
        effects.verify(
                () -> EffectDispatch.spawnEssentiaStream(
                        level, Vec3.atCenterOf(BlockPos.ZERO.west()), target, 0x123456, 0, 0, 0.1F, 5, 0.0),
                times(2));
    }
}
