package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class JarNodeItem extends BlockItem {
    public JarNodeItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (stack.get(TCDataComponents.NODE_DATA.get()) == null) {
            NodeData data = NodeGenerator.rollRandomNodeData(level, entity.blockPosition(), level.getRandom(), false, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY,
                    NodeGenerator.DEFAULT_BASE_AURA);
            if (data != null) {
                stack.set(TCDataComponents.NODE_DATA.get(), data);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        NodeData data = stack.get(TCDataComponents.NODE_DATA.get());
        if (data == null) {
            return;
        }
        Component type = Component.translatable("nodetype.thaumaturge." + data.type().getSerializedName());
        Component line = data.modifier().<Component>map(modifier -> Component.translatable("tc.node.typemod", type, Component.translatable("nodemod.thaumaturge." + modifier.getSerializedName())))
                .orElse(type);
        builder.accept(line.copy().withStyle(ChatFormatting.DARK_PURPLE));
        MutableComponent aspects = null;
        for (AspectInstance entry : data.aspects().entries()) {
            TextColor color = TextColor.fromRgb(entry.aspect().value().color());
            MutableComponent chunk = Component.literal(String.valueOf(entry.amount())).withStyle(style -> style.withColor(color));
            if (aspects == null) {
                aspects = Component.empty().append(chunk);
            } else {
                aspects.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY)).append(chunk);
            }
        }
        if (aspects != null) {
            builder.accept(aspects);
        }
    }
}
