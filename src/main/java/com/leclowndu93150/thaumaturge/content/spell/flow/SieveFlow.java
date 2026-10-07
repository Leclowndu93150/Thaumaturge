package com.leclowndu93150.thaumaturge.content.spell.flow;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record SieveFlow() implements SpellBehavior {
    public static final MapCodec<SieveFlow> CODEC = MapCodec.unit(new SieveFlow());

    private static final String SETTING = "keep";
    private static final int ENTITIES = 0;
    private static final int BLOCKS = 1;
    private static final int ENEMIES = 2;
    private static final int ALLIES = 3;
    private static final int UNDEAD = 4;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.SIEVE.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        int mode = ctx.setting(SETTING);
        List<SpellTarget> kept = new ArrayList<>(incoming.size());
        for (SpellTarget target : incoming) {
            if (keeps(ctx, target, mode)) {
                kept.add(target);
            }
        }
        ctx.proceed(kept);
    }

    private static boolean keeps(CastContext ctx, SpellTarget target, int mode) {
        if (mode == BLOCKS) {
            return target.block().isPresent();
        }
        Entity entity = target.entity().orElse(null);
        if (!(entity instanceof LivingEntity living)) {
            return mode == ENTITIES && entity != null;
        }
        return switch (mode) {
            case ENTITIES -> true;
            case ENEMIES -> !ctx.isAlly(living);
            case ALLIES -> ctx.isAlly(living);
            case UNDEAD -> living.is(EntityTypeTags.UNDEAD);
            default -> !living.is(EntityTypeTags.UNDEAD);
        };
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
