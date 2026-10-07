package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class WardEffect extends AbstractEffectBehavior {
    public static final MapCodec<WardEffect> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("vis_per_block", 0.5F).forGetter(WardEffect::visPerBlock)).apply(i, WardEffect::new));

    private static final float ZAP_VOLUME = 0.25F;

    private final float visPerBlock;

    public WardEffect(float visPerBlock) {
        this.visPerBlock = visPerBlock;
    }

    public float visPerBlock() {
        return visPerBlock;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.WARD.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        if (target.block().isEmpty() || !(ctx.caster() instanceof Player player)) {
            return;
        }
        if (toggle(ctx.level(), player, target.block().get().getBlockPos())) {
            ctx.fx().impact(ctx.part().fx(), Vec3.atCenterOf(target.block().get().getBlockPos()), ctx.color());
        }
    }

    private boolean toggle(ServerLevel level, Player player, BlockPos pos) {
        UUID owner = player.getUUID();
        BlockPos partner = WardHandler.partner(level.getBlockState(pos), pos);
        if (WardHandler.isWarded(level, pos)) {
            if (!WardHandler.unward(level, pos, owner)) {
                return false;
            }
            if (partner != null) {
                WardHandler.unward(level, partner, owner);
            }
            zap(level, pos);
            return true;
        }
        if (!WardHandler.canWard(level, pos) || partner != null && !WardHandler.canWard(level, partner)) {
            return false;
        }
        float cost = partner == null ? visPerBlock : visPerBlock * 2.0F;
        if (!WandVisHelper.consumeVisFromHotbar(player, cost, TTAspects.ORDO, false) || !WardHandler.ward(level, pos, owner)) {
            return false;
        }
        if (partner != null && !WardHandler.ward(level, partner, owner)) {
            WardHandler.unward(level, pos, owner);
            return false;
        }
        WandVisHelper.consumeVisFromHotbar(player, cost, TTAspects.ORDO, true);
        zap(level, pos);
        return true;
    }

    private static void zap(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, 1.0F);
    }
}
