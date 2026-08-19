package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.TCResearchCategories;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
        ARCANE(TCResearchCategories.AUROMANCY, false, false), PRESERVED(TCResearchCategories.ALCHEMY, false, false), ANCIENT(TCResearchCategories.GOLEMANCY, false, false), ELDRITCH(
                TCResearchCategories.ELDRITCH, true,
                false), KNOWLEDGE(TCResearchCategories.INFUSION, false, false), TWISTED(TCResearchCategories.ARTIFICE, false, false), RITES(TCResearchCategories.ELDRITCH, true, true);

        private final ResourceKey<IResearchCategory> category;
        private final boolean warping;
        private final boolean rites;

        Variant(ResourceKey<IResearchCategory> category, boolean warping, boolean rites) {
            this.category = category;
            this.warping = warping;
            this.rites = rites;
        }
    }

    private static final Identifier CRIMSON_RITES_RESEARCH = TCIds.rl("crimson_rites");
    private static final int RITES_WARP_THRESHOLD = 20;
    private static final int NORMAL_WARP = 1;
    private static final int TEMPORARY_WARP = 5;

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
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.LEARN.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (variant.rites && WarpHelper.getActualWarp(player) <= RITES_WARP_THRESHOLD) {
                player.sendSystemMessage(Component.translatable("fail.crimsonrites").withStyle(ChatFormatting.DARK_PURPLE));
                return InteractionResult.SUCCESS;
            }
            if (variant.rites && !KnowledgeAccess.of(player).isResearchKnown(CRIMSON_RITES_RESEARCH)) {
                ResearchManager.complete(serverPlayer, CRIMSON_RITES_RESEARCH);
            }
            grantKnowledge(serverPlayer);
            if (variant.warping) {
                WarpHelper.addWarp(serverPlayer, NORMAL_WARP, WarpType.NORMAL);
                WarpHelper.addWarp(serverPlayer, TEMPORARY_WARP, WarpType.TEMPORARY);
                if (variant.rites && serverPlayer.getRandom().nextBoolean()) {
                    WarpHelper.addWarp(serverPlayer, NORMAL_WARP, WarpType.PERMANENT);
                }
            }
            RandomSource random = serverPlayer.getRandom();
            ResearchGrants.grantConvertedKnowledge(serverPlayer, KnowledgeType.OBSERVATION,
                    Mth.randomBetweenInclusive(random, KnowledgeType.OBSERVATION.progression() / 2, KnowledgeType.OBSERVATION.progression()));
            ResearchGrants.grantConvertedKnowledge(serverPlayer, KnowledgeType.THEORY,
                    Mth.randomBetweenInclusive(random, KnowledgeType.THEORY.progression() / 3, KnowledgeType.THEORY.progression() / 2));
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
            player.sendSystemMessage(Component.translatable("tc.knowledge.gained").withStyle(ChatFormatting.DARK_PURPLE));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private static void grantKnowledge(ServerPlayer player) {
        RandomSource random = player.getRandom();
        int observation = KnowledgeType.OBSERVATION.progression();
        int theory = KnowledgeType.THEORY.progression();
        ResearchGrants.grantConvertedKnowledge(player, KnowledgeType.OBSERVATION, Mth.randomBetweenInclusive(random, observation / 2, observation));
        ResearchGrants.grantConvertedKnowledge(player, KnowledgeType.THEORY, Mth.randomBetweenInclusive(random, theory / 3, theory / 2));
    }

    private static Holder<IResearchCategory> categoryHolder(ServerPlayer player, ResourceKey<IResearchCategory> key) {
        return player.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY).getOrThrow(key);
    }

    private static Holder<IResearchCategory> randomCategory(ServerPlayer player) {
        List<Holder.Reference<IResearchCategory>> categories = player.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY).listElements().toList();
        return categories.get(player.getRandom().nextInt(categories.size()));
    }
}
