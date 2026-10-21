package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.ITransactionalWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.IWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.wands.WandPayment;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.WandSlot;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@EventBusSubscriber(modid = TTIds.MODID)
public final class WandStorageTests {
    private static final ExternalStorage STORAGE = new ExternalStorage();
    private static final WandRod TRANSACTIONAL_ROD = rod(STORAGE);
    private static final WandRod LEGACY_ROD = rod(new IWandVisStorage() {
        @Override
        public WandVis getVis(ItemStack wand) {
            return STORAGE.getVis(wand);
        }

        @Override
        public void setVis(ItemStack wand, WandVis vis) {
            STORAGE.setVis(wand, vis);
        }
    });

    private WandStorageTests() {}

    @SubscribeEvent
    public static void registerRods(RegisterEvent event) {
        event.register(WandRod.REGISTRY_KEY, registration -> {
            registration.register(TTIds.rl("test_transactional_rod"), TRANSACTIONAL_ROD);
            registration.register(TTIds.rl("test_legacy_storage_rod"), LEGACY_ROD);
        });
    }

    public static void register(TTTestRegistrar registrar) {
        registrar.add("wand/external_storage_nested_commit_root_abort", 20, helper -> {
            STORAGE.reset();
            ServerPlayer player = player(helper, "tc_wand_external");
            TestSlot slot = new TestSlot(player, wand(TRANSACTIONAL_ROD));
            ItemStack observer = wand(TRANSACTIONAL_ROD);
            WandVis original = WandAccess.getAllVis(observer);
            var charge = WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 100).orElseThrow();
            var payment = WandPayment.quote(player, slot, Map.of(TTAspects.IGNIS, 50, TTAspects.AQUA, 25), WandPayment.CostKind.RAW).orElseThrow();
            if (charge.acceptedCentivis() != 100 || payment.costs().size() != 2 || !WandAccess.getAllVis(observer).equals(original) || STORAGE.revision(observer) != 0 || STORAGE.notifications != 0
                    || WandAccess.capacityCentivis(slot.stack(), TTAspects.IGNIS).orElseThrow() != 1000) {
                helper.fail("External storage previews mutated backing state or ignored its capacity");
                return;
            }
            try (Transaction root = Transaction.openRoot()) {
                try (Transaction nested = Transaction.open(root)) {
                    if (WandAccess.tryCharge(player, slot, charge, nested) != 100) {
                        helper.fail("External charge was refused");
                        return;
                    }
                    payment = WandPayment.quote(player, slot, Map.of(TTAspects.IGNIS, 50, TTAspects.AQUA, 25), WandPayment.CostKind.RAW).orElseThrow();
                    if (!WandPayment.tryPay(player, slot, payment, nested)) {
                        helper.fail("External multi-pool payment was refused");
                        return;
                    }
                    nested.commit();
                }
                if (WandAccess.getVis(observer, TTAspects.IGNIS) != 550 || WandAccess.getVis(observer, TTAspects.AQUA) != 175 || STORAGE.revision(observer) != 2 || STORAGE.notifications != 0
                        || slot.notifications != 0) {
                    helper.fail("Nested commit did not expose shared state silently");
                    return;
                }
            }
            if (!WandAccess.getAllVis(observer).equals(original) || STORAGE.revision(observer) != 0 || STORAGE.notifications != 0 || slot.notifications != 0) {
                helper.fail("Root abort did not restore every external pool and its revision silently");
                return;
            }
            helper.succeed();
        });

