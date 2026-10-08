package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.golem.CropUtils;
import com.leclowndu93150.thaumaturge.content.golem.GolemInteractionHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class HarvestBehavior implements ISealBehavior {
    public static final SealSetting REPLANT = new SealSetting("prep", "gui.thaumaturge.seal.setting.replant", true);
    public static final SealSetting REQUEST_SEEDS =
            new SealSetting("ppro", "gui.thaumaturge.seal.setting.provision", false);
    public static final MapCodec<HarvestBehavior> CODEC = ReplantSite.CODEC
            .listOf()
            .optionalFieldOf("replant", List.of())
            .xmap(HarvestBehavior::new, behavior -> List.copyOf(behavior.sites.values()));

    private static final int STAGGER = 33;
    private static final int SCAN_PERIOD = 5;
    private static final int TIDY_PERIOD = 100;
    private static final int BREAK_EFFECT = 2001;
    private static final int REPLANT_LIFESPAN = 300;

    private final SealClock clock = new SealClock(STAGGER);
    private final Map<Long, ReplantSite> sites = new HashMap<>();
    private int cursor;

    public HarvestBehavior() {}

    private HarvestBehavior(List<ReplantSite> saved) {
        saved.forEach(site -> sites.put(site.pos().asLong(), site));
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.at(TIDY_PERIOD)) {
            forgetSitesOutside(level, SealArea.bounds(seal));
        }
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        BlockPos cell = SealArea.cell(seal, cursor++);
        if (CropUtils.isGrownCrop(level, cell)) {
            post(level, seal, cell);
            return;
        }
        ReplantSite site = sites.get(cell.asLong());
        if (seal.setting(REPLANT)
                && site != null
                && level.getBlockState(cell).isAir()
                && !TaskBoard.of(level).isLive(site.taskId())) {
            site.assign(post(level, seal, site.pos()).id());
        }
    }

    private void forgetSitesOutside(ServerLevel level, AABB bounds) {
        sites.values().removeIf(site -> {
            if (bounds.contains(Vec3.atCenterOf(site.pos()))) {
                return false;
            }
            Task replant = TaskBoard.of(level).find(site.taskId());
            if (replant != null) {
                replant.end();
            }
            return true;
        });
    }

    private static Task post(ServerLevel level, ISealEntity seal, BlockPos pos) {
        Task task = Task.atBlock(seal.pos(), pos);
        task.setPriority(seal.priority());
        TaskBoard.of(level).post(task);
        return task;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (CropUtils.isGrownCrop(level, task.pos())) {
            reap(level, seal, golem, task);
        } else {
            replant(level, golem, task);
        }
        task.end();
        return true;
    }

    private void reap(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        BlockPos pos = task.pos();
        FakePlayer hand = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        BlockState crop = level.getBlockState(pos);
        if (CropUtils.isClickableCrop(crop)) {
            Vec3 toward = Vec3.atCenterOf(pos).subtract(golem.asEntity().position());
            crop.useWithoutItem(
                    level,
                    hand,
                    new BlockHitResult(
                            Vec3.atCenterOf(pos),
                            Direction.getNearest(toward.x, toward.y, toward.z).getOpposite(),
                            pos,
                            false));
            golem.addRankXp(1);
            golem.swingArm();
            return;
        }
        GolemInteractionHelper.golemClick(level, golem, pos, seal.pos().face(), ItemStack.EMPTY, false, true);
        if (!CropUtils.isGrownCrop(level, pos)) {
            return;
        }
        ItemStack seed = CropUtils.getSeed(level, pos, crop);
        BlockBreakerEngine.harvestBlock(level, hand, pos, false, 0);
        golem.addRankXp(1);
        golem.swingArm();
        if (!seal.setting(REPLANT) || seed.isEmpty()) {
            return;
        }
        Direction plantFace = plantingFace(crop, level.getBlockState(pos.below()));
        if (plantFace != null) {
            Task replant = Task.atBlock(seal.pos(), pos);
            replant.setPriority(task.priority());
            replant.setLife(REPLANT_LIFESPAN);
            TaskBoard.of(level).post(replant);
            ReplantSite site = new ReplantSite(
                    pos,
                    plantFace,
                    seed.copy(),
                    level.getBlockState(pos.below()).getBlock() instanceof FarmBlock);
            site.assign(replant.id());
            sites.put(pos.asLong(), site);
        }
    }

    private static @Nullable Direction plantingFace(BlockState crop, BlockState soil) {
        boolean cocoa = crop.getBlock() instanceof CocoaBlock;
        if (!cocoa && soil.is(BlockTags.DIRT) || soil.getBlock() instanceof FarmBlock) {
            return Direction.DOWN;
        }
        return cocoa ? crop.getValue(CocoaBlock.FACING) : null;
    }

    private void replant(ServerLevel level, IGolemAPI golem, Task task) {
        BlockPos pos = task.pos();
        ReplantSite site = sites.get(pos.asLong());
        if (site == null
                || site.taskId() != task.id()
                || !level.getBlockState(pos).isAir()
                || !golem.hands().holds(site.seed())) {
            return;
        }
        FakePlayer hand = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        BlockState soil = level.getBlockState(pos.below());
        if (site.tilled() && soil.is(BlockTags.DIRT) && !(soil.getBlock() instanceof FarmBlock)) {
            useOn(
                    hand,
                    new ItemStack(Items.DIAMOND_HOE),
                    new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false));
        }
        ItemStack seed = site.seed().copyWithCount(1);
        BlockPos support = pos.relative(site.face());
        if (useOn(
                        hand,
                        seed.copy(),
                        new BlockHitResult(Vec3.atCenterOf(support), site.face().getOpposite(), support, false))
                .consumesAction()) {
            level.levelEvent(BREAK_EFFECT, pos, Block.getId(level.getBlockState(pos)));
            golem.hands().release(seed);
            golem.addRankXp(1);
            golem.swingArm();
        }
    }

    private static InteractionResult useOn(FakePlayer hand, ItemStack tool, BlockHitResult hit) {
        hand.setItemInHand(InteractionHand.MAIN_HAND, tool);
        InteractionResult result = tool.useOn(new UseOnContext(hand, InteractionHand.MAIN_HAND, hit));
        hand.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return result;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ReplantSite site = sites.get(task.pos().asLong());
        if (site == null || site.taskId() != task.id()) {
            return true;
        }
        boolean holdingSeed = golem.hands().holds(site.seed());
        if (!holdingSeed && seal.setting(REQUEST_SEEDS)) {
            GolemHelper.requestProvisioning(golem.level(), seal, site.seed());
        }
        return holdingSeed;
    }
}
