package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.capability.ResearchStatus;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerKnowledge implements IPlayerKnowledge {
    public static final MapCodec<PlayerKnowledge> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(ResearchRecord.CODEC.listOf().optionalFieldOf("research", List.of()).forGetter(PlayerKnowledge::researchEntries),
                    KnowledgeRecord.CODEC.listOf().optionalFieldOf("knowledge", List.of()).forGetter(PlayerKnowledge::knowledgeEntries)).apply(instance, PlayerKnowledge::fromRecords));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledge> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final Set<Identifier> research = new HashSet<>();
    private final Set<Identifier> completed = new HashSet<>();
    private final Map<Identifier, Integer> stages = new HashMap<>();
    private final Map<Identifier, EnumSet<ResearchFlag>> flags = new HashMap<>();
    private final Map<KnowledgeKey, Integer> knowledge = new HashMap<>();

    public PlayerKnowledge() {}

    private static PlayerKnowledge fromRecords(List<ResearchRecord> researchRecords, List<KnowledgeRecord> knowledgeRecords) {
        PlayerKnowledge result = new PlayerKnowledge();
        for (ResearchRecord record : researchRecords) {
            result.research.add(record.key());
            if (record.complete()) {
                result.completed.add(record.key());
            }
            record.stage().ifPresent(stage -> result.stages.put(record.key(), stage));
            EnumSet<ResearchFlag> flagSet = EnumSet.noneOf(ResearchFlag.class);
            flagSet.addAll(record.flags());
            if (!flagSet.isEmpty()) {
                result.flags.put(record.key(), flagSet);
            }
        }
        for (KnowledgeRecord record : knowledgeRecords) {
            result.knowledge.put(new KnowledgeKey(record.type(), record.category().map(ResourceKey::identifier).orElse(null)), record.amount());
        }
        return result;
    }

    private List<ResearchRecord> researchEntries() {
        return research.stream().sorted().map(key -> new ResearchRecord(key, Optional.ofNullable(stages.get(key)), flagsAsList(key), completed.contains(key))).toList();
    }

    private List<ResearchFlag> flagsAsList(Identifier key) {
        EnumSet<ResearchFlag> set = flags.get(key);
        return set == null ? List.of() : List.copyOf(set);
    }

    private List<KnowledgeRecord> knowledgeEntries() {
        return knowledge.entrySet().stream().filter(e -> e.getValue() > 0)
                .map(e -> new KnowledgeRecord(e.getKey().type(), Optional.ofNullable(e.getKey().category()).map(loc -> ResourceKey.create(IResearchCategory.REGISTRY_KEY, loc)), e.getValue()))
                .sorted((a, b) -> {
                    int byType = a.type().compareTo(b.type());
                    if (byType != 0)
                        return byType;
                    String an = a.category().map(k -> k.identifier().toString()).orElse("");
                    String bn = b.category().map(k -> k.identifier().toString()).orElse("");
                    return an.compareTo(bn);
                }).toList();
    }

    @Override
    public void clear() {
        research.clear();
        completed.clear();
        stages.clear();
        flags.clear();
        knowledge.clear();
    }

    @Override
    public ResearchStatus researchStatus(Identifier res) {
        if (!isResearchKnown(res)) {
            return ResearchStatus.UNKNOWN;
        }
        return completed.contains(res) ? ResearchStatus.COMPLETE : ResearchStatus.IN_PROGRESS;
    }

    @Override
    public boolean isResearchComplete(Identifier res) {
        return res != null && completed.contains(res);
    }

    public boolean markComplete(Identifier res) {
        if (res == null || !research.contains(res))
            return false;
        return completed.add(res);
    }

    public boolean clearComplete(Identifier res) {
        if (res == null)
            return false;
        return completed.remove(res);
    }

    @Override
    public boolean isResearchKnown(Identifier res) {
        return res != null && research.contains(res);
    }

    @Override
    public boolean isResearchKnown(Identifier res, int minimumStage) {
        return isResearchKnown(res) && researchStage(res) >= minimumStage;
    }

    @Override
    public int researchStage(Identifier res) {
        if (res == null || !research.contains(res)) {
            return -1;
        }
        Integer stage = stages.get(res);
        return stage == null ? 0 : stage;
    }

    @Override
    public boolean setResearchStage(Identifier res, int stage) {
        if (res == null || !research.contains(res) || stage <= 0) {
            return false;
        }
        Integer prev = stages.put(res, stage);
        return prev == null || prev != stage;
    }

    @Override
    public boolean addResearch(Identifier res) {
        if (res == null)
            return false;
        return research.add(res);
    }

    @Override
    public boolean removeResearch(Identifier res) {
        if (res == null)
            return false;
        boolean removed = research.remove(res);
        if (removed) {
            completed.remove(res);
            stages.remove(res);
            flags.remove(res);
        }
        return removed;
    }

    @Override
    public Set<Identifier> researchList() {
        return Collections.unmodifiableSet(research);
    }

    @Override
    public boolean setResearchFlag(Identifier res, ResearchFlag flag) {
        if (res == null || flag == null)
            return false;
        return flags.computeIfAbsent(res, k -> EnumSet.noneOf(ResearchFlag.class)).add(flag);
    }

    @Override
    public boolean clearResearchFlag(Identifier res, ResearchFlag flag) {
        if (res == null || flag == null)
            return false;
        EnumSet<ResearchFlag> set = flags.get(res);
        if (set == null)
            return false;
        boolean removed = set.remove(flag);
        if (set.isEmpty()) {
            flags.remove(res);
        }
        return removed;
    }

    @Override
    public boolean hasResearchFlag(Identifier res, ResearchFlag flag) {
        if (res == null || flag == null)
            return false;
        EnumSet<ResearchFlag> set = flags.get(res);
        return set != null && set.contains(flag);
    }

    @Override
    public boolean addKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount) {
        if (type == null || amount == 0)
            return false;
        KnowledgeKey key = new KnowledgeKey(type, category == null ? null : category.identifier());
        int prev = knowledge.getOrDefault(key, 0);
        int next = Math.max(0, prev + amount);
        if (next == prev)
            return false;
        if (next == 0) {
            knowledge.remove(key);
        } else {
            knowledge.put(key, next);
        }
        return true;
    }

    @Override
    public int knowledge(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return rawKnowledge(type, category) / type.progression();
    }

    @Override
    public int rawKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        KnowledgeKey key = new KnowledgeKey(type, category == null ? null : category.identifier());
        return knowledge.getOrDefault(key, 0);
    }

    @Override
    public void sync(ServerPlayer player) {
        player.syncData(TCAttachments.KNOWLEDGE);
    }

    public void copyFrom(PlayerKnowledge other) {
        clear();
        research.addAll(other.research);
        completed.addAll(other.completed);
        stages.putAll(other.stages);
        for (Map.Entry<Identifier, EnumSet<ResearchFlag>> entry : other.flags.entrySet()) {
            flags.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
        }
        knowledge.putAll(other.knowledge);
    }

    public void applyAutoUnlock(HolderLookup.Provider registries) {
        registries.lookup(IResearchEntry.REGISTRY_KEY).ifPresent(lookup -> lookup.listElements().forEach(holder -> {
            if (holder.value().hasMeta(ResearchEntryMeta.AUTOUNLOCK)) {
                holder.unwrapKey().ifPresent(k -> addResearch(k.identifier()));
            }
        }));
    }

    private record KnowledgeKey(KnowledgeType type, Identifier category) {
    }

    private record ResearchRecord(Identifier key, Optional<Integer> stage, List<ResearchFlag> flags, boolean complete) {
        static final Codec<ResearchRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(LegacyIds.IDENTIFIER_CODEC.fieldOf("id").forGetter(ResearchRecord::key),
                Codec.INT.optionalFieldOf("stage").forGetter(ResearchRecord::stage), ResearchFlag.CODEC.listOf().optionalFieldOf("flags", List.of()).forGetter(ResearchRecord::flags),
                Codec.BOOL.optionalFieldOf("complete", false).forGetter(ResearchRecord::complete)).apply(instance, ResearchRecord::new));
    }

    private record KnowledgeRecord(KnowledgeType type, Optional<ResourceKey<IResearchCategory>> category, int amount) {
        static final Codec<KnowledgeRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(KnowledgeType.CODEC.fieldOf("type").forGetter(KnowledgeRecord::type),
                LegacyIds.resourceKeyCodec(IResearchCategory.REGISTRY_KEY).optionalFieldOf("category").forGetter(KnowledgeRecord::category),
                Codec.INT.fieldOf("amount").forGetter(KnowledgeRecord::amount)).apply(instance, KnowledgeRecord::new));
    }
}
