package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import static com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.blocks.ILabelable;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockAlembic extends BaseEntityBlock implements ILabelable {

    private static final MapCodec<BlockAlembic> CODEC = simpleCodec(BlockAlembic::new);

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public BlockAlembic(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (level instanceof Level lvl) {
            if (directionToNeighbour.getStepY() != 0)
                return state;
            boolean connect = canConnectTo(lvl, neighbourPos, directionToNeighbour.getOpposite());
            return state.setValue(propertyFor(directionToNeighbour), connect);
        }
        return state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return recomputeConnections(state, level, pos, Direction.UP, Direction.DOWN);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityAlembic(blockPos, blockState);
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction face, ItemStack stack) {
        if (!(player.level().getBlockEntity(pos) instanceof BlockEntityAlembic alembic))
            return false;
        if (!(stack.getItem() instanceof ILabel label))
            return false;
        if (face.getStepY() != 0)
            return false;
        if (alembic.aspectFilterKey() != null)
            return false;
        ResourceKey<IAspect> labelAspect = label.getFilteredAspect(stack);

        if (alembic.amount() == 0 && labelAspect == null)
            return false;

        ResourceKey<IAspect> aspect = null;
        if (alembic.amount() == 0 && labelAspect != null)
            aspect = labelAspect;
        if (alembic.amount() > 0)
            aspect = alembic.aspectKey();

        if (aspect == null)
            return false;
        if (labelAspect != null && !labelAspect.equals(aspect))
            return false;

        BlockState state = player.level().getBlockState(pos);
        setPlacedBy(player.level(), pos, state, player, stack);
        alembic.setAspectFilter(aspect);
        alembic.setFacing(face);
        alembic.setChanged();
        alembic.syncToClient();
        player.level().playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic))
            return InteractionResult.PASS;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!player.isCrouching())
            return InteractionResult.PASS;

        if (alembic.aspectFilterKey() != null && hitResult.getDirection() == alembic.facing()) {
            alembic.setAspectFilter(null);
            alembic.setChanged();
            alembic.syncToClient();
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            BlockAlembic.popResourceFromFace(level, pos, hitResult.getDirection(), new ItemStack(TCItems.LABEL.get()));
        } else {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
            float pitch = 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F;
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5F, pitch);
            AuraHelper.polluteAura(level, pos, alembic.amount(), true);
            alembic.clearAspect();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.box(0.125, 0, 0.125, 0.875, 1, 0.875);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic))
            return 0;
        float r = (float) alembic.amount() / BlockEntityAlembic.CAPACITY;
        return Mth.floor(r * 14) + (alembic.amount() > 0 ? 1 : 0);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide())
            return super.playerWillDestroy(level, pos, state, player);
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic))
            return super.playerWillDestroy(level, pos, state, player);
        if (alembic.aspectFilterKey() != null) {
            popResource(level, pos, new ItemStack(TCItems.LABEL.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
