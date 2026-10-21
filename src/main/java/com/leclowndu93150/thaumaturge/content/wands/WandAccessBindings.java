package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.ITransactionalWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.IWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandChargeQuote;
import com.leclowndu93150.thaumaturge.api.wands.WandPayment;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.WandSlot;
import com.leclowndu93150.thaumaturge.api.wands.WandSnapshot;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class WandAccessBindings implements WandAccess.Bindings {

    @Override
    public Optional<WandSnapshot> inspect(ItemStack wand) {
        if (wand.isEmpty() || !(wand.getItem() instanceof ItemWand)) {
            return Optional.empty();
        }
        WandParts parts = wand.get(TTDataComponents.WAND_PARTS.get());
        if (parts == null || parts.rod() == null || parts.cap() == null || parts.sceptre() && parts.rod().staff()) {
            return Optional.empty();
        }
        Identifier rod = TTWandParts.rods().getKey(parts.rod());
        Identifier cap = TTWandParts.caps().getKey(parts.cap());
        if (rod == null || cap == null) {
            return Optional.empty();
        }
        WandSnapshot.Variant variant = parts.sceptre() ? WandSnapshot.Variant.SCEPTRE : parts.rod().staff() ? WandSnapshot.Variant.STAFF : WandSnapshot.Variant.WAND;
        return Optional.of(new WandSnapshot(ResourceKey.create(WandRod.REGISTRY_KEY, rod), ResourceKey.create(WandCap.REGISTRY_KEY, cap), variant));
    }

    @Override
    public OptionalInt capacityCentivis(ItemStack wand, ResourceKey<IAspect> aspect) {
        if (!TTAspects.PRIMALS.contains(aspect) || inspect(wand).isEmpty()) {
            return OptionalInt.empty();
        }
        WandParts parts = wand.get(TTDataComponents.WAND_PARTS.get());
        IWandVisStorage storage = parts.rod().visStorage();
        if (storage instanceof ITransactionalWandVisStorage transactional) {
            int capacity = transactional.capacityCentivis(wand, aspect);
            return capacity < 0 ? OptionalInt.empty() : OptionalInt.of(capacity);
        }
        if (storage != null) {
            return OptionalInt.empty();
        }
        long capacity = (long) parts.rod().capacity() * (parts.sceptre() ? WandEconomy.SCEPTRE_CAPACITY_PER_VIS : WandEconomy.CENTIVIS_PER_VIS);
        return capacity < 0 || capacity > Integer.MAX_VALUE ? OptionalInt.empty() : OptionalInt.of((int) capacity);
    }

    @Override
    public Optional<WandChargeQuote> quoteCharge(ServerPlayer player, WandSlot slot, ResourceKey<IAspect> aspect, int maximumCentivis) {
        if (maximumCentivis < 0 || !accessible(player, slot)) {
            return Optional.empty();
        }
        ItemStack wand = slot.stack();
        OptionalInt capacity = capacityCentivis(wand, aspect);
        if (capacity.isEmpty()) {
            return Optional.empty();
        }
        WandVis before = getAllVis(wand);
        if (!validVis(before)) {
            return Optional.empty();
        }
        int accepted = (int) Math.min(maximumCentivis, Math.max(0L, (long) capacity.getAsInt() - before.amount(aspect)));
        return Optional.of(new Charge(new State(player, slot, wand, before), aspect, maximumCentivis, accepted));
    }

    @Override
    public int tryCharge(ServerPlayer player, WandSlot slot, WandChargeQuote quote, TransactionContext transaction) {
        if (!(quote instanceof Charge charge) || charge.acceptedCentivis() <= 0 || !charge.state().matches(player, slot)) {
            return 0;
        }
        Optional<WandChargeQuote> current = quoteCharge(player, slot, charge.aspect(), charge.maximum());
        if (current.isEmpty() || current.get().acceptedCentivis() != charge.acceptedCentivis() || !charge.state().matches(player, slot)) {
            return 0;
        }
        WandVis before = charge.state().before;
        WandVis after = before.with(charge.aspect(), before.amount(charge.aspect()) + charge.acceptedCentivis());
        return charge.state().replace(after, transaction) ? charge.acceptedCentivis() : 0;
    }

    @Override
    public Optional<WandPayment.Quote> quotePayment(ServerPlayer player, WandSlot slot, Map<ResourceKey<IAspect>, Integer> costs, WandPayment.CostKind kind) {
        if (kind == null || costs == null || costs.isEmpty() || !accessible(player, slot) || inspect(slot.stack()).isEmpty()) {
            return Optional.empty();
        }
        ItemStack wand = slot.stack();
        WandVis before = getAllVis(wand);
        if (!validVis(before)) {
            return Optional.empty();
        }
        Map<ResourceKey<IAspect>, Integer> discounted = new LinkedHashMap<>();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : costs.entrySet()) {
            if (entry.getValue() == null || entry.getValue() < 0 || capacityCentivis(wand, entry.getKey()).isEmpty()) {
                return Optional.empty();
            }
            int amount = kind == WandPayment.CostKind.RAW ? entry.getValue() : WandVisHelper.scaledCost(wand, player, entry.getKey(), entry.getValue(), kind == WandPayment.CostKind.CRAFTING);
            if (amount < 0 || before.amount(entry.getKey()) < amount) {
                return Optional.empty();
            }
            discounted.put(entry.getKey(), amount);
        }
        return Optional.of(new Payment(new State(player, slot, wand, before), Map.copyOf(costs), kind, Map.copyOf(discounted)));
    }

    @Override
    public boolean tryPay(ServerPlayer player, WandSlot slot, WandPayment.Quote quote, TransactionContext transaction) {
        if (!(quote instanceof Payment payment) || !payment.state().matches(player, slot)) {
            return false;
        }
        Optional<WandPayment.Quote> current = quotePayment(player, slot, payment.original(), payment.kind());
        if (current.isEmpty() || !current.get().costs().equals(payment.costs()) || !payment.state().matches(player, slot)) {
            return false;
        }
        WandVis after = payment.state().before;
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : payment.costs().entrySet()) {
            after = after.with(entry.getKey(), after.amount(entry.getKey()) - entry.getValue());
        }
        return payment.state().replace(after, transaction);
    }

    private static boolean accessible(ServerPlayer player, WandSlot slot) {
        return player.level().getServer().isSameThread() && player.isAlive() && !player.isSpectator() && slot.mayAccess(player);
    }

    private static boolean validVis(WandVis vis) {
        return vis.centivis().entrySet().stream().allMatch(entry -> TTAspects.PRIMALS.contains(entry.getKey()) && entry.getValue() >= 0);
    }

    private record Charge(State state, ResourceKey<IAspect> aspect, int maximum, int acceptedCentivis) implements WandChargeQuote {
    }

    private record Payment(State state, Map<ResourceKey<IAspect>, Integer> original, WandPayment.CostKind kind, Map<ResourceKey<IAspect>, Integer> costs) implements WandPayment.Quote {
    }

    private static final class State extends SnapshotJournal<Optional<WandVis>> {
        private final ServerPlayer player;
        private final WandSlot slot;
        private final ItemStack wand;
        private final ItemStack fingerprint;
        private final WandVis before;
        private final long tick;
        private final IWandVisStorage storage;
        private final long revision;

        private State(ServerPlayer player, WandSlot slot, ItemStack wand, WandVis before) {
            this.player = player;
            this.slot = slot;
            this.wand = wand;
            this.fingerprint = wand.copy();
            this.before = before;
            this.tick = player.level().getGameTime();
            this.storage = WandVisHelper.partsOf(wand).rod().visStorage();
            this.revision = storage instanceof ITransactionalWandVisStorage transactional ? transactional.revision(wand) : 0;
        }

        private boolean matches(ServerPlayer actor, WandSlot target) {
            return actor == player && target == slot && accessible(actor, target) && player.level().getGameTime() == tick && slot.stack() == wand && ItemStack.matches(wand, fingerprint)
                    && WandVisHelper.chargeOf(wand).equals(before) && (!(storage instanceof ITransactionalWandVisStorage transactional) || transactional.revision(wand) == revision);
        }

        private boolean replace(WandVis after, TransactionContext transaction) {
            try (Transaction nested = Transaction.open(transaction)) {
                updateSnapshots(nested);
                if (storage instanceof ITransactionalWandVisStorage transactional) {
                    if (!transactional.replaceVis(wand, revision, before, after, nested)) {
                        return false;
                    }
                } else if (storage == null) {
                    WandVisHelper.writeCharge(wand, after);
                } else {
                    return false;
                }
                nested.commit();
                return true;
            }
        }

        @Override
        protected Optional<WandVis> createSnapshot() {
            return Optional.ofNullable(wand.get(TTDataComponents.WAND_VIS.get()));
        }

        @Override
        protected void revertToSnapshot(Optional<WandVis> snapshot) {
            if (storage == null) {
                if (snapshot.isPresent()) {
                    WandVisHelper.writeCharge(wand, snapshot.get());
                } else {
                    wand.remove(TTDataComponents.WAND_VIS.get());
                }
            }
        }

        @Override
        protected void onRootCommit(Optional<WandVis> original) {
            slot.changed();
        }
    }

    @Override
    public WandVis getAllVis(ItemStack wand) {
        return WandVisHelper.chargeOf(wand);
    }

    @Override
    public void setAllVis(ItemStack wand, WandVis vis) {
        WandVisHelper.writeCharge(wand, vis);
    }
}
