package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.crucible.CrucibleEvent;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ResearchProgressionEvents {
    private static final int MILESTONE_CHECK_INTERVAL = 200;
    private static final int WALK_MILESTONE_CM = 160000;
    private static final int SPRINT_MILESTONE_CM = 80000;
    private static final int JUMP_MILESTONE = 500;
    private static final int SWIM_MILESTONE_CM = 8000;
    private static final int DEEP_DOWN_DEPTH = 10;
    private static final double UP_HIGH_FRACTION = 0.4;

    private static final ResourceLocation GOT_CRYSTALS = TTIds.rl("gotcrystals");
    private static final ResourceLocation GOT_DREAM = TTIds.rl("gotdream");
    private static final ResourceLocation GOT_THAUMONOMICON = TTIds.rl("gotthaumonomicon");
    private static final ResourceLocation UNLOCK_AUROMANCY = TTIds.rl("unlock_auromancy");
    private static final ResourceLocation BASE_AUROMANCY = TTIds.rl("base_auromancy");
    private static final ResourceLocation F_ONFIRE = TTIds.rl("f_onfire");
    private static final ResourceLocation FOCUS_PROJECTILE = TTIds.rl("focus_projectile");
    private static final ResourceLocation F_ARROW = TTIds.rl("f_arrow");
    private static final ResourceLocation F_FIREBALL = TTIds.rl("f_fireball");
    private static final ResourceLocation F_SPIT = TTIds.rl("f_spit");

    private ResearchProgressionEvents() {}

    @SubscribeEvent
    public static void onCrucibleCraft(CrucibleEvent.Crafted event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            recordCrafted(player, event.getResult());
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            recordCrafted(player, event.getCrafting());
        }
    }

    public static void recordCrafted(ServerPlayer player, ItemStack crafted) {
        if (crafted.isEmpty()) return;
        ResourceLocation itemId = crafted.getItem()
                .builtInRegistryHolder()
                .unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
        if (itemId == null) return;
        PlayerKnowledge knowledge = (PlayerKnowledge) PlayerResearchAccess.of(player);
        ResourceLocation craftedKey = ResearchManager.craftedKey(itemId);
        if (knowledge.knows(craftedKey)) return;
        if (!isCraftReference(player, crafted)) return;
        knowledge.learn(craftedKey);
        knowledge.queueSync(player);
    }

    private static boolean isCraftReference(ServerPlayer player, ItemStack crafted) {
        return CraftReferenceHolder.isReference(player.registryAccess(), crafted.getItem());
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getOriginalStack();
        recordCrafted(player, stack);
        PlayerKnowledge knowledge = (PlayerKnowledge) PlayerResearchAccess.of(player);
        if (stack.is(TTItems.ESSENTIA_CRYSTAL.get())) {
            grantCrystalDiscovery(player, knowledge);
        }
        if (stack.is(TTItems.THAUMONOMICON.get()) && !knowledge.knows(GOT_THAUMONOMICON)) {
            knowledge.learn(GOT_THAUMONOMICON);
            knowledge.markComplete(GOT_THAUMONOMICON);
            knowledge.queueSync(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCrystalBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()
                || !(event.getPlayer() instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || !(event.getState().getBlock() instanceof BlockCrystal)) {
            return;
        }
        grantCrystalDiscovery(player, (PlayerKnowledge) PlayerResearchAccess.of(player));
    }

    private static void grantCrystalDiscovery(ServerPlayer player, PlayerKnowledge knowledge) {
        if (knowledge.knows(GOT_CRYSTALS)) return;
        knowledge.learn(GOT_CRYSTALS);
        knowledge.markComplete(GOT_CRYSTALS);
        knowledge.queueSync(player);
        player.sendSystemMessage(
                Component.translatable("message.thaumaturge.discovery.crystals").withStyle(ChatFormatting.DARK_PURPLE));
        if (ThaumaturgeCommonConfig.NO_SLEEP.get() && !knowledge.knows(GOT_DREAM)) {
            giveDreamJournal(player, knowledge);
        }
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerKnowledge knowledge = (PlayerKnowledge) PlayerResearchAccess.of(player);
        if (knowledge.knows(GOT_CRYSTALS) && !knowledge.knows(GOT_DREAM)) {
            giveDreamJournal(player, knowledge);
        }
    }

    private static void giveDreamJournal(ServerPlayer player, PlayerKnowledge knowledge) {
        knowledge.learn(GOT_DREAM);
        knowledge.markComplete(GOT_DREAM);
        knowledge.queueSync(player);
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(
                DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(
                        Filterable.passThrough(Component.translatable("book.thaumaturge.start.title")
                                .getString()),
                        player.getName().getString(),
                        WrittenBookContent.MAX_GENERATION,
                        List.of(
                                Filterable.passThrough(Component.translatable("book.thaumaturge.start.1")),
                                Filterable.passThrough(Component.translatable("book.thaumaturge.start.2")),
                                Filterable.passThrough(Component.translatable("book.thaumaturge.start.3"))),
                        false));
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
        player.sendSystemMessage(
                Component.translatable("message.thaumaturge.discovery.dream").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @SubscribeEvent
    public static void onFireDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getSource().is(DamageTypeTags.IS_FIRE)) return;
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        if (!knowledge.hasReachedStage(BASE_AUROMANCY, 1) && !knowledge.hasCompleted(BASE_AUROMANCY)) return;
        if (knowledge.knows(F_ONFIRE)) return;
        PlayerKnowledge pk = (PlayerKnowledge) knowledge;
        pk.learn(F_ONFIRE);
        pk.markComplete(F_ONFIRE);
        pk.queueSync(player);
        sendActionBar(player, "message.thaumaturge.discovery.on_fire");
    }

    @SubscribeEvent
    public static void onProjectileDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation research = projectileResearch(event.getSource().getDirectEntity());
        if (research == null) return;
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        if (!knowledge.hasReachedStage(FOCUS_PROJECTILE, 1) && !knowledge.hasCompleted(FOCUS_PROJECTILE)) return;
        if (knowledge.knows(research)) return;
        PlayerKnowledge pk = (PlayerKnowledge) knowledge;
        pk.learn(research);
        pk.markComplete(research);
        pk.queueSync(player);
        sendActionBar(player, "message.thaumaturge.discovery.projectile");
    }

    private static @Nullable ResourceLocation projectileResearch(@Nullable Entity direct) {
        if (direct instanceof AbstractArrow) return F_ARROW;
        if (direct instanceof AbstractHurtingProjectile) return F_FIREBALL;
        if (direct instanceof LlamaSpit) return F_SPIT;
        return null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount == 0) return;
        PlayerKnowledge knowledge = (PlayerKnowledge) PlayerResearchAccess.of(player);
        boolean auromancyInProgress = knowledge.knows(UNLOCK_AUROMANCY)
                && !knowledge.hasReachedStage(UNLOCK_AUROMANCY, 1)
                && !knowledge.hasCompleted(UNLOCK_AUROMANCY);
        if (auromancyInProgress) {
            milestone(
                    player,
                    knowledge,
                    TTIds.rl("m_deepdown"),
                    "message.thaumaturge.discovery.deep_down",
                    player.getY() < player.level().getMinBuildHeight() + DEEP_DOWN_DEPTH);
            milestone(
                    player,
                    knowledge,
                    TTIds.rl("m_uphigh"),
                    "message.thaumaturge.discovery.up_high",
                    player.getY() > player.level().getMaxBuildHeight() * UP_HIGH_FRACTION);
        }
        if (player.tickCount % MILESTONE_CHECK_INTERVAL != 0) return;
        if (player.level().hasChunkAt(player.blockPosition())) {
            Holder<Biome> biome = player.level().getBiome(player.blockPosition());
            milestone(
                    player,
                    knowledge,
                    TTIds.rl("m_hellandback"),
                    "message.thaumaturge.discovery.hell_and_back",
                    biome.is(BiomeTags.IS_NETHER));
            milestone(
                    player,
                    knowledge,
                    TTIds.rl("m_endoftheworld"),
                    "message.thaumaturge.discovery.end_of_the_world",
                    biome.is(BiomeTags.IS_END));
        }
        milestone(
                player,
                knowledge,
                TTIds.rl("m_walker"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)) > WALK_MILESTONE_CM);
        milestone(
                player,
                knowledge,
                TTIds.rl("m_runner"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)) > SPRINT_MILESTONE_CM);
        milestone(
                player,
                knowledge,
                TTIds.rl("m_jumper"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.JUMP)) > JUMP_MILESTONE);
        milestone(
                player,
                knowledge,
                TTIds.rl("m_swimmer"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.SWIM_ONE_CM)) > SWIM_MILESTONE_CM);
    }

    private static void milestone(
            ServerPlayer player, PlayerKnowledge knowledge, ResourceLocation id, String messageKey, boolean condition) {
        if (!condition || knowledge.knows(id)) return;
        knowledge.learn(id);
        knowledge.markComplete(id);
        knowledge.queueSync(player);
        if (messageKey != null) {
            sendActionBar(player, messageKey);
        }
    }

    private static void sendActionBar(ServerPlayer player, String key) {
        player.connection.send(new ClientboundSetActionBarTextPacket(
                Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE)));
    }
}
