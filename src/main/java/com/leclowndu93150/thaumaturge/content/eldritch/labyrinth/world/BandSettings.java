package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.SocketProfile;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;

public record BandSettings(int baseY, Optional<BlockState> fill) {
    public static final int DEFAULT_BASE_Y = 32;
    private static final int MAX_BASE_Y = OuterLandsChunkGenerator.GEN_DEPTH - SocketProfile.HEIGHT;
    public static final BandSettings DEFAULT = new BandSettings(DEFAULT_BASE_Y, Optional.empty());
    private static final Codec<Integer> BASE_Y =
            Codec.INT.validate(value -> value % SectionPos.SECTION_SIZE == 0 && value >= 0 && value <= MAX_BASE_Y
                    ? DataResult.success(value)
                    : DataResult.error(
                            () -> "Band base_y must be a multiple of 16 from 0 to " + MAX_BASE_Y + ", got " + value));
    public static final Codec<BandSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BASE_Y.optionalFieldOf("base_y", DEFAULT_BASE_Y).forGetter(BandSettings::baseY),
                    BlockState.CODEC.optionalFieldOf("fill").forGetter(BandSettings::fill))
            .apply(instance, BandSettings::new));

    public int floorY() {
        return baseY + SocketProfile.FLOOR;
    }

    public int topY() {
        return baseY + SocketProfile.HEIGHT;
    }

    public BlockState fillState() {
        return fill.orElseGet(() -> TTBlocks.ELDRITCH_NOTHING_DORMANT.get().defaultBlockState());
    }
}
