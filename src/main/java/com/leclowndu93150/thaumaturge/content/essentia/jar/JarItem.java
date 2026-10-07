package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
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
import net.minecraft.world.level.block.state.BlockState;

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
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null)
            return super.useOn(context);
        if (level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic) {
            AspectList aspects = ComponentEssentia.jar(stack).getAspects();
            // We use Direction.UP to allow insertion/extraction from all faces with fials
            if (alembic.aspectKey() != null) {
                Holder<IAspect> aspect = EssentiaTransportHelper.resolve(level, alembic.aspectKey());
                if (aspect != null && (aspects.isEmpty() || aspect == aspects.entries().getFirst().aspect())) {
                    int alembicAmount = Math.min(alembic.amount(), BlockEntityJar.CAPACITY - aspects.totalAmount());
                    if (alembicAmount >= 0) {
                        if (level.isClientSide()) {
                            player.swing(context.getHand());
                            return InteractionResult.SUCCESS;
                        }
                        int taken = alembic.takeEssentia(aspect, alembicAmount, Direction.UP);
                        if (taken > 0) {
                            ItemStack filled = stack.copyWithCount(1);
                            ComponentEssentia.jar(filled).setAspects(aspects.add(aspect, taken));
                            if (stack.getCount() == 1) {
                                player.setItemInHand(context.getHand(), filled);
                            } else {
                                stack.shrink(1);
                                if (!player.addItem(filled)) {
                                    player.drop(filled, false);
                                }
                            }
                            level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, 0.25f, 1.0f);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return super.useOn(context);
    }
}
