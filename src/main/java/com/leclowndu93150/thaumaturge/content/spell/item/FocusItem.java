package com.leclowndu93150.thaumaturge.content.spell.item;

import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class FocusItem extends Item {
    private static final String INDENT = "  ";

    public FocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        describe(stack, context.registries(), builder);
    }

    public static void describe(ItemStack focus, HolderLookup.Provider registries, Consumer<Component> builder) {
        Spell spell = Spells.spellOf(focus);
        if (spell == null || registries == null) {
            builder.accept(SpellText.blank());
            return;
        }
        Optional<SpellSummary> summary = FocusItems.summary(focus, registries, null);
        if (summary.isEmpty()) {
            return;
        }
        builder.accept(SpellText.visCost(FocusItems.formatVis(summary.get().vis())));
        builder.accept(SpellText.styleLine(spell.style()));
        for (SpellProblem problem : summary.get().problems()) {
            if (problem.fatal()) {
                builder.accept(SpellText.invalid());
                builder.accept(problem.message().copy().withStyle(ChatFormatting.RED));
                break;
            }
        }
        for (SpellNode child : spell.root().children()) {
            line(child, registries, builder, 0);
        }
    }

    private static void line(SpellNode node, HolderLookup.Provider registries, Consumer<Component> builder, int depth) {
        Optional<SpellPart> part = Spells.part(registries, node.part());
        MutableComponent text = Component.literal(INDENT.repeat(depth)).append(SpellText.partName(node.part()).withStyle(ChatFormatting.DARK_PURPLE));
        if (part.isPresent()) {
            if (part.get().aspect().selectable()) {
                part.get().aspect().resolve(node.aspect(), registries)
                        .ifPresent(aspect -> text.append(Component.literal(" (").append(SpellText.aspectName(aspect)).append(")").withStyle(ChatFormatting.GOLD)));
            }
            MutableComponent values = Component.empty();
            boolean first = true;
            for (SettingSpec spec : part.get().settings()) {
                values.append(first ? Component.literal(" [") : Component.literal(", "));
                values.append(SpellText.setting(spec)).append(" ").append(spec.label(node.settings().getOrDefault(spec.key(), spec.defaultValue())));
                first = false;
            }
            if (!first) {
                text.append(values.append("]").withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        builder.accept(text);
        for (SpellNode child : node.children()) {
            line(child, registries, builder, depth + 1);
        }
    }
}
