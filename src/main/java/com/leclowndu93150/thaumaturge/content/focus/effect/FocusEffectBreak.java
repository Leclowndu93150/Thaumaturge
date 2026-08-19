package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.focus.FocusFX;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectBreak implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("break");

    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final int SILK_COMPLEXITY_FACTOR = 4;
    private static final int FORTUNE_COMPLEXITY_FACTOR = 3;
    private static final float HARDNESS_TO_DURABILITY = 100.0F;
    private static final float DELAY_DIVISOR = 3.0F;
    private static final float BASE_VIS_COST = 0.25F;
    private static final float SILK_VIS_COST = 0.25F;
    private static final float FORTUNE_VIS_COST = 0.1F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_break"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.PERDITIO;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return settings.value("power") * POWER_COMPLEXITY_FACTOR + settings.value("silk") * SILK_COMPLEXITY_FACTOR
                + (settings.value("fortune") == 0 ? 0 : (settings.value("fortune") + 1) * FORTUNE_COMPLEXITY_FACTOR);
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(ctx.level() instanceof ServerLevel level)) {
            return true;
        }
        if (target instanceof BlockHitResult blockHit) {
            FocusFX.impact(level, Vec3.atCenterOf(blockHit.getBlockPos()), id());
            boolean silk = settings.value("silk") > 0;
            int fortune = settings.value("fortune");
            float strength = settings.value("power") * ctx.power();
            float dur = level.getBlockState(blockHit.getBlockPos()).getDestroySpeed(level, blockHit.getBlockPos()) * HARDNESS_TO_DURABILITY;
            dur = (float) Math.sqrt(dur);
            if (ctx.caster() instanceof Player player) {
                BlockBreakerEngine.breaker(blockHit.getBlockPos(), level.getBlockState(blockHit.getBlockPos()), player).showFx().silkTouch(silk).fortune(fortune).strength(strength).durability(dur)
                        .delay((int) (dur / strength / DELAY_DIVISOR * index)).visCost(BASE_VIS_COST + (silk ? SILK_VIS_COST : 0.0F) + fortune * FORTUNE_VIS_COST).queue(level);
            }
        }
        return true;
    }

    @Override
    public List<SettingDefinition> settings() {
        int[] silk = new int[]{0, 1};
        String[] silkDesc = new String[]{"focus.common.no", "focus.common.yes"};
        int[] fortune = new int[]{0, 1, 2, 3, 4};
        String[] fortuneDesc = new String[]{"focus.common.no", "I", "II", "III", "IV"};
        return List.of(new SettingDefinition("power", "focus.break.power", new SettingDefinition.IntRange(1, 5)),
                new SettingDefinition("fortune", "focus.common.fortune", new SettingDefinition.IntList(fortune, fortuneDesc)),
                new SettingDefinition("silk", "focus.common.silk", new SettingDefinition.IntList(silk, silkDesc)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        int q = level.getRandom().nextInt(4);
        CrackShardParticleOptions data = new CrackShardParticleOptions(0xFFFFFF, q, (float) (1.7F + level.getRandom().nextGaussian() * 0.3F), 6 + level.getRandom().nextInt(6));
        level.addParticle(data, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.END_GATEWAY_SPAWN, SoundSource.PLAYERS, 0.1F, 2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}
