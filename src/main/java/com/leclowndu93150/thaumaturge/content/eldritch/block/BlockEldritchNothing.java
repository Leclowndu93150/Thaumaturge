package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchNothing extends Block implements EntityBlock {
    public static final MapCodec<BlockEldritchNothing> CODEC = simpleCodec(BlockEldritchNothing::new);
    public static final BooleanProperty EXPOSED = BooleanProperty.create("exposed");

    private static final VoxelShape COLLISION = Block.box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);
    private static final float VOID_DAMAGE = 8.0F;
    private static final int GRACE_TICKS = 20;

    public BlockEldritchNothing(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(EXPOSED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<BlockEldritchNothing> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EXPOSED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchNothing(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean exposed = isExposed(level, pos);
        if (state.getValue(EXPOSED) != exposed) {
            level.setBlock(pos, state.setValue(EXPOSED, exposed), 3);
        }
    }

    private static boolean isExposed(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            cursor.setWithOffset(pos, dir);
            BlockState neighbor = level.getBlockState(cursor);
            if (!neighbor.isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide() || entity.tickCount <= GRACE_TICKS) {
            return;
        }
        if (entity instanceof Player player && player.getAbilities().invulnerable) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            entity.hurtServer(serverLevel, serverLevel.damageSources().fellOutOfWorld(), VOID_DAMAGE);
        }
    }
}
