package com.leclowndu93150.thaumaturge.content.device.mirror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTCapabilities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MirrorInboxCutoffTest {
    private static final BlockPos SOURCE_POS = BlockPos.ZERO;
    private static final BlockPos TARGET_POS = BlockPos.ZERO.east();
    private static final HolderLookup.Provider REGISTRIES =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    private final ServerLevel level = mock(ServerLevel.class);
    private final MinecraftServer server = mock(MinecraftServer.class);
    private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        var randomField = Level.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(level, RandomSource.create(0));
        when(level.isClientSide()).thenReturn(false);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        when(level.getServer()).thenReturn(server);
        when(server.getLevel(Level.OVERWORLD)).thenReturn(level);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockEntity(any())).thenAnswer(call -> blockEntities.get(call.getArgument(0)));
    }

    @Test
    void registersMirrorInboxAndRejectsUnpairedInsertion() {
        RegisterCapabilitiesEvent event = mock(RegisterCapabilitiesEvent.class);
        TTCapabilities.onRegisterCapabilities(event);
        BlockEntityMirror mirror = mirror(SOURCE_POS);
        ICapabilityProvider<BlockEntity, Direction, IItemHandler> provider = itemProvider(event);

        IItemHandler inbox = provider.getCapability(mirror, Direction.NORTH);
        assertSame(mirror.inbox(), inbox);
        for (Direction side : Direction.values()) {
            assertSame(mirror.inbox(), provider.getCapability(mirror, side));
        }
        ItemStack offered = new ItemStack(Items.STONE, 8);
        assertSame(offered, inbox.insertItem(0, offered, false));
        assertTrue(inbox.getStackInSlot(0).isEmpty());
    }

    @Test
    void pairedMirrorAcceptsInputAndForwardsItIntoPartnerQueue() {
        BlockEntityMirror source = mirror(SOURCE_POS);
        BlockEntityMirror target = mirror(TARGET_POS);
        pair(source, target);
        ItemStack offered = new ItemStack(Items.STONE, 12);

        assertTrue(source.inbox().insertItem(0, offered, true).isEmpty());
        assertTrue(source.inbox().getStackInSlot(0).isEmpty());
        assertTrue(source.inbox().insertItem(0, offered, false).isEmpty());
        assertEquals(12, source.inbox().getStackInSlot(0).getCount());

        source.count = 1;
        source.serverTick(level, SOURCE_POS);

        assertTrue(source.inbox().getStackInSlot(0).isEmpty());
        List<ItemStack> queued = savedStacks(target, "Items");
        assertEquals(1, queued.size());
        assertEquals(Items.STONE, queued.getFirst().getItem());
        assertEquals(12, queued.getFirst().getCount());
        assertEquals(1, source.instability);
    }

    @Test
    void inboxPersistsAndCannotBeExtractedThroughTheCapability() {
        BlockEntityMirror source = mirror(SOURCE_POS);
        BlockEntityMirror target = mirror(TARGET_POS);
        pair(source, target);
        source.inbox().insertItem(0, new ItemStack(Items.STONE, 6), false);
        IItemHandler inbox = source.inbox();

        assertTrue(inbox.extractItem(0, 64, false).isEmpty());
        assertEquals(6, inbox.getStackInSlot(0).getCount());

        CompoundTag saved = new CompoundTag();
        source.saveAdditional(saved, REGISTRIES);
        BlockEntityMirror loaded = mirror(BlockPos.ZERO.south());
        loaded.loadAdditional(saved, REGISTRIES);

        assertEquals(Items.STONE, loaded.inbox().getStackInSlot(0).getItem());
        assertEquals(6, loaded.inbox().getStackInSlot(0).getCount());
    }

    @Test
    void brokenPairingRejectsInputAndAnAlreadyBufferedStackUsesPortalFallback() {
        BlockEntityMirror source = mirror(SOURCE_POS);
        BlockEntityMirror target = mirror(TARGET_POS);
        pair(source, target);
        ItemStack offered = new ItemStack(Items.STONE, 4);
        source.inbox().insertItem(0, offered, false);
        target.linked = false;
        assertFalse(source.isLinkValidSimple());
        assertSame(offered, source.inbox().insertItem(0, offered, true));

        when(level.addFreshEntity(any(ItemEntity.class))).thenReturn(true);
        source.serverTick(level, SOURCE_POS);

        Mockito.verify(level).addFreshEntity(any(ItemEntity.class));
        assertTrue(source.inbox().getStackInSlot(0).isEmpty());
    }

    private BlockEntityMirror mirror(BlockPos pos) {
        BlockState state = TTBlocks.MIRROR.get().defaultBlockState();
        BlockEntityMirror mirror = new BlockEntityMirror(pos, state);
        mirror.setLevel(level);
        blockEntities.put(pos, mirror);
        return mirror;
    }

    private void pair(BlockEntityMirror source, BlockEntityMirror target) {
        source.linked = true;
        target.linked = true;
        source.link = GlobalPos.of(Level.OVERWORLD, target.getBlockPos());
        target.link = GlobalPos.of(Level.OVERWORLD, source.getBlockPos());
    }

    private List<ItemStack> savedStacks(BlockEntityMirror mirror, String key) {
        CompoundTag saved = new CompoundTag();
        mirror.saveAdditional(saved, REGISTRIES);
        return TTNbt.read(saved, key, ItemStack.CODEC.listOf(), REGISTRIES).orElse(List.of());
    }

    private ICapabilityProvider<BlockEntity, Direction, IItemHandler> itemProvider(RegisterCapabilitiesEvent event) {
        return Mockito.mockingDetails(event).getInvocations().stream()
                .filter(call -> call.getMethod().getName().equals("registerBlockEntity"))
                .filter(call -> call.getArgument(0) == Capabilities.ItemHandler.BLOCK)
                .filter(call -> call.getArgument(1) == TTBlockEntities.MIRROR.get())
                .map(call -> (ICapabilityProvider<BlockEntity, Direction, IItemHandler>) call.getArgument(2))
                .findFirst()
                .orElseThrow();
    }
}
