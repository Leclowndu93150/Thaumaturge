package com.leclowndu93150.thaumaturge.api.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * Makes an item rechargeable from the aura. Stored as the {@code thaumaturge:rechargeable} data component, usually as a default
 * component set on the item, so datapacks and KubeJS can make any item rechargeable. The current charge lives separately in the
 * {@code thaumaturge:charge} component and is read and written through {@link RechargeAccess}.
 *
 * @param capacity the most vis the stack holds; positive
 * @param display  when the recharge HUD shows the stack's meter
 * @since 1.0.0
 */
public record ChargeProfile(int capacity, ChargeDisplay display) {
    /** The persistent codec. */
    public static final Codec<ChargeProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("capacity").forGetter(ChargeProfile::capacity),
                    ChargeDisplay.CODEC
                            .optionalFieldOf("display", ChargeDisplay.ALWAYS)
                            .forGetter(ChargeProfile::display))
            .apply(instance, ChargeProfile::new));

    /** The network codec. */
    public static final StreamCodec<ByteBuf, ChargeProfile> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ChargeProfile::capacity,
            ByteBufCodecs.idMapper(index -> ChargeDisplay.values()[index], ChargeDisplay::ordinal),
            ChargeProfile::display,
            ChargeProfile::new);
}
