package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record BoreDebrisParticleOptions(
        BlockState state, int targetEntityId, double tx, double ty, double tz, double sx, double sy, double sz)
        implements ParticleOptions {
    public static final int NO_ENTITY = -1;
    private static final Codec<BlockState> BLOCK_STATE_CODEC =
            Codec.withAlternative(BlockState.CODEC, BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState);

    public static final MapCodec<BoreDebrisParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    BLOCK_STATE_CODEC.fieldOf("block_state").forGetter(BoreDebrisParticleOptions::state),
                    Codec.INT.fieldOf("target_entity_id").forGetter(BoreDebrisParticleOptions::targetEntityId),
                    Codec.DOUBLE.fieldOf("tx").forGetter(BoreDebrisParticleOptions::tx),
                    Codec.DOUBLE.fieldOf("ty").forGetter(BoreDebrisParticleOptions::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(BoreDebrisParticleOptions::tz),
                    Codec.DOUBLE.fieldOf("sx").forGetter(BoreDebrisParticleOptions::sx),
                    Codec.DOUBLE.fieldOf("sy").forGetter(BoreDebrisParticleOptions::sy),
                    Codec.DOUBLE.fieldOf("sz").forGetter(BoreDebrisParticleOptions::sz))
            .apply(inst, BoreDebrisParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoreDebrisParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, o) -> {
                buf.writeVarInt(Block.getId(o.state()));
                buf.writeVarInt(o.targetEntityId());
                buf.writeDouble(o.tx());
                buf.writeDouble(o.ty());
                buf.writeDouble(o.tz());
                buf.writeDouble(o.sx());
                buf.writeDouble(o.sy());
                buf.writeDouble(o.sz());
            },
            buf -> new BoreDebrisParticleOptions(
                    Block.stateById(buf.readVarInt()),
                    buf.readVarInt(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()));

    public BoreDebrisParticleOptions(
            BlockState state, double tx, double ty, double tz, double sx, double sy, double sz) {
        this(state, NO_ENTITY, tx, ty, tz, sx, sy, sz);
    }

    @Override
    public ParticleType<?> getType() {
        return TTParticles.BORE_DEBRIS.get();
    }
}
