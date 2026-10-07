package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSporeStalk;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractTaintSpore extends Monster {
    public static final int MIN_SIZE = 2;
    public static final int MAX_SIZE = 10;

    private static final EntityDataAccessor<Integer> DATA_SPORE_SIZE = SynchedEntityData.defineId(AbstractTaintSpore.class, EntityDataSerializers.INT);
    private static final int CHECK_INTERVAL = 20;
    private static final int GROWTH_INTERVAL = 1200;
    private static final float DISPLAY_GROWTH_PER_TICK = 0.02F;
    private static final float STARVE_DAMAGE = 1.0F;
    private static final float UNSET_DISPLAY_SIZE = -1.0F;
    private static final byte EVENT_RELEASE = 16;
    private static final int RELEASE_TICKS = 30;

    private boolean burst;
    private float displaySize = UNSET_DISPLAY_SIZE;
    private float oldDisplaySize = UNSET_DISPLAY_SIZE;
    private int releaseTicks;

    protected AbstractTaintSpore(EntityType<? extends AbstractTaintSpore> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    protected abstract boolean requiresStalkSupport();

    protected abstract void onBurst(ServerLevel level);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        super.defineSynchedData(data);
        data.define(DATA_SPORE_SIZE, MIN_SIZE);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            return;
        }
        if (releaseTicks > 0) {
            releaseTicks--;
        }
        float target = getSporeSize();
        if (displaySize < 0.0F) {
            displaySize = target;
            oldDisplaySize = target;
            return;
        }
        oldDisplaySize = displaySize;
        displaySize = displaySize < target ? Math.min(target, displaySize + DISPLAY_GROWTH_PER_TICK) : target;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setDeltaMovement(Vec3.ZERO);
        if (!(level() instanceof ServerLevel server) || tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        if (server.getDifficulty() == Difficulty.PEACEFUL) {
            demoteSupport(server);
            discard();
            return;
        }
        if (tickCount % GROWTH_INTERVAL == 0 && getSporeSize() < MAX_SIZE) {
            setSporeSize(getSporeSize() + 1);
        }
        if (!TaintBiomeManager.isTainted(server, blockPosition())) {
            hurtServer(server, server.damageSources().starve(), STARVE_DAMAGE);
            if (isRemoved()) {
                return;
            }
        }
        if (requiresStalkSupport() && !server.getBlockState(blockPosition().below()).is(TTBlocks.TAINT_SPORE_STALK.get())) {
            burst(server);
        }
    }

    public int getSporeSize() {
        return entityData.get(DATA_SPORE_SIZE);
    }

    protected void setSporeSize(int size) {
        entityData.set(DATA_SPORE_SIZE, Mth.clamp(size, MIN_SIZE, MAX_SIZE));
    }

    public float getDisplaySize(float partialTick) {
        if (displaySize < 0.0F || oldDisplaySize < 0.0F) {
            return getSporeSize();
        }
        return Mth.lerp(partialTick, oldDisplaySize, displaySize);
    }

    public int releaseTicks() {
        return releaseTicks;
    }

    protected final void signalRelease(ServerLevel level) {
        level.broadcastEntityEvent(this, EVENT_RELEASE);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_RELEASE) {
            releaseTicks = RELEASE_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (level() instanceof ServerLevel server) {
            burst(server);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel server) {
            burst(server);
        } else {
            super.die(source);
        }
    }

    protected final void burst(ServerLevel level) {
        if (burst) {
            return;
        }
        burst = true;
        onBurst(level);
        demoteSupport(level);
        discard();
    }

    private void demoteSupport(ServerLevel level) {
        BlockPos below = blockPosition().below();
        BlockState support = level.getBlockState(below);
        if (support.is(TTBlocks.TAINT_SPORE_STALK.get()) && support.getValue(BlockTaintSporeStalk.MATURE)) {
            level.setBlock(below, support.setValue(BlockTaintSporeStalk.MATURE, false), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("SporeSize", getSporeSize());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setSporeSize(input.getIntOr("SporeSize", MIN_SIZE));
    }
}
