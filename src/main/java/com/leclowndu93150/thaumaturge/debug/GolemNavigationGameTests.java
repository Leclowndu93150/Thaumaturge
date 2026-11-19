package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.ai.GolemSpaceNavigation;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSeals;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class GolemNavigationGameTests {
    private GolemNavigationGameTests() {}

    @GameTest(template = "golem_navigation", batch = "climb", timeoutTicks = 100)
    public static void climbsSublevelStairs(GameTestHelper helper) {
        stairs(helper, false, false, 3, false);
    }

    @GameTest(template = "golem_navigation", batch = "descend", timeoutTicks = 100)
    public static void descendsSublevelStairs(GameTestHelper helper) {
        stairs(helper, true, false, 3, false);
    }

    @GameTest(template = "golem_navigation", batch = "moved", timeoutTicks = 100)
    public static void climbsMovedSublevelStairs(GameTestHelper helper) {
        stairs(helper, false, true, 3, false);
    }

    @GameTest(template = "golem_navigation", batch = "larger", timeoutTicks = 100)
    public static void climbsLargerSublevelStairs(GameTestHelper helper) {
        stairs(helper, false, false, 5, false);
    }

    @GameTest(template = "golem_navigation", batch = "walk", timeoutTicks = 300)
    public static void walksUpSublevelStairs(GameTestHelper helper) {
        stairs(helper, false, false, 3, true);
    }

    @GameTest(template = "golem_navigation", batch = "moving-walk", timeoutTicks = 300)
    public static void walksWhileSublevelMoves(GameTestHelper helper) {
        stairs(helper, false, true, 3, true);
    }

    @GameTest(template = "golem_navigation", batch = "collect", timeoutTicks = 600)
    public static void collectsUpstairsAndStoresDownstairs(GameTestHelper helper) {
        stairs(helper, false, false, 3, false, true);
    }

    @GameTest(template = "golem_navigation", batch = "collect-detour", timeoutTicks = 600)
    public static void collectsDirectlyAboveViaStairs(GameTestHelper helper) {
        stairs(helper, false, false, 3, false, true, true);
    }

    @GameTest(template = "golem_navigation", batch = "saved-layout", timeoutTicks = 600)
    public static void collectsOnSavedPlatformLayout(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 1; x <= 6; x++)
            for (int z = 1; z <= 8; z++) helper.setBlock(new BlockPos(x, 2, z), Blocks.JUNGLE_WOOD);
        for (int x = 1; x <= 5; x++)
            for (int z = 2; z <= 8; z++) {
                if (x != 3 || z < 4 || z > 6) helper.setBlock(new BlockPos(x, 6, z), Blocks.JUNGLE_WOOD);
            }
        helper.setBlock(new BlockPos(3, 3, 6), Blocks.JUNGLE_WOOD);
        helper.setBlock(new BlockPos(3, 4, 5), Blocks.JUNGLE_WOOD);
        helper.setBlock(new BlockPos(3, 5, 4), Blocks.JUNGLE_WOOD);
        helper.setBlock(new BlockPos(3, 3, 3), Blocks.CHEST);
        var min = helper.absolutePos(new BlockPos(1, 2, 1));
        var max = helper.absolutePos(new BlockPos(6, 6, 8));
        var bounds = new BoundingBox3i(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        var sublevel = SubLevelAssemblyHelper.assembleBlocks(level, min, BlockPos.betweenClosed(min, max), bounds);
        helper.runAtTickTime(10, () -> {
            var physics = SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
            physics.teleport(
                    sublevel,
                    new Vector3d(sublevel.logicalPose().position()),
                    new org.joml.Quaterniond(-0.000031158459, -0.003452062, 0.00015504, 0.99999404).normalize());
            sublevel.updateLastPose();
            var anchor = sublevel.getPlot().getCenterBlock();
            var start = anchor.offset(4, 1, 1);
            var pickup = anchor.offset(4, 4, 1);
            var chest = anchor.offset(2, 1, 2);
            var startWorld = LevelSpaces.toWorld(level, net.minecraft.world.phys.Vec3.atBottomCenterOf(start));
            var shift = new Vector3d(
                    Math.floor(startWorld.x) + 0.675886023 - startWorld.x,
                    0,
                    Math.floor(startWorld.z) + 0.858996726 - startWorld.z);
            physics.teleport(
                    sublevel,
                    new Vector3d(sublevel.logicalPose().position()).add(shift),
                    sublevel.logicalPose().orientation());
            sublevel.updateLastPose();
            startWorld = LevelSpaces.toWorld(level, net.minecraft.world.phys.Vec3.atBottomCenterOf(start));
            var mob = TTEntities.THAUMATURGE_GOLEM.get().create(level);
            if (mob == null) {
                helper.fail("Could not create golem");
                return;
            }
            mob.moveTo(startWorld.x, startWorld.y + 0.01, startWorld.z, 0, 0);
            mob.setPersistenceRequired();
            mob.setValidSpawn();
            level.addFreshEntity(mob);
            mob.updateEntityAttributes();
            var pickupSeal = new SealPos(pickup, Direction.UP);
            var storeSeal = new SealPos(chest, Direction.UP);
            var owner = java.util.UUID.randomUUID();
            if (!SealHandler.addSealEntity(
                            level, SealEntity.place(pickupSeal, TTSeals.PICKUP.getId(), TTSeals.PICKUP.get(), owner))
                    || !SealHandler.addSealEntity(
                            level, SealEntity.place(storeSeal, TTSeals.FILL.getId(), TTSeals.FILL.get(), owner))) {
                helper.fail("Could not place seals");
                return;
            }
            var itemPos = LevelSpaces.toWorld(
                    level,
                    net.minecraft.world.phys.Vec3.atBottomCenterOf(pickup.above())
                            .add(0, 0, 0.3));
            var item = new ItemEntity(level, itemPos.x, itemPos.y + 0.1, itemPos.z, new ItemStack(Items.DIAMOND));
            item.setDeltaMovement(0, 0, 0);
            level.addFreshEntity(item);
            helper.succeedWhen(() -> {
                if (!(level.getBlockEntity(chest) instanceof ChestBlockEntity inventory)
                        || inventory.countItem(Items.DIAMOND) != 1)
                    throw new GameTestAssertException("Golem did not deliver item: " + mob.position());
                SealHandler.removeSealEntity(level, pickupSeal, true);
                SealHandler.removeSealEntity(level, storeSeal, true);
            });
        });
    }

    private static void stairs(GameTestHelper helper, boolean descending, boolean move, int height, boolean walk) {
        stairs(helper, descending, move, height, walk, false);
    }

    private static void stairs(
            GameTestHelper helper, boolean descending, boolean move, int height, boolean walk, boolean collect) {
        stairs(helper, descending, move, height, walk, collect, false);
    }

    private static void stairs(
            GameTestHelper helper,
            boolean descending,
            boolean move,
            int height,
            boolean walk,
            boolean collect,
            boolean detour) {
        var level = helper.getLevel();
        for (int x = 1; x <= 12; x++)
            for (int z = 2; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
            }
        for (int x = 3; x <= 11; x++)
            for (int y = 3; y <= 2 + Math.min(height, x - 2); y++) {
                for (int z = collect ? 2 : 3; z <= (collect ? 4 : 3); z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        if (detour) {
            for (int x = 1; x <= 3; x++) helper.setBlock(new BlockPos(x, 2 + height, 3), Blocks.STONE);
        }
        if (collect) helper.setBlock(new BlockPos(2, 3, 2), Blocks.CHEST);
        BlockPos min = helper.absolutePos(new BlockPos(1, 2, 2));
        BlockPos max = helper.absolutePos(new BlockPos(12, 2 + height, 4));
        var bounds = new BoundingBox3i(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        var sublevel = SubLevelAssemblyHelper.assembleBlocks(level, min, BlockPos.betweenClosed(min, max), bounds);
        var startRelative = descending ? new BlockPos(10, 3 + height, 3) : new BlockPos(1, 3, 3);
        helper.runAtTickTime(10, () -> {
            if (move && !walk) {
                var physics =
                        SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
                physics.teleport(
                        sublevel,
                        new Vector3d(sublevel.logicalPose().position()).add(0.25, 0, 0),
                        sublevel.logicalPose().orientation());
                sublevel.updateLastPose();
            }
            var plotAnchor = sublevel.getPlot().getCenterBlock();
            var localStart = plotAnchor.offset(startRelative.offset(-1, -2, -2));
            var worldStart = LevelSpaces.toWorld(level, net.minecraft.world.phys.Vec3.atBottomCenterOf(localStart));
            var mob = TTEntities.THAUMATURGE_GOLEM.get().create(level);
            if (mob == null) {
                helper.fail("Could not create a golem");
                return;
            }
            mob.moveTo(worldStart.x, worldStart.y + 0.01, worldStart.z, 0, 0);
            mob.setNoAi(!walk && !collect);
            mob.setPersistenceRequired();
            mob.setValidSpawn();
            level.addFreshEntity(mob);
            if (walk || collect) {
                if (walk) mob.setFollowingOwner(true);
                mob.updateEntityAttributes();
            }
            if (collect) {
                var chestPos = plotAnchor.offset(1, 1, 0);
                var pickupPos = plotAnchor.offset(detour ? 0 : 9, height, 1);
                var pickupSeal = new SealPos(pickupPos, Direction.UP);
                var storeSeal = new SealPos(chestPos, Direction.UP);
                var owner = java.util.UUID.randomUUID();
                if (!SealHandler.addSealEntity(
                                level,
                                SealEntity.place(pickupSeal, TTSeals.PICKUP.getId(), TTSeals.PICKUP.get(), owner))
                        || !SealHandler.addSealEntity(
                                level, SealEntity.place(storeSeal, TTSeals.FILL.getId(), TTSeals.FILL.get(), owner))) {
                    helper.fail("Could not place seals");
                    return;
                }
                var itemPos =
                        LevelSpaces.toWorld(level, net.minecraft.world.phys.Vec3.atBottomCenterOf(pickupPos.above()));
                var item = new ItemEntity(level, itemPos.x, itemPos.y + 0.1, itemPos.z, new ItemStack(Items.DIAMOND));
                item.setDeltaMovement(0, 0, 0);
                level.addFreshEntity(item);
                helper.succeedWhen(() -> {
                    if (!(level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)
                            || chest.countItem(Items.DIAMOND) != 1) {
                        throw new GameTestAssertException("Golem has not stored the item; hands="
                                + mob.hands().contents() + " pos=" + mob.position());
                    }
                    SealHandler.removeSealEntity(level, pickupSeal, true);
                    SealHandler.removeSealEntity(level, storeSeal, true);
                });
                return;
            }
            var navigation =
                    walk ? mob.getNavigation() : new GolemSpaceNavigation(mob, level, mob.getNavigation(), false);
            var targetRelative = descending ? new BlockPos(1, 3, 3) : new BlockPos(10, 3 + height, 3);
            var target = plotAnchor.offset(targetRelative.offset(-1, -2, -2));
            var path = navigation.createPath(target, 1);
            if (path == null) helper.fail("Golem cannot find a path on the sublevel stairs");
            else if (!walk) helper.succeed();
            else {
                navigation.moveTo(path, 1.0);
                if (move)
                    helper.runAtTickTime(25, () -> {
                        var physics = SubLevelContainer.getContainer(level)
                                .physicsSystem()
                                .getPipeline();
                        physics.teleport(
                                sublevel,
                                new Vector3d(sublevel.logicalPose().position()).add(0.25, 0, 0),
                                sublevel.logicalPose().orientation());
                        sublevel.updateLastPose();
                    });
                helper.succeedWhen(() -> {
                    if (mob.position().distanceToSqr(LevelSpaces.worldCenter(level, target)) > 4.0) {
                        throw new GameTestAssertException("Golem has not reached the upper deck: pos=" + mob.position()
                                + " target=" + LevelSpaces.worldCenter(level, target) + " path="
                                + path.getClass().getSimpleName()
                                + " node=" + path.getNextNodeIndex() + " done=" + navigation.isDone()
                                + " ticks=" + mob.tickCount + " alive=" + mob.isAlive()
                                + " removal=" + mob.getRemovalReason() + " health=" + mob.getHealth()
                                + " current=" + (navigation.getPath() == path));
                    }
                });
            }
        });
    }
}
