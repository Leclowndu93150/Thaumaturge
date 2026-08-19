package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumRoot;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class ItemFocus extends Item {
    public static final int WHITE = 0xFFFFFF;
    private static final int VIS_COST_DIVISOR = 5;
    private static final int MIN_ACTIVATION_TICKS = 5;
    private static final int ACTIVATION_COST_DIVISOR = 5;
    private static final int ACTIVATION_SCALE_DIVISOR = 4;
    private static final String INDENT = "  ";

    private final int maxComplexity;

    public ItemFocus(Properties properties, int maxComplexity) {
        super(properties);
        this.maxComplexity = maxComplexity;
    }

    public static void setPackage(ItemStack focusStack, FocusPackage core) {
        focusStack.set(TCDataComponents.FOCUS_PACKAGE.get(), core);
    }

    public static @Nullable FocusPackage getPackage(ItemStack focusStack) {
        if (focusStack.isEmpty()) {
            return null;
        }
        return focusStack.get(TCDataComponents.FOCUS_PACKAGE.get());
    }

    public static int getFocusColor(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            ItemStackTemplate socketed = focusStack.get(TCDataComponents.SOCKETED_FOCUS.get());
            if (socketed != null) {
                core = socketed.get(TCDataComponents.FOCUS_PACKAGE.get());
            }
        }
        return getFocusColor(core);
    }

    public static int getFocusColor(@Nullable FocusPackage core) {
        if (core == null) {
            return WHITE;
        }
        List<Identifier> effects = FocusEngine.effectIds(core);
        if (effects.isEmpty()) {
            return WHITE;
        }
        int r = 0;
        int g = 0;
        int b = 0;
        for (Identifier effectId : effects) {
            int color = FocusEngine.color(effectId);
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }
        r /= effects.size();
        g /= effects.size();
        b /= effects.size();
        return (r << 16) | (g << 8) | b;
    }

    public @Nullable String getSortingHelper(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            return null;
        }
        return focusStack.getHoverName().getString() + core.sortingHash();
    }

    public float getVisCost(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        return core == null ? 0.0F : (float) core.complexity() / VIS_COST_DIVISOR;
    }

    public int getActivationTime(ItemStack focusStack) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            return 0;
        }
        int complexity = core.complexity();
        return Math.max(MIN_ACTIVATION_TICKS, complexity / ACTIVATION_COST_DIVISOR * (complexity / ACTIVATION_SCALE_DIVISOR));
    }

    public int getMaxComplexity() {
        return maxComplexity;
    }

    public static Component formatVis(float amount) {
        float rounded = Math.round(amount * 10.0F) / 10.0F;
        if (Mth.equal(rounded, (int) rounded)) {
            return Component.literal(String.valueOf((int) rounded));
        }
        return Component.literal(String.valueOf(rounded));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        addFocusInformation(stack, builder);
    }

    public void addFocusInformation(ItemStack focusStack, Consumer<Component> builder) {
        FocusPackage core = getPackage(focusStack);
        if (core == null) {
            return;
        }
        builder.accept(Component.translatable("tooltip.thaumaturge.focus.vis_cost", formatVis(getVisCost(focusStack))));
        buildInfo(builder, core, 0);
    }

    private void buildInfo(Consumer<Component> builder, FocusPackage pack, int depth) {
        for (FocusUnit unit : pack.units()) {
            if (unit.element().equals(FocusMediumRoot.KEY)) {
                continue;
            }
            FocusElement element = FocusEngine.element(unit.element());
            if (element == null) {
                continue;
            }
            MutableComponent line = Component.literal(INDENT.repeat(depth));
            line.append(Component.translatable(element.nameKey()).withStyle(ChatFormatting.DARK_PURPLE));
            List<SettingDefinition> definitions = element.settings();
            if (!definitions.isEmpty()) {
                FocusSettings settings = FocusSettings.of(element, unit.settings());
                MutableComponent values = Component.literal(" [");
                boolean following = false;
                for (SettingDefinition definition : definitions) {
                    if (following) {
                        values.append(", ");
                    }
                    int value = settings.value(definition.key());
                    values.append(Component.translatable(definition.nameKey())).append(" ").append(definition.values().labelAt(definition.values().indexOf(value)));
                    following = true;
                }
                values.append("]");
                line.append(values.withStyle(ChatFormatting.DARK_AQUA));
            }
            builder.accept(line);
            for (FocusPackage branch : unit.branches()) {
                buildInfo(builder, branch, depth + 1);
            }
        }
    }
}
