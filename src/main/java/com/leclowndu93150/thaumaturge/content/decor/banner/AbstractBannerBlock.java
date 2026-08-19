package com.leclowndu93150.thaumaturge.content.decor.banner;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBannerBlock extends Block implements EntityBlock {
    private final @Nullable DyeColor dye;

    protected AbstractBannerBlock(@Nullable DyeColor dye, Properties properties) {
        super(properties);
        this.dye = dye;
    }

    public @Nullable DyeColor dye() {
        return dye;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBanner(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (dye == null) {
            return InteractionResult.PASS;
        }
        boolean clearing = player.isShiftKeyDown();
        boolean settingFromPhial = stack.getItem() instanceof PhialItem;
        if (!clearing && !settingFromPhial) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof BlockEntityBanner banner)) {
            return InteractionResult.PASS;
        }
        if (clearing) {
            banner.setAspect(null);
        } else {
            AspectList aspects = ((PhialItem) stack.getItem()).getAspects(stack);
            if (aspects.isEmpty()) {
                return InteractionResult.PASS;
            }
            banner.setAspect(aspects.entries().get(0).aspect().unwrapKey().orElse(null));
            stack.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }
}
