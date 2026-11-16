package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class JarItem extends BlockItem {
    private static final int BAR_MAX_WIDTH = 13;
    private static final float SCOOP_VOLUME = 0.25F;
    private static final float SCOOP_PITCH = 1.0F;

    public JarItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        EssentiaList contents = stack.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        return contents != null && !contents.isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        EssentiaList contents = stack.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        int stored = contents == null ? 0 : contents.totalAmount();
        return Math.clamp(Math.round((float) stored * BAR_MAX_WIDTH / IEssentiaJar.DEFAULT_CAPACITY), 0, BAR_MAX_WIDTH);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        EssentiaList contents = stack.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        if (contents == null || contents.isEmpty()) {
            return 0;
        }
        return contents.contents().entries().getFirst().aspect().value().color();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null
                || !(level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic)
                || alembic.aspectKey() == null) {
            return super.useOn(context);
        }
        Holder<IAspect> aspect = Aspects.resolve(level, alembic.aspectKey());
        if (aspect == null) {
            return super.useOn(context);
        }
        ItemStack held = context.getItemInHand();
        AspectList contents = contentsOf(held);
        ResourceKey<IAspect> filter = held.get(TTDataComponents.ASPECT_FILTER.get());
        if ((filter != null && !aspect.is(filter))
                || (!contents.isEmpty()
                        && !contents.entries().getFirst().aspect().equals(aspect))) {
            return super.useOn(context);
        }
        int scoop = Math.min(alembic.amount(), IEssentiaJar.DEFAULT_CAPACITY - contents.totalAmount());
        if (level instanceof ServerLevel server) {
            if (scoop <= 0 || alembic.extract(aspect, scoop, Direction.UP, false) <= 0) {
                return super.useOn(context);
            }
            replaceHeld(player, context.getHand(), held, scooped(held, contents, aspect, scoop));
            server.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, SCOOP_VOLUME, SCOOP_PITCH);
            return InteractionResult.CONSUME;
        }
        if (scoop <= 0 || !alembic.releasesOn(Direction.UP)) {
            return super.useOn(context);
        }
        player.swing(context.getHand());
        return InteractionResult.SUCCESS;
    }

    private static AspectList contentsOf(ItemStack stack) {
        EssentiaList contents = stack.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        return contents == null ? AspectList.EMPTY : contents.contents();
    }

    private static ItemStack scooped(ItemStack held, AspectList contents, Holder<IAspect> aspect, int scoop) {
        ItemStack result = held.copyWithCount(1);
        result.set(
                TTDataComponents.ESSENTIA_CONTENTS.get(),
                new EssentiaList(contents.add(new AspectInstance(aspect, scoop))));
        return result;
    }

    private static void replaceHeld(Player player, InteractionHand hand, ItemStack held, ItemStack scooped) {
        if (held.getCount() <= 1) {
            player.setItemInHand(hand, scooped);
            return;
        }
        held.shrink(1);
        if (!player.getInventory().add(scooped) && player.level() instanceof ServerLevel) {
            player.spawnAtLocation(scooped);
        }
    }
}
