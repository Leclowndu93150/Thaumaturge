package com.leclowndu93150.thaumaturge.content.research.book;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.share.ShareBinding;
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

public final class SharingThaumonomiconItem extends Item {
    public SharingThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        ShareBinding binding = stack.get(TCDataComponents.SHARE_BINDING.get());
        IPlayerKnowledge know = ResearchManager.of(serverPlayer);
        if (!(know instanceof PlayerKnowledge knowledge))
            return InteractionResult.SUCCESS_SERVER;
        AspectPoolData discoveredAspects = AspectPools.data(player);
        if (binding == null) {
            stack.set(TCDataComponents.SHARE_BINDING.get(), new ShareBinding(player.getUUID(), player.getGameProfile().name(), discoveredAspects, knowledge));
            player.playSound(TCSounds.WRITE.get(), 1.0F, 1.0F);
            TCActionBar.sendPurple(player, "tc.thaumonomicon.sharing.bound");
            return InteractionResult.SUCCESS_SERVER;
        }
        if (binding.player().equals(player.getUUID())) {
            TCActionBar.sendPurple(player, "tc.thaumonomicon.sharing.self");
            return InteractionResult.SUCCESS_SERVER;
        }
        knowledge.copyFrom(binding.knowledge());
        discoveredAspects.copyFrom(binding.discoveredAspects());

        knowledge.sync(serverPlayer);
        AspectPools.sync(serverPlayer);

        player.playSound(TCSounds.WRITE.get(), 1.0F, 1.0F);
        TCActionBar.sendPurple(player, "tc.thaumonomicon.sharing.used", binding.name());
        stack.shrink(1);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        ShareBinding binding = stack.get(TCDataComponents.SHARE_BINDING.get());
        if (binding != null) {
            builder.accept(Component.translatable("tooltip.thaumaturge.sharing.bound", binding.name()).withStyle(ChatFormatting.GRAY));
        }
        builder.accept(Component.translatable("tooltip.thaumaturge.sharing.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
