package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class SpellMine extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> ARMED = SynchedEntityData.defineId(SpellMine.class, EntityDataSerializers.BOOLEAN);
    private static final String TRAP_KEY = "trap";
    private static final String ARMED_KEY = "armed";
    private static final String ALLIES_KEY = "allies";
    private static final float THROW_SPEED = 0.3F;
    private static final float NO_DIVERGENCE = 0.0F;
    private static final double MINE_GRAVITY = 0.01;
    private static final int MAX_AGE = 1200;
    private static final int MAX_VICTIMS = 4096;
    private static final int ARM_DELAY = 40;
    private static final int SCAN_INTERVAL = 5;
    private static final double TRIGGER_RANGE = 1.0;
    private static final double SETTLE_DAMPING = 0.25;
    private static final double SETTLE_STEP = 0.05;
    private static final int SETTLE_ATTEMPTS = 10;
    private static final int GLOW_INTERVAL = 5;
    private static final double GLOW_SPREAD = 0.1;

    private final CarrierCharge charge = new CarrierCharge();
    private boolean allies;
    private int armedAt;
    private @Nullable List<UUID> victims;
    private int nextVictim;

    public SpellMine(EntityType<? extends SpellMine> type, Level level) {
        super(type, level);
    }

    public static void place(ServerLevel level, LivingEntity owner, CarrierPayload payload, SpellTarget origin, boolean allies) {
        SpellMine mine = new SpellMine(TTEntities.FOCUS_MINE.get(), level);
        mine.setOwner(owner);
        mine.allies = allies;
        mine.charge.arm(level, payload);
        mine.setPos(origin.position());
        Vec3 aim = origin.direction();
        mine.shoot(aim.x, aim.y, aim.z, THROW_SPEED, NO_DIVERGENCE);
        level.addFreshEntity(mine);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ARMED, false);
    }

    public boolean armed() {
        return entityData.get(ARMED);
    }

    public int color() {
        return charge.look().color();
    }

    @Override
    protected double getDefaultGravity() {
        return MINE_GRAVITY;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.writeLook(buffer);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.readLook(buffer);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ValueOutput trap = output.child(TRAP_KEY);
        trap.putBoolean(ARMED_KEY, armed());
        trap.putBoolean(ALLIES_KEY, allies);
        trap.putInt("age", tickCount);
        trap.putInt("armed_at", armedAt);
        trap.putInt("next_victim", nextVictim);
        trap.storeNullable("victims", UUIDUtil.CODEC.listOf(0, MAX_VICTIMS), victims);
        charge.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        charge.load(input);
        ValueInput trap = input.childOrEmpty(TRAP_KEY);
        allies = trap.getBooleanOr(ALLIES_KEY, false);
        entityData.set(ARMED, trap.getBooleanOr(ARMED_KEY, false));
        tickCount = trap.getIntOr("age", 0);
        armedAt = trap.getIntOr("armed_at", -ARM_DELAY);
        victims = trap.read("victims", UUIDUtil.CODEC.listOf(0, MAX_VICTIMS)).orElse(null);
        nextVictim = Math.clamp(trap.getIntOr("next_victim", 0), 0, victims == null ? 0 : victims.size());
    }

    @Override
    protected void onHit(HitResult result) {
        if (!armed() && !tryArm()) {
            return;
        }
        setDeltaMovement(getDeltaMovement().scale(SETTLE_DAMPING));
        pushOut(outwardOf(result));
    }

    private boolean tryArm() {
        if (!(level() instanceof ServerLevel) || getOwner() == null) {
            return false;
        }
        armedAt = tickCount;
        entityData.set(ARMED, true);
        return true;
    }

    private static Direction outwardOf(HitResult result) {
        return result instanceof BlockHitResult block ? block.getDirection() : Direction.UP;
    }

    private void pushOut(Direction outward) {
        int attempt = 0;
        while (attempt < SETTLE_ATTEMPTS && isEmbedded()) {
            setPos(position().relative(outward, SETTLE_STEP));
            attempt++;
        }
    }

    private boolean isEmbedded() {
        return !level().noBlockCollision(this, getBoundingBox());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            pulseGlow();
        } else if (!isRemoved()) {
            serverTick(server);
        }
    }

    private void pulseGlow() {
        if (!armed() || tickCount % GLOW_INTERVAL != 0) {
            return;
        }
        Vec3 jitter = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(GLOW_SPREAD);
        CarrierPayload.particle(level(), charge.look(), position().add(jitter), Vec3.ZERO);
    }

    private void serverTick(ServerLevel server) {
        if (!(getOwner() instanceof LivingEntity master) || tickCount > MAX_AGE || !charge.valid(server)) {
            discard();
            return;
        }
        if (victims != null) {
            releaseNext(server);
        } else if (scanDue()) {
            gather(server, master);
        }
    }

    private boolean scanDue() {
        return armed() && tickCount % SCAN_INTERVAL == 0 && tickCount - armedAt >= ARM_DELAY;
    }

    private void gather(ServerLevel server, LivingEntity master) {
        List<LivingEntity> found = SpellTargeting.livingWithin(server, position(), TRIGGER_RANGE, candidate -> SpellTargeting.isAlly(master, candidate) == allies);
        if (!found.isEmpty()) {
            victims = found.stream().limit(MAX_VICTIMS).map(LivingEntity::getUUID).toList();
            nextVictim = 0;
        }
    }

    private @Nullable LivingEntity nextLiving(ServerLevel level) {
        while (nextVictim < victims.size()) {
            if (level.getEntity(victims.get(nextVictim++)) instanceof LivingEntity candidate && candidate.isAlive()) {
                return candidate;
            }
        }
        return null;
    }

    private void releaseNext(ServerLevel server) {
        LivingEntity victim = nextLiving(server);
        if (victim != null) {
            Vec3 heading = victim.getBoundingBox().getCenter().subtract(position());
            charge.resume(server, List.of(SpellTarget.entity(victim, heading)));
        }
        if (victim == null || nextVictim >= victims.size() || charge.isSpent()) {
            discard();
        }
    }
}
