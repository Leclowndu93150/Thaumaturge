package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityEtherealBloom extends BlockEntity {
    private static final int WORK_INTERVAL = 20;
    private static final int SAMPLES_PER_WORK = 4;
    private static final int VERTICAL_SAMPLE_RANGE = 4;
    private static final int IDLE_WORK_LIMIT = 300;
    private static final int SLEEP_RECHECK_INTERVAL = 1200;
    private static final float PRESSURE_CLEAN_AMOUNT = 0.005F;
    private static final int CHUNK_CENTER = 8;
    private static final int RADIUS_SQ = TaintBlooms.PROTECTION_RADIUS * TaintBlooms.PROTECTION_RADIUS;

    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private int ticks;
    private int idleWork;
    private boolean sleeping;

    public BlockEntityEtherealBloom(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ETHEREAL_BLOOM.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        ticks++;
        if (sleeping) {
            if (ticks % SLEEP_RECHECK_INTERVAL != 0 || !hasNearbyEcology(server, pos)) {
                return;
            }
            sleeping = false;
            idleWork = 0;
            setChanged();
        }
        if (ticks % WORK_INTERVAL != 0) {
            return;
        }
        boolean worked = cleanEcology(server, pos);
        worked |= cleanSampledBlocks(server, pos, server.getRandom());
        if (worked) {
            idleWork = 0;
        } else if (++idleWork >= IDLE_WORK_LIMIT) {
            sleeping = true;
            setChanged();
        }
    }

    private boolean cleanEcology(ServerLevel level, BlockPos origin) {
        boolean worked = false;
        int radius = TaintBlooms.PROTECTION_RADIUS;
        for (int chunkX = SectionPos.blockToSectionCoord(origin.getX() - radius); chunkX <= SectionPos.blockToSectionCoord(origin.getX() + radius); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(origin.getZ() - radius); chunkZ <= SectionPos.blockToSectionCoord(origin.getZ() + radius); chunkZ++) {
                cursor.set(SectionPos.sectionToBlockCoord(chunkX, CHUNK_CENTER), origin.getY(), SectionPos.sectionToBlockCoord(chunkZ, CHUNK_CENTER));
                if (level.hasChunkAt(cursor) && TaintEcology.getSaturation(level, cursor) > 0.0F) {
                    TaintEcology.clean(level, cursor, PRESSURE_CLEAN_AMOUNT);
                    worked = true;
                }
            }
        }
        return worked;
    }

    private boolean hasNearbyEcology(ServerLevel level, BlockPos origin) {
        int radius = TaintBlooms.PROTECTION_RADIUS;
        for (int chunkX = SectionPos.blockToSectionCoord(origin.getX() - radius); chunkX <= SectionPos.blockToSectionCoord(origin.getX() + radius); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(origin.getZ() - radius); chunkZ <= SectionPos.blockToSectionCoord(origin.getZ() + radius); chunkZ++) {
                cursor.set(SectionPos.sectionToBlockCoord(chunkX, CHUNK_CENTER), origin.getY(), SectionPos.sectionToBlockCoord(chunkZ, CHUNK_CENTER));
                if (level.hasChunkAt(cursor) && (TaintEcology.getSaturation(level, cursor) > 0.0F || TaintBiomeManager.isTainted(level, cursor))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean cleanSampledBlocks(ServerLevel level, BlockPos origin, RandomSource random) {
        boolean worked = false;
        int radius = TaintBlooms.PROTECTION_RADIUS;
        for (int sample = 0; sample < SAMPLES_PER_WORK; sample++) {
            int x = random.nextIntBetweenInclusive(-radius, radius);
            int z = random.nextIntBetweenInclusive(-radius, radius);
            if (x * x + z * z > RADIUS_SQ) {
                continue;
            }
            cursor.set(origin.getX() + x, origin.getY(), origin.getZ() + z);
            if (!level.hasChunkAt(cursor)) {
                continue;
            }
            worked |= TaintBiomeManager.restoreColumn(level, cursor);
            for (int y = VERTICAL_SAMPLE_RANGE; y >= -VERTICAL_SAMPLE_RANGE; y--) {
                if (x * x + y * y + z * z > RADIUS_SQ) {
                    continue;
                }
                cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                BlockState state = level.getBlockState(cursor);
                if (state.getBlock() instanceof ITaintBlock taintBlock) {
                    taintBlock.decay(level, cursor.immutable(), state);
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
            TaintBlooms.register(server, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            TaintBlooms.unregister(server, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ticks = input.getIntOr("Ticks", 0);
        idleWork = input.getIntOr("IdleWork", 0);
        sleeping = input.getBooleanOr("Sleeping", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Ticks", ticks);
        output.putInt("IdleWork", idleWork);
        output.putBoolean("Sleeping", sleeping);
    }
}
