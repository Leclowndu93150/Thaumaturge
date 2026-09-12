package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.content.taint.block.ITaintBlock;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityEtherealBloom extends BlockEntity {
    private static final int WORK_INTERVAL = 20;
    private static final int SAMPLES_PER_WORK = 4;
    private static final int VERTICAL_SAMPLE_RANGE = 4;
    private static final int IDLE_WORK_LIMIT = 300;
    private static final int SLEEP_RECHECK_INTERVAL = 1200;
    private static final float PRESSURE_CLEAN_AMOUNT = 0.005F;

    private int ticks;
    private int idleWork;
    private boolean sleeping;

    public BlockEntityEtherealBloom(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ETHEREAL_BLOOM.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEtherealBloom bloom) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        bloom.ticks++;
        if (bloom.sleeping) {
            if (bloom.ticks % SLEEP_RECHECK_INTERVAL != 0 || !bloom.hasNearbyEcology(server, pos)) {
                return;
            }
            bloom.sleeping = false;
            bloom.idleWork = 0;
        }
        if (bloom.ticks % WORK_INTERVAL != 0) {
            return;
        }

        boolean worked = bloom.cleanEcology(server, pos);
        worked |= bloom.cleanSampledBlocks(server, pos, server.getRandom());
        if (worked) {
            bloom.idleWork = 0;
        } else if (++bloom.idleWork >= IDLE_WORK_LIMIT) {
            bloom.sleeping = true;
            bloom.setChanged();
        }
    }

    private boolean cleanEcology(ServerLevel level, BlockPos origin) {
        boolean worked = false;
        int radius = TaintBloomRegistry.PROTECTION_RADIUS;
        int minChunkX = (origin.getX() - radius) >> 4;
        int maxChunkX = (origin.getX() + radius) >> 4;
        int minChunkZ = (origin.getZ() - radius) >> 4;
        int maxChunkZ = (origin.getZ() + radius) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                BlockPos sample = new BlockPos((chunkX << 4) + 8, origin.getY(), (chunkZ << 4) + 8);
                if (!level.hasChunkAt(sample)) {
                    continue;
                }
                float before = TaintEcology.getSaturation(level, sample);
                if (before > 0.0F) {
                    TaintEcology.clean(level, sample, PRESSURE_CLEAN_AMOUNT);
                    worked = true;
                }
            }
        }
        return worked;
    }

    private boolean hasNearbyEcology(ServerLevel level, BlockPos origin) {
        int radius = TaintBloomRegistry.PROTECTION_RADIUS;
        int minChunkX = (origin.getX() - radius) >> 4;
        int maxChunkX = (origin.getX() + radius) >> 4;
        int minChunkZ = (origin.getZ() - radius) >> 4;
        int maxChunkZ = (origin.getZ() + radius) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                BlockPos sample = new BlockPos((chunkX << 4) + 8, origin.getY(), (chunkZ << 4) + 8);
                if (level.hasChunkAt(sample)
                        && (TaintEcology.getSaturation(level, sample) > 0.0F
                                || TaintBiomeManager.isTainted(level, sample))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean cleanSampledBlocks(ServerLevel level, BlockPos origin, RandomSource random) {
        boolean worked = false;
        int radius = TaintBloomRegistry.PROTECTION_RADIUS;
        for (int sample = 0; sample < SAMPLES_PER_WORK; sample++) {
            int x = random.nextIntBetweenInclusive(-radius, radius);
            int z = random.nextIntBetweenInclusive(-radius, radius);
            if (x * x + z * z > radius * radius) {
                continue;
            }

            BlockPos column = origin.offset(x, 0, z);
            if (!level.hasChunkAt(column)) {
                continue;
            }
            // TC5's Ethereal Bloom reset the biome as part of the same cleansing operation. The
            // modern equivalent restores this quart column from the active generator biome source.
            worked |= TaintBiomeManager.restoreColumn(level, column);

            for (int y = VERTICAL_SAMPLE_RANGE; y >= -VERTICAL_SAMPLE_RANGE; y--) {
                BlockPos target = origin.offset(x, y, z);
                if (target.distSqr(origin) > radius * radius) {
                    continue;
                }
                BlockState targetState = level.getBlockState(target);
                if (targetState.getBlock() instanceof ITaintBlock taintBlock) {
                    taintBlock.die(level, target, targetState);
                    worked = true;
                    break;
                }
            }
        }
        return worked;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            TaintBloomRegistry.add(server, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            TaintBloomRegistry.remove(server, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ticks", ticks);
        tag.putInt("idle_work", idleWork);
        tag.putBoolean("sleeping", sleeping);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ticks = tag.getInt("ticks");
        idleWork = tag.getInt("idle_work");
        sleeping = tag.getBoolean("sleeping");
    }
}
