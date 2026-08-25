package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum NodeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = TCIds.rl("node");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!JadeConfig.shouldShow(config, JadeConfig.NODES, accessor)) return;
        if (!(accessor.getBlockEntity() instanceof BlockEntityNode node)) {
            return;
        }
        Player player = accessor.getPlayer();
        if (!GogglesAccess.revealsNodes(player)
                && !(player.getMainHandItem().getItem() instanceof ThaumometerItem)
                && !(player.getOffhandItem().getItem() instanceof ThaumometerItem)) {
            return;
        }
        if (node instanceof BlockEntityJarNode
                || node.getNodeType() != NodeType.NORMAL
                || node.getNodeModifier() != null) {
            MutableComponent type = Component.translatable(
                    "jade.thaumaturge.node.type." + node.getNodeType().getSerializedName());
            if (node.getNodeModifier() != null) {
                type = Component.translatable(
                        "jade.thaumaturge.node.modified",
                        Component.translatable("jade.thaumaturge.node.modifier."
                                + node.getNodeModifier().getSerializedName()),
                        type);
            }
            tooltip.add(type);
        }
        AspectList aspects = node.getAspects();
        if (!aspects.isEmpty()) {
            JadeComponents.addAspectLines(tooltip, "jade.thaumaturge.node.aspects", aspects);
        }
        if (node.isEnergized()) {
            tooltip.add(
                    Component.translatable("jade.thaumaturge.node.energized").withStyle(ChatFormatting.AQUA));
            if (!accessor.showDetails()) return;
            tooltip.add(Component.translatable(
                    node.getNodeType() == NodeType.TAINTED
                            ? "jade.thaumaturge.node.feeds_flux"
                            : "jade.thaumaturge.node.feeds_aura"));
            AspectList original = node.getAspectsBaseOriginal();
            if (original != null && !original.isEmpty()) {
                JadeComponents.addAspectLines(tooltip, "jade.thaumaturge.node.reverts_to", original);
            }
        }
    }
}
