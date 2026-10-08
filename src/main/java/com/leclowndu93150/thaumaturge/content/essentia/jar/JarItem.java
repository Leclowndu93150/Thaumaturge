package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class JarItem extends BlockItem {

    public JarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !ComponentEssentia.jar(stack).getAspects().isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        AspectList list = ComponentEssentia.jar(stack).getAspects();
        if (list.isEmpty()) {
            return 0;
        }
        int amount = list.totalAmount();
        int max = BlockEntityJar.CAPACITY;
        return Mth.clamp(Math.round((float) amount * 13.0F / (float) max), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        AspectList list = ComponentEssentia.jar(stack).getAspects();
        if (list.isEmpty()) {
            return 0;
        }
        AspectInstance first = list.entries().getFirst();
        if (first == null) {
            return 0;
        }
        return first.aspect().value().color();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return super.useOn(context);
        }
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic) || alembic.aspectKey() == null) {
            return super.useOn(context);
        }
        AspectList aspects = ComponentEssentia.jar(stack).getAspects();
        Holder<IAspect> aspect = EssentiaTransportHelper.resolve(level, alembic.aspectKey());
        if (aspect == null
                || (!aspects.isEmpty() && aspect != aspects.entries().getFirst().aspect())) {
            return super.useOn(context);
        }
        int available = Math.min(alembic.amount(), BlockEntityJar.CAPACITY - aspects.totalAmount());
        if (available <= 0) {
            return super.useOn(context);
        }
        if (level.isClientSide()) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        int taken = alembic.takeEssentia(aspect, available, Direction.UP);
        if (taken <= 0) {
            return super.useOn(context);
        }
        ItemStack filled = stack.split(1);
        ComponentEssentia.jar(filled).setAspects(aspects.add(aspect, taken));
        if (stack.isEmpty()) {
            player.setItemInHand(context.getHand(), filled);
        } else if (!player.addItem(filled)) {
            player.drop(filled, false);
        }
        level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
        return InteractionResult.SUCCESS;
    }
}
