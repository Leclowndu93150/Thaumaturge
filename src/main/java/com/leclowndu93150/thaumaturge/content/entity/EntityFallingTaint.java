package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public final class EntityFallingTaint extends Entity implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Integer> SOURCE_BLOCK_ID = SynchedEntityData.defineId(EntityFallingTaint.class, EntityDataSerializers.INT);

    private static final int MAX_HANG_TIME = 100;
    private static final int MAX_FALL_TIME = 600;
    private static final float MOTION_DAMP = 0.98F;
    private static final float LANDING_DAMP_HORIZONTAL = 0.7F;
    private static final float LANDING_DAMP_VERTICAL = -0.5F;

    private BlockState fallTile = TCBlocks.TAINT_CRUST.get().defaultBlockState();
    private BlockPos originPos = BlockPos.ZERO;
    private int fallTime;

    public EntityFallingTaint(EntityType<? extends EntityFallingTaint> type, Level level) {
        super(type, level);
    }

    public EntityFallingTaint(Level level, double x, double y, double z, BlockState state, BlockPos origin) {
        this(TCEntities.FALLING_TAINT.get(), level);
        this.fallTile = state;
        this.originPos = origin.immutable();
        this.setPos(x, y, z);
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public BlockState getFallTile() {
        return fallTile;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(SOURCE_BLOCK_ID, Block.getId(fallTile));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.fallTile.isAir()) {
            this.discard();
            return;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        BlockPos here = this.blockPosition();

        if (fallTime == 0) {
            BlockState atOrigin = server.getBlockState(originPos);
            if (!atOrigin.is(fallTile.getBlock())) {
                this.discard();
                return;
            }
            server.setBlock(originPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        fallTime++;

        BlockState below = server.getBlockState(here.below());
        boolean overGoo = below.is(TCBlocks.FLUX_GOO.get());

        if (!this.onGround() && !overGoo) {
            if (fallTime > MAX_HANG_TIME && (here.getY() < 1 || here.getY() > 256)) {
                this.discard();
                return;
            }
            if (fallTime > MAX_FALL_TIME) {
                this.discard();
                return;
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(LANDING_DAMP_HORIZONTAL, LANDING_DAMP_VERTICAL, LANDING_DAMP_HORIZONTAL));
            server.playSound(null, here, TCSounds.GORE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            this.discard();
            BlockState landingState = server.getBlockState(here);
            if (landingState.canBeReplaced() || landingState.isAir() || landingState.is(TCBlocks.FLUX_GOO.get())) {
                server.setBlock(here, fallTile, Block.UPDATE_ALL);
            }
            return;
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(MOTION_DAMP));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("BlockId", Block.getId(fallTile));
        output.putLong("Origin", originPos.asLong());
        output.putInt("Time", fallTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        int id = input.getIntOr("BlockId", Block.getId(fallTile));
        BlockState resolved = Block.stateById(id);
        if (!resolved.isAir()) {
            fallTile = resolved;
        }
        originPos = BlockPos.of(input.getLongOr("Origin", 0L));
        fallTime = input.getIntOr("Time", 0);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(Block.getId(fallTile));
        buf.writeBlockPos(originPos);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        fallTile = Block.stateById(buf.readVarInt());
        originPos = buf.readBlockPos();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }
}
