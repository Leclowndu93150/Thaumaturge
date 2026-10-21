package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Context;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.InventorySlot;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Status;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public final class ResearchNoteApiTests {
    private ResearchNoteApiTests() {}

    private static ServerPlayer player(GameTestHelper helper, String name) {
        ServerPlayer player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name));
        player.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1)).getCenter());
        return player;
    }

    private static ItemStack note(ServerPlayer player) {
        var holder = player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).listElements()
                .filter(candidate -> ResearchNotes.theoryRowsBefore(candidate.value(), candidate.value().stages().size()) > 0).findFirst().orElseThrow();
        var entry = holder.value();
        var knowledge = KnowledgeAccess.of(player);
        knowledge.addResearch(holder.key().identifier());
        knowledge.setResearchStage(holder.key().identifier(), entry.stages().size() - 1);
        ItemStack stack = new ItemStack(TTItems.RESEARCH_NOTE.get());
        stack.set(TTDataComponents.RESEARCH_NOTE.get(),
                NoteGenerator.generate(holder.key().identifier(), 0, ResearchNotes.anchors(player.registryAccess(), entry), entry.complexity(), player.getRandom()));
        player.getInventory().setItem(0, stack);
        return stack;
    }

    private static String fingerprint(ServerPlayer player, ItemStack stack) {
        return ResearchNotesAccess.inspect(stack, player.registryAccess()).note().orElseThrow().fingerprint();
    }

    public static void register(TTTestRegistrar registrar) {
        registrar.add("research_api/complete_existing", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_complete");
            ItemStack stack = note(player);
            var data = ResearchNotes.dataOf(stack);
            int stage = KnowledgeAccess.of(player).researchStage(data.entry());
            var result = ResearchNotesAccess.completeExisting(new Context(player, source -> true), new InventorySlot(0), fingerprint(player, stack), transaction -> true);
            ItemStack finished = player.getInventory().getItem(0);
            if (result != Status.COMPLETE || !ResearchNotes.dataOf(finished).complete() || KnowledgeAccess.of(player).isResearchKnown(data.learnKey())
                    || KnowledgeAccess.of(player).researchStage(data.entry()) != stage) {
                helper.fail("Completing the note changed knowledge or failed to replace the item");
                return;
            }
            var ops = RegistryOps.create(JsonOps.INSTANCE, player.registryAccess());
            ItemStack restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, finished).getOrThrow()).getOrThrow();
            if (!ResearchNotes.dataOf(restored).complete()) {
                helper.fail("Completed note did not survive serialization");
                return;
            }
            AtomicBoolean charged = new AtomicBoolean();
            if (ResearchNotesAccess.completeExisting(new Context(player, source -> true), new InventorySlot(0), fingerprint(player, finished), transaction -> {
                charged.set(true);
                return true;
            }) != Status.COMPLETE || charged.get()) {
                helper.fail("Repeated completion charged for a finished note");
                return;
            }
            int completed = AspectPools.data(player).completedNotes();
            player.setItemInHand(InteractionHand.MAIN_HAND, restored);
            restored.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
            if (!KnowledgeAccess.of(player).isResearchKnown(data.learnKey()) || AspectPools.data(player).completedNotes() != completed + 1 || !restored.isEmpty()) {
                helper.fail("The restored note did not learn exactly once through normal use");
                return;
            }
            helper.succeed();
        });

        registrar.add("research_api/reject_replacement", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_replace");
            ItemStack stack = note(player);
            var prepared = ResearchNotesAccess.prepareCompletion(new Context(player, source -> true), new InventorySlot(0), fingerprint(player, stack));
            player.getInventory().setItem(0, stack.copy());
            AtomicBoolean charged = new AtomicBoolean();
            Status status = prepared.completion().orElseThrow().complete(transaction -> {
                charged.set(true);
                return true;
            });
            if (status != Status.STALE || charged.get() || ResearchNotes.dataOf(player.getInventory().getItem(0)).complete()) {
                helper.fail("A replaced stack was completed or charged");
                return;
            }
            helper.succeed();
        });

        registrar.add("research_api/recheck_authorization", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_auth");
            ItemStack stack = note(player);
            AtomicBoolean allowed = new AtomicBoolean(true);
            var prepared = ResearchNotesAccess.prepareCompletion(new Context(player, source -> allowed.get()), new InventorySlot(0), fingerprint(player, stack));
            allowed.set(false);
            AtomicBoolean charged = new AtomicBoolean();
            Status status = prepared.completion().orElseThrow().complete(transaction -> {
                charged.set(true);
                return true;
            });
            if (status != Status.UNAUTHORIZED || charged.get()) {
                helper.fail("Authorization was not rechecked before payment");
                return;
            }
            helper.succeed();
        });

        registrar.add("research_api/recheck_research", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_research");
            ItemStack stack = note(player);
            var prepared = ResearchNotesAccess.prepareCompletion(new Context(player, source -> true), new InventorySlot(0), fingerprint(player, stack));
            KnowledgeAccess.of(player).removeResearch(ResearchNotes.dataOf(stack).entry());
            AtomicBoolean charged = new AtomicBoolean();
            Status status = prepared.completion().orElseThrow().complete(transaction -> {
                charged.set(true);
                return true;
            });
            if (status != Status.UNAUTHORIZED || charged.get() || ResearchNotes.dataOf(stack).complete()) {
                helper.fail("Research availability was not rechecked before completion");
                return;
            }
            helper.succeed();
        });

        registrar.add("research_api/invalid_and_payment_failure", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_invalid");
            ItemStack stack = note(player);
            var context = new Context(player, source -> true);
            var source = new InventorySlot(0);
            if (ResearchNotesAccess.inspect(stack, HolderLookup.Provider.create(Stream.empty())).status() != Status.INVALID) {
                helper.fail("Missing registries did not reject note inspection");
                return;
            }
            if (ResearchNotesAccess.completeExisting(context, source, fingerprint(player, stack), transaction -> false) != Status.PAYMENT_FAILED || ResearchNotes.dataOf(stack).complete()) {
                helper.fail("A failed payment completed the note");
                return;
            }
            stack.setCount(2);
            if (ResearchNotesAccess.inspect(stack, player.registryAccess()).status() != Status.INVALID
                    || ResearchNotesAccess.inspect(new ItemStack(TTItems.RESEARCH_NOTE.get()), player.registryAccess()).status() != Status.INVALID
                    || ResearchNotesAccess.inspect(new ItemStack(Items.PAPER), player.registryAccess()).status() != Status.NOT_A_NOTE) {
                helper.fail("Invalid or merged notes were accepted");
                return;
            }
            helper.succeed();
        });

        registrar.add("research_api/reject_spectator", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_spectator");
            ItemStack stack = note(player);
            var prepared = ResearchNotesAccess.prepareCompletion(new Context(player, source -> true), new InventorySlot(0), fingerprint(player, stack));
            player.setGameMode(GameType.SPECTATOR);
            AtomicBoolean charged = new AtomicBoolean();
            Status status = prepared.completion().orElseThrow().complete(transaction -> {
                charged.set(true);
                return true;
            });
            player.setGameMode(GameType.SURVIVAL);
            if (status != Status.UNAUTHORIZED || charged.get() || ResearchNotes.dataOf(stack).complete()) {
                helper.fail("Spectator completed or paid for a note");
                return;
            }
            helper.succeed();
        });

        registrar.add("research_api/transaction_rollback", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_rollback");
            ItemStack stack = note(player);
            AtomicInteger fuel = new AtomicInteger(1);
            SnapshotJournal<Integer> journal = new SnapshotJournal<>() {
                @Override
                protected Integer createSnapshot() {
                    return fuel.get();
                }

                @Override
                protected void revertToSnapshot(Integer snapshot) {
                    fuel.set(snapshot);
                }
            };
            AtomicBoolean allowed = new AtomicBoolean(true);
            var status = ResearchNotesAccess.completeExisting(new Context(player, source -> allowed.get()), new InventorySlot(0), fingerprint(player, stack), transaction -> {
                journal.updateSnapshots(transaction);
                fuel.decrementAndGet();
                allowed.set(false);
                return true;
            });
            if (status != Status.UNAUTHORIZED || fuel.get() != 1 || ResearchNotes.dataOf(stack).complete()) {
                helper.fail("Revalidation failure did not roll back final payment");
                return;
            }
            helper.succeed();
        });

        registrar.add("research_api/dropped_note_movement", 20, helper -> {
            ServerPlayer player = player(helper, "note_api_entity");
            ItemStack stack = note(player);
            player.getInventory().setItem(0, ItemStack.EMPTY);
            ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            entity.setNoPickUpDelay();
            helper.getLevel().addFreshEntity(entity);
            var source = new ResearchNotesAccess.DroppedNote(entity);
            var prepared = ResearchNotesAccess.prepareCompletion(new Context(player, target -> true), source, fingerprint(player, stack));
            entity.setPos(entity.position().add(1, 0, 0));
            AtomicBoolean charged = new AtomicBoolean();
            Status status = prepared.completion().orElseThrow().complete(transaction -> {
                charged.set(true);
                return true;
            });
            if (status != Status.STALE || charged.get() || ResearchNotes.dataOf(entity.getItem()).complete()) {
                helper.fail("Moved dropped note was completed or charged");
                return;
            }
            entity.discard();
            helper.succeed();
        });
    }
}
