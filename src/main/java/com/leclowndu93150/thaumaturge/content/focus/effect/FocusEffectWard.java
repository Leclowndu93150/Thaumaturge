package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.focus.FocusFX;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectWard implements FocusEffect {
    public static final Identifier ID = TCIds.rl("ward");

    private static final int COMPLEXITY = 4;
    private static final float VIS_COST_PER_BLOCK = 0.5F;
    private static final int SPARKLE_COLOR = 0xFCA200;
    private static final int SPARKLE_COUNT = 7;
    private static final float SPARKLE_ALPHA = 0.9F;
    private static final float COLOR_JITTER = 0.2F;
    private static final float SPARKLE_SPREAD = 1.2F;
    private static final float SPARKLE_RISE = 0.02F;
    private static final int SPARKLE_BASE_AGE = 5;
    private static final int SPARKLE_AGE_JITTER = 8;
    private static final int SPARKLE_DELAY_JITTER = 10;
    private static final float SPARKLE_BASE_SCALE = 0.7F;
    private static final float SPARKLE_SCALE_JITTER = 0.4F;
    private static final float ZAP_VOLUME = 0.25F;
    private static final float ZAP_PITCH = 1.0F;

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_ward"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.ORDO;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return COMPLEXITY;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(target instanceof BlockHitResult blockHit) || !(ctx.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!(ctx.caster() instanceof Player player)) {
            return false;
        }
        BlockPos pos = blockHit.getBlockPos();
        UUID owner = player.getUUID();
        if (!WardHandler.isWarded(level, pos) && !WardHandler.canWard(level, pos)) {
            return false;
        }
        if (!WandVisHelper.consumeVisFromHotbar(player, VIS_COST_PER_BLOCK, false)) {
            return false;
        }
        boolean changed = WardHandler.isWarded(level, pos) ? WardHandler.unward(level, pos, owner) : WardHandler.ward(level, pos, owner);
        if (!changed) {
            return false;
        }
        WandVisHelper.consumeVisFromHotbar(player, VIS_COST_PER_BLOCK, true);
        FocusFX.impact(level, Vec3.atCenterOf(pos), id());
        level.playSound(null, pos, TCSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, ZAP_PITCH);
        return true;
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        RandomSource random = level.getRandom();
        float baseRed = ARGB.red(SPARKLE_COLOR) / 255.0F;
        float baseGreen = ARGB.green(SPARKLE_COLOR) / 255.0F;
        float baseBlue = ARGB.blue(SPARKLE_COLOR) / 255.0F;
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            float red = Mth.clamp(baseRed - COLOR_JITTER + random.nextFloat() * COLOR_JITTER * 2.0F, 0.0F, 1.0F);
            float green = Mth.clamp(baseGreen - COLOR_JITTER + random.nextFloat() * COLOR_JITTER * 2.0F, 0.0F, 1.0F);
            float blue = Mth.clamp(baseBlue - COLOR_JITTER + random.nextFloat() * COLOR_JITTER * 2.0F, 0.0F, 1.0F);
            ShieldSparkParticleOptions data = new ShieldSparkParticleOptions(ARGB.colorFromFloat(1.0F, red, green, blue), SPARKLE_ALPHA, SPARKLE_BASE_SCALE + random.nextFloat() * SPARKLE_SCALE_JITTER,
                    SPARKLE_BASE_AGE + random.nextInt(SPARKLE_AGE_JITTER), random.nextInt(SPARKLE_DELAY_JITTER), true);
            level.addParticle(data, pos.x - SPARKLE_SPREAD / 2.0F + random.nextFloat() * SPARKLE_SPREAD, pos.y - SPARKLE_SPREAD / 2.0F + random.nextFloat() * SPARKLE_SPREAD,
                    pos.z - SPARKLE_SPREAD / 2.0F + random.nextFloat() * SPARKLE_SPREAD, 0.0, random.nextFloat() * SPARKLE_RISE, 0.0);
        }
    }
}
