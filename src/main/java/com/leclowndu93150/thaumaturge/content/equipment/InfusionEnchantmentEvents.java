package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityFollowingItem;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class InfusionEnchantmentEvents {
    private static final double ARC_HORIZONTAL_BASE = 1.5;
    private static final double ARC_VERTICAL_BASE = 1.0;
    private static final double ARC_DAMAGE_FRACTION = 0.5;
    private static final double ARC_KNOCKBACK = 0.5;
    private static final double ARC_LIFT = 0.1;
    private static final int ARC_SLASH_TICKS = 8;
    private static final float ARC_SOUND_VOLUME = 1.0F;
    private static final float ARC_SOUND_PITCH_BASE = 0.9F;
    private static final float ARC_SOUND_PITCH_SPREAD = 0.2F;
    private static final double HALF = 2.0;
    private static final int SOUNDING_WEAR = 5;
    private static final float SOUNDING_VOLUME = 0.4F;
    private static final float SOUNDING_PITCH_BASE = 0.45F;
    private static final float SOUNDING_PITCH_SPREAD = 0.1F;
    private static final double BLOCK_CENTER = 0.5;
    private static final int TOOL_WEAR = 1;
    private static final float EFFECTIVE_SPEED = 1.0F;
    private static final double REFINING_CHANCE_BASE = 1.0;
    private static final double REFINING_CHANCE_STEP = 0.125;
    private static final double REFINING_ORE_WEIGHT = 1.0;
    private static final float REFINING_VOLUME = 0.2F;
    private static final float REFINING_PITCH_BASE = 0.75F;
    private static final float REFINING_PITCH_SPREAD = 0.2F;
    private static final int LAMPLIGHT_THRESHOLD = 10;
    private static final int LIGHT_FALLOFF = 1;
    private static final int CUBE_SIDE = 3;
    private static final int CUBE_CELLS = CUBE_SIDE * CUBE_SIDE * CUBE_SIDE;
    private static final int ESSENCE_ROLL_BOUND = InfusionEnchantment.ESSENCE.maxLevel();
    private static final int ESSENCE_SKIP_MIN = 1;
    private static final int ESSENCE_SKIP_OPTIONS = 2;
    private static final Map<UUID, DestructiveTarget> TARGETS = new HashMap<>();
    private static final Map<Direction.Axis, List<Vec3i>> PLANE_STEPS = planeSteps();
    private static final ThreadLocal<Boolean> EXPANDING = ThreadLocal.withInitial(() -> false);
    private static final List<NuggetChance> NUGGET_CHANCES = Stream
            .of(new NuggetChance(state -> state.is(BlockTags.DIAMOND_ORES), 0.05), new NuggetChance(state -> state.is(BlockTags.EMERALD_ORES), 0.075),
                    new NuggetChance(state -> state.is(BlockTags.LAPIS_ORES), 0.01), new NuggetChance(state -> state.is(BlockTags.COAL_ORES), 0.001),
                    new NuggetChance(state -> state.is(BlockTags.REDSTONE_ORES), 0.01), new NuggetChance(state -> state.is(TTBlocks.ORE_QUARTZ), 0.05),
                    new NuggetChance(state -> state.is(Tags.Blocks.ORES_QUARTZ), 0.01), new NuggetChance(state -> state.is(TTBlockTags.ORES_AMBER), 0.05))
            .sorted(Comparator.comparingDouble(NuggetChance::chance).reversed()).toList();

    private InfusionEnchantmentEvents() {}

    public static void resetSession() {
        TARGETS.clear();
    }

    @SubscribeEvent
    public static void onBreakBlock(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || EnchantMining.isHarvestingFurthest()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        BlockState state = event.getState();
        boolean burrowable = state.is(BlockTags.LOGS) || state.is(Tags.Blocks.ORES);
        if (player.isShiftKeyDown() || !burrowable || !InfusionEnchantmentHelper.has(tool, InfusionEnchantment.BURROWING) || !tool.isCorrectToolForDrops(state)) {
            return;
        }
        event.setCanceled(true);
        event.setNotifyClient(true);
        EnchantMining.breakFurthest(level, event.getPos(), state, player);
        tool.hurtAndBreak(TOOL_WEAR, player, EquipmentSlot.MAINHAND);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        DestructiveTarget swing = startsDestructiveSwing(event, player) ? new DestructiveTarget(event.getLevel().dimension(), event.getPos().immutable(), event.getFace()) : null;
        TARGETS.compute(player.getUUID(), (id, previous) -> swing);
    }

    private static boolean startsDestructiveSwing(PlayerInteractEvent.LeftClickBlock event, Player player) {
        if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.ABORT || player.isShiftKeyDown() || event.getFace() == null) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        BlockState hit = event.getLevel().getBlockState(event.getPos());
        return held.isCorrectToolForDrops(hit) && InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        if (!(attacker.level() instanceof ServerLevel level) || !(event.getTarget() instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }
        int rank = cappedLevel(attacker.getMainHandItem(), InfusionEnchantment.ARCING);
        if (rank < 1) {
            return;
        }
        AABB reach = target.getBoundingBox().inflate(ARC_HORIZONTAL_BASE + rank, ARC_VERTICAL_BASE + rank / HALF, ARC_HORIZONTAL_BASE + rank);
        List<Mob> victims = new ArrayList<>(level.getEntitiesOfClass(Mob.class, reach, mob -> isArcVictim(attacker, target, mob)));
        victims.sort(Comparator.comparingDouble(mob -> mob.distanceToSqr(target)));
        float damage = (float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * ARC_DAMAGE_FRACTION);
        Vec3 facing = horizontalFacing(attacker);
        int struck = 0;
        for (Mob victim : victims.subList(0, Math.min(rank, victims.size()))) {
            DamageSource source = attacker.damageSources().playerAttack(attacker);
            if (victim.hurtServer(level, source, damage)) {
                EnchantmentHelper.doPostAttackEffects(level, victim, source);
                victim.push(facing.x * ARC_KNOCKBACK, ARC_LIFT, facing.z * ARC_KNOCKBACK);
                arcBolt(level, bodyCenter(target), bodyCenter(victim));
                struck++;
            }
        }
        if (struck > 0) {
            float pitch = ARC_SOUND_PITCH_BASE + level.getRandom().nextFloat() * ARC_SOUND_PITCH_SPREAD;
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), TTSounds.WIND.get(), SoundSource.PLAYERS, ARC_SOUND_VOLUME, pitch);
            arcBolt(level, bodyCenter(attacker), bodyCenter(target));
        }
    }

    private static Vec3 horizontalFacing(Entity entity) {
        float radians = entity.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack tool = event.getItemStack();
        int rank = cappedLevel(tool, InfusionEnchantment.SOUNDING);
        if (!player.isShiftKeyDown() || rank < 1) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getLevel() instanceof ServerLevel level) || !(player instanceof ServerPlayer viewer)) {
            return;
        }
        BlockPos pos = event.getPos();
        tool.hurtAndBreak(SOUNDING_WEAR, viewer, event.getHand().asEquipmentSlot());
        float pitch = SOUNDING_PITCH_BASE + level.getRandom().nextFloat() * SOUNDING_PITCH_SPREAD;
        level.playSound(null, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, SoundEvents.NOTE_BLOCK_BASEDRUM, SoundSource.BLOCKS, SOUNDING_VOLUME, pitch);
        SoundingScan.perform(level, viewer, pos, rank);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !(event.getSource().getEntity() instanceof Player killer)) {
            return;
        }
        ItemStack weapon = killer.getMainHandItem();
        boolean collector = InfusionEnchantmentHelper.has(weapon, InfusionEnchantment.COLLECTOR);
        if (collector) {
            follow(level, event.getDrops(), killer);
        }
        int essence = cappedLevel(weapon, InfusionEnchantment.ESSENCE);
        if (essence >= 1) {
            distil(level, event, killer, collector, essence);
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        ServerLevel level = event.getLevel();
        if (event.getBreaker() instanceof Player breaker) {
            ItemStack held = breaker.getMainHandItem();
            refine(level, event, held);
            dropNugget(level, event, isSilkTouch(level, held));
            applyHarvestPerks(level, event, breaker, held);
        } else {
            dropNugget(level, event, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        TARGETS.remove(event.getEntity().getUUID());
    }

    private static void applyHarvestPerks(ServerLevel level, BlockDropsEvent event, Player player, ItemStack tool) {
        DestructiveTarget target = TARGETS.remove(player.getUUID());
        boolean sneaking = player.isShiftKeyDown();
        if (!sneaking && InfusionEnchantmentHelper.has(tool, InfusionEnchantment.COLLECTOR)) {
            follow(level, event.getDrops(), player);
        }
        if (!sneaking && InfusionEnchantmentHelper.has(tool, InfusionEnchantment.LAMPLIGHT)) {
            placeGlimmer(level, event.getPos());
        }
        expand(level, player, tool, target, event);
    }

    private static int cappedLevel(ItemStack stack, InfusionEnchantment enchantment) {
        return Math.min(InfusionEnchantmentHelper.level(stack, enchantment), enchantment.maxLevel());
    }

    private static boolean isSilkTouch(ServerLevel level, ItemStack tool) {
        return EnchantmentHelper.getItemEnchantmentLevel(silkHolder(level), tool) > 0;
    }

    private static Holder<Enchantment> silkHolder(ServerLevel level) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
    }

    private static boolean isArcVictim(Player attacker, LivingEntity target, Mob mob) {
        return mob != target && !mob.isRemoved() && mob.isAlive() && mob.getRootVehicle() != attacker.getRootVehicle() && !mob.isAlliedTo(attacker)
                && !(mob instanceof OwnableEntity ownable && ownable.getOwner() == attacker);
    }

    private static Vec3 bodyCenter(Entity entity) {
        return entity.position().add(0.0, entity.getBbHeight() / HALF, 0.0);
    }

    private static void arcBolt(ServerLevel level, Vec3 from, Vec3 to) {
        Effects.slash(level, from.x, from.y, from.z, to.x, to.y, to.z, ARC_SLASH_TICKS);
    }

    private static void refine(ServerLevel level, BlockDropsEvent event, ItemStack tool) {
        int rank = cappedLevel(tool, InfusionEnchantment.REFINING);
        if (rank < 1) {
            return;
        }
        BlockState broken = event.getState();
        Item cluster = RefiningResults.clusterFor(broken);
        if (cluster == null || !(tool.isCorrectToolForDrops(broken) || isSilkTouch(level, tool))) {
            return;
        }
        double chance = (REFINING_CHANCE_BASE + rank) * REFINING_CHANCE_STEP * REFINING_ORE_WEIGHT;
        if (swapForClusters(level, event.getDrops(), cluster, chance) > 0) {
            refineChime(level, event.getPos());
        }
    }

    private static void refineChime(ServerLevel level, BlockPos pos) {
        float pitch = REFINING_PITCH_BASE + level.getRandom().nextFloat() * REFINING_PITCH_SPREAD;
        level.playSound(null, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, REFINING_VOLUME, pitch);
    }

    private static int swapForClusters(ServerLevel level, Collection<ItemEntity> drops, Item cluster, double chance) {
        List<ItemEntity> chosen = drops.stream().filter(drop -> level.getRandom().nextDouble() <= chance).toList();
        chosen.forEach(drop -> drop.setItem(new ItemStack(cluster, drop.getItem().getCount())));
        return chosen.size();
    }

    private static void dropNugget(ServerLevel level, BlockDropsEvent event, boolean silkTouched) {
        if (!silkTouched && rollNugget(level, event.getState())) {
            event.getDrops().add(nuggetAt(level, event.getPos()));
        }
    }

    private static boolean rollNugget(ServerLevel level, BlockState broken) {
        double chance = nuggetChance(broken);
        return chance > 0.0 && level.getRandom().nextDouble() < chance;
    }

    private static double nuggetChance(BlockState broken) {
        for (NuggetChance entry : NUGGET_CHANCES) {
            if (entry.match().test(broken)) {
                return entry.chance();
            }
        }
        return 0.0;
    }

    private static ItemEntity nuggetAt(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        return new ItemEntity(level, center.x, center.y, center.z, new ItemStack(TTItems.NUGGET_QUARTZ.get()), 0.0, 0.0, 0.0);
    }

    private static void follow(ServerLevel level, Collection<ItemEntity> drops, Player collector) {
        List<ItemEntity> followers = drops.stream().<ItemEntity>map(drop -> adopt(level, drop, collector)).toList();
        drops.clear();
        drops.addAll(followers);
    }

    private static EntityFollowingItem adopt(ServerLevel level, ItemEntity drop, Player collector) {
        Vec3 at = drop.position();
        EntityFollowingItem follower = new EntityFollowingItem(level, at.x, at.y, at.z, drop.getItem().copy(), collector);
        follower.setDefaultPickUpDelay();
        follower.setDeltaMovement(drop.getDeltaMovement());
        return follower;
    }

    private static void placeGlimmer(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).isAir() && settledLight(level, pos) < LAMPLIGHT_THRESHOLD) {
            level.setBlock(pos, TTBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static int settledLight(ServerLevel level, BlockPos pos) {
        int own = level.getRawBrightness(pos, 0);
        int spill = Stream.of(Direction.values()).map(pos::relative).filter(level::hasChunkAt).mapToInt(next -> level.getRawBrightness(next, 0) - LIGHT_FALLOFF).max().orElse(Integer.MIN_VALUE);
        return Math.max(own, spill);
    }

    private static void expand(ServerLevel level, Player player, ItemStack tool, @Nullable DestructiveTarget target, BlockDropsEvent event) {
        if (EXPANDING.get() || player.isShiftKeyDown() || !InfusionEnchantmentHelper.has(tool, InfusionEnchantment.DESTRUCTIVE) || !tool.isCorrectToolForDrops(event.getState())) {
            return;
        }
        BlockPos origin = event.getPos();
        Direction.Axis axis = swingFace(level, origin, player, target).getAxis();
        EXPANDING.set(true);
        try {
            Iterator<Vec3i> steps = PLANE_STEPS.get(axis).iterator();
            while (steps.hasNext() && !tool.isEmpty()) {
                harvestAdjacent(level, player, tool, origin.offset(steps.next()));
            }
        } finally {
            EXPANDING.set(false);
        }
    }

    private static Direction swingFace(ServerLevel level, BlockPos pos, Player player, @Nullable DestructiveTarget target) {
        if (target != null && target.covers(level.dimension(), pos)) {
            return target.face();
        }
        return Direction.getApproximateNearest(player.getLookAngle());
    }

    private static Vec3i cell(int index) {
        return new Vec3i(index / (CUBE_SIDE * CUBE_SIDE) - 1, index / CUBE_SIDE % CUBE_SIDE - 1, index % CUBE_SIDE - 1);
    }

    private static Map<Direction.Axis, List<Vec3i>> planeSteps() {
        Map<Direction.Axis, List<Vec3i>> steps = new EnumMap<>(Direction.Axis.class);
        for (Direction.Axis axis : Direction.Axis.values()) {
            List<Vec3i> plane = IntStream.range(0, CUBE_CELLS).mapToObj(InfusionEnchantmentEvents::cell)
                    .filter(cell -> axis.choose(cell.getX(), cell.getY(), cell.getZ()) == 0 && !cell.equals(Vec3i.ZERO)).toList();
            steps.put(axis, plane);
        }
        return steps;
    }

    private static boolean canBeSpedUp(ServerLevel level, BlockPos pos, ItemStack tool) {
        BlockState target = level.getBlockState(pos);
        return tool.getDestroySpeed(target) > EFFECTIVE_SPEED && target.getDestroySpeed(level, pos) >= 0.0F;
    }

    private static void harvestAdjacent(ServerLevel level, Player player, ItemStack tool, BlockPos pos) {
        if (canBeSpedUp(level, pos, tool) && EnchantMining.harvestBlock(level, player, pos, false)) {
            tool.hurtAndBreak(TOOL_WEAR, player, EquipmentSlot.MAINHAND);
        }
    }

    private static void distil(ServerLevel level, LivingDropsEvent event, Player killer, boolean collector, int rank) {
        RandomSource random = level.getRandom();
        if (random.nextInt(ESSENCE_ROLL_BOUND) >= rank) {
            return;
        }
        LivingEntity dead = event.getEntity();
        List<AspectInstance> remaining = new ArrayList<>(EntityAspects.of(dead).entries());
        int counted = 0;
        while (counted < rank && !remaining.isEmpty()) {
            int index = random.nextInt(remaining.size());
            AspectInstance picked = remaining.get(index);
            event.getDrops().add(crystalDrop(level, dead, killer, collector, picked));
            if (picked.amount() > 1) {
                remaining.set(index, picked.withAmount(picked.amount() - 1));
            } else {
                remaining.remove(index);
            }
            counted++;
            if (random.nextInt(rank) == 0) {
                counted += ESSENCE_SKIP_MIN + random.nextInt(ESSENCE_SKIP_OPTIONS);
            }
        }
    }

    private static ItemEntity crystalDrop(ServerLevel level, LivingEntity dead, Player killer, boolean collector, AspectInstance picked) {
        ItemStack crystal = EssentiaCrystalAccess.create(picked.aspect(), 1);
        return collector ? new EntityFollowingItem(level, dead.getX(), dead.getEyeY(), dead.getZ(), crystal, killer) : new ItemEntity(level, dead.getX(), dead.getEyeY(), dead.getZ(), crystal);
    }

    private static void discardRandom(RandomSource random, List<AspectInstance> pool, int count) {
        for (int removed = 0; removed < count && !pool.isEmpty(); removed++) {
            pool.remove(random.nextInt(pool.size()));
        }
    }

    private record DestructiveTarget(ResourceKey<Level> dimension, BlockPos pos, Direction face) {
        boolean covers(ResourceKey<Level> where, BlockPos at) {
            return dimension.equals(where) && pos.equals(at);
        }
    }

    private record NuggetChance(Predicate<BlockState> match, double chance) {
    }
}
