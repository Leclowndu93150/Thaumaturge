package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistKnight;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityCultistPortalGreater extends EntityThaumaturgeBoss {
    private static final byte PULSE_EVENT = 16;
    private static final int PORTAL_XP = 30;
    private static final double ACTIVATION_RANGE = 48.0;
    private static final double MINION_SCAN_RANGE = 32.0;
    private static final int MINION_TIMING_PER_CULTIST = 20;
    private static final int BOSS_STAGE = 12;
    private static final float KNIGHT_CHANCE = 0.67F;
    private static final int BANNER_DISTANCE = 6;
    private static final int BANNER_STAGE_TICK = 160;
    private static final int CRATE_WINDOW_MAX = 150;
    private static final int CRATE_WINDOW_MIN = 20;
    private static final int CRATE_INTERVAL = 13;
    private static final int CRATE_SPREAD = 5;
    private static final float CRATE_RARE_CHANCE = 0.05F;
    private static final float CRATE_UNCOMMON_CHANCE = 0.2F;
    private static final float TOUCH_DAMAGE = 8.0F;
    private static final double TOUCH_RANGE_SQ = 3.0;
    private static final int OVERSPAWN_DAMAGE_BASE = 5;
    private static final float DEATH_EXPLOSION_POWER = 2.0F;
    private static final int ARC_COLOR = 0xBB2222;

    private int stage;
    private int stageCounter = 200;
    public int pulse;

    public EntityCultistPortalGreater(EntityType<? extends EntityCultistPortalGreater> type, Level level) {
        super(type, level);
        this.xpReward = PORTAL_XP;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.ATTACK_DAMAGE, 0.0).add(Attributes.ARMOR, 5.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("stage", this.stage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.stage = input.getIntOr("stage", 0);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {}

    @Override
    public void aiStep() {
        if (this.pulse > 0) {
            this.pulse--;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel server = (ServerLevel) this.level();
        if (this.stageCounter <= 0) {
            if (this.level().getNearestPlayer(this, ACTIVATION_RANGE) != null) {
                this.level().broadcastEntityEvent(this, PULSE_EVENT);
                if (this.stage <= 4) {
                    this.stageCounter = 15 + this.random.nextInt(10 - this.stage) - this.stage;
                    this.spawnMinions(server);
                } else if (this.stage == BOSS_STAGE) {
                    this.stageCounter = 50 + this.getTiming() * 2 + this.random.nextInt(50);
                    this.spawnBoss(server);
                } else {
                    int timing = this.getTiming();
                    this.stageCounter = timing + this.random.nextInt(5 + timing / 3);
                    this.spawnMinions(server);
                }
                this.stage++;
            } else {
                this.stageCounter = 30 + this.random.nextInt(30);
            }
        } else {
            this.stageCounter--;
            if (this.stageCounter == BANNER_STAGE_TICK && this.stage == 0) {
                this.level().broadcastEntityEvent(this, PULSE_EVENT);
                this.placeBanners(server);
            }
            if (this.stageCounter > CRATE_WINDOW_MIN && this.stageCounter < CRATE_WINDOW_MAX && this.stage == 0 && this.stageCounter % CRATE_INTERVAL == 0) {
                this.placeCrate(server);
            }
        }
        if (this.stage < BOSS_STAGE) {
            this.heal(1.0F);
        }
    }

    private void placeBanners(ServerLevel server) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos pos = new BlockPos((int) this.getX() - dir.getStepX() * BANNER_DISTANCE, (int) this.getY(), (int) this.getZ() + dir.getStepZ() * BANNER_DISTANCE);
            int rotation = switch (dir) {
                case NORTH -> 8;
                case WEST -> 12;
                case EAST -> 4;
                default -> 0;
            };
            BlockState banner = TCBlocks.BANNER_CRIMSON_CULT.get().defaultBlockState().setValue(BlockStateProperties.ROTATION_16, rotation);
            server.setBlock(pos, banner, Block.UPDATE_ALL);
            Effects.arcBolt(server, this.position().add(0.0, this.getBbHeight() / 2.0, 0.0)).to(Vec3.atCenterOf(pos)).color(ARC_COLOR).send();
            this.playSound(TCSounds.WANDFAIL.get(), 1.0F, 1.0F);
        }
    }

    private void placeCrate(ServerLevel server) {
        int x = (int) this.getX() + this.random.nextInt(CRATE_SPREAD) - this.random.nextInt(CRATE_SPREAD);
        int z = (int) this.getZ() + this.random.nextInt(CRATE_SPREAD) - this.random.nextInt(CRATE_SPREAD);
        BlockPos pos = new BlockPos(x, (int) this.getY(), z);
        if (x == (int) this.getX() || z == (int) this.getZ() || !server.isEmptyBlock(pos)) {
            return;
        }
        this.level().broadcastEntityEvent(this, PULSE_EVENT);
        float roll = this.random.nextFloat();
        Block crate = TCBlocks.LOOT_CRATE_COMMON.get();
        if (roll < CRATE_RARE_CHANCE) {
            crate = TCBlocks.LOOT_CRATE_RARE.get();
        } else if (roll < CRATE_UNCOMMON_CHANCE) {
            crate = TCBlocks.LOOT_CRATE_UNCOMMON.get();
        }
        server.setBlock(pos, crate.defaultBlockState(), Block.UPDATE_ALL);
        Effects.arcBolt(server, this.position().add(0.0, this.getBbHeight() / 2.0, 0.0)).to(Vec3.atCenterOf(pos)).color(ARC_COLOR).send();
        this.playSound(TCSounds.WANDFAIL.get(), 1.0F, 1.0F);
    }

    private int getTiming() {
        return this.level().getEntitiesOfClass(EntityCultist.class, this.getBoundingBox().inflate(MINION_SCAN_RANGE)).size() * MINION_TIMING_PER_CULTIST;
    }

    private void spawnMinions(ServerLevel server) {
        EntityCultist cultist = this.random.nextFloat() < KNIGHT_CHANCE
                ? new EntityCultistKnight(TCEntities.CULTIST_KNIGHT.get(), server)
                : new EntityCultistCleric(TCEntities.CULTIST_CLERIC.get(), server);
        this.spawnCultist(server, cultist);
        if (this.stage > BOSS_STAGE) {
            this.hurtServer(server, this.damageSources().fellOutOfWorld(), OVERSPAWN_DAMAGE_BASE + this.random.nextInt(5));
        }
    }

    private void spawnBoss(ServerLevel server) {
        EntityCultistLeader leader = new EntityCultistLeader(TCEntities.CULTIST_LEADER.get(), server);
        this.spawnCultist(server, leader);
    }

    private void spawnCultist(ServerLevel server, Mob cultist) {
        cultist.setPos(this.getX() + this.random.nextFloat() - this.random.nextFloat(), this.getY() + 0.25, this.getZ() + this.random.nextFloat() - this.random.nextFloat());
        cultist.finalizeSpawn(server, server.getCurrentDifficultyAt(cultist.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        cultist.setHomeTo(this.blockPosition(), 32);
        server.addFreshEntity(cultist);
        if (cultist instanceof EntityCultist minion) {
            minion.spawnCultistArrivalParticles();
        }
        cultist.playSound(TCSounds.WANDFAIL.get(), 1.0F, 1.0F);
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide() || this.distanceToSqr(player) >= TOUCH_RANGE_SQ) {
            return;
        }
        if (player.hurtServer((ServerLevel) this.level(), this.damageSources().indirectMagic(this, this), TOUCH_DAMAGE)) {
            this.playSound(TCSounds.ZAP.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F + 1.0F);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.75F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 540;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.MONOLITH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.ZAP.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.SHOCK.get();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        level.addFreshEntity(new EntitySpecialItem(level, this.getX(), this.getY() + this.getBbHeight() / 2.0F, this.getZ(), new ItemStack(TCItems.PRIMORDIAL_PEARL.get())));
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == PULSE_EVENT) {
            this.pulse = 10;
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, @Nullable Entity source) {
        return false;
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), DEATH_EXPLOSION_POWER, false, Level.ExplosionInteraction.NONE);
        }
        super.die(source);
    }
}
