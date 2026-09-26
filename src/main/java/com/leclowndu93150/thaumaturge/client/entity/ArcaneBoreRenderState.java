package com.leclowndu93150.thaumaturge.client.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class ArcaneBoreRenderState extends LivingEntityRenderState {
    public boolean digging;
    public float headPitch;
    public float beamUvScroll;
    public float beamSpin;
    public Vec3 tip = Vec3.ZERO;
    public int tipFrame;
}
