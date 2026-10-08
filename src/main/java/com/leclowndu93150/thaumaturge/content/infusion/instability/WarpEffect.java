package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

public record WarpEffect(float permanentChance, IntProvider permanent, IntProvider temporary)
        implements InstabilityEffect {
    public static final MapCodec<WarpEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("permanent_chance").forGetter(WarpEffect::permanentChance),
                    IntProvider.CODEC.fieldOf("permanent").forGetter(WarpEffect::permanent),
                    IntProvider.CODEC.fieldOf("temporary").forGetter(WarpEffect::temporary))
            .apply(instance, WarpEffect::new));

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.WARP.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        List<ServerPlayer> players = context.nearby(ServerPlayer.class);
        if (players.isEmpty()) {
            return;
        }
        RandomSource random = context.random();
        ServerPlayer target = players.get(random.nextInt(players.size()));
        if (random.nextFloat() < permanentChance) {
            WarpHelper.addWarp(target, permanent.sample(random), WarpType.NORMAL);
        } else {
            WarpHelper.addWarp(target, temporary.sample(random), WarpType.TEMPORARY);
        }
    }
}
