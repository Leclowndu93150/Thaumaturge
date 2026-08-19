package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectPrimal implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("primal");

    private static final int BASE_COMPLEXITY = 20;
    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final int BASE_DAMAGE = 4;
    private static final float EXPLOSION_STRENGTH = 1.5F;
    private static final int CHAOS_CHANCE = 100;
    private static final float CHAOS_FLUX = 5.0F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_primal"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.PRAECANTATIO;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return BASE_COMPLEXITY + settings.value("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float damageForDisplay(FocusSettings settings, float power) {
        return (BASE_DAMAGE + settings.value("power")) * power;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(ctx.level() instanceof ServerLevel level)) {
            return false;
        }
        Vec3 origin = target.getLocation();
        Effects.bamf(level, origin).withSound().fancy().send();
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            struck.hurtServer(level, level.damageSources().indirectMagic(struck, ctx.caster()), damageForDisplay(settings, ctx.power()));
        }
        level.explode(ctx.caster(), origin.x, origin.y, origin.z, EXPLOSION_STRENGTH, Level.ExplosionInteraction.MOB);
        if (level.getRandom().nextInt(CHAOS_CHANCE) == 0) {
            BlockPos pos = BlockPos.containing(origin);
            if (level.getRandom().nextBoolean()) {
                AuraHelper.polluteAura(level, pos, CHAOS_FLUX, true);
            } else {
                NodeGenerator.createRandomNodeAt(level, pos.above(), level.getRandom(), false, false, true, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
            }
        }
        return true;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("power", "focus.common.power", new SettingDefinition.IntRange(1, 5)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        level.addParticle(TCParticles.PRIMAL_FLARE.get(), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }
}
