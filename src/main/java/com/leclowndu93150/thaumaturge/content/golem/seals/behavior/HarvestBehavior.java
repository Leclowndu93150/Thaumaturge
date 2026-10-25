package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class HarvestBehavior implements ISealBehavior {
    public static final SealSetting REPLANT =
            new SealSetting("replant_crops", "gui.thaumaturge.seal.setting.replant", true).readingAlso("prep");
    public static final SealSetting REQUEST_SEEDS =
            new SealSetting("request_seeds", "gui.thaumaturge.seal.setting.provision", false).readingAlso("ppro");
    private static final String SITES_KEY = "replant_sites";
    private static final String LEGACY_SITES_KEY = "replant";
    public static final MapCodec<HarvestBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ReplantSite.CODEC.listOf().optionalFieldOf(SITES_KEY).forGetter(HarvestBehavior::storedSites),
                    ReplantSite.LEGACY_CODEC
                            .listOf()
                            .lenientOptionalFieldOf(LEGACY_SITES_KEY)
                            .forGetter(behavior -> Optional.empty()))
            .apply(instance, HarvestBehavior::restore));

    private static final int HARVEST_JOB = 0;
    static final int REPLANT_JOB = 1;
    private static final int STAGGER = 41;
    private static final int SCAN_PERIOD = 5;
    private static final int TIDY_PERIOD = 100;
    private static final int REPLANT_LIFESPAN = 300;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<BlockPos> harvests = new TaskLedger<>();
    private final Map<Long, ReplantSite> sites = new HashMap<>();
    private int cursor;

    public HarvestBehavior() {}

    private HarvestBehavior(List<ReplantSite> saved) {
        saved.forEach(site -> sites.put(site.pos().asLong(), site));
    }

    private static HarvestBehavior restore(Optional<List<ReplantSite>> current, Optional<List<ReplantSite>> legacy) {
        return new HarvestBehavior(current.or(() -> legacy).orElse(List.of()));
    }

    private Optional<List<ReplantSite>> storedSites() {
        return sites.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(sites.values()));
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int now = clock.advance();
        if (now % TIDY_PERIOD == 0) {
            harvests.dropFinished(level);
            forgetSitesOutside(level, seal, SealArea.bounds(seal));
        }
        if (now % SCAN_PERIOD != 0) {
            return;
        }
        BlockPos cell = SealArea.cell(seal, cursor++);
        if (CropUtils.isGrownCrop(level, cell)) {
            if (!harvests.tracks(cell)) {
                post(level, seal, cell.immutable(), HARVEST_JOB);
            }
            return;
        }
        ReplantSite site = sites.get(cell.asLong());
        if (seal.setting(REPLANT)
                && site != null
                && level.getBlockState(cell).isAir()
                && !TaskBoard.of(level).isLive(site.taskId())) {
            site.assign(post(level, seal, site.pos(), REPLANT_JOB).id());
        }
    }

    private void forgetSitesOutside(ServerLevel level, ISealEntity seal, AABB bounds) {
        boolean forgot = sites.values().removeIf(site -> {
            if (bounds.contains(Vec3.atCenterOf(site.pos()))) {
                return false;
            }
            Task replant = TaskBoard.of(level).find(site.taskId());
            if (replant != null) {
                replant.end();
            }
            return true;
        });
        if (forgot) {
            seal.markChanged(level);
        }
    }

    private Task post(ServerLevel level, ISealEntity seal, BlockPos pos, int job) {
        Task task = Task.atBlock(seal.pos(), pos);
        task.setPriority(seal.priority());
        task.setData(job);
        GolemDispatch.postTask(level, task);
        if (job == HARVEST_JOB) {
            harvests.record(task, pos);
        }
        return task;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ReplantSite site = siteFor(task);
        if (isReplantTask(task, site)) {
            replant(level, golem, task);
        } else {
            reap(level, seal, golem, task);
            harvests.forget(task);
        }
        task.end();
        return true;
    }

    static boolean isReplantTask(Task task, @Nullable ReplantSite site) {
        return task.data() == REPLANT_JOB || site != null && site.taskId() == task.id();
    }

    private @Nullable ReplantSite siteFor(Task task) {
        ReplantSite site = sites.get(task.pos().asLong());
        return site != null && site.taskId() == task.id() ? site : null;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        harvests.forget(task);
        ReplantSite site = siteFor(task);
        if (site != null && isReplantTask(task, site)) {
            site.assign(0);
        }
    }

    private void reap(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        BlockPos pos = task.pos();
        BlockState crop = level.getBlockState(pos);
        if (CropUtils.isClickableCrop(crop)) {
            GolemInteractionHelper.golemClick(level, golem, pos, Direction.UP, ItemStack.EMPTY, false, false);
            return;
        }
        FakePlayer hand = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        ItemStack seed = CropUtils.getSeed(level, pos, crop);
        Direction support = CropUtils.plantingSupport(crop).orElse(null);
        boolean tilled = level.getBlockState(pos.below()).getBlock() instanceof FarmBlock;
        GolemInteractionHelper.golemClick(level, golem, pos, Direction.UP, ItemStack.EMPTY, false, false);
        if (!CropUtils.isGrownCrop(level, pos)) {
            return;
        }
        BlockBreakerEngine.harvestBlock(level, hand, pos, false, 0);
        golem.grantRankExperience(1);
        golem.animateSwing();
        if (!seal.setting(REPLANT)
                || seed.isEmpty()
                || support == null
                || !level.getBlockState(pos).isAir()) {
            return;
        }
        Task replant = Task.atBlock(seal.pos(), pos);
        replant.setPriority(task.priority());
        replant.setLife(REPLANT_LIFESPAN);
        replant.setData(REPLANT_JOB);
        GolemDispatch.postTask(level, replant);
        ReplantSite site = new ReplantSite(pos, support, seed.copyWithCount(1), support == Direction.DOWN && tilled);
        site.assign(replant.id());
        sites.put(pos.asLong(), site);
        seal.markChanged(level);
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
        Direction support = supportOf(level, site);
        BlockPos ground = pos.relative(support);
        if (site.tilled() && support == Direction.DOWN && !isFarmland(level.getBlockState(ground))) {
            till(level, hand, ground);
        }
        ItemStack seed = site.seed().copyWithCount(1);
        InteractionResult result = plant(level, golem, seed, ground, support);
        if (result == InteractionResult.SUCCESS && !level.getBlockState(pos).isAir()) {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(level.getBlockState(pos)));
            golem.hands().release(site.seed().copyWithCount(1));
            golem.grantRankExperience(1);
            golem.animateSwing();
        }
    }

    private static InteractionResult plant(
            ServerLevel level, IGolemAPI golem, ItemStack seed, BlockPos ground, Direction support) {
        FakePlayer player = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, seed);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(ground), support.getOpposite(), ground, false);
            return player.gameMode.useItemOn(player, level, seed, InteractionHand.MAIN_HAND, hit);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    private static Direction supportOf(ServerLevel level, ReplantSite site) {
        Direction stored = site.face();
        if (stored.getAxis().isVertical()) {
            return Direction.DOWN;
        }
        BlockPos pos = site.pos();
        boolean storedSideEmpty = level.getBlockState(pos.relative(stored)).isAir();
        boolean oppositeSideEmpty =
                level.getBlockState(pos.relative(stored.getOpposite())).isAir();
        return resolveSupport(stored, storedSideEmpty, oppositeSideEmpty);
    }

    static Direction resolveSupport(Direction stored, boolean storedSideEmpty, boolean oppositeSideEmpty) {
        if (stored.getAxis().isVertical()) {
            return Direction.DOWN;
        }
        return storedSideEmpty && !oppositeSideEmpty ? stored.getOpposite() : stored;
    }

    private static void till(ServerLevel level, FakePlayer player, BlockPos ground) {
        ItemStack hoe = new ItemStack(Items.WOODEN_HOE);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
            Vec3 hit = Vec3.atCenterOf(ground).relative(Direction.UP, 0.5);
            player.gameMode.useItemOn(
                    player,
                    level,
                    hoe,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(hit, Direction.UP, ground, false));
        } catch (RuntimeException e) {
            Thaumaturge.LOGGER.error("Golem could not till {}", ground, e);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    private static boolean isFarmland(BlockState state) {
        return state.getBlock() instanceof FarmBlock;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ReplantSite site = siteFor(task);
        if (!isReplantTask(task, site)) {
            return true;
        }
        if (site == null) {
            task.end();
            return false;
        }
        boolean holdingSeed = golem.hands().holds(site.seed());
        if (!holdingSeed && seal.setting(REQUEST_SEEDS)) {
            GolemDispatch.orderSupply(golem.level(), seal, site.seed());
        }
        return holdingSeed;
    }
}
