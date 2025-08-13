package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class TargetedSpellDelivery implements SpellDelivery {

    private final ServerPlayerEntity caster;
    private final ServerWorld world;
    private final Entity targetEntity;
    private final BlockPos blockPos;
    private final Direction face;
    
    private int projectileCount = 1;
    private float spread = 0.0f;
    private double maxDistance = 16.0;
    private boolean swapActorTarget = false;
    private float powerMultiplier = 1.0f;
    private final List<Consumer<Entity>> onHitEffects = new ArrayList<>();
    private final List<Consumer<BlockHitResult>> onBlockHitEffects = new ArrayList<>();
    private int scatterSize = 0;
    private List<ModifierEffect> modifiers = List.of();
    private final SpellCastPacket.KeyType tier;

    public TargetedSpellDelivery(ServerPlayerEntity caster, Entity targetEntity) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.targetEntity = Objects.requireNonNull(targetEntity, "targetEntity");
        this.blockPos = null;
        this.face = null;
        this.tier = SpellCastPacket.KeyType.SECONDARY; // Default for targeted
    }

    public TargetedSpellDelivery(ServerPlayerEntity caster, BlockPos blockPos, Direction face) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.blockPos = Objects.requireNonNull(blockPos, "blockPos");
        this.face = Objects.requireNonNull(face, "face");
        this.targetEntity = null;
        this.tier = SpellCastPacket.KeyType.SECONDARY; // Default for targeted
    }

    public TargetedSpellDelivery(ServerPlayerEntity caster, Entity targetEntity, SpellCastPacket.KeyType tier) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.targetEntity = Objects.requireNonNull(targetEntity, "targetEntity");
        this.blockPos = null;
        this.face = null;
        this.tier = tier;
    }

    public TargetedSpellDelivery(ServerPlayerEntity caster, BlockPos blockPos, Direction face, SpellCastPacket.KeyType tier) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.blockPos = Objects.requireNonNull(blockPos, "blockPos");
        this.face = Objects.requireNonNull(face, "face");
        this.targetEntity = null;
        this.tier = tier;
    }

    public ServerPlayerEntity getCaster() { return caster; }
    public ServerWorld getWorld() { return world; }
    public boolean isEntityTarget() { return targetEntity != null; }
    public boolean isBlockTarget()  { return blockPos != null && face != null; }
    public Entity getTargetEntity() { return targetEntity; }
    public BlockPos getBlockPos()   { return blockPos; }
    public Direction getFace()      { return face; }

    public int getScatterSize() { return scatterSize; }
    public void setScatterSize(int scatterSize) { this.scatterSize = scatterSize; }
    
    public void setProjectileCount(int count) { this.projectileCount = count; }
    public void setSpread(float spread) { this.spread = spread; }
    public void setMaxDistance(double maxDistance) { this.maxDistance = maxDistance; }
    public void setSwapActorTarget(boolean swap) { this.swapActorTarget = swap; }
    public float getPowerMultiplier() { return powerMultiplier; }
    public void setPowerMultiplier(float powerMultiplier) { this.powerMultiplier = powerMultiplier; }
    
    public void addOnHitEffect(Consumer<Entity> effect) { onHitEffects.add(effect); }
    public void addBlockHitEffect(Consumer<BlockHitResult> effect) { onBlockHitEffects.add(effect); }
    public List<Consumer<Entity>> getOnHitEffects() { return onHitEffects; }
    public List<Consumer<BlockHitResult>> getOnBlockHitEffects() { return onBlockHitEffects; }
    
    @Override
    public void setModifiers(List<ModifierEffect> mods) {
        this.modifiers = mods != null ? List.copyOf(mods) : List.of();
    }
    
    public List<ModifierEffect> getModifiers() { return modifiers; }
    
    public SpellCastPacket.KeyType getTier() { return tier; }

    public void execute(ServerPlayerEntity caster) {
        Vec3d eyePos = caster.getEyePos();

        for (int i = 0; i < projectileCount; i++) {
            float yawOffset = (i - (projectileCount - 1) / 2.0f) * spread;
            float currentYaw = caster.getYaw() + yawOffset;
            float currentPitch = caster.getPitch();

            Vec3d direction = Vec3d.fromPolar(currentPitch, currentYaw).normalize();
            Vec3d endPos = eyePos.add(direction.multiply(maxDistance));

            EntityHitResult entityHit = ProjectileUtil.raycast(
                    caster,
                    eyePos,
                    endPos,
                    new Box(eyePos, endPos),
                    entity -> !entity.isSpectator() && entity.isAlive() && entity != caster,
                    maxDistance
            );

            if (entityHit != null) {
                Entity target = swapActorTarget ? caster : entityHit.getEntity();
                for (Consumer<Entity> effect : onHitEffects) {
                    effect.accept(target);
                }
            } else {
                RaycastContext raycastContext = new RaycastContext(
                        eyePos, endPos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.ANY,
                        caster
                );
                BlockHitResult blockHit = world.raycast(raycastContext);
                if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
                    for (Consumer<BlockHitResult> effect : onBlockHitEffects) {
                        effect.accept(blockHit);
                    }
                }
            }
        }
    }
}
