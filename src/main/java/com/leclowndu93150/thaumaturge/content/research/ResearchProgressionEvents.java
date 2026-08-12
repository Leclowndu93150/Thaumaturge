package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.crucible.CrucibleEvent;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ResearchProgressionEvents {
    private static final int MILESTONE_CHECK_INTERVAL = 200;
    private static final int WALK_MILESTONE_CM = 160000;
    private static final int SPRINT_MILESTONE_CM = 80000;
    private static final int JUMP_MILESTONE = 500;
    private static final int SWIM_MILESTONE_CM = 8000;
    private static final int DEEP_DOWN_DEPTH = 10;
    private static final double UP_HIGH_FRACTION = 0.4;

    private static final Identifier GOT_CRYSTALS = TCIds.rl("gotcrystals");
    private static final Identifier GOT_DREAM = TCIds.rl("gotdream");
    private static final Identifier GOT_THAUMONOMICON = TCIds.rl("gotthaumonomicon");
    private static final Identifier UNLOCK_AUROMANCY = TCIds.rl("unlock_auromancy");
    private static final Identifier BASE_AUROMANCY = TCIds.rl("base_auromancy");
    private static final Identifier F_ONFIRE = TCIds.rl("f_onfire");

    private ResearchProgressionEvents() {}

    @SubscribeEvent
    public static void onCrucibleCraft(CrucibleEvent.CrucibleCraftedEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            recordCrafted(player, event.getCraftedStack());
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
        Identifier itemId = crafted.getItem()
                .builtInRegistryHolder()
                .unwrapKey()
                .map(ResourceKey::identifier)
                .orElse(null);
        if (itemId == null) return;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        Identifier craftedKey = ResearchManager.craftedKey(itemId);
        if (knowledge.isResearchKnown(craftedKey)) return;
        if (!isCraftReference(player, crafted)) return;
        knowledge.addResearch(craftedKey);
        knowledge.sync(player);
    }

    private static boolean isCraftReference(ServerPlayer player, ItemStack crafted) {
        return CraftReferenceHolder.isReference(player.registryAccess(), crafted.getItem());
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getOriginalStack();
        recordCrafted(player, stack);
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (stack.is(TCItems.ESSENTIA_CRYSTAL.get()) && !knowledge.isResearchKnown(GOT_CRYSTALS)) {
            knowledge.addResearch(GOT_CRYSTALS);
            knowledge.markComplete(GOT_CRYSTALS);
            knowledge.sync(player);
            player.sendSystemMessage(Component.translatable("got.crystals").withStyle(ChatFormatting.DARK_PURPLE));
            if (ThaumaturgeCommonConfig.NO_SLEEP.get() && !knowledge.isResearchKnown(GOT_DREAM)) {
                giveDreamJournal(player, knowledge);
            }
        }
        if (stack.is(TCItems.THAUMONOMICON.get()) && !knowledge.isResearchKnown(GOT_THAUMONOMICON)) {
            knowledge.addResearch(GOT_THAUMONOMICON);
            knowledge.markComplete(GOT_THAUMONOMICON);
            knowledge.sync(player);
        }
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (knowledge.isResearchKnown(GOT_CRYSTALS) && !knowledge.isResearchKnown(GOT_DREAM)) {
            giveDreamJournal(player, knowledge);
        }
    }

    private static void giveDreamJournal(ServerPlayer player, PlayerKnowledge knowledge) {
        knowledge.addResearch(GOT_DREAM);
        knowledge.markComplete(GOT_DREAM);
        knowledge.sync(player);
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
        player.sendSystemMessage(Component.translatable("got.dream").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @SubscribeEvent
    public static void onFireDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getSource().is(DamageTypeTags.IS_FIRE)) return;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(BASE_AUROMANCY, 1) && !knowledge.isResearchComplete(BASE_AUROMANCY)) return;
        if (knowledge.isResearchKnown(F_ONFIRE)) return;
        PlayerKnowledge pk = (PlayerKnowledge) knowledge;
        pk.addResearch(F_ONFIRE);
        pk.markComplete(F_ONFIRE);
        pk.sync(player);
        sendActionBar(player, "got.onfire");
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount == 0) return;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        boolean auromancyInProgress = knowledge.isResearchKnown(UNLOCK_AUROMANCY)
                && !knowledge.isResearchKnown(UNLOCK_AUROMANCY, 1)
                && !knowledge.isResearchComplete(UNLOCK_AUROMANCY);
        if (auromancyInProgress) {
            milestone(
                    player,
                    knowledge,
                    TCIds.rl("m_deepdown"),
                    "got.deepdown",
                    player.getY() < player.level().getMinY() + DEEP_DOWN_DEPTH);
            milestone(
                    player,
                    knowledge,
                    TCIds.rl("m_uphigh"),
                    "got.uphigh",
                    player.getY() > player.level().getMaxY() * UP_HIGH_FRACTION);
        }
        if (player.tickCount % MILESTONE_CHECK_INTERVAL != 0) return;
        Holder<Biome> biome = player.level().getBiome(player.blockPosition());
        milestone(player, knowledge, TCIds.rl("m_hellandback"), "got.hellandback", biome.is(BiomeTags.IS_NETHER));
        milestone(player, knowledge, TCIds.rl("m_endoftheworld"), "got.endoftheworld", biome.is(BiomeTags.IS_END));
        milestone(
                player,
                knowledge,
                TCIds.rl("m_walker"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)) > WALK_MILESTONE_CM);
        milestone(
                player,
                knowledge,
                TCIds.rl("m_runner"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)) > SPRINT_MILESTONE_CM);
        milestone(
                player,
                knowledge,
                TCIds.rl("m_jumper"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.JUMP)) > JUMP_MILESTONE);
        milestone(
                player,
                knowledge,
                TCIds.rl("m_swimmer"),
                null,
                player.getStats().getValue(Stats.CUSTOM.get(Stats.SWIM_ONE_CM)) > SWIM_MILESTONE_CM);
    }

    private static void milestone(
            ServerPlayer player, PlayerKnowledge knowledge, Identifier id, String messageKey, boolean condition) {
        if (!condition || knowledge.isResearchKnown(id)) return;
        knowledge.addResearch(id);
        knowledge.markComplete(id);
        knowledge.sync(player);
        if (messageKey != null) {
            sendActionBar(player, messageKey);
        }
    }

    private static void sendActionBar(ServerPlayer player, String key) {
        player.connection.send(new ClientboundSetActionBarTextPacket(
                Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE)));
    }
}
