package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.crucible.CrucibleEvent;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ResearchProgressionEvents {
    private static final Identifier CRYSTALS = TTIds.rl("gotcrystals");
    private static final Identifier DREAM = TTIds.rl("gotdream");
    private static final Identifier BOOK = TTIds.rl("gotthaumonomicon");
    private static final Identifier UNLOCK_AUROMANCY = TTIds.rl("unlock_auromancy");
    private static final Identifier BASE_AUROMANCY = TTIds.rl("base_auromancy");
    private static final Identifier FOCUS_PROJECTILE = TTIds.rl("focus_projectile");
    private static final Identifier ON_FIRE = TTIds.rl("f_onfire");
    private static final Identifier ARROW = TTIds.rl("f_arrow");
    private static final Identifier FIREBALL = TTIds.rl("f_fireball");
    private static final Identifier SPIT = TTIds.rl("f_spit");
    private static final Identifier DEEP_DOWN = TTIds.rl("m_deepdown");
    private static final Identifier UP_HIGH = TTIds.rl("m_uphigh");
    private static final Identifier HELL_AND_BACK = TTIds.rl("m_hellandback");
    private static final Identifier END_OF_THE_WORLD = TTIds.rl("m_endoftheworld");
    private static final Identifier WALKER = TTIds.rl("m_walker");
    private static final Identifier RUNNER = TTIds.rl("m_runner");
    private static final Identifier JUMPER = TTIds.rl("m_jumper");
    private static final Identifier SWIMMER = TTIds.rl("m_swimmer");

    private static final String CRYSTALS_MESSAGE = "message.thaumaturge.discovery.crystals";
    private static final String DREAM_MESSAGE = "message.thaumaturge.discovery.dream";
    private static final String ON_FIRE_MESSAGE = "message.thaumaturge.discovery.on_fire";
    private static final String PROJECTILE_MESSAGE = "message.thaumaturge.discovery.projectile";
    private static final String DEEP_DOWN_MESSAGE = "message.thaumaturge.discovery.deep_down";
    private static final String UP_HIGH_MESSAGE = "message.thaumaturge.discovery.up_high";
    private static final String HELL_AND_BACK_MESSAGE = "message.thaumaturge.discovery.hell_and_back";
    private static final String END_OF_THE_WORLD_MESSAGE = "message.thaumaturge.discovery.end_of_the_world";
    private static final String BOOK_TITLE_KEY = "book.thaumaturge.start.title";
    private static final List<String> BOOK_PAGE_KEYS = List.of("book.thaumaturge.start.1", "book.thaumaturge.start.2", "book.thaumaturge.start.3");

    private static final int MILESTONE_INTERVAL_TICKS = 200;
    private static final int DEEP_MARGIN_BLOCKS = 10;
    private static final double HIGH_HEIGHT_FACTOR = 0.4;
    private static final int WALK_CENTIMETRES = 160000;
    private static final int RUN_CENTIMETRES = 80000;
    private static final int JUMP_COUNT = 500;
    private static final int SWIM_CENTIMETRES = 8000;
    private static final int MINIMUM_STAGE = 1;

    private static final List<ProjectileRule> PROJECTILE_RULES = List.of(new ProjectileRule(AbstractArrow.class, ARROW), new ProjectileRule(AbstractHurtingProjectile.class, FIREBALL),
            new ProjectileRule(LlamaSpit.class, SPIT));

    private ResearchProgressionEvents() {}

    private static final List<BiomeMilestone> BIOME_MILESTONES = List.of(new BiomeMilestone(BiomeTags.IS_NETHER, HELL_AND_BACK, HELL_AND_BACK_MESSAGE),
            new BiomeMilestone(BiomeTags.IS_END, END_OF_THE_WORLD, END_OF_THE_WORLD_MESSAGE));

    private static final List<StatMilestone> STAT_MILESTONES = List.of(new StatMilestone(WALKER, Stats.WALK_ONE_CM, WALK_CENTIMETRES), new StatMilestone(RUNNER, Stats.SPRINT_ONE_CM, RUN_CENTIMETRES),
            new StatMilestone(JUMPER, Stats.JUMP, JUMP_COUNT), new StatMilestone(SWIMMER, Stats.SWIM_ONE_CM, SWIM_CENTIMETRES));

    private record ProjectileRule(Class<? extends Entity> type, Identifier research) {
    }

    private record StatMilestone(Identifier research, Identifier stat, int threshold) {
    }

    private record BiomeMilestone(TagKey<Biome> biomes, Identifier research, String messageKey) {
    }

    @SubscribeEvent
    public static void onCrucibleCraft(CrucibleEvent.Crafted event) {
        serverPlayer(event.getPlayer()).ifPresent(player -> recordCrafted(player, event.getResult()));
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        serverPlayer(event.getEntity()).ifPresent(player -> recordCrafted(player, event.getCrafting()));
    }

    public static void recordCrafted(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !CraftReferenceHolder.isReference(player.registryAccess(), stack.getItem())) {
            return;
        }
        Optional<Identifier> craftedKey = BuiltInRegistries.ITEM.getResourceKey(stack.getItem()).map(ResourceKey::identifier).map(ResearchManager::craftedKey);
        PlayerKnowledge knowledge = knowledgeOf(player);
        craftedKey.filter(key -> !knowledge.isResearchKnown(key)).ifPresent(key -> {
            knowledge.addResearch(key);
            knowledge.sync(player);
        });
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        ItemStack stack = event.getOriginalStack();
        Optional<ServerPlayer> picker = serverPlayer(event.getPlayer());
        picker.ifPresent(player -> recordCrafted(player, stack));
        picker.filter(player -> stack.is(TTItems.THAUMONOMICON)).ifPresent(player -> awardIfNew(player, knowledgeOf(player), BOOK));
        picker.filter(player -> stack.is(TTItems.ESSENTIA_CRYSTAL)).ifPresent(ResearchProgressionEvents::crystalPickup);
    }

    public static void bookOpened(Entity reader) {
        serverPlayer(reader).filter(player -> !(player instanceof FakePlayer)).ifPresent(player -> awardIfNew(player, knowledgeOf(player), BOOK));
    }

    private static boolean dreamUnlocked(ServerPlayer player) {
        PlayerKnowledge knowledge = knowledgeOf(player);
        return knowledge.isResearchKnown(CRYSTALS) && !knowledge.isResearchKnown(DREAM);
    }

    private static void crystalPickup(ServerPlayer player) {
        PlayerKnowledge knowledge = knowledgeOf(player);
        if (awardIfNew(player, knowledge, CRYSTALS)) {
            tell(player, CRYSTALS_MESSAGE);
        }
        Optional.of(player).filter(online -> ThaumaturgeCommonConfig.NO_SLEEP.get() && !knowledgeOf(online).isResearchKnown(DREAM)).ifPresent(ResearchProgressionEvents::grantDreamJournal);
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        serverPlayer(event.getEntity()).filter(ResearchProgressionEvents::dreamUnlocked).ifPresent(ResearchProgressionEvents::grantDreamJournal);
    }

    @SubscribeEvent
    public static void onFireDamage(LivingDamageEvent.Post event) {
        Optional<ServerPlayer> victim = serverPlayer(event.getEntity()).filter(player -> event.getSource().is(DamageTypeTags.IS_FIRE));
        victim.filter(player -> reached(knowledgeOf(player), BASE_AUROMANCY)).ifPresent(player -> discover(player, knowledgeOf(player), ON_FIRE, ON_FIRE_MESSAGE));
    }

    @SubscribeEvent
    public static void onProjectileDamage(LivingDamageEvent.Post event) {
        Optional<Identifier> research = Optional.ofNullable(projectileResearch(event.getSource()));
        Optional<ServerPlayer> victim = serverPlayer(event.getEntity()).filter(player -> reached(knowledgeOf(player), FOCUS_PROJECTILE));
        victim.ifPresent(player -> research.ifPresent(id -> discover(player, knowledgeOf(player), id, PROJECTILE_MESSAGE)));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Optional<ServerPlayer> online = serverPlayer(event.getEntity()).filter(player -> player.tickCount != 0);
        if (online.isEmpty()) {
            return;
        }
        ServerPlayer player = online.get();
        PlayerKnowledge knowledge = knowledgeOf(player);
        boolean auromancyPending = knowledge.isResearchKnown(UNLOCK_AUROMANCY) && !reached(knowledge, UNLOCK_AUROMANCY) && !knowledge.isResearchComplete(UNLOCK_AUROMANCY);
        if (auromancyPending) {
            checkAltitude(player, knowledge);
        }
        if (player.tickCount % MILESTONE_INTERVAL_TICKS == 0) {
            checkBiome(player, knowledge);
            checkStatistics(player, knowledge);
        }
    }

    private static void checkAltitude(ServerPlayer player, PlayerKnowledge knowledge) {
        Level level = player.level();
        double y = player.getY();
        if (y < level.getMinY() + DEEP_MARGIN_BLOCKS) {
            discover(player, knowledge, DEEP_DOWN, DEEP_DOWN_MESSAGE);
        }
        if (y > level.getMaxY() * HIGH_HEIGHT_FACTOR) {
            discover(player, knowledge, UP_HIGH, UP_HIGH_MESSAGE);
        }
    }

    private static void checkBiome(ServerPlayer player, PlayerKnowledge knowledge) {
        BlockPos feet = player.blockPosition();
        Level level = player.level();
        Optional<Holder<Biome>> biome = level.hasChunkAt(feet) ? Optional.of(level.getBiome(feet)) : Optional.empty();
        for (BiomeMilestone milestone : BIOME_MILESTONES) {
            biome.filter(holder -> holder.is(milestone.biomes())).ifPresent(holder -> discover(player, knowledge, milestone.research(), milestone.messageKey()));
        }
    }

    private static void checkStatistics(ServerPlayer player, PlayerKnowledge knowledge) {
        for (StatMilestone milestone : STAT_MILESTONES) {
            if (player.getStats().getValue(Stats.CUSTOM, milestone.stat()) > milestone.threshold()) {
                awardIfNew(player, knowledge, milestone.research());
            }
        }
    }

    private static @Nullable Identifier projectileResearch(DamageSource source) {
        Entity direct = source.getDirectEntity();
        return PROJECTILE_RULES.stream().filter(rule -> rule.type().isInstance(direct)).map(ProjectileRule::research).findFirst().orElse(null);
    }

    private static void tell(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static Optional<ServerPlayer> serverPlayer(@Nullable Entity entity) {
        return entity instanceof ServerPlayer player ? Optional.of(player) : Optional.empty();
    }

    private static PlayerKnowledge knowledgeOf(ServerPlayer player) {
        return (PlayerKnowledge) KnowledgeAccess.of(player);
    }

    private static boolean reached(PlayerKnowledge knowledge, Identifier research) {
        return knowledge.isResearchKnown(research, MINIMUM_STAGE) || knowledge.isResearchComplete(research);
    }

    private static boolean awardIfNew(ServerPlayer player, PlayerKnowledge knowledge, Identifier research) {
        if (knowledge.isResearchKnown(research)) {
            return false;
        }
        award(player, knowledge, research);
        return true;
    }

    private static void discover(ServerPlayer player, PlayerKnowledge knowledge, Identifier research, String messageKey) {
        if (awardIfNew(player, knowledge, research)) {
            actionBar(player, messageKey);
        }
    }

    private static void award(ServerPlayer player, PlayerKnowledge knowledge, Identifier research) {
        knowledge.addResearch(research);
        knowledge.markComplete(research);
        knowledge.sync(player);
    }

    private static void actionBar(ServerPlayer player, String key) {
        player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static Filterable<Component> page(String key) {
        Component text = Component.translatable(key);
        return Filterable.passThrough(text);
    }

    private static ItemStack writeDreamBook(ServerPlayer player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContent content = dreamContent(player);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return book;
    }

    private static WrittenBookContent dreamContent(ServerPlayer player) {
        List<Filterable<Component>> pages = BOOK_PAGE_KEYS.stream().map(ResearchProgressionEvents::page).toList();
        Filterable<String> title = Filterable.passThrough(Component.translatable(BOOK_TITLE_KEY).getString());
        return new WrittenBookContent(title, player.getDisplayName().getString(), WrittenBookContent.MAX_GENERATION, pages, false);
    }

    private static void grantDreamJournal(ServerPlayer player) {
        award(player, knowledgeOf(player), DREAM);
        ItemStack book = writeDreamBook(player);
        boolean stored = player.getInventory().add(book);
        if (!stored) {
            player.drop(book, false);
        }
        tell(player, DREAM_MESSAGE);
    }
}
