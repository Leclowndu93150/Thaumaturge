package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.mojang.serialization.Codec;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum CrownRule implements StringRepresentable {
    OPEN_AIR("open_air", false),
    REPLACEABLE("replaceable", true);

    public static final Codec<CrownRule> CODEC = StringRepresentable.fromEnum(CrownRule::values);

    private static final double CROWN_FLOOR = 0.3;
    private static final float CROWN_FLOOR_SINGLE = 0.3F;
    private static final double CENTER = 0.5;
    private static final float CENTER_SINGLE = 0.5F;

    private final String name;
    private final boolean singlePrecision;

    CrownRule(String name, boolean singlePrecision) {
        this.name = name;
        this.singlePrecision = singlePrecision;
    }

    public static boolean canHostLog(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.canBeReplaced();
    }

    public boolean isOpen(BlockState state, Block ownLeaves) {
        return singlePrecision ? canHostLog(state) : state.isAir() || state.is(ownLeaves);
    }

    public boolean belowCrown(int layer, int heightLimit) {
        return singlePrecision ? layer < heightLimit * CROWN_FLOOR_SINGLE : layer < heightLimit * CROWN_FLOOR;
    }

    public int clusterCoordinate(int trunk, double offset) {
        return singlePrecision ? trunk + Mth.floor(offset + CENTER) : Mth.floor(offset + trunk + CENTER);
    }

    public int lineCoordinate(int start, int delta, int steps, int index, boolean probing) {
        if (singlePrecision) {
            return start + Mth.floor(CENTER_SINGLE + index * ((float) delta / steps));
        }
        return Mth.floor(start + index * ((double) delta / steps) + (probing ? 0.0 : CENTER));
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
