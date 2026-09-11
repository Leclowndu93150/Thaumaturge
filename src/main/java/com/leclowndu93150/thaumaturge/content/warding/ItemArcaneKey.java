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
        if (player == null || !isLock(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        GlobalPos target = GlobalPos.of(level.dimension(), pos);
        GlobalPos link = stack.get(TCDataComponents.ARCANE_KEY_LINK.get());
        boolean gold = stack.is(com.leclowndu93150.thaumaturge.registry.TCItems.ARCANE_KEY_GOLD.get());
        if (link == null) {
            if (!ArcaneAccess.canBind(server, pos, player, gold)) {
                player.sendSystemMessage(Component.translatable("message.thaumaturge.arcane_key_no_access"));
                return InteractionResult.FAIL;
            }
            ItemStack boundKey = stack.copyWithCount(1);
            boundKey.set(TCDataComponents.ARCANE_KEY_LINK.get(), target);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (!player.getInventory().add(boundKey)) {
                player.drop(boundKey, false);
            }
            player.sendSystemMessage(Component.translatable("message.thaumaturge.arcane_key_bound"));
            return InteractionResult.SUCCESS;
        }
        if (!link.equals(target)) {
            player.sendSystemMessage(Component.translatable("message.thaumaturge.arcane_key_wrong_lock"));
            return InteractionResult.SUCCESS;
        }
        if (player.getUUID().equals(ArcaneAccess.owner(server, pos))) {
            player.sendSystemMessage(Component.translatable("message.thaumaturge.arcane_key_already_bound"));
            return InteractionResult.SUCCESS;
        }
        if (ArcaneAccess.canAccess(server, pos, player)) {
            player.sendSystemMessage(Component.translatable("message.thaumaturge.arcane_key_already_access"));
            return InteractionResult.SUCCESS;
        }
        if (!ArcaneAccess.grantAccess(server, pos, player.getUUID(), gold)) {
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.sendSystemMessage(Component.translatable(
                gold ? "message.thaumaturge.arcane_key_granted_gold" : "message.thaumaturge.arcane_key_granted_iron"));
        return InteractionResult.SUCCESS;
    }

    private static boolean isLock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TCBlocks.ARCANE_DOOR.get())
                || level.getBlockState(pos).is(TCBlocks.ARCANE_PRESSURE_PLATE.get());
    }
}
