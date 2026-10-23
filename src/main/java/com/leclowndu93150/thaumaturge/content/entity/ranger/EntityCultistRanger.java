package com.leclowndu93150.thaumaturge.content.entity.ranger;

import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;

public class EntityCultistRanger extends AbstractCultistRanger {
    private static final double MAX_HEALTH = 30.0;
    private static final double MELEE_SPEED = 1.0;
    private static final int MELEE_PRIORITY = 2;

    public EntityCultistRanger(EntityType<? extends EntityCultistRanger> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createCultistAttributes(MAX_HEALTH);
    }

    @Override
    public boolean isPatrolLeader() {
        return false;
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MELEE_SPEED, false));
    }

    @Override
    protected ResourceKey<LootTable> equipmentTable() {
        return TTLootTables.EQUIPMENT_CULTIST_RANGER;
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(
            ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        this.enchantSpawnedWeapon(level, random, difficulty);
    }
}
