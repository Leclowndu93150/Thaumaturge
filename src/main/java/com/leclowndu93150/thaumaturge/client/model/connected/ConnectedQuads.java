package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class ConnectedQuads {
    public static final ConnectedQuads EMPTY = new Builder().build();
    private final Map<Direction, List<BakedQuad>> culled;
    private final List<BakedQuad> unculled;

    private ConnectedQuads(Map<Direction, List<BakedQuad>> culled, List<BakedQuad> unculled) {
        this.culled = culled;
        this.unculled = unculled;
    }

    public List<BakedQuad> get(@Nullable Direction side) {
        return side == null ? unculled : culled.getOrDefault(side, List.of());
    }

    public static final class Builder {
        private final Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
        private final List<BakedQuad> unculled = new ArrayList<>();

        public Builder addCulledFace(Direction side, BakedQuad quad) {
            culled.computeIfAbsent(side, ignored -> new ArrayList<>()).add(quad);
            return this;
        }

        public Builder addUnculledFace(BakedQuad quad) {
            unculled.add(quad);
            return this;
        }

        public Builder addAll(ConnectedQuads quads) {
            quads.culled.forEach((side, faces) ->
                    culled.computeIfAbsent(side, ignored -> new ArrayList<>()).addAll(faces));
            unculled.addAll(quads.unculled);
            return this;
        }

        public ConnectedQuads build() {
            Map<Direction, List<BakedQuad>> copy = new EnumMap<>(Direction.class);
            culled.forEach((side, faces) -> copy.put(side, List.copyOf(faces)));
            return new ConnectedQuads(Map.copyOf(copy), List.copyOf(unculled));
        }
    }
}
