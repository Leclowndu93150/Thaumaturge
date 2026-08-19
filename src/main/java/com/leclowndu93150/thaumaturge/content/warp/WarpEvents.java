package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import java.util.List;
import java.util.function.IntPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class WarpEvents {
    private static final int GRINNING_DEVIL_MASK = 0;
    private static final int GRINNING_DEVIL_BASE_REDUCTION = 2;
    private static final int GRINNING_DEVIL_RANDOM_REDUCTION = 4;

    private static final int EFFECT_AMP_DIVISOR = 15;
    private static final int MAX_EFFECT_AMP = 3;
    private static final int MAX_GUARDIANS = 8;
    private static final int MAX_SPIDERS = 50;
    private static final int SPAWN_ATTEMPTS = 50;

    private static final int BATH_SALTS_WARP_THRESHOLD = 10;
    private static final int ELDRITCH_MINOR_WARP_THRESHOLD = 25;
    private static final int ELDRITCH_MAJOR_WARP_THRESHOLD = 50;
    private static final Identifier BATH_SALTS_ENTRY = TCIds.rl("bath_salts");
    private static final Identifier BATHSALTS_FLAG = TCIds.rl("bathsalts");
    private static final Identifier ELDRITCH_MINOR_FLAG = TCIds.rl("eldritchminor");
    private static final Identifier ELDRITCH_MAJOR_FLAG = TCIds.rl("eldritchmajor");

    @FunctionalInterface
    private interface Action {
        void apply(ServerPlayer player, Roll roll);
    }

    private record Roll(int warp, int normalWarp, RandomSource rand) {
    }

    private record Event(IntPredicate matches, Action action) {
    }

    private static final List<Event> EVENTS = List.of(upTo(4, (player, roll) -> creeperHiss(player)), upTo(8, (player, roll) -> distantExplosion(player, roll.rand())),
            message(12, "warp.thaumaturge.text.11"), upTo(16, (player, roll) -> applyEffect(player, TCMobEffects.VIS_EXHAUST, 5000, ampFor(roll.warp()), "warp.thaumaturge.text.1")),
            upTo(20, (player, roll) -> applyEffect(player, TCMobEffects.THAUMARHIA, Math.min(32000, 10 * roll.warp()), 0, "warp.thaumaturge.text.15")),
            upTo(24, (player, roll) -> applyEffect(player, TCMobEffects.UNNATURAL_HUNGER, 5000, ampFor(roll.warp()), "warp.thaumaturge.text.2")), message(28, "warp.thaumaturge.text.12"),
            upTo(32, (player, roll) -> spawnMist(player, 1)), upTo(36, (player, roll) -> applyEffect(player, TCMobEffects.BLURRED_VISION, Math.min(32000, 10 * roll.warp()), 0, null)),
            upTo(40, (player, roll) -> applyEffect(player, TCMobEffects.SUN_SCORNED, 5000, ampFor(roll.warp()), "warp.thaumaturge.text.5")),
            upTo(44, (player, roll) -> applyEffect(player, MobEffects.MINING_FATIGUE, 1200, ampFor(roll.warp()), "warp.thaumaturge.text.9")),
            upTo(48, (player, roll) -> applyEffect(player, TCMobEffects.INFECTIOUS_VIS_EXHAUST, 6000, ampFor(roll.warp()), "warp.thaumaturge.text.1")),
            upTo(52, (player, roll) -> applyEffect(player, MobEffects.NIGHT_VISION, Math.min(40 * roll.warp(), 6000), 0, "warp.thaumaturge.text.10")),
            upTo(56, (player, roll) -> applyEffect(player, TCMobEffects.DEATH_GAZE, 6000, ampFor(roll.warp()), "warp.thaumaturge.text.4")),
            upTo(60, (player, roll) -> suddenlySpiders(player, roll.warp(), false)), message(64, "warp.thaumaturge.text.13"), upTo(68, (player, roll) -> spawnMist(player, roll.warp() / 30)),
            upTo(72, (player, roll) -> applyEffect(player, MobEffects.BLINDNESS, Math.min(32000, 5 * roll.warp()), 0, null)), exactly(76, WarpEvents::easeNormalWarp),
            upTo(80, (player, roll) -> applyEffect(player, TCMobEffects.UNNATURAL_HUNGER, 6000, ampFor(roll.warp()), "warp.thaumaturge.text.2")),
            upTo(88, (player, roll) -> GuardianSpawner.spawnPortal(player)), upTo(92, (player, roll) -> suddenlySpiders(player, roll.warp(), true)),
            new Event(eff -> true, (player, roll) -> spawnMist(player, roll.warp() / 15)));

    private WarpEvents() {}

    public static void checkWarpEvent(ServerPlayer player) {
        WarpData wc = WarpManager.data(player);
        WarpManager.addWarp(player, -1, WarpType.TEMPORARY);
        int tw = wc.get(WarpType.TEMPORARY);
        int nw = wc.get(WarpType.NORMAL);
        int pw = wc.get(WarpType.PERMANENT);
        int warp = tw + nw + pw;
        int gearWarp = WarpManager.getWarpFromGear(player);
        warp += gearWarp;
        int warpCounter = wc.getCounter();
        RandomSource rand = player.getRandom();
        int r = rand.nextInt(100);
        if (warpCounter <= 0 || warp <= 0 || r > Math.sqrt(warpCounter)) {
            return;
        }
        warp = Math.min(100, (warp + warp + warpCounter) / 3);
        warpCounter = (int) (warpCounter - Math.max(5.0, Math.sqrt(warpCounter) * 2.0 - gearWarp * 2));
        wc.setCounter(warpCounter);
        int eff = rand.nextInt(warp) + gearWarp;
        ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helm.getItem() instanceof FortressArmorItem && FortressArmorItem.mask(helm) == GRINNING_DEVIL_MASK) {
            eff -= GRINNING_DEVIL_BASE_REDUCTION + rand.nextInt(GRINNING_DEVIL_RANDOM_REDUCTION);
        }
        PacketDistributor.sendToPlayer(player, ClientboundWarpFXPayload.heartbeat());
        if (eff > 0) {
            dispatchEvent(player, eff, new Roll(warp, nw, rand));
        }
        checkWarpMilestones(player, nw + pw);
    }

    private static void dispatchEvent(ServerPlayer player, int eff, Roll roll) {
        for (Event event : EVENTS) {
            if (event.matches().test(eff)) {
                event.action().apply(player, roll);
                return;
            }
        }
    }

    private static Event upTo(int bound, Action action) {
        return new Event(eff -> eff <= bound, action);
    }

    private static Event exactly(int value, Action action) {
        return new Event(eff -> eff == value, action);
    }

    private static Event message(int bound, String key) {
        return upTo(bound, (player, roll) -> WarpManager.sendActionBar(player, key));
    }

    private static void checkWarpMilestones(ServerPlayer player, int actualWarp) {
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (actualWarp > BATH_SALTS_WARP_THRESHOLD && !knowledge.isResearchKnown(BATH_SALTS_ENTRY) && !knowledge.isResearchKnown(BATHSALTS_FLAG)) {
            WarpManager.sendActionBar(player, "warp.thaumaturge.text.8");
            ResearchManager.complete(player, BATHSALTS_FLAG);
        }
        if (actualWarp > ELDRITCH_MINOR_WARP_THRESHOLD && !knowledge.isResearchKnown(ELDRITCH_MINOR_FLAG)) {
            ResearchManager.complete(player, ELDRITCH_MINOR_FLAG);
        }
        if (actualWarp > ELDRITCH_MAJOR_WARP_THRESHOLD && !knowledge.isResearchKnown(ELDRITCH_MAJOR_FLAG)) {
            ResearchManager.complete(player, ELDRITCH_MAJOR_FLAG);
        }
    }

    private static void creeperHiss(ServerPlayer player) {
        if (!ThaumaturgeCommonConfig.NO_STRESS.get()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CREEPER_PRIMED, SoundSource.AMBIENT, 1.0F, 0.5F);
        }
    }

    private static void distantExplosion(ServerPlayer player, RandomSource rand) {
        if (!ThaumaturgeCommonConfig.NO_STRESS.get()) {
            player.level().playSound(null, player.getX() + rand.nextInt(10) - rand.nextInt(10), player.getY() + rand.nextInt(10) - rand.nextInt(10),
                    player.getZ() + rand.nextInt(10) - rand.nextInt(10), SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }

    private static void easeNormalWarp(ServerPlayer player, Roll roll) {
        if (roll.normalWarp() > 0) {
            WarpManager.addWarp(player, -1, WarpType.NORMAL);
        }
        WarpManager.sendActionBar(player, "warp.thaumaturge.text.14");
    }

    private static int ampFor(int warp) {
        return Math.min(MAX_EFFECT_AMP, warp / EFFECT_AMP_DIVISOR);
    }

    private static void applyEffect(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier, @Nullable String messageKey) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
        if (messageKey != null) {
            WarpManager.sendActionBar(player, messageKey);
        }
    }

    private static void spawnMist(ServerPlayer player, int guardians) {
        PacketDistributor.sendToPlayer(player, ClientboundWarpFXPayload.mist());
        int count = Math.min(MAX_GUARDIANS, guardians);
        GuardianSpawner.spawn(player, count);
        WarpManager.sendActionBar(player, "warp.thaumaturge.text.6");
    }

    private static void suddenlySpiders(ServerPlayer player, int warp, boolean real) {
        ServerLevel level = player.level();
        RandomSource rand = player.getRandom();
        int spawns = Math.min(MAX_SPIDERS, warp);
        for (int i = 0; i < spawns; i++) {
            for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
                EntityMindSpider spider = TCEntities.MIND_SPIDER.get().create(level, EntitySpawnReason.EVENT);
                if (spider == null) {
                    return;
                }
                double x = player.getX() + rand.nextInt(15) - rand.nextInt(15);
                double y = player.getY() + rand.nextInt(5) - rand.nextInt(5);
                double z = player.getZ() + rand.nextInt(15) - rand.nextInt(15);
                spider.snapTo(x, y, z, rand.nextFloat() * 360.0F, 0.0F);
                BlockPos below = spider.blockPosition().below();
                if (!level.getBlockState(below).isCollisionShapeFullBlock(level, below) || !level.noCollision(spider) || level.containsAnyLiquid(spider.getBoundingBox())) {
                    spider.discard();
                    continue;
                }
                spider.setTarget(player);
                if (!real) {
                    spider.setViewer(player.getGameProfile().name());
                    spider.setHarmless(true);
                }
                level.addFreshEntity(spider);
                break;
            }
        }
        WarpManager.sendActionBar(player, "warp.thaumaturge.text.7");
    }

    public static void checkDeathGaze(ServerPlayer player) {
        MobEffectInstance gaze = player.getEffect(TCMobEffects.DEATH_GAZE);
        if (gaze == null) {
            return;
        }
        ServerLevel level = player.level();
        int range = Math.min(8 + gaze.getAmplifier() * 3, 24);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range))) {
            if (entity == player || !entity.isAlive() || entity.hasEffect(MobEffects.WITHER)) {
                continue;
            }
            if (entity instanceof ServerPlayer && !level.isPvpAllowed()) {
                continue;
            }
            if (!player.hasLineOfSight(entity)) {
                continue;
            }
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
            if (entity instanceof Mob mob) {
                mob.setTarget(player);
            }
        }
    }
}
