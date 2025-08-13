package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class AoeSpellDelivery implements SpellDelivery {

    private final ServerPlayerEntity caster;
    private final ServerWorld world;
    private final BlockPos center;
    private float radius;
    private final List<Consumer<BlockPos>> effects = new ArrayList<>();
    private float powerMultiplier = 1.0f;
    private int scatterSize = 0;
    private List<ModifierEffect> modifiers = List.of();
    private final SpellCastPacket.KeyType tier;

    public AoeSpellDelivery(ServerPlayerEntity caster, BlockPos center, float radius) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.center = Objects.requireNonNull(center, "center");
        this.radius = Math.max(0f, Math.min(radius, 32f));
        this.tier = SpellCastPacket.KeyType.TERNARY;
    }

    public AoeSpellDelivery(ServerPlayerEntity caster, BlockPos center, float radius, SpellCastPacket.KeyType tier) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.center = Objects.requireNonNull(center, "center");
        this.radius = Math.max(0f, Math.min(radius, 32f));
        this.tier = tier;
    }

    public ServerPlayerEntity getCaster() { return caster; }
    public ServerWorld getWorld() { return world; }
    public BlockPos getCenter() { return center; }
    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }
    
    public int getScatterSize() { return scatterSize; }
    public void setScatterSize(int scatterSize) { this.scatterSize = scatterSize; }
    public void addEffect(Consumer<BlockPos> effect) { effects.add(effect); }
    public float getPowerMultiplier() { return powerMultiplier; }
    public void setPowerMultiplier(float powerMultiplier) { this.powerMultiplier = powerMultiplier; }
    
    @Override
    public void setModifiers(List<ModifierEffect> mods) {
        this.modifiers = mods != null ? List.copyOf(mods) : List.of();
    }
    
    public List<ModifierEffect> getModifiers() { return modifiers; }
    
    public SpellCastPacket.KeyType getTier() { return tier; }

    public <T extends Entity> List<T> getEntitiesInAabb(Class<T> type, Predicate<T> filter) {
        double cx = center.getX() + 0.5;
        double cy = center.getY() + 0.5;
        double cz = center.getZ() + 0.5;
        Box box = new Box(cx - radius, cy - 2, cz - radius, cx + radius, cy + 2, cz + radius);
        return world.getEntitiesByClass(type, box, filter);
    }

    public void execute(ServerPlayerEntity caster) {
        BlockPos center = caster.getBlockPos();
        int radiusInt = MathHelper.floor(this.radius);
        BlockPos min = center.add(-radiusInt, -radiusInt, -radiusInt);
        BlockPos max = center.add(radiusInt, radiusInt, radiusInt);
        BlockPos.iterate(min, max).forEach(pos -> {
            for (Consumer<BlockPos> effect : effects) {
                effect.accept(pos);
            }
        });
    }
}
