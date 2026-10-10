package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper.InvFilter;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BreakBehavior extends CellWorkBehavior {
    public static final SealSetting SILK_TOUCH = new SealSetting("silk_touch", "gui.thaumaturge.seal.setting.silk", false).readingAlso("psilk");

    private static final int STAGGER = 61;
    private static final float WORK_PER_HARDNESS = 10.0F;
    private static final int STRIKE_POWER = 21;
    private static final int SILK_STRIKE_POWER = 7;
    private static final int NO_FORTUNE = 0;
    private static final int BREAK_XP = 1;
    private static final float STRIKE_VOLUME = 0.25F;
    private static final float STRIKE_PITCH = 0.5F;
    private static final int CRACK_STAGES = 10;
    private static final int CRACK_CLEARED = -1;

    public BreakBehavior() {
        super(STAGGER);
    }

    @Override
    protected boolean isWorkable(Level level, ISealEntity seal, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.liquid() || state.getDestroySpeed(level, pos) < 0) {
            return false;
        }
        return passesFilter(level, seal, pos, state);
    }

    @Override
    protected void prepare(ServerLevel level, BlockPos pos, Task task) {
        task.setData(workFor(level, pos, level.getBlockState(pos)));
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        BlockPos pos = task.pos();
        if (!isWorkable(level, seal, pos)) {
            return retire(task);
        }
        golem.swingArm();
        BlockState state = level.getBlockState(pos);
        boolean silk = seal.setting(SILK_TOUCH);
        int strike = silk ? SILK_STRIKE_POWER : STRIKE_POWER;
        int crackId = golem.asEntity().getId();
        if (task.data() > strike) {
            task.setData(task.data() - strike);
            keepAlive(task);
            level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, STRIKE_VOLUME, STRIKE_PITCH);
            level.destroyBlockProgress(crackId, pos, crackStage(workFor(level, pos, state), task.data()));
            return false;
        }
        BlockBreakerEngine.harvestBlock(level, TTFakePlayer.GOLEM.at(level, golem.asEntity()), pos, silk, NO_FORTUNE);
        level.destroyBlockProgress(crackId, pos, CRACK_CLEARED);
        golem.addRankXp(BREAK_XP);
        return retire(task);
    }

    private static int workFor(Level level, BlockPos pos, BlockState state) {
        return Mth.ceil(state.getDestroySpeed(level, pos) * WORK_PER_HARDNESS);
    }

    private static int crackStage(int total, int remaining) {
        if (total <= 0) {
            return 0;
        }
        float done = (float) (total - remaining) / total;
        return Mth.clamp(Mth.floor(done * CRACK_STAGES), 0, CRACK_STAGES - 1);
    }

    private static boolean passesFilter(Level level, ISealEntity seal, BlockPos pos, BlockState state) {
        ISealFilter filter = ItemMatchSettings.filterOf(seal);
        if (filter.stacks().stream().allMatch(ItemStack::isEmpty)) {
            return true;
        }
        ItemStack form = itemForm(level, pos, state);
        if (form.isEmpty()) {
            return filter.isBlacklist();
        }
        boolean exact = seal.setting(ItemMatchSettings.MATCH_DAMAGE);
        InvFilter match = new InvFilter(!exact, !exact, false, false);
        return InvHelper.matchesFilters(filter.stacks(), filter.isBlacklist(), form, match);
    }

    private static ItemStack itemForm(Level level, BlockPos pos, BlockState state) {
        ItemStack picked = state.getCloneItemStack(level, pos, false);
        return picked.isEmpty() ? new ItemStack(state.getBlock().asItem()) : picked;
    }
}
