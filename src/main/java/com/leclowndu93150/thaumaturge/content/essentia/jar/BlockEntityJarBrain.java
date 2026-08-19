package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityJarBrain extends BlockEntity {
    public static final int XP_MAX = 2000;
    private static final double PULL_RANGE = 8.0;
    private static final double EAT_INFLATE = 0.1;
    private static final double SIGH_RANGE = 6.0;
    private static final long SIGH_INITIAL_DELAY = 30L;
    private static final long SIGH_DELAY_BASE = 100L;
    private static final int SIGH_DELAY_SPREAD = 500;

    private int xp;
    private int eatDelay;

    public float rota;
    public float rotb;
    private float targetRot;
    private float wander;
    private float wanderStep;
    private long nextSigh = Long.MIN_VALUE;

    public BlockEntityJarBrain(BlockPos pos, BlockState state) {
        super(TCBlockEntities.JAR_BRAIN.get(), pos, state);
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Mth.clamp(xp, 0, XP_MAX);
    }

    public void setEatDelay(int eatDelay) {
        this.eatDelay = eatDelay;
    }

    public void syncToClient() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityJarBrain jar) {
        if (jar.xp > XP_MAX) {
            jar.xp = XP_MAX;
        }
        if (jar.xp < XP_MAX) {
            jar.pullClosestOrb(level, pos);
        }
        if (jar.eatDelay > 0) {
            jar.eatDelay--;
            return;
        }
        if (jar.xp >= XP_MAX) {
            return;
        }
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class,
                new AABB(pos.getX() - EAT_INFLATE, pos.getY() - EAT_INFLATE, pos.getZ() - EAT_INFLATE, pos.getX() + 1 + EAT_INFLATE, pos.getY() + 1 + EAT_INFLATE, pos.getZ() + 1 + EAT_INFLATE));
        if (orbs.isEmpty()) {
            return;
        }
        for (ExperienceOrb orb : orbs) {
            jar.xp += orb.getValue();
            orb.playSound(SoundEvents.GENERIC_EAT.value(), 0.1F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
            orb.discard();
        }
        jar.setChanged();
        jar.syncToClient();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityJarBrain jar) {
        Entity focus = null;
        if (jar.xp < XP_MAX) {
            focus = jar.pullClosestOrb(level, pos);
        }
        jar.rotb = jar.rota;
        if (focus == null) {
            focus = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SIGH_RANGE, false);
            if (focus != null) {
                long time = level.getGameTime();
                if (jar.nextSigh == Long.MIN_VALUE) {
                    jar.nextSigh = time + SIGH_INITIAL_DELAY;
                } else if (time >= jar.nextSigh) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.BRAIN.get(), SoundSource.AMBIENT, 0.15F, 0.8F + level.getRandom().nextFloat() * 0.4F, false);
                    jar.nextSigh = time + SIGH_DELAY_BASE + level.getRandom().nextInt(SIGH_DELAY_SPREAD);
                }
            }
        }
        if (focus != null) {
            double dx = focus.getX() - (pos.getX() + 0.5F);
            double dz = focus.getZ() - (pos.getZ() + 0.5F);
            jar.targetRot = (float) Math.atan2(dz, dx);
            jar.wanderStep += 0.1F;
            if (jar.wanderStep < 0.5F || level.getRandom().nextInt(40) == 0) {
                float previous = jar.wander;
                do {
                    jar.wander = jar.wander + (level.getRandom().nextInt(4) - level.getRandom().nextInt(4));
                } while (previous == jar.wander);
            }
        } else {
            jar.targetRot += 0.01F;
        }
        jar.rota = wrapRadians(jar.rota);
        jar.targetRot = wrapRadians(jar.targetRot);
        float delta = wrapRadians(jar.targetRot - jar.rota);
        jar.rota += delta * 0.04F;
        if (jar.eatDelay > 0) {
            jar.eatDelay--;
        }
    }

    private static float wrapRadians(float angle) {
        while (angle >= (float) Math.PI) {
            angle -= (float) (Math.PI * 2);
        }
        while (angle < -(float) Math.PI) {
            angle += (float) (Math.PI * 2);
        }
        return angle;
    }

    private @Nullable Entity pullClosestOrb(Level level, BlockPos pos) {
        ExperienceOrb closest = null;
        double closestDist = Double.MAX_VALUE;
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(PULL_RANGE))) {
            double dist = orb.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (dist < closestDist) {
                closest = orb;
                closestDist = dist;
            }
        }
        if (closest != null && eatDelay == 0) {
            double dx = (pos.getX() + 0.5 - closest.getX()) / 25.0;
            double dy = (pos.getY() + 0.5 - closest.getY()) / 25.0;
            double dz = (pos.getZ() + 0.5 - closest.getZ()) / 25.0;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double strength = 1.0 - dist;
            if (strength > 0.0) {
                strength *= strength;
                Vec3 motion = closest.getDeltaMovement();
                closest.setDeltaMovement(motion.x + dx / dist * strength * 0.3, motion.y + dy / dist * strength * 0.5, motion.z + dz / dist * strength * 0.3);
            }
        }
        return closest;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        xp = input.getIntOr("XP", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("XP", xp);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (xp > 0) {
            builder.set(TCDataComponents.STORED_XP.get(), xp);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        Integer stored = input.get(TCDataComponents.STORED_XP.get());
        if (stored != null) {
            setXp(stored);
        }
    }
}
