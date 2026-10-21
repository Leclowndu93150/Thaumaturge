package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
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
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchEvent;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.api.research.ResearchUnlockConditions;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.network.ClientboundKnowledgeGainPayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class ResearchManager {
    private static final int STAGE_EXPERIENCE = 5;
    private static final String CRAFTED_SEGMENT = "crafted";
    private static final String PATH_SEPARATOR = "/";
    private static final String ADDENDUM_KEY = "message.thaumaturge.research.addendum_added";
    private static final int MIN_OBSERVATION_MULTIPLIER = 1;
    private static final int WARP_NORMAL_DIVISOR = 2;
    private static final int SINGLE_WARP = 1;
    private static final int FIRST_STAGE = 0;

    private ResearchManager() {}

    public static Identifier craftedKey(Identifier item) {
        return TTIds.rl(String.join(PATH_SEPARATOR, CRAFTED_SEGMENT, item.getNamespace(), item.getPath()));
    }

    public static boolean unlockRequested(ServerPlayer player, Identifier id) {
        IResearchEntry entry = entryOf(player, id);
        return entry != null && categoryOpen(player, entry) && !isHiddenRoot(entry) && unlock(player, id);
    }

    private static boolean categoryOpen(ServerPlayer player, IResearchEntry entry) {
        return entry.category().value().requiredResearch().map(of(player)::isResearchComplete).orElse(true);
    }

    private static boolean isHiddenRoot(IResearchEntry entry) {
        return entry.parents().isEmpty() && entry.hasMeta(ResearchEntryMeta.HIDDEN);
    }

    public static boolean unlock(ServerPlayer player, Identifier id) {
        if (id == null) {
            return false;
        }
        PlayerKnowledge knowledge = record(player);
        if (!mayUnlock(player, knowledge, id)) {
            return false;
        }
        knowledge.addResearch(id);
        knowledge.sync(player);
        return true;
    }

    private static boolean mayUnlock(ServerPlayer player, PlayerKnowledge knowledge, Identifier id) {
        if (knowledge.isResearchKnown(id)) {
            return false;
        }
        IResearchEntry entry = entryOf(player, id);
        boolean parentsOk = entry == null || parentsSatisfied(knowledge, entry);
        return parentsOk && ResearchUnlockConditions.passes(player, knowledge, id) && !vetoed(new ResearchEvent.Unlocked(player, id));
    }

    public static boolean advanceStage(ServerPlayer player, Identifier id) {
        return advanceStage(player, id, true);
    }

    public static boolean advanceStage(ServerPlayer player, Identifier id, boolean checkRequirements) {
        if (id == null) {
            return false;
        }
        IResearchEntry entry = entryOf(player, id);
        PlayerKnowledge knowledge = record(player);
        OptionalInt reachable = reachableStage(knowledge, entry, id);
        if (reachable.isEmpty()) {
            return false;
        }
        int current = reachable.getAsInt();
        if (checkRequirements && !stageRequirementsMet(player, knowledge, entry, id, current)) {
            return false;
        }
        if (vetoed(new ResearchEvent.StageAdvanced(player, id, current, current + 1))) {
            return false;
        }
        if (checkRequirements) {
            consume(player, entry, entry.stages().get(current));
        }
        progressTo(player, knowledge, entry, id, current);
        finishAdvance(player, knowledge);
        return true;
    }

    private static OptionalInt reachableStage(PlayerKnowledge knowledge, @Nullable IResearchEntry entry, Identifier id) {
        if (entry == null || !knowledge.isResearchKnown(id) || knowledge.isResearchComplete(id)) {
            return OptionalInt.empty();
        }
        int current = knowledge.researchStage(id);
        return current < entry.stages().size() ? OptionalInt.of(current) : OptionalInt.empty();
    }

    private static void finishAdvance(ServerPlayer player, PlayerKnowledge knowledge) {
        player.giveExperiencePoints(STAGE_EXPERIENCE);
        knowledge.sync(player);
    }

    private static void progressTo(ServerPlayer player, PlayerKnowledge knowledge, IResearchEntry entry, Identifier id, int current) {
        List<IResearchStage> stages = entry.stages();
        int total = stages.size();
        applyStageEffects(stages.get(Math.max(current, FIRST_STAGE)), knowledge, player);
        IResearchStage finalStage = stages.get(total - 1);
        boolean finishNow = current + 1 == total - 1 && !hasRequirements(finalStage);
        if (finishNow) {
            applyStageEffects(finalStage, knowledge, player);
        }
        int next = finishNow ? total : current + 1;
        if (next >= total) {
            complete(player, id);
        } else {
            knowledge.setResearchStage(id, next);
        }
    }

    public static boolean setStage(ServerPlayer player, Identifier id, int stage) {
        PlayerKnowledge knowledge = record(player);
        if (id == null || stage <= 0 || !knowledge.isResearchKnown(id)) {
            return false;
        }
        int current = knowledge.researchStage(id);
        if (current == stage || vetoed(new ResearchEvent.StageAdvanced(player, id, current, stage))) {
            return false;
        }
        knowledge.setResearchStage(id, stage);
        knowledge.sync(player);
        return true;
    }

    public static boolean complete(ServerPlayer player, Identifier id) {
        if (id == null) {
            return false;
        }
        PlayerKnowledge knowledge = record(player);
        boolean added = false;
        if (!knowledge.isResearchKnown(id)) {
            added = knowledge.addResearch(id);
            if (!added) {
                return false;
            }
        }
        if (knowledge.isResearchComplete(id) || vetoed(new ResearchEvent.Completed(player, id)) || !knowledge.markComplete(id)) {
            if (added) {
                knowledge.sync(player);
            }
            return false;
        }
        IResearchEntry entry = entryOf(player, id);
        if (entry != null) {
            markFinished(knowledge, id, entry);
            notifyAddenda(player, knowledge, id);
            completeSiblings(player, knowledge, entry);
        }
        knowledge.sync(player);
        return true;
    }

    public static boolean gainKnowledge(ServerPlayer player, KnowledgeType type, Holder<IResearchCategory> category, int amount) {
        if (type == null || amount == 0) {
            return false;
        }
        ResearchEvent gainEvent = new ResearchEvent.KnowledgeGained(player, type, category, amount);
        if (vetoed(gainEvent)) {
            return false;
        }
        PlayerKnowledge knowledge = record(player);
        ResourceKey<IResearchCategory> key = categoryKey(category);
        int before = knowledge.knowledge(type, key);
        if (!knowledge.addKnowledge(type, key, amount)) {
            return false;
        }
        knowledge.sync(player);
        int gained = knowledge.knowledge(type, key) - before;
        if (gained > 0) {
            PacketDistributor.sendToPlayer(player, new ClientboundKnowledgeGainPayload(type, Optional.ofNullable(key), gained));
        }
        return true;
    }

    public static void applyAutoUnlock(ServerPlayer player) {
        syncAfter(player, knowledge -> knowledge.applyAutoUnlock(player));
    }

    private static void syncAfter(ServerPlayer player, Consumer<PlayerKnowledge> change) {
        PlayerKnowledge knowledge = record(player);
        change.accept(knowledge);
        knowledge.sync(player);
    }

    private static void markFinished(PlayerKnowledge knowledge, Identifier id, IResearchEntry entry) {
        knowledge.setResearchStage(id, entry.stages().size());
        Stream.of(ResearchFlag.POPUP, ResearchFlag.RESEARCH).forEach(flag -> knowledge.setResearchFlag(id, flag));
    }

    public static boolean doesPassGate(Player player, @Nullable ResearchGate gate) {
        if (gate == null) {
            return true;
        }
        return doesPassGate(KnowledgeAccess.of(player), gate);
    }

    public static boolean doesPassGate(IPlayerKnowledge knowledge, @Nullable ResearchGate gate) {
        return gate == null || gate.requirements().stream().allMatch(requirement -> passesRequirement(knowledge, requirement));
    }

    private static boolean passesRequirement(IPlayerKnowledge knowledge, ResearchGate gate) {
        boolean known = gate.stage().map(stage -> knowledge.isResearchKnown(gate.entry(), stage)).orElseGet(() -> knowledge.isResearchComplete(gate.entry()));
        return gate.negate() != known;
    }

    public static IPlayerKnowledge of(ServerPlayer player) {
        return KnowledgeAccess.of(player);
    }

    public static boolean parentsSatisfied(IPlayerKnowledge knowledge, IResearchEntry entry) {
        return entry.parents().stream().allMatch(parent -> parent.isSatisfiedBy(knowledge));
    }

    public static boolean stageRequirementsMet(Player player, IPlayerKnowledge knowledge, IResearchEntry entry, Identifier id, int stageIndex) {
        if (stageIndex < 0 || stageIndex >= entry.stages().size()) {
            return false;
        }
        IResearchStage stage = entry.stages().get(stageIndex);
        boolean prerequisitesDone = stage.requiredResearch().stream().allMatch(knowledge::isResearchComplete);
        boolean itemsHeld = stage.obtain().stream().allMatch(requirement -> countMatching(player, requirement) >= requirement.amount());
        boolean craftsDone = stage.craft().stream().allMatch(requirement -> isCraftSatisfied(player, knowledge, requirement));
        if (!prerequisitesDone || !itemsHeld || !craftsDone || !theoryNotesKnown(knowledge, entry, id, stageIndex)) {
            return false;
        }
        AspectList cost = aspectCost(entry, stage);
        return cost.isEmpty() || AspectPoolAccess.canAfford(player, cost);
    }

    public static boolean isCraftSatisfied(Player player, IPlayerKnowledge knowledge, ResearchRequirement requirement) {
        for (Holder<Item> item : requirement.items()) {
            Optional<ResourceKey<Item>> key = item.unwrapKey();
            if (key.isPresent() && knowledge.isResearchKnown(craftedKey(key.get().identifier()))) {
                return true;
            }
        }
        return countMatching(player, requirement) > 0;
    }

    public static int countMatching(Player player, ResearchRequirement requirement) {
        Inventory inventory = player.getInventory();
        return IntStream.range(0, inventory.getContainerSize()).mapToObj(inventory::getItem).filter(stack -> !stack.isEmpty() && requirement.matches(stack)).mapToInt(ItemStack::getCount).sum();
    }

    public static void applyWarp(ServerPlayer player, int amount) {
        if (amount <= 0 || ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        if (amount == SINGLE_WARP) {
            WarpHelper.addWarp(player, SINGLE_WARP, WarpType.PERMANENT);
            return;
        }
        int normal = amount / WARP_NORMAL_DIVISOR;
        WarpHelper.addWarp(player, normal, WarpType.NORMAL);
        WarpHelper.addWarp(player, amount - normal, WarpType.PERMANENT);
    }

    private static PlayerKnowledge record(ServerPlayer player) {
        return player.getData(TTAttachments.KNOWLEDGE);
    }

    private static @Nullable IResearchEntry entryOf(ServerPlayer player, Identifier id) {
        return player.registryAccess().lookup(IResearchEntry.REGISTRY_KEY).flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, id))).map(Holder.Reference::value).orElse(null);
    }

    private static @Nullable ResourceKey<IResearchCategory> categoryKey(@Nullable Holder<IResearchCategory> category) {
        return category == null ? null : category.unwrapKey().orElse(null);
    }

    private static boolean vetoed(ResearchEvent event) {
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    private static boolean hasRequirements(IResearchStage stage) {
        return !stage.obtain().isEmpty() || !stage.craft().isEmpty() || !stage.requiredKnowledge().isEmpty() || !stage.requiredResearch().isEmpty();
    }

    private static void applyStageEffects(IResearchStage stage, PlayerKnowledge knowledge, ServerPlayer player) {
        stage.knowledge().forEach(reward -> knowledge.addKnowledge(reward.type(), categoryKey(reward.category()), reward.amount()));
        if (stage.warp() > 0) {
            applyWarp(player, stage.warp());
        }
    }

    private static int theoryRowsBefore(List<IResearchStage> stages, int stageIndex) {
        return (int) stages.subList(0, stageIndex).stream().flatMap(stage -> stage.requiredKnowledge().stream()).filter(reward -> reward.type() == KnowledgeType.THEORY).count();
    }

    private static boolean theoryNotesKnown(IPlayerKnowledge knowledge, IResearchEntry entry, Identifier id, int stageIndex) {
        int offset = theoryRowsBefore(entry.stages(), stageIndex);
        long theoryRows = entry.stages().get(stageIndex).requiredKnowledge().stream().filter(reward -> reward.type() == KnowledgeType.THEORY).count();
        return IntStream.range(0, (int) theoryRows).allMatch(row -> knowledge.isResearchKnown(ResearchNoteData.learnKey(id, offset + row)));
    }

    private static AspectList aspectCost(IResearchEntry entry, IResearchStage stage) {
        OptionalInt observation = stage.requiredKnowledge().stream().filter(reward -> reward.type() != KnowledgeType.THEORY).mapToInt(KnowledgeReward::amount).max();
        if (observation.isEmpty()) {
            return AspectList.EMPTY;
        }
        int multiplier = Math.max(MIN_OBSERVATION_MULTIPLIER, observation.getAsInt());
        AspectList cost = AspectList.EMPTY;
        for (AspectInstance instance : entry.noteAspects().entries()) {
            cost = cost.add(instance.aspect(), instance.amount() * multiplier);
        }
        return cost;
    }

    private static void consume(ServerPlayer player, IResearchEntry entry, IResearchStage stage) {
        for (ResearchRequirement requirement : stage.obtain()) {
            removeMatching(player.getInventory(), requirement);
        }
        AspectList cost = aspectCost(entry, stage);
        if (!cost.isEmpty()) {
            AspectPoolAccess.spendAll(player, cost);
        }
    }

    private static void removeMatching(Inventory inventory, ResearchRequirement requirement) {
        int remaining = requirement.amount();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && requirement.matches(stack)) {
                remaining -= inventory.removeItem(slot, remaining).getCount();
            }
        }
    }

    private static void notifyAddenda(ServerPlayer player, PlayerKnowledge knowledge, Identifier completed) {
        player.registryAccess().lookup(IResearchEntry.REGISTRY_KEY).ifPresent(lookup -> lookup.listElements().forEach(holder -> {
            Identifier examinedId = holder.key().identifier();
            IResearchEntry examined = holder.value();
            if (examined.addenda().isEmpty() || !knowledge.isResearchComplete(examinedId) || !references(examined, completed)) {
                return;
            }
            player.sendSystemMessage(Component.translatable(ADDENDUM_KEY, Component.translatable(examined.nameKey())).withStyle(ChatFormatting.DARK_PURPLE));
            knowledge.setResearchFlag(examinedId, ResearchFlag.PAGE);
        }));
    }

    private static boolean references(IResearchEntry examined, Identifier completed) {
        for (ResearchAddendum addendum : examined.addenda()) {
            if (addendum.requiredResearch().contains(completed)) {
                return true;
            }
        }
        return false;
    }

    private static void completeSiblings(ServerPlayer player, PlayerKnowledge knowledge, IResearchEntry entry) {
        for (Identifier sibling : entry.siblings()) {
            if (knowledge.isResearchComplete(sibling)) {
                continue;
            }
            IResearchEntry siblingEntry = entryOf(player, sibling);
            if (siblingEntry != null && !parentsSatisfied(knowledge, siblingEntry)) {
                continue;
            }
            if (!ResearchUnlockConditions.passes(player, knowledge, sibling)) {
                continue;
            }
            knowledge.addResearch(sibling);
            complete(player, sibling);
        }
    }
}
