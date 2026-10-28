package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintFeeding;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityBottleTaint extends ThrowableItemProjectile implements ItemSupplier {
    public static final ThrowProfile THROW = new ThrowProfile(0.66F, 1.0F, -5.0F, 0.5F);

    private static final double SPLASH_RADIUS = 5.0;
    private static final int FLUX_TAINT_TICKS = 100;
    private static final int FIBRE_SEARCH_UP = 2;
    private static final int FIBRE_SEARCH_DOWN = 3;
    private static final int HYBRID_GOO_ATTEMPTS = 3;
    private static final int HYBRID_GOO_REACH = 2;
    private static final int HYBRID_GOO_MAX_EXTRA_QUANTA = 2;
    private static final int SPLOSION_COUNT = 100;
    private static final double SPLOSION_VELOCITY = 1.0;
    private static final int BOTTLE_CRACK_COUNT = 8;
    private static final double CRACK_SPREAD = 0.15;
    private static final double CRACK_LIFT = 0.2;
    private static final float BREAK_VOLUME = 1.0F;
    private static final float BREAK_PITCH_BASE = 0.9F;
    private static final float BREAK_PITCH_SPREAD = 0.1F;
    private static final byte SHATTER_EVENT = 3;

    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
        super(type, level);
    }

    public EntityBottleTaint(Level level, LivingEntity owner, ItemStack stack) {
        super(TTEntities.BOTTLE_TAINT.get(), owner, level);
        setItem(stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TTItems.BOTTLE_TAINT.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == SHATTER_EVENT) {
            for (int a = 0; a < SPLOSION_COUNT; a++) {
                this.level()
                        .addParticle(
                                TTParticles.TAINT_SPLOSION.get(),
                                this.getX(),
                                this.getY() + this.random.nextFloat() * this.getBbHeight(),
                                this.getZ(),
                                signed() * SPLOSION_VELOCITY,
                                signed() * SPLOSION_VELOCITY,
                                signed() * SPLOSION_VELOCITY);
            }
            ItemParticleOption crack =
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(TTItems.BOTTLE_TAINT.get()));
            for (int k = 0; k < BOTTLE_CRACK_COUNT; k++) {
                this.level()
                        .addParticle(
                                crack,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                this.random.nextGaussian() * CRACK_SPREAD,
                                this.random.nextDouble() * CRACK_LIFT,
                                this.random.nextGaussian() * CRACK_SPREAD);
            }
            this.level()
                    .playLocalSound(
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.SPLASH_POTION_BREAK,
                            SoundSource.NEUTRAL,
                            BREAK_VOLUME,
                            this.random.nextFloat() * BREAK_PITCH_SPREAD + BREAK_PITCH_BASE,
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
        Vec3 impact = result.getLocation();
        BlockPos origin = BlockPos.containing(impact);
        applyAreaEffect(server, impact);
        plantSeed(server, origin);
        scatterHybridGoo(server, origin);
        server.broadcastEntityEvent(this, SHATTER_EVENT);
        this.discard();
    }

    private void applyAreaEffect(ServerLevel server, Vec3 impact) {
        AABB box = new AABB(impact, impact).inflate(SPLASH_RADIUS);
        for (LivingEntity target : server.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> !MobTraits.isTainted(e) && !e.getType().is(EntityTypeTags.UNDEAD))) {
            target.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true));
        }
    }

    private void plantSeed(ServerLevel server, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = origin.mutable();
        for (int y = origin.getY() + FIBRE_SEARCH_UP; y >= origin.getY() - FIBRE_SEARCH_DOWN; y--) {
            cursor.setY(y);
            BlockPos below = cursor.below();
            if (server.getBlockState(cursor).isAir()
                    && server.getBlockState(below).isFaceSturdy(server, below, Direction.UP)) {
                TaintHelper.plantSeed(server, cursor.immutable(), TaintFeeding.reserveCap());
                return;
            }
        }
    }

    private void scatterHybridGoo(ServerLevel server, BlockPos origin) {
        for (int attempt = 0; attempt < HYBRID_GOO_ATTEMPTS; attempt++) {
            BlockPos column = origin.offset(gooOffset(), 0, gooOffset());
            int quanta = 1 + this.random.nextInt(HYBRID_GOO_MAX_EXTRA_QUANTA);
            if (server.hasChunkAt(column) && !tryGoo(server, column, quanta)) {
                tryGoo(server, column.below(), quanta);
            }
        }
    }

    private int gooOffset() {
        return this.random.nextInt(HYBRID_GOO_REACH * 2 + 1) - HYBRID_GOO_REACH;
    }

    private static boolean tryGoo(ServerLevel server, BlockPos pos, int quanta) {
        BlockState here = server.getBlockState(pos);
        BlockPos support = pos.below();
        if ((here.isAir() || here.canBeReplaced())
                && server.getBlockState(support).isRedstoneConductor(server, support)) {
            return PhysicalFlux.placeGoo(server, pos, quanta);
        }
        return false;
    }

    private double signed() {
        return this.random.nextDouble() * 2.0 - 1.0;
    }
}
