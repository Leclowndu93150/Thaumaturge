package com.leclowndu93150.thaumaturge.content.research.note;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Anchor;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Completion;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Context;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.DroppedNote;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.FinalCost;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Inspection;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.InventorySlot;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Note;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Preparation;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Source;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess.Status;
import com.leclowndu93150.thaumaturge.api.research.ResearchUnlockConditions;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class ResearchNoteAccessImpl implements ResearchNotesAccess.Provider {
    public static final ResearchNoteAccessImpl INSTANCE = new ResearchNoteAccessImpl();

    private ResearchNoteAccessImpl() {}

    @Override
    public Inspection inspect(ItemStack stack, HolderLookup.Provider registries) {
        if (!stack.is(TTItems.RESEARCH_NOTE.get())) {
            return new Inspection(Status.NOT_A_NOTE, Optional.empty());
        }
        ResearchNoteData data = ResearchNotes.dataOf(stack);
        if (stack.getCount() != 1 || data == null || !valid(data, registries)) {
            return new Inspection(Status.INVALID, Optional.empty());
        }
        var encoded = ItemStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), stack).result();
        if (encoded.isEmpty()) {
            return new Inspection(Status.INVALID, Optional.empty());
        }
        List<Anchor> anchors = data.cells().stream().filter(cell -> cell.type() == ResearchNoteData.TYPE_ROOT)
                .map(cell -> new Anchor(cell.hex().q(), cell.hex().r(), cell.aspect().orElseThrow().unwrapKey().orElseThrow().identifier())).toList();
        Note note = new Note(fingerprint(encoded.get().toString()), data.entry(), data.index(), anchors, data.cells().size(), data.complete());
        return new Inspection(data.complete() ? Status.COMPLETE : Status.READY, Optional.of(note));
    }

    private static boolean valid(ResearchNoteData data, HolderLookup.Provider registries) {
        var research = registries.lookup(IResearchEntry.REGISTRY_KEY);
        var aspects = registries.lookup(IAspect.REGISTRY_KEY);
        if (research.isEmpty() || aspects.isEmpty()) {
            return false;
        }
        IResearchEntry entry = research.get().get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, data.entry())).map(holder -> holder.value()).orElse(null);
        if (entry == null || data.index() < 0 || data.index() >= ResearchNotes.theoryRowsBefore(entry, entry.stages().size()) || data.copies() < 0 || data.cells().isEmpty()
                || data.cells().size() > 61) {
            return false;
        }
        var positions = new HashSet<HexGrid.Hex>();
        boolean hasRoot = false;
        for (ResearchNoteData.Cell cell : data.cells()) {
            if (!positions.add(cell.hex()) || Math.abs((long) cell.hex().q()) > 4 || Math.abs((long) cell.hex().r()) > 4 || Math.abs((long) cell.hex().q() + cell.hex().r()) > 4
                    || cell.type() < ResearchNoteData.TYPE_BLANK || cell.type() > ResearchNoteData.TYPE_PLACED || (cell.type() == ResearchNoteData.TYPE_BLANK) != cell.aspect().isEmpty()
                    || cell.aspect().isPresent() && cell.aspect().get().unwrapKey().flatMap(key -> aspects.get().get(key)).isEmpty()) {
                return false;
            }
            hasRoot |= cell.type() == ResearchNoteData.TYPE_ROOT;
        }
        return hasRoot;
    }

    private static String fingerprint(String encoded) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(encoded.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public Preparation prepare(Context context, Source source, String expectedFingerprint) {
        requireServerThread(context);
        Plan plan = new Plan(context, source, expectedFingerprint);
        Status status = plan.validate();
        return new Preparation(status, status == Status.READY ? Optional.of(plan) : Optional.empty());
    }

    private static void requireServerThread(Context context) {
        if (!context.player().level().getServer().isSameThread()) {
            throw new IllegalStateException("Research note completion requires the server thread");
        }
    }

    private static ItemStack stack(Context context, Source source) {
        if (source instanceof InventorySlot slot) {
            var inventory = context.player().getInventory();
            return slot.slot() >= 0 && slot.slot() < inventory.getContainerSize() ? inventory.getItem(slot.slot()) : ItemStack.EMPTY;
        }
        return ((DroppedNote) source).entity().getItem();
    }

    private static final class Plan implements Completion {
        private final Context context;
        private final Source source;
        private final String fingerprint;
        private final ItemStack original;
        private final ServerLevel level;
        private final Vec3 position;
        private boolean used;

        private Plan(Context context, Source source, String fingerprint) {
            this.context = context;
            this.source = source;
            this.fingerprint = fingerprint;
            original = stack(context, source);
            level = (ServerLevel) context.player().level();
            position = source instanceof DroppedNote dropped ? dropped.entity().position() : context.player().position();
        }

        private Status validate() {
            if (context.player().isRemoved() || context.player().level() != level || !context.player().isAlive() || context.player().isSpectator() || !context.authorization().test(source)) {
                return Status.UNAUTHORIZED;
            }
            if (!level.hasChunkAt(context.player().blockPosition())) {
                return Status.STALE;
            }
            if (source instanceof DroppedNote dropped) {
                var entity = dropped.entity();
                if (entity.isRemoved() || entity.level() != level || level.getEntity(entity.getId()) != entity || !level.hasChunkAt(entity.blockPosition()) || !entity.position().equals(position)) {
                    return Status.STALE;
                }
                if (entity.hasPickUpDelay() || entity.getTarget() != null && !entity.getTarget().equals(context.player().getUUID())) {
                    return Status.UNAUTHORIZED;
                }
            }
            ItemStack current = stack(context, source);
            if (current != original) {
                return Status.STALE;
            }
            Inspection inspection = INSTANCE.inspect(current, level.registryAccess());
            if (inspection.status() != Status.READY) {
                return inspection.status();
            }
            Note note = inspection.note().orElseThrow();
            if (!note.fingerprint().equals(fingerprint)) {
                return Status.STALE;
            }
            var knowledge = KnowledgeAccess.of(context.player());
            IResearchEntry entry = level.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).getOrThrow(ResourceKey.create(IResearchEntry.REGISTRY_KEY, note.entry())).value();
            int stage = Math.max(0, knowledge.researchStage(note.entry()));
            if (!knowledge.isResearchKnown(note.entry()) || knowledge.isResearchComplete(note.entry()) || stage >= entry.stages().size()
                    || note.theoryRow() >= ResearchNotes.theoryRowsBefore(entry, stage + 1) || knowledge.isResearchKnown(ResearchNoteData.learnKey(note.entry(), note.theoryRow()))
                    || !ResearchUnlockConditions.passes(context.player(), knowledge, note.entry())) {
                return Status.UNAUTHORIZED;
            }
            return Status.READY;
        }

        @Override
        public Status complete(FinalCost cost) {
            requireServerThread(context);
            if (used) {
                return Status.STALE;
            }
            used = true;
            Status status = validate();
            if (status != Status.READY) {
                return status;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                if (!cost.pay(transaction)) {
                    return Status.PAYMENT_FAILED;
                }
                status = validate();
                if (status != Status.READY) {
                    return status;
                }
                new SnapshotJournal<ItemStack>() {
                    @Override
                    protected ItemStack createSnapshot() {
                        return original;
                    }

                    @Override
                    protected void revertToSnapshot(ItemStack snapshot) {
                        replace(snapshot);
                    }

                    @Override
                    protected void onRootCommit(ItemStack snapshot) {
                        context.player().getInventory().setChanged();
                        context.player().containerMenu.broadcastChanges();
                    }
                }.updateSnapshots(transaction);
                ItemStack finished = original.copy();
                finished.set(TTDataComponents.RESEARCH_NOTE.get(), ResearchNotes.dataOf(original).asComplete());
                replace(finished);
                transaction.commit();
            }
            return Status.COMPLETE;
        }

        private void replace(ItemStack replacement) {
            if (source instanceof InventorySlot slot) {
                context.player().getInventory().setItem(slot.slot(), replacement);
            } else {
                ((DroppedNote) source).entity().setItem(replacement);
            }
        }
    }
}
