package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.content.research.KnowledgeGrant;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class PechWandItem extends Item {
    private static final ResourceLocation PREREQUISITE = TTIds.rl("base_auromancy");
    private static final ResourceLocation REVEALED_RESEARCH = TTIds.rl("scanned/pechwand");
    private static final List<KnowledgeGrant> INSIGHTS = List.of(
            new KnowledgeGrant(KnowledgeType.OBSERVATION, 3, 2), new KnowledgeGrant(KnowledgeType.THEORY, 5, 4));
    private static final float STUDY_VOLUME = 0.5F;
    private static final float STUDY_PITCH = 0.42F;
    private static final float STUDY_PITCH_SPREAD = 0.08F;

    public PechWandItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumaturge.curio.read"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!KnowledgeAccess.of(player).isResearchKnown(PREREQUISITE)) {
            if (player instanceof ServerPlayer) {
                player.sendSystemMessage(Component.translatable("message.thaumaturge.pech_wand.unfathomable")
                        .withStyle(ChatFormatting.RED));
            }
            return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
        }
        player.getItemInHand(hand).consume(1, player);
        if (player instanceof ServerPlayer scholar) {
            study(scholar);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    private static void study(ServerPlayer scholar) {
        scholar.level()
                .playSound(
                        null,
                        scholar.getX(),
                        scholar.getY(),
                        scholar.getZ(),
                        TTSounds.LEARN.get(),
                        SoundSource.NEUTRAL,
                        STUDY_VOLUME,
                        (float) (STUDY_PITCH + scholar.getRandom().triangle(0.0F, STUDY_PITCH_SPREAD)));
        scholar.sendSystemMessage(Component.translatable("message.thaumaturge.discovery.pech_wand")
                .withStyle(ChatFormatting.DARK_PURPLE));
        if (!KnowledgeAccess.of(scholar).isResearchKnown(REVEALED_RESEARCH)) {
            ResearchManager.complete(scholar, REVEALED_RESEARCH);
        }
        INSIGHTS.forEach(insight -> insight.award(scholar));
    }
}
