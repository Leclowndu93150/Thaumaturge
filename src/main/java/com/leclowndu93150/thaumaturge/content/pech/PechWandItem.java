package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class PechWandItem extends Item {
    private static final ResourceLocation GATE_RESEARCH = TTIds.rl("base_auromancy");
    private static final ResourceLocation FOCUS_PECH = TTIds.rl("focuspech");

    public PechWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.curio.text"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!KnowledgeAccess.of(player).isResearchKnown(GATE_RESEARCH)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("not.pechwand").withStyle(ChatFormatting.RED));
            }
            return super.use(level, player, hand);
        }
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    TTSounds.LEARN.get(),
                    SoundSource.NEUTRAL,
                    0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            serverPlayer.sendSystemMessage(
                    Component.translatable("got.pechwand").withStyle(ChatFormatting.DARK_PURPLE));
            if (!KnowledgeAccess.of(serverPlayer).isResearchKnown(FOCUS_PECH)) {
                ResearchManager.complete(serverPlayer, FOCUS_PECH);
            }
            int oProg = KnowledgeType.OBSERVATION.progression();
            ResearchGrants.grantConvertedKnowledge(
                    serverPlayer,
                    KnowledgeType.OBSERVATION,
                    Mth.nextInt(serverPlayer.getRandom(), oProg / 3, oProg / 2));
            int tProg = KnowledgeType.THEORY.progression();
            ResearchGrants.grantConvertedKnowledge(
                    serverPlayer, KnowledgeType.THEORY, Mth.nextInt(serverPlayer.getRandom(), tProg / 5, tProg / 4));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
