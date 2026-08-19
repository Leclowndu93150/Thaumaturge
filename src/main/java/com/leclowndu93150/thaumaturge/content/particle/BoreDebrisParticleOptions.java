package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record BoreDebrisParticleOptions(BlockState state, int targetEntityId, double tx, double ty, double tz, double sx, double sy, double sz) implements ParticleOptions {
    public static final int NO_ENTITY = -1;
    private static final Codec<BlockState> BLOCK_STATE_CODEC = Codec.withAlternative(BlockState.CODEC, BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState);

    public static final MapCodec<BoreDebrisParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(BLOCK_STATE_CODEC.fieldOf("block_state").forGetter(BoreDebrisParticleOptions::state), Codec.INT.fieldOf("target_entity_id").forGetter(BoreDebrisParticleOptions::targetEntityId),
                    Codec.DOUBLE.fieldOf("tx").forGetter(BoreDebrisParticleOptions::tx), Codec.DOUBLE.fieldOf("ty").forGetter(BoreDebrisParticleOptions::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(BoreDebrisParticleOptions::tz), Codec.DOUBLE.fieldOf("sx").forGetter(BoreDebrisParticleOptions::sx),
                    Codec.DOUBLE.fieldOf("sy").forGetter(BoreDebrisParticleOptions::sy), Codec.DOUBLE.fieldOf("sz").forGetter(BoreDebrisParticleOptions::sz))
            .apply(inst, BoreDebrisParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoreDebrisParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
            BoreDebrisParticleOptions::state, ByteBufCodecs.VAR_INT, BoreDebrisParticleOptions::targetEntityId, ByteBufCodecs.DOUBLE, BoreDebrisParticleOptions::tx, ByteBufCodecs.DOUBLE,
            BoreDebrisParticleOptions::ty, ByteBufCodecs.DOUBLE, BoreDebrisParticleOptions::tz, ByteBufCodecs.DOUBLE, BoreDebrisParticleOptions::sx, ByteBufCodecs.DOUBLE,
            BoreDebrisParticleOptions::sy, ByteBufCodecs.DOUBLE, BoreDebrisParticleOptions::sz, BoreDebrisParticleOptions::new);

    public BoreDebrisParticleOptions(BlockState state, double tx, double ty, double tz, double sx, double sy, double sz) {
        this(state, NO_ENTITY, tx, ty, tz, sx, sy, sz);
    }

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BORE_DEBRIS.get();
    }
}
