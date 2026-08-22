package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.CategoryComponents;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class TCTooltips {
    private static final String SCANNED_ASPECT_PREFIX = "scanned/aspect/";

    private TCTooltips() {}

    public static Component categoryName(ResourceKey<IResearchCategory> key) {
        return CategoryComponents.name(key);
    }

    public static Component categoryPercent(ResourceKey<IResearchCategory> key, int percent) {
        return Component.translatable("tc.research_category.percent", CategoryComponents.name(key), percent);
    }

    public static Component knowledgeLabel(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return Component.translatable(
                "tc.knowledge.tooltip",
                Component.translatable("knowledge_type.thaumaturge." + type.getSerializedName()),
                CategoryComponents.name(category));
    }

    public static Component need(String which) {
        return Component.translatable("tc.need." + which);
    }

    public static Component cardUnknown() {
        return Component.translatable("tc.card.unknown");
    }

    public static Component noInkLine0() {
        return Component.translatable("tile.researchtable.noink.0");
    }

    public static Component noInkLine1() {
        return Component.translatable("tile.researchtable.noink.1");
    }

    public static Component noPaperLine0() {
        return Component.translatable("tile.researchtable.nopaper.0");
    }

    public static Component returnLabel() {
        return Component.translatable("recipe.return");
    }

    public static Component beginResearch() {
        return Component.translatable("tc.research.begin").withStyle(ChatFormatting.GREEN);
    }

    public static Component researchStage(int stage, int total) {
        return Component.translatable("tc.research.stage")
                .append(Component.literal(" "))
                .append(Component.translatable("tc.research.stage.short", stage, total))
                .withStyle(ChatFormatting.AQUA);
    }

    public static Component researchMissing() {
        return Component.translatable("tc.researchmissing").withStyle(ChatFormatting.RED);
    }

    public static Component researchNew() {
        return Component.translatable("tc.research.newresearch");
    }

    public static Component pageNew() {
        return Component.translatable("tc.research.newpage");
    }

    public static MutableComponent entryNameGold(Component name) {
        return Component.empty().append(name).withStyle(ChatFormatting.GOLD);
    }

    public static List<Component> entryHover(
            Component name,
            EntryStatus status,
            int currentStage,
            int totalStages,
            List<Component> missingParents,
            boolean hasNewResearch,
            boolean hasNewPage) {
        List<Component> lines = new ArrayList<>();
        lines.add(entryNameGold(name));
        switch (status) {
            case CAN_UNLOCK -> {
                if (currentStage > 0) {
                    lines.add(researchStage(currentStage, totalStages));
                } else {
                    lines.add(beginResearch());
                }
            }
            case MISSING_PREREQ -> {
                lines.add(researchMissing());
                for (Component parent : missingParents) {
                    lines.add(Component.literal(" - ").append(parent).withStyle(ChatFormatting.YELLOW));
                }
            }
            case COMPLETE -> {}
        }
        if (hasNewResearch) lines.add(researchNew());
        if (hasNewPage) lines.add(pageNew());
        return lines;
    }

    public static Component prereqEntryName(ResourceLocation id) {
        String path = id.getPath();
        if (path.startsWith(SCANNED_ASPECT_PREFIX)) {
            String[] parts = path.substring(SCANNED_ASPECT_PREFIX.length()).split("/");
            return parts.length < 2
                    ? Component.translatable("aspect." + TCIds.MODID + "." + parts[0])
                    : Component.translatable("aspect." + parts[0] + "." + parts[1]);
        }
        return entryNameKey(id)
                .map(Component::translatable)
                .orElseGet(() -> Component.translatableWithFallback(
                        "research." + id.getNamespace() + "." + path + ".title", "?"));
    }

    private static Optional<String> entryNameKey(ResourceLocation id) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return Optional.empty();
        }
        return player.registryAccess()
                .lookup(IResearchEntry.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, id)))
                .map(holder -> holder.value().nameKey());
    }

    public enum EntryStatus {
        CAN_UNLOCK,
        MISSING_PREREQ,
        COMPLETE
    }
}
