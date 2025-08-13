package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class SelfSpellDelivery implements SpellDelivery {

    private final ServerPlayerEntity caster;
    private float powerMultiplier = 1.0f;
    private final List<Consumer<Entity>> effects = new ArrayList<>();
    private boolean redirectToTarget = false;
    private int scatterSize = 0;
    private List<ModifierEffect> modifiers = List.of();
    private final SpellCastPacket.KeyType tier;

    public SelfSpellDelivery(ServerPlayerEntity caster) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.tier = SpellCastPacket.KeyType.PRIMARY;
    }

    public SelfSpellDelivery(ServerPlayerEntity caster, SpellCastPacket.KeyType tier) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.tier = tier;
    }

    public ServerPlayerEntity getCaster() { return caster; }
    
    public List<Consumer<Entity>> getEffects() { return Collections.unmodifiableList(effects); }
    public void setRedirectToTarget(boolean redirect) { this.redirectToTarget = redirect; }
    public float getPowerMultiplier() { return powerMultiplier; }
    public void setPowerMultiplier(float powerMultiplier) { this.powerMultiplier = powerMultiplier; }
    public void addEffect(Consumer<Entity> effect) { effects.add(effect); }
    public int getScatterSize() { return scatterSize; }
    public void setScatterSize(int scatterSize) { this.scatterSize = scatterSize; }
    
    @Override
    public void setModifiers(List<ModifierEffect> mods) {
        this.modifiers = mods != null ? List.copyOf(mods) : List.of();
    }
    
    public List<ModifierEffect> getModifiers() { return modifiers; }
    
    public SpellCastPacket.KeyType getTier() { return tier; }

    public void execute(ServerPlayerEntity caster) {
        if (redirectToTarget) {
            World world = caster.getWorld();
            Vec3d eyePos = caster.getEyePos();
            Vec3d direction = caster.getRotationVector().normalize();
            Vec3d endPos = eyePos.add(direction.multiply(16.0));

            EntityHitResult entityHit = ProjectileUtil.raycast(
                    caster,
                    eyePos,
                    endPos,
                    new Box(eyePos, endPos),
                    entity -> !entity.isSpectator() && entity.isAlive() && entity != caster,
                    16.0
            );

            if (entityHit != null) {
                Entity target = entityHit.getEntity();
                effects.forEach(effect -> effect.accept(target));
            }
        } else {
            effects.forEach(effect -> effect.accept(caster));
        }
    }
}
