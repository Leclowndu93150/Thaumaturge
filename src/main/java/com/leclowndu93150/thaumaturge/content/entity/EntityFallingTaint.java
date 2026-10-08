package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public final class EntityFallingTaint extends Entity implements IEntityWithComplexSpawn, ISidedHurt {

    private static final int MAX_HANG_TIME = 100;
    private static final int MAX_FALL_TIME = 600;
    private static final float MOTION_DAMP = 0.98F;
    private static final float LANDING_DAMP_HORIZONTAL = 0.7F;
    private static final float LANDING_DAMP_VERTICAL = -0.5F;

    private BlockState fallTile = TTBlocks.TAINT_CRUST.get().defaultBlockState();
    private BlockPos originPos = BlockPos.ZERO;
    private int fallTime;

    public EntityFallingTaint(EntityType<? extends EntityFallingTaint> type, Level level) {
        super(type, level);
    }

    public EntityFallingTaint(Level level, double x, double y, double z, BlockState state, BlockPos origin) {
        this(TTEntities.FALLING_TAINT.get(), level);
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

    public BlockPos origin() {
        return originPos;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {}

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
        boolean overGoo = below.is(TTBlocks.FLUX_GOO.get());

        if (!this.onGround() && !overGoo) {
            if (fallTime > MAX_HANG_TIME
                    && (here.getY() < server.getMinBuildHeight() || here.getY() >= server.getMaxBuildHeight())) {
                this.discard();
                return;
            }
            if (fallTime > MAX_FALL_TIME) {
                this.discard();
                return;
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement()
                    .multiply(LANDING_DAMP_HORIZONTAL, LANDING_DAMP_VERTICAL, LANDING_DAMP_HORIZONTAL));
            server.playSound(null, here, TTSounds.GORE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            this.discard();
            BlockState landingState = server.getBlockState(here);
            if (landingState.canBeReplaced() || landingState.isAir() || landingState.is(TTBlocks.FLUX_GOO.get())) {
                server.setBlock(here, fallTile, Block.UPDATE_ALL);
            }
            return;
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(MOTION_DAMP));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        TTNbt.store(output, "Block", BlockState.CODEC, registryAccess(), fallTile);
        output.putLong("Origin", originPos.asLong());
        output.putInt("Time", fallTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        int id = (input.contains("BlockId") ? input.getInt("BlockId") : Block.getId(fallTile));
        BlockState resolved = Block.stateById(id);
        if (!resolved.isAir()) {
            fallTile = resolved;
        }
        TTNbt.read(input, "Block", BlockState.CODEC, registryAccess())
                .filter(state -> !state.isAir())
                .ifPresent(state -> fallTile = state);
        originPos = BlockPos.of(input.getLong("Origin"));
        fallTime = input.getInt("Time");
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
    public boolean hurt(DamageSource source, float damage) {
        return hurtSided(level(), source, damage);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean hurtClient(DamageSource source, float damage) {
        return false;
    }
}
