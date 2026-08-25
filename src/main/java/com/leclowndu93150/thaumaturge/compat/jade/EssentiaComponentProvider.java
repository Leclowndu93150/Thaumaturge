package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EssentiaComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = TCIds.rl("essentia");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof BlockEntityJar jar) {
            AspectList contents = jar.getContents(accessor.getLevel().registryAccess());
            if (contents.isEmpty()) {
                tooltip.add(Component.translatable("jade.thaumaturge.essentia.empty"));
                return;
            }
            for (AspectInstance entry : contents.entries()) {
                tooltip.add(Component.translatable(
                        "jade.thaumaturge.essentia.fill",
                        AspectComponents.name(entry.aspect()),
                        entry.amount(),
                        jar.capacity()));
            }
            return;
        }
        if (accessor.getBlockEntity() instanceof BlockEntityNode) {
            return;
        }
        if (accessor.getBlockEntity() instanceof IAspectContainer container) {
            AspectList aspects = container.getAspects();
            if (!aspects.isEmpty()) {
                tooltip.add(JadeComponents.aspectLine("jade.thaumaturge.essentia.contents", aspects));
            }
            return;
        }
        if (accessor.getBlockEntity() instanceof IAspectQuery query) {
            AspectList advertised = query.queryAspects();
            if (advertised.isEmpty()) {
                tooltip.add(Component.translatable("jade.thaumaturge.essentia.unfiltered"));
                return;
            }
            tooltip.add(Component.translatable(
                    "jade.thaumaturge.essentia.filter",
                    AspectComponents.name(advertised.entries().getFirst().aspect())));
        }
    }
}
