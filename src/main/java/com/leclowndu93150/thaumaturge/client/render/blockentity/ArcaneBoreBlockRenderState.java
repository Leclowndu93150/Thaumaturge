package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;

public final class ArcaneBoreBlockRenderState extends BlockEntityRenderState {
    public boolean digging;
    public float yaw;
    public float pitch;
    public float beamUvScroll;
    public float beamSpin;
    public Vec3 tip = Vec3.ZERO;
    public int tipFrame;
}
