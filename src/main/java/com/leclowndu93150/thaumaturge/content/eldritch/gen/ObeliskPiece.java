package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchAltar;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class ObeliskPiece extends ScatteredFeaturePiece {
    public static final int SIZE_XZ = 7;
    public static final int SIZE_Y = 13;

    private static final int SURFACE_OFFSET = -4;
    private static final int GROUND = 4;
    private static final int CENTER = 3;
    private static final float OBSIDIAN_CHANCE_ONE_IN = 4;

    public ObeliskPiece(RandomSource random, int west, int north) {
        super(TCStructures.ELDRITCH_OBELISK_PIECE.get(), west, 64, north, SIZE_XZ, SIZE_Y, SIZE_XZ, Direction.SOUTH);
    }

    public ObeliskPiece(CompoundTag tag) {
        super(TCStructures.ELDRITCH_OBELISK_PIECE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        if (!this.updateAverageGroundHeight(level, chunkBB, SURFACE_OFFSET)) {
            return;
        }
        for (int x = 0; x < SIZE_XZ; x++) {
            for (int z = 0; z < SIZE_XZ; z++) {
                boolean corner = (x == 0 || x == SIZE_XZ - 1) && (z == 0 || z == SIZE_XZ - 1);
                if (corner) {
                    continue;
                }
                for (int q = 0; q <= GROUND; q++) {
                    this.placeBlock(level, platformState(random), x, q, z, chunkBB);
                }
                for (int q = GROUND + 1; q < SIZE_Y; q++) {
                    this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, q, z, chunkBB);
                }
            }
        }
        this.placeBlock(level, TCBlocks.OBSIDIAN_TILE.get().defaultBlockState(), CENTER, GROUND, CENTER, chunkBB);
        this.placeBlock(level, TCBlocks.ELDRITCH_ALTAR.get().defaultBlockState(), CENTER, GROUND + 1, CENTER, chunkBB);
        this.placeBlock(level, TCBlocks.ELDRITCH_OBELISK.get().defaultBlockState(), CENTER, GROUND + 3, CENTER, chunkBB);
        for (int q = GROUND + 4; q <= GROUND + 7; q++) {
            this.placeBlock(level, TCBlocks.ELDRITCH_PILLAR.get().defaultBlockState(), CENTER, q, CENTER, chunkBB);
        }
        for (int x = 0; x < SIZE_XZ; x++) {
            for (int z = 0; z < SIZE_XZ; z++) {
                boolean edge = ((x == 0 || x == SIZE_XZ - 1) && Math.abs((z - CENTER) % 2) == 1 || (z == 0 || z == SIZE_XZ - 1) && Math.abs((x - CENTER) % 2) == 1)
                        && Math.abs(x - CENTER) != Math.abs(z - CENTER);
                if (edge) {
                    this.placeBlock(level, TCBlocks.OBSIDIAN_TILE.get().defaultBlockState(), x, GROUND, z, chunkBB);
                    this.placeBlock(level, TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), x, GROUND + 1, z, chunkBB);
                }
            }
        }
        configureAltar(level, random, chunkBB);
        placeNode(level, random, chunkBB);
    }

    private void placeNode(WorldGenLevel level, RandomSource random, BoundingBox chunkBB) {
        BlockPos pos = this.getWorldPos(CENTER, GROUND + 2, CENTER);
        if (chunkBB.isInside(pos)) {
            NodeGenerator.createRandomNodeAt(level, pos, random, false, true, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
        }
    }

    private void configureAltar(WorldGenLevel level, RandomSource random, BoundingBox chunkBB) {
        BlockPos altarPos = this.getWorldPos(CENTER, GROUND + 1, CENTER);
        if (!chunkBB.isInside(altarPos)) {
            return;
        }
        if (!(level.getBlockEntity(altarPos) instanceof BlockEntityEldritchAltar altar)) {
            return;
        }
        int roll = random.nextInt(10);
        if (roll >= 1 && roll <= 4) {
            altar.setSpawner(true);
            altar.setSpawnType(BlockEntityEldritchAltar.SPAWN_CULTIST);
            placeBanners(level, chunkBB);
        } else if (roll == 6 || roll == 7) {
            altar.setSpawner(true);
            altar.setSpawnType(BlockEntityEldritchAltar.SPAWN_GUARDIAN);
        }
    }

    private void placeBanners(WorldGenLevel level, BoundingBox chunkBB) {
        placeBanner(level, chunkBB, CENTER, CENTER - 3, 0);
        placeBanner(level, chunkBB, CENTER, CENTER + 3, 8);
        placeBanner(level, chunkBB, CENTER - 3, CENTER, 4);
        placeBanner(level, chunkBB, CENTER + 3, CENTER, 12);
    }

    private void placeBanner(WorldGenLevel level, BoundingBox chunkBB, int x, int z, int rotation) {
        BlockState banner = TCBlocks.BANNER_CRIMSON_CULT.get().defaultBlockState().setValue(BannerStandingBlock.ROTATION, rotation);
        this.placeBlock(level, banner, x, GROUND + 1, z, chunkBB);
    }

    private static BlockState platformState(RandomSource random) {
        return random.nextInt((int) OBSIDIAN_CHANCE_ONE_IN) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : TCBlocks.OBSIDIAN_TILE.get().defaultBlockState();
    }
}
