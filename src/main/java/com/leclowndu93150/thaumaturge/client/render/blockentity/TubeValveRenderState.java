package com.leclowndu93150.thaumaturge.client.render.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class TubeValveRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.UP;
    public float rotation;
    public long seed;
    public @Nullable BlockStateModel model;
    public final List<BlockStateModelPart> parts = new ArrayList<>();
}
