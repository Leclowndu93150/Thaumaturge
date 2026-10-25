package com.leclowndu93150.thaumaturge.content.entity;

import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RiftBounds {
    private double minX = Double.POSITIVE_INFINITY;
    private double minY = Double.POSITIVE_INFINITY;
    private double minZ = Double.POSITIVE_INFINITY;
    private double maxX = Double.NEGATIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;
    private double maxZ = Double.NEGATIVE_INFINITY;

    static AABB around(List<Vec3> points, Vec3 offset) {
        RiftBounds bounds = new RiftBounds();
        points.forEach(bounds::include);
        return bounds.shifted(offset);
    }

    private void include(Vec3 point) {
        this.minX = Math.min(this.minX, point.x);
        this.minY = Math.min(this.minY, point.y);
        this.minZ = Math.min(this.minZ, point.z);
        this.maxX = Math.max(this.maxX, point.x);
        this.maxY = Math.max(this.maxY, point.y);
        this.maxZ = Math.max(this.maxZ, point.z);
    }

    private AABB shifted(Vec3 offset) {
        return new AABB(
                this.minX + offset.x,
                this.minY + offset.y,
                this.minZ + offset.z,
                this.maxX + offset.x,
                this.maxY + offset.y,
                this.maxZ + offset.z);
    }
}
