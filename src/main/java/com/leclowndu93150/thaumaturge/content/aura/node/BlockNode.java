package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockNode extends Block implements EntityBlock {
    private static final Identifier PRIMORDIAL_NODES_RESEARCH = TCIds.rl("primordial_nodes");
    private static final float PEARL_FLUX = 25.0F;
    private static final float PEARL_EXPLOSION_BASE = 3.0F;
    private static final float PEARL_EXPLOSION_SPREAD = 5.0F;
    private static final float PEARL_EXPLOSION_SPREAD_RESEARCHED = 3.0F;
    private static final double PEARL_EXPLOSION_LIFT = 1.5;
    private static final int PEARL_SPILLS = 33;
    private static final int PEARL_SPILL_SPREAD = 6;
    public static final MapCodec<BlockNode> CODEC = simpleCodec(BlockNode::new);

    private static final VoxelShape SHAPE = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

    public BlockNode(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockNode> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult pearlResult = tryPrimordialPearl(stack, level, pos, player);
        return pearlResult != null ? pearlResult : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    public static @Nullable InteractionResult tryPrimordialPearl(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!stack.is(TCItems.PRIMORDIAL_PEARL.get()) || stack.getDamageValue() > PrimordialPearlItem.PEARL_MAX_DAMAGE || !(level.getBlockEntity(pos) instanceof BlockEntityNode node)
                || node.isEnergized()) {
            return null;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        boolean researched = KnowledgeAccess.of(player).isResearchComplete(PRIMORDIAL_NODES_RESEARCH);
        RandomSource random = serverLevel.getRandom();
        node.applyPrimordialPearl(random, researched);
        stack.consume(1, player);
        AuraHelper.polluteAura(serverLevel, pos, PEARL_FLUX, true);
        float strength = PEARL_EXPLOSION_BASE + random.nextFloat() * (researched ? PEARL_EXPLOSION_SPREAD_RESEARCHED : PEARL_EXPLOSION_SPREAD);
        serverLevel.explode(null, pos.getX() + 0.5, pos.getY() + PEARL_EXPLOSION_LIFT, pos.getZ() + 0.5, strength, Level.ExplosionInteraction.BLOCK);
        for (int i = 0; i < PEARL_SPILLS; i++) {
            BlockPos target = pos.offset(spillOffset(random), spillOffset(random), spillOffset(random));
            if (!serverLevel.hasChunkAt(target)) {
                continue;
            }
            if (target.getY() < pos.getY()) {
                PhysicalFlux.placeGoo(serverLevel, target, PhysicalFlux.MAX_QUANTA);
            } else {
                PhysicalFlux.placeGas(serverLevel, target, PhysicalFlux.MAX_QUANTA);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static int spillOffset(RandomSource random) {
        return random.nextInt(PEARL_SPILL_SPREAD) - random.nextInt(PEARL_SPILL_SPREAD);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = new ItemStack(TCItems.CREATIVE_NODE_PLACER.get());
        if (level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(node.getType(), node.saveCustomOnly(level.registryAccess())));
            stack.applyComponents(node.collectComponents());
        }
        return stack;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNode(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TCBlockEntities.NODE.get()) {
            return null;
        }
        if (level.isClientSide()) {
            return (tickLevel, pos, tickState, node) -> ((BlockEntityNode) node).clientTick(tickLevel, pos);
        }
        return (tickLevel, pos, tickState, node) -> ((BlockEntityNode) node).serverTick(tickLevel, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        burstNode(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    public static void burstNode(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            node.burstIntoOrbs(serverLevel, pos);
        }
    }
}
