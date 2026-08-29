package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public final class EntityBottleTaint extends ThrowableItemProjectile implements ItemSupplier {
    private static final double SPLASH_RADIUS = 5.0;
    private static final int FLUX_TAINT_TICKS = 100;
    private static final int TAINT_ATTEMPTS = 10;
    private static final float TAINT_SPREAD_RADIUS = 5.0F;
    private static final int HYBRID_GOO_ATTEMPTS = 3;
    private static final int SPLOSION_COUNT = 100;
    private static final int BOTTLE_CRACK_COUNT = 8;

    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
        super(type, level);
    }

    public EntityBottleTaint(Level level, LivingEntity owner, ItemStack stack) {
        super(TCEntities.BOTTLE_TAINT.get(), owner, level);
        this.setItem(stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TCItems.BOTTLE_TAINT.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int a = 0; a < SPLOSION_COUNT; a++) {
                this.level()
                        .addParticle(
                                TCParticles.TAINT_SPLOSION.get(),
                                this.getX(),
                                this.getY() + this.random.nextFloat() * this.getBbHeight(),
                                this.getZ(),
                                this.random.nextDouble() * 2.0 - 1.0,
                                this.random.nextDouble() * 2.0 - 1.0,
                                this.random.nextDouble() * 2.0 - 1.0);
            }
            ItemParticleOption crack =
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(TCItems.BOTTLE_TAINT.get()));
            for (int k = 0; k < BOTTLE_CRACK_COUNT; k++) {
                this.level()
                        .addParticle(
                                crack,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                this.random.nextGaussian() * 0.15,
                                this.random.nextDouble() * 0.2,
                                this.random.nextGaussian() * 0.15);
            }
            this.level()
                    .playLocalSound(
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.SPLASH_POTION_BREAK,
                            SoundSource.NEUTRAL,
                            1.0F,
                            this.random.nextFloat() * 0.1F + 0.9F,
                            false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        applyAreaEffect(server);
        seedTaint(server);
        scatterHybridGoo(server);
        server.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    private void applyAreaEffect(ServerLevel server) {
        AABB box = new AABB(this.position(), this.position()).inflate(SPLASH_RADIUS);
        for (LivingEntity target : server.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> !(e instanceof ITaintedMob) && !e.getType().is(EntityTypeTags.UNDEAD))) {
            target.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true));
        }
    }

    private void seedTaint(ServerLevel server) {
        BlockPos center = this.blockPosition();
        for (int attempt = 0; attempt < TAINT_ATTEMPTS; attempt++) {
            if (!this.random.nextBoolean()) {
                continue;
            }
            int xx = (int) ((this.random.nextFloat() - this.random.nextFloat()) * TAINT_SPREAD_RADIUS);
            int zz = (int) ((this.random.nextFloat() - this.random.nextFloat()) * TAINT_SPREAD_RADIUS);
            BlockPos column = center.offset(xx, 0, zz);
            if (!server.hasChunkAt(column)) {
                continue;
            }
            TaintBiomeManager.taintColumn(server, column);
            BlockPos fibrePos = findFibrePosition(server, column);
            if (fibrePos != null) {
                server.setBlock(fibrePos, BlockTaintFibre.stateForWorld(server, fibrePos), Block.UPDATE_ALL);
                TaintEcology.addPressure(server, fibrePos, 0.03F);
            }
        }
    }

    private void scatterHybridGoo(ServerLevel server) {
        // Later-era physical-pollution flourish: keep a few small Goo spatters around the direct
        // TC4 tainting effect, without replacing the bottle's primary Tainted Lands behavior.
        BlockPos center = this.blockPosition();
        for (int attempt = 0; attempt < HYBRID_GOO_ATTEMPTS; attempt++) {
            int xx = this.random.nextInt(5) - 2;
            int zz = this.random.nextInt(5) - 2;
            BlockPos target = center.offset(xx, 0, zz);
            if (canHostGoo(server, target)) {
                PhysicalFlux.placeGoo(server, target, 1 + this.random.nextInt(2));
            } else if (canHostGoo(server, target.below())) {
                PhysicalFlux.placeGoo(server, target.below(), 1 + this.random.nextInt(2));
            }
        }
    }

    private static BlockPos findFibrePosition(ServerLevel server, BlockPos column) {
        BlockPos.MutableBlockPos cursor = column.mutable();
        for (int dy = 2; dy >= -3; dy--) {
            cursor.set(column.getX(), column.getY() + dy, column.getZ());
            BlockState here = server.getBlockState(cursor);
            if ((here.isAir() || here.canBeReplaced()) && BlockTaintFibre.hasSolidAttachment(server, cursor)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean canHostGoo(ServerLevel server, BlockPos pos) {
        BlockState below = server.getBlockState(pos.below());
        BlockState here = server.getBlockState(pos);
        return below.isRedstoneConductor(server, pos.below()) && (here.isAir() || here.canBeReplaced());
    }
}
