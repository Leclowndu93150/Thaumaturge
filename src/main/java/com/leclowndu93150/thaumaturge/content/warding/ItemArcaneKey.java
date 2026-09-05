package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class ItemArcaneKey extends Item {
    public ItemArcaneKey(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).is(TCBlocks.ARCANE_DOOR.get())
                && level.getBlockState(pos).getValue(net.minecraft.world.level.block.DoorBlock.HALF)
                        == DoubleBlockHalf.UPPER) pos = pos.below();
        if (player == null
                || !(level instanceof ServerLevel server)
                || !isLock(level, pos)
                || !ArcaneAccess.canAccess(server, pos, player)) return InteractionResult.PASS;
        stack.set(TCDataComponents.ARCANE_KEY_LINK.get(), GlobalPos.of(level.dimension(), pos));
        player.sendSystemMessage(Component.translatable("message.thaumaturge.arcane_key_bound"));
        return InteractionResult.SUCCESS;
    }

    private static boolean isLock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TCBlocks.ARCANE_DOOR.get())
                || level.getBlockState(pos).is(TCBlocks.ARCANE_PRESSURE_PLATE.get());
    }
}
