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
            mob.restrictTo(start, 32);
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

    @GameTest(template = "golem_navigation", batch = "world-stone-vanilla", timeoutTicks = 600)
    public static void worldStoneVanilla(GameTestHelper helper) {
        worldPlatform(helper, 0, false);
    }

    @GameTest(template = "golem_navigation", batch = "world-stone-sublevel", timeoutTicks = 600)
    public static void worldStoneSublevel(GameTestHelper helper) {
        worldPlatform(helper, 0, true);
    }

    @GameTest(template = "golem_navigation", batch = "world-gold-vanilla", timeoutTicks = 600)
    public static void worldGoldVanilla(GameTestHelper helper) {
        worldPlatform(helper, 1, false);
    }

    @GameTest(template = "golem_navigation", batch = "world-gold-sublevel", timeoutTicks = 600)
    public static void worldGoldSublevel(GameTestHelper helper) {
        worldPlatform(helper, 1, true);
    }

    @GameTest(template = "golem_navigation", batch = "world-purpur-vanilla", timeoutTicks = 600)
    public static void worldPurpurVanilla(GameTestHelper helper) {
        worldPlatform(helper, 2, false);
    }

    @GameTest(template = "golem_navigation", batch = "world-purpur-sublevel", timeoutTicks = 600)
    public static void worldPurpurSublevel(GameTestHelper helper) {
        worldPlatform(helper, 2, true);
    }

    @GameTest(template = "golem_navigation", batch = "world-tuff-vanilla", timeoutTicks = 600)
    public static void worldTuffVanilla(GameTestHelper helper) {
        worldPlatform(helper, 3, false);
    }

    @GameTest(template = "golem_navigation", batch = "world-tuff-sublevel", timeoutTicks = 600)
    public static void worldTuffSublevel(GameTestHelper helper) {
        worldPlatform(helper, 3, true);
    }

    @GameTest(template = "golem_navigation", batch = "world-diorite-vanilla", timeoutTicks = 600)
    public static void worldDioriteVanilla(GameTestHelper helper) {
        worldPlatform(helper, 4, false);
    }

    @GameTest(template = "golem_navigation", batch = "world-diorite-sublevel", timeoutTicks = 600)
    public static void worldDioriteSublevel(GameTestHelper helper) {
        worldPlatform(helper, 4, true);
    }

    @GameTest(template = "golem_navigation", batch = "world-diorite-offset", timeoutTicks = 600)
    public static void worldDioriteOffset(GameTestHelper helper) {
        worldPlatform(helper, 4, true, 0.19, -0.16, 0);
    }

    @GameTest(template = "golem_navigation", batch = "world-tuff-offset", timeoutTicks = 600)
    public static void worldTuffOffset(GameTestHelper helper) {
        worldPlatform(helper, 3, true, -0.15, 0.18, 0);
    }

    @GameTest(template = "golem_navigation", batch = "world-diorite-turned", timeoutTicks = 600)
    public static void worldDioriteTurned(GameTestHelper helper) {
        worldPlatform(helper, 4, true, 0.13, 0.17, Math.PI / 2);
    }

    @GameTest(template = "golem_navigation", batch = "world-tuff-repeated", timeoutTicks = 1800)
    public static void worldTuffRepeated(GameTestHelper helper) {
        worldPlatform(helper, 3, true, -0.15, 0.18, 0, 4);
    }

    @GameTest(template = "golem_navigation", batch = "world-diorite-repeated", timeoutTicks = 1800)
    public static void worldDioriteRepeated(GameTestHelper helper) {
        worldPlatform(helper, 4, true, 0.13, 0.17, Math.PI / 2, 4);
    }

    private static void worldPlatform(GameTestHelper helper, int kind, boolean sublevelTest) {
        worldPlatform(helper, kind, sublevelTest, 0, 0, 0);
    }

    private static void worldPlatform(
            GameTestHelper helper, int kind, boolean sublevelTest, double offsetX, double offsetZ, double yaw) {
        worldPlatform(helper, kind, sublevelTest, offsetX, offsetZ, yaw, 1);
    }

    private static void worldPlatform(
            GameTestHelper helper,
            int kind,
            boolean sublevelTest,
            double offsetX,
            double offsetZ,
            double yaw,
            int rounds) {
        var level = helper.getLevel();
        var block =
                switch (kind) {
                    case 0 -> Blocks.STONE;
                    case 1 -> Blocks.GOLD_BLOCK;
                    case 2 -> Blocks.PURPUR_BLOCK;
                    case 3 -> Blocks.TUFF_BRICKS;
                    default -> Blocks.DIORITE;
                };
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 5; z++) {
                helper.setBlock(new BlockPos(x + 1, 2, z + 1).above(120), block);
                if (z != 2 || x < 2 || (kind == 0 && x == 2)) {
                    helper.setBlock(new BlockPos(x + 1, 5, z + 1).above(120), block);
                }
            }
        }
        helper.setBlock(new BlockPos(1, 5, 1).above(120), Blocks.NETHERITE_BLOCK);
        helper.setBlock(new BlockPos(6, 2, 1).above(120), Blocks.DIAMOND_BLOCK);
        helper.setBlock(new BlockPos(6, 2, 5).above(120), Blocks.IRON_BLOCK);
        helper.setBlock(new BlockPos(6, 3, 1).above(120), Blocks.CHEST);
        for (int x = 1; x <= 3; x++) helper.setBlock(new BlockPos(x + 1, 3, 3).above(120), block);
        for (int x = 1; x <= 2; x++) helper.setBlock(new BlockPos(x + 1, 4, 3).above(120), block);
        if (kind == 1 || kind == 4) {
            var stair = Blocks.OAK_STAIRS
                    .defaultBlockState()
                    .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST);
            if (kind == 1) helper.setBlock(new BlockPos(5, 3, 3).above(120), stair);
            helper.setBlock(new BlockPos(kind == 1 ? 4 : 3, 4, 3).above(120), stair);
            helper.setBlock(new BlockPos(kind == 1 ? 3 : 2, 5, 3).above(120), stair);
            if (kind == 4) {
                helper.setBlock(new BlockPos(4, 3, 3).above(120), stair);
                helper.setBlock(new BlockPos(4, 4, 3).above(120), Blocks.AIR);
            }
        }
        var min = helper.absolutePos(new BlockPos(1, 2, 1).above(120));
        var max = helper.absolutePos(new BlockPos(6, 5, 5).above(120));
        var physics = SubLevelContainer.getContainer(level).physicsSystem();
        var platform = sublevelTest
                ? SubLevelAssemblyHelper.assembleBlocks(
                        level,
                        min,
                        BlockPos.betweenClosed(min, max),
                        new BoundingBox3i(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()))
                : null;
        Runnable[] cleanup = {() -> {}};
        ItemEntity[] droppedItem = {null};
        int[] offered = {1};
        com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem[] subject = {null};
        helper.runAtTickTime(10, () -> {
            if (platform != null) {
                physics.setPaused(true);
                level.setChunkForced(0, 0, true);
                var rotation = kind == 3
                        ? new org.joml.Quaterniond(-0.01336944, -0.01431487, -0.25215298, 0.96748912).normalize()
                        : kind == 4
                                ? new org.joml.Quaterniond(0.09446037, 0.02579588, -0.25502026, 0.96196491).normalize()
                                : kind == 1
                                        ? new org.joml.Quaterniond(0.00071199, 0.00275303, 0.00554622, 0.99998063)
                                                .normalize()
                                        : new org.joml.Quaterniond();
                rotation.rotateY(yaw);
                physics.getPipeline().teleport(platform, new Vector3d(8 + offsetX, 100, 8 + offsetZ), rotation);
                physics.updatePose(platform);
                platform.updateBoundingBox();
                platform.updateLastPose();
            }
            var anchor = platform == null ? min : platform.getPlot().getCenterBlock();
            var pickup = anchor.offset(0, 3, 0);
            var chest = anchor.offset(5, 1, 0);
            var start = anchor.offset(5, 1, 4);
            var worldStart = LevelSpaces.toWorld(
                    level, net.minecraft.world.phys.Vec3.atBottomCenterOf(start).add(offsetX, 0, offsetZ));
            var mob = TTEntities.THAUMATURGE_GOLEM.get().create(level);
            if (mob == null) throw new GameTestAssertException("Could not create golem");
            subject[0] = mob;
            mob.applyBuild(new com.leclowndu93150.thaumaturge.content.golem.GolemProperties(
                    com.leclowndu93150.thaumaturge.registry.TTGolemParts.WOOD.get(),
                    com.leclowndu93150.thaumaturge.registry.TTGolemParts.HEAD_SMART.get(),
                    com.leclowndu93150.thaumaturge.registry.TTGolemParts.ARMS_BASIC.get(),
                    com.leclowndu93150.thaumaturge.registry.TTGolemParts.LEGS_WALKER.get(),
                    com.leclowndu93150.thaumaturge.registry.TTGolemParts.ADDON_NONE.get(),
                    0));
            mob.moveTo(worldStart.x, worldStart.y + 0.05, worldStart.z, 0, 0);
            mob.setPersistenceRequired();
            mob.setValidSpawn();
            level.addFreshEntity(mob);
            mob.restrictTo(start, 32);
            mob.updateEntityAttributes();
            var pickupSeal = new SealPos(pickup, Direction.UP);
            var storeSeal = new SealPos(chest, Direction.WEST);
            var owner = java.util.UUID.randomUUID();
            if (!SealHandler.addSealEntity(
                            level,
                            SealEntity.place(
                                    pickupSeal, TTSeals.PICKUP_ADVANCED.getId(), TTSeals.PICKUP_ADVANCED.get(), owner))
                    || !SealHandler.addSealEntity(
                            level,
                            SealEntity.place(
                                    storeSeal, TTSeals.FILL_ADVANCED.getId(), TTSeals.FILL_ADVANCED.get(), owner)))
                throw new GameTestAssertException("Could not place seals");
            var itemPos = LevelSpaces.toWorld(level, net.minecraft.world.phys.Vec3.atBottomCenterOf(pickup.above()));
            var item = new ItemEntity(level, itemPos.x, itemPos.y + 0.1, itemPos.z, new ItemStack(Items.DIAMOND));
            item.setDeltaMovement(0, 0, 0);
            level.addFreshEntity(item);
            droppedItem[0] = item;
            cleanup[0] = () -> {
                SealHandler.removeSealEntity(level, pickupSeal, true);
                SealHandler.removeSealEntity(level, storeSeal, true);
                mob.discard();
                if (droppedItem[0] != null) droppedItem[0].discard();
                if (platform != null) {
                    SubLevelContainer.getContainer(level)
                            .removeSubLevel(
                                    platform, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
                    physics.setPaused(false);
                    level.setChunkForced(0, 0, false);
                }
            };
            if (kind != 0)
                helper.succeedWhen(() -> {
                    if (platform != null
                            && (kind == 3 || kind == 4)
                            && new Vector3d(0, 1, 0)
                                            .rotate(platform.logicalPose().orientation())
                                            .y
                                    > 0.98) throw new GameTestAssertException("Sublevel lost its tilt");
                    if (!(level.getBlockEntity(chest) instanceof ChestBlockEntity inventory)
                            || inventory.countItem(Items.DIAMOND) != offered[0])
                        throw new GameTestAssertException("Golem did not deliver item: " + mob.position());
                    if (mob.distanceToSqr(LevelSpaces.worldCenter(level, start)) >= 5.0)
                        throw new GameTestAssertException("Golem did not return home: " + mob.position());
                    if (offered[0] < rounds) {
                        net.minecraft.world.phys.Vec3 nextPos = LevelSpaces.toWorld(
                                level, net.minecraft.world.phys.Vec3.atBottomCenterOf(pickup.above()));
                        droppedItem[0] = new ItemEntity(
                                level, nextPos.x, nextPos.y + 0.1, nextPos.z, new ItemStack(Items.DIAMOND));
                        droppedItem[0].setDeltaMovement(0, 0, 0);
                        level.addFreshEntity(droppedItem[0]);
                        offered[0]++;
                        throw new GameTestAssertException("Waiting for delivery " + offered[0]);
                    }
                    cleanup[0].run();
                });
        });
        helper.runAtTickTime(rounds == 1 ? 580 : 1780, () -> {
            var chest = (platform == null ? min : platform.getPlot().getCenterBlock()).offset(5, 1, 0);
            boolean delivered = level.getBlockEntity(chest) instanceof ChestBlockEntity inventory
                    && inventory.countItem(Items.DIAMOND) > 0;
            boolean itemPresent = droppedItem[0] != null
                    && droppedItem[0].isAlive()
                    && droppedItem[0].getItem().is(Items.DIAMOND);
            String details = subject[0] == null
                    ? "golem missing"
                    : "pos=" + subject[0].position()
                            + " alive=" + subject[0].isAlive() + " hands="
                            + subject[0].hands().contents()
                            + " itemPresent=" + itemPresent + " delivered=" + delivered
                            + " task="
                            + (subject[0].getTask() == null
                                    ? "none"
                                    : subject[0].getTask().id())
                            + " nav=" + subject[0].getNavigation().getClass().getSimpleName()
                            + " navDone=" + subject[0].getNavigation().isDone()
                            + " navTarget=" + subject[0].getNavigation().getTargetPos();
            cleanup[0].run();
            if (kind != 0) helper.fail("Golem did not finish: " + details);
            else if (delivered || !itemPresent) helper.fail("Golem collected item through blocked stairs");
            else helper.succeed();
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
        var system = SubLevelContainer.getContainer(level).physicsSystem();
        Runnable[] cleanup = {() -> {}};
        helper.runAtTickTime(10, () -> {
            level.setChunkForced(0, 0, true);
            system.setPaused(true);
            system.getPipeline().teleport(sublevel, new Vector3d(8, 100, 8), new org.joml.Quaterniond());
            system.updatePose(sublevel);
            sublevel.updateBoundingBox();
            sublevel.updateLastPose();
            cleanup[0] = () -> {
                SubLevelContainer.getContainer(level)
                        .removeSubLevel(sublevel, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
                system.setPaused(false);
                level.setChunkForced(0, 0, false);
            };
            if (move && !walk) {
                var physics =
                        SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
                physics.teleport(
                        sublevel,
                        new Vector3d(sublevel.logicalPose().position()).add(0.25, 0, 0),
                        sublevel.logicalPose().orientation());
                system.updatePose(sublevel);
                sublevel.updateBoundingBox();
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
                    mob.discard();
                    cleanup[0].run();
                });
                return;
            }
            var navigation =
                    walk ? mob.getNavigation() : new GolemSpaceNavigation(mob, level, mob.getNavigation(), false);
            var targetRelative = descending ? new BlockPos(1, 3, 3) : new BlockPos(10, 3 + height, 3);
            var target = plotAnchor.offset(targetRelative.offset(-1, -2, -2));
            var path = navigation.createPath(target, 1);
            if (path == null) {
                mob.discard();
                cleanup[0].run();
                helper.fail("Golem cannot find a path on the sublevel stairs");
            } else if (!walk) {
                mob.discard();
                cleanup[0].run();
                helper.succeed();
            } else {
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
                        system.updatePose(sublevel);
                        sublevel.updateBoundingBox();
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
                    mob.discard();
                    cleanup[0].run();
                });
            }
        });
        helper.runAtTickTime(collect ? 590 : walk ? 290 : 90, () -> cleanup[0].run());
    }
}
