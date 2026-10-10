package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.blocks.ILabelable;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStreamPort;
import com.leclowndu93150.thaumaturge.content.essentia.storage.LabelledVesselActions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockJar extends BaseEntityBlock implements ILabelable, IEssentiaStreamPort, IEssentiaJar {
    public static final MapCodec<BlockJar> CODEC = simpleCodec(BlockJar::new);
    public static final VoxelShape SHAPE = Shapes.or(Block.box(3, 0, 3, 13, 12, 13), Block.box(5, 12, 5, 11, 14, 11));

    private static final double MOUTH_ANCHOR_HEIGHT = 0.8;
    private static final double MOUTH_CLEARANCE_HEIGHT = 1.4;
    private static final int MAX_COMPARATOR_SIGNAL = 15;
    private static final int COMPARATOR_STEPS = 14;
    private static final float KEY_VOLUME = 1.0F;
    private static final float KEY_PITCH = 1.0F;

    public BlockJar(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction face, ItemStack stack) {
        Level level = player.level();
        BlockEntity found = level.getBlockEntity(pos);
        if (!(found instanceof BlockEntityJar jar)) {
            return false;
        }
        ResourceKey<IAspect> label = LabelledVesselActions.aspectToLabel(stack, jar.aspectFilterKey(), jar.aspectKey(), jar.amount());
        if (label != null) {
            jar.setAspectFilter(label);
            jar.setFacing(LabelledVesselActions.labelSideFacing(player));
            LabelledVesselActions.playLabelSound(level, pos);
        }
        return label != null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(level, pos, player, hit, ItemStack.EMPTY);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean holdingBrace = stack.is(TTItems.JAR_BRACE.get());
        return holdingBrace ? interact(level, pos, player, hit, stack) : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer == null) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityJar jar && jar.aspectFilterKey() != null) {
            jar.setFacing(placer.getDirection().getOpposite());
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof BlockEntityJar jar && jar.isBlocked() && !level.isClientSide()) {
            popResource(level, pos, new ItemStack(TTItems.JAR_BRACE.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntity found = level.getBlockEntity(pos);
        return found instanceof BlockEntityJar jar ? comparatorSignal(jar.amount()) : 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public StreamPort essentiaStreamPort(BlockGetter level, BlockPos pos, BlockState state, Vec3 farEnd, boolean outgoing) {
        Vec3 floor = Vec3.atBottomCenterOf(pos);
        Vec3 anchor = floor.add(0.0, MOUTH_ANCHOR_HEIGHT, 0.0);
        Vec3 clearance = floor.add(0.0, MOUTH_CLEARANCE_HEIGHT, 0.0);
        return new StreamPort(anchor, clearance);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.JAR.get(), BlockEntityJar::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJar(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockJar> codec() {
        return CODEC;
    }

    private static int comparatorSignal(int amount) {
        if (amount <= 0) {
            return 0;
        }
        double fill = (double) amount / DEFAULT_CAPACITY;
        return Math.min(MAX_COMPARATOR_SIGNAL, (int) Math.floor(fill * COMPARATOR_STEPS) + 1);
    }

    private static InteractionResult interact(Level level, BlockPos pos, Player player, BlockHitResult hit, ItemStack brace) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJar jar)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!brace.isEmpty() && !jar.isBlocked()) {
            installBrace(jar, brace, player, level, pos);
        } else if (player.isShiftKeyDown()) {
            dispose(jar, level, pos, hit.getDirection());
        } else {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void dispose(BlockEntityJar jar, Level level, BlockPos pos, Direction hitFace) {
        boolean onLabel = jar.aspectFilterKey() != null && hitFace == jar.facing();
        if (!onLabel) {
            LabelledVesselActions.pourOut(level, pos, jar.amount());
            jar.clearAspect();
            jar.setChangedAndSync();
            return;
        }
        jar.setAspectFilter(null);
        LabelledVesselActions.removeLabel(level, pos, hitFace);
    }

    private static void installBrace(BlockEntityJar jar, ItemStack stack, Player player, Level level, BlockPos pos) {
        jar.setBraced(true);
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        level.playSound(null, pos, TTSounds.KEY.get(), SoundSource.BLOCKS, KEY_VOLUME, KEY_PITCH);
    }
}
