package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DelayedSpells {
    private final List<Pending> pending = new ArrayList<>();

    static boolean schedule(ServerLevel level, int ticks, SpellContinuation continuation, List<SpellTarget> targets) {
        DelayedSpells queue = level.getData(TTAttachments.DELAYED_SPELLS);
        if (queue.pending.size() >= ThaumaturgeServerConfig.SPELL_MAX_DELAYED_PER_LEVEL.get()) {
            return false;
        }
        queue.pending.add(new Pending(level.getGameTime() + Math.max(1, ticks), continuation, List.copyOf(targets)));
        return true;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.hasData(TTAttachments.DELAYED_SPELLS)) {
            return;
        }
        DelayedSpells queue = level.getData(TTAttachments.DELAYED_SPELLS);
        if (queue.pending.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        List<Pending> due = new ArrayList<>();
        Iterator<Pending> iterator = queue.pending.iterator();
        while (iterator.hasNext()) {
            Pending entry = iterator.next();
            if (entry.due <= now) {
                due.add(entry);
                iterator.remove();
            }
        }
        for (Pending entry : due) {
            SpellEngine.resume(level, entry.continuation, live(entry.targets));
        }
    }

    private static List<SpellTarget> live(List<SpellTarget> targets) {
        List<SpellTarget> out = new ArrayList<>(targets.size());
        for (SpellTarget target : targets) {
            Entity entity = target.entity().orElse(null);
            if (entity == null || entity.isAlive()) {
                out.add(target);
            } else {
                out.add(new SpellTarget(
                        BlockHitResult.miss(target.position(), Direction.UP, target.blockPos()),
                        target.position(),
                        target.direction()));
            }
        }
        return out;
    }

    private record Pending(long due, SpellContinuation continuation, List<SpellTarget> targets) {}
}
