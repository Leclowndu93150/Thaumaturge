package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class FocusPouchItem extends Item {
    public static final int SIZE = 18;

    public FocusPouchItem(Properties properties) {
        super(properties);
    }

    public static NonNullList<ItemStack> getInventory(ItemStack pouch) {
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ItemContainerContents contents = pouch.get(TCDataComponents.POUCH_CONTENTS.get());
        if (contents != null) {
            contents.copyInto(list);
        }
        return list;
    }

    public static void setInventory(ItemStack pouch, NonNullList<ItemStack> list) {
        pouch.set(TCDataComponents.POUCH_CONTENTS.get(), ItemContainerContents.fromItems(list));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        int count = 0;
        for (ItemStack focus : getInventory(stack)) {
            if (focus.getItem() instanceof ItemFocus) {
                count++;
            }
        }
        builder.accept(Component.translatable("tooltip.thaumaturge.focus_pouch.count", count, SIZE).withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TCIds.rl("focus_pouch"))) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("item.thaumaturge.focus_pouch");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                    return new MenuFocusPouch(containerId, inventory, hand);
                }
            }, buf -> buf.writeBoolean(hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResult.SUCCESS;
    }
}
