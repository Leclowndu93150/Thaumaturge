package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class HilltopStonesFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_Y = 85;
    private static final int RING_RADIUS = 3;
    private static final int SAMPLE_RADIUS = 2;
    private static final int MAX_AIR_GAP = 2;
    private static final int FILL_DEPTH = 4;
    private static final int PILLAR_STOP_MIN_HEIGHT = 2;
    private static final int VINE_ONE_IN = 3;
    private static final int VINE_LENGTH = 4;
    private static final int NODE_HEIGHT = 5;
    private static final int PLACE_FLAGS = 3;

    public HilltopStonesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int i = origin.getX();
        int j = origin.getY();
        int k = origin.getZ();
        if (!validSpawn(level, i - SAMPLE_RADIUS, j, k - SAMPLE_RADIUS)
                || !validSpawn(level, i, j, k)
                || !validSpawn(level, i + SAMPLE_RADIUS, j, k)
                || !validSpawn(level, i + SAMPLE_RADIUS, j, k + SAMPLE_RADIUS)
                || !validSpawn(level, i, j, k + SAMPLE_RADIUS)) {
            return false;
        }
        BlockState fill = groundState(level, i, j, k);
        boolean vines = !level.getBiome(origin).value().coldEnoughToSnow(origin);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = i - RING_RADIUS; x <= i + RING_RADIUS; x++) {
            for (int z = k - RING_RADIUS; z <= k + RING_RADIUS; z++) {
                if ((x == i - RING_RADIUS || x == i + RING_RADIUS) && (z == k - RING_RADIUS || z == k + RING_RADIUS)) {
                    continue;
                }
                level.setBlock(cursor.set(x, j, z), floorState(random), PLACE_FLAGS);
                boolean stop = false;
                for (int y = 1; y <= FILL_DEPTH; y++) {
                    if (j - y >= level.getMinBuildHeight()) {
                        BlockState below = level.getBlockState(cursor.set(x, j - y, z));
                        if (below.isAir()
                                || below.is(Blocks.SNOW)
                                || below.is(Blocks.SHORT_GRASS)
                                || below.is(BlockTags.SMALL_FLOWERS)
                                || below.is(TCBlockTags.MAGICAL_PLANTS)) {
                            level.setBlock(cursor, fill, PLACE_FLAGS);
                        }
                    }
                    if (x == i && z == k && y == 1) {
                        placeCenter(level, random, i, j, k);
                    }
                    if (!stop && isPillarColumn(x, z, i, k)) {
                        level.setBlock(
                                cursor.set(x, j + y, z),
                                TCBlocks.OBSIDIAN_TOTEM.get().defaultBlockState(),
                                PLACE_FLAGS);
                        if (y >= PILLAR_STOP_MIN_HEIGHT && random.nextBoolean()) {
                            stop = true;
                            if (vines) {
                                growVinesAround(level, random, x, j + y, z);
                            }
                        }
                    }
                }
                if (isPillarColumn(x, z, i, k)) {
                    ObsidianTotemFeature.reshapeTotems(level, new BlockPos(x, j, z), FILL_DEPTH);
                }
            }
        }
        NodeGenerator.createRandomNodeAt(
                level,
                new BlockPos(i, j + NODE_HEIGHT, k),
                random,
                false,
                true,
                false,
                NodeGenerator.DEFAULT_SPECIAL_RARITY,
                NodeGenerator.DEFAULT_BASE_AURA);
        return true;
    }

    private static boolean isPillarColumn(int x, int z, int i, int k) {
        return (x == i - RING_RADIUS || x == i + RING_RADIUS) && Math.abs((z - k) % 2) == 1
                || (z == k - RING_RADIUS || z == k + RING_RADIUS) && Math.abs((x - i) % 2) == 1;
    }

    private static BlockState floorState(RandomSource random) {
        return random.nextBoolean()
                ? TCBlocks.OBSIDIAN_TILE.get().defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
    }

    private void placeCenter(WorldGenLevel level, RandomSource random, int i, int j, int k) {
        level.setBlock(new BlockPos(i, j + 1, k), TCBlocks.OBSIDIAN_TILE.get().defaultBlockState(), PLACE_FLAGS);
        BlockPos chestPos = new BlockPos(i, j + 2, k);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), PLACE_FLAGS);
        RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, BuiltInLootTables.SIMPLE_DUNGEON);
        BlockPos spawnerPos = new BlockPos(i, j, k);
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), PLACE_FLAGS);
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(TCEntities.WISP.get(), random);
        }
    }

    private static void growVinesAround(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        tryGrowVines(level, random, new BlockPos(x - 1, y, z), VineBlock.EAST);
        tryGrowVines(level, random, new BlockPos(x + 1, y, z), VineBlock.WEST);
        tryGrowVines(level, random, new BlockPos(x, y, z - 1), VineBlock.SOUTH);
        tryGrowVines(level, random, new BlockPos(x, y, z + 1), VineBlock.NORTH);
    }

    private static void tryGrowVines(WorldGenLevel level, RandomSource random, BlockPos pos, BooleanProperty face) {
        if (random.nextInt(VINE_ONE_IN) != 0 || !level.getBlockState(pos).isAir()) {
            return;
        }
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(face, true);
        level.setBlock(pos, vine, PLACE_FLAGS);
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int a = 0; a < VINE_LENGTH; a++) {
            cursor.move(Direction.DOWN);
            if (!level.getBlockState(cursor).isAir()) {
                break;
            }
            level.setBlock(cursor, vine, PLACE_FLAGS);
        }
    }

    private static boolean validSpawn(WorldGenLevel level, int x, int y, int z) {
        if (y < MIN_Y) {
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
        int distanceToAir = 0;
        while (distanceToAir <= MAX_AIR_GAP
                && !level.getBlockState(cursor.setY(y + distanceToAir)).isAir()) {
            distanceToAir++;
        }
        if (distanceToAir > MAX_AIR_GAP) {
            return false;
        }
        int groundY = y + distanceToAir - 1;
        BlockState ground = level.getBlockState(cursor.setY(groundY));
        BlockState above = level.getBlockState(cursor.setY(groundY + 1));
        BlockState below = level.getBlockState(cursor.setY(groundY - 1));
        if (!above.isAir()) {
            return false;
        }
        if (isValidGround(ground)) {
            return true;
        }
        return (ground.is(Blocks.SNOW) || ground.is(Blocks.SHORT_GRASS) || ground.is(BlockTags.SMALL_FLOWERS) || ground.is(TCBlockTags.MAGICAL_PLANTS)) && isValidGround(below);
    }

    private static boolean isValidGround(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT);
    }

    private static BlockState groundState(WorldGenLevel level, int x, int y, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y - 1, z);
        while (cursor.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(cursor);
            if (isValidGround(state)) {
                return state;
            }
            if (!state.isAir() && !state.is(Blocks.SNOW) && !state.is(Blocks.SHORT_GRASS) && !state.is(TCBlockTags.MAGICAL_PLANTS)) {
                break;
            }
            cursor.move(Direction.DOWN);
        }
        return Blocks.DIRT.defaultBlockState();
    }
}
