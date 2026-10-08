package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.SpellRegistries;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFx;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import com.leclowndu93150.thaumaturge.compat.sable.SableCompat;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpellFxPayload;
import com.leclowndu93150.thaumaturge.registry.TTSpellFx;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

final class SpellFxBatch implements SpellFx {
    private static final int MAX_EVENTS = 24;
    private static final int MAX_ARCS = 8;
    private static final double BROADCAST_RADIUS = 64.0;

    private final ServerLevel level;
    private final List<ClientboundSpellFxPayload.Event> events = new ArrayList<>();
    private Vec3 anchor;
    private int arcs;

    SpellFxBatch(ServerLevel level) {
        this.level = level;
    }

    @Override
    public void impact(ResourceLocation style, Vec3 at, int color) {
        add(style, at, Vec3.ZERO, color, false);
    }

    @Override
    public void burst(ResourceLocation style, Vec3 at, Vec3 direction, int color) {
        add(style, at, direction, color, true);
    }

    @Override
    public void arc(Vec3 from, Vec3 to, int color, float width) {
        if (arcs++ < MAX_ARCS) {
            Effects.arcBolt(level, SableCompat.worldPosition(level, from))
                    .to(SableCompat.worldPosition(level, to))
                    .color(color)
                    .width(width)
                    .send();
        }
    }

    @Override
    public void beam(Vec3 from, Vec3 to, int color, int ticks) {
        if (arcs++ < MAX_ARCS) {
            Effects.beamBore(level, SableCompat.worldPosition(level, from))
                    .to(SableCompat.worldPosition(level, to))
                    .color(color)
                    .age(ticks)
                    .send();
        }
    }

    void flush() {
        if (events.isEmpty()) {
            return;
        }
        ClientboundSpellFxPayload payload = new ClientboundSpellFxPayload(anchor, List.copyOf(events));
        PacketDistributor.sendToPlayersNear(level, null, anchor.x, anchor.y, anchor.z, BROADCAST_RADIUS, payload);
        events.clear();
        anchor = null;
    }

    private void add(ResourceLocation style, Vec3 at, Vec3 motion, int color, boolean directed) {
        if (events.size() >= MAX_EVENTS) {
            return;
        }
        at = SableCompat.worldPosition(level, at);
        if (anchor == null) {
            anchor = at;
        }
        Vec3 offset = at.subtract(anchor);
        events.add(new ClientboundSpellFxPayload.Event(
                resolve(style),
                (float) offset.x,
                (float) offset.y,
                (float) offset.z,
                (float) motion.x,
                (float) motion.y,
                (float) motion.z,
                color,
                directed));
    }

    private static Holder<SpellFxStyle> resolve(ResourceLocation style) {
        Optional<Holder.Reference<SpellFxStyle>> holder =
                SpellRegistries.fxStyles().getHolder(style);
        return holder.isPresent() ? holder.get() : TTSpellFx.SPARKLE;
    }
}
