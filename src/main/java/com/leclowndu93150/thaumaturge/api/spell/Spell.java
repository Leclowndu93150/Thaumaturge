package com.leclowndu93150.thaumaturge.api.spell;

import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A complete spell as stored on a focus: the cast style and the node tree, rooted at the
 * {@link #ORIGIN} part.
 *
 * <p>Spells are plain data. Whether one is castable depends on the parts loaded from datapacks, the
 * focus it sits in and the caster's research; {@link Spells#validate} answers that.
 *
 * @param style the cast style
 * @param root  the origin node; everything else hangs below it
 * @since 1.0.0
 */
public record Spell(CastStyle style, SpellNode root) {
    /** The hidden flow part every spell tree starts from. */
    public static final ResourceKey<SpellPart> ORIGIN = ResourceKey.create(SpellPart.REGISTRY_KEY, Identifier.fromNamespaceAndPath("thaumaturge", "origin"));

    /** Disk codec. */
    public static final Codec<Spell> CODEC = RecordCodecBuilder
            .create(i -> i.group(CastStyle.CODEC.optionalFieldOf("style", CastStyle.INSTANT).forGetter(Spell::style), SpellNode.CODEC.fieldOf("root").forGetter(Spell::root)).apply(i, Spell::new));

    /** Network codec mirroring {@link #CODEC}. */
    public static final StreamCodec<ByteBuf, Spell> STREAM_CODEC = StreamCodec.composite(CastStyle.STREAM_CODEC, Spell::style, SpellNode.STREAM_CODEC, Spell::root, Spell::new);

    /**
     * A spell with only the origin node.
     *
     * @return the empty spell, instant style
     */
    public static Spell empty() {
        return new Spell(CastStyle.INSTANT, SpellNode.of(ORIGIN));
    }

    /**
     * A copy with another style.
     *
     * @param style the style
     * @return the copy
     */
    public Spell withStyle(CastStyle style) {
        return new Spell(style, root);
    }

    /**
     * A copy with another tree.
     *
     * @param root the new origin node
     * @return the copy
     */
    public Spell withRoot(SpellNode root) {
        return new Spell(style, root);
    }

    /**
     * Whether nothing hangs below the origin.
     *
     * @return true for an empty spell
     */
    public boolean isEmpty() {
        return root.children().isEmpty();
    }

    /**
     * Every node of the tree, origin first, depth-first.
     *
     * @return the nodes
     */
    public List<SpellNode> nodes() {
        List<SpellNode> out = new ArrayList<>();
        root.forEach(out::add);
        return out;
    }
}
