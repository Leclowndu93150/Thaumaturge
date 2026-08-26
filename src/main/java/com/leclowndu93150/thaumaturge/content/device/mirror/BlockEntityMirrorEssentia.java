package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.content.infusion.EssentiaSources;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityMirrorEssentia extends BlockEntityMirrorBase implements IAspectSource {
    private static final int INSTABILITY_THRESHOLD = 64;
    private static final int TARGET_RANGE = 8;

    private @Nullable EssentiaSources targetSources;
    private @Nullable BlockPos targetSourcesCenter;

    public BlockEntityMirrorEssentia(BlockPos pos, BlockState state) {
        super(TCBlockEntities.MIRROR_ESSENTIA.get(), pos, state);
    }

    @Override
    protected int instabilityThreshold() {
        return INSTABILITY_THRESHOLD;
    }

    @Override
    protected boolean isSameKind(BlockEntity target) {
        return target instanceof BlockEntityMirrorEssentia;
    }

    public void serverTick(Level level, BlockPos pos) {
        tickLink();
    }

    private @Nullable EssentiaSources sourcesAtTarget() {
        if (link == null) {
            return null;
        }
        if (targetSources == null || !link.pos().equals(targetSourcesCenter)) {
            ServerLevel targetLevel = targetLevel();
            if (targetLevel == null) {
                return null;
            }
            BlockState targetState = targetLevel.getBlockState(link.pos());
            if (!targetState.hasProperty(BlockMirror.FACING)) {
                return null;
            }
            targetSources = new EssentiaSources(link.pos(), TARGET_RANGE)
                    .facing(targetState.getValue(BlockMirror.FACING))
                    .ignoring(blockEntity -> blockEntity instanceof BlockEntityMirrorEssentia)
                    .drainEffectTarget(Vec3.atCenterOf(link.pos()));
            targetSourcesCenter = link.pos();
        }
        return targetSources;
    }

    @Override
    public AspectList getAspects() {
        return AspectList.EMPTY;
    }

    @Override
    public void setAspects(AspectList aspects) {}

    @Override
    public boolean doesContainerAccept(Holder<IAspect> aspect) {
        return isLinkValidSimple();
    }

    @Override
    public int addToContainer(Holder<IAspect> aspect, int amount) {
        if (amount > 1 || !isLinkValid()) {
            return amount;
        }
        ServerLevel targetLevel = targetLevel();
        EssentiaSources sources = sourcesAtTarget();
        if (targetLevel == null || sources == null) {
            return amount;
        }
        if (sources.insert(targetLevel, aspect, 0)) {
            addInstability(amount);
            return 0;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Holder<IAspect> aspect, int amount) {
        if (amount > 1 || !isLinkValid()) {
            return false;
        }
        ServerLevel targetLevel = targetLevel();
        EssentiaSources sources = sourcesAtTarget();
        if (targetLevel == null || sources == null) {
            return false;
        }
        if (sources.drain(targetLevel, aspect, 0)) {
            addInstability(amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Holder<IAspect> aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(Holder<IAspect> aspect) {
        return 0;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }
}
