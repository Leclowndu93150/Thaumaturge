package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.blocks.ILabelable;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStreamPort;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockAlembic;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockJar extends BaseEntityBlock implements ILabelable, IEssentiaStreamPort, IEssentiaJar {
    public static final MapCodec<BlockJar> CODEC = simpleCodec(BlockJar::new);

    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);
    private static final double MOUTH_HEIGHT = 0.8;
    private static final double MOUTH_CLEARANCE = 1.4;

    public BlockJar(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockJar> codec() {
        return CODEC;
    }

    @Override
    public StreamPort essentiaStreamPort(
            BlockGetter level, BlockPos pos, BlockState state, Vec3 farEnd, boolean outgoing) {
        Vec3 base = Vec3.atBottomCenterOf(pos);
        return new StreamPort(base.add(0.0, MOUTH_HEIGHT, 0.0), base.add(0.0, MOUTH_CLEARANCE, 0.0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJar(pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide()) return super.playerWillDestroy(level, pos, state, player);
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar))
            return super.playerWillDestroy(level, pos, state, player);
        if (jar.isBlocked()) {
            popResource(level, pos, new ItemStack(TCItems.JAR_BRACE.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return 0;
        int amt = jar.amount();
        if (amt <= 0) return 0;
        float ratio = amt / (float) BlockEntityJar.CAPACITY;
        return Math.min(15, (int) Math.floor(ratio * 14.0F) + 1);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (stack.is(TCItems.JAR_BRACE.get())) {
            if (jar.isBlocked()) return InteractionResult.TRY_WITH_EMPTY_HAND;
            jar.setBraced(true);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, TCSounds.KEY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!player.isCrouching()) return InteractionResult.PASS;

        if (jar.aspectFilterKey() != null && hitResult.getDirection() == jar.facing()) {
            jar.setAspectFilter(null);
            jar.setChanged();
            jar.syncToClient();
            level.playSound(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    TCSounds.PAGE.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F);
            BlockAlembic.popResourceFromFace(level, pos, hitResult.getDirection(), new ItemStack(TCItems.LABEL.get()));
        } else {
            level.playSound(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    TCSounds.JAR.get(),
                    SoundSource.BLOCKS,
                    0.4F,
                    1.0F);
            float pitch =
                    1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F;
            level.playSound(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.BOTTLE_FILL,
                    SoundSource.BLOCKS,
                    0.5F,
                    pitch);
            AuraHelper.polluteAura(level, pos, jar.amount(), true);
            jar.clearAspect();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) return;
        if (by == null) return;
        if (jar.aspectFilterKey() == null) return;
        jar.setFacing(by.getDirection().getOpposite());
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction face, ItemStack stack) {
        if (!(player.level().getBlockEntity(pos) instanceof BlockEntityJar jar)) return false;
        if (!(stack.getItem() instanceof ILabel label)) return false;
        if (face.getStepY() != 0) return false;
        if (jar.aspectFilterKey() != null) return false;

        ResourceKey<IAspect> labelAspect = label.getFilteredAspect(stack);
        if (jar.amount() == 0 && labelAspect == null) return false;

        ResourceKey<IAspect> aspect = null;
        if (jar.amount() == 0 && labelAspect != null) aspect = labelAspect;
        if (jar.amount() > 0) aspect = jar.aspectKey();

        if (aspect == null) return false;
        if (labelAspect != null && !labelAspect.equals(aspect)) return false;

        BlockState state = player.level().getBlockState(pos);
        setPlacedBy(player.level(), pos, state, player, stack);
        jar.setAspectFilter(aspect);
        jar.setFacing(face);
        jar.setChanged();
        jar.syncToClient();
        player.level()
                .playSound(
                        null,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        TCSounds.PAGE.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F);
        return true;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.JAR.get(), BlockEntityJar::serverTick);
    }
}
