package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.resources.Identifier;

public final class ParticleSheet {
    private static final int PNG_DIMENSION_OFFSET = 16;

    private final String name;
    private final Identifier texture;
    private final Map<RenderPipeline, SingleQuadParticle.Layer> layers = new ConcurrentHashMap<>();
    private volatile int frames;

    ParticleSheet(String name, Identifier texture) {
        this.name = name;
        this.texture = texture;
    }

    public Identifier texture() {
        return texture;
    }

    public int frames() {
        int resolved = frames;
        if (resolved == 0) {
            resolved = readFrameCount();
            frames = resolved;
        }
        return resolved;
    }

    public float u0(int frame) {
        return (float) frame / frames();
    }

    public float u1(int frame) {
        return (float) (frame + 1) / frames();
    }

    SingleQuadParticle.Layer layer(boolean sorted, RenderPipeline pipeline) {
        return layers.computeIfAbsent(pipeline, p -> new SingleQuadParticle.Layer(sorted, texture, p));
    }

    void invalidate() {
        frames = 0;
        layers.clear();
    }

    private int readFrameCount() {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture).open(); DataInputStream data = new DataInputStream(stream)) {
            data.skipBytes(PNG_DIMENSION_OFFSET);
            int width = data.readInt();
            int height = data.readInt();
            return Math.max(1, width / Math.max(1, height));
        } catch (IOException e) {
            Thaumaturge.LOGGER.error("Cannot read particle sheet {}", texture, e);
            return 1;
        }
    }
}
