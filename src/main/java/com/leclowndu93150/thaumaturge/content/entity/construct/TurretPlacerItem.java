package com.leclowndu93150.thaumaturge.content.entity.construct;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class TurretPlacerItem extends Item {
    private final ConstructDeployment<?> deployment;

    public TurretPlacerItem(Item.Properties properties, ConstructDeployment<?> deployment) {
        super(properties);
        this.deployment = deployment;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return deployment.deploy(context);
    }
}
