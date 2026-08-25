package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.api.research.ResearchEvent;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.network.ClientboundKnowledgeGainPayload;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class ResearchManager {
    private static final int STAGE_XP_REWARD = 5;
    private static final String CRAFTED_PREFIX = "crafted/";

    private ResearchManager() {}

    public static ResourceLocation craftedKey(ResourceLocation item) {
        return ResourceLocation.fromNamespaceAndPath(
                "thaumaturge", CRAFTED_PREFIX + item.getNamespace() + "/" + item.getPath());
    }

    public static boolean unlock(ServerPlayer player, ResourceLocation research) {
        if (research == null) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (knowledge.isResearchKnown(research)) return false;
        IResearchEntry entry = entry(player, research).orElse(null);
        if (entry != null && !parentsSatisfied(knowledge, entry)) return false;
        ResearchEvent.Unlocked event = new ResearchEvent.Unlocked(player, research);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        boolean changed = knowledge.addResearch(research);
        if (changed) {
            knowledge.sync(player);
        }
        return changed;
    }

    public static boolean advanceStage(ServerPlayer player, ResourceLocation research) {
        return advanceStage(player, research, true);
    }

    public static boolean advanceStage(ServerPlayer player, ResourceLocation research, boolean checkRequisites) {
        if (research == null) return false;
        IResearchEntry entry = entry(player, research).orElse(null);
        if (entry == null) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(research)) return false;
        if (knowledge.isResearchComplete(research)) return false;
        int currentStage = knowledge.researchStage(research);
        int totalStages = entry.stages().size();
        if (currentStage >= totalStages) return false;
        IResearchStage finished = entry.stages().get(Math.max(0, currentStage));
        if (checkRequisites && !stageRequirementsMet(player, knowledge, entry, research, Math.max(0, currentStage))) {
            return false;
        }
        int next = currentStage + 1;
        ResearchEvent.StageAdvanced event = new ResearchEvent.StageAdvanced(player, research, currentStage, next);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        if (checkRequisites) {
            consumeStageRequirements(player, knowledge, entry, finished);
        }
        applyStageEffects(player, knowledge, finished);
        if (next == totalStages - 1 && stageHasNoRequirements(entry.stages().get(next))) {
            applyStageEffects(player, knowledge, entry.stages().get(next));
            next = totalStages;
        }
        if (next < totalStages) {
            knowledge.setResearchStage(research, next);
        } else {
            markCompleteInternal(player, knowledge, research);
        }
        player.giveExperiencePoints(STAGE_XP_REWARD);
        knowledge.sync(player);
        return true;
    }

    public static boolean setStage(ServerPlayer player, ResourceLocation research, int stage) {
        if (research == null || stage <= 0) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(research)) return false;
        int previous = knowledge.researchStage(research);
        if (previous == stage) return false;
        ResearchEvent.StageAdvanced event = new ResearchEvent.StageAdvanced(player, research, previous, stage);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        knowledge.setResearchStage(research, stage);
        knowledge.sync(player);
        return true;
    }

    public static boolean complete(ServerPlayer player, ResourceLocation research) {
        if (research == null) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(research)) {
            if (!unlockSilent(knowledge, research)) return false;
        }
        if (knowledge.isResearchComplete(research)) return false;
        boolean changed = markCompleteInternal(player, knowledge, research);
        if (changed) knowledge.sync(player);
        return changed;
    }

    public static boolean gainKnowledge(
            ServerPlayer player, KnowledgeType type, Holder<IResearchCategory> category, int amount) {
        if (type == null || amount == 0) return false;
        ResearchEvent.KnowledgeGained event = new ResearchEvent.KnowledgeGained(player, type, category, amount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        ResourceKey<IResearchCategory> key =
                category == null ? null : category.unwrapKey().orElse(null);
        int pointsBefore = knowledge.knowledge(type, key);
        boolean changed = knowledge.addKnowledge(type, key, amount);
        if (changed) knowledge.sync(player);
        int pointsGained = knowledge.knowledge(type, key) - pointsBefore;
        if (pointsGained > 0) {
            PacketDistributor.sendToPlayer(
                    player, new ClientboundKnowledgeGainPayload(type, Optional.ofNullable(key), pointsGained));
        }
        return changed;
    }

    public static void applyAutoUnlock(ServerPlayer player) {
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        knowledge.applyAutoUnlock(player.registryAccess());
        knowledge.sync(player);
    }

    public static boolean doesPassGate(Player player, @Nullable ResearchGate gate) {
        if (gate == null) return true;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        boolean known = gate.stage().isPresent()
                ? knowledge.isResearchKnown(gate.entry(), gate.stage().get())
                : knowledge.isResearchComplete(gate.entry());
        return gate.negate() != known;
    }

    public static IPlayerKnowledge of(ServerPlayer player) {
        return KnowledgeAccess.of(player);
    }

    public static boolean parentsSatisfied(IPlayerKnowledge knowledge, IResearchEntry entry) {
        for (ResearchParent parent : entry.parents()) {
            if (!parent.isSatisfiedBy(knowledge)) return false;
        }
        return true;
    }

    public static boolean stageRequirementsMet(
            Player player, IPlayerKnowledge knowledge, IResearchEntry entry, ResourceLocation entryId, int stageIndex) {
        IResearchStage stage = entry.stages().get(stageIndex);
        for (ResourceLocation required : stage.requiredResearch()) {
            if (!knowledge.isResearchComplete(required)) return false;
        }
        for (ResearchRequirement req : stage.obtain()) {
            if (countMatching(player, req) < req.amount()) return false;
        }
        for (ResearchRequirement req : stage.craft()) {
            if (!isCraftSatisfied(player, knowledge, req)) return false;
        }
        int theoryOrdinal = ResearchNotes.theoryRowsBefore(entry, stageIndex);
        for (KnowledgeReward cost : stage.requiredKnowledge()) {
            if (cost.type() == KnowledgeType.THEORY) {
                if (!knowledge.isResearchKnown(ResearchNoteData.learnKey(entryId, theoryOrdinal))) return false;
                theoryOrdinal++;
            }
        }
        AspectList observation = ResearchNotes.stageObservationCost(entry, stage);
        if (!observation.isEmpty() && !AspectPools.canAfford(player, observation)) return false;
        return true;
    }

    public static boolean isCraftSatisfied(Player player, IPlayerKnowledge knowledge, ResearchRequirement req) {
        for (Holder<Item> holder : req.items()) {
            ResourceLocation itemId =
                    holder.unwrapKey().map(ResourceKey::location).orElse(null);
            if (itemId != null && knowledge.isResearchKnown(craftedKey(itemId))) return true;
        }
        return countMatching(player, req) > 0;
    }

    public static int countMatching(Player player, ResearchRequirement req) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (req.matches(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consumeStageRequirements(
            ServerPlayer player, PlayerKnowledge knowledge, IResearchEntry entry, IResearchStage stage) {
        for (ResearchRequirement req : stage.obtain()) {
            int remaining = req.amount();
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (req.matches(stack)) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                }
            }
        }
        AspectList observation = ResearchNotes.stageObservationCost(entry, stage);
        if (!observation.isEmpty()) {
            AspectPools.spendAll(player, observation);
        }
    }

    private static boolean stageHasNoRequirements(IResearchStage stage) {
        return stage.obtain().isEmpty()
                && stage.craft().isEmpty()
                && stage.requiredKnowledge().isEmpty()
                && stage.requiredResearch().isEmpty();
    }

    private static boolean markCompleteInternal(
            ServerPlayer player, PlayerKnowledge knowledge, ResourceLocation research) {
        ResearchEvent.Completed event = new ResearchEvent.Completed(player, research);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        boolean changed = knowledge.markComplete(research);
        if (!changed) return false;
        IResearchEntry entry = entry(player, research).orElse(null);
        if (entry != null) {
            knowledge.setResearchStage(research, entry.stages().size());
            knowledge.setResearchFlag(research, ResearchFlag.POPUP);
            knowledge.setResearchFlag(research, ResearchFlag.RESEARCH);
            notifyAddenda(player, knowledge, research);
            for (ResourceLocation sibling : entry.siblings()) {
                if (knowledge.isResearchComplete(sibling)) continue;
                IResearchEntry siblingEntry = entry(player, sibling).orElse(null);
                if (siblingEntry != null && !parentsSatisfied(knowledge, siblingEntry)) continue;
                if (!knowledge.isResearchKnown(sibling)) {
                    knowledge.addResearch(sibling);
                }
                markCompleteInternal(player, knowledge, sibling);
            }
        }
        return true;
    }

    private static void notifyAddenda(ServerPlayer player, PlayerKnowledge knowledge, ResourceLocation completed) {
        player.registryAccess()
                .lookup(IResearchEntry.REGISTRY_KEY)
                .ifPresent(lookup -> lookup.listElements().forEach(holder -> {
                    IResearchEntry other = holder.value();
                    if (other.addenda().isEmpty()) return;
                    ResourceLocation otherId = holder.key().location();
                    if (!knowledge.isResearchComplete(otherId)) return;
                    for (ResearchAddendum addendum : other.addenda()) {
                        if (addendum.requiredResearch().contains(completed)) {
                            player.sendSystemMessage(
                                    Component.translatable("tc.addaddendum", Component.translatable(other.nameKey()))
                                            .withStyle(ChatFormatting.DARK_PURPLE));
                            knowledge.setResearchFlag(otherId, ResearchFlag.PAGE);
                            break;
                        }
                    }
                }));
    }

    private static boolean unlockSilent(PlayerKnowledge knowledge, ResourceLocation research) {
        return knowledge.addResearch(research);
    }

    private static void applyStageEffects(ServerPlayer player, PlayerKnowledge knowledge, IResearchStage stage) {
        for (KnowledgeReward reward : stage.knowledge()) {
            ResourceKey<IResearchCategory> key = reward.category().unwrapKey().orElse(null);
            knowledge.addKnowledge(reward.type(), key, reward.amount());
        }
        if (stage.warp() > 0) {
            applyWarp(player, stage.warp());
        }
    }

    public static void applyWarp(ServerPlayer player, int amount) {
        if (amount <= 0 || ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        if (amount > 1) {
            int normal = amount / 2;
            WarpHelper.addWarp(player, normal, WarpType.NORMAL);
            WarpHelper.addWarp(player, amount - normal, WarpType.PERMANENT);
        } else {
            WarpHelper.addWarp(player, amount, WarpType.PERMANENT);
        }
    }

    private static Optional<IResearchEntry> entry(ServerPlayer player, ResourceLocation research) {
        return player.registryAccess()
                .lookup(IResearchEntry.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)))
                .map(Holder.Reference::value);
    }
}
