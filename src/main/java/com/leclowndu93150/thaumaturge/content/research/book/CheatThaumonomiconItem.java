package com.leclowndu93150.thaumaturge.content.research.book;

import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.network.ClientboundOpenThaumonomiconPayload;
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
import net.neoforged.neoforge.network.PacketDistributor;

public final class CheatThaumonomiconItem extends ThaumonomiconItem {
    public CheatThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            int granted = ResearchGrants.grantAll(serverPlayer);
            if (granted > 0) {
                TCActionBar.sendPurple(player, "tc.thaumonomicon.cheat.granted", granted);
            }
            PacketDistributor.sendToPlayer(serverPlayer, ClientboundOpenThaumonomiconPayload.INSTANCE);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumaturge.creative_only").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
