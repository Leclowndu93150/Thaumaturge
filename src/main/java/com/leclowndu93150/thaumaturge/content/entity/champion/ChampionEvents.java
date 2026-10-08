package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ChampionEvents {
    private static final int ROLL_BOUND = 100;
    private static final double MIN_CHAMPION_HEALTH = 10.0;
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
        boolean allowed = ThaumaturgeCommonConfig.ALLOW_CHAMPION_MOBS.get();
        int roll = mob.getRandom().nextInt(ROLL_BOUND);
        Level level = mob.level();
        if (level.getDifficulty() == Difficulty.EASY || !allowed) {
            roll += 2;
        }
        if (level.getDifficulty() == Difficulty.HARD && allowed) {
            roll -= 2;
        }
        Holder<Biome> biome = level.getBiome(mob.blockPosition());
        if (biome.is(TTBiomeTags.IS_SPOOKY) || level.dimension() == Level.NETHER || level.dimension() == Level.END) {
            roll -= allowed ? 2 : 1;
        }
        int whitelistBonus = 0;
        boolean whitelisted = false;
        Integer weight = mob.getType().builtInRegistryHolder().getData(ChampionDataMaps.CHAMPION_WHITELIST);
        if (weight != null) {
            whitelisted = true;
            if (allowed) {
                whitelistBonus = Math.max(whitelistBonus, weight - 1);
            }
        }
        roll -= whitelistBonus;
        if (whitelisted && roll <= 0 && mob.getAttributeBaseValue(Attributes.MAX_HEALTH) >= MIN_CHAMPION_HEALTH) {
            ChampionHelper.makeChampion(mob, false);
        } else {
            ChampionHelper.markRolled(mob);
        }
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
        Entity killer = event.getSource().getEntity();
        if (killer instanceof FakePlayer) {
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
        if (entity.getKillCredit() instanceof Player player) {
            params.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());
        }
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
}
