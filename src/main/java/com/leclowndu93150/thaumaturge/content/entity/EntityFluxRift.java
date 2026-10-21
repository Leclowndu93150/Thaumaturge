package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.api.rifts.RiftChange;
import com.leclowndu93150.thaumaturge.api.rifts.RiftSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class EntityFluxRift extends Entity {
    private static final AtomicLong REVISION_CLOCK = new AtomicLong();
    private long revision = REVISION_CLOCK.updateAndGet(current -> current == Long.MAX_VALUE ? current : current + 1);
    private @Nullable UUID transferToken;
    private boolean persistedTransferToken;
    private boolean transferSuspended;
    private boolean transferChecked;
    private @Nullable RiftChange pendingChange;
    private final MutationJournal mutationJournal = new MutationJournal();
    public static final int MAX_RIFT_SIZE = 100;

    private static final EntityDataAccessor<Boolean> COLLAPSE = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> STABILITY = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SEED = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);

    private static final String COLLAPSE_SIZE_KEY = "collapse_size";
    private static final String SIZE_KEY = "rift_size";
    private static final String SEED_KEY = "rift_seed";
    private static final String STABILITY_KEY = "stability_value";
    private static final String COLLAPSING_KEY = "is_collapsing";
    private static final String LEGACY_COLLAPSE_SIZE_KEY = "MaxSize";
    private static final String LEGACY_SIZE_KEY = "RiftSize";
    private static final String LEGACY_SEED_KEY = "RiftSeed";
    private static final String LEGACY_STABILITY_KEY = "Stability";
    private static final String LEGACY_COLLAPSING_KEY = "collapse";
    private static final int DEFAULT_SIZE = 5;

    private static final float MAX_STABILITY = 100.0F;
    private static final float VERY_STABLE_THRESHOLD = 50.0F;
    private static final float UNSTABLE_THRESHOLD = 0.0F;
    private static final float VERY_UNSTABLE_THRESHOLD = -25.0F;
    private static final float STABILIZER_STEP = 0.125F;
    private static final int DECAY_INTERVAL = 120;
    private static final float DECAY_AMOUNT = 0.2F;

    private static final int MIN_OUTLINE_POINTS = 3;
    private static final int INITIAL_OUTLINE_CAPACITY = 16;

    private static final int SOUND_INTERVAL = 300;
    private static final float SOUND_VOLUME_BASE = 0.15F;
    private static final float SOUND_VOLUME_SPREAD = 0.066F;
    private static final float SOUND_PITCH_BASE = 0.75F;
    private static final float SOUND_PITCH_SPREAD = 0.1F;

    public final List<Vec3> outline = new ArrayList<>(INITIAL_OUTLINE_CAPACITY);
    public final List<Float> outlineWidths = new ArrayList<>(INITIAL_OUTLINE_CAPACITY);

    private final RiftSegmentSweep sweep = new RiftSegmentSweep();
    private final RiftCollapse collapse = new RiftCollapse();
    private final RiftTaskSchedule schedule = new RiftTaskSchedule(List.of(new RiftGrowthTask(), new RiftEventTask()));

    private int collapseSize;
    private int builtSize = -1;
    private int builtSeed;

    public EntityFluxRift(EntityType<? extends EntityFluxRift> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static void spawnNear(ServerLevel level, BlockPos origin) {
        RiftSpawner.spawnNear(level, origin);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SEED, 0);
        builder.define(SIZE, DEFAULT_SIZE);
        builder.define(STABILITY, 0.0F);
        builder.define(COLLAPSE, false);
    }

    private <T> T synced(EntityDataAccessor<T> accessor) {
        return this.entityData.get(accessor);
    }

    private <T> void publish(EntityDataAccessor<T> accessor, T value) {
        if (!java.util.Objects.equals(this.entityData.get(accessor), value)) {
            changed();
        }
        this.entityData.set(accessor, value);
    }

    private void changed() {
        if (revision < Long.MAX_VALUE) {
            long next = revision + 1;
            revision = REVISION_CLOCK.updateAndGet(current -> current == Long.MAX_VALUE ? current : Math.max(current + 1, next));
        }
    }

    public RiftSnapshot snapshot() {
        return new RiftSnapshot(getUUID(), level().dimension(), revision, position(), currentSize(), shapeSeed(), stabilityValue(), isCollapsing(),
                pendingChange != null || transferSuspended || revision == Long.MAX_VALUE);
    }

    public Optional<UUID> transferToken() {
        return Optional.ofNullable(transferToken);
    }

    public void transferToken(UUID token) {
        transferToken = token;
        persistedTransferToken = false;
        transferChecked = false;
    }

    public boolean hasPersistedTransferToken() {
        return persistedTransferToken;
    }

    public void suspendTransfer() {
        transferSuspended = true;
    }

    public void resumeTransfer() {
        transferSuspended = false;
        transferChecked = true;
    }

    public void restoreSnapshot(RiftSnapshot snapshot) {
        setUUID(snapshot.identity());
        setPos(snapshot.position());
        reseed(snapshot.seed());
        resize(snapshot.size());
        restoreStability(snapshot.stability());
        publish(COLLAPSE, snapshot.collapsing());
        revision = Math.max(revision, snapshot.revision());
        changed();
    }

    void stageChange(RiftChange change, TransactionContext transaction) {
        mutationJournal.updateSnapshots(transaction);
        pendingChange = change;
    }

    private final class MutationJournal extends SnapshotJournal<RiftSnapshot> {
        @Override
        protected RiftSnapshot createSnapshot() {
            return snapshot();
        }

        @Override
        protected void revertToSnapshot(RiftSnapshot original) {
            pendingChange = null;
        }

        @Override
        protected void onRootCommit(RiftSnapshot original) {
            RiftChange change = pendingChange;
            pendingChange = null;
            if (change != null) {
                setPos(change.position());
                reseed(change.seed());
                resize(change.size());
                restoreStability(change.stability());
                if (change.collapse()) {
                    beginCollapse();
                }
                changed();
            }
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        if (getX() != x || getY() != y || getZ() != z) {
            changed();
        }
        super.setPos(x, y, z);
    }

    private void refreshOutlineIfStale() {
        boolean stale = this.builtSize != currentSize() || this.builtSeed != shapeSeed();
        if (stale) {
            rebuildOutline();
        }
    }

    private void ensureSeeded() {
        if (shapeSeed() != 0) {
            return;
        }
        reseed(this.random.nextInt());
    }

    private boolean onInterval(int interval) {
        return this.tickCount % interval == 0;
    }

    private void tickServer(ServerLevel level) {
        ensureSeeded();
        this.sweep.tick(level, this);
        boolean collapsing = isCollapsing();
        if (!collapsing && this.outline.size() < MIN_OUTLINE_POINTS) {
            beginCollapse();
            collapsing = true;
        }
        if (collapsing && this.collapse.tick(level, this)) {
            return;
        }
        if (this.tickCount > 0 && onInterval(DECAY_INTERVAL)) {
            adjustStability(-DECAY_AMOUNT);
        }
        this.schedule.tick(level, this);
        if (onInterval(SOUND_INTERVAL) && !this.isRemoved()) {
            emitAmbientSound(level);
        }
    }

    private float gaussianAround(float base, float spread) {
        return base + (float) this.random.nextGaussian() * spread;
    }

    private void emitAmbientSound(ServerLevel world) {
        world.playSound(null, this.getX(), this.getY(), this.getZ(), TTSounds.EVILPORTAL.get(), SoundSource.AMBIENT, gaussianAround(SOUND_VOLUME_BASE, SOUND_VOLUME_SPREAD),
                gaussianAround(SOUND_PITCH_BASE, SOUND_PITCH_SPREAD));
    }

    private void rebuildOutline() {
        this.builtSize = currentSize();
        this.builtSeed = shapeSeed();
        this.outlineWidths.clear();
        this.outline.clear();
        RiftOutline.build(this.builtSize, this.builtSeed, this.outline, this.outlineWidths);
        this.reapplyPosition();
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (level() instanceof ServerLevel && !transferChecked) {
            transferChecked = true;
            if (!RiftTransferOperations.reconcile(this)) {
                transferSuspended = true;
            }
        }
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel) {
            if (!transferChecked) {
                transferChecked = true;
                if (!RiftTransferOperations.reconcile(this)) {
                    transferSuspended = true;
                }
            }
            if (transferSuspended || pendingChange != null || isRemoved()) {
                return;
            }
        }
        super.tick();
        refreshOutlineIfStale();
        if (this.level() instanceof ServerLevel server) {
            tickServer(server);
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement) {}

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    public int currentSize() {
        return synced(SIZE);
    }

    public void resize(int size) {
        publish(SIZE, size);
        rebuildOutline();
    }

    public int shapeSeed() {
        return synced(SEED);
    }

    public void reseed(int seed) {
        publish(SEED, seed);
    }

    public boolean isCollapsing() {
        return synced(COLLAPSE);
    }

    public void beginCollapse() {
        if (isCollapsing()) {
            return;
        }
        this.collapseSize = currentSize();
        publish(COLLAPSE, true);
    }

    public int collapseSize() {
        return this.collapseSize;
    }

    public float stabilityValue() {
        return synced(STABILITY);
    }

    public void adjustStability(float delta) {
        restoreStability(stabilityValue() + delta);
    }

    public void nudgeStability() {
        adjustStability(STABILIZER_STEP);
    }

    private void restoreStability(float stability) {
        publish(STABILITY, Mth.clamp(stability, -MAX_STABILITY, MAX_STABILITY));
    }

    public Stability stabilityTier() {
        float stability = stabilityValue();
        if (stability > VERY_STABLE_THRESHOLD) {
            return Stability.VERY_STABLE;
        }
        if (stability >= UNSTABLE_THRESHOLD) {
            return Stability.STABLE;
        }
        return stability > VERY_UNSTABLE_THRESHOLD ? Stability.UNSTABLE : Stability.VERY_UNSTABLE;
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        List<Vec3> points = this.outline;
        if (points == null || points.isEmpty()) {
            return super.makeBoundingBox(position);
        }
        return RiftBounds.around(points, position);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putLong("api_revision", revision);
        if (transferToken != null) {
            output.store("transfer_token", UUIDUtil.CODEC, transferToken);
        }
        output.putBoolean(COLLAPSING_KEY, isCollapsing());
        output.putFloat(STABILITY_KEY, stabilityValue());
        output.putInt(SEED_KEY, shapeSeed());
        output.putInt(SIZE_KEY, currentSize());
        output.putInt(COLLAPSE_SIZE_KEY, this.collapseSize);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        publish(COLLAPSE, input.getBooleanOr(COLLAPSING_KEY, input.getBooleanOr(LEGACY_COLLAPSING_KEY, false)));
        restoreStability(input.getFloatOr(STABILITY_KEY, input.getFloatOr(LEGACY_STABILITY_KEY, 0.0F)));
        publish(SIZE, input.getIntOr(SIZE_KEY, input.getIntOr(LEGACY_SIZE_KEY, DEFAULT_SIZE)));
        publish(SEED, input.getIntOr(SEED_KEY, input.getIntOr(LEGACY_SEED_KEY, 0)));
        this.collapseSize = input.getIntOr(COLLAPSE_SIZE_KEY, input.getIntOr(LEGACY_COLLAPSE_SIZE_KEY, 0));
        revision = Math.max(0, input.getLongOr("api_revision", 0));
        changed();
        transferToken = input.read("transfer_token", UUIDUtil.CODEC).orElse(null);
        persistedTransferToken = transferToken != null;
        transferChecked = false;
        rebuildOutline();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public enum Stability {
        VERY_STABLE, STABLE, UNSTABLE, VERY_UNSTABLE
    }
}
