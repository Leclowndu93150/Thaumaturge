package com.leclowndu93150.thaumaturge.api.research;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Server-side completion of existing, transferable research notes without learning their research. */
public final class ResearchNotesAccess {
    private static Provider binding;

    private ResearchNotesAccess() {}

    public enum Status {
        READY, COMPLETE, NOT_A_NOTE, INVALID, UNAUTHORIZED, STALE, PAYMENT_FAILED
    }

    public record Anchor(int q, int r, Identifier aspect) {
    }

    public record Note(String fingerprint, Identifier entry, int theoryRow, List<Anchor> anchors, int workCells, boolean complete) {
        public Note {
            anchors = List.copyOf(anchors);
        }
    }

    public record Inspection(Status status, Optional<Note> note) {
    }

    public sealed interface Source permits InventorySlot, DroppedNote {}

    /** A slot in the acting player's inventory. */
    public record InventorySlot(int slot) implements Source {
    }

    /** A loaded dropped note. Pickup restrictions and caller authorization both apply. */
    public record DroppedNote(ItemEntity entity) implements Source {
        public DroppedNote {
            Objects.requireNonNull(entity);
        }
    }

    /** Authorization must be side-effect free and is evaluated again immediately before completion. */
    public record Context(ServerPlayer player, Predicate<Source> authorization) {
        public Context {
            Objects.requireNonNull(player);
            Objects.requireNonNull(authorization);
        }
    }

    /** All payment mutations must participate in the supplied transaction. */
    @FunctionalInterface
    public interface FinalCost {
        boolean pay(TransactionContext transaction);
    }

    /** A single-use plan bound to one source, stack instance, position, and fingerprint. */
    public interface Completion {
        Status complete(FinalCost cost);
    }

    public record Preparation(Status status, Optional<Completion> completion) {
    }

    public interface Provider {
        Inspection inspect(ItemStack stack, HolderLookup.Provider registries);

        Preparation prepare(Context context, Source source, String expectedFingerprint);
    }

    public static Inspection inspect(ItemStack stack, HolderLookup.Provider registries) {
        return provider().inspect(stack, registries);
    }

    public static Preparation prepareCompletion(Context context, Source source, String expectedFingerprint) {
        return provider().prepare(context, source, expectedFingerprint);
    }

    public static Status completeExisting(Context context, Source source, String expectedFingerprint, FinalCost cost) {
        Preparation prepared = prepareCompletion(context, source, expectedFingerprint);
        return prepared.completion().map(completion -> completion.complete(cost)).orElse(prepared.status());
    }

    /** Implementation binding, installed once during mod initialization. */
    public static void bind(Provider provider) {
        if (binding != null) {
            throw new IllegalStateException("ResearchNotesAccess already bound");
        }
        binding = Objects.requireNonNull(provider);
    }

    private static Provider provider() {
        if (binding == null) {
            throw new IllegalStateException("ResearchNotesAccess accessed before binding");
        }
        return binding;
    }
}
