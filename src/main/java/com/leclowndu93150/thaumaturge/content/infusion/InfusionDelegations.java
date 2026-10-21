package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.infusion.InfusionAutomationPolicy;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionDelegation;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionDelegation.Context;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionDelegation.Device;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionDelegation.Handle;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public final class InfusionDelegations implements InfusionDelegation.Bindings {
    @Override
    public Optional<Handle> grant(ServerPlayer owner, BlockPos devicePosition, BlockPos matrixPosition, Set<ResourceKey<Recipe<?>>> recipes, long lifetimeTicks) {
        return issue(owner, devicePosition, matrixPosition, recipes, lifetimeTicks, null);
    }

    @Override
    public Optional<Handle> grant(ServerPlayer owner, BlockPos devicePosition, BlockPos matrixPosition, Set<ResourceKey<Recipe<?>>> recipes, long lifetimeTicks, Identifier policy) {
        return issue(owner, devicePosition, matrixPosition, recipes, lifetimeTicks, policy);
    }

    private Optional<Handle> issue(ServerPlayer owner, BlockPos devicePosition, BlockPos matrixPosition, Set<ResourceKey<Recipe<?>>> recipes, long lifetimeTicks, @Nullable Identifier policy) {
        MinecraftServer server = owner.level().getServer();
        if (!server.isSameThread() || server.getPlayerList().getPlayer(owner.getUUID()) != owner || lifetimeTicks <= 0 || recipes.isEmpty()) {
            return Optional.empty();
        }
        InfusionDelegationData data = InfusionDelegationData.get(server);
        InfusionMatrixContext matrix = InfusionMatrixContext.of(owner, matrixPosition);
        if (!data.enabled || !owner.level().hasChunkAt(devicePosition) || !owner.level().hasChunkAt(matrixPosition) || !owner.level().mayInteract(owner, devicePosition)
                || !owner.level().mayInteract(owner, matrixPosition) || !(owner.level().getBlockEntity(matrixPosition) instanceof BlockEntityInfusionMatrix)
                || !(owner.level().getBlockEntity(devicePosition) instanceof Device device) || !owner.getUUID().equals(device.infusionOwner()) || device.infusionDeviceId() == null
                || device.infusionAddon() == null || !permitted(matrix, devicePosition)) {
            return Optional.empty();
        }
        long now = server.overworld().getGameTime();
        int surcharge = policy == null ? 0 : InfusionAutomationPolicy.resolve(policy, matrix, owner);
        if (lifetimeTicks > Long.MAX_VALUE - now || surcharge < 0) {
            return Optional.empty();
        }
        UUID id = UUID.randomUUID();
        data.grants.values().removeIf(grant -> grant.dimension().equals(owner.level().dimension().identifier()) && grant.position().equals(devicePosition));
        data.grants.put(id, new InfusionDelegationData.Grant(id, owner.getUUID(), device.infusionDeviceId(), device.infusionAddon(), owner.level().dimension().identifier(), devicePosition,
                matrixPosition, recipes.stream().map(ResourceKey::identifier).sorted().toList(), data.revision, now + lifetimeTicks, surcharge));
        data.setDirty();
        return Optional.of(new Handle(id));
    }

    @Override
    public boolean revoke(ServerPlayer owner, Handle handle) {
        MinecraftServer server = owner.level().getServer();
        if (!server.isSameThread() || server.getPlayerList().getPlayer(owner.getUUID()) != owner) {
            return false;
        }
        InfusionDelegationData data = InfusionDelegationData.get(server);
        InfusionDelegationData.Grant grant = data.grants.get(handle.id());
        if (grant == null || !grant.owner().equals(owner.getUUID())) {
            return false;
        }
        data.grants.remove(handle.id());
        data.setDirty();
        return true;
    }

    @Override
    public void configure(MinecraftServer server, boolean enabled, boolean allowOffline) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Infusion policy must be changed on the server thread");
        }
        InfusionDelegationData data = InfusionDelegationData.get(server);
        if (data.enabled != enabled || data.offline != allowOffline) {
            data.enabled = enabled;
            data.offline = allowOffline;
            data.revision++;
            data.grants.clear();
            data.setDirty();
        }
    }

    public static boolean valid(Context context, @Nullable ResourceKey<Recipe<?>> recipe) {
        InfusionMatrixContext matrix = context.matrix();
        MinecraftServer server = matrix.level().getServer();
        if (!server.isSameThread() || !matrix.level().hasChunkAt(matrix.position()) || !matrix.level().hasChunkAt(context.devicePosition())) {
            return false;
        }
        InfusionDelegationData data = InfusionDelegationData.get(server);
        InfusionDelegationData.Grant grant = data.grants.get(context.handle().id());
        if (!data.enabled || grant == null || grant.revision() != data.revision || grant.expires() <= server.overworld().getGameTime() || !grant.owner().equals(matrix.actingPlayer())
                || !grant.dimension().equals(matrix.level().dimension().identifier()) || !grant.matrix().equals(matrix.position()) || !grant.position().equals(context.devicePosition())
                || (recipe != null && !grant.recipes().contains(recipe.identifier())) || !(matrix.level().getBlockEntity(matrix.position()) instanceof BlockEntityInfusionMatrix)) {
            return false;
        }
        if (!(matrix.level().getBlockEntity(context.devicePosition()) instanceof Device device) || !grant.owner().equals(device.infusionOwner()) || !grant.device().equals(device.infusionDeviceId())
                || !grant.addon().equals(device.infusionAddon())) {
            data.grants.remove(grant.id());
            data.setDirty();
            return false;
        }
        ServerPlayer owner = server.getPlayerList().getPlayer(grant.owner());
        if (owner != null && (!matrix.level().mayInteract(owner, matrix.position()) || !matrix.level().mayInteract(owner, context.devicePosition()))) {
            return false;
        }
        return (data.offline || owner != null) && permitted(matrix, context.devicePosition());
    }

    public static @Nullable IPlayerKnowledge knowledge(Context context) {
        if (!valid(context, null)) {
            return null;
        }
        MinecraftServer server = context.matrix().level().getServer();
        ServerPlayer owner = server.getPlayerList().getPlayer(context.matrix().actingPlayer());
        if (owner != null) {
            return KnowledgeAccess.of(owner);
        }
        Path path = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(context.matrix().actingPlayer() + ".dat");
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            CompoundTag player = NbtIo.readCompressed(path, NbtAccounter.create(16 * 1024 * 1024L));
            return player.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY).flatMap(attachments -> attachments.getCompound(TTIds.rl("knowledge").toString()))
                    .flatMap(tag -> PlayerKnowledge.CODEC.codec().parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag).result()).orElse(null);
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    public static int surcharge(Context context) {
        if (!valid(context, null)) {
            return -1;
        }
        return InfusionDelegationData.get(context.matrix().level().getServer()).grants.get(context.handle().id()).surcharge();
    }

    private static boolean permitted(InfusionMatrixContext matrix, BlockPos devicePosition) {
        return !NeoForge.EVENT_BUS.post(new InfusionDelegation.PermissionCheck(matrix, devicePosition)).isCanceled();
    }
}
