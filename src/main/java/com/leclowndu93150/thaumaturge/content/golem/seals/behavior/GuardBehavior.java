package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public final class GuardBehavior extends HuntBehavior {
    public static final SealSetting MONSTERS =
            new SealSetting("target_monsters", "gui.thaumaturge.seal.setting.mob", true).readingAlso("pmob");
    public static final SealSetting ANIMALS =
            new SealSetting("target_animals", "gui.thaumaturge.seal.setting.animal", false).readingAlso("panimal");
    public static final SealSetting PLAYERS =
            new SealSetting("target_players", "gui.thaumaturge.seal.setting.player", false).readingAlso("pplayer");

    private static final int STAGGER = 22;
    private static final int SCAN_PERIOD = 20;

    private final SealClock clock = new SealClock(STAGGER);

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        for (LivingEntity intruder : LevelSpaces.entitiesIn(
                level, LivingEntity.class, seal.pos().pos(), SealArea.bounds(seal), EntitySelector.NO_SPECTATORS)) {
            if (isQuarry(level, seal, intruder)) {
                mark(level, seal, intruder);
            }
        }
    }

    @Override
    protected boolean isQuarry(ServerLevel level, ISealEntity seal, LivingEntity target) {
        if (seal.setting(MONSTERS) && target instanceof Enemy) {
            return true;
        }
        if (seal.setting(ANIMALS) && (target instanceof Animal || target instanceof WaterAnimal)) {
            return true;
        }
        return seal.setting(PLAYERS) && level.getServer().isPvpAllowed() && target instanceof Player;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        Entity target = task.entity();
        return target != null && !golem.asEntity().isAlliedTo(target);
    }
}
