package com.leclowndu93150.thaumaturge.content.research.note;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ItemResearchNote extends Item {
    public ItemResearchNote(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResearchNoteData data = ResearchNotes.dataOf(stack);
        if (data == null || !data.complete()) {
            return InteractionResultHolder.pass(stack);
        }
        ResourceLocation learnKey = data.learnKey();
        if (KnowledgeAccess.of(player).isResearchKnown(learnKey)) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(serverPlayer);
            knowledge.addResearch(learnKey);
            knowledge.sync(serverPlayer);
            AspectPools.data(serverPlayer).incrementCompletedNotes();
            AspectPools.sync(serverPlayer);
            stack.shrink(1);
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    TTSounds.LEARN.get(),
                    SoundSource.PLAYERS,
                    0.66F,
                    1.0F);
            serverPlayer.sendSystemMessage(Component.translatable(
                            "tc.researchnote.learned",
                            ResearchNotes.entryName(serverPlayer.registryAccess(), data.entry()))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            ResearchManager.advanceStage(serverPlayer, data.entry());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        ResearchNoteData data = ResearchNotes.dataOf(stack);
        if (data != null && data.complete()) {
            return Component.translatable("item.thaumaturge.research_note.complete");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResearchNoteData data = ResearchNotes.dataOf(stack);
        if (data == null) {
            return;
        }
        if (context.registries() != null) {
            tooltip.add(Component.translatable(
                            "tc.researchtheory", ResearchNotes.entryName(context.registries(), data.entry()))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (data.complete()) {
            tooltip.add(Component.translatable("tc.researchnote.use").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("tc.researchnote.table").withStyle(ChatFormatting.GRAY));
        }
    }

    public static Rarity rarityFor(ItemStack stack) {
        ResearchNoteData data = ResearchNotes.dataOf(stack);
        return data != null && data.complete() ? Rarity.EPIC : Rarity.RARE;
    }
}
