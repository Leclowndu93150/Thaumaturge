package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.mixin.world.entity.ExperienceOrbAccessor;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityJarBrain extends AbstractSyncedBlockEntity {
    public static final int XP_MAX = 2000;

    private static final String XP_KEY = "XP";
    private static final double PULL_RANGE = 6.0;
    private static final double PULL_FADE_DISTANCE = 7.0;
    private static final double PULL_STRENGTH = 0.08;
    private static final double MIN_PULL_DISTANCE = 1.0E-3;
    private static final double COLLECT_MARGIN = 0.2;
    private static final double BLOCK_CENTER = 0.5;
    private static final float EAT_VOLUME = 0.1F;
    private static final float EAT_BASE_PITCH = 1.0F;
    private static final float EAT_PITCH_SPREAD = 0.4F;
    private static final double PLAYER_RANGE = 6.0;
    private static final long SIGH_FIRST_DELAY = 30L;
    private static final long SIGH_MIN_DELAY = 100L;
    private static final int SIGH_DELAY_SPREAD = 500;
    private static final long UNSCHEDULED = -1L;
    private static final float SIGH_VOLUME = 0.15F;
    private static final float SIGH_BASE_PITCH = 0.8F;
    private static final float SIGH_PITCH_SPREAD = 0.3F;
    private static final float IDLE_SPIN = 0.05F;
    private static final float TURN_RATE = 0.12F;

    public float rota;
    public float rotb;

    private int xp;
    private int eatDelay;
    private float targetYaw;
    private long nextSigh = UNSCHEDULED;

    public BlockEntityJarBrain(BlockPos pos, BlockState state) {
        super(TTBlockEntities.JAR_BRAIN.get(), pos, state);
    }

    public int xp() {
        return xp;
    }

    public void setXp(int newXp) {
        xp = Mth.clamp(newXp, 0, XP_MAX);
        setChangedAndSync();
    }

    public void setEatDelay(int ticks) {
        eatDelay = ticks;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityJarBrain brain) {
        if (brain.eatDelay > 0) {
            brain.eatDelay--;
        }
        ExperienceOrb lure = null;
        if (brain.isHungry()) {
            lure = brain.attractOrbs(level, pos);
            brain.collectOrbs(level, pos);
        }
        brain.sigh(level, pos, lure);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityJarBrain brain) {
        brain.rotb = brain.rota;
        ExperienceOrb orb = brain.isHungry() ? nearestOrb(level, pos) : null;
        Vec3 center = LevelSpaces.worldCenter(level, pos);
        if (orb != null) {
            brain.targetYaw = yawToward(level, pos, center, orb.position());
        } else {
            Player watcher = level.getNearestPlayer(center.x, center.y, center.z, PLAYER_RANGE, false);
            brain.targetYaw =
                    watcher == null ? brain.rota + IDLE_SPIN : yawToward(level, pos, center, watcher.position());
        }
        float remaining = Mth.wrapDegrees((brain.targetYaw - brain.rota) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        brain.rota = Mth.wrapDegrees((brain.rota + remaining * TURN_RATE) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
    }

    private boolean isHungry() {
        return xp < XP_MAX && eatDelay <= 0;
    }

    private static float yawToward(Level level, BlockPos pos, Vec3 from, Vec3 to) {
        Vec3 local = LevelSpaces.frameOf(level, pos).directionToLocal(to.subtract(from));
        return (float) Mth.atan2(local.z, local.x);
    }

    private static @Nullable ExperienceOrb nearestOrb(Level level, BlockPos pos) {
        Vec3 center = LevelSpaces.worldCenter(level, pos);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(
                ExperienceOrb.class,
                LevelSpaces.worldBox(level, pos, new AABB(pos).inflate(PULL_RANGE)),
                EntitySelector.ENTITY_STILL_ALIVE);
        ExperienceOrb nearest = null;
        double bestDistance = PULL_RANGE * PULL_RANGE;
        for (ExperienceOrb orb : orbs) {
            double distance = orb.distanceToSqr(center);
            if (distance <= bestDistance) {
                bestDistance = distance;
                nearest = orb;
            }
        }
        return nearest;
    }

    private @Nullable ExperienceOrb attractOrbs(Level level, BlockPos pos) {
        ExperienceOrb orb = nearestOrb(level, pos);
        if (orb == null) {
            return null;
        }
        Vec3 toJar = LevelSpaces.worldCenter(level, pos).subtract(orb.position());
        double distance = toJar.length();
        if (distance > MIN_PULL_DISTANCE) {
            double strength = Math.max(0.0, 1.0 - distance / PULL_FADE_DISTANCE) * PULL_STRENGTH;
            orb.setDeltaMovement(orb.getDeltaMovement().add(toJar.scale(strength / distance)));
        }
        return orb;
    }

    private void collectOrbs(Level level, BlockPos pos) {
        int collectedXp = xp;
        boolean collected = false;
        RandomSource random = level.getRandom();
        for (ExperienceOrb orb : level.getEntitiesOfClass(
                ExperienceOrb.class,
                LevelSpaces.worldBox(level, pos, new AABB(pos).inflate(COLLECT_MARGIN)),
                EntitySelector.ENTITY_STILL_ALIVE)) {
            if (collectedXp >= XP_MAX) {
                break;
            }
            int eaten = orbXp(orb.getValue(), ((ExperienceOrbAccessor) orb).thaumaturge$getCount());
            collectedXp = (int) Math.min(XP_MAX, (long) collectedXp + eaten);
            level.playSound(
                    null,
                    orb.getX(),
                    orb.getY(),
                    orb.getZ(),
                    SoundEvents.GENERIC_EAT,
                    SoundSource.BLOCKS,
                    EAT_VOLUME,
                    EAT_BASE_PITCH + random.nextFloat() * EAT_PITCH_SPREAD);
            orb.discard();
            collected = true;
        }
        if (collected) {
            setXp(collectedXp);
        }
    }

    static int orbXp(int value, int count) {
        return (int) Math.min(Integer.MAX_VALUE, (long) Math.max(0, value) * Math.max(1, count));
    }

    private void sigh(Level level, BlockPos pos, @Nullable ExperienceOrb lure) {
        long now = level.getGameTime();
        RandomSource random = level.getRandom();
        if (nextSigh == UNSCHEDULED) {
            nextSigh = now + SIGH_FIRST_DELAY + random.nextInt(SIGH_DELAY_SPREAD);
            return;
        }
        if (now < nextSigh) {
            return;
        }
        nextSigh = now + SIGH_MIN_DELAY + random.nextInt(SIGH_DELAY_SPREAD);
        if (lure == null
                && level.getNearestPlayer(
                                pos.getX() + BLOCK_CENTER,
                                pos.getY() + BLOCK_CENTER,
                                pos.getZ() + BLOCK_CENTER,
                                PLAYER_RANGE,
                                false)
                        != null) {
            level.playSound(
                    null,
                    pos,
                    TTSounds.BRAIN.get(),
                    SoundSource.BLOCKS,
                    SIGH_VOLUME,
                    SIGH_BASE_PITCH + random.nextFloat() * SIGH_PITCH_SPREAD);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        xp = input.getInt(XP_KEY);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt(XP_KEY, xp);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (xp > 0) {
            components.set(TTDataComponents.STORED_XP.get(), xp);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        Integer stored = components.get(TTDataComponents.STORED_XP.get());
        if (stored != null) {
            setXp(stored);
        }
    }
}
