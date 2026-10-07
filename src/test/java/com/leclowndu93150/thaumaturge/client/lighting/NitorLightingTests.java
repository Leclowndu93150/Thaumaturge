package com.leclowndu93150.thaumaturge.client.lighting;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class NitorLightingTests {
    private static final BlockPos SOURCE = new BlockPos(0, 8, 0);

    private NitorLightingTests() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        if (!Boolean.getBoolean("thaumaturge.testNitorLighting")) {
            return;
        }
        try {
            BlockLightEngine engine = createEngine(false, false);
            update(engine, Map.of(SOURCE.asLong(), 15));
            expect(engine, SOURCE, 15);
            expect(engine, SOURCE.east(), 14);
            expect(engine, SOURCE.east(14), 1);
            expect(engine, SOURCE.east(15), 0);

            BlockPos moved = SOURCE.west(4);
            update(engine, Map.of(moved.asLong(), 15));
            expect(engine, moved, 15);
            expect(engine, SOURCE, 11);
            update(engine, Map.of());
            expect(engine, moved, 0);
            expect(engine, SOURCE, 0);

            update(engine, Map.of(SOURCE.asLong(), 15, SOURCE.east().asLong(), 15));
            update(engine, Map.of(SOURCE.east().asLong(), 15));
            expect(engine, SOURCE.east(), 15);
            expect(engine, SOURCE, 14);
            update(engine, Map.of());
            expect(engine, SOURCE, 0);

            engine = createEngine(true, false);
            update(engine, Map.of(SOURCE.asLong(), 15));
            expect(engine, SOURCE.east(), 14);
            expect(engine, SOURCE.east(3), 0);

            engine = createEngine(false, false);
            BlockLightEngine other = createEngine(false, false);
            update(engine, Map.of(SOURCE.asLong(), 15));
            other.checkBlock(SOURCE);
            other.runLightUpdates();
            expect(engine, SOURCE, 15);
            expect(other, SOURCE, 0);
            NitorDynamicLights.clear();
            engine.runLightUpdates();
            expect(engine, SOURCE, 0);

            engine = createEngine(false, true);
            update(engine, Map.of(SOURCE.asLong(), 15));
            expect(engine, SOURCE, 15);
            update(engine, Map.of());
            expect(engine, SOURCE, 14);
            expect(engine, SOURCE.east(), 13);
            Thaumaturge.LOGGER.info("Nitor lighting tests passed: propagation, movement, removal, overlap, occlusion, engine isolation, reset and existing light");
        } finally {
            NitorDynamicLights.clear();
        }
    }

    private static void update(BlockLightEngine engine, Map<Long, Integer> sources) {
        NitorDynamicLights.updateSources(engine, sources);
        engine.runLightUpdates();
    }

    private static BlockLightEngine createEngine(boolean wall, boolean torch) {
        BlockLightEngine engine = new BlockLightEngine(new TestWorld(wall, torch));
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y < 3; y++) {
                    engine.updateSectionStatus(SectionPos.of(x, y, z), false);
                }
                engine.setLightEnabled(new ChunkPos(x, z), true);
            }
        }
        engine.runLightUpdates();
        return engine;
    }

    private static void expect(BlockLightEngine engine, BlockPos pos, int expected) {
        int actual = engine.getLightValue(pos);
        if (actual != expected) {
            throw new AssertionError("Expected light " + expected + " at " + pos + ", got " + actual);
        }
    }

    private record TestWorld(boolean wall, boolean torch) implements LightChunkGetter, LightChunk {
        @Override
        public LightChunk getChunkForLighting(int x, int z) {
            return this;
        }

        @Override
        public BlockGetter getLevel() {
            return this;
        }

        @Override
        public void findBlockLightSources(BiConsumer<BlockPos, BlockState> consumer) {}

        @Override
        public ChunkSkyLightSources getSkyLightSources() {
            return new ChunkSkyLightSources(this);
        }

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (wall && pos.getX() == 2) {
                return Blocks.STONE.defaultBlockState();
            }
            return torch && pos.equals(SOURCE) ? Blocks.TORCH.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            return 48;
        }

        @Override
        public int getMinY() {
            return 0;
        }
    }
}
