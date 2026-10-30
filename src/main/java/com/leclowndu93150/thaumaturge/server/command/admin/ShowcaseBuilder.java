package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.recipe.dust.MultiblockMatcher;
import com.leclowndu93150.thaumaturge.content.recipe.dust.RotatedBlueprint;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

final class ShowcaseBuilder {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    private static final int BLOCK_COLUMNS = 16;
    private static final int BLOCK_SPACING = 4;
    private static final int ENTITY_SPACING = 6;
    private static final int SIDE_MARGIN = 2;
    private static final int FRONT_MARGIN = 2;
    private static final int WALL_GAP = 3;
    private static final int WALL_BORDER = 1;
    private static final int MIN_CLEAR_HEIGHT = 6;
    private static final int LIGHT_SPACING = 6;
    private static final int LIGHT_HEIGHT = 4;
    private static final int LIGHT_LAYER_STEP = 5;
    private static final Direction[] SUPPORT_SIDES = {
        Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP
    };

    private final ServerLevel level;
    private final BlockPos origin;
    private final Direction forward;
    private final Direction right;
    private final List<Block> blocks;
    private final List<Item> items;
    private final List<EntityType<?>> entities;
    private final List<BlockState> supports;
    private final int width;
    private final int wallRow;
    private final int entityColumns;
    private final int entityStartRow;
    private final int beanShowcaseRow;
    private final int frameColumns;
    private final int frameRows;
    private final int clearHeight;

    ShowcaseBuilder(
            ServerLevel level,
            BlockPos origin,
            Direction forward,
            List<Block> blocks,
            List<Item> items,
            List<EntityType<?>> entities) {
        this.level = level;
        this.origin = origin;
        this.forward = forward;
        this.right = forward.getClockWise();
        this.blocks = blocks;
        this.items = items;
        this.entities = entities;
        this.supports = List.of(
                Blocks.SMOOTH_STONE.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.SAND.defaultBlockState(),
                TTBlocks.LOG_GREATWOOD.get().defaultBlockState(),
                TTBlocks.SMELTER_BASIC.get().defaultBlockState());
        int blockRows = Mth.positiveCeilDiv(blocks.size(), BLOCK_COLUMNS);
        this.width = SIDE_MARGIN * 2 + (BLOCK_COLUMNS - 1) * BLOCK_SPACING + 1;
        this.beanShowcaseRow = FRONT_MARGIN + Math.max(0, blockRows - 1) * BLOCK_SPACING + 2;
        this.entityStartRow = beanShowcaseRow + ENTITY_SPACING;
        this.entityColumns = (width - SIDE_MARGIN * 2 - 1) / ENTITY_SPACING + 1;
        int entityRows = Mth.positiveCeilDiv(entities.size(), entityColumns);
        this.wallRow = entityStartRow + Math.max(0, entityRows - 1) * ENTITY_SPACING + WALL_GAP + 1;
        this.frameColumns = width - WALL_BORDER * 2;
        this.frameRows = Mth.positiveCeilDiv(items.size(), frameColumns);
        int entityHeight = entities.stream()
                .mapToInt(type -> Mth.ceil(type.getHeight()) + 2)
                .max()
                .orElse(0);
        this.clearHeight = Math.max(Math.max(MIN_CLEAR_HEIGHT, entityHeight), frameRows + WALL_BORDER * 2);
    }

    boolean fitsHeight() {
        return origin.getY() - 1 >= level.getMinBuildHeight()
                && origin.getY() + clearHeight <= level.getMaxBuildHeight() - 1;
    }

