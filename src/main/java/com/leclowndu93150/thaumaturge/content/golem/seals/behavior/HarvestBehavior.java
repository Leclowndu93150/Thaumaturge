package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.Thaumaturge;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class HarvestBehavior implements ISealBehavior {
    public static final SealSetting REPLANT = new SealSetting("replant_crops", "gui.thaumaturge.seal.setting.replant", true);
    public static final SealSetting REQUEST_SEEDS = new SealSetting("request_seeds", "gui.thaumaturge.seal.setting.provision", false);
    public static final MapCodec<HarvestBehavior> CODEC = ReplantSite.CODEC.codec().listOf().optionalFieldOf("replant_sites", List.of()).xmap(HarvestBehavior::new, HarvestBehavior::savedSites);

    private static final int STAGGER = 41;
    private static final int SCAN_PERIOD = 5;
    private static final int UPKEEP_PERIOD = 100;
    private static final int REPLANT_LIFE = 300;
    private static final int BREAK_XP = 1;
    private static final int NO_FORTUNE = 0;
    private static final int HARVEST_JOB = 0;
    private static final int REPLANT_JOB = 1;
    private static final double FACE_OFFSET = 0.5;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<BlockPos> crops = new TaskLedger<>();
    private final Map<BlockPos, ReplantSite> sites = new LinkedHashMap<>();
    private int cellCursor;

    public HarvestBehavior() {}

    private HarvestBehavior(List<ReplantSite> loaded) {
        loaded.forEach(site -> sites.put(site.pos(), site));
    }

    private List<ReplantSite> savedSites() {
        return List.copyOf(sites.values());
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int now = clock.advance();
        if (now % UPKEEP_PERIOD == 0) {
            crops.dropFinished(level);
            dropSitesOutside(level, seal);
        }
        if (now % SCAN_PERIOD != 0) {
            return;
        }
        BlockPos cell = SealArea.cell(seal, cellCursor++);
        if (!level.hasChunkAt(cell)) {
            return;
        }
        if (CropUtils.isGrownCrop(level, cell)) {
            if (!crops.tracks(cell)) {
                postHarvest(level, seal, cell.immutable());
            }
            return;
        }
        ReplantSite site = sites.get(cell);
        if (site != null && seal.setting(REPLANT) && level.getBlockState(cell).isAir() && !hasLiveTask(level, site)) {
            postReplant(level, seal, site, seal.priority());
        }
    }

    private void postHarvest(ServerLevel level, ISealEntity seal, BlockPos cell) {
        Task task = Task.atBlock(seal.pos(), cell);
        task.setPriority(seal.priority());
        task.setData(HARVEST_JOB);
        GolemHelper.addGolemTask(level, task);
        crops.record(task, cell);
    }

    private void postReplant(ServerLevel level, ISealEntity seal, ReplantSite site, byte priority) {
        Task task = Task.atBlock(seal.pos(), site.pos());
        task.setPriority(priority);
        task.setLife(REPLANT_LIFE);
        task.setData(REPLANT_JOB);
        GolemHelper.addGolemTask(level, task);
        sites.put(site.pos(), site.withTask(task));
    }

    private static boolean hasLiveTask(ServerLevel level, ReplantSite site) {
        Task task = site.task();
        return task != null && !task.isEnded() && TaskBoard.of(level).isLive(task.id());
    }

    private void dropSitesOutside(ServerLevel level, ISealEntity seal) {
        AABB area = SealArea.bounds(seal);
        boolean changed = false;
        Iterator<ReplantSite> iterator = sites.values().iterator();
        while (iterator.hasNext()) {
            ReplantSite site = iterator.next();
            if (area.contains(Vec3.atCenterOf(site.pos()))) {
                continue;
            }
            Task task = site.task();
            if (task != null) {
                task.end();
            }
            iterator.remove();
            changed = true;
        }
        if (changed) {
            seal.markChanged(level);
        }
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        if (task.data() != REPLANT_JOB) {
            return true;
        }
        ReplantSite site = siteFor(task);
        if (site == null) {
            task.end();
            return false;
        }
        if (golem.hands().holds(site.seed())) {
            return true;
        }
        if (seal.setting(REQUEST_SEEDS)) {
            GolemHelper.requestProvisioning(golem.level(), seal, site.seed().copyWithCount(1));
        }
        return false;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (task.data() == REPLANT_JOB) {
            ReplantSite site = siteFor(task);
            if (site != null) {
                replant(level, golem, site);
            }
        } else {
            harvest(level, seal, golem, task);
            crops.forget(task);
        }
        task.end();
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        crops.forget(task);
        ReplantSite site = siteFor(task);
        if (site != null) {
            sites.put(site.pos(), site.withTask(null));
        }
    }

    private @Nullable ReplantSite siteFor(Task task) {
        ReplantSite site = sites.get(task.pos());
        return site != null && task.equals(site.task()) ? site : null;
    }

    private void harvest(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        BlockPos pos = task.pos();
        if (!CropUtils.isGrownCrop(level, pos)) {
            return;
        }
        BlockState crop = level.getBlockState(pos);
        if (CropUtils.isClickableCrop(crop)) {
            GolemInteractionHelper.golemClick(level, golem, pos, Direction.UP, ItemStack.EMPTY, false, false);
            return;
        }
        ItemStack seed = CropUtils.getSeed(level, pos, crop);
        Optional<Direction> support = CropUtils.plantingSupport(crop);
        boolean tilled = isFarmland(level.getBlockState(pos.below()));
        GolemInteractionHelper.golemClick(level, golem, pos, Direction.UP, ItemStack.EMPTY, false, false);
        if (!CropUtils.isGrownCrop(level, pos)) {
            return;
        }
        BlockBreakerEngine.harvestBlock(level, TTFakePlayer.GOLEM.at(level, golem.asEntity()), pos, false, NO_FORTUNE);
        golem.addRankXp(BREAK_XP);
        golem.swingArm();
        if (seal.setting(REPLANT) && !seed.isEmpty() && support.isPresent() && level.getBlockState(pos).isAir()) {
            Direction side = support.get();
            postReplant(level, seal, new ReplantSite(pos.immutable(), side, seed.copyWithCount(1), side == Direction.DOWN && tilled, null), task.priority());
            seal.markChanged(level);
        }
    }

    private static void replant(ServerLevel level, IGolemAPI golem, ReplantSite site) {
        BlockPos pos = site.pos();
        if (!level.getBlockState(pos).isAir() || !golem.hands().holds(site.seed())) {
            return;
        }
        Direction support = supportOf(level, site);
        BlockPos ground = pos.relative(support);
        if (site.tilled() && support == Direction.DOWN && !isFarmland(level.getBlockState(ground))) {
            till(level, golem, ground);
        }
        ItemStack seed = golem.hands().release(site.seed().copyWithCount(1));
        if (seed.isEmpty()) {
            return;
        }
        GolemInteractionHelper.golemClick(level, golem, ground, support.getOpposite(), seed, false, false);
        BlockState planted = level.getBlockState(pos);
        if (!planted.isAir()) {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(planted));
        }
    }

    private static Direction supportOf(ServerLevel level, ReplantSite site) {
        Direction stored = site.face();
        if (stored.getAxis().isVertical()) {
            return Direction.DOWN;
        }
        BlockPos pos = site.pos();
        boolean storedSideEmpty = level.getBlockState(pos.relative(stored)).isAir();
        return storedSideEmpty && !level.getBlockState(pos.relative(stored.getOpposite())).isAir() ? stored.getOpposite() : stored;
    }

    private static void till(ServerLevel level, IGolemAPI golem, BlockPos ground) {
        FakePlayer player = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        ItemStack hoe = new ItemStack(Items.WOODEN_HOE);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
            Vec3 hit = Vec3.atCenterOf(ground).relative(Direction.UP, FACE_OFFSET);
            player.gameMode.useItemOn(player, level, hoe, InteractionHand.MAIN_HAND, new BlockHitResult(hit, Direction.UP, ground, false));
        } catch (RuntimeException e) {
            Thaumaturge.LOGGER.error("Golem could not till {}", ground, e);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private static boolean isFarmland(BlockState state) {
        return state.getBlock() instanceof FarmlandBlock;
    }
}
