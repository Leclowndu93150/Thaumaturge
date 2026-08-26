package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusSplit;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityFocalManipulator extends BlockEntity implements MenuProvider {
    public static final int SLOT_FOCUS = 0;
    private static final int DRAIN_INTERVAL = 20;
    private static final float DRAIN_PER_CYCLE = 20.0F;
    private static final int CHARGER_CHUNK_RANGE = 1;
    private static final Component TITLE = Component.translatable("block.thaumaturge.focal_manipulator");

    public float vis;
    public String focusName = "";
    public AspectList crystalsSync = AspectList.EMPTY;
    public final Map<Integer, FocusElementNode> data = new LinkedHashMap<>();
    public int clientDataStamp;

    private AspectList crystals = AspectList.EMPTY;
    private int xpCost;
    private int ticks;
    private final TableInventory inventory = new TableInventory();

    public BlockEntityFocalManipulator(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    public ItemStackHandler items() {
        return inventory;
    }

    public ItemStack focusStack() {
        ItemStack resource = inventory.getStackInSlot(SLOT_FOCUS);
        int amount = inventory.getStackInSlot(SLOT_FOCUS).getCount();
        return resource.isEmpty() || amount <= 0 ? ItemStack.EMPTY : resource.copyWithCount(amount);
    }

    private void setFocusStack(ItemStack stack) {
        inventory.setStackInSlot(SLOT_FOCUS, stack.copyWithCount(stack.getCount()));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        if (level instanceof ServerLevel serverLevel) {
            table.tickServer(serverLevel);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        table.tickClient();
    }

    private void tickServer(ServerLevel level) {
        ticks++;
        if (ticks % DRAIN_INTERVAL != 0 || vis <= 0.0F) {
            return;
        }
        ItemStack focus = focusStack();
        if (!(focus.getItem() instanceof ItemFocus)) {
            vis = 0.0F;
            level.playSound(null, worldPosition, TCSounds.WANDFAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
            syncToClient();
            return;
        }
        float amount = spendAura(level, Math.min(DRAIN_PER_CYCLE, vis));
        if (amount > 0.0F) {
            RandomSource rand = level.getRandom();
            Vec3 origin = new Vec3(
                    worldPosition.getX() + rand.nextInt(3) - rand.nextInt(3),
                    worldPosition.getY() + rand.nextInt(3),
                    worldPosition.getZ() + rand.nextInt(3) - rand.nextInt(3));
            EffectDispatch.spawnVisSparkle(
                    level, origin, new Vec3(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ()));
            vis -= amount;
            setChanged();
            syncToClient();
        }
        if (vis <= 0.0F) {
            endCraft(level);
        }
    }

    private void tickClient() {
        if (vis <= 0.0F || level == null) {
            return;
        }
        RandomSource rand = level.getRandom();
        ShieldSparkParticleOptions fx = new ShieldSparkParticleOptions(
                ARGB32.colorFromFloat(
                        1.0F,
                        0.5F + rand.nextFloat() * 0.4F,
                        1.0F - rand.nextFloat() * 0.4F,
                        1.0F - rand.nextFloat() * 0.4F),
                0.8F,
                0.3F + rand.nextFloat() * 0.3F,
                6 + rand.nextInt(5),
                0,
                true);
        level.addParticle(
                fx,
                worldPosition.getX() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 0.3F,
                worldPosition.getY() + 1.4 + (rand.nextFloat() - rand.nextFloat()) * 0.3F,
                worldPosition.getZ() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 0.3F,
                0.0,
                0.0,
                0.0);
    }

    private float spendAura(ServerLevel level, float amount) {
        if (level.getBlockState(worldPosition.above()).getBlock() != TCBlocks.ARCANE_WORKBENCH_CHARGER.get()) {
            return AuraHelper.drainVis(level, worldPosition, amount, false);
        }
        float remaining = amount;
        float slice = amount / 9.0F;
        for (int xx = -CHARGER_CHUNK_RANGE; xx <= CHARGER_CHUNK_RANGE; xx++) {
            for (int zz = -CHARGER_CHUNK_RANGE; zz <= CHARGER_CHUNK_RANGE; zz++) {
                float take = Math.min(slice, remaining);
                remaining -= AuraHelper.drainVis(level, worldPosition.offset(xx * 16, 0, zz * 16), take, false);
                if (remaining <= 0.0F) {
                    return amount - remaining;
                }
            }
        }
        return amount - remaining;
    }

    public boolean startCraft(Player player) {
        if (data.isEmpty() || vis > 0.0F || !(focusStack().getItem() instanceof ItemFocus focusItem)) {
            return false;
        }
        int maxComplexity = focusItem.getMaxComplexity();
        crystals = AspectList.EMPTY;
        int totalComplexity = 0;
        HashMap<String, Integer> compCount = new HashMap<>();
        for (FocusElementNode node : data.values()) {
            FocusElement element = node.resolve();
            if (element == null) {
                return false;
            }
            if (!ResearchManager.doesPassGate(player, element.research())) {
                return false;
            }
            int a = compCount.getOrDefault(node.element.toString(), 0);
            node.complexityMultiplier = 0.5F * (++a + 1);
            compCount.put(node.element.toString(), a);
            totalComplexity =
                    (int) (totalComplexity + element.complexity(node.resolvedSettings()) * node.complexityMultiplier);
            if (element.aspect() != null) {
                crystals = crystals.add(resolveAspect(element.aspect()), 1);
            }
        }
        vis = totalComplexity * 10 + maxComplexity / 5.0F;
        xpCost = (int) Math.max(1L, Math.round(Math.sqrt(totalComplexity)));
        if (!player.getAbilities().instabuild && player.experienceLevel < xpCost) {
            vis = 0.0F;
            return false;
        }
        if (crystals.entries().isEmpty()) {
            vis = 0.0F;
            return false;
        }
        for (AspectInstance instance : crystals.entries()) {
            ItemStack crystal = EssentiaCrystalFactory.of(instance.aspect(), instance.amount());
            if (!isPlayerCarrying(player, crystal)) {
                vis = 0.0F;
                return false;
            }
        }
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-xpCost);
        }
        for (AspectInstance instance : crystals.entries()) {
            consumePlayerItem(player, EssentiaCrystalFactory.of(instance.aspect(), instance.amount()));
        }
        crystalsSync = crystals;
        setChanged();
        syncToClient();
        if (level != null) {
            level.playSound(null, worldPosition, TCSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    private Holder<IAspect> resolveAspect(ResourceKey<IAspect> key) {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    private static boolean isPlayerCarrying(Player player, ItemStack required) {
        int found = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, required)) {
                found += stack.getCount();
                if (found >= required.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void consumePlayerItem(Player player, ItemStack required) {
        int remaining = required.getCount();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, required)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                inv.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
        }
    }

    private void endCraft(ServerLevel level) {
        vis = 0.0F;
        ItemStack focus = focusStack();
        if (focus.getItem() instanceof ItemFocus) {
            FocusPackage core = generateFocus();
            if (core != null) {
                level.playSound(null, worldPosition, TCSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!focusName.isEmpty()) {
                    focus.set(DataComponents.CUSTOM_NAME, Component.literal(focusName));
                }
                ItemFocus.setPackage(focus, core);
                setFocusStack(focus);
                crystalsSync = AspectList.EMPTY;
                data.clear();
            }
        }
        setChanged();
        syncToClient();
    }

    private @Nullable FocusPackage generateFocus() {
        if (data.isEmpty()) {
            return null;
        }
        int totalComplexity = 0;
        HashMap<String, Integer> compCount = new HashMap<>();
        for (FocusElementNode node : data.values()) {
            FocusElement element = node.resolve();
            if (element != null) {
                int a = compCount.getOrDefault(node.element.toString(), 0);
                node.complexityMultiplier = 0.5F * (++a + 1);
                compCount.put(node.element.toString(), a);
                totalComplexity = (int)
                        (totalComplexity + element.complexity(node.resolvedSettings()) * node.complexityMultiplier);
            }
        }
        FocusPackage.Builder core = FocusPackage.builder().complexity(totalComplexity);
        appendChain(core, data.get(0));
        return core.build();
    }

    private void appendChain(FocusPackage.Builder builder, @Nullable FocusElementNode node) {
        while (node != null && node.element != null) {
            if (node.children.length > 1 && FocusEngine.element(node.element) instanceof FocusSplit) {
                List<FocusPackage> branches = new ArrayList<>(node.children.length);
                for (int c : node.children) {
                    FocusPackage.Builder branch = FocusPackage.builder();
                    appendChain(branch, data.get(c));
                    branches.add(branch.build());
                }
                builder.add(new FocusUnit(node.element, node.settings, branches));
                return;
            }
            builder.add(FocusUnit.of(node.element, node.settings));
            node = node.children.length == 1 ? data.get(node.children[0]) : null;
        }
    }

    public void acceptClientData(Map<Integer, FocusElementNode> nodes, String name) {
        data.clear();
        data.putAll(nodes);
        focusName = name;
        setChanged();
        syncToClient();
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuFocalManipulator(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("inventory", inventory.serializeNBT(registries));
        output.putFloat("Vis", vis);
        output.putString("FocusName", focusName);
        TCNbt.store(output, "Crystals", AspectList.CODEC, registries, crystalsSync);
        TCNbt.store(output, "Nodes", FocusElementNode.CODEC.listOf(), registries, List.copyOf(data.values()));
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        inventory.deserializeNBT(registries, input.getCompound("inventory"));
        vis = input.getFloat("Vis");
        focusName = input.getString("FocusName");
        crystalsSync =
                TCNbt.read(input, "Crystals", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        data.clear();
        TCNbt.read(input, "Nodes", FocusElementNode.CODEC.listOf(), registries).ifPresent(nodes -> {
            for (FocusElementNode node : nodes) {
                data.put(node.id, node);
            }
        });
        clientDataStamp++;
    }

    private void syncToClient() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        {
            CompoundTag output = new CompoundTag();
            saveAdditional(output, registries);
            nbt.merge(output);
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private final class TableInventory extends ItemStackHandler {
        TableInventory() {
            super(NonNullList.withSize(1, ItemStack.EMPTY));
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                vis = 0.0F;
                crystalsSync = AspectList.EMPTY;
                syncToClient();
            }
        }

        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return resource.getItem() instanceof ItemFocus;
        }
    }
}
