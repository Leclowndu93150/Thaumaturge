package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.network.ClientboundKnowledgeGainPayload;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ResearchGrants {

    private ResearchGrants() {}

    public static void grantConvertedKnowledge(ServerPlayer player, KnowledgeType type, int amount) {
        for (int i = 0; i < amount; i++) {
            Holder<IAspect> target = type == KnowledgeType.THEORY ? randomDiscoveredCompound(player) : randomPrimal(player);
            AspectPools.grant(player, target, 1);
        }
    }

    private static Holder<IAspect> randomPrimal(ServerPlayer player) {
        List<Holder.Reference<IAspect>> primals = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(aspect -> aspect.value().isPrimal()).toList();
        return primals.get(player.getRandom().nextInt(primals.size()));
    }

    private static Holder<IAspect> randomDiscoveredCompound(ServerPlayer player) {
        List<Holder.Reference<IAspect>> compounds = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(aspect -> !aspect.value().isPrimal())
                .filter(aspect -> AspectPools.isDiscovered(player, aspect)).toList();
        if (compounds.isEmpty()) {
            return randomPrimal(player);
        }
        return compounds.get(player.getRandom().nextInt(compounds.size()));
    }

    public static int grantAll(ServerPlayer player) {
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        List<Holder.Reference<IResearchEntry>> entries = player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).listElements().toList();

        Set<Identifier> entryIds = new HashSet<>();
        for (Holder.Reference<IResearchEntry> entry : entries) {
            entryIds.add(entry.key().identifier());
        }
        for (Holder.Reference<IResearchEntry> entry : entries) {
            for (ResearchParent parent : entry.value().parents()) {
                if (!entryIds.contains(parent.id())) {
                    ResearchManager.complete(player, parent.id());
                }
            }
            for (IResearchStage stage : entry.value().stages()) {
                for (Identifier required : stage.requiredResearch()) {
                    if (!entryIds.contains(required)) {
                        ResearchManager.complete(player, required);
                    }
                }
            }
        }

        int granted = 0;
        for (Holder.Reference<IResearchEntry> entry : entries) {
            Identifier id = entry.key().identifier();
            if (knowledge.isResearchComplete(id)) {
                continue;
            }
            if (ResearchManager.complete(player, id)) {
                ResearchManager.setStage(player, id, entry.value().stages().size());
                granted++;
                Optional<ResourceKey<IResearchCategory>> category = entry.value().category().unwrapKey();
                PacketDistributor.sendToPlayer(player, new ClientboundKnowledgeGainPayload(KnowledgeType.OBSERVATION, category, 1));
            }
        }
        for (Holder.Reference<IAspect> aspect : player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().toList()) {
            ResearchManager.unlock(player, ScanKeys.aspect(aspect.key()));
        }
        AspectPools.grantAllForCommand(player, AspectPools.SOFT_CAP);
        return granted;
    }
}
