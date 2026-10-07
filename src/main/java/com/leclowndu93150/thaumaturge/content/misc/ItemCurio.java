package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.TTResearchCategories;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.research.KnowledgeGrant;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class ItemCurio extends Item {
    public enum Variant {
        ARCANE(TTResearchCategories.AUROMANCY, false, false), PRESERVED(TTResearchCategories.ALCHEMY, false, false), ANCIENT(TTResearchCategories.GOLEMANCY, false, false), ELDRITCH(
                TTResearchCategories.ELDRITCH, true,
                false), KNOWLEDGE(TTResearchCategories.INFUSION, false, false), TWISTED(TTResearchCategories.ARTIFICE, false, false), RITES(TTResearchCategories.ELDRITCH, true, true);

        private final ResourceKey<IResearchCategory> category;
        private final boolean warping;
        private final boolean rites;

        Variant(ResourceKey<IResearchCategory> category, boolean warping, boolean rites) {
            this.category = category;
            this.warping = warping;
            this.rites = rites;
        }
    }

    private static final Identifier CRIMSON_RITES_RESEARCH = TTIds.rl("crimson_rites");
    private static final int RITES_WARP_THRESHOLD = 20;
    private static final int NORMAL_WARP = 1;
    private static final int TEMPORARY_WARP = 5;
    private static final List<KnowledgeGrant> INSIGHTS = List.of(new KnowledgeGrant(KnowledgeType.OBSERVATION, 2, 1), new KnowledgeGrant(KnowledgeType.THEORY, 3, 2));

    private final Variant variant;

    public ItemCurio(Item.Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("item.curio.text"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TTSounds.LEARN.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (variant.rites && WarpHelper.getActualWarp(player) <= RITES_WARP_THRESHOLD) {
                player.sendSystemMessage(Component.translatable("fail.crimsonrites").withStyle(ChatFormatting.DARK_PURPLE));
                return InteractionResult.SUCCESS;
            }
            if (variant.rites && !KnowledgeAccess.of(player).isResearchKnown(CRIMSON_RITES_RESEARCH)) {
                ResearchManager.complete(serverPlayer, CRIMSON_RITES_RESEARCH);
            }
            INSIGHTS.forEach(insight -> insight.award(serverPlayer));
            if (variant.warping) {
                WarpHelper.addWarp(serverPlayer, NORMAL_WARP, WarpType.NORMAL);
                WarpHelper.addWarp(serverPlayer, TEMPORARY_WARP, WarpType.TEMPORARY);
                if (variant.rites && serverPlayer.getRandom().nextBoolean()) {
                    WarpHelper.addWarp(serverPlayer, NORMAL_WARP, WarpType.PERMANENT);
                }
            }
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
            player.sendSystemMessage(Component.translatable("tc.knowledge.gained").withStyle(ChatFormatting.DARK_PURPLE));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
