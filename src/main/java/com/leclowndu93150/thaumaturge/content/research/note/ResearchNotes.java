package com.leclowndu93150.thaumaturge.content.research.note;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.items.IScribeTools;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class ResearchNotes {
    private ResearchNotes() {}

    public static int theoryRowsBefore(IResearchEntry entry, int stageIndex) {
        int count = 0;
        for (int i = 0; i < stageIndex && i < entry.stages().size(); i++) {
            count += theoryRows(entry.stages().get(i));
        }
        return count;
    }

    public static int theoryRows(IResearchStage stage) {
        int count = 0;
        for (KnowledgeReward reward : stage.requiredKnowledge()) {
            if (reward.type() == KnowledgeType.THEORY) {
                count++;
            }
        }
        return count;
    }

    public static AspectList anchors(HolderLookup.Provider registries, IResearchEntry entry) {
        AspectList authored = entry.noteAspects();
        if (!authored.isEmpty()) {
            return authored;
        }
        HolderLookup.RegistryLookup<IAspect> lookup = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        AspectList fallback = AspectList.EMPTY;
        for (ResourceKey<IAspect> primal : List.of(TCAspects.AER, TCAspects.IGNIS, TCAspects.AQUA)) {
            fallback = fallback.add(lookup.getOrThrow(primal), 1);
        }
        return fallback;
    }

    public static AspectList observationCost(IResearchEntry entry, int amount) {
        AspectList cost = AspectList.EMPTY;
        for (AspectInstance instance : entry.noteAspects().entries()) {
            cost = cost.add(instance.aspect(), instance.amount() * Math.max(1, amount));
        }
        return cost;
    }

    public static int stageObservationAmount(IResearchStage stage) {
        int amount = -1;
        for (KnowledgeReward reward : stage.requiredKnowledge()) {
            if (reward.type() != KnowledgeType.THEORY) {
                amount = Math.max(amount, reward.amount());
            }
        }
        return amount;
    }

    public static AspectList stageObservationCost(IResearchEntry entry, IResearchStage stage) {
        int amount = stageObservationAmount(stage);
        return amount < 0 ? AspectList.EMPTY : observationCost(entry, amount);
    }

    public static @Nullable ResearchNoteData dataOf(ItemStack stack) {
        return stack.get(TCDataComponents.RESEARCH_NOTE.get());
    }

    public static boolean hasNoteFor(Player player, Identifier learnKey) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ResearchNoteData data = dataOf(inv.getItem(i));
            if (data != null && data.learnKey().equals(learnKey)) {
                return true;
            }
        }
        return false;
    }

    public static boolean obtainNote(ServerPlayer player, Identifier entryId, int ordinal) {
        ResourceKey<IResearchEntry> key = ResourceKey.create(IResearchEntry.REGISTRY_KEY, entryId);
        IResearchEntry entry = player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(key).map(holder -> holder.value()).orElse(null);
        if (entry == null) {
            return false;
        }
        Identifier learnKey = ResearchNoteData.learnKey(entryId, ordinal);
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (knowledge.isResearchKnown(learnKey) || hasNoteFor(player, learnKey)) {
            return false;
        }
        if ((!consumeInk(player, true) || !hasItem(player, Items.PAPER)) && !player.getAbilities().instabuild) {
            player.sendSystemMessage(Component.translatable("tc.researchnote.missing").withStyle(ChatFormatting.DARK_PURPLE));
            return false;
        }
        consumeInk(player, false);
        consumeItem(player, Items.PAPER);
        AspectList anchors = anchors(player.registryAccess(), entry);
        ResearchNoteData data = NoteGenerator.generate(entryId, ordinal, anchors, entry.complexity(), player.getRandom());
        ItemStack note = new ItemStack(TCItems.RESEARCH_NOTE.get());
        note.set(TCDataComponents.RESEARCH_NOTE.get(), data);
        if (!player.getInventory().add(note)) {
            player.drop(note, false);
        }
        player.level().playSound(null, player, TCSounds.WRITE.get(), SoundSource.UI, 0.5F, 1.0F);
        return true;
    }

    public static boolean consumeInk(Player player, boolean simulate) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof IScribeTools && stack.getDamageValue() < stack.getMaxDamage()) {
                if (!simulate) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }
                return true;
            }
        }
        if (player.containerMenu instanceof MenuResearchTable table) {
            if (table.blockEntity() != null && table.blockEntity().hasInkReady()) {
                if (!simulate) {
                    return table.blockEntity().consumeInk();
                }
                return true;
            }
        }
        return false;
    }

    private static boolean hasItem(Player player, Item item) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static void consumeItem(Player player, Item item) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return;
            }
        }
    }

    public static Component entryName(HolderLookup.Provider registries, Identifier entryId) {
        return registries.lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, entryId))
                .map(holder -> (Component) Component.translatable(holder.value().nameKey())).orElse(Component.literal(entryId.toString()));
    }
}
