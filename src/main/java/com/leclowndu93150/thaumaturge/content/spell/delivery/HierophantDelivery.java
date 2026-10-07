package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.AbstractHierophantSpell;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

public record HierophantDelivery(EntityType<?> entity) implements SpellBehavior {
    public static final MapCodec<HierophantDelivery> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(HierophantDelivery::entity)).apply(i, HierophantDelivery::new));

    private static final String LEFT = "left";

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.HIEROPHANT.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        if (!(ctx.caster() instanceof EntityEldritchHierophant caster) || !ctx.hasNext()) {
            return;
        }
        boolean left = ctx.setting(LEFT) == 1;
        for (SpellTarget origin : incoming) {
            Entity created = entity.create(ctx.level(), EntitySpawnReason.MOB_SUMMONED);
            if (created instanceof AbstractHierophantSpell spell) {
                spell.cast(caster, ctx.continuation(), origin.position(), origin.direction(), left);
                ctx.level().addFreshEntity(spell);
            }
        }
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
