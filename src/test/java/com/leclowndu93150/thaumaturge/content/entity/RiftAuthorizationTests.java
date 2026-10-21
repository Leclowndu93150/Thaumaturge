package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization.Action;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationContext;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class RiftAuthorizationTests {
    private static final Identifier ONLINE = TTIds.rl("gametest/rift_online");
    private static final Identifier OFFLINE = TTIds.rl("gametest/rift_offline");

    private RiftAuthorizationTests() {}

    @SubscribeEvent
    public static void registerPolicies(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) {
            return;
        }
        RiftAuthorization.Resolver resolver = new RiftAuthorization.Resolver() {
            @Override
            public Optional<RiftAuthorization.Machine> resolve(ServerLevel level, BlockPos position) {
                return level.getBlockEntity(position) instanceof Machine machine ? Optional.of(new RiftAuthorization.Machine(machine.owner, machine.instance)) : Optional.empty();
            }

            @Override
            public boolean authorizeOffline(ServerLevel level, BlockPos position, RiftAuthorization.Machine identity, Action action, AABB volume) {
                return level.getBlockEntity(position) instanceof Machine machine && machine.allowOffline;
            }
        };
        RiftAuthorization.register(ONLINE, resolver, false);
        RiftAuthorization.register(OFFLINE, resolver, true);
    }

    public static void register(TTTestRegistrar registrar) {
        registrar.add("rift_api/machine_grant_scope_and_revocation", 40, helper -> {
            var player = TTTestPlayers.create(helper);
            var level = helper.getLevel();
            BlockPos position = helper.absolutePos(new BlockPos(2, 2, 2));
            level.setBlockAndUpdate(position, Blocks.CHEST.defaultBlockState());
            Machine machine = new Machine(position, player.getUUID());
            level.setBlockEntity(machine);
            try {
                var handle = RiftAuthorization.grant(player, position, ONLINE, Set.of(Action.MOVE), 4, 100).orElseThrow();
                var context = RiftOperationContext.machine(level, position, handle, ONLINE);
                check(RiftAuthorizations.valid(context, Action.MOVE, position), "Valid machine grant rejected");
                check(!RiftAuthorizations.valid(context, Action.MUTATE, position), "Move grant allowed property mutation");
                check(!RiftAuthorizations.valid(context, Action.MOVE, position.offset(5, 0, 0)), "Grant escaped radius");
                check(!RiftAuthorizations.valid(RiftOperationContext.machine(level, position.above(), handle, ONLINE), Action.MOVE, position), "Grant authorized a different machine position");
                var data = RiftAuthorizationData.get(level.getServer());
                var grant = data.grants.get(handle.id());
                var encoded = RiftAuthorizationData.Grant.CODEC.encodeStart(JsonOps.INSTANCE, grant).getOrThrow();
                data.grants.put(handle.id(), RiftAuthorizationData.Grant.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
                check(RiftAuthorizations.valid(context, Action.MOVE, position), "Serialized grant lost authority");
                player.getAbilities().mayBuild = false;
                check(!RiftAuthorizations.valid(context, Action.MOVE, position), "Machine ignored owner build restriction");
                check(RiftAuthorization.grant(player, position, ONLINE, Set.of(Action.MOVE), 4, 100).isEmpty(), "Restricted owner issued authority");
                check(!RiftAuthorizations.valid(RiftOperationContext.player(player, ONLINE), Action.MOVE, position), "Direct actor ignored build restriction");
                player.getAbilities().mayBuild = true;
                check(RiftAuthorization.revoke(player, handle), "Owner could not revoke grant");
                check(!RiftAuthorizations.valid(context, Action.MOVE, position), "Revoked grant remained valid");
            } finally {
                level.getServer().getPlayerList().remove(player);
            }
            helper.succeed();
        });

        registrar.add("rift_api/machine_replacement_invalidates_grant", 40, helper -> {
            var player = TTTestPlayers.create(helper);
            var level = helper.getLevel();
            BlockPos position = helper.absolutePos(new BlockPos(2, 2, 2));
            level.setBlockAndUpdate(position, Blocks.CHEST.defaultBlockState());
            Machine machine = new Machine(position, player.getUUID());
            level.setBlockEntity(machine);
            try {
                var handle = RiftAuthorization.grant(player, position, ONLINE, Set.of(Action.CAPTURE), 4, 100).orElseThrow();
                var context = RiftOperationContext.machine(level, position, handle, ONLINE);
                UUID instance = machine.instance;
                machine.instance = UUID.randomUUID();
                check(!RiftAuthorizations.valid(context, Action.CAPTURE, position), "Replacement retained authority");
                machine.instance = instance;
                check(!RiftAuthorizations.valid(context, Action.CAPTURE, position), "Restored identity revived authority");
                handle = RiftAuthorization.grant(player, position, ONLINE, Set.of(Action.CAPTURE), 4, 100).orElseThrow();
                context = RiftOperationContext.machine(level, position, handle, ONLINE);
                machine.owner = UUID.randomUUID();
                check(!RiftAuthorizations.valid(context, Action.CAPTURE, position), "Changed owner retained authority");
                machine.owner = player.getUUID();
                check(!RiftAuthorizations.valid(context, Action.CAPTURE, position), "Restored owner revived authority");
            } finally {
                level.getServer().getPlayerList().remove(player);
            }
            helper.succeed();
        });

        registrar.add("rift_api/offline_requires_explicit_machine_policy", 40, helper -> {
            var player = TTTestPlayers.create(helper);
            var level = helper.getLevel();
            BlockPos position = helper.absolutePos(new BlockPos(2, 2, 2));
            level.setBlockAndUpdate(position, Blocks.CHEST.defaultBlockState());
            Machine machine = new Machine(position, player.getUUID());
            level.setBlockEntity(machine);
            var online = RiftAuthorization.grant(player, position, ONLINE, Set.of(Action.RELEASE), 4, 100).orElseThrow();
            var offline = RiftAuthorization.grant(player, position, OFFLINE, Set.of(Action.RELEASE), 4, 100).orElseThrow();
            level.getServer().getPlayerList().remove(player);
            var context = RiftOperationContext.machine(level, position, offline, OFFLINE);
            check(!RiftAuthorizations.valid(RiftOperationContext.machine(level, position, online, ONLINE), Action.RELEASE, position), "Online-only policy authorized offline owner");
            check(!RiftAuthorizations.valid(context, Action.RELEASE, position), "Offline operation ignored resolver denial");
            machine.allowOffline = true;
            check(RiftAuthorizations.validate(context, Action.RELEASE, new AABB(position)), "Explicit offline policy rejected valid volume");
            machine.allowOffline = false;
            check(!RiftAuthorizations.validate(context, Action.RELEASE, new AABB(position)), "Offline claim change ignored");
            helper.succeed();
        });
    }

    private static void check(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static final class Machine extends ChestBlockEntity {
        private UUID owner;
        private UUID instance = UUID.randomUUID();
        private boolean allowOffline;

        private Machine(BlockPos position, UUID owner) {
            super(position, Blocks.CHEST.defaultBlockState());
            this.owner = owner;
        }
    }
}
