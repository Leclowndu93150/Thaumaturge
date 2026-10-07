package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellWall;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellRayTrace;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class WallDelivery extends AbstractCarrierDelivery {
    public static final MapCodec<WallDelivery> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.doubleRange(1.0, 32.0).optionalFieldOf("reach", 8.0).forGetter(WallDelivery::reach)).apply(i, WallDelivery::new));

    private static final String WIDTH = "width";
    private static final String DURATION = "duration";
    private static final double DROP = 4.0;
    private static final double STAND_OFF = 0.5;

    private final double reach;

    public WallDelivery(double reach) {
        this.reach = reach;
    }

    public double reach() {
        return reach;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.WALL.get();
    }

    @Override
    protected void launch(CastContext ctx, LivingEntity caster, CarrierPayload payload, SpellTarget origin) {
        Vec3 heading = new Vec3(origin.direction().x, 0.0, origin.direction().z);
        Vec3 facing = heading.lengthSqr() > 0.0 ? heading.normalize() : new Vec3(1.0, 0.0, 0.0);
        Vec3 anchor = origin.entity().filter(entity -> entity == caster).isPresent() ? reachPoint(ctx, caster, origin) : origin.position();
        BlockHitResult floor = SpellRayTrace.clipBlocks(ctx.level(), caster, anchor, anchor.subtract(0.0, DROP, 0.0));
        Vec3 base = floor.getType() == HitResult.Type.BLOCK ? floor.getLocation() : anchor;
        SpellWall.raise(ctx.level(), caster, payload, base, facing, Math.max(1, ctx.setting(WIDTH)), seconds(ctx, DURATION));
    }

    private Vec3 reachPoint(CastContext ctx, LivingEntity caster, SpellTarget origin) {
        HitResult hit = SpellRayTrace.clipBlocks(ctx.level(), caster, origin.position(), origin.position().add(origin.direction().scale(reach)));
        Vec3 end = SpellRayTrace.endOf(hit, origin.position(), origin.direction(), reach);
        return end.subtract(origin.direction().scale(STAND_OFF));
    }
}
