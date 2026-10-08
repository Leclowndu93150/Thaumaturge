package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterScaling;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchSpawns;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

final class EncounterSpawner {
    private EncounterSpawner() {}

    static Optional<Mob> spawn(
            ServerLevel level,
            MazeRecord record,
            EntityType<?> type,
            BlockPos pos,
            EncounterRole role,
            EncounterScaling scaling,
            int participants) {
        Optional<Mob> prepared = EldritchSpawns.prepare(
                level,
                type,
                pos,
                EncounterGeometry.anchor(record),
                ThaumaturgeServerConfig.LABYRINTH.bossLeashRadius.get(),
                level.getRandom());
        if (prepared.isEmpty()) {
            return Optional.empty();
        }
        Mob mob = prepared.get();
        mob.setData(
                TTAttachments.LABYRINTH_BINDING,
                LabyrinthBinding.of(record.plan().id(), role));
        scale(mob, scaling, participants, true);
        if (!level.addFreshEntity(mob)) {
            return Optional.empty();
        }
        record.state().encounter().add(BoundEntity.spawned(mob.getUUID(), type, role));
        return Optional.of(mob);
    }

    static void scale(LivingEntity entity, EncounterScaling scaling, int participants, boolean fill) {
        int counted = ThaumaturgeServerConfig.LABYRINTH.scaleWithParticipants.get() ? participants : 1;
        float ratio = entity.getMaxHealth() > 0.0F ? entity.getHealth() / entity.getMaxHealth() : 1.0F;
        modify(entity, Attributes.MAX_HEALTH, scaling.healthMultiplier(counted) - 1.0F);
        modify(entity, Attributes.ATTACK_DAMAGE, scaling.damageMultiplier(counted) - 1.0F);
        entity.setHealth(fill ? entity.getMaxHealth() : entity.getMaxHealth() * ratio);
    }

    private static void modify(LivingEntity entity, Holder<Attribute> attribute, float amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount <= 0.0F) {
            instance.removeModifier(TTIds.LABYRINTH_SCALING);
            return;
        }
        instance.addOrReplacePermanentModifier(new AttributeModifier(
                TTIds.LABYRINTH_SCALING, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
