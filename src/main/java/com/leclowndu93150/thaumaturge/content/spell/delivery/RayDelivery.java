package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellRayTrace;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record RayDelivery(double range, boolean reach, Visual visual, float width, int linger) implements SpellBehavior {
    public static final MapCodec<RayDelivery> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.doubleRange(0.5, 64.0).optionalFieldOf("range", 16.0).forGetter(RayDelivery::range),
            Codec.BOOL.optionalFieldOf("reach", false).forGetter(RayDelivery::reach), Visual.CODEC.optionalFieldOf("visual", Visual.ARC).forGetter(RayDelivery::visual),
            Codec.FLOAT.optionalFieldOf("width", 0.66F).forGetter(RayDelivery::width), Codec.intRange(1, 100).optionalFieldOf("linger", 6).forGetter(RayDelivery::linger)).apply(i, RayDelivery::new));

    private static final String RANGE = "range";
    private static final double MOB_REACH = 3.0;
    private static final double FLASH_SPEED = 0.5;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.RAY.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        double distance = distance(ctx);
        int pierce = Math.max(0, Math.round(ctx.state().get(SpellStats.PIERCE)));
        SpellLook look = SpellLook.downstream(ctx);
        List<SpellTarget> out = new ArrayList<>();
        for (SpellTarget origin : incoming) {
            Entity source = origin.entity().orElse(ctx.caster());
            List<HitResult> hits = SpellRayTrace.pierce(ctx.level(), source, origin.position(), origin.direction(), distance, pierce + 1);
            Vec3 end = hits.isEmpty() ? origin.position().add(origin.direction().scale(distance)) : hits.getLast().getLocation();
            show(ctx, look, origin, end);
            for (HitResult hit : hits) {
                out.add(SpellTarget.of(hit, origin.direction()));
            }
        }
        ctx.proceed(out);
    }

    private double distance(CastContext ctx) {
        if (ctx.part().setting(RANGE).isPresent()) {
            return ctx.setting(RANGE);
        }
        if (!reach) {
            return range;
        }
        LivingEntity caster = ctx.caster();
        return caster instanceof Player player ? player.blockInteractionRange() : MOB_REACH;
    }

    private void show(CastContext ctx, SpellLook look, SpellTarget origin, Vec3 end) {
        switch (visual) {
            case ARC -> ctx.fx().arc(origin.position(), end, look.color(), width * ctx.power());
            case BEAM -> ctx.fx().beam(origin.position(), end, look.color(), linger);
            case FLASH -> ctx.fx().burst(look.fx(), origin.position(), origin.direction().scale(FLASH_SPEED), look.color());
        }
    }

    public enum Visual implements StringRepresentable {
        ARC("arc"), BEAM("beam"), FLASH("flash");

        public static final Codec<Visual> CODEC = StringRepresentable.fromEnum(Visual::values);

        private final String name;

        Visual(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
