package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum GolemComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    private static final Identifier UID = TCIds.rl("golem");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!JadeConfig.shouldShow(config, JadeConfig.GOLEMS, accessor))
            return;
        if (!(accessor.getEntity() instanceof EntityThaumaturgeGolem golem)) {
            return;
        }
        int rank = golem.properties().rank();
        tooltip.add(Component.translatable("jade.thaumaturge.golem.rank", rank));
        if (accessor.showDetails() && rank < EntityThaumaturgeGolem.MAX_RANK) {
            tooltip.add(Component.translatable("jade.thaumaturge.golem.xp", accessor.getServerData().getIntOr("RankXp", 0), accessor.getServerData().getIntOr("RankXpRequired", 0)));
        }
    }
}
