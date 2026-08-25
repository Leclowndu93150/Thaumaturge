package com.leclowndu93150.thaumaturge.content.casters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SocketedFocus(ItemStack focus) {
    public SocketedFocus {
        if (focus == null) focus = ItemStack.EMPTY;
    }

    public static final Codec<SocketedFocus> CODEC = RecordCodecBuilder.create(
            i -> i.group(ItemStack.CODEC.fieldOf("focus").forGetter(SocketedFocus::focus))
                    .apply(i, SocketedFocus::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SocketedFocus> STREAM_CODEC =
            StreamCodec.composite(ItemStack.STREAM_CODEC, SocketedFocus::focus, SocketedFocus::new);
}
