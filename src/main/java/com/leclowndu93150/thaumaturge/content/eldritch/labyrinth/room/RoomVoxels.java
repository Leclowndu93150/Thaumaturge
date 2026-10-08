package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

public interface RoomVoxels {
    int sizeX();

    int sizeY();

    int sizeZ();

    Kind kind(int x, int y, int z);

    static int index(int x, int y, int z, int sizeY, int sizeZ) {
        return (x * sizeY + y) * sizeZ + z;
    }

    enum Kind {
        UNSPECIFIED,
        AIR,
        SOLID,
        PASSABLE,
        MARKER;

        public boolean open() {
            return this == AIR || this == PASSABLE || this == MARKER;
        }
    }
}
