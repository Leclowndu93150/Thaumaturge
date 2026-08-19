package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityFollowingItem;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class InfusionEnchantmentEvents {
    private static final int FOLLOW_TYPE = 10;
    private static final float REFINING_CHANCE_PER_LEVEL = 0.125F;
    private static final int SOUNDING_DAMAGE = 5;
    private static final float ARCING_DAMAGE_FRACTION = 0.5F;
    private static final int SLASH_LIFE = 8;
    private static final int GLIMMER_LIGHT_THRESHOLD = 10;
    private static final float TC_QUARTZ_NUGGET_CHANCE = 0.05F;

    private static final ThreadLocal<Boolean> DESTRUCTIVE_RECURSION = ThreadLocal.withInitial(() -> false);

    private InfusionEnchantmentEvents() {}

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        int rank = InfusionEnchantmentHelper.level(held, InfusionEnchantment.ARCING);
        if (rank <= 0 || !event.getTarget().isAlive()) {
            return;
        }
        Entity target = event.getTarget();
        ServerLevel level = (ServerLevel) player.level();
        AABB area = target.getBoundingBox().inflate(1.5 + rank, 1.0F + rank / 2.0F, 1.5 + rank);
        List<Entity> nearby = level.getEntities(player, area);
        int count = 0;
        for (Entity other : nearby) {
            if (count >= rank) {
                break;
            }
            if (other.isRemoved() || other.getId() == target.getId() || !other.isAlive() || isFriendly(player, other)) {
                continue;
            }
            if (!(other instanceof Mob living)) {
                continue;
            }
            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (living.hurtServer(level, level.damageSources().playerAttack(player), damage * ARCING_DAMAGE_FRACTION)) {
                EnchantmentHelper.doPostAttackEffects(level, living, level.damageSources().playerAttack(player));
                float yaw = player.getYRot() * ((float) Math.PI / 180.0F);
                living.push(-Mth.sin(yaw) * 0.5F, 0.1, Mth.cos(yaw) * 0.5F);
                Effects.slash(level, target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(), living.getX(), living.getY() + living.getBbHeight() / 2.0, living.getZ(), SLASH_LIFE);
                count++;
            }
        }
        if (count > 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.WIND.get(), SoundSource.PLAYERS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            Effects.slash(level, player.getX(), player.getY() + player.getBbHeight() / 2.0, player.getZ(), target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(), SLASH_LIFE);
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
        if (event.getLevel().isClientSide() || event.getEntity() == null) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand() == null ? InteractionHand.MAIN_HAND : event.getHand());
        int rank = InfusionEnchantmentHelper.level(held, InfusionEnchantment.SOUNDING);
        if (rank > 0 && player.isShiftKeyDown()) {
            held.hurtAndBreak(SOUNDING_DAMAGE, player, event.getHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
            ServerLevel level = (ServerLevel) event.getLevel();
            level.playSound(null, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, TCSounds.WANDFAIL.get(), SoundSource.BLOCKS, 0.2F,
                    0.2F + level.getRandom().nextFloat() * 0.2F);
            if (player instanceof ServerPlayer serverPlayer) {
                SoundingScan.perform(level, serverPlayer, event.getPos(), rank);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BreakBlockEvent event) {
        if (event.getLevel().isClientSide() || event.getPlayer() == null) {
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
        held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        EnchantMining.breakFurthest(level, pos, state, player);
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        ServerLevel level = event.getLevel();
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        addRareNugget(event, level, state);
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();

        if (InfusionEnchantmentHelper.has(held, InfusionEnchantment.REFINING)) {
            int fortune = 1 + InfusionEnchantmentHelper.level(held, InfusionEnchantment.REFINING);
            float chance = fortune * REFINING_CHANCE_PER_LEVEL;
            Item cluster = RefiningResults.clusterFor(state);
            if (cluster != null) {
                boolean changed = false;
                for (ItemEntity drop : event.getDrops()) {
                    if (level.getRandom().nextFloat() <= chance) {
                        drop.setItem(new ItemStack(cluster, drop.getItem().getCount()));
                        changed = true;
                    }
                }
                if (changed) {
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 0.7F + level.getRandom().nextFloat() * 0.2F);
                }
            }
        }

        if (!DESTRUCTIVE_RECURSION.get() && InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE) && !player.isShiftKeyDown() && held.isCorrectToolForDrops(state)) {
            DESTRUCTIVE_RECURSION.set(true);
            try {
                Direction face = Direction.getApproximateNearest(player.getViewVector(1.0F));
                for (int aa = -1; aa <= 1; aa++) {
                    for (int bb = -1; bb <= 1; bb++) {
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
                            held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                            EnchantMining.harvestBlock(level, player, offset, false);
                        }
                    }
                }
            } finally {
                DESTRUCTIVE_RECURSION.set(false);
            }
        }

        if (InfusionEnchantmentHelper.has(held, InfusionEnchantment.COLLECTOR) && !player.isShiftKeyDown()) {
            for (ItemEntity drop : event.getDrops()) {
                EntityFollowingItem follow = new EntityFollowingItem(level, drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy(), player, FOLLOW_TYPE);
                follow.setDeltaMovement(drop.getDeltaMovement());
                follow.setDefaultPickUpDelay();
                level.addFreshEntity(follow);
            }
            event.getDrops().clear();
        }

        if (InfusionEnchantmentHelper.has(held, InfusionEnchantment.LAMPLIGHT) && !player.isShiftKeyDown()) {
            if (level.isEmptyBlock(pos) && level.getMaxLocalRawBrightness(pos) < GLIMMER_LIGHT_THRESHOLD) {
                level.setBlock(pos, TCBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide()) {
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
                EntityFollowingItem follow = new EntityFollowingItem(level, drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy(), player, FOLLOW_TYPE);
                follow.setDeltaMovement(drop.getDeltaMovement());
                follow.setDefaultPickUpDelay();
                event.getDrops().add(follow);
            }
        }

        if (list.contains(InfusionEnchantment.ESSENCE)) {
            int rank = InfusionEnchantmentHelper.level(held, InfusionEnchantment.ESSENCE);
            AspectList aspects = EntityAspects.of(victim);
            if (!aspects.isEmpty()) {
                distillEssence(level, player, victim, aspects, rank, list.contains(InfusionEnchantment.COLLECTOR), event);
            }
        }
    }

    private static void distillEssence(ServerLevel level, Player player, LivingEntity victim, AspectList aspects, int rank, boolean collector, LivingDropsEvent event) {
        AspectList remaining = aspects;
        int produced = level.getRandom().nextInt(5) < rank ? 0 : 99;
        double x = victim.getX();
        double y = victim.getY() + victim.getEyeHeight();
        double z = victim.getZ();
        while (produced < rank && !remaining.isEmpty()) {
            List<AspectInstance> entries = remaining.entries();
            AspectInstance entry = entries.get(level.getRandom().nextInt(entries.size()));
            remaining = remaining.remove(entry.aspect(), 1);
            ItemStack crystal = EssentiaCrystalFactory.of(entry.aspect());
            if (collector) {
                event.getDrops().add(new EntityFollowingItem(level, x, y, z, crystal, player, FOLLOW_TYPE));
            } else {
                event.getDrops().add(new ItemEntity(level, x, y, z, crystal));
            }
            produced++;
            if (!remaining.isEmpty() && level.getRandom().nextInt(rank) == 0) {
                produced += 1 + level.getRandom().nextInt(2);
            }
        }
    }

    private static void addRareNugget(BlockDropsEvent event, ServerLevel level, BlockState state) {
        boolean silk = event.getBreaker() instanceof Player p
                && EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), p.getMainHandItem()) > 0;
        if (silk) {
            return;
        }
        float roll = level.getRandom().nextFloat();
        boolean rare = state.is(BlockTags.DIAMOND_ORES) && roll < 0.05F || state.is(BlockTags.EMERALD_ORES) && roll < 0.075F || state.is(BlockTags.LAPIS_ORES) && roll < 0.01F
                || state.is(BlockTags.COAL_ORES) && roll < 0.001F || state.is(BlockTags.REDSTONE_ORES) && roll < 0.01F || state.is(TCBlocks.ORE_QUARTZ.get()) && roll < TC_QUARTZ_NUGGET_CHANCE
                || state.is(Tags.Blocks.ORES_QUARTZ) && roll < 0.01F || state.is(TCBlockTags.ORES_AMBER) && roll < 0.05F;
        if (rare) {
            BlockPos pos = event.getPos();
            event.getDrops().add(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(TCItems.NUGGET_QUARTZ.get())));
        }
    }
}
