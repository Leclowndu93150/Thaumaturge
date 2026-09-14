package com.leclowndu93150.thaumaturge.content.research.link;

import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

public final class ResearchLinkData extends SavedData {

    public record Progress(int stage, boolean complete) {
        static final Codec<Progress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.optionalFieldOf("stage", 0).forGetter(Progress::stage),
                        Codec.BOOL.optionalFieldOf("complete", false).forGetter(Progress::complete))
                .apply(instance, Progress::new));

        Progress merge(Progress other) {
            return new Progress(Math.max(stage, other.stage), complete || other.complete);
        }
    }

    public static final class Link {
        final UUID first;
        final UUID second;
        final Map<ResourceLocation, Progress> progress;

        Link(UUID first, UUID second, Set<ResourceLocation> legacyUnion, Map<ResourceLocation, Progress> progress) {
            this.first = first;
            this.second = second;
            this.progress = new LinkedHashMap<>(progress);
            legacyUnion.forEach(id -> this.progress.putIfAbsent(id, new Progress(0, true)));
        }

        public UUID first() {
            return first;
        }

        public UUID second() {
            return second;
        }

        public Map<ResourceLocation, Progress> progress() {
            return progress;
        }

        public boolean involves(UUID player) {
            return first.equals(player) || second.equals(player);
        }

        static final Codec<Link> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                        UUIDUtil.CODEC.fieldOf("first").forGetter(link -> link.first),
                        UUIDUtil.CODEC.fieldOf("second").forGetter(link -> link.second),
                        LegacyIds.IDENTIFIER_CODEC
                                .listOf()
                                .optionalFieldOf("union", List.of())
                                .xmap(Set::copyOf, List::copyOf)
                                .forGetter(link -> Set.of()),
                        Codec.unboundedMap(LegacyIds.IDENTIFIER_CODEC, Progress.CODEC)
                                .optionalFieldOf("progress", Map.of())
                                .forGetter(link -> link.progress))
                .apply(builder, Link::new));
    }

    public static final Codec<ResearchLinkData> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(Link.CODEC.listOf().fieldOf("links").forGetter(data -> data.links))
                    .apply(builder, ResearchLinkData::new));

    public static final SavedData.Factory<ResearchLinkData> FACTORY =
            new SavedData.Factory<>(ResearchLinkData::new, ResearchLinkData::load, DataFixTypes.LEVEL);

    private final List<Link> links;

    public ResearchLinkData() {
        this(List.of());
    }

    private ResearchLinkData(List<Link> links) {
        this.links = new ArrayList<>(links);
    }

    public static ResearchLinkData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, "research_share");
    }

    private static ResearchLinkData load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get("data")).result().orElseGet(ResearchLinkData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        DataResult<Tag> encoded = CODEC.encodeStart(NbtOps.INSTANCE, this);
        tag.put("data", encoded.getOrThrow());
        return tag;
    }

    public List<Link> links() {
        return links;
    }

    public @Nullable Link linkBetween(UUID a, UUID b) {
        for (Link link : links) {
            if (link.involves(a) && link.involves(b)) {
                return link;
            }
        }
        return null;
    }

    public Link link(UUID a, UUID b) {
        Link existing = linkBetween(a, b);
        if (existing != null) {
            return existing;
        }
        Link link = new Link(a, b, Set.of(), Map.of());
        links.add(link);
        setDirty();
        return link;
    }

    public int unlinkAll(UUID player) {
        int removed = 0;
        for (int i = links.size() - 1; i >= 0; i--) {
            if (links.get(i).involves(player)) {
                links.remove(i);
                removed++;
            }
        }
        if (removed > 0) {
            setDirty();
        }
        return removed;
    }
}
