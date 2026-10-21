package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.rifts.CapturedRift;
import com.leclowndu93150.thaumaturge.api.rifts.IRiftCaptureItem;
import com.leclowndu93150.thaumaturge.api.rifts.RiftCapturePolicy;
import com.leclowndu93150.thaumaturge.api.rifts.RiftContents;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationContext;
import com.leclowndu93150.thaumaturge.api.rifts.RiftResult;
import com.leclowndu93150.thaumaturge.api.rifts.RiftTransfers;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class RiftTransferTests {
    private RiftTransferTests() {}

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        if (Boolean.getBoolean("thaumaturge.gametest")) {
            event.register(Registries.ITEM, registry -> {
                registry.register(TTIds.rl("test_rift_vessel"), new TestVessel(false));
                registry.register(TTIds.rl("test_paid_rift_vessel"), new TestVessel(true));
            });
        }
    }

    private static final class TestVessel extends Item implements IRiftCaptureItem {
        private final boolean paid;

        private TestVessel(boolean paid) {
            super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, TTIds.rl(paid ? "test_paid_rift_vessel" : "test_rift_vessel"))).stacksTo(1));
            this.paid = paid;
        }

        @Override
        public RiftCapturePolicy capturePolicy() {
            return new RiftCapturePolicy(100, Optional.empty(), paid ? Map.of(TTAspects.IGNIS, 100) : Map.of());
        }
    }

    public static void register(TTTestRegistrar registrar) {
        registrar.add("rift_transfer/payment_checkpoint_before_and_after_save", 60, helper -> {
            EntityFluxRift rift = spawn(helper);
            var player = TTTestPlayers.create(helper);
            ItemStack beforeJar = vessel(true);
            ItemStack beforeWand = new ItemStack(TTItems.WAND.get());
            beforeWand.set(TTDataComponents.WAND_PARTS, new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_WOOD.get(), false));
            beforeWand = WandAccess.withVis(beforeWand, TTAspects.IGNIS, 500);
            ItemStack afterWand = WandAccess.withVis(beforeWand, TTAspects.IGNIS, 400);
            player.getInventory().setItem(0, beforeJar.copy());
            player.getInventory().setItem(1, beforeWand.copy());
            helper.getLevel().getServer().getPlayerList().saveAll();
            CapturedRift captured = CapturedRift.create(rift.getUUID(), new RiftContents(5, 12345, 25, rift.snapshot().revision()));
            ItemStack afterJar = beforeJar.copy();
            afterJar.set(TTDataComponents.CAPTURED_RIFT, captured);
            var location = new RiftTransferJournal.Location(helper.getLevel().dimension(), rift.position());
            var checkpoint = new RiftTransferJournal.Checkpoint(0, 1, beforeJar, beforeWand, afterWand, Optional.empty());
            var pending = new RiftTransferJournal.Entry(captured, player.getUUID(), location, Optional.empty(), true, Optional.of(checkpoint));
            try {
                helper.getLevel().save(null, true, false);
                RiftTransferJournal.write(helper.getLevel(), pending);
                player.getInventory().setItem(0, afterJar.copy());
                player.getInventory().setItem(1, afterWand.copy());
                rift.suspendTransfer();
                if (!RiftTransferOperations.reconcile(rift) || rift.isRemoved() || RiftTransferJournal.read(helper.getLevel(), rift.getUUID()).isPresent()
                        || WandAccess.getVis(player.getInventory().getItem(1), TTAspects.IGNIS) != 500 || RiftTransfers.captured(player.getInventory().getItem(0)).isPresent()) {
                    helper.fail("Interrupted capture before the player checkpoint did not restore source and payment");
                    return;
                }
                RiftTransferJournal.write(helper.getLevel(), pending);
                player.getInventory().setItem(0, afterJar.copy());
                player.getInventory().setItem(1, afterWand.copy());
                rift.suspendTransfer();
                RiftPlayerCheckpoint.save(player, captured.token());
                player.getInventory().setItem(2, player.getInventory().getItem(0));
                player.getInventory().setItem(0, ItemStack.EMPTY);
                helper.getLevel().getServer().getPlayerList().saveAll();
                if (RiftTransferOperations.reconcile(rift) || !rift.isRemoved() || RiftTransferJournal.read(helper.getLevel(), rift.getUUID()).orElseThrow().checkpoint().isPresent()
                        || WandAccess.getVis(player.getInventory().getItem(1), TTAspects.IGNIS) != 400
                        || !RiftTransfers.captured(player.getInventory().getItem(2)).filter(captured::equals).isPresent()) {
                    helper.fail("Interrupted capture after the checkpoint did not finalize exactly one paid capture");
                    return;
                }
            } catch (IOException exception) {
                helper.fail("Payment checkpoint failed: " + exception.getMessage());
                return;
            } finally {
                rift.discard();
            }
            helper.succeed();
        });
        registrar.add("rift_transfer/paid_capture_checkpoints_inventory", 60, helper -> {
            EntityFluxRift rift = spawn(helper);
            var player = TTTestPlayers.create(helper);
            var context = RiftOperationContext.player(player, TTIds.rl("test_paid_rift_transfer"));
            player.getInventory().setItem(0, vessel(true));
            ItemStack wand = new ItemStack(TTItems.WAND.get());
            wand.set(TTDataComponents.WAND_PARTS, new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_WOOD.get(), false));
            player.getInventory().setItem(1, WandAccess.withVis(wand, TTAspects.IGNIS, 500));
            var quote = RiftTransfers.prepareCapture(context, rift.getUUID(), rift.snapshot().revision(), 0, 1).orElseThrow();
            if (RiftTransfers.tryCapture(context, quote) != RiftResult.SUCCESS || !rift.isRemoved() || WandAccess.getVis(player.getInventory().getItem(1), TTAspects.IGNIS) != 400) {
                helper.fail("Paid capture did not checkpoint and debit exactly once");
                return;
            }
            try {
                CapturedRift captured = RiftTransfers.captured(player.getInventory().getItem(0)).orElseThrow();
                if (!RiftPlayerCheckpoint.committed(helper.getLevel(), player.getUUID(), captured.token())
                        || RiftTransferJournal.read(helper.getLevel(), rift.getUUID()).orElseThrow().checkpoint().isPresent()) {
                    helper.fail("Paid capture has no durable player checkpoint");
                    return;
                }
            } catch (IOException exception) {
                helper.fail("Payment checkpoint failed: " + exception.getMessage());
                return;
            }
            helper.succeed();
        });
        registrar.add("rift_transfer/opaque_payload_version_and_bounds", 20, helper -> {
            CapturedRift unknown = new CapturedRift(17, UUID.randomUUID(), UUID.randomUUID(), "opaque extension state");
            var encoded = CapturedRift.CODEC.encodeStart(JsonOps.INSTANCE, unknown).getOrThrow();
            if (!CapturedRift.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().equals(unknown) || unknown.contents().isPresent()) {
                helper.fail("Unknown rift state was changed or migrated");
                return;
            }
            encoded.getAsJsonObject().addProperty("state", "x".repeat(CapturedRift.MAX_STATE_BYTES + 1));
            if (CapturedRift.CODEC.parse(JsonOps.INSTANCE, encoded).result().isPresent()) {
                helper.fail("Oversized rift payload was accepted");
                return;
            }
            helper.succeed();
        });
        registrar.add("rift_transfer/slot_capture_release_replay", 60, helper -> {
            EntityFluxRift rift = spawn(helper);
            var player = TTTestPlayers.create(helper);
            var context = RiftOperationContext.player(player, TTIds.rl("test_rift_transfer"));
            player.getInventory().setItem(0, vessel(false));
            var beforePreparation = rift.snapshot();
            ItemStack beforeVessel = player.getInventory().getItem(0).copy();
            var stale = RiftTransfers.prepareCapture(context, rift.getUUID(), rift.snapshot().revision(), 0, -1).orElseThrow();
            if (!rift.snapshot().equals(beforePreparation) || !ItemStack.matches(player.getInventory().getItem(0), beforeVessel)) {
                helper.fail("Capture preparation mutated source or inventory");
                return;
            }
            player.getInventory().setItem(0, vessel(false));
            if (RiftTransfers.tryCapture(context, stale) != RiftResult.STALE || rift.isRemoved()) {
                helper.fail("Replacement vessel did not invalidate preparation");
                return;
            }
            var quote = RiftTransfers.prepareCapture(context, rift.getUUID(), rift.snapshot().revision(), 0, -1).orElseThrow();
            if (RiftTransfers.tryCapture(context, quote) != RiftResult.SUCCESS || !rift.isRemoved()) {
                helper.fail("Capture did not remove the source rift");
                return;
            }
            ItemStack duplicate = player.getInventory().getItem(0).copy();
            Vec3 target = rift.position().add(1, 0, 0);
            if (RiftTransfers.tryRelease(context, 0, target) != RiftResult.SUCCESS) {
                helper.fail("Captured rift could not be released");
                return;
            }
            EntityFluxRift released = (EntityFluxRift) helper.getLevel().getEntity(rift.getUUID());
            if (released == null || released.currentSize() != 5 || released.shapeSeed() != 12345 || released.stabilityValue() != 25) {
                helper.fail("Release changed rift identity or contents");
                return;
            }
            player.getInventory().setItem(0, duplicate);
            if (RiftTransfers.tryRelease(context, 0, target.add(1, 0, 0)) == RiftResult.SUCCESS) {
                helper.fail("Copied carrier released a second rift");
                return;
            }
            released.discard();
            helper.succeed();
        });
        registrar.add("rift_transfer/denied_spawn_preserves_carrier", 60, helper -> {
            EntityFluxRift rift = spawn(helper);
            var player = TTTestPlayers.create(helper);
            var context = RiftOperationContext.player(player, TTIds.rl("test_rift_transfer"));
            player.getInventory().setItem(0, vessel(false));
            var quote = RiftTransfers.prepareCapture(context, rift.getUUID(), rift.snapshot().revision(), 0, -1).orElseThrow();
            if (RiftTransfers.tryCapture(context, quote) != RiftResult.SUCCESS) {
                helper.fail("Capture failed");
                return;
            }
            ItemStack before = player.getInventory().getItem(0).copy();
            Consumer<EntityJoinLevelEvent> deny = event -> {
                if (event.getEntity().getUUID().equals(rift.getUUID())) {
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(deny);
            RiftResult result;
            try {
                result = RiftTransfers.tryRelease(context, 0, rift.position());
            } finally {
                NeoForge.EVENT_BUS.unregister(deny);
            }
            if (result == RiftResult.SUCCESS || !ItemStack.matches(before, player.getInventory().getItem(0))) {
                helper.fail("Denied spawn consumed or changed the carrier");
                return;
            }
            if (RiftTransfers.tryRelease(context, 0, rift.position()) != RiftResult.SUCCESS) {
                helper.fail("Denied spawn consumed the release token");
                return;
            }
            helper.getLevel().getEntity(rift.getUUID()).discard();
            helper.succeed();
        });
        registrar.add("rift_transfer/journal_failure_rolls_back_payment", 40, helper -> {
            EntityFluxRift rift = spawn(helper);
            var player = TTTestPlayers.create(helper);
            var context = RiftOperationContext.player(player, TTIds.rl("test_paid_rift_transfer"));
            player.getInventory().setItem(0, vessel(true));
            ItemStack wand = new ItemStack(TTItems.WAND.get());
            wand.set(TTDataComponents.WAND_PARTS, new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_WOOD.get(), false));
            wand = WandAccess.withVis(wand, TTAspects.IGNIS, 500);
            player.getInventory().setItem(1, wand);
            var quote = RiftTransfers.prepareCapture(context, rift.getUUID(), rift.snapshot().revision(), 0, 1).orElseThrow();
            Path path = helper.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("data/thaumaturge/rift_transfers").resolve(rift.getUUID() + ".dat");
            try {
                Files.createDirectories(path.resolveSibling(path.getFileName() + ".tmp"));
                if (RiftTransfers.tryCapture(context, quote) == RiftResult.SUCCESS || rift.isRemoved() || WandAccess.getVis(wand, TTAspects.IGNIS) != 500
                        || RiftTransfers.captured(player.getInventory().getItem(0)).isPresent()) {
                    helper.fail("Failed capture changed payment, source or vessel");
                    return;
                }
            } catch (IOException exception) {
                helper.fail("Could not prepare journal failure: " + exception.getMessage());
                return;
            } finally {
                try {
                    Files.deleteIfExists(path);
                    Files.deleteIfExists(path.resolveSibling(path.getFileName() + ".tmp"));
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
                rift.discard();
            }
            helper.succeed();
        });
        registrar.add("rift_transfer/pending_identity_and_settled_movement", 30, helper -> {
            EntityFluxRift rift = spawn(helper);
            CapturedRift captured = CapturedRift.create(rift.getUUID(), new RiftContents(5, 12345, 25));
            var location = new RiftTransferJournal.Location(helper.getLevel().dimension(), rift.position());
            try {
                RiftTransferJournal.write(helper.getLevel(), new RiftTransferJournal.Entry(captured, UUID.randomUUID(), location, Optional.of(location), false));
                rift.transferToken(captured.token());
                if (RiftTransferOperations.reconcile(rift)) {
                    helper.fail("Fresh target settled without a persisted token");
                    return;
                }
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
                rift.addAdditionalSaveData(output);
                rift.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
                if (!RiftTransferOperations.reconcile(rift) || !RiftTransferJournal.read(helper.getLevel(), rift.getUUID()).orElseThrow().settled()) {
                    helper.fail("Persisted target did not settle after reload");
                    return;
                }
                rift.setPos(rift.position().add(1, 0, 0));
                if (!RiftTransferOperations.reconcile(rift)) {
                    helper.fail("Settled movement lost transfer ownership");
                    return;
                }
            } catch (IOException exception) {
                helper.fail("Journal failure: " + exception.getMessage());
                return;
            } finally {
                rift.discard();
            }
            helper.succeed();
        });
    }

    private static ItemStack vessel(boolean paid) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(TTIds.rl(paid ? "test_paid_rift_vessel" : "test_rift_vessel")));
    }

    private static EntityFluxRift spawn(GameTestHelper helper) {
        EntityFluxRift rift = TTEntities.FLUX_RIFT.get().create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        rift.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        rift.resize(5);
        rift.reseed(12345);
        rift.adjustStability(25);
        if (!helper.getLevel().addFreshEntity(rift)) {
            throw new IllegalStateException("Could not create test rift");
        }
        return rift;
    }
}
