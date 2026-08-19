package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ScanBindings implements ScanningManager.Bindings {
    @Override
    public boolean progressResearch(Player player, Identifier research) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        boolean hasEntry = serverPlayer.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)).isPresent();
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(serverPlayer);
        if (hasEntry) {
            if (!knowledge.isResearchKnown(research)) {
                if (!ResearchManager.unlock(serverPlayer, research)) {
                    return false;
                }
                ResearchManager.advanceStage(serverPlayer, research);
                return true;
            }
            return ResearchManager.advanceStage(serverPlayer, research);
        }
        if (knowledge.isResearchKnown(research)) {
            return false;
        }
        knowledge.addResearch(research);
        knowledge.markComplete(research);
        knowledge.sync(serverPlayer);
        return true;
    }

    @Override
    public boolean addKnowledge(Player player, KnowledgeType type, Identifier category, int amount) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        ResearchGrants.grantConvertedKnowledge(serverPlayer, type, amount);
        return true;
    }

    @Override
    public AspectList itemAspects(ItemStack stack) {
        return AspectIndexAccess.index().of(stack);
    }

    @Override
    public AspectList entityAspects(Entity entity) {
        return EntityAspects.of(entity);
    }
}
