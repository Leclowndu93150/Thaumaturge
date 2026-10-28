package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityFollowingItem;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class InfusionEnchantmentEvents {
    private static final float REFINING_CHANCE_PER_LEVEL = 0.125F;
    private static final int SOUNDING_DAMAGE = 5;
    private static final float ARCING_DAMAGE_FRACTION = 0.5F;
    private static final int SLASH_LIFE = 8;
    private static final int GLIMMER_LIGHT_THRESHOLD = 10;
    private static final ThreadLocal<Boolean> DESTRUCTIVE_RECURSION = ThreadLocal.withInitial(() -> false);

    private InfusionEnchantmentEvents() {}

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        int rank = cappedLevel(held, InfusionEnchantment.ARCING);
        if (rank <= 0 || !event.getTarget().isAlive()) {
            return;
        }
        Entity target = event.getTarget();
        ServerLevel level = (ServerLevel) player.level();
        AABB area = target.getBoundingBox().inflate(1.5 + rank, 1.0F + rank / 2.0F, 1.5 + rank);
        List<Mob> nearby = new ArrayList<>(level.getEntitiesOfClass(
                Mob.class,
                area,
                other -> other.getId() != target.getId()
                        && !other.isRemoved()
                        && other.isAlive()
                        && !isFriendly(player, other)));
        nearby.sort(Comparator.comparingDouble(mob -> mob.distanceToSqr(target)));
        int count = 0;
        for (Mob living : nearby) {
            if (count >= rank) {
                break;
            }
            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (living.hurt(level.damageSources().playerAttack(player), damage * ARCING_DAMAGE_FRACTION)) {
                EnchantmentHelper.doPostAttackEffects(
                        level, living, level.damageSources().playerAttack(player));
                float yaw = player.getYRot() * ((float) Math.PI / 180.0F);
                living.push(-Mth.sin(yaw) * 0.5F, 0.1, Mth.cos(yaw) * 0.5F);
                Effects.slash(
                        level,
                        target.getX(),
                        target.getY() + target.getBbHeight() / 2.0,
                        target.getZ(),
                        living.getX(),
                        living.getY() + living.getBbHeight() / 2.0,
                        living.getZ(),
                        SLASH_LIFE);
                count++;
            }
        }
        if (count > 0) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    TTSounds.WIND.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    0.9F + level.getRandom().nextFloat() * 0.2F);
            Effects.slash(
                    level,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    target.getX(),
                    target.getY() + target.getBbHeight() / 2.0,
                    target.getZ(),
                    SLASH_LIFE);
        }
    }

    private static boolean isFriendly(Player source, Entity target) {
        if (source.getRootVehicle() == target.getRootVehicle() || source.isAlliedTo(target)) {
            return true;
        }
        return target instanceof OwnableEntity ownable && source == ownable.getOwner();
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() == null) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand() == null ? InteractionHand.MAIN_HAND : event.getHand());
        int rank = cappedLevel(held, InfusionEnchantment.SOUNDING);
        if (rank <= 0 || !player.isShiftKeyDown()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getLevel() instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        held.hurtAndBreak(
                SOUNDING_DAMAGE,
                serverPlayer,
                event.getHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
        float pitch = 0.45F + level.getRandom().nextFloat() * 0.1F;
        level.playSound(
                null,
                event.getPos().getX() + 0.5,
                event.getPos().getY() + 0.5,
                event.getPos().getZ() + 0.5,
                SoundEvents.NOTE_BLOCK_BASEDRUM,
                SoundSource.BLOCKS,
                0.4F,
                pitch);
        SoundingScan.perform(level, serverPlayer, event.getPos(), rank);
    }

    @SubscribeEvent
    public static void onBreakBlock(BreakEvent event) {
        if (event.getLevel().isClientSide() || event.getPlayer() == null || EnchantMining.isHarvestingFurthest()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack held = player.getMainHandItem();
        if (!InfusionEnchantmentHelper.has(held, InfusionEnchantment.BURROWING) || player.isShiftKeyDown()) {
            return;
        }
        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (!held.isCorrectToolForDrops(state)) {
            return;
        }
        if (!(EnchantMining.isLog(level, pos) || EnchantMining.isOre(level, pos))) {
            return;
        }
        event.setCanceled(true);
        EnchantMining.breakFurthest(level, pos, state, player);
        held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCreativeDestructiveBreak(BreakEvent event) {
        if (event.getLevel().isClientSide() || event.isCanceled() || DESTRUCTIVE_RECURSION.get()) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null || !player.getAbilities().instabuild || player.isShiftKeyDown()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE)
                || !held.isCorrectToolForDrops(event.getState())) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        DestructiveSwingMemory swingMemory = player.getExistingDataOrNull(TTAttachments.DESTRUCTIVE_SWING.get());
        Direction face = swingMemory == null ? null : swingMemory.takeFace(level.dimension(), event.getPos());
        if (face == null) {
            face = Direction.getNearest(
                    player.getViewVector(1.0F).x, player.getViewVector(1.0F).y, player.getViewVector(1.0F).z);
        }
        breakDestructiveNeighbors(level, player, held, event.getPos(), face);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        if (event.getAction() == Action.ABORT
                || player.isShiftKeyDown()
                || event.getFace() == null
                || !InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE)
                || !held.isCorrectToolForDrops(event.getLevel().getBlockState(event.getPos()))) {
            player.getData(TTAttachments.DESTRUCTIVE_SWING).forget();
            return;
        }
        player.getData(TTAttachments.DESTRUCTIVE_SWING)
                .remember(event.getLevel().dimension(), event.getPos(), event.getFace());
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        ServerLevel level = event.getLevel();
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof Player player)) {
            addRareNugget(event, level, state);
            return;
        }
        DestructiveSwingMemory swingMemory = player.getExistingDataOrNull(TTAttachments.DESTRUCTIVE_SWING.get());
        Direction swungFace = swingMemory == null ? null : swingMemory.takeFace(level.dimension(), pos);
        ItemStack held = player.getMainHandItem();

        if (InfusionEnchantmentHelper.has(held, InfusionEnchantment.REFINING)) {
            int rank = cappedLevel(held, InfusionEnchantment.REFINING);
            Item cluster = RefiningResults.clusterFor(state);
            if (cluster != null) {
                float chance = (1 + rank) * REFINING_CHANCE_PER_LEVEL;
                boolean changed = false;
                for (ItemEntity drop : event.getDrops()) {
                    if (level.getRandom().nextFloat() <= chance) {
                        drop.setItem(new ItemStack(cluster, drop.getItem().getCount()));
                        changed = true;
                    }
                }
                if (changed) {
                    level.playSound(
                            null,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.BLOCKS,
                            0.2F,
                            0.75F + level.getRandom().nextFloat() * 0.2F);
                }
            }
        }
        addRareNugget(event, level, state);

        boolean sneaking = player.isShiftKeyDown();
        if (InfusionEnchantmentHelper.has(held, InfusionEnchantment.COLLECTOR) && !sneaking) {
            List<ItemEntity> followers = new ArrayList<>(event.getDrops().size());
            for (ItemEntity drop : event.getDrops()) {
                EntityFollowingItem follow = new EntityFollowingItem(
                        level,
                        drop.getX(),
                        drop.getY(),
                        drop.getZ(),
                        drop.getItem().copy(),
                        player);
                follow.setDeltaMovement(drop.getDeltaMovement());
                follow.setDefaultPickUpDelay();
                followers.add(follow);
            }
            event.getDrops().clear();
            event.getDrops().addAll(followers);
        }

        if (InfusionEnchantmentHelper.has(held, InfusionEnchantment.LAMPLIGHT) && !sneaking) {
            if (level.isEmptyBlock(pos) && settledLight(level, pos) < GLIMMER_LIGHT_THRESHOLD) {
                level.setBlock(pos, TTBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        if (!player.isCreative()
                && !DESTRUCTIVE_RECURSION.get()
                && InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE)
                && !player.isShiftKeyDown()
                && held.isCorrectToolForDrops(state)) {
            Direction face = swungFace != null
                    ? swungFace
                    : Direction.getNearest(
                            player.getViewVector(1.0F).x, player.getViewVector(1.0F).y, player.getViewVector(1.0F).z);
            breakDestructiveNeighbors(level, player, held, pos, face);
        }
    }

    private static void breakDestructiveNeighbors(
            ServerLevel level, Player player, ItemStack held, BlockPos pos, Direction face) {
        if (DESTRUCTIVE_RECURSION.get()) {
            return;
        }
        DESTRUCTIVE_RECURSION.set(true);
        try {
            for (int aa = -1; aa <= 1 && !held.isEmpty(); aa++) {
                for (int bb = -1; bb <= 1 && !held.isEmpty(); bb++) {
                    if (aa == 0 && bb == 0) {
                        continue;
                    }
                    int xx = 0;
                    int yy = 0;
                    int zz = 0;
                    int axis = face.ordinal();
                    if (axis <= 1) {
                        xx = aa;
                        zz = bb;
                    } else if (axis <= 3) {
                        xx = aa;
                        yy = bb;
                    } else {
                        zz = aa;
                        yy = bb;
                    }
                    BlockPos offset = pos.offset(xx, yy, zz);
                    BlockState neighbour = level.getBlockState(offset);
                    if (neighbour.getDestroySpeed(level, offset) >= 0.0F && held.getDestroySpeed(neighbour) > 1.0F) {
                        if (!player.isCreative()) {
                            held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        }
                        EnchantMining.harvestBlock(level, player, offset, false);
                    }
                }
            }
        } finally {
            DESTRUCTIVE_RECURSION.set(false);
        }
    }

    static int settledLight(ServerLevel level, BlockPos pos) {
        int light = level.getRawBrightness(pos, 0);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            cursor.setWithOffset(pos, direction);
            if (level.hasChunkAt(cursor)) {
                light = Math.max(light, level.getRawBrightness(cursor, 0) - 1);
            }
        }
        return light;
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || player.level().isClientSide()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        List<InfusionEnchantment> list = InfusionEnchantmentHelper.list(held);
        ServerLevel level = (ServerLevel) player.level();
        LivingEntity victim = event.getEntity();

        if (list.contains(InfusionEnchantment.COLLECTOR)) {
            List<ItemEntity> drops = List.copyOf(event.getDrops());
            event.getDrops().clear();
            for (ItemEntity drop : drops) {
                EntityFollowingItem follow = new EntityFollowingItem(
                        level,
                        drop.getX(),
                        drop.getY(),
                        drop.getZ(),
                        drop.getItem().copy(),
                        player);
                follow.setDeltaMovement(drop.getDeltaMovement());
                follow.setDefaultPickUpDelay();
                event.getDrops().add(follow);
            }
        }

        if (list.contains(InfusionEnchantment.ESSENCE)) {
            int rank = cappedLevel(held, InfusionEnchantment.ESSENCE);
            distillEssence(level, player, victim, rank, list.contains(InfusionEnchantment.COLLECTOR), event);
        }
    }

    private static void distillEssence(
            ServerLevel level,
            Player player,
            LivingEntity victim,
            int rank,
            boolean collector,
            LivingDropsEvent event) {
        var random = level.getRandom();
        if (random.nextInt(InfusionEnchantment.ESSENCE.maxLevel()) >= rank) {
            return;
        }
        AspectList remaining = EntityAspects.of(victim);
        int produced = 0;
        double x = victim.getX();
        double y = victim.getY() + victim.getEyeHeight();
        double z = victim.getZ();
        while (produced < rank && !remaining.isEmpty()) {
            List<AspectInstance> entries = remaining.entries();
            AspectInstance entry = entries.get(random.nextInt(entries.size()));
            remaining = remaining.remove(entry.aspect(), 1);
            ItemStack crystal = EssentiaCrystalFactory.of(entry.aspect());
            if (collector) {
                event.getDrops().add(new EntityFollowingItem(level, x, y, z, crystal, player));
            } else {
                event.getDrops().add(new ItemEntity(level, x, y, z, crystal));
            }
            produced++;
            if (random.nextInt(rank) == 0) {
                produced += 1 + random.nextInt(2);
            }
        }
    }

    private static int cappedLevel(ItemStack stack, InfusionEnchantment enchantment) {
        return Math.min(InfusionEnchantmentHelper.level(stack, enchantment), enchantment.maxLevel());
    }

    private static void addRareNugget(BlockDropsEvent event, ServerLevel level, BlockState state) {
        if (event.getBreaker() instanceof Player player && hasSilkTouch(level, player.getMainHandItem())) {
            return;
        }
        double chance = rareNuggetChance(state);
        if (chance <= 0.0 || level.getRandom().nextDouble() >= chance) {
            return;
        }
        BlockPos pos = event.getPos();
        event.getDrops()
                .add(new ItemEntity(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        new ItemStack(TTItems.NUGGET_QUARTZ.get())));
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack stack) {
        return stack.getEnchantmentLevel(level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH))
                > 0;
    }

    private static double rareNuggetChance(BlockState state) {
        if (state.is(BlockTags.EMERALD_ORES)) {
            return 0.075;
        }
        if (state.is(BlockTags.DIAMOND_ORES)
                || state.is(TTBlocks.ORE_QUARTZ.get())
                || state.is(TTBlockTags.ORES_AMBER)) {
            return 0.05;
        }
        if (state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(Tags.Blocks.ORES_QUARTZ)) {
            return 0.01;
        }
        if (state.is(BlockTags.COAL_ORES)) {
            return 0.001;
        }
        return 0.0;
    }
}
