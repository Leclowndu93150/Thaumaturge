package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityBottleTaint extends ThrowableItemProjectile implements ItemSupplier {
    public static final ThrowProfile THROW = new ThrowProfile(0.66F, 1.0F, -5.0F, 0.5F);

    private static final double EFFECT_REACH = 5.0;
    private static final int EFFECT_TICKS = 100;
    private static final int PLANT_ATTEMPTS = 10;
    private static final float PLANT_CHANCE = 0.5F;
    private static final float PLANT_SPREAD = 5.0F;
    private static final int PLANT_SCAN_ABOVE = 2;
    private static final int PLANT_SCAN_BELOW = 3;
    private static final float PLANT_PRESSURE = 0.03F;
    private static final int GOO_ATTEMPTS = 3;
    private static final int GOO_REACH = 2;
    private static final int GOO_MAX_QUANTITY = 2;
    private static final int SHARD_COUNT = 8;
    private static final double SHARD_SPREAD = 0.15;
    private static final double SHARD_LIFT = 0.2;
    private static final float BREAK_VOLUME = 1.0F;
    private static final float BREAK_PITCH_BASE = 0.9F;
    private static final float BREAK_PITCH_SPREAD = 0.1F;
    private static final byte BURST_SIGNAL = 3;
    private static final int SPRAY_COUNT = 48;

    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
        super(type, level);
    }

    public EntityBottleTaint(Level level, LivingEntity thrower, ItemStack stack) {
        super(TTEntities.BOTTLE_TAINT.get(), thrower, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TTItems.BOTTLE_TAINT.get();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel server) || this.isRemoved()) {
            return;
        }
        Vec3 impact = hit.getLocation();
        BlockPos impactBlock = BlockPos.containing(impact);
        afflictNearby(server, impact);
        if (!ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            sowFibres(server, impactBlock);
        }
        splashGoo(server, impactBlock);
        server.broadcastEntityEvent(this, BURST_SIGNAL);
        float pitch = BREAK_PITCH_BASE + server.getRandom().nextFloat() * BREAK_PITCH_SPREAD;
        server.playSound(null, impact.x, impact.y, impact.z, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, BREAK_VOLUME, pitch);
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != BURST_SIGNAL) {
            super.handleEntityEvent(id);
            return;
        }
        Level level = this.level();
        RandomSource random = this.random;
        for (int i = 0; i < SPRAY_COUNT; i++) {
            level.addParticle(TTParticles.TAINT_SPLOSION.get(), this.getX(), this.getY(), this.getZ(), signed(random), signed(random), signed(random));
        }
        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, Items.GLASS_BOTTLE);
        for (int i = 0; i < SHARD_COUNT; i++) {
            level.addParticle(shard, this.getX(), this.getY(), this.getZ(), signed(random) * SHARD_SPREAD, SHARD_LIFT * random.nextDouble(), signed(random) * SHARD_SPREAD);
        }
    }

    private void afflictNearby(ServerLevel level, Vec3 impact) {
        AABB reach = new AABB(impact, impact).inflate(EFFECT_REACH);
        for (LivingEntity creature : level.getEntitiesOfClass(LivingEntity.class, reach, EntityBottleTaint::canAfflict)) {
            creature.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, EFFECT_TICKS, 0));
        }
    }

    private void sowFibres(ServerLevel level, BlockPos impactBlock) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < PLANT_ATTEMPTS; attempt++) {
            BlockPos column = impactBlock.offset(plantOffset(random), 0, plantOffset(random));
            if (random.nextFloat() >= PLANT_CHANCE || !level.hasChunkAt(column) || TaintBlooms.isProtected(level, column)) {
                continue;
            }
            if (TaintBiomeManager.isTainted(level, column) || TaintBiomeManager.taintColumn(level, column)) {
                plantInColumn(level, column);
            }
        }
    }

    private void splashGoo(ServerLevel level, BlockPos impactBlock) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < GOO_ATTEMPTS; attempt++) {
            BlockPos column = impactBlock.offset(random.nextInt(GOO_REACH * 2 + 1) - GOO_REACH, 0, random.nextInt(GOO_REACH * 2 + 1) - GOO_REACH);
            int amount = 1 + random.nextInt(GOO_MAX_QUANTITY);
            if (level.hasChunkAt(column) && !depositGoo(level, column, amount)) {
                depositGoo(level, column.below(), amount);
            }
        }
    }

    private static boolean depositGoo(ServerLevel level, BlockPos pos, int amount) {
        BlockPos floor = pos.below();
        if (!level.getBlockState(pos).canBeReplaced() || !level.getBlockState(floor).isCollisionShapeFullBlock(level, floor)) {
            return false;
        }
        return PhysicalFlux.placeGoo(level, pos, amount);
    }

    private void plantInColumn(ServerLevel level, BlockPos column) {
        for (int y = column.getY() + PLANT_SCAN_ABOVE; y >= column.getY() - PLANT_SCAN_BELOW; y--) {
            BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
            BlockState state = level.getBlockState(pos);
            if ((state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, pos)) {
                level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
                TaintApi.addEcologicalPressure(level, pos, PLANT_PRESSURE);
                return;
            }
        }
    }

    private static boolean canAfflict(LivingEntity creature) {
        return !MobTraits.isTainted(creature) && !creature.is(EntityTypeTags.UNDEAD);
    }

    private static int plantOffset(RandomSource random) {
        return (int) ((random.nextFloat() - random.nextFloat()) * PLANT_SPREAD);
    }

    private static double signed(RandomSource random) {
        return random.nextDouble() * 2.0 - 1.0;
    }
}
