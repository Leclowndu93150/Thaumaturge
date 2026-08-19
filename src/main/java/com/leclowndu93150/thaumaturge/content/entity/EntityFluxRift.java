package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityFluxRift extends Entity {
    private static final EntityDataAccessor<Integer> DATA_SEED = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_STABILITY = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_COLLAPSE = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.BOOLEAN);

    public static final int MAX_RIFT_SIZE = 100;
    private static final float MAX_STABILITY = 100.0F;
    private static final int STABILITY_DECAY_INTERVAL = 120;
    private static final float STABILITY_DECAY = 0.2F;
    private static final int GROWTH_INTERVAL = 600;
    private static final int AMBIENT_SOUND_INTERVAL = 300;
    private static final float SEGMENT_DAMAGE = 2.0F;
    private static final double MIN_SPAWN_SIZE = 5.0;
    private static final double RIFT_EXCLUSION_RANGE = 32.0;
    private static final double COLLAPSE_EFFECT_RANGE = 32.0;
    private static final int EVENT_WISP = 0;
    private static final int EVENT_TAINT_SEED = 1;
    private static final int EVENT_FLUX_PHAGE = 2;
    private static final int EVENT_COLLAPSE = 4;
    private static final int[][] EVENT_TABLE = {{EVENT_WISP, 50, 5, 1}, {EVENT_TAINT_SEED, 10, 0, 0}, {EVENT_FLUX_PHAGE, 20, 10, 1}, {EVENT_COLLAPSE, 1, 0, 1}};

    private int maxSize;
    private int lastSize = -1;
    public final List<Vec3> points = new ArrayList<>();
    public final List<Float> pointsWidth = new ArrayList<>();

    public EntityFluxRift(EntityType<? extends EntityFluxRift> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_SEED, 0);
        entityData.define(DATA_SIZE, 5);
        entityData.define(DATA_STABILITY, 0.0F);
        entityData.define(DATA_COLLAPSE, false);
    }

    public boolean getCollapse() {
        return this.entityData.get(DATA_COLLAPSE);
    }

    public void setCollapse(boolean collapse) {
        if (collapse) {
            this.maxSize = getRiftSize();
        }
        this.entityData.set(DATA_COLLAPSE, collapse);
    }

    public float getRiftStability() {
        return this.entityData.get(DATA_STABILITY);
    }

    public void setRiftStability(float stability) {
        this.entityData.set(DATA_STABILITY, Mth.clamp(stability, -MAX_STABILITY, MAX_STABILITY));
    }

    public void addStability() {
        setRiftStability(getRiftStability() + 0.125F);
    }

    public int getRiftSize() {
        return this.entityData.get(DATA_SIZE);
    }

    public void setRiftSize(int size) {
        this.entityData.set(DATA_SIZE, size);
        recalcShape();
    }

    public int getRiftSeed() {
        return this.entityData.get(DATA_SEED);
    }

    public void setRiftSeed(int seed) {
        this.entityData.set(DATA_SEED, seed);
    }

    public Stability getStability() {
        float stability = getRiftStability();
        if (stability > 50.0F) {
            return Stability.VERY_STABLE;
        }
        if (stability >= 0.0F) {
            return Stability.STABLE;
        }
        return stability > -25.0F ? Stability.UNSTABLE : Stability.VERY_UNSTABLE;
    }

    private void recalcShape() {
        calcSteps(points, pointsWidth, RandomSource.create(getRiftSeed()));
        lastSize = getRiftSize();
        setBoundingBox(makeBoundingBox());
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        if (points == null || points.isEmpty()) {
            return super.makeBoundingBox(pos);
        }
        double x0 = Double.MAX_VALUE;
        double y0 = Double.MAX_VALUE;
        double z0 = Double.MAX_VALUE;
        double x1 = -Double.MAX_VALUE;
        double y1 = -Double.MAX_VALUE;
        double z1 = -Double.MAX_VALUE;
        for (Vec3 v : points) {
            x0 = Math.min(x0, v.x);
            y0 = Math.min(y0, v.y);
            z0 = Math.min(z0, v.z);
            x1 = Math.max(x1, v.x);
            y1 = Math.max(y1, v.y);
            z1 = Math.max(z1, v.z);
        }
        return new AABB(pos.x + x0, pos.y + y0, pos.z + z0, pos.x + x1, pos.y + y1, pos.z + z1);
    }

    @Override
    public void move(MoverType type, Vec3 movement) {}

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (lastSize != getRiftSize()) {
            recalcShape();
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
        }
    }

    private void serverTick(ServerLevel level) {
        RandomSource rand = this.random;
        if (getRiftSeed() == 0) {
            setRiftSeed(rand.nextInt());
        }
        if (points.size() > 1) {
            int pi = rand.nextInt(points.size() - 1);
            Vec3 v1 = points.get(pi).add(getX(), getY(), getZ());
            Vec3 v2 = points.get(pi + 1).add(getX(), getY(), getZ());
            eatBlocks(level, v1, v2);
            hurtTouching(level, v1);
        }
        if (points.size() < 3 && !getCollapse()) {
            setCollapse(true);
        }
        if (getCollapse()) {
            setRiftSize(getRiftSize() - 1);
            if (rand.nextBoolean()) {
                AuraHelper.addVis(level, blockPosition(), 1.0F);
            } else {
                AuraHelper.polluteAura(level, blockPosition(), 1.0F, false);
            }
            if (rand.nextInt(10) == 0) {
                level.explode(this, getX() + rand.nextGaussian() * 2.0, getY() + rand.nextGaussian() * 2.0, getZ() + rand.nextGaussian() * 2.0, rand.nextFloat() / 2.0F,
                        Level.ExplosionInteraction.NONE);
            }
            if (getRiftSize() <= 1) {
                completeCollapse(level);
                return;
            }
        }
        if (this.tickCount % STABILITY_DECAY_INTERVAL == 0) {
            setRiftStability(getRiftStability() - STABILITY_DECAY);
        }
        if (this.tickCount % GROWTH_INTERVAL == getId() % GROWTH_INTERVAL) {
            float flux = AuraHelper.getFlux(level, blockPosition());
            double growthCost = Math.sqrt(getRiftSize() * 2);
            if (flux >= growthCost && getRiftSize() < MAX_RIFT_SIZE && getStability() != Stability.VERY_STABLE) {
                AuraHelper.drainFlux(level, blockPosition(), (float) growthCost, false);
                setRiftSize(getRiftSize() + 1);
            }
            if (getRiftStability() < 0.0F && rand.nextInt(1000) < Math.abs(getRiftStability()) + getRiftSize()) {
                executeRiftEvent(level);
            }
        }
        if (!isRemoved() && this.tickCount % AMBIENT_SOUND_INTERVAL == 0) {
            level.playSound(null, getX(), getY(), getZ(), TCSounds.EVILPORTAL.get(), SoundSource.AMBIENT, (float) (0.15F + rand.nextGaussian() * 0.066), (float) (0.75 + rand.nextGaussian() * 0.1));
        }
    }

    private void eatBlocks(ServerLevel level, Vec3 v1, Vec3 v2) {
        BlockHitResult hit = level.clip(new ClipContext(v1, v2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0.0F) {
            level.destroyBlock(pos, false);
        }
    }

    private void hurtTouching(ServerLevel level, Vec3 point) {
        AABB box = new AABB(point.x, point.y, point.z, point.x, point.y, point.z).inflate(0.5);
        for (Entity e : level.getEntities(this, box)) {
            if (e.isRemoved() || (e instanceof Player player && player.isCreative())) {
                continue;
            }
            e.hurt(level.damageSources().fellOutOfWorld(), SEGMENT_DAMAGE);
            if (e instanceof ItemEntity) {
                e.discard();
            }
        }
    }

    private void executeRiftEvent(ServerLevel level) {
        int totalWeight = 0;
        for (int[] entry : EVENT_TABLE) {
            totalWeight += entry[1];
        }
        int roll = this.random.nextInt(totalWeight);
        int[] chosen = null;
        for (int[] entry : EVENT_TABLE) {
            roll -= entry[1];
            if (roll < 0) {
                chosen = entry;
                break;
            }
        }
        if (chosen == null) {
            return;
        }
        boolean nearTaintAllowed = chosen[3] == 1;
        if (!nearTaintAllowed && TaintApi.isNearTaintSeed(level, blockPosition())) {
            return;
        }
        boolean didit = switch (chosen[0]) {
            case EVENT_WISP -> spawnWisp(level);
            case EVENT_TAINT_SEED -> spawnTaintSeed(level);
            case EVENT_FLUX_PHAGE -> inflictFluxPhage(level);
            case EVENT_COLLAPSE -> {
                setCollapse(true);
                yield false;
            }
            default -> false;
        };
        if (didit) {
            setRiftStability(getRiftStability() + chosen[2]);
        }
    }

    private boolean spawnWisp(ServerLevel level) {
        WispEntity wisp = TCEntities.WISP.get().create(level, EntitySpawnReason.EVENT);
        if (wisp == null) {
            return false;
        }
        wisp.snapTo(getX() + this.random.nextGaussian() * 5.0, getY() + this.random.nextGaussian() * 5.0, getZ() + this.random.nextGaussian() * 5.0, 0.0F, 0.0F);
        if (this.random.nextInt(5) == 0) {
            wisp.setAspect(TCAspects.VITIUM.identifier());
        }
        if (level.noCollision(wisp)) {
            return level.addFreshEntity(wisp);
        }
        wisp.discard();
        return false;
    }

    private boolean spawnTaintSeed(ServerLevel level) {
        EntityTaintSeedPrime seed = TCEntities.TAINT_SEED_PRIME.get().create(level, EntitySpawnReason.EVENT);
        if (seed == null) {
            return false;
        }
        seed.snapTo((int) (getX() + this.random.nextGaussian() * 5.0) + 0.5, (int) (getY() + this.random.nextGaussian() * 5.0), (int) (getZ() + this.random.nextGaussian() * 5.0) + 0.5,
                this.random.nextInt(360), 0.0F);
        if (level.noCollision(seed) && level.addFreshEntity(seed)) {
            AuraHelper.polluteAura(level, blockPosition(), getRiftSize() / 2.0F, true);
            discard();
            return true;
        }
        seed.discard();
        return false;
    }

    private boolean inflictFluxPhage(ServerLevel level) {
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16.0));
        boolean didit = false;
        for (LivingEntity target : targets) {
            didit = true;
            if (target instanceof ServerPlayer player) {
                WarpManager.sendActionBar(player, "warp.thaumaturge.fluxevent.2");
            }
            target.addEffect(new MobEffectInstance(TCMobEffects.INFECTIOUS_VIS_EXHAUST, 3000, 2));
        }
        return didit;
    }

    private void completeCollapse(ServerLevel level) {
        int strength = (int) Math.sqrt(maxSize);
        if (this.random.nextInt(100) < strength) {
            ItemStack pearl = new ItemStack(TCItems.PRIMORDIAL_PEARL.get());
            pearl.setDamageValue(4 + this.random.nextInt(4));
            spawnAtLocation(level, pearl, 0.0F);
        }
        for (int a = 0; a < strength; a++) {
            spawnAtLocation(level, new ItemStack(TCItems.VOID_SEED.get()), 0.0F);
        }
        level.explode(this, getX(), getY(), getZ(), 0.0F, Level.ExplosionInteraction.NONE);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(COLLAPSE_EFFECT_RANGE));
        Stability stability = getStability();
        if (stability != Stability.VERY_STABLE) {
            if (stability == Stability.VERY_UNSTABLE) {
                for (LivingEntity target : nearby) {
                    int w = (int) ((1.0 - distanceToSqr(target) / (COLLAPSE_EFFECT_RANGE * COLLAPSE_EFFECT_RANGE)) * 120.0);
                    if (w > 0) {
                        target.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, w * 20, 0));
                    }
                }
            }
            if (stability == Stability.VERY_UNSTABLE || stability == Stability.UNSTABLE) {
                for (LivingEntity target : nearby) {
                    int w = (int) ((1.0 - distanceToSqr(target) / (COLLAPSE_EFFECT_RANGE * COLLAPSE_EFFECT_RANGE)) * 300.0);
                    if (w > 0) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, w * 20, 0));
                    }
                }
            }
            for (LivingEntity target : nearby) {
                if (target instanceof ServerPlayer player) {
                    int w = (int) ((1.0 - distanceToSqr(target) / (COLLAPSE_EFFECT_RANGE * COLLAPSE_EFFECT_RANGE)) * 25.0);
                    if (w > 0) {
                        WarpHelper.addWarp(player, w, WarpType.NORMAL);
                        WarpHelper.addWarp(player, w, WarpType.TEMPORARY);
                    }
                }
            }
        }
        discard();
    }

    private static final double WANDER_ANGLE = 0.33;
    private static final double STEP_LENGTH = 0.2;
    private static final double TIP_LENGTH = 0.1;
    private static final float GIRTH_PER_SIZE = 1.0F / 300.0F;
    private static final float SIZE_PER_STEP = 3.0F;

    private void calcSteps(List<Vec3> outPoints, List<Float> outWidths, RandomSource rand) {
        outPoints.clear();
        outWidths.clear();
        Vec3 heading = new Vec3(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian()).normalize();
        Arm right = new Arm(heading);
        Arm left = new Arm(heading.scale(-1.0));
        int steps = Mth.ceil(getRiftSize() / SIZE_PER_STEP);
        float girth = getRiftSize() * GIRTH_PER_SIZE;
        float taper = girth / steps;
        for (int a = 0; a < steps; a++) {
            girth -= taper;
            right.wander(rand);
            outPoints.add(right.advance(STEP_LENGTH));
            outWidths.add(girth);
            left.wander(rand);
            outPoints.add(0, left.advance(STEP_LENGTH));
            outWidths.add(0, girth);
        }
        outPoints.add(right.advance(TIP_LENGTH));
        outWidths.add(0.0F);
        outPoints.add(0, left.advance(TIP_LENGTH));
        outWidths.add(0, 0.0F);
    }

    private static final class Arm {
        private Vec3 heading;
        private Vec3 tip = Vec3.ZERO;

        Arm(Vec3 heading) {
            this.heading = heading;
        }

        void wander(RandomSource rand) {
            heading = heading.xRot((float) (rand.nextGaussian() * WANDER_ANGLE)).yRot((float) (rand.nextGaussian() * WANDER_ANGLE));
        }

        Vec3 advance(double distance) {
            tip = tip.add(heading.scale(distance));
            return tip;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("MaxSize", maxSize);
        output.putInt("RiftSize", getRiftSize());
        output.putInt("RiftSeed", getRiftSeed());
        output.putFloat("Stability", getRiftStability());
        output.putBoolean("collapse", getCollapse());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        maxSize = input.getIntOr("MaxSize", 0);
        setRiftSize(input.getIntOr("RiftSize", 5));
        setRiftSeed(input.getIntOr("RiftSeed", 0));
        setRiftStability(input.getIntOr("Stability", 0));
        setCollapse(input.getBooleanOr("collapse", false));
    }

    public static void createRift(ServerLevel level, BlockPos pos) {
        RandomSource rand = level.getRandom();
        pos = pos.offset(rand.nextInt(16), 0, rand.nextInt(16));
        BlockPos target = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        if (!level.dimensionType().hasSkyLight()) {
            target = new BlockPos(target.getX(), 10, target.getZ());
            while (!level.isEmptyBlock(target)) {
                if (target.getY() > level.getMaxY() - 5) {
                    return;
                }
                target = target.above(rand.nextInt(5) + 1);
            }
        }
        if (target.getY() >= level.getMaxY() - 4) {
            return;
        }
        AABB exclusion = new AABB(target).inflate(RIFT_EXCLUSION_RANGE);
        if (!level.getEntitiesOfClass(EntityFluxRift.class, exclusion).isEmpty()) {
            return;
        }
        EntityFluxRift rift = TCEntities.FLUX_RIFT.get().create(level, EntitySpawnReason.EVENT);
        if (rift == null) {
            return;
        }
        rift.setRiftSeed(rand.nextInt());
        rift.snapTo(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, rand.nextInt(360), 0.0F);
        float flux = AuraHelper.getFlux(level, target);
        double size = Math.sqrt(flux * 3.0F);
        if (size > MIN_SPAWN_SIZE && level.addFreshEntity(rift)) {
            rift.setRiftSize((int) size);
            AuraHelper.drainFlux(level, target, (float) size, false);
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, exclusion)) {
                WarpManager.sendActionBar(player, "warp.thaumaturge.fluxevent.3");
            }
        }
    }

    public enum Stability {
        VERY_STABLE, STABLE, UNSTABLE, VERY_UNSTABLE
    }
}
