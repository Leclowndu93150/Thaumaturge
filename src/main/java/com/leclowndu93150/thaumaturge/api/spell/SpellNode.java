package com.leclowndu93150.thaumaturge.api.spell;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

/**
 * One placed part of a spell tree: which part, the aspect chosen for it, its setting values and
 * the nodes that run after it.
 *
 * <p>Nodes are immutable values. Every {@code with...} method returns a copy.
 *
 * @param part     the placed part
 * @param aspect   the chosen aspect, empty when the part takes none or uses its fixed aspect
 * @param settings setting values keyed by {@link SettingSpec#key()}; missing keys fall back to the spec default
 * @param children the nodes run after this one, in order
 * @since 1.0.0
 */
public record SpellNode(
        ResourceKey<SpellPart> part,
        Optional<ResourceKey<IAspect>> aspect,
        Map<String, Integer> settings,
        List<SpellNode> children) {
    /** Deepest nesting either codec accepts. Deeper trees fail to decode. */
    public static final int MAX_DEPTH = 32;

    /** Most children one node may carry in serialized form. */
    public static final int MAX_CHILDREN = 8;

    /** Most setting entries one node may carry in serialized form. */
    public static final int MAX_SETTINGS = 16;

    /** Longest setting key either codec accepts. */
    public static final int MAX_SETTING_KEY = 64;

    /** Recursive disk codec. Rejects trees deeper than {@link #MAX_DEPTH} or wider than {@link #MAX_CHILDREN}. */
    public static final Codec<SpellNode> CODEC = Codec.<SpellNode>recursive(
                    "SpellNode",
                    self -> RecordCodecBuilder.create(i -> i.group(
                                    ResourceKey.codec(SpellPart.REGISTRY_KEY)
                                            .fieldOf("part")
                                            .forGetter(SpellNode::part),
                                    ResourceKey.codec(IAspect.REGISTRY_KEY)
                                            .optionalFieldOf("aspect")
                                            .forGetter(SpellNode::aspect),
                                    Codec.unboundedMap(Codec.string(0, MAX_SETTING_KEY), Codec.INT)
                                            .optionalFieldOf("settings", Map.of())
                                            .forGetter(SpellNode::settings),
                                    self.sizeLimitedListOf(MAX_CHILDREN)
                                            .optionalFieldOf("children", List.of())
                                            .forGetter(SpellNode::children))
                            .apply(i, SpellNode::new)))
            .validate(node -> node.depth() <= MAX_DEPTH
                    ? DataResult.success(node)
                    : DataResult.error(() -> "Spell nested deeper than " + MAX_DEPTH));

    /**
     * Network codec mirroring {@link #CODEC}. Decoding counts depth as it goes and fails past
     * {@link #MAX_DEPTH}, {@link #MAX_CHILDREN} or {@link #MAX_SETTINGS} before allocating further.
     */
    public static final StreamCodec<ByteBuf, SpellNode> STREAM_CODEC = new BoundedStreamCodec();

    /**
     * Canonicalizes the components into immutable copies.
     */
    public SpellNode {
        settings = Map.copyOf(settings);
        children = List.copyOf(children);
    }

    /**
     * A node with no aspect, default settings and no children.
     *
     * @param part the part
     * @return the node
     */
    public static SpellNode of(ResourceKey<SpellPart> part) {
        return new SpellNode(part, Optional.empty(), Map.of(), List.of());
    }

    /**
     * A copy with other children.
     *
     * @param children the new children
     * @return the copy
     */
    public SpellNode withChildren(List<SpellNode> children) {
        return new SpellNode(part, aspect, settings, children);
    }

    /**
     * A copy with one more child appended.
     *
     * @param child the child to append
     * @return the copy
     */
    public SpellNode then(SpellNode child) {
        List<SpellNode> next = new ArrayList<>(children);
        next.add(child);
        return withChildren(next);
    }

    /**
     * A copy with another aspect.
     *
     * @param aspect the aspect, or empty
     * @return the copy
     */
    public SpellNode withAspect(Optional<ResourceKey<IAspect>> aspect) {
        return new SpellNode(part, aspect, settings, children);
    }

    /**
     * A copy with one setting changed.
     *
     * @param key   the setting key
     * @param value the new value
     * @return the copy
     */
    public SpellNode withSetting(String key, int value) {
        Map<String, Integer> next = new HashMap<>(settings);
        next.put(key, value);
        return new SpellNode(part, aspect, next, children);
    }

    /**
     * Visits this node and every descendant depth-first, parents before children.
     *
     * @param visitor the visitor
     */
    public void forEach(Consumer<SpellNode> visitor) {
        visitor.accept(this);
        for (SpellNode child : children) {
            child.forEach(visitor);
        }
    }

    /**
     * The number of nodes on the longest path from this node down, this node included.
     *
     * @return the depth, at least 1
     */
    public int depth() {
        int deepest = 0;
        for (SpellNode child : children) {
            deepest = Math.max(deepest, child.depth());
        }
        return deepest + 1;
    }

    private static final class BoundedStreamCodec implements StreamCodec<ByteBuf, SpellNode> {
        private static final StreamCodec<ByteBuf, ResourceKey<SpellPart>> PART =
                ResourceKey.streamCodec(SpellPart.REGISTRY_KEY);
        private static final StreamCodec<ByteBuf, Optional<ResourceKey<IAspect>>> ASPECT =
                ByteBufCodecs.optional(ResourceKey.streamCodec(IAspect.REGISTRY_KEY));
        private static final StreamCodec<ByteBuf, Map<String, Integer>> SETTINGS = ByteBufCodecs.map(
                HashMap::new, ByteBufCodecs.stringUtf8(MAX_SETTING_KEY), ByteBufCodecs.VAR_INT, MAX_SETTINGS);

        @Override
        public SpellNode decode(ByteBuf buffer) {
            return decode(buffer, 1);
        }

        @Override
        public void encode(ByteBuf buffer, SpellNode node) {
            PART.encode(buffer, node.part());
            ASPECT.encode(buffer, node.aspect());
            SETTINGS.encode(buffer, node.settings());
            ByteBufCodecs.writeCount(buffer, node.children().size(), MAX_CHILDREN);
            for (SpellNode child : node.children()) {
                encode(buffer, child);
            }
        }

        private SpellNode decode(ByteBuf buffer, int depth) {
            if (depth > MAX_DEPTH) {
                throw new DecoderException("Spell nested deeper than " + MAX_DEPTH);
            }
            ResourceKey<SpellPart> part = PART.decode(buffer);
            Optional<ResourceKey<IAspect>> aspect = ASPECT.decode(buffer);
            Map<String, Integer> settings = SETTINGS.decode(buffer);
            int count = ByteBufCodecs.readCount(buffer, MAX_CHILDREN);
            List<SpellNode> children = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                children.add(decode(buffer, depth + 1));
            }
            return new SpellNode(part, aspect, settings, children);
        }
    }
}
