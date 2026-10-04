package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.server.TCFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class BreakBehavior extends CellWorkBehavior {
    public static final SealSetting SILK_TOUCH = new SealSetting("psilk", "golem.prop.silk", false);

    private static final int STAGGER = 42;
    private static final float HARDNESS_SCALE = 10.0F;
    private static final int CHIP_POWER = 21;
    private static final int GENTLE_CHIP_POWER = 7;
    private static final float CRACK_STAGES = 9.0F;
    private static final int SHATTERED = 10;
    private static final float CHIP_VOLUME_BOOST = 0.7F;
    private static final float CHIP_VOLUME_SCALE = 8.0F;
    private static final float CHIP_PITCH_SCALE = 0.5F;

    public BreakBehavior() {
        super(STAGGER);
    }

    @Override
    protected boolean isWorkable(Level level, ISealEntity seal, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        ItemStack asItem = new ItemStack(state.getBlock());
        boolean blacklist = seal.filter().orElseThrow().isBlacklist();
        for (ItemStack ghost : seal.filter().orElseThrow().stacks()) {
            if (!ghost.isEmpty() && blacklist == (!asItem.isEmpty() && ItemStack.isSameItem(asItem, ghost))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void prepare(ServerLevel level, BlockPos pos, Task task) {
        task.setData((int) (level.getBlockState(pos).getDestroySpeed(level, pos) * HARDNESS_SCALE));
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        BlockPos pos = task.pos();
        if (stillMine(level, seal, task)) {
            BlockState state = level.getBlockState(pos);
            golem.swingArm();
            boolean silky = seal.setting(SILK_TOUCH);
            int chip = silky ? GENTLE_CHIP_POWER : CHIP_POWER;
            if (task.data() > chip) {
                chipAt(level, golem, task, state, chip);
                return false;
            }
            level.destroyBlockProgress(golem.asEntity().getId(), pos, SHATTERED);
            BlockBreakerEngine.harvestBlock(level, TCFakePlayer.GOLEM.at(level, golem.asEntity()), pos, silky, 0);
            golem.addRankXp(1);
            release(task);
        }
        task.suspend();
        return true;
    }

    private static void chipAt(ServerLevel level, IGolemAPI golem, Task task, BlockState state, int chip) {
        BlockPos pos = task.pos();
        float toughness = state.getDestroySpeed(level, pos) * HARDNESS_SCALE;
        keepAlive(task);
        task.setData(task.data() - chip);
        SoundType sound = state.getSoundType();
        level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + CHIP_VOLUME_BOOST) / CHIP_VOLUME_SCALE, sound.getPitch() * CHIP_PITCH_SCALE);
        level.destroyBlockProgress(golem.asEntity().getId(), pos, (int) (CRACK_STAGES * (1.0F - task.data() / toughness)));
    }
}
