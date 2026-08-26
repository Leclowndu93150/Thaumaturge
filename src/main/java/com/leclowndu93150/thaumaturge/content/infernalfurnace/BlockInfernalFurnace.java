package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockInfernalFurnace extends BaseEntityBlock {
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BlockEntityInfernalFurnace furnace) {
                for (int i = 0; i < furnace.inventory().getSlots(); i++) {
                    ItemStack resource = furnace.inventory().getStackInSlot(i);
                    if (!resource.isEmpty()) {
                        Containers.dropItemStack(
                                level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, resource.copy());
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static final MapCodec<BlockInfernalFurnace> CODEC = simpleCodec(BlockInfernalFurnace::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final int BLAZE_REGEN_TICKS = 6000;
    private static final int BLAZE_RESIST_TICKS = 12000;

    public BlockInfernalFurnace(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.box(0, 0, 0, 1, 0.5, 1);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    protected static void destroyFurnace(LevelAccessor level, BlockPos pos, BlockState state, BlockPos startpos) {
        if (level.isClientSide()) {
            return;
        }
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                for (int c = -1; c <= 1; c++) {
                    BlockPos shellPos = pos.offset(a, b, c);
                    if (shellPos.equals(startpos)) {
                        continue;
                    }
                    BlockState bs = level.getBlockState(shellPos);
                    if (bs.is(TCBlocks.NETHER_BRICKS_PLACEHOLDER)) {
                        level.setBlock(shellPos, Blocks.NETHER_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                    } else if (bs.is(TCBlocks.OBSIDIAN_PLACEHOLDER)) {
                        level.setBlock(shellPos, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        BlockPos barsPos = pos.relative(state.getValue(FACING).getOpposite());
        if (level.isEmptyBlock(barsPos) && !barsPos.equals(startpos)) {
            level.setBlock(barsPos, Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (!ThaumaturgeServerConfig.INFERNAL_FURNACE_TURN_TO_BLAZE.get()) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            if (level instanceof ServerLevel serverLevel) {
                Blaze blaze = EntityType.BLAZE.create(serverLevel);
                if (blaze != null) {
                    blaze.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
                    blaze.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BLAZE_REGEN_TICKS, 2));
                    blaze.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BLAZE_RESIST_TICKS, 0));
                    serverLevel.addFreshEntity(blaze);
                }
            }
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        destroyFurnace(level, pos, state, pos);
        super.destroy(level, pos, state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityInfernalFurnace(blockPos, blockState);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.box(-1, -1, -1, 2, 2, 2);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.INFERNAL_FURNACE.get(), BlockEntityInfernalFurnace::staticTick);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.getX() < pos.getX() + 0.3F)
            entity.setDeltaMovement(entity.getDeltaMovement().add(1.0E-4F, 0, 0));
        if (entity.getX() > pos.getX() + 0.7F)
            entity.setDeltaMovement(entity.getDeltaMovement().add(-1.0E-4F, 0, 0));
        if (entity.getZ() < pos.getZ() + 0.3F)
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0, 1.0E-4F));
        if (entity.getZ() > pos.getZ() + 0.7F)
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0, -1.0E-4F));

        if (!level.isClientSide() && entity.tickCount % 10 == 0) {
            if (entity instanceof ItemEntity it) {
                entity.setDeltaMovement(entity.getDeltaMovement().with(Direction.Axis.Y, 0.025F));
                if (entity.onGround()) {
                    BlockEntityInfernalFurnace furnace = (BlockEntityInfernalFurnace) level.getBlockEntity(pos);
                    if (furnace != null) {
                        it.setItem(furnace.addItemsToInventory(it.getItem()));
                    }
                }
            } else if (entity instanceof LivingEntity lv && !lv.fireImmune()) {
                entity.lavaHurt();
                lv.igniteForSeconds(10);
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
