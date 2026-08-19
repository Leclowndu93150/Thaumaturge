package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeSavedData;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintacle;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchGolem;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchWarden;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityTaintacleGiant;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityEldritchLock extends BlockEntity {
    private static final int OPEN_TICKS = 100;
    private static final int PUMP_INTERVAL = 5;
    private static final double MESSAGE_RANGE_SQ = 300.0;
    private static final int[][] PEDESTAL = {{2, 2, 2}, {0, -1, 1}, {3, 3, 3}};

    private int count = -1;

    public BlockEntityEldritchLock(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_LOCK.get(), pos, state);
    }

    public boolean isIdle() {
        return count < 0;
    }

    public int getCount() {
        return count;
    }

    public void activate() {
        count = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void clientTick() {
        if (count >= 0) {
            count++;
        }
    }

    public void serverTick(Level level, BlockPos pos) {
        if (count == -1) {
            return;
        }
        count++;
        if (count % PUMP_INTERVAL == 0) {
            level.playSound(null, pos, TCSounds.PUMP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (count > OPEN_TICKS) {
            doBossSpawn(level, pos);
        }
    }

    private void doBossSpawn(Level level, BlockPos pos) {
        level.playSound(null, pos, TCSounds.ICE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        int centerx = cx;
        int centerz = cz;
        int exit = 0;
        MazeSavedData maze = MazeSavedData.get(serverLevel);
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                MazeCell cell = maze.getCell(cx + a, cz + b);
                if (cell != null && cell.feature == MazeCell.FEATURE_BOSS_NW) {
                    centerx = cx + a;
                    centerz = cz + b;
                }
                if (cell != null && cell.feature >= MazeCell.FEATURE_BOSS_NW && cell.feature <= MazeCell.FEATURE_BOSS_SE && cell.hasAnyOpening()) {
                    exit = cell.feature;
                }
            }
        }
        int bossCount = maze.nextBossCount(serverLevel.getRandom().nextFloat() < 0.25F);
        switch (bossCount % 4) {
            case 0 -> spawnGolemBossRoom(serverLevel, pos, centerx, centerz, exit);
            case 1 -> spawnWardenBossRoom(serverLevel, pos, centerx, centerz, exit);
            case 2 -> spawnCultistBossRoom(serverLevel, pos, centerx, centerz);
            case 3 -> spawnTaintBossRoom(serverLevel, pos, centerx, centerz);
        }
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                for (int c = -2; c <= 2; c++) {
                    BlockPos barrier = pos.offset(a, b, c);
                    if (serverLevel.getBlockState(barrier).is(Blocks.BARRIER)) {
                        Effects.smokeSpiral(serverLevel, Vec3.atCenterOf(barrier)).radius(0.5F).start(90).minY(barrier.getY()).color(0x400040).send();
                        serverLevel.removeBlock(barrier, false);
                    }
                }
            }
        }
        serverLevel.removeBlock(pos, false);
    }

    private static void announce(ServerLevel level, BlockPos pos, String key) {
        for (Player player : level.players()) {
            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < MESSAGE_RANGE_SQ) {
                player.sendSystemMessage(Component.translatable(key));
            }
        }
    }

    private static void placePedestal(ServerLevel level, int x, int y, int z) {
        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 3; b++) {
                if (PEDESTAL[a][b] < 0) {
                    level.setBlock(new BlockPos(x - 1 + b, y, z - 1 + a), TCBlocks.STONE_ELDRITCH_TILE.get().defaultBlockState(), 3);
                } else {
                    level.setBlock(new BlockPos(x - 1 + b, y, z - 1 + a), stairState(PEDESTAL[a][b]), 3);
                }
            }
        }
    }

    private static BlockState stairState(int legacyMeta) {
        return EldritchArenaShapes.stairFromLegacyMeta(TCBlocks.STAIRS_ELDRITCH.get(), legacyMeta);
    }

    private static void placeCrystal(Level level, BlockPos pos) {
        BlockState crystal = TCBlocks.ELDRITCH_STONE_CRYSTAL.get().defaultBlockState();
        level.setBlock(pos, Block.updateFromNeighbourShapes(crystal, level, pos), 3);
    }

    private void spawnWardenBossRoom(ServerLevel level, BlockPos lockPos, int cx, int cz, int exit) {
        announce(level, lockPos, "tc.boss.warden");
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        int x2 = x;
        int z2 = z;
        switch (exit) {
            case 2 -> {
                x2 += 8;
                z2 += 8;
            }
            case 3 -> {
                x2 -= 8;
                z2 += 8;
            }
            case 4 -> {
                x2 += 8;
                z2 -= 8;
            }
            case 5 -> {
                x2 -= 8;
                z2 -= 8;
            }
        }
        EldritchArenaShapes.genObelisk(level, x2, y + 4, z);
        EldritchArenaShapes.genObelisk(level, x, y + 4, z2);
        level.setBlock(new BlockPos(x2, y + 2, z), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x, y + 2, z2), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
        RandomSource rand = level.getRandom();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a != 0 && b != 0 && rand.nextFloat() < 0.9F) {
                    level.setBlock(new BlockPos(x2 + a, y + 2, z + b), urnState(rand, 0.1F, 0.3F), 3);
                }
                if (a != 0 && b != 0 && rand.nextFloat() < 0.9F) {
                    level.setBlock(new BlockPos(x + a, y + 2, z2 + b), urnState(rand, 0.1F, 0.3F), 3);
                }
            }
        }
        level.setBlock(new BlockPos(x - 2, y + 3, z - 2), TCBlocks.ELDRITCH_TRAP.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x - 2, y + 3, z + 2), TCBlocks.ELDRITCH_TRAP.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x + 2, y + 3, z + 2), TCBlocks.ELDRITCH_TRAP.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x + 2, y + 3, z - 2), TCBlocks.ELDRITCH_TRAP.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x - 2, y + 2, z - 2), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x - 2, y + 2, z + 2), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x + 2, y + 2, z + 2), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x + 2, y + 2, z - 2), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
        placePedestal(level, x2, y + 2, z2);
        EntityEldritchWarden boss = new EntityEldritchWarden(TCEntities.ELDRITCH_WARDEN.get(), level);
        faceBoss(boss, lockPos, x2 + 0.5, y + 3, z2 + 0.5);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(x2, y + 3, z2)), EntitySpawnReason.EVENT, null);
        boss.setHomeTo(new BlockPos(x, y + 2, z), 32);
        level.addFreshEntity(boss);
    }

    private void spawnGolemBossRoom(ServerLevel level, BlockPos lockPos, int cx, int cz, int exit) {
        announce(level, lockPos, "tc.boss.golem");
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        int x2 = 0;
        int z2 = 0;
        switch (exit) {
            case 2 -> {
                x2 = 8;
                z2 = 8;
            }
            case 3 -> {
                x2 = -8;
                z2 = 8;
            }
            case 4 -> {
                x2 = 8;
                z2 = -8;
            }
            case 5 -> {
                x2 = -8;
                z2 = -8;
            }
        }
        EldritchArenaShapes.genObelisk(level, x + x2, y + 4, z + z2);
        EldritchArenaShapes.genObelisk(level, x - x2, y + 4, z + z2);
        EldritchArenaShapes.genObelisk(level, x + x2, y + 4, z - z2);
        level.setBlock(new BlockPos(x + x2, y + 2, z + z2), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x - x2, y + 2, z + z2), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x + x2, y + 2, z - z2), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
        placePedestal(level, x, y + 2, z);
        RandomSource rand = level.getRandom();
        for (int a = -10; a <= 10; a++) {
            for (int b = -10; b <= 10; b++) {
                if ((a < -2 && b < -2 || a > 2 && b > 2 || a < -2 && b > 2 || a > 2 && b < -2) && rand.nextFloat() < 0.15F && level.isEmptyBlock(new BlockPos(x + a, y + 2, z + b))) {
                    BlockState loot = rand.nextFloat() < 0.3F ? crateState(rand, 0.05F, 0.2F) : urnState(rand, 0.05F, 0.2F);
                    level.setBlock(new BlockPos(x + a, y + 2, z + b), loot, 3);
                }
            }
        }
        EntityEldritchGolem boss = new EntityEldritchGolem(TCEntities.ELDRITCH_GOLEM.get(), level);
        faceBoss(boss, lockPos, x + 0.5, y + 3, z + 0.5);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(x, y + 3, z)), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(boss);
    }

    private void spawnCultistBossRoom(ServerLevel level, BlockPos lockPos, int cx, int cz) {
        announce(level, lockPos, "tc.boss.crimson");
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        RandomSource rand = level.getRandom();
        for (int a = -4; a <= 4; a++) {
            for (int b = -4; b <= 4; b++) {
                if ((Math.abs(a) != 2 && Math.abs(b) != 2 || !rand.nextBoolean()) && (Math.abs(a) != 3 && Math.abs(b) != 3 || !(rand.nextFloat() > 0.33F))
                        && (Math.abs(a) != 4 && Math.abs(b) != 4 || !(rand.nextFloat() > 0.25F))) {
                    level.setBlock(new BlockPos(x + b, y + 1, z + a), TCBlocks.ELDRITCH_DOOR.get().defaultBlockState(), 3);
                }
            }
        }
        for (int a = 0; a < 5; a++) {
            for (int b = 0; b < 5; b++) {
                if (a == 0 || a == 4 || b == 0 || b == 4) {
                    int px = x - 8 + b * 4;
                    int pz = z - 8 + a * 4;
                    level.setBlock(new BlockPos(px, y + 2, pz), TCBlocks.ELDRITCH_STONE.get().defaultBlockState(), 3);
                    placeCrystal(level, new BlockPos(px, y + 3, pz));
                    level.setBlock(new BlockPos(px, y + 4, pz), TCBlocks.SLAB_ARCANE_STONE.get().defaultBlockState(), 3);
                    level.setBlock(new BlockPos(px, y + 10, pz), TCBlocks.ELDRITCH_STONE.get().defaultBlockState(), 3);
                    placeCrystal(level, new BlockPos(px, y + 9, pz));
                    level.setBlock(new BlockPos(px, y + 8, pz), TCBlocks.SLAB_ARCANE_STONE.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP), 3);
                }
            }
        }
        EntityCultistPortalGreater boss = new EntityCultistPortalGreater(TCEntities.CULTIST_PORTAL_GREATER.get(), level);
        boss.snapTo(x + 0.5, y + 2, z + 0.5, 0.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(x, y + 2, z)), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(boss);
    }

    private void spawnTaintBossRoom(ServerLevel level, BlockPos lockPos, int cx, int cz) {
        announce(level, lockPos, "tc.boss.taint");
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        RandomSource rand = level.getRandom();
        for (int a = -12; a <= 12; a++) {
            for (int b = -12; b <= 12; b++) {
                for (int c = 0; c < 9; c++) {
                    BlockPos target = new BlockPos(x + b, y + 2 + c, z + a);
                    if (level.isEmptyBlock(target) && isAdjacentToSolid(level, target) && rand.nextInt(3) != 0) {
                        level.setBlock(target, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), 3);
                    }
                }
                if (rand.nextFloat() < 0.15F) {
                    level.setBlock(new BlockPos(x + b, y + 2, z + a), TCBlocks.TAINT_CRUST.get().defaultBlockState(), 3);
                    if (rand.nextFloat() < 0.2F) {
                        level.setBlock(new BlockPos(x + b, y + 3, z + a), TCBlocks.TAINT_CRUST.get().defaultBlockState(), 3);
                    }
                }
                if ((Math.abs(a) != 4 && Math.abs(b) != 4 || !rand.nextBoolean()) && (Math.abs(a) < 5 && Math.abs(b) < 5 || !(rand.nextFloat() > 0.33F))
                        && (Math.abs(a) < 7 && Math.abs(b) < 7 || !(rand.nextFloat() > 0.25F))) {
                    level.setBlock(new BlockPos(x + b, y + 1, z + a), TCBlocks.TAINT_SOIL.get().defaultBlockState(), 3);
                }
            }
        }
        boolean hard = level.getDifficulty() == Difficulty.HARD;
        spawnTaintacle(level, hard, x + 0.5, y + 3, z + 0.5);
        boolean secondGiant = rand.nextBoolean();
        spawnTaintacle(level, secondGiant, x + 3.5, y + 3, z + 3.5);
        spawnTaintacle(level, !secondGiant, x - 2.5, y + 3, z + 3.5);
        boolean fourthGiant = rand.nextBoolean();
        spawnTaintacle(level, fourthGiant, x + 3.5, y + 3, z - 2.5);
        spawnTaintacle(level, !fourthGiant, x - 2.5, y + 3, z - 2.5);
    }

    private static void spawnTaintacle(ServerLevel level, boolean giant, double x, double y, double z) {
        Monster boss = giant ? new EntityTaintacleGiant(TCEntities.TAINTACLE_GIANT.get(), level) : new EntityTaintacle(TCEntities.TAINTACLE.get(), level);
        boss.snapTo(x, y, z, 0.0F, 0.0F);
        ChampionHelper.makeChampion(boss, true);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(boss);
    }

    private static boolean isAdjacentToSolid(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    private static void faceBoss(Mob boss, BlockPos lockPos, double x, double y, double z) {
        double d0 = lockPos.getX() - x;
        double d1 = lockPos.getY() - (y + boss.getEyeHeight());
        double d2 = lockPos.getZ() - z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float yaw = (float) (Mth.atan2(d2, d0) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Mth.atan2(d1, d3) * 180.0 / Math.PI));
        boss.snapTo(x, y, z, yaw, pitch);
    }

    private static BlockState urnState(RandomSource rand, float rareChance, float uncommonChance) {
        float roll = rand.nextFloat();
        if (roll < rareChance) {
            return TCBlocks.LOOT_URN_RARE.get().defaultBlockState();
        }
        return roll < uncommonChance ? TCBlocks.LOOT_URN_UNCOMMON.get().defaultBlockState() : TCBlocks.LOOT_URN_COMMON.get().defaultBlockState();
    }

    private static BlockState crateState(RandomSource rand, float rareChance, float uncommonChance) {
        float roll = rand.nextFloat();
        if (roll < rareChance) {
            return TCBlocks.LOOT_CRATE_RARE.get().defaultBlockState();
        }
        return roll < uncommonChance ? TCBlocks.LOOT_CRATE_UNCOMMON.get().defaultBlockState() : TCBlocks.LOOT_CRATE_COMMON.get().defaultBlockState();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        count = input.getShortOr("count", (short) -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("count", (short) count);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
