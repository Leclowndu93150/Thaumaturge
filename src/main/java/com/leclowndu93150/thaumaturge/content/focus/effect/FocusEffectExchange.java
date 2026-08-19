package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectExchange implements FocusEffect, IFocusBlockPicker {
    private static final Identifier KEY = TCIds.rl("exchange");

    private static final int BASE_COMPLEXITY = 5;
    private static final int SILK_COMPLEXITY_FACTOR = 4;
    private static final int FORTUNE_COMPLEXITY_FACTOR = 3;
    private static final int SWAP_FX_COLOR = 8038177;
    private static final float BASE_VIS_COST = 0.25F;
    private static final float SILK_VIS_COST = 0.25F;
    private static final float FORTUNE_VIS_COST = 0.1F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_exchange"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.PERMUTATIO;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return BASE_COMPLEXITY + settings.value("silk") * SILK_COMPLEXITY_FACTOR + settings.value("fortune") == 0 ? 0 : (settings.value("fortune") + 1) * FORTUNE_COMPLEXITY_FACTOR;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(target instanceof BlockHitResult blockHit)) {
            return false;
        }
        if (!(ctx.level() instanceof ServerLevel level)) {
            return false;
        }
        LivingEntity caster = ctx.caster();
        if (caster == null) {
            return false;
        }
        ItemStack casterStack = ItemStack.EMPTY;
        if (caster.getMainHandItem().getItem() instanceof ICaster) {
            casterStack = caster.getMainHandItem();
        } else if (caster.getOffhandItem().getItem() instanceof ICaster) {
            casterStack = caster.getOffhandItem();
        }
        if (casterStack.isEmpty()) {
            return false;
        }
        boolean silk = settings.value("silk") > 0;
        int fortune = settings.value("fortune");
        BlockState picked = ((ICaster) casterStack.getItem()).getPickedBlock(casterStack);
        if (caster instanceof Player player && picked != null && !picked.isAir()) {
            BlockBreakerEngine.swapper(blockHit.getBlockPos(), level.getBlockState(blockHit.getBlockPos()), picked, player).consumeTarget().showFx(SWAP_FX_COLOR, false).pickupDrops().silkTouch(silk)
                    .fortune(fortune).visCost(BASE_VIS_COST + (silk ? SILK_VIS_COST : 0.0F) + fortune * FORTUNE_VIS_COST).queue(level);
        }
        return true;
    }

    @Override
    public List<SettingDefinition> settings() {
        int[] silk = new int[]{0, 1};
        String[] silkDesc = new String[]{"focus.common.no", "focus.common.yes"};
        int[] fortune = new int[]{0, 1, 2, 3, 4};
        String[] fortuneDesc = new String[]{"focus.common.no", "I", "II", "III", "IV"};
        return List.of(new SettingDefinition("fortune", "focus.common.fortune", new SettingDefinition.IntList(fortune, fortuneDesc)),
                new SettingDefinition("silk", "focus.common.silk", new SettingDefinition.IntList(silk, silkDesc)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        float shade = 0.25F + level.getRandom().nextFloat() * 0.25F;
        ShieldSparkParticleOptions data = new ShieldSparkParticleOptions(ARGB.colorFromFloat(1.0F, shade, shade, shade), 0.6F, 0.5F, 9, 0, true);
        level.addParticle(data, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.2F,
                2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}
