package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryStateView;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStates;
import java.util.List;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class GolemRenderState extends CopperGolemRenderState {
    public GolemProperties props;
    public byte color;
    public float bodyRot;
    public float headYawDelta;
    public float pitch;
    public float walkPos;
    public float walkSpeed;
    public double speedSq;
    public float yawDelta;
    public float wheelRotation;
    public float grinderRot;
    public boolean combat;
    public boolean invisible;
    public boolean ghost;
    public boolean xray;
    public final ItemStackRenderState heldItem = new ItemStackRenderState();
    public boolean holdingItem;
    public final ItemStackRenderState haulerItem = new ItemStackRenderState();
    public boolean haulingItem;
    public boolean heldItemIsBlock;
    public boolean haulerItemIsBlock;
    public List<GolemAccessory> accessories = List.of();
    public GolemAccessoryStateView accessoryStates = GolemAccessoryStates.EMPTY;
}
