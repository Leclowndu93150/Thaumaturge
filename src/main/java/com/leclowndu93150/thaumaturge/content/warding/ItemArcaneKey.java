package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
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
import net.minecraft.world.level.block.state.BlockState;

public final class ItemArcaneKey extends Item {
    private static final String MESSAGE_BOUND = "message.thaumaturge.arcane_key_bound";
    private static final String MESSAGE_NO_ACCESS = "message.thaumaturge.arcane_key_no_access";
    private static final String MESSAGE_WRONG_LOCK = "message.thaumaturge.arcane_key_wrong_lock";
    private static final String MESSAGE_ALREADY_BOUND = "message.thaumaturge.arcane_key_already_bound";
    private static final String MESSAGE_ALREADY_ACCESS = "message.thaumaturge.arcane_key_already_access";
    private static final String MESSAGE_GRANTED_IRON = "message.thaumaturge.arcane_key_granted_iron";
    private static final String MESSAGE_GRANTED_GOLD = "message.thaumaturge.arcane_key_granted_gold";

    private final boolean gold;

    public ItemArcaneKey(Properties properties, boolean gold) {
        super(properties);
        this.gold = gold;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (player == null || !ArcaneAccess.isLock(state)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos origin = ArcaneAccess.lockOrigin(state, context.getClickedPos());
        GlobalPos target = GlobalPos.of(level.dimension(), origin);
        GlobalPos link = stack.get(TTDataComponents.ARCANE_KEY_LINK.get());
        if (link == null) {
            return bind(serverLevel, origin, target, stack, player);
        }
        if (!link.equals(target)) {
            message(player, MESSAGE_WRONG_LOCK);
            return InteractionResult.SUCCESS;
        }
        if (player.getUUID().equals(ArcaneAccess.owner(serverLevel, origin))) {
            message(player, MESSAGE_ALREADY_BOUND);
            return InteractionResult.SUCCESS;
        }
        if (ArcaneAccess.canAccess(serverLevel, origin, player)) {
            message(player, MESSAGE_ALREADY_ACCESS);
            return InteractionResult.SUCCESS;
        }
        if (!ArcaneAccess.grantAccess(serverLevel, origin, player.getUUID(), gold)) {
            return InteractionResult.FAIL;
        }
        stack.consume(1, player);
        message(player, gold ? MESSAGE_GRANTED_GOLD : MESSAGE_GRANTED_IRON);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult bind(ServerLevel level, BlockPos origin, GlobalPos target, ItemStack stack, Player player) {
        if (!ArcaneAccess.canBind(level, origin, player, gold)) {
            message(player, MESSAGE_NO_ACCESS);
            return InteractionResult.FAIL;
        }
        ItemStack boundKey = stack.copyWithCount(1);
        boundKey.set(TTDataComponents.ARCANE_KEY_LINK.get(), target);
        stack.consume(1, player);
        if (!player.getInventory().add(boundKey)) {
            player.drop(boundKey, false);
        }
        message(player, MESSAGE_BOUND);
        return InteractionResult.SUCCESS;
    }

    private static void message(Player player, String key) {
        player.sendSystemMessage(Component.translatable(key));
    }
}
