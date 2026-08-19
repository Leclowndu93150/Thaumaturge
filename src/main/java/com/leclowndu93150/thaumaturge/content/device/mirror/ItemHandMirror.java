package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ItemHandMirror extends Item {
    public ItemHandMirror(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (!(level.getBlockState(pos).getBlock() instanceof BlockMirror clicked) || clicked.isEssentia() || player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityMirror) {
            stack.set(TCDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), pos));
            level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
            player.sendSystemMessage(Component.translatable("tc.handmirrorlinked"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TCIds.rl("mirror_hand"))) {
            return InteractionResult.SUCCESS_SERVER;
        }
        ItemStack stack = player.getItemInHand(hand);
        GlobalPos link = stack.get(TCDataComponents.MIRROR_LINK.get());
        if (link == null || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        ServerLevel targetLevel = serverPlayer.level().getServer().getLevel(link.dimension());
        BlockEntity target = targetLevel == null ? null : targetLevel.getBlockEntity(link.pos());
        if (!(target instanceof BlockEntityMirror)) {
            stack.remove(TCDataComponents.MIRROR_LINK.get());
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), TCSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
            serverPlayer.sendSystemMessage(Component.translatable("tc.handmirrorerror"));
            return InteractionResult.SUCCESS;
        }
        serverPlayer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("item.thaumaturge.hand_mirror");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return new MenuHandMirror(containerId, inventory);
            }
        });
        return InteractionResult.SUCCESS;
    }

    public static boolean transport(ItemStack mirror, ItemStack items, ServerPlayer player) {
        GlobalPos link = mirror.get(TCDataComponents.MIRROR_LINK.get());
        if (link == null) {
            return false;
        }
        ServerLevel targetLevel = player.level().getServer().getLevel(link.dimension());
        BlockEntity target = targetLevel == null ? null : targetLevel.getBlockEntity(link.pos());
        if (target instanceof BlockEntityMirror targetMirror) {
            if (targetMirror.transportDirect(items)) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1F, 1.0F);
            }
            return true;
        }
        mirror.remove(TCDataComponents.MIRROR_LINK.get());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
        player.sendSystemMessage(Component.translatable("tc.handmirrorerror"));
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(TCDataComponents.MIRROR_LINK.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        GlobalPos link = stack.get(TCDataComponents.MIRROR_LINK.get());
        if (link != null) {
            tooltip.accept(Component.translatable("tc.handmirrorlinkedto.full", link.pos().getX(), link.pos().getY(), link.pos().getZ(), link.dimension().identifier().toString()));
        }
    }
}
