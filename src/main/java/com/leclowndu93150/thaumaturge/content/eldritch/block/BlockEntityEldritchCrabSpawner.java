package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchCrab;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockEntityEldritchCrabSpawner extends BlockEntity {
    private static final int VENT_EVENT = 1;
    private static final int ACTIVATION_RANGE = 16;
    private static final int MAX_CRABS = 5;
    private static final int CRAB_SCAN_RANGE = 16;
    private static final int VENT_COLOR = 0x9988AA;
    private static final float VENT_SPEED = 0.25F;
    private static final float PLATE_THICKNESS = 0.25F;
    private static final int VENT_WARNING_TICKS = 15;
    private static final int RETRY_DELAY_BASE = 60;
    private static final int RETRY_DELAY_ROLL = 101;
    private static final int SPAWN_DELAY_ROLL = 61;
    private static final int INITIAL_DELAY_ROLL = 201;
    private static final int HELM_ROLL = 100;
    private static final int CHAMPION_ROLL = 1000;

    private int ticks = -1;
    private int venting = -1;

    public BlockEntityEldritchCrabSpawner(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_CRAB_SPAWNER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            if (ticks < 0) {
                ticks = level.getRandom().nextInt(INITIAL_DELAY_ROLL);
            }
            ticks--;
            boolean canSpawn = canSpawnCrab(level, pos);
            if (ticks == VENT_WARNING_TICKS && canSpawn) {
                level.blockEvent(pos, state.getBlock(), VENT_EVENT, 0);
                level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.1F);
            } else if (ticks == 0) {
                if (canSpawn) {
                    ticks = baseSpawnDelay(level) + level.getRandom().nextInt(SPAWN_DELAY_ROLL);
                    spawnCrab(level, pos, state);
                } else {
                    ticks = RETRY_DELAY_BASE + level.getRandom().nextInt(RETRY_DELAY_ROLL);
                }
            } else if (ticks % 5 == 0 && level.getRandom().nextInt(20) == 0) {
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.5F, 0.65F);
            }
        } else if (venting > 0) {
            venting--;
            int bursts = 2 + level.getRandom().nextInt(4);
            for (int a = 0; a < bursts; a++) {
                drawVent(level, pos, state);
            }
        } else if (level.getRandom().nextInt(20) == 0) {
            drawVent(level, pos, state);
        }
    }

    private static int baseSpawnDelay(Level level) {
        return switch (level.getDifficulty()) {
            case HARD -> 120;
            case NORMAL -> 150;
            default -> 200;
        };
    }

    private void drawVent(Level level, BlockPos pos, BlockState state) {
        Direction dir = state.getValue(BlockEldritchCrabSpawner.FACING);
        RandomSource rand = level.getRandom();
        float surface = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? PLATE_THICKNESS : 1.0F - PLATE_THICKNESS;
        double x = dir.getAxis() == Direction.Axis.X ? surface + (rand.nextFloat() - rand.nextFloat()) / 4.0 : rand.nextFloat() / 2.0 + 0.25;
        double y = dir.getAxis() == Direction.Axis.Y ? surface + (rand.nextFloat() - rand.nextFloat()) / 4.0 : rand.nextFloat() / 2.0 + 0.25;
        double z = dir.getAxis() == Direction.Axis.Z ? surface + (rand.nextFloat() - rand.nextFloat()) / 4.0 : rand.nextFloat() / 2.0 + 0.25;
        level.addParticle(new VentParticleOptions(dir.getStepX() * VENT_SPEED, dir.getStepY() * VENT_SPEED, dir.getStepZ() * VENT_SPEED, VENT_COLOR, 2.0F, false), pos.getX() + x, pos.getY() + y,
                pos.getZ() + z, 0.0, 0.0, 0.0);
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == VENT_EVENT) {
            venting = 20;
            return true;
        }
        return super.triggerEvent(event, param);
    }

    private static boolean canSpawnCrab(Level level, BlockPos pos) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ACTIVATION_RANGE, false) == null) {
            return false;
        }
        List<EntityEldritchCrab> crabs = level.getEntitiesOfClass(EntityEldritchCrab.class, new AABB(pos).inflate(CRAB_SCAN_RANGE));
        return crabs.size() < MAX_CRABS;
    }

    private void spawnCrab(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction dir = state.getValue(BlockEldritchCrabSpawner.FACING);
        EntityEldritchCrab crab = new EntityEldritchCrab(TCEntities.ELDRITCH_CRAB.get(), level);
        double offsetX = dir.getAxis() == Direction.Axis.X ? crab.getBbWidth() / 2.0 : 0.5;
        double offsetY = dir.getAxis() == Direction.Axis.Y ? (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.75 - crab.getBbHeight() : 0.25) : 0.5 - crab.getBbHeight() / 2.0;
        double offsetZ = dir.getAxis() == Direction.Axis.Z ? crab.getBbWidth() / 2.0 : 0.5;
        crab.snapTo(pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, dir.toYRot(), 0.0F);
        crab.setDeltaMovement(dir.getStepX() * 0.2F, dir.getStepY() * 0.2F, dir.getStepZ() * 0.2F);
        crab.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), EntitySpawnReason.SPAWNER, null);
        int difficulty = Math.max((int) (level.getDifficulty().getId() + serverLevel.getCurrentDifficultyAt(pos).getEffectiveDifficulty()), 1);
        crab.setHelm(level.getRandom().nextInt(Math.max(HELM_ROLL / difficulty, 1)) == 0);
        if (level.getRandom().nextInt(Math.max(CHAMPION_ROLL / difficulty, 1)) == 0) {
            ChampionHelper.makeChampion(crab, false);
        }
        if (level.addFreshEntity(crab)) {
            level.playSound(null, pos, TCSounds.GORE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }
}
