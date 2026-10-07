package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record SpellLook(Identifier fx, int color) {
    public static final Identifier DEFAULT_FX = Identifier.fromNamespaceAndPath("thaumaturge", "sparkle");
    public static final SpellLook DEFAULT = new SpellLook(DEFAULT_FX, 0xFFFFFF);

    public static final Codec<SpellLook> CODEC = RecordCodecBuilder.create(i -> i
            .group(Identifier.CODEC.optionalFieldOf("fx", DEFAULT_FX).forGetter(SpellLook::fx), Codec.INT.optionalFieldOf("color", 0xFFFFFF).forGetter(SpellLook::color)).apply(i, SpellLook::new));

    public static final StreamCodec<ByteBuf, SpellLook> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, SpellLook::fx, ByteBufCodecs.INT, SpellLook::color, SpellLook::new);

    public static SpellLook downstream(CastContext ctx) {
        return downstream(ctx.registries(), ctx.node().children(), new SpellLook(ctx.part().fx(), ctx.color()));
    }

    public static SpellLook downstream(HolderLookup.Provider registries, List<SpellNode> roots, SpellLook fallback) {
        Deque<SpellNode> open = new ArrayDeque<>(roots);
        Identifier fx = null;
        int r = 0;
        int g = 0;
        int b = 0;
        int count = 0;
        while (!open.isEmpty()) {
            SpellNode node = open.poll();
            open.addAll(node.children());
            Optional<SpellPart> part = Spells.part(registries, node.part());
            if (part.isEmpty() || part.get().kind() != SpellPartKind.EFFECT) {
                continue;
            }
            int color = Spells.color(registries, node);
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
            count++;
            if (fx == null) {
                fx = effectFx(registries, node, part.get());
            }
        }
        if (count == 0) {
            return fallback;
        }
        return new SpellLook(fx, (r / count) << 16 | (g / count) << 8 | b / count);
    }

    private static Identifier effectFx(HolderLookup.Provider registries, SpellNode node, SpellPart part) {
        Optional<ResourceKey<IAspect>> aspect = part.aspect().resolve(node.aspect(), registries);
        if (aspect.isPresent()) {
            Optional<AspectAffinity> affinity = Spells.affinity(registries, aspect.get());
            if (affinity.isPresent()) {
                return affinity.get().fx();
            }
        }
        return part.fx();
    }
}
