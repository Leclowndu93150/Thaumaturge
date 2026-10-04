package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public record RotatedBlueprint(Blueprint blueprint, int quarterTurns) {
    private static final int FULL_TURN = 4;

    public RotatedBlueprint {
        quarterTurns = Math.floorMod(quarterTurns, FULL_TURN);
    }

    public int width() {
        return sideways() ? blueprint.zSize() : blueprint.xSize();
    }

    public int depth() {
        return sideways() ? blueprint.xSize() : blueprint.zSize();
    }

    public int height() {
        return blueprint.ySize();
    }

    public @Nullable BlueprintPart partAt(int layer, int x, int z) {
        int lastX = blueprint.xSize() - 1;
        int lastZ = blueprint.zSize() - 1;
        return switch (quarterTurns) {
            case 1 -> blueprint.cell(layer, lastX - z, x);
            case 2 -> blueprint.cell(layer, lastX - x, lastZ - z);
            case 3 -> blueprint.cell(layer, z, lastZ - x);
            default -> blueprint.cell(layer, x, z);
        };
    }

    public List<BlueprintCell> cells() {
        List<BlueprintCell> cells = new ArrayList<>();
        int topLayer = height() - 1;
        for (int layer = 0; layer <= topLayer; layer++) {
            for (int x = 0; x < width(); x++) {
                for (int z = 0; z < depth(); z++) {
                    BlueprintPart part = partAt(layer, x, z);
                    if (part != null) {
                        cells.add(new BlueprintCell(new BlockPos(x, topLayer - layer, z), part));
                    }
                }
            }
        }
        return cells;
    }

    private boolean sideways() {
        return quarterTurns % 2 == 1;
    }
}
