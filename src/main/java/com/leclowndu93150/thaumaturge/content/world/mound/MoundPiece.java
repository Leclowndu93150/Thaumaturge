package com.leclowndu93150.thaumaturge.content.world.mound;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCStructures;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class MoundPiece extends ScatteredFeaturePiece {
    private static final int SURFACE_OFFSET = -10;
    private static final float RARE_CHANCE = 0.1F;
    private static final float UNCOMMON_CHANCE = 0.33F;
    private static final float CRATE_CHANCE = 0.3F;
    private static final int TRAPPED_CHEST_ONE_IN = 3;
    private static final BlockPos URN_A = new BlockPos(9, 2, 7);
    private static final BlockPos URN_B = new BlockPos(9, 2, 11);
    private static final BlockPos CHEST = new BlockPos(10, 2, 9);
    private static final BlockPos SPAWNER_A = new BlockPos(4, 6, 4);
    private static final BlockPos SPAWNER_B = new BlockPos(4, 6, 14);
    private static final BlockPos PORTAL = new BlockPos(9, 2, 9);
    private static final BlockPos NODE = new BlockPos(9, 8, 9);

    private boolean spawnedPortal;

    public MoundPiece(RandomSource random, int west, int north) {
        super(TCStructures.MOUND_PIECE.get(), west, 64, north, MoundLayout.SIZE_X, MoundLayout.SIZE_Y, MoundLayout.SIZE_Z, Direction.SOUTH);
    }

    public MoundPiece(CompoundTag tag) {
        super(TCStructures.MOUND_PIECE.get(), tag);
        this.spawnedPortal = tag.getBooleanOr("Portal", false);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putBoolean("Portal", this.spawnedPortal);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        if (!this.updateAverageGroundHeight(level, chunkBB, SURFACE_OFFSET)) {
            return;
        }
        String data = MoundLayout.DATA;
        for (int i = 0; i < data.length(); i += MoundLayout.ENTRY_CHARS) {
            int x = data.charAt(i) - 'A';
            int y = data.charAt(i + 1) - 'A';
            int z = data.charAt(i + 2) - 'A';
            int id = data.charAt(i + 3) - 'A';
            this.placeBlock(level, stateFor(id).mirror(Mirror.LEFT_RIGHT), x, y, z, chunkBB);
        }
        fillFoundation(level, chunkBB);
        this.placeBlock(level, lootContainer(random), URN_A.getX(), URN_A.getY(), URN_A.getZ(), chunkBB);
        this.placeBlock(level, lootContainer(random), URN_B.getX(), URN_B.getY(), URN_B.getZ(), chunkBB);
        placeChest(level, random, chunkBB);
        placeSpawner(level, chunkBB, SPAWNER_A, EntityType.SKELETON, random);
        placeSpawner(level, chunkBB, SPAWNER_B, EntityType.ZOMBIE, random);
        spawnPortal(level, chunkBB);
        placeNode(level, random, chunkBB);
    }

    private void fillFoundation(WorldGenLevel level, BoundingBox chunkBB) {
        String data = MoundLayout.DATA;
        Map<Integer, Integer> columnMinY = new HashMap<>();
        for (int i = 0; i < data.length(); i += MoundLayout.ENTRY_CHARS) {
            int x = data.charAt(i) - 'A';
            int y = data.charAt(i + 1) - 'A';
            int z = data.charAt(i + 2) - 'A';
            int id = data.charAt(i + 3) - 'A';
            if (!stateFor(id).blocksMotion()) {
                continue;
            }
            columnMinY.merge(x << 8 | z, y, Math::min);
        }
        for (Map.Entry<Integer, Integer> column : columnMinY.entrySet()) {
            int x = column.getKey() >> 8;
            int z = column.getKey() & 0xFF;
            int below = column.getValue() - 1;
            BlockPos worldPos = this.getWorldPos(x, below, z);
            if (!chunkBB.isInside(worldPos)) {
                continue;
            }
            if (!this.isReplaceableByStructures(level.getBlockState(worldPos))) {
                continue;
            }
            this.placeBlock(level, Blocks.DIRT.defaultBlockState(), x, below, z, chunkBB);
            this.fillColumnDown(level, Blocks.STONE.defaultBlockState(), x, below - 1, z, chunkBB);
        }
    }

    private void placeNode(WorldGenLevel level, RandomSource random, BoundingBox chunkBB) {
        BlockPos pos = this.getWorldPos(NODE.getX(), NODE.getY(), NODE.getZ());
        if (chunkBB.isInside(pos)) {
            NodeGenerator.createRandomNodeAt(level, pos, random, false, true, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
        }
    }

    private void placeChest(WorldGenLevel level, RandomSource random, BoundingBox chunkBB) {
        BlockPos pos = this.getWorldPos(CHEST.getX(), CHEST.getY(), CHEST.getZ());
        if (!chunkBB.isInside(pos)) {
            return;
        }
        boolean trapped = random.nextInt(TRAPPED_CHEST_ONE_IN) == 0;
        Block chestBlock = trapped ? Blocks.TRAPPED_CHEST : Blocks.CHEST;
        level.setBlock(pos, chestBlock.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, BuiltInLootTables.SIMPLE_DUNGEON);
        if (trapped) {
            level.setBlock(pos.below(2), Blocks.TNT.defaultBlockState(), 2);
        }
    }

    private void placeSpawner(WorldGenLevel level, BoundingBox chunkBB, BlockPos local, EntityType<?> entityType, RandomSource random) {
        BlockPos pos = this.getWorldPos(local.getX(), local.getY(), local.getZ());
        if (!chunkBB.isInside(pos)) {
            return;
        }
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(entityType, random);
        }
    }

    private void spawnPortal(WorldGenLevel level, BoundingBox chunkBB) {
        if (this.spawnedPortal) {
            return;
        }
        BlockPos pos = this.getWorldPos(PORTAL.getX(), PORTAL.getY(), PORTAL.getZ());
        if (!chunkBB.isInside(pos)) {
            return;
        }
        this.spawnedPortal = true;
        EntityCultistPortalLesser portal = TCEntities.CULTIST_PORTAL_LESSER.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (portal != null) {
            portal.setPersistenceRequired();
            portal.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            portal.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
            level.addFreshEntityWithPassengers(portal);
        }
    }

    private static BlockState lootContainer(RandomSource random) {
        float roll = random.nextFloat();
        boolean crate = random.nextFloat() < CRATE_CHANCE;
        if (roll < RARE_CHANCE) {
            return (crate ? TCBlocks.LOOT_CRATE_RARE : TCBlocks.LOOT_URN_RARE).get().defaultBlockState();
        }
        if (roll < UNCOMMON_CHANCE) {
            return (crate ? TCBlocks.LOOT_CRATE_UNCOMMON : TCBlocks.LOOT_URN_UNCOMMON).get().defaultBlockState();
        }
        return (crate ? TCBlocks.LOOT_CRATE_COMMON : TCBlocks.LOOT_URN_COMMON).get().defaultBlockState();
    }

    private static BlockState stateFor(int id) {
        return switch (id) {
            case MoundLayout.COBBLESTONE -> Blocks.COBBLESTONE.defaultBlockState();
            case MoundLayout.DIRT -> Blocks.DIRT.defaultBlockState();
            case MoundLayout.GRASS_BLOCK -> Blocks.GRASS_BLOCK.defaultBlockState();
            case MoundLayout.MOSSY_COBBLESTONE -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case MoundLayout.SHORT_GRASS -> Blocks.SHORT_GRASS.defaultBlockState();
            case MoundLayout.STAIRS_EAST -> stairs(Direction.EAST, Half.BOTTOM);
            case MoundLayout.STAIRS_NORTH -> stairs(Direction.NORTH, Half.BOTTOM);
            case MoundLayout.STAIRS_WEST -> stairs(Direction.WEST, Half.BOTTOM);
            case MoundLayout.STAIRS_SOUTH -> stairs(Direction.SOUTH, Half.BOTTOM);
            case MoundLayout.STAIRS_EAST_TOP -> stairs(Direction.EAST, Half.TOP);
            case MoundLayout.STAIRS_WEST_TOP -> stairs(Direction.WEST, Half.TOP);
            case MoundLayout.STAIRS_SOUTH_TOP -> stairs(Direction.SOUTH, Half.TOP);
            case MoundLayout.STAIRS_NORTH_TOP -> stairs(Direction.NORTH, Half.TOP);
            case MoundLayout.LADDER_EAST -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST);
            case MoundLayout.IRON_BARS -> Blocks.IRON_BARS.defaultBlockState();
            case MoundLayout.CHISELED_STONE_BRICKS -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            default -> Blocks.AIR.defaultBlockState();
        };
    }

    private static BlockState stairs(Direction facing, Half half) {
        return Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }
}
