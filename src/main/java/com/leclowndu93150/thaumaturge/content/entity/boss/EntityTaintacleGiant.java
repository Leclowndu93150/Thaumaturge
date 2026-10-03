package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.entity.ISidedHurt;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public class EntityTaintacleGiant extends AbstractTaintacle implements IEldritchMob, ISidedHurt {
    private static final EntityDataAccessor<Integer> DATA_AGGRO =
            SynchedEntityData.defineId(EntityTaintacleGiant.class, EntityDataSerializers.INT);

    private static final int GIANT_XP = 20;
    private static final int HEAL_INTERVAL = 30;
    private static final int ANGRY_PARTICLE_CHANCE = 15;
    private static final float ENRAGE_THRESHOLD = 35.0F;
    private static final int ENRAGE_TICKS = 200;
    private static final double LONELY_RANGE = 48.0;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public EntityTaintacleGiant(EntityType<? extends EntityTaintacleGiant> type, Level level) {
        super(type, level);
        this.xpReward = GIANT_XP;
        this.bossEvent.setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTaintacleAttributes(175.0, 9.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_AGGRO, 0);
    }

    public int getAnger() {
        return this.entityData.get(DATA_AGGRO);
    }

    public void setAnger(int anger) {
        this.entityData.set(DATA_AGGRO, anger);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity() instanceof EntityTaintacleGiant giant) {
            ChampionHelper.makeChampion(giant, true);
            event.setCanceled(true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getAnger() > 0 && !this.level().isClientSide()) {
            this.setAnger(this.getAnger() - 1);
        }
        if (this.level().isClientSide() && this.getAnger() > 0 && this.random.nextInt(ANGRY_PARTICLE_CHANCE) == 0) {
            this.level()
                    .addParticle(
                            ParticleTypes.ANGRY_VILLAGER,
                            this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                            this.getBoundingBox().minY + this.getBbHeight() + this.random.nextFloat() * 0.5,
                            this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                            this.random.nextGaussian() * 0.02,
                            this.random.nextGaussian() * 0.02,
                            this.random.nextGaussian() * 0.02);
        }
        if (!this.level().isClientSide() && this.tickCount % HEAL_INTERVAL == 0) {
            this.heal(1.0F);
        }
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) level();
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return hurtSided(level(), source, damage);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage > ENRAGE_THRESHOLD) {
            if (this.getAnger() == 0) {
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ENRAGE_TICKS, (int) (damage / 15.0F)));
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ENRAGE_TICKS, (int) (damage / 10.0F)));
                this.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, ENRAGE_TICKS, (int) (damage / 40.0F)));
                this.setAnger(ENRAGE_TICKS);
                if (source.getEntity() instanceof ServerPlayer player) {
                    player.connection.send(new ClientboundSetActionBarTextPacket(Component.empty()
                            .append(this.getDisplayName())
                            .append(" ")
                            .append(Component.translatable("tc.boss.enrage"))));
                }
            }
            damage = ENRAGE_THRESHOLD;
        }
        return super.hurt(source, damage);
    }

    @Override
    public boolean hurtClient(DamageSource source, float damage) {
        return super.hurt(source, damage);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (level.getEntitiesOfClass(
                        EntityTaintacleGiant.class, this.getBoundingBox().inflate(LONELY_RANGE), other -> other != this)
                .isEmpty()) {
            level.addFreshEntity(new EntitySpecialItem(
                    level,
                    this.getX(),
                    this.getY() + this.getBbHeight() / 2.0F,
                    this.getZ(),
                    new ItemStack(TCItems.PRIMORDIAL_PEARL.get())));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
