package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

record ReplantSite(BlockPos pos, Direction face, ItemStack seed, boolean tilled, @Nullable Task task) {
    private static final long DEFAULT_POS = 0L;
    private static final byte DEFAULT_FACE = 0;

    static final MapCodec<ReplantSite> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance
                    .group(Codec.LONG.optionalFieldOf("pos", DEFAULT_POS).forGetter(site -> site.pos().asLong()),
                            Codec.BYTE.optionalFieldOf("face", DEFAULT_FACE).forGetter(site -> (byte) site.face().get3DDataValue()),
                            Codec.BOOL.optionalFieldOf("tilled", false).forGetter(ReplantSite::tilled), ItemStack.OPTIONAL_CODEC.optionalFieldOf("seed", ItemStack.EMPTY).forGetter(ReplantSite::seed))
                    .apply(instance, ReplantSite::restore));

    private static ReplantSite restore(long pos, byte face, boolean tilled, ItemStack seed) {
        return new ReplantSite(BlockPos.of(pos), Direction.from3DDataValue(face), seed, tilled, null);
    }

    ReplantSite withTask(@Nullable Task assigned) {
        return new ReplantSite(pos, face, seed, tilled, assigned);
    }
}
