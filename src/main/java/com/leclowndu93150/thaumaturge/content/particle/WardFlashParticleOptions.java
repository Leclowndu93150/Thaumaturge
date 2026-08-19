package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record WardFlashParticleOptions(Direction face, float hitX, float hitY, float hitZ) implements ParticleOptions {
    private static final float CENTRE = 0.5F;

    public static final MapCodec<WardFlashParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(Direction.CODEC.fieldOf("face").forGetter(WardFlashParticleOptions::face), Codec.FLOAT.fieldOf("hit_x").forGetter(WardFlashParticleOptions::hitX),
                    Codec.FLOAT.fieldOf("hit_y").forGetter(WardFlashParticleOptions::hitY), Codec.FLOAT.fieldOf("hit_z").forGetter(WardFlashParticleOptions::hitZ))
            .apply(inst, WardFlashParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WardFlashParticleOptions> STREAM_CODEC = StreamCodec.composite(Direction.STREAM_CODEC, WardFlashParticleOptions::face, ByteBufCodecs.FLOAT,
            WardFlashParticleOptions::hitX, ByteBufCodecs.FLOAT, WardFlashParticleOptions::hitY, ByteBufCodecs.FLOAT, WardFlashParticleOptions::hitZ, WardFlashParticleOptions::new);

    /**
     * Builds options for a flash on one face of a block, taking the impact point from the hit
     * location when one is known and the block centre otherwise.
     *
     * @param pos  the warded block
     * @param face the struck face
     * @param hit  the exact impact point, or null when unknown
     * @return the particle options
     */
    public static WardFlashParticleOptions at(BlockPos pos, Direction face, @Nullable Vec3 hit) {
        if (hit == null) {
            return new WardFlashParticleOptions(face, CENTRE, CENTRE, CENTRE);
        }
        return new WardFlashParticleOptions(face, (float) (hit.x - pos.getX()), (float) (hit.y - pos.getY()), (float) (hit.z - pos.getZ()));
    }

    @Override
    public ParticleType<?> getType() {
        return TCParticles.WARD_FLASH.get();
    }
}
