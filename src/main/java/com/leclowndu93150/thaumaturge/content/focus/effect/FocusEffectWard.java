package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.focus.FocusFX;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumRoot;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
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
    public static final Identifier ID = TTIds.rl("ward");

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
    private static final ResourceKey<IAspect> ASPECT = TTAspects.ORDO;

    @Override
    public Identifier id() {
        return ID;
    }

    public static boolean removesOwnedWard(Player player, FocusPackage focus) {
        if (!FocusEngine.effectIds(focus).equals(List.of(ID))) {
            return false;
        }
        if (!(player.pick(player.blockInteractionRange(), 0.0F, false) instanceof BlockHitResult blockHit)) {
            return false;
        }
        BlockPos pos = blockHit.getBlockPos();
        if (player.level() instanceof ServerLevel level) {
            return player.getUUID().equals(WardHandler.owner(level, pos));
        }
        return WardHandler.isWarded(player.level(), pos) && ClientWardHolder.isOwned(pos);
    }

    public static boolean castStandalone(Player player, FocusPackage focus) {
        if (!isStandaloneWardFocus(focus) || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        return player.pick(player.blockInteractionRange(), 0.0F, false) instanceof BlockHitResult blockHit && applyAt(level, player, blockHit.getBlockPos());
    }

    private static boolean isStandaloneWardFocus(FocusPackage focus) {
        List<FocusUnit> units = focus.units();
        int first = !units.isEmpty() && units.getFirst().element().equals(FocusMediumRoot.KEY) ? 1 : 0;
        return units.size() - first == 1 && units.get(first).element().equals(ID) && units.get(first).branches().isEmpty();
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TTIds.rl("focus_ward"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return ASPECT;
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
        return applyAt(level, player, blockHit.getBlockPos());
    }

    private static boolean applyAt(ServerLevel level, Player player, BlockPos pos) {
        UUID owner = player.getUUID();
        BlockPos partner = WardHandler.partner(level.getBlockState(pos), pos);
        if (WardHandler.isWarded(level, pos)) {
            if (!WardHandler.unward(level, pos, owner)) {
                return false;
            }
            if (partner != null) {
                WardHandler.unward(level, partner, owner);
            }
            FocusFX.impact(level, Vec3.atCenterOf(pos), ID);
            level.playSound(null, pos, TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, ZAP_PITCH);
            return true;
        }
        if (!WardHandler.canWard(level, pos) || (partner != null && !WardHandler.canWard(level, partner))) {
            return false;
        }
        float visCost = partner == null ? VIS_COST_PER_BLOCK : VIS_COST_PER_BLOCK * 2.0F;
        if (!WandVisHelper.consumeVisFromHotbar(player, visCost, ASPECT, false)) {
            return false;
        }
        if (!WardHandler.ward(level, pos, owner)) {
            return false;
        }
        if (partner != null && !WardHandler.ward(level, partner, owner)) {
            WardHandler.unward(level, pos, owner);
            return false;
        }
        WandVisHelper.consumeVisFromHotbar(player, visCost, ASPECT, true);
        FocusFX.impact(level, Vec3.atCenterOf(pos), ID);
        level.playSound(null, pos, TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, ZAP_PITCH);
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
