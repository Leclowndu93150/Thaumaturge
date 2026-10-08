package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryView;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class EldritchReliquaryRenderState {
    public Direction facing = Direction.NORTH;
    public ReliquaryView view = ReliquaryView.INELIGIBLE;
    public float ticks;
    public float rewardLift;
    public @Nullable ItemStack reward;
}
