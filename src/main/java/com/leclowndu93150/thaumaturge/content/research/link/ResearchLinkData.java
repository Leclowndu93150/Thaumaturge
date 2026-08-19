package com.leclowndu93150.thaumaturge.content.research.link;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    public static final class Link {
        final UUID first;
        final UUID second;
        final Set<Identifier> union;

        Link(UUID first, UUID second, Set<Identifier> union) {
            this.first = first;
            this.second = second;
            this.union = new LinkedHashSet<>(union);
        }

        public UUID first() {
            return first;
        }

        public UUID second() {
            return second;
        }

        public Set<Identifier> union() {
            return union;
        }

        public boolean involves(UUID player) {
            return first.equals(player) || second.equals(player);
        }

        static final Codec<Link> CODEC = RecordCodecBuilder.create(builder -> builder
                .group(UUIDUtil.CODEC.fieldOf("first").forGetter(link -> link.first), UUIDUtil.CODEC.fieldOf("second").forGetter(link -> link.second),
                        LegacyIds.IDENTIFIER_CODEC.listOf().fieldOf("union").xmap(list -> (Set<Identifier>) new LinkedHashSet<>(list), List::copyOf).forGetter(link -> link.union))
                .apply(builder, Link::new));
    }

    public static final Codec<ResearchLinkData> CODEC = RecordCodecBuilder
            .create(builder -> builder.group(Link.CODEC.listOf().fieldOf("links").forGetter(data -> data.links)).apply(builder, ResearchLinkData::new));

    public static final SavedDataType<ResearchLinkData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "research_share"), ResearchLinkData::new, CODEC, DataFixTypes.LEVEL);

    private final List<Link> links;

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
        Link link = new Link(a, b, Set.of());
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
