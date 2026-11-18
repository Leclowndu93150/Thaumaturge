package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.SpellRegistries;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFx;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpellFxPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundWandSpellFxPayload;
import com.leclowndu93150.thaumaturge.registry.TTSpellFx;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jspecify.annotations.Nullable;

final class SpellFxBatch implements SpellFx {
    private static final int MAX_EVENTS = 24;
    private static final int MAX_ARCS = 8;
    private static final double BROADCAST_RADIUS = 64.0;

    private final ServerLevel level;
    private final List<ClientboundSpellFxPayload.Event> events = new ArrayList<>();
    private Vec3 anchor;
    private @Nullable WandOrigin wandOrigin;
    private int wandEvents;
    private int arcs;

    SpellFxBatch(ServerLevel level) {
        this.level = level;
    }

    void setWandOrigin(int casterId, InteractionHand hand, Vec3 position) {
        this.wandOrigin = new WandOrigin(casterId, hand, position);
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
            Vec3 source = LevelSpaces.toWorld(level, from);
            Vec3 target = LevelSpaces.toWorld(level, to);
            sendStream(
                    source,
                    ClientboundStreamEffectPayload.bolt(
                            source.x, source.y, source.z, target.x, target.y, target.z, color, width));
        }
    }

    @Override
    public void beam(Vec3 from, Vec3 to, int color, int ticks) {
        beam(from, to, color, ticks, false);
    }

    @Override
    public void beam(Vec3 from, Vec3 to, int color, int ticks, boolean hit) {
        if (arcs++ < MAX_ARCS) {
            Vec3 source = LevelSpaces.toWorld(level, from);
            Vec3 target = LevelSpaces.toWorld(level, to);
            sendStream(
                    source,
                    ClientboundStreamEffectPayload.beam(
                            source.x, source.y, source.z, target.x, target.y, target.z, color, ticks, 0, 1.0F, false,
                            -1, true, hit));
        }
    }

    private void sendStream(Vec3 source, ClientboundStreamEffectPayload payload) {
        if (wandOrigin != null && wandOrigin.position().equals(source)) {
            payload = payload.withWandSource(wandOrigin.casterId(), wandOrigin.hand());
        }
        PacketDistributor.sendToPlayersNear(level, null, source.x, source.y, source.z, BROADCAST_RADIUS, payload);
    }

    void flush() {
        if (events.isEmpty()) {
            return;
        }
        ClientboundSpellFxPayload payload = new ClientboundSpellFxPayload(anchor, List.copyOf(events));
        ServerPlayer excluded = null;
        if (wandEvents != 0
                && wandOrigin != null
                && level.getEntity(wandOrigin.casterId()) instanceof ServerPlayer caster
                && NetworkRegistry.hasChannel(caster.connection, ClientboundWandSpellFxPayload.TYPE.id())) {
            PacketDistributor.sendToPlayer(
                    caster,
                    new ClientboundWandSpellFxPayload(
                            payload, wandOrigin.hand() == InteractionHand.OFF_HAND, wandEvents));
            excluded = caster;
        }
        PacketDistributor.sendToPlayersNear(level, excluded, anchor.x, anchor.y, anchor.z, BROADCAST_RADIUS, payload);
        events.clear();
        anchor = null;
        wandEvents = 0;
    }

    private void add(ResourceLocation style, Vec3 at, Vec3 motion, int color, boolean directed) {
        if (events.size() >= MAX_EVENTS) {
            return;
        }
        at = LevelSpaces.toWorld(level, at);
        if (directed && wandOrigin != null && wandOrigin.position().equals(at)) {
            wandEvents |= 1 << events.size();
        }
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

    private record WandOrigin(int casterId, InteractionHand hand, Vec3 position) {}
}
