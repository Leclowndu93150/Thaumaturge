package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.EntityHitResult;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractHierophantSpell extends Entity {
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(AbstractHierophantSpell.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> CASTER_YAW = SynchedEntityData.defineId(AbstractHierophantSpell.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3fc> DIRECTION = SynchedEntityData.defineId(AbstractHierophantSpell.class, EntityDataSerializers.VECTOR3);
    private static final double RENDER_DISTANCE_SQUARED = 9216;
    private static final double OWNER_RANGE_SQUARED = 4096.0;

    private final Set<UUID> hit = new HashSet<>();
    private @Nullable EntityEldritchHierophant owner;
    private @Nullable SpellContinuation continuation;

    protected AbstractHierophantSpell(EntityType<? extends AbstractHierophantSpell> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public final void cast(EntityEldritchHierophant caster, SpellContinuation continuation, Vec3 position, Vec3 direction, boolean left) {
        owner = caster;
        entityData.set(CASTER_YAW, caster.getYRot());
        this.continuation = continuation;
        setLeft(left);
        entityData.set(DIRECTION, direction.toVector3f());
        final float yaw = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90;
        snapTo(position.x, position.y, position.z, yaw, 0);
        entityData.set(START, level().getGameTime());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(START, 0L);
        builder.define(CASTER_YAW, 0.0F);
        builder.define(DIRECTION, new Vector3f(0, 0, 1));
    }

    public final float age(float partialTick) {
        return Math.max(0, level().getGameTime() - entityData.get(START) + partialTick);
    }
    public final float casterYaw() {
        return entityData.get(CASTER_YAW);
    }
    public final Vec3 forward() {
        return new Vec3(entityData.get(DIRECTION));
    }
    public void setLeft(boolean left) {}
    public boolean isLeft() {
        return false;
    }
    @Override
    public final boolean shouldRenderAtSqrDistance(double distance) {
        return distance < RENDER_DISTANCE_SQUARED;
    }
    @Override
    public final boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    public abstract int lifetime();
    protected abstract void tickSpell(ServerLevel level, int age);

    @Override
    public final void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        final int age = (int) age(0);
        if (owner == null || !owner.isAlive() || owner.isRemoved() || distanceToSqr(owner) > OWNER_RANGE_SQUARED || age >= lifetime()) {
            discard();
            return;
        }
        tickSpell(server, age);
    }

    protected final boolean resolveTargets(ServerLevel level, AABB bounds, Vec3 origin) {
        if (owner == null || continuation == null) {
            return false;
        }
        boolean struck = false;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, this::canHit)) {
            if (!intersects(target, (int) age(0))) {
                continue;
            }
            final Vec3 aim = target.getBoundingBox().getCenter();
            if (level.clip(new ClipContext(origin, aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) {
                continue;
            }
            hit.add(target.getUUID());
            struck = true;
            Spells.resume(level, continuation, List.of(new SpellTarget(new EntityHitResult(target, aim), aim, aim.subtract(origin).normalize())));
        }
        return struck;
    }

    protected boolean intersects(LivingEntity target, int age) {
        return true;
    }

    private boolean canHit(LivingEntity target) {
        return owner != null && target.isAlive() && target != owner && !target.isSpectator() && !owner.considersEntityAsAlly(target)
                && !(target instanceof Player player && player.getAbilities().instabuild) && !hit.contains(target.getUUID());
    }

    @Override
    protected final void readAdditionalSaveData(ValueInput input) {}
    @Override
    protected final void addAdditionalSaveData(ValueOutput output) {}
}
