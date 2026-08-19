package com.leclowndu93150.thaumaturge.content.research.book;

import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.content.research.link.LinkBinding;
import com.leclowndu93150.thaumaturge.content.research.link.ResearchLinkData;
import com.leclowndu93150.thaumaturge.content.research.link.ResearchLinkEvents;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class LinkingThaumonomiconItem extends Item {
    public LinkingThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        LinkBinding binding = stack.get(TCDataComponents.LINK_BINDING.get());
        if (binding == null) {
            stack.set(TCDataComponents.LINK_BINDING.get(), new LinkBinding(player.getUUID(), player.getGameProfile().name()));
            player.playSound(TCSounds.WRITE.get(), 1.0F, 1.0F);
            TCActionBar.sendPurple(player, "tc.thaumonomicon.sharing.bound");
            return InteractionResult.SUCCESS_SERVER;
        }
        if (binding.player().equals(player.getUUID())) {
            TCActionBar.sendPurple(player, "tc.thaumonomicon.sharing.self");
            return InteractionResult.SUCCESS_SERVER;
        }
        ResearchLinkData data = ResearchLinkData.get(serverPlayer.level().getServer());
        ResearchLinkData.Link link = data.link(binding.player(), player.getUUID());
        ResearchLinkEvents.syncLink(serverPlayer.level().getServer(), data, link);
        player.playSound(TCSounds.WRITE.get(), 1.0F, 1.0F);
        TCActionBar.sendPurple(player, "tc.thaumonomicon.sharing.linked", binding.name());
        ServerPlayer partner = serverPlayer.level().getServer().getPlayerList().getPlayer(binding.player());
        if (partner != null) {
            TCActionBar.sendPurple(partner, "tc.thaumonomicon.sharing.linked", player.getGameProfile().name());
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        LinkBinding binding = stack.get(TCDataComponents.LINK_BINDING.get());
        if (binding != null) {
            builder.accept(Component.translatable("tooltip.thaumaturge.sharing.bound", binding.name()).withStyle(ChatFormatting.GRAY));
        }
        builder.accept(Component.translatable("tooltip.thaumaturge.sharing.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
