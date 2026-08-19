package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/**
 * A multiblock structure diagram shown on a research entry page, rendered as an exploded
 * isometric view of the structure's layers with an optional vis cost underneath.
 *
 * <p>Cells are listed top layer first. Within a layer, cells run in x-major order matching
 * the in-world footprint. Each cell is either an item id ({@code "minecraft:glass"}), an item
 * tag reference ({@code "#minecraft:wooden_slabs"}, cycled in the display), or the empty
 * string for a position left open.
 *
 * @param xSize the structure footprint along x, at least 1
 * @param ySize the structure height in layers, at least 1
 * @param zSize the structure footprint along z, at least 1
 * @param cells the cell specifiers, exactly {@code xSize * ySize * zSize} entries
 * @param cost the vis paid when the structure is activated, empty when activation is free
 * @since 1.0.0
 */
public record ResearchConstruct(int xSize, int ySize, int zSize, List<String> cells, AspectList cost) {
    public static final Codec<ResearchConstruct> CODEC = RecordCodecBuilder
            .<ResearchConstruct>create(instance -> instance.group(Codec.INT.fieldOf("x_size").forGetter(ResearchConstruct::xSize), Codec.INT.fieldOf("y_size").forGetter(ResearchConstruct::ySize),
                    Codec.INT.fieldOf("z_size").forGetter(ResearchConstruct::zSize), Codec.STRING.listOf().fieldOf("cells").forGetter(ResearchConstruct::cells),
                    AspectList.CODEC.optionalFieldOf("cost", AspectList.EMPTY).forGetter(ResearchConstruct::cost)).apply(instance, ResearchConstruct::new))
            .validate(construct -> construct.cells().size() == construct.xSize() * construct.ySize() * construct.zSize()
                    ? DataResult.success(construct)
                    : DataResult.error(() -> "construct cells must contain exactly x_size * y_size * z_size entries"));
}
