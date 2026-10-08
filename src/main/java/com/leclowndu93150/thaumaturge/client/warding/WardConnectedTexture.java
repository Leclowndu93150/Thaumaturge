package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.connected.FaceCorners;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class WardConnectedTexture {
    public static final ResourceLocation SPRITES = TTIds.rl("block/ward");
    private static final int NEIGHBOURS = 8;
    private static final int[] COLUMN = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] ROW = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final ResourceLocation[] SPRITE_IDS = FaceCorners.sprites(SPRITES);

    private WardConnectedTexture() {}

    public static ResourceLocation sprite(int corner, int state) {
        return SPRITE_IDS[FaceCorners.slot(corner, state)];
    }

    public static int connectionMask(
            BlockPos pos, Direction face, BlockPos.MutableBlockPos cursor, Predicate<BlockPos> connected) {
        int connections = 0;
        for (int bit = 0; bit < NEIGHBOURS; bit++) {
            if (probe(pos, face, bit, cursor, connected)) {
                connections |= 1 << bit;
            }
        }
        return connections;
    }

    private static boolean probe(
            BlockPos pos, Direction face, int bit, BlockPos.MutableBlockPos cursor, Predicate<BlockPos> connected) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (face.getAxis()) {
            case Y -> cursor.set(x + COLUMN[bit], y, z + ROW[bit]);
            case Z -> {
                int near = face == Direction.NORTH ? -1 : 1;
                cursor.set(x + COLUMN[bit] * near, y - ROW[bit], z);
            }
            case X -> {
                int near = face == Direction.WEST ? 1 : -1;
                cursor.set(x, y - ROW[bit], z + COLUMN[bit] * near);
            }
        }
        return connected.test(cursor);
    }
}