    boolean isLoaded() {
        BlockPos near = at(0, 0, 0);
        BlockPos far = at(width - 1, wallRow, 0);
        int minX = SectionPos.blockToSectionCoord(Math.min(near.getX(), far.getX()));
        int maxX = SectionPos.blockToSectionCoord(Math.max(near.getX(), far.getX()));
        int minZ = SectionPos.blockToSectionCoord(Math.min(near.getZ(), far.getZ()));
        int maxZ = SectionPos.blockToSectionCoord(Math.max(near.getZ(), far.getZ()));
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!level.hasChunk(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    void build() {
        removeDisplayEntities();
        AABB area = AABB.encapsulatingFullBlocks(at(0, 0, -1), at(width - 1, wallRow, clearHeight));
        Set<UUID> existing = level.getEntities((Entity) null, area, entity -> true).stream()
                .map(Entity::getUUID)
                .collect(Collectors.toSet());
        clearArea();
        // Taint removal can spawn crawlers that would obstruct the new block placements.
        for (LivingEntity entity :
                level.getEntitiesOfClass(LivingEntity.class, area, entity -> !existing.contains(entity.getUUID()))) {
            entity.discard();
        }
        placeLights();
        placeBlocks();
        placeManaPodStages();
        placeEntities();
        buildWall();
        hangFrames();
    }

    private void removeDisplayEntities() {
        AABB area = AABB.encapsulatingFullBlocks(at(0, 0, -1), at(width - 1, wallRow, clearHeight));
        for (Entity entity : level.getEntities(
                (Entity) null, area, entity -> entity instanceof HangingEntity || ShowcaseEntities.isDisplay(entity))) {
            entity.discard();
        }
    }

    private void clearArea() {
        BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        // Removing formed controllers can restore source blocks into cells already visited.
        for (int pass = 0; pass < 2; pass++) {
            for (int column = 0; column < width; column++) {
                for (int row = 0; row <= wallRow; row++) {
                    if (pass == 0) {
                        set(at(column, row, -1), floor);
                    }
                    for (int up = 0; up < clearHeight; up++) {
                        set(at(column, row, up), air);
                    }
                }
            }
        }
    }

    private void placeLights() {
        BlockState light = Blocks.LIGHT.defaultBlockState();
        for (int column = LIGHT_SPACING / 2; column < width; column += LIGHT_SPACING) {
            for (int row = LIGHT_SPACING / 2; row < wallRow - 1; row += LIGHT_SPACING) {
                for (int up = LIGHT_HEIGHT; up < clearHeight; up += LIGHT_LAYER_STEP) {
                    set(at(column, row, up), light);
                }
            }
        }
    }

    private void placeBlocks() {
        for (int i = 0; i < blocks.size(); i++) {
            BlockPos pos = at(
                    SIDE_MARGIN + i % BLOCK_COLUMNS * BLOCK_SPACING,
                    FRONT_MARGIN + i / BLOCK_COLUMNS * BLOCK_SPACING,
                    0);
            BlockState state = blocks.get(i).defaultBlockState();
            // The symmetric altar's explicit pillar states use the authored south-facing layout.
            if (state.is(TTBlocks.INFUSION_MATRIX) && placeBlueprint("infusion_altar", pos, Direction.SOUTH)) {
                continue;
            }
            if (state.is(TTBlocks.THAUMATORIUM) && placeBlueprint("thaumatorium", pos, forward.getOpposite())) {
                continue;
            }
            if (state.is(TTBlocks.ARCANE_WORKBENCH_CHARGER)) {
                set(pos, TTBlocks.ARCANE_WORKBENCH.get().defaultBlockState());
                pos = pos.above();
            }
            if (!state.getFluidState().isEmpty()) {
                set(pos.below(), state);
                continue;
            }
            ItemStack stack = new ItemStack(state.getBlock());
            if (stack.getItem() instanceof BlockItem item) {
                for (Direction face : new Direction[] {
                    Direction.UP, forward.getOpposite(), forward, right, right.getOpposite(), Direction.DOWN
                }) {
                    DirectionalPlaceContext context = new DirectionalPlaceContext(level, pos, forward, stack, face);
                    BlockState placement = state.getBlock().getStateForPlacement(context);
                    if (placement == null) {
                        support(pos, state);
                        placement = state.getBlock().getStateForPlacement(context);
                    }
                    if (placement != null && !placement.canSurvive(level, pos)) {
                        support(pos, placement);
                    }
                    if (item.place(context).consumesAction()) {
                        break;
                    }
                }
                BlockState placed = level.getBlockState(pos);
                if (placed.hasProperty(BlockStateProperties.PERSISTENT)) {
                    set(pos, placed.setValue(BlockStateProperties.PERSISTENT, true));
                }
            }
        }
    }

    private void placeManaPodStages() {
        int center = width / 2;
        for (int age = 0; age <= BlockEntityManaPod.MAX_AGE; age++) {
            BlockPos logPos = at(center + age * 2 - BlockEntityManaPod.MAX_AGE, beanShowcaseRow, 3);
            set(
                    logPos,
                    TTBlocks.LOG_GREATWOOD
                            .get()
                            .defaultBlockState()
                            .setValue(BlockStateProperties.AXIS, right.getAxis()));

            BlockPos podPos = logPos.below();
            BlockState podState = TTBlocks.MANA_POD.get().defaultBlockState().setValue(BlockManaPod.AGE, age);
            set(podPos, podState);
            if (!(level.getBlockEntity(podPos) instanceof BlockEntityManaPod pod)) {
                throw new IllegalStateException("Cannot create Mana Pod showcase block entity");
            }
            pod.setShowcase();
            pod.setAspect(TTAspects.IGNIS);
        }
    }

    private boolean placeBlueprint(String name, BlockPos center, Direction facing) {
        Blueprint blueprint =
                level.registryAccess().registryOrThrow(Blueprint.REGISTRY_KEY).get(TTIds.rl(name));
        if (blueprint == null) {
            return false;
        }
        RotatedBlueprint rotated = new RotatedBlueprint(blueprint, MultiblockMatcher.rotationsFor(facing));
        for (var cell : rotated.cells()) {
            BlueprintTarget target = cell.part().target();
            if (target instanceof BlueprintTarget.Air) {
                continue;
            }
            BlockState state = cell.part().source().getState();
            if (target instanceof BlueprintTarget.BlockTarget blockTarget) {
                state = blockTarget.block().defaultBlockState();
                Direction targetFacing = blockTarget.opposite() ? facing.getOpposite() : facing;
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, targetFacing);
                }
            } else if (target instanceof BlueprintTarget.StateTarget stateTarget) {
                state = stateTarget.state();
            }
            BlockPos offset = cell.offset();
            set(
                    center.offset(
                            offset.getX() - rotated.width() / 2, offset.getY(), offset.getZ() - rotated.depth() / 2),
                    state);
        }
        return true;
    }

