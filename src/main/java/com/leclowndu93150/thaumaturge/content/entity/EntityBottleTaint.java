package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
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
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class EntityBottleTaint extends ThrowableItemProjectile implements ItemSupplier {
    public static final ThrowProfile THROW = new ThrowProfile(0.66F, 1.0F, -5.0F, 0.5F);

    private static final double SPLASH_RADIUS = 5.0;
    private static final int FLUX_TAINT_TICKS = 100;
    private static final int TAINT_ATTEMPTS = 10;
    private static final float TAINT_SPREAD_RADIUS = 5.0F;
    private static final float TAINT_PRESSURE = 0.03F;
    private static final int FIBRE_SEARCH_UP = 2;
    private static final int FIBRE_SEARCH_DOWN = 3;
    private static final int HYBRID_GOO_ATTEMPTS = 3;
    private static final int HYBRID_GOO_RANGE = 5;
    private static final int HYBRID_GOO_MAX_EXTRA_QUANTA = 2;
    private static final int SPLOSION_COUNT = 100;
    private static final int BOTTLE_CRACK_COUNT = 8;

    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
        super(type, level);
    }

    public EntityBottleTaint(Level level, LivingEntity owner, ItemStack stack) {
        super(TCEntities.BOTTLE_TAINT.get(), owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TCItems.BOTTLE_TAINT.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int a = 0; a < SPLOSION_COUNT; a++) {
                this.level().addParticle(TCParticles.TAINT_SPLOSION.get(), this.getX(), this.getY() + this.random.nextFloat() * this.getBbHeight(), this.getZ(), this.random.nextDouble() * 2.0 - 1.0,
                        this.random.nextDouble() * 2.0 - 1.0, this.random.nextDouble() * 2.0 - 1.0);
            }
            ItemParticleOption crack = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(new ItemStack(TCItems.BOTTLE_TAINT.get())));
            for (int k = 0; k < BOTTLE_CRACK_COUNT; k++) {
                this.level().addParticle(crack, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.15, this.random.nextDouble() * 0.2, this.random.nextGaussian() * 0.15);
            }
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, this.random.nextFloat() * 0.1F + 0.9F, false);
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
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, box, e -> !MobTraits.isTainted(e) && !e.is(EntityTypeTags.UNDEAD))) {
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
            if (!server.hasChunkAt(column) || TaintBlooms.isProtected(server, column)) {
                continue;
            }
            if (!TaintBiomeManager.isTainted(server, column) && !TaintBiomeManager.taintColumn(server, column)) {
                continue;
            }
            BlockPos fibrePos = findFibrePosition(server, column);
            if (fibrePos != null) {
                server.setBlock(fibrePos, BlockTaintFibre.stateForWorld(server, fibrePos), Block.UPDATE_ALL);
                TaintEcology.addPressure(server, fibrePos, TAINT_PRESSURE);
            }
        }
    }

    private void scatterHybridGoo(ServerLevel server) {
        BlockPos center = this.blockPosition();
        for (int attempt = 0; attempt < HYBRID_GOO_ATTEMPTS; attempt++) {
            BlockPos target = center.offset(this.random.nextInt(HYBRID_GOO_RANGE) - HYBRID_GOO_RANGE / 2, 0, this.random.nextInt(HYBRID_GOO_RANGE) - HYBRID_GOO_RANGE / 2);
            int quanta = 1 + this.random.nextInt(HYBRID_GOO_MAX_EXTRA_QUANTA);
            if (canHostGoo(server, target)) {
                PhysicalFlux.placeGoo(server, target, quanta);
            } else if (canHostGoo(server, target.below())) {
                PhysicalFlux.placeGoo(server, target.below(), quanta);
            }
        }
    }

    private static @Nullable BlockPos findFibrePosition(ServerLevel server, BlockPos column) {
        BlockPos.MutableBlockPos cursor = column.mutable();
        for (int dy = FIBRE_SEARCH_UP; dy >= -FIBRE_SEARCH_DOWN; dy--) {
            cursor.set(column.getX(), column.getY() + dy, column.getZ());
            BlockState here = server.getBlockState(cursor);
            if ((here.isAir() || here.canBeReplaced()) && here.getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(server, cursor)) {
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
