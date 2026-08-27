package com.leclowndu93150.thaumaturge.content.research.pool;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.network.ClientboundAspectGainPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundUpdateJEIAspectListPayload;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class AspectPools {
    public static final int SOFT_CAP = 100;
    private static final float HARD_CAP_FACTOR = 1.25F;
    private static final int DISCOVERY_BONUS = 2;
    private static final int PRIMAL_SEED_BASE = 15;
    private static final int PRIMAL_SEED_SPREAD = 5;

    private static final List<ResourceKey<IAspect>> PRIMALS = List.of(
            TCAspects.AER, TCAspects.TERRA, TCAspects.IGNIS, TCAspects.AQUA, TCAspects.ORDO, TCAspects.PERDITIO);

    private AspectPools() {}

    public static AspectPoolData data(Player player) {
        return player.getData(TCAttachments.ASPECT_POOL);
    }

    public static void sync(ServerPlayer player) {
        player.syncData(TCAttachments.ASPECT_POOL);
        PacketDistributor.sendToPlayer(player, new ClientboundUpdateJEIAspectListPayload());
    }

    public static void seedIfNew(ServerPlayer player) {
        AspectPoolData data = data(player);
        if (!data.isEmpty()) {
            return;
        }
        for (ResourceKey<IAspect> primal : PRIMALS) {
            data.add(primal.location(), PRIMAL_SEED_BASE + player.getRandom().nextInt(PRIMAL_SEED_SPREAD));
        }
        sync(player);
    }

    public static boolean isDiscovered(Player player, Holder<IAspect> aspect) {
        return aspect.value().isPrimal() || data(player).isDiscovered(idOf(aspect));
    }

    public static int amount(Player player, Holder<IAspect> aspect) {
        return data(player).amount(idOf(aspect));
    }

    public static boolean hasDiscoveredComponents(Player player, Holder<IAspect> aspect) {
        if (aspect.value().isPrimal()) {
            return true;
        }
        for (Holder<IAspect> component : aspect.value().components()) {
            if (!isDiscovered(player, component)) {
                return false;
            }
        }
        return true;
    }

    public static int grant(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        AspectPoolData data = data(player);
        ResourceLocation id = idOf(aspect);
        boolean discovery = !data.isDiscovered(id);
        int granted = amount;
        if (discovery) {
            granted += DISCOVERY_BONUS;
            player.sendSystemMessage(Component.translatable("tc.addaspectdiscovery", AspectComponents.trueName(aspect))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        int current = data.amount(id);
        if (current >= SOFT_CAP) {
            granted = (int) Math.sqrt(granted);
        }
        if (granted > 1 && current >= SOFT_CAP * HARD_CAP_FACTOR) {
            granted = 1;
        }
        if (granted <= 0 && !discovery) {
            return 0;
        }
        data.add(id, Math.max(0, granted));
        data.discover(id);
        sync(player);
        if (granted > 0) {
            if (data.tryClaimGrantSound(player.level().getGameTime())) {
                player.level()
                        .playSound(
                                null,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP,
                                SoundSource.PLAYERS,
                                0.2F,
                                0.9F + player.getRandom().nextFloat() * 0.2F);
            }
            PacketDistributor.sendToPlayer(player, new ClientboundAspectGainPayload(id, granted));
        }
        return granted;
    }

    public static void grantAll(ServerPlayer player, AspectList aspects) {
        for (AspectInstance entry : aspects.entries()) {
            if (hasDiscoveredComponents(player, entry.aspect())) {
                grant(player, entry.aspect(), entry.amount());
            } else {
                notifyMissingComponent(player, entry.aspect());
            }
        }
    }

    public static void notifyMissingComponent(ServerPlayer player, Holder<IAspect> aspect) {
        MutableComponent message = missingComponentHint(player, aspect);
        if (message != null) {
            player.sendSystemMessage(message.withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public static @Nullable MutableComponent missingComponentHint(Player player, Holder<IAspect> aspect) {
        for (Holder<IAspect> component : aspect.value().components()) {
            if (!isDiscovered(player, component)) {
                return missingComponentMessage(player, component);
            }
        }
        return null;
    }

    public static MutableComponent missingComponentMessage(Player player, Holder<IAspect> component) {
        if (!component.value().isPrimal() && hasDiscoveredComponents(player, component)) {
            return Component.translatable("tc.discoveryerror.derive", AspectComponents.composition(component));
        }
        return Component.translatable("tc.discoveryerror", AspectComponents.help(component));
    }

    public static boolean spend(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        AspectPoolData data = data(player);
        ResourceLocation id = idOf(aspect);
        if (data.amount(id) < amount) {
            return false;
        }
        data.add(id, -amount);
        sync(player);
        return true;
    }

    public static boolean canAfford(Player player, AspectList cost) {
        AspectPoolData data = data(player);
        for (AspectInstance entry : cost.entries()) {
            if (data.amount(idOf(entry.aspect())) < entry.amount()) {
                return false;
            }
        }
        return true;
    }

    public static boolean spendAll(ServerPlayer player, AspectList cost) {
        if (!canAfford(player, cost)) {
            return false;
        }
        AspectPoolData data = data(player);
        for (AspectInstance entry : cost.entries()) {
            data.add(idOf(entry.aspect()), -entry.amount());
        }
        sync(player);
        return true;
    }

    public static void refund(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        data(player).add(idOf(aspect), amount);
        sync(player);
        player.level()
                .playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.2F,
                        0.9F + player.getRandom().nextFloat() * 0.2F);
    }

    public static int grantAllForCommand(ServerPlayer player, int amount) {
        AspectPoolData data = data(player);
        List<Holder.Reference<IAspect>> aspects = player.registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .toList();
        for (Holder.Reference<IAspect> aspect : aspects) {
            ResourceLocation id = aspect.key().location();
            data.add(id, amount);
            data.discover(id);
        }
        sync(player);
        PacketDistributor.sendToPlayer(
                player,
                new ClientboundAspectGainPayload(aspects.getFirst().key().location(), 0));
        player.level()
                .playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        TCSounds.LEARN.get(),
                        SoundSource.PLAYERS,
                        0.5F,
                        1.0F);
        return aspects.size();
    }

    public static void grantForCommand(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        AspectPoolData data = data(player);
        ResourceLocation id = idOf(aspect);
        data.add(id, amount);
        data.discover(id);
        sync(player);
        PacketDistributor.sendToPlayer(player, new ClientboundAspectGainPayload(id, amount));
        player.level()
                .playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        TCSounds.LEARN.get(),
                        SoundSource.PLAYERS,
                        0.5F,
                        1.0F);
    }

    public static ResourceLocation idOf(Holder<IAspect> aspect) {
        return aspect.getKey().location();
    }
}
