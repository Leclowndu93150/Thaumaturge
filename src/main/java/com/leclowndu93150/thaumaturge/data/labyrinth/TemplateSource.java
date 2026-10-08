package com.leclowndu93150.thaumaturge.data.labyrinth;

import java.util.Optional;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

interface TemplateSource {
    int sizeX();

    int sizeY();

    int sizeZ();

    @Nullable
    BlockState get(int x, int y, int z);

    Optional<String> metadata(int x, int y, int z);
}
