package com.leclowndu93150.thaumaturge.data.labyrinth;

@FunctionalInterface
interface RoomShape {
    boolean contains(int x, int y, int z);

    default RoomShape and(RoomShape other) {
        return (x, y, z) -> contains(x, y, z) && other.contains(x, y, z);
    }

    default RoomShape minus(RoomShape other) {
        return (x, y, z) -> contains(x, y, z) && !other.contains(x, y, z);
    }

    static RoomShape box(int x0, int y0, int z0, int x1, int y1, int z1) {
        int minX = Math.min(x0, x1);
        int minY = Math.min(y0, y1);
        int minZ = Math.min(z0, z1);
        int maxX = Math.max(x0, x1);
        int maxY = Math.max(y0, y1);
        int maxZ = Math.max(z0, z1);
        return (x, y, z) -> x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    static RoomShape cylinder(double centerX, double centerZ, double radius, int y0, int y1) {
        double radiusSq = radius * radius;
        return (x, y, z) -> {
            double dx = x + 0.5 - centerX;
            double dz = z + 0.5 - centerZ;
            return y >= y0 && y <= y1 && dx * dx + dz * dz <= radiusSq;
        };
    }

    static RoomShape annulus(double centerX, double centerZ, double outer, double inner, int y0, int y1) {
        return cylinder(centerX, centerZ, outer, y0, y1).minus(cylinder(centerX, centerZ, inner, y0, y1));
    }

    static RoomShape octagon(double centerX, double centerZ, double half, double chamfer, int y0, int y1) {
        return (x, y, z) -> {
            double dx = Math.abs(x + 0.5 - centerX);
            double dz = Math.abs(z + 0.5 - centerZ);
            return y >= y0 && y <= y1 && dx <= half && dz <= half && dx + dz <= half * 2.0 - chamfer;
        };
    }

    static RoomShape ellipsoid(
            double centerX, double centerY, double centerZ, double radiusX, double radiusY, double radiusZ) {
        return (x, y, z) -> {
            double dx = (x + 0.5 - centerX) / radiusX;
            double dy = (y + 0.5 - centerY) / radiusY;
            double dz = (z + 0.5 - centerZ) / radiusZ;
            return dx * dx + dy * dy + dz * dz <= 1.0;
        };
    }

    static RoomShape dome(double centerX, int baseY, double centerZ, double radiusX, double radiusY, double radiusZ) {
        return ellipsoid(centerX, baseY, centerZ, radiusX, radiusY, radiusZ).and((x, y, z) -> y > baseY);
    }
}
