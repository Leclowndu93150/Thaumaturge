package com.leclowndu93150.thaumaturge.content.spell;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.InscriptionCheck;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class SpellText {
    private static final String PROBLEM = "spell.thaumaturge.problem.";
    private static final String TOOLTIP = "tooltip.thaumaturge.spell.";

    private SpellText() {}

    public static MutableComponent partName(ResourceKey<SpellPart> key) {
        return Component.translatable(SpellPart.nameKey(key));
    }

    public static MutableComponent partDescription(ResourceKey<SpellPart> key) {
        return Component.translatable(SpellPart.descriptionKey(key));
    }

    public static MutableComponent aspectName(ResourceKey<IAspect> aspect) {
        return Component.translatable("aspect." + aspect.identifier().getNamespace() + "." + aspect.identifier().getPath());
    }

    public static MutableComponent kind(SpellPartKind kind) {
        return Component.translatable(kind.nameKey());
    }

    public static MutableComponent kindWithAspect(SpellPartKind kind, ResourceKey<IAspect> aspect) {
        return Component.translatable("spell.thaumaturge.kind_aspect", kind(kind), aspectName(aspect));
    }

    public static MutableComponent style(CastStyle style) {
        return Component.translatable(style.nameKey());
    }

    public static MutableComponent setting(SettingSpec spec) {
        return Component.translatable(spec.nameKey());
    }

    public static MutableComponent researchTitle(Identifier entry) {
        return Component.translatable("research." + entry.getNamespace() + "." + entry.getPath() + ".title");
    }

    public static MutableComponent problem(String key, Object... args) {
        return Component.translatable(PROBLEM + key, args);
    }

    public static Component visCost(Component amount) {
        return Component.translatable(TOOLTIP + "vis_cost", amount).withStyle(ChatFormatting.AQUA);
    }

    public static Component styleLine(CastStyle style) {
        return Component.translatable(TOOLTIP + "style", style(style)).withStyle(ChatFormatting.GRAY);
    }

    public static Component complexityLine(int complexity, int budget) {
        return Component.translatable(TOOLTIP + "complexity", complexity, budget).withStyle(ChatFormatting.GRAY);
    }

    public static Component complexityOnly(int complexity) {
        return Component.translatable(TOOLTIP + "node_complexity", complexity).withStyle(ChatFormatting.GRAY);
    }

    public static Component emptySlot() {
        return Component.translatable(TOOLTIP + "empty_slot").withStyle(ChatFormatting.GRAY);
    }

    public static Component removeHint() {
        return Component.translatable(TOOLTIP + "remove_hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
    }

    public static MutableComponent inspectorLabel(String key) {
        return Component.translatable("gui.thaumaturge.focal_manipulator." + key);
    }

    public static Component inscribing(int percent) {
        return Component.translatable("gui.thaumaturge.focal_manipulator.inscribing", percent);
    }

    public static Component styleHelp(CastStyle style) {
        return Component.translatable(style.nameKey() + ".desc");
    }

    public static Component visHelp(Component vis) {
        return Component.translatable("gui.thaumaturge.focal_manipulator.vis_help", vis);
    }

    public static List<Component> inscribeTooltip(SpellSummary summary, InscriptionCheck check) {
        List<Component> lines = new ArrayList<>();
        if (!check.focused()) {
            lines.add(gui("needs_focus").withStyle(ChatFormatting.RED));
            return lines;
        }
        if (check.busy()) {
            lines.add(gui("busy").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        if (!check.valid()) {
            lines.add(gui("fix_problems").withStyle(ChatFormatting.RED));
        }
        lines.add(gui("inscribe_cost").withStyle(ChatFormatting.GOLD));
        if (check.missingLevels() > 0) {
            lines.add(gui("inscribe_xp_short", summary.xp(), check.missingLevels()).withStyle(ChatFormatting.RED));
        } else {
            lines.add(gui("inscribe_xp", summary.xp()).withStyle(ChatFormatting.GREEN));
        }
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : summary.crystals().entrySet()) {
            Integer lacking = check.missingCrystals().get(entry.getKey());
            if (lacking != null) {
                lines.add(gui("inscribe_crystal_short", entry.getValue(), aspectName(entry.getKey()), lacking).withStyle(ChatFormatting.RED));
            } else {
                lines.add(gui("inscribe_crystal", entry.getValue(), aspectName(entry.getKey())).withStyle(ChatFormatting.AQUA));
            }
        }
        return lines;
    }

    private static MutableComponent gui(String key, Object... args) {
        return Component.translatable("gui.thaumaturge.focal_manipulator." + key, args);
    }

    public static Component invalid() {
        return Component.translatable(TOOLTIP + "invalid").withStyle(ChatFormatting.RED);
    }

    public static Component blank() {
        return Component.translatable(TOOLTIP + "blank").withStyle(ChatFormatting.DARK_GRAY);
    }

    public static Component fizzle() {
        return Component.translatable("spell.thaumaturge.fizzle");
    }

    public static Component notEnoughVis() {
        return Component.translatable("spell.thaumaturge.not_enough_vis");
    }

    public static Component maxLabel() {
        return Component.translatable("spell.thaumaturge.max");
    }
}
