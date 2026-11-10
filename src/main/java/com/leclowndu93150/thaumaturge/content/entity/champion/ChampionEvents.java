package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityThaumaturgeBoss;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ChampionEvents {
    private static final int ROLL_BOUND = 100;
    private static final double MIN_CHAMPION_HEALTH = 10.0;
    private static final int EASY_OR_DISABLED_PENALTY = 2;
    private static final int HARD_BONUS = 2;
    private static final int SPOOKY_BONUS = 2;
    private static final int SPOOKY_BONUS_DISABLED = 1;
    private static final int WHITELIST_FLOOR = 1;
    private static final int XP_BASE = 5;
    private static final int XP_SPREAD = 3;

    private ChampionEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()
                || !(event.getEntity() instanceof Monster mob)
                || mob instanceof EntityCultistPortalLesser
                || ChampionHelper.rolled(mob)) {
            return;
        }
        Level level = mob.level();
        if (winsRoll(level, mob)) {
            ChampionHelper.makeChampion(mob, false);
        } else {
            ChampionHelper.markRolled(mob);
        }
    }

    private static boolean winsRoll(Level level, Monster mob) {
        Integer weight =
                BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(mob.getType()).getData(ChampionDataMaps.CHAMPION_WHITELIST);
        if (weight == null || mob.getAttributeBaseValue(Attributes.MAX_HEALTH) < MIN_CHAMPION_HEALTH) {
            return false;
        }
        return mob.getRandom().nextInt(ROLL_BOUND)
                <= rollModifier(
                        ThaumaturgeCommonConfig.ALLOW_CHAMPION_MOBS.get(),
                        level.getDifficulty(),
                        isSpooky(level, mob),
                        weight,
                        isBoss(mob));
    }

    static int rollModifier(boolean enabled, Difficulty difficulty, boolean spooky, int weight, boolean boss) {
        int modifier = 0;
        if (!enabled || difficulty == Difficulty.EASY) {
            modifier -= EASY_OR_DISABLED_PENALTY;
        }
        if (enabled && difficulty == Difficulty.HARD) {
            modifier += HARD_BONUS;
        }
        if (spooky) {
            modifier += enabled ? SPOOKY_BONUS : SPOOKY_BONUS_DISABLED;
        }
        if ((enabled || boss) && weight > WHITELIST_FLOOR) {
            modifier += weight - WHITELIST_FLOOR;
        }
        return modifier;
    }

    private static boolean isBoss(Monster mob) {
        return mob instanceof EntityThaumaturgeBoss || mob.getType().is(Tags.EntityTypes.BOSSES);
    }

    private static boolean isSpooky(Level level, Monster mob) {
        return level.getBiome(mob.blockPosition()).is(TTBiomeTags.IS_SPOOKY)
                || level.dimension() == Level.NETHER
                || level.dimension() == Level.END;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!event.getEntity().level().isClientSide()
                && event.getEntity().getType().is(ThaumaturgeEntityTypeTags.ELDRITCH)) {
            ShieldChargeSound.playIfShielded(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel server)
                || !event.isRecentlyHit()
                || !MobTraits.isChampion(entity)) {
            return;
        }
        Player killer = eligibleKiller(entity);
        if (killer == null) {
            return;
        }
        int xp = XP_BASE + entity.getRandom().nextInt(XP_SPREAD);
        ExperienceOrb.award(server, entity.position(), xp);
        LootParams.Builder params = new LootParams.Builder(server)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                .withOptionalParameter(
                        LootContextParams.ATTACKING_ENTITY, event.getSource().getEntity())
                .withOptionalParameter(
                        LootContextParams.DIRECT_ATTACKING_ENTITY,
                        event.getSource().getDirectEntity());
        params.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer).withLuck(killer.getLuck());
        server.getServer()
                .reloadableRegistries()
                .getLootTable(TTLootTables.CHAMPION_BAG)
                .getRandomItems(
                        params.create(LootContextParamSets.ENTITY),
                        entity.getLootTableSeed(),
                        bag -> event.getDrops()
                                .add(new ItemEntity(
                                        server,
                                        entity.getX(),
                                        entity.getY() + entity.getEyeHeight(),
                                        entity.getZ(),
                                        bag)));
    }

    static Player eligibleKiller(LivingEntity entity) {
        return entity.getKillCredit() instanceof Player player && !(player instanceof FakePlayer) ? player : null;
    }
}
