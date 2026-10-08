package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.api.labyrinth.SiteContext;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

record AltarSiteContext(ServerLevel level, BlockEntityEldritchAltar altarEntity) implements SiteContext {
    @Override
    public BlockPos altar() {
        return altarEntity.getBlockPos();
    }

    @Override
    public RandomSource random() {
        return level.getRandom();
    }

    @Override
    public boolean playerWithin(double range) {
        BlockPos pos = altar();
        return level.getNearestPlayer(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        range,
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR)
                != null;
    }

    @Override
    public boolean activated() {
        return altarEntity.garrison().activated();
    }

    @Override
    public void activate(int budget) {
        altarEntity.setGarrison(altarEntity.garrison().activate(budget));
    }

    @Override
    public int budget() {
        return altarEntity.garrison().budget();
    }

    @Override
    public boolean consumeBudget() {
        if (altarEntity.garrison().budget() <= 0) {
            return false;
        }
        altarEntity.setGarrison(altarEntity.garrison().spend());
        return true;
    }

    @Override
    public void quell() {
        altarEntity.setGarrison(altarEntity.garrison().quell());
    }

    @Override
    public List<Mob> members(double radius) {
        return SiteSpawns.members(level, altar(), radius);
    }

    @Override
    public Optional<Mob> spawnMember(EntityType<?> type, int minRadius, int maxRadius) {
        return SiteSpawns.spawn(level, altar(), type, minRadius, maxRadius, level.getRandom());
    }
}
