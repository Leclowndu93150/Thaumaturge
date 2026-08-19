package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.CultistHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class EntityCultistKnight extends EntityCultist {
    private static final float SPECIAL_WEAPON_CHANCE_HARD = 0.05F;
    private static final float SPECIAL_WEAPON_CHANCE = 0.01F;
    private static final float ENCHANT_CHANCE = 0.25F;

    public EntityCultistKnight(EntityType<? extends EntityCultistKnight> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createCultistAttributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new CultistHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityEldritchGuardian.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }

    @Override
    protected void setLoot(DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CRIMSON_PLATE_HELM.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CRIMSON_PLATE_CHEST.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CRIMSON_PLATE_LEGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.CRIMSON_BOOTS.get()));
        float specialChance = this.level().getDifficulty() == Difficulty.HARD ? SPECIAL_WEAPON_CHANCE_HARD : SPECIAL_WEAPON_CHANCE;
        if (this.random.nextFloat() < specialChance) {
            if (this.random.nextInt(5) == 0) {
                this.setItemInHand(this.getUsedItemHand(), new ItemStack(TCItems.VOID_SWORD.get()));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CRIMSON_ROBE_HELM.get()));
            } else {
                this.setItemInHand(this.getUsedItemHand(), new ItemStack(TCItems.THAUMIUM_SWORD.get()));
                if (this.random.nextBoolean()) {
                    this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
        } else {
            this.setItemInHand(this.getUsedItemHand(), new ItemStack(Items.IRON_SWORD));
        }
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        float f = difficulty.getSpecialMultiplier();
        ItemStack weapon = this.getMainHandItem();
        if (!weapon.isEmpty() && random.nextFloat() < ENCHANT_CHANCE * f) {
            EnchantmentHelper.enchantItemFromProvider(weapon, level.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, random);
        }
    }
}
