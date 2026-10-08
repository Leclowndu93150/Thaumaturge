package com.leclowndu93150.thaumaturge.client.model.connected;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record ConnectedTexture(ResourceLocation texture, ResourceLocation sheet, Optional<ResourceLocation> framed) {
    public static final MapCodec<ConnectedTexture> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(ConnectedTexture::texture),
                    ResourceLocation.CODEC.fieldOf("sheet").forGetter(ConnectedTexture::sheet),
                    ResourceLocation.CODEC.optionalFieldOf("framed").forGetter(ConnectedTexture::framed))
            .apply(instance, ConnectedTexture::new));
    public static final Codec<ConnectedTexture> CODEC = MAP_CODEC.codec();

    public ResourceLocation unconnectedCorner() {
        return framed.orElse(texture);
    }
}