        registrar.add("wand/external_storage_commit_and_revision_guard", 20, helper -> {
            STORAGE.reset();
            ServerPlayer player = player(helper, "tc_wand_revision");
            TestSlot slot = new TestSlot(player, wand(TRANSACTIONAL_ROD));
            ItemStack observer = wand(TRANSACTIONAL_ROD);
            var stale = WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 100).orElseThrow();
            STORAGE.setVis(observer, STORAGE.getVis(observer));
            try (Transaction root = Transaction.openRoot()) {
                if (WandAccess.tryCharge(player, slot, stale, root) != 0) {
                    helper.fail("A quote survived an external revision change with identical balances");
                    return;
                }
                var fresh = WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 100).orElseThrow();
                if (WandAccess.tryCharge(player, slot, fresh, root) != 100 || STORAGE.notifications != 0 || slot.notifications != 0) {
                    helper.fail("Fresh external charge failed or notified before root commit");
                    return;
                }
                root.commit();
            }
            if (WandAccess.getVis(observer, TTAspects.IGNIS) != 600 || STORAGE.revision(observer) != 2 || STORAGE.notifications != 1 || slot.notifications != 1) {
                helper.fail("Root commit did not retain and notify the shared external charge exactly once");
                return;
            }
            helper.succeed();
        });

        registrar.add("wand/legacy_storage_rejects_transactional_access", 20, helper -> {
            STORAGE.reset();
            ServerPlayer player = player(helper, "tc_wand_legacy");
            TestSlot slot = new TestSlot(player, wand(LEGACY_ROD));
            WandVis before = WandAccess.getAllVis(slot.stack());
            if (WandAccess.inspect(slot.stack()).isEmpty() || WandAccess.capacityCentivis(slot.stack(), TTAspects.IGNIS).isPresent()
                    || WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 100).isPresent() || WandPayment.quote(player, slot, Map.of(TTAspects.IGNIS, 100), WandPayment.CostKind.RAW).isPresent()
                    || !WandAccess.getAllVis(slot.stack()).equals(before) || STORAGE.revision(slot.stack()) != 0) {
                helper.fail("Legacy storage was rejected for inspection, accepted for transactions or mutated by a preview");
                return;
            }
            helper.succeed();
        });
    }

    private static WandRod rod(IWandVisStorage storage) {
        return new WandRod(25, 1, TTIds.rl("textures/models/wand_rod_wood.png"), null, false, false, false, null, storage);
    }

    private static ItemStack wand(WandRod rod) {
        ItemStack wand = new ItemStack(TTItems.WAND.get());
        wand.set(TTDataComponents.WAND_PARTS.get(), new WandParts(TTWandParts.CAP_GOLD.get(), rod, false));
        return wand;
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }

    private static final class TestSlot implements WandSlot {
        private final ServerPlayer owner;
        private final ItemStack stack;
        private int notifications;

        private TestSlot(ServerPlayer owner, ItemStack stack) {
            this.owner = owner;
            this.stack = stack;
        }

        @Override
        public ItemStack stack() {
            return stack;
        }

        @Override
        public boolean mayAccess(ServerPlayer player) {
            return player == owner;
        }

        @Override
        public void changed() {
            notifications++;
        }
    }

    private record StorageState(WandVis vis, long revision) {
    }

    private static final class ExternalStorage extends SnapshotJournal<StorageState> implements ITransactionalWandVisStorage {
        private StorageState state = new StorageState(WandVis.EMPTY, 0);
        private int notifications;

        private void reset() {
            state = new StorageState(new WandVis(Map.of(TTAspects.IGNIS, 500, TTAspects.AQUA, 200)), 0);
            notifications = 0;
        }

        @Override
        public WandVis getVis(ItemStack wand) {
            return state.vis();
        }

        @Override
        public void setVis(ItemStack wand, WandVis vis) {
            state = new StorageState(vis, state.revision() + 1);
        }

        @Override
        public int capacityCentivis(ItemStack wand, ResourceKey<IAspect> aspect) {
            return 1000;
        }

        @Override
        public long revision(ItemStack wand) {
            return state.revision();
        }

        @Override
        public boolean replaceVis(ItemStack wand, long expectedRevision, WandVis expected, WandVis replacement, TransactionContext transaction) {
            if (state.revision() != expectedRevision || !state.vis().equals(expected)) {
                return false;
            }
            updateSnapshots(transaction);
            setVis(wand, replacement);
            return true;
        }

        @Override
        protected StorageState createSnapshot() {
            return state;
        }

        @Override
        protected void revertToSnapshot(StorageState snapshot) {
            state = snapshot;
        }

        @Override
        protected void onRootCommit(StorageState original) {
            notifications++;
        }
    }
}
