package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSporeStalk;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * TC4-style Taint Spore restored around the modern Seed/ecology system.
 *
 * <p>The legacy Spore synchronized its growth size to clients and eased the displayed model size toward the new
 * value. Keep that split here so the renderer can reproduce the old slow swelling instead of snapping whenever the
 * 1200-tick growth step fires.</p>
 */
public class EntityTaintSpore extends Monster
        implements ITaintedMob, net.minecraft.world.entity.projectile.ItemSupplier {
    private static final EntityDataAccessor<Integer> DATA_SPORE_SIZE =
            SynchedEntityData.defineId(EntityTaintSpore.class, EntityDataSerializers.INT);
    private static final int GROWTH_INTERVAL = 1200;
    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 10;
    private static final float DISPLAY_GROWTH_PER_TICK = 0.02F;

    private boolean burst;
    private float displaySize = -1.0F;
    private float oldDisplaySize = -1.0F;

    public EntityTaintSpore(EntityType<? extends EntityTaintSpore> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_SPORE_SIZE, MIN_SIZE);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            return;
        }

        float target = getSporeSize();
        if (displaySize < 0.0F) {
            // TC4 sent the current size as spawn data so a newly observed spore did not grow from zero.
            displaySize = target;
            oldDisplaySize = target;
            return;
        }

        oldDisplaySize = displaySize;
        if (displaySize < target) {
            displaySize = Math.min(target, displaySize + DISPLAY_GROWTH_PER_TICK);
        } else if (displaySize > target) {
            displaySize = target;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setDeltaMovement(0.0, 0.0, 0.0);
        if (!(level() instanceof ServerLevel server) || tickCount % 20 != 0) {
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
            // TC4 spores slowly starved outside literal Tainted Lands instead of treating an
            // abstract pollution scalar as their habitat permission.
            hurt(server.damageSources().starve(), 1.0F);
            if (isRemoved()) {
                return;
            }
        }
        BlockState support = server.getBlockState(blockPosition().below());
        if (requiresStalkSupport() && !support.is(TCBlocks.TAINT_SPORE_STALK.get())) {
            burst(server);
        }
    }

    protected boolean requiresStalkSupport() {
        return true;
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

    protected void burst(ServerLevel level) {
        if (burst) return;
        burst = true;
        int sporeSize = getSporeSize();
        if (sporeSize >= 8 && TaintEcology.getSaturation(level, blockPosition()) >= 0.85F && random.nextInt(4) == 0) {
            TaintHelper.trySpawnSatelliteSeed(level, blockPosition().below(), random);
        }
        int count = level.getDifficulty() == Difficulty.PEACEFUL
                ? 0
                : Math.min(6, sporeSize / 3 + random.nextInt(sporeSize / 2 + 1));
        for (int i = 0; i < count; i++) {
            EntityTaintSpider spider = TCEntities.TAINT_SPIDER.get().create(level);
            if (spider != null) {
                spider.moveTo(
                        getX() + random.nextDouble() - 0.5,
                        getY(),
                        getZ() + random.nextDouble() - 0.5,
                        random.nextFloat() * 360.0F,
                        0.0F);
                level.addFreshEntity(spider);
            }
        }
        demoteSupport(level);
        discard();
    }

    protected void demoteSupport(ServerLevel level) {
        BlockState support = level.getBlockState(blockPosition().below());
        BooleanProperty mature = BlockTaintSporeStalk.MATURE;
        if (support.is(TCBlocks.TAINT_SPORE_STALK.get()) && support.getValue(mature)) {
            level.setBlock(blockPosition().below(), support.setValue(mature, false), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TCBlocks.TAINT_FEATURE.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SporeSize", getSporeSize());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSporeSize(tag.contains("SporeSize") ? tag.getInt("SporeSize") : MIN_SIZE);
    }
}
