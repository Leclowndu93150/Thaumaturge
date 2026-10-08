package com.leclowndu93150.thaumaturge.content.spell.item;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FocusItem extends Item {
    private static final String INDENT = "  ";

    public FocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> builder, TooltipFlag flag) {
        describe(stack, context.registries(), builder::add);
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
        MutableComponent text = SpellText.partName(node.part()).withStyle(ChatFormatting.DARK_PURPLE);
        if (part.isPresent()) {
            if (part.get().aspect().selectable()) {
                Optional<ResourceKey<IAspect>> aspect = part.get().aspect().resolve(node.aspect(), registries);
                if (aspect.isPresent()) {
                    text = Component.translatable(
                            "tooltip.thaumaturge.focus.with_aspect",
                            text,
                            SpellText.aspectName(aspect.get()).withStyle(ChatFormatting.GOLD));
                }
            }
            MutableComponent values = null;
            for (SettingSpec spec : part.get().settings()) {
                Component value = Component.translatable(
                        "tooltip.thaumaturge.focus.setting",
                        SpellText.setting(spec),
                        spec.label(node.settings().getOrDefault(spec.key(), spec.defaultValue())));
                values = values == null
                        ? value.copy()
                        : Component.translatable("tooltip.thaumaturge.list", values, value);
            }
            if (values != null) {
                text = Component.translatable(
                        "tooltip.thaumaturge.focus.with_settings", text, values.withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        builder.accept(Component.literal(INDENT.repeat(depth)).append(text));
        for (SpellNode child : node.children()) {
            line(child, registries, builder, depth + 1);
        }
    }
}