    private void support(BlockPos pos, BlockState state) {
        for (Direction side : SUPPORT_SIDES) {
            BlockPos supportPos = pos.relative(side);
            BlockState previous = level.getBlockState(supportPos);
            for (BlockState candidate : supports) {
                if (side.getAxis().isHorizontal() && candidate.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    candidate = candidate.setValue(BlockStateProperties.HORIZONTAL_FACING, side);
                }
                set(supportPos, candidate);
                if (state.canSurvive(level, pos)) {
                    return;
                }
            }
            set(supportPos, previous);
        }
    }

    private void buildWall() {
        BlockState wall = Blocks.POLISHED_ANDESITE.defaultBlockState();
        for (int column = 0; column < width; column++) {
            for (int up = 0; up < frameRows + WALL_BORDER * 2; up++) {
                set(at(column, wallRow, up), wall);
            }
        }
    }

    private void hangFrames() {
        Direction facing = forward.getOpposite();
        for (int i = 0; i < items.size(); i++) {
            ItemFrame frame = new ItemFrame(
                    level, at(WALL_BORDER + i % frameColumns, wallRow - 1, frameRows - i / frameColumns), facing);
            frame.setItem(new ItemStack(items.get(i)), false);
            frame.setInvulnerable(true);
            level.addFreshEntity(frame);
        }
    }

    private void placeEntities() {
        for (int i = 0; i < entities.size(); i++) {
            EntityType<?> type = entities.get(i);
            Entity entity = type.create(level);
            if (entity == null) {
                throw new IllegalStateException("Cannot create showcase entity " + EntityType.getKey(type));
            }
            BlockPos pos = at(
                    SIDE_MARGIN + i % entityColumns * ENTITY_SPACING,
                    entityStartRow + i / entityColumns * ENTITY_SPACING,
                    0);
            entity.moveTo(
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    forward.getOpposite().toYRot(),
                    0.0F);
            ShowcaseEntities.freeze(entity);
            if (entity instanceof EntityFluxRift rift) {
                rift.reseed(2319160);
                rift.resize(15);
                double rise = Math.max(0.0, pos.getY() + 0.25 - rift.getBoundingBox().minY);
                rift.setPos(rift.getX(), rift.getY() + rise, rift.getZ());
            }
            if (!level.addFreshEntity(entity)) {
                throw new IllegalStateException("Cannot add showcase entity " + EntityType.getKey(type));
            }
        }
    }

    private BlockPos at(int column, int row, int up) {
        return origin.relative(right, column).relative(forward, row).above(up);
    }

    private void set(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, FLAGS);
    }
}
