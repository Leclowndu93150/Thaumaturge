package com.leclowndu93150.thaumaturge.content.spell.flow;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public record RelayFlow(double seekRange) implements SpellBehavior {
    public static final MapCodec<RelayFlow> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.doubleRange(1.0, 64.0).optionalFieldOf("seek_range", 12.0).forGetter(RelayFlow::seekRange)).apply(i, RelayFlow::new));

    private static final String SETTING = "direction";
    private static final int FORWARD = 0;
    private static final int REFLECT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    private static final int TO_CASTER = 4;
    private static final double SURFACE_LIFT = 0.05;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.RELAY.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        int mode = ctx.setting(SETTING);
        List<SpellTarget> out = new ArrayList<>(incoming.size());
        for (SpellTarget target : incoming) {
            Vec3 position = lifted(target);
            out.add(target.moved(position, heading(ctx, target, position, mode)));
        }
        ctx.proceed(out);
    }

    private Vec3 heading(CastContext ctx, SpellTarget target, Vec3 position, int mode) {
        return switch (mode) {
            case FORWARD -> target.direction();
            case REFLECT -> reflect(target);
            case UP -> new Vec3(0.0, 1.0, 0.0);
            case DOWN -> new Vec3(0.0, -1.0, 0.0);
            case TO_CASTER -> {
                LivingEntity caster = ctx.caster();
                yield caster == null ? target.direction().reverse() : caster.getEyePosition().subtract(position);
            }
            default -> seek(ctx, target, position);
        };
    }

    private Vec3 seek(CastContext ctx, SpellTarget target, Vec3 position) {
        Entity struck = target.entity().orElse(null);
        Optional<LivingEntity> enemy = SpellTargeting.nearest(ctx.level(), position, seekRange,
                living -> living != struck && !ctx.isAlly(living) && SpellTargeting.canSee(ctx.level(), position, living, struck));
        return enemy.map(living -> living.getBoundingBox().getCenter().subtract(position)).orElse(target.direction());
    }

    private static Vec3 reflect(SpellTarget target) {
        Optional<BlockHitResult> block = target.block();
        if (block.isEmpty()) {
            return target.direction().reverse();
        }
        Direction face = block.get().getDirection();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 direction = target.direction();
        return direction.subtract(normal.scale(2.0 * direction.dot(normal)));
    }

    private static Vec3 lifted(SpellTarget target) {
        Optional<BlockHitResult> block = target.block();
        if (block.isEmpty()) {
            return target.position();
        }
        Direction face = block.get().getDirection();
        return target.position().add(face.getStepX() * SURFACE_LIFT, face.getStepY() * SURFACE_LIFT, face.getStepZ() * SURFACE_LIFT);
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
