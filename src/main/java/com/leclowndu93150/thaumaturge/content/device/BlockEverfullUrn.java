package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jspecify.annotations.Nullable;

public final class BlockEverfullUrn extends BaseEntityBlock {
    public static final MapCodec<BlockEverfullUrn> CODEC = simpleCodec(BlockEverfullUrn::new);

    private static final VoxelShape SHAPE = Shapes.or(box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0), box(5.0, 10.0, 5.0, 11.0, 14.0, 11.0), box(4.0, 14.0, 4.0, 12.0, 16.0, 12.0));
    private static final int BOTTLE_COST = 333;

    public BlockEverfullUrn(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEverfullUrn> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEverfullUrn(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof BlockEntityEverfullUrn urn)) {
            return InteractionResult.PASS;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            urn.setChanged();
            playSplash(level, pos);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.GLASS_BOTTLE) && urn.waterAmount() >= BOTTLE_COST) {
            ItemStack bottle = new ItemStack(Items.POTION);
            bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            stack.consume(1, player);
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
            urn.drainWater(BOTTLE_COST);
            playSplash(level, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void playSplash(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.33F, 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TCBlockEntities.EVERFULL_URN.get(), BlockEntityEverfullUrn::serverTick);
    }
}
