package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityJarNode extends BlockEntityNode {
    private static final int JARRED_FEED_FACTOR = 2;

    public BlockEntityJarNode(BlockPos pos, BlockState state) {
        super(TTBlockEntities.JAR_NODE.get(), pos, state);
    }

    @Override
    protected boolean allowDischarge() {
        return false;
    }

    @Override
    protected boolean allowTypeBehavior() {
        return false;
    }

    @Override
    protected int feedingIntervalFactor() {
        return JARRED_FEED_FACTOR * super.feedingIntervalFactor();
    }

    @Override
    public void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        NodeData data = components.get(TTDataComponents.NODE_DATA.get());
        if (data != null) {
            applyNodeData(data);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(
                TTDataComponents.NODE_DATA.get(),
                new NodeData(getNodeType(), Optional.ofNullable(getNodeModifier()), getAspects(), getAspectsBase()));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag output) {
        output.remove("Type");
        output.remove("Modifier");
        output.remove("Aspects");
        output.remove("AspectsBase");
    }
}
