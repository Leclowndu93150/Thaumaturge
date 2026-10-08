package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CrownShape(
        int trunkWidth, double trunkShare, double branchSlope, double crownWidth, boolean stackedCrown) {
    public static final Codec<CrownShape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(1, 2).fieldOf("trunk_width").forGetter(CrownShape::trunkWidth),
                    Codec.doubleRange(0.0, 1.0).fieldOf("trunk_share").forGetter(CrownShape::trunkShare),
                    Codec.DOUBLE.fieldOf("branch_slope").forGetter(CrownShape::branchSlope),
                    Codec.DOUBLE.fieldOf("crown_width").forGetter(CrownShape::crownWidth),
                    Codec.BOOL.fieldOf("stacked_crown").forGetter(CrownShape::stackedCrown))
            .apply(instance, CrownShape::new));
}
