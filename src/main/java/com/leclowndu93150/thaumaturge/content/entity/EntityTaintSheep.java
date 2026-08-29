package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintMobCombat;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class EntityTaintSheep extends Sheep implements ITaintedMob {
    public EntityTaintSheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Sheep.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        // Sheep.customServerAiStep() unconditionally dereferences Sheep's private eatBlockGoal.
        // Let vanilla initialize that mandatory field, then replace the actual selectors with
        // the hostile tainted behavior. Removing the vanilla EatBlockGoal from the selector is
        // safe; the field remains non-null for Sheep's inherited animation bookkeeping.
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);

        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new ConvertGrassGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            TaintMobCombat.maybeApplyFluxTaint(this, living);
        }
        return hit;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    /** TC4 AIConvertGrass: roughly 1/250 AI checks, 40-tick grazing animation, then Fibre + Tainted Lands. */
    private static final class ConvertGrassGoal extends Goal {
        private final EntityTaintSheep sheep;
        private int timer;

        private ConvertGrassGoal(EntityTaintSheep sheep) {
            this.sheep = sheep;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!(sheep.level() instanceof ServerLevel level)
                    || sheep.getRandom().nextInt(250) != 0) {
                return false;
            }
            BlockPos pos = sheep.blockPosition();
            return level.getBlockState(pos).is(Blocks.SHORT_GRASS)
                    || level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK);
        }

        @Override
        public void start() {
            timer = 40;
            sheep.level().broadcastEntityEvent(sheep, (byte) 10);
            sheep.getNavigation().stop();
        }

        @Override
        public void stop() {
            timer = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return timer > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            timer = Math.max(0, timer - 1);
            if (timer != 4 || !(sheep.level() instanceof ServerLevel level)) {
                return;
            }
            BlockPos pos = sheep.blockPosition();
            if (level.getBlockState(pos).is(Blocks.SHORT_GRASS)) {
                level.levelEvent(2001, pos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                level.destroyBlock(pos, false);
                infect(level, pos);
            } else if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
                level.levelEvent(2001, pos.below(), Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                infect(level, pos);
            }
        }

        private void infect(ServerLevel level, BlockPos pos) {
            TaintBiomeManager.taintColumn(level, pos);
            if (level.getBlockState(pos).canBeReplaced() && BlockTaintFibre.hasSolidAttachment(level, pos)) {
                level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
            }
            sheep.ate();
        }
    }
}
