package com.leclowndu93150.thaumaturge.content.research.link;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

public final class ResearchLinkData extends SavedData {

    public record Progress(int stage, boolean complete) {
        static final Codec<Progress> CODEC = RecordCodecBuilder.create(builder -> builder
                .group(Codec.intRange(0, Integer.MAX_VALUE).fieldOf("stage").forGetter(Progress::stage), Codec.BOOL.fieldOf("complete").forGetter(Progress::complete)).apply(builder, Progress::new));

        Progress merge(Progress other) {
            return new Progress(Math.max(stage, other.stage), complete || other.complete);
        }
    }

    public static final class Link {
        final UUID first;
        final UUID second;
        final Map<Identifier, Progress> progress;

        Link(UUID first, UUID second, Map<Identifier, Progress> progress) {
            this.first = first;
            this.second = second;
            this.progress = new LinkedHashMap<>(progress);
        }

        public UUID first() {
            return first;
        }

        public UUID second() {
            return second;
        }

        public Map<Identifier, Progress> progress() {
            return progress;
        }

        public boolean involves(UUID player) {
            return first.equals(player) || second.equals(player);
        }

        static final Codec<Link> CODEC = RecordCodecBuilder
                .create(builder -> builder.group(UUIDUtil.CODEC.fieldOf("first").forGetter(link -> link.first), UUIDUtil.CODEC.fieldOf("second").forGetter(link -> link.second),
                        Codec.unboundedMap(LegacyIds.IDENTIFIER_CODEC, Progress.CODEC).fieldOf("progress").forGetter(link -> link.progress)).apply(builder, Link::new));
    }

    public static final Codec<ResearchLinkData> CODEC = RecordCodecBuilder
            .create(builder -> builder.group(Link.CODEC.listOf().fieldOf("links").forGetter(data -> data.links)).apply(builder, ResearchLinkData::new));

    public static final SavedDataType<ResearchLinkData> TYPE = new SavedDataType<>(TCIds.rl("research_share"), ResearchLinkData::new, CODEC, DataFixTypes.LEVEL);

    private final List<Link> links;
    private final Set<UUID> pendingPlayers = new HashSet<>();

    Set<UUID> pendingPlayers() {
        return pendingPlayers;
    }

    public ResearchLinkData() {
        this(List.of());
    }

    private ResearchLinkData(List<Link> links) {
        this.links = new ArrayList<>(links);
    }

    public static ResearchLinkData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
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
        Link link = new Link(a, b, Map.of());
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
