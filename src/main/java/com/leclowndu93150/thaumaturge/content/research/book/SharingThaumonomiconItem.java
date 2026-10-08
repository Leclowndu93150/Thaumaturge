package com.leclowndu93150.thaumaturge.content.research.book;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.share.ShareBinding;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class SharingThaumonomiconItem extends Item {
    public SharingThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        ItemStack stack = player.getItemInHand(hand);
        ShareBinding binding = stack.get(TTDataComponents.SHARE_BINDING.get());
        IPlayerKnowledge know = ResearchManager.of(serverPlayer);
        if (!(know instanceof PlayerKnowledge knowledge))
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        AspectPoolData discoveredAspects = AspectPools.data(player);
        if (binding == null) {
            stack.set(
                    TTDataComponents.SHARE_BINDING.get(),
                    new ShareBinding(
                            player.getUUID(),
                            player.getGameProfile().getName(),
                            AspectPoolData.snapshotOf(discoveredAspects),
                            PlayerKnowledge.snapshotOf(knowledge)));
            player.playSound(TTSounds.WRITE.get(), 1.0F, 1.0F);
            TTActionBar.sendPurple(player, "message.thaumaturge.thaumonomicon.sharing_bound");
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        if (binding.player().equals(player.getUUID())) {
            TTActionBar.sendPurple(player, "message.thaumaturge.thaumonomicon.sharing_self");
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        knowledge.mergeResearchFrom(binding.knowledge());
        discoveredAspects.mergeDiscoveriesFrom(binding.discoveredAspects());

        knowledge.sync(serverPlayer);
        AspectPools.sync(serverPlayer);

        player.playSound(TTSounds.WRITE.get(), 1.0F, 1.0F);
        TTActionBar.sendPurple(player, "message.thaumaturge.thaumonomicon.sharing_used", binding.name());
        stack.shrink(1);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        ShareBinding binding = stack.get(TTDataComponents.SHARE_BINDING.get());
        if (binding != null) {
            builder.add(Component.translatable("tooltip.thaumaturge.sharing.bound", binding.name())
                    .withStyle(ChatFormatting.GRAY));
        }
        builder.add(Component.translatable("tooltip.thaumaturge.sharing.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
