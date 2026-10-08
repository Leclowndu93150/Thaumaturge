package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

record RoomRecipe(
        String name,
        int width,
        int depth,
        List<RoomSocket> sockets,
        List<TagKey<RoomType>> tags,
        int weight,
        int maxPerMaze,
        boolean decorate,
        Optional<String> varietyGroup,
        Optional<ResourceKey<StructureProcessorList>> processors,
        Consumer<RoomCanvas> body) {
    private static final String TEMPLATE_PREFIX = "outer_lands/";

    ResourceKey<RoomType> key() {
        return ResourceKey.create(RoomType.REGISTRY_KEY, TTIds.rl(name));
    }

    ResourceLocation template() {
        return TTIds.rl(TEMPLATE_PREFIX + name);
    }

    static Builder builder(String name, Consumer<RoomCanvas> body) {
        return new Builder(name, body);
    }

    static final class Builder {
        private final String name;
        private final Consumer<RoomCanvas> body;
        private final List<RoomSocket> sockets = new ArrayList<>();
        private final List<TagKey<RoomType>> tags = new ArrayList<>();
        private int width = 1;
        private int depth = 1;
        private int weight = 1;
        private int maxPerMaze = RoomType.UNLIMITED;
        private boolean decorate = true;
        private Optional<String> varietyGroup = Optional.empty();
        private Optional<ResourceKey<StructureProcessorList>> processors = Optional.empty();

        private Builder(String name, Consumer<RoomCanvas> body) {
            this.name = name;
            this.body = body;
        }

        Builder footprint(int width, int depth) {
            this.width = width;
            this.depth = depth;
            return this;
        }

        Builder junction(Junction junction) {
            for (Direction side : junction.sides()) {
                socket(0, 0, side);
            }
            return this;
        }

        Builder entrance() {
            return socket(0, 0, Direction.NORTH);
        }

        Builder socket(int cellX, int cellZ, Direction side) {
            sockets.add(new RoomSocket(cellX, cellZ, side));
            return this;
        }

        @SafeVarargs
        final Builder tags(TagKey<RoomType>... keys) {
            tags.addAll(List.of(keys));
            return this;
        }

        Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        Builder maxPerMaze(int maxPerMaze) {
            this.maxPerMaze = maxPerMaze;
            return this;
        }

        Builder plain() {
            this.decorate = false;
            return this;
        }

        Builder variety(String group) {
            this.varietyGroup = Optional.of(group);
            return this;
        }

        Builder processors(ResourceKey<StructureProcessorList> processors) {
            this.processors = Optional.of(processors);
            return this;
        }

        RoomRecipe build() {
            return new RoomRecipe(
                    name,
                    width,
                    depth,
                    List.copyOf(sockets),
                    List.copyOf(tags),
                    weight,
                    maxPerMaze,
                    decorate,
                    varietyGroup,
                    processors,
                    body);
        }
    }
}
