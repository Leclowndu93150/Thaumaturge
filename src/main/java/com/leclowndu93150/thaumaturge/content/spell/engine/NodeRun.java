package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.cast.TouchBudget;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFx;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

final class NodeRun implements CastContext {
    private final CastRun run;
    private final SpellNode node;
    private final ResourceKey<SpellPart> key;
    private final SpellPart part;
    private final SpellState state;
    private final Optional<Holder<IAspect>> aspect;
    private final int color;

    NodeRun(CastRun run, SpellNode node, SpellPart part, SpellState state, Optional<Holder<IAspect>> aspect, int color) {
        this.run = run;
        this.node = node;
        this.key = node.part();
        this.part = part;
        this.state = state;
        this.aspect = aspect;
        this.color = color;
    }

    @Override
    public ServerLevel level() {
        return run.level();
    }

    @Override
    public @Nullable LivingEntity caster() {
        return run.caster();
    }

    @Override
    public Optional<UUID> casterId() {
        return run.casterId();
    }

    @Override
    public UUID castId() {
        return run.castId();
    }

    @Override
    public SpellNode node() {
        return node;
    }

    @Override
    public ResourceKey<SpellPart> partKey() {
        return key;
    }

    @Override
    public SpellPart part() {
        return part;
    }

    @Override
    public int setting(String settingKey) {
        Optional<SettingSpec> spec = part.setting(settingKey);
        if (spec.isEmpty()) {
            return 0;
        }
        Integer stored = node.settings().get(settingKey);
        return spec.get().clamp(stored != null ? stored : spec.get().defaultValue());
    }

    @Override
    public Optional<Holder<IAspect>> aspect() {
        return aspect;
    }

    @Override
    public int color() {
        return color;
    }

    @Override
    public SpellState state() {
        return state;
    }

    @Override
    public RandomSource random() {
        return run.level().getRandom();
    }

    @Override
    public TouchBudget budget() {
        return run.budget();
    }

    @Override
    public SpellFx fx() {
        return run.fx();
    }

    @Override
    public boolean isAlly(Entity entity) {
        return SpellTargeting.isAlly(run.caster(), entity);
    }

    @Override
    public RegistryAccess registries() {
        return run.level().registryAccess();
    }

    @Override
    public boolean hasNext() {
        return !node.children().isEmpty();
    }

    @Override
    public void proceed(List<SpellTarget> targets) {
        proceed(targets, state);
    }

    @Override
    public void proceed(List<SpellTarget> targets, SpellState next) {
        if (targets.isEmpty()) {
            return;
        }
        for (SpellNode child : node.children()) {
            SpellEngine.runNode(run, child, targets, next);
        }
    }

    @Override
    public void proceedLater(int ticks, List<SpellTarget> targets, SpellState next) {
        if (!targets.isEmpty() && hasNext()) {
            DelayedSpells.schedule(run.level(), ticks, continuation(next), targets);
        }
    }

    @Override
    public SpellContinuation continuation() {
        return continuation(state);
    }

    @Override
    public SpellContinuation continuation(SpellState next) {
        return new SpellContinuation(node.children(), next, run.casterId(), run.castId());
    }
}
