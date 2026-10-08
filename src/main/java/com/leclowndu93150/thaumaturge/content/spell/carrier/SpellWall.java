package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpellWall extends AbstractSpellCarrier {
    private static final EntityDataAccessor<Integer> WIDTH =
            SynchedEntityData.defineId(SpellWall.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FACING =
            SynchedEntityData.defineId(SpellWall.class, EntityDataSerializers.FLOAT);
    private static final double HEIGHT = 2.5;
    private static final double THICKNESS = 0.5;
    private static final int PULSE_INTERVAL = 5;
    private static final int REHIT_COOLDOWN = 20;
    private static final int SHIMMER_PER_CELL = 1;
    private static final int DEFAULT_WIDTH = 3;
    private static final double SHIMMER_RISE = 0.01;

    private final Map<Integer, Long> cooldowns = new HashMap<>();

    public SpellWall(EntityType<? extends SpellWall> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static void raise(
            ServerLevel level,
            LivingEntity owner,
            CarrierPayload payload,
            Vec3 base,
            Vec3 facing,
            int width,
            int lifetime) {
        SpellWall wall = new SpellWall(TTEntities.SPELL_WALL.get(), level);
        wall.bind(owner, payload, lifetime);
        wall.entityData.set(WIDTH, width);
        wall.entityData.set(FACING, (float) Math.atan2(facing.z, facing.x));
        wall.setPos(base.x, base.y, base.z);
        level.addFreshEntity(wall);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WIDTH, DEFAULT_WIDTH);
        builder.define(FACING, 0.0F);
    }

    @Override
    protected void saveCarrierData(CompoundTag output) {
        output.putInt("width", entityData.get(WIDTH));
        output.putFloat("facing", entityData.get(FACING));
    }

    @Override
    protected void loadCarrierData(CompoundTag input) {
        entityData.set(WIDTH, (input.contains("width") ? input.getInt("width") : DEFAULT_WIDTH));
        entityData.set(FACING, (input.contains("facing") ? input.getFloat("facing") : 0.0F));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            shimmer();
            return;
        }
        if (expired()) {
            discard();
            return;
        }
        if (tickCount % PULSE_INTERVAL == 0 && level() instanceof ServerLevel level) {
            pulse(level);
        }
    }

    private Vec3 along() {
        float facing = entityData.get(FACING);
        return new Vec3(-Math.sin(facing), 0.0, Math.cos(facing));
    }

    private List<Vec3> cells() {
        int width = entityData.get(WIDTH);
        Vec3 along = along();
        List<Vec3> out = new ArrayList<>(width);
        for (int cell = 0; cell < width; cell++) {
            out.add(position().add(along.scale(cell - (width - 1) / 2.0)));
        }
        return out;
    }

    private void pulse(ServerLevel level) {
        long now = level.getGameTime();
        cooldowns.values().removeIf(until -> until <= now);
        float facing = entityData.get(FACING);
        Vec3 normal = new Vec3(Math.cos(facing), 0.0, Math.sin(facing));
        List<SpellTarget> targets = new ArrayList<>();
        for (Vec3 cell : cells()) {
            AABB box = new AABB(
                    cell.x - THICKNESS,
                    cell.y,
                    cell.z - THICKNESS,
                    cell.x + THICKNESS,
                    cell.y + HEIGHT,
                    cell.z + THICKNESS);
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box, Entity::isAlive)) {
                if (!cooldowns.containsKey(living.getId())) {
                    cooldowns.put(living.getId(), now + REHIT_COOLDOWN);
                    targets.add(SpellTarget.entity(living, normal));
                }
            }
        }
        if (!targets.isEmpty()) {
            charge.resume(level, targets);
        }
    }

    private void shimmer() {
        for (Vec3 cell : cells()) {
            for (int mote = 0; mote < SHIMMER_PER_CELL; mote++) {
                Vec3 at = cell.add(
                        (random.nextDouble() - 0.5) * THICKNESS,
                        random.nextDouble() * HEIGHT,
                        (random.nextDouble() - 0.5) * THICKNESS);
                CarrierPayload.particle(level(), charge.look(), at, new Vec3(0.0, SHIMMER_RISE, 0.0));
            }
        }
    }
}
