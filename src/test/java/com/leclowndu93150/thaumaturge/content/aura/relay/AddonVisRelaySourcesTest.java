package com.leclowndu93150.thaumaturge.content.aura.relay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.aura.VisRelaySourceContext;
import com.leclowndu93150.thaumaturge.api.aura.VisRelaySourceRegistration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class AddonVisRelaySourcesTest {
    private static final ResourceKey<IAspect> AER = aspect("aer");
    private static final ResourceKey<IAspect> IGNIS = aspect("ignis");

    @Test
    void simulationDoesNotMutateAndCommitRunsOnce() {
        Fixture fixture = fixture(BlockPos.ZERO);
        Source source = new Source(Map.of(AER, 40));
        try (VisRelaySourceRegistration ignored = fixture.register(source)) {
            assertEquals(25, fixture.drain(AER, 25, true));
            assertEquals(40, source.stored.get(AER));
            assertEquals(0, source.commits);
            assertEquals(25, fixture.drain(AER, 25, false));
            assertEquals(15, source.stored.get(AER));
            assertEquals(1, source.commits);
        }
    }

    @Test
    void clampsResponsesAndKeepsAspectsIsolated() {
        Fixture fixture = fixture(BlockPos.ZERO);
        Source source = new Source(Map.of(AER, 100, IGNIS, 7));
        source.overquote = true;
        try (VisRelaySourceRegistration ignored = fixture.register(source)) {
            assertEquals(10, fixture.drain(AER, 10, true));
            assertEquals(20, fixture.drain(IGNIS, 20, false));
            assertEquals(100, source.stored.get(AER));
            assertEquals(0, source.stored.get(IGNIS));
        }
    }

    @Test
    void hostsInvalidateAndUnloadIndependently() {
        Fixture first = fixture(BlockPos.ZERO);
        Fixture second = new Fixture(first.level, new BlockPos(4, 0, 0));
        when(first.level.isLoaded(second.position)).thenReturn(true);
        Source one = new Source(Map.of(AER, 10));
        Source two = new Source(Map.of(AER, 20));
        VisRelaySourceRegistration firstRegistration = first.register(one);
        try (firstRegistration;
                VisRelaySourceRegistration ignored = second.register(two)) {
            firstRegistration.invalidate();
            assertEquals(0, first.drain(AER, 10, false));
            assertEquals(10, second.drain(AER, 10, false));
            when(first.level.isLoaded(second.position)).thenReturn(false);
            assertEquals(0, second.drain(AER, 10, false));
        }
    }

    @Test
    void recursiveHostRequestIsRejected() {
        Fixture fixture = fixture(BlockPos.ZERO);
        Source source = new Source(Map.of(AER, 10));
        source.onCommit = () -> source.nestedResult = fixture.drain(AER, 1, false);
        try (VisRelaySourceRegistration ignored = fixture.register(source)) {
            assertEquals(5, fixture.drain(AER, 5, false));
            assertEquals(0, source.nestedResult);
            assertEquals(1, source.commits);
        }
    }

    private static Fixture fixture(BlockPos position) {
        MinecraftServer server = mock(MinecraftServer.class);
        when(server.isSameThread()).thenReturn(true);
        ServerLevel level = mock(ServerLevel.class);
        when(level.getServer()).thenReturn(server);
        when(level.isLoaded(position)).thenReturn(true);
        return new Fixture(level, position);
    }

    private static ResourceKey<IAspect> aspect(String path) {
        return ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("test", path));
    }

    private record Fixture(ServerLevel level, BlockPos position) {
        VisRelaySourceRegistration register(Source source) {
            return AddonVisRelaySources.INSTANCE.register(
                    new VisRelaySourceContext(level, UUID.randomUUID(), position), source);
        }

        int drain(ResourceKey<IAspect> aspect, int amount, boolean simulate) {
            return AddonVisRelaySources.INSTANCE.drain(level, position, aspect, amount, simulate);
        }
    }

    private static final class Source implements IVisRelaySource {
        private final Map<ResourceKey<IAspect>, Integer> stored = new HashMap<>();
        private boolean overquote;
        private int commits;
        private int nestedResult;
        private Runnable onCommit = () -> {};

        private Source(Map<ResourceKey<IAspect>, Integer> stored) {
            this.stored.putAll(stored);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isLinked() {
            return true;
        }

        @Override
        public Reservation reserve(ResourceKey<IAspect> primal, int requested) {
            int available = stored.getOrDefault(primal, 0);
            if (available <= 0) return null;
            int reserved = Math.min(available, requested);
            return new Reservation() {
                private boolean committed;

                @Override
                public int amount() {
                    return overquote ? requested + 100 : reserved;
                }

                @Override
                public int commit() {
                    if (committed) throw new IllegalStateException("double commit");
                    committed = true;
                    commits++;
                    stored.put(primal, available - reserved);
                    onCommit.run();
                    return overquote ? requested + 100 : reserved;
                }

                @Override
                public void close() {}
            };
        }
    }
}
