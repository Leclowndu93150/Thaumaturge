package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCWandParts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityRechargePedestal extends BlockEntityPedestal {
    private static final int RECHARGE_INTERVAL_TICKS = 10;
    private static final int RECHARGE_AMOUNT = 5;
    private static final int SPARKLE_SPREAD = 3;
    private static final int NODE_DRAW_INTERVAL_TICKS = 5;
    private static final int NODE_DRAW_RANGE = 8;
    private static final int RELAY_DRAW_CV = 100;

    private int counter;
    private @Nullable BlockPos drainPos;
    private int drainColor = 0xFFFFFF;
    private int drainTicks;

    private static final int DRAIN_LINGER_TICKS = 10;

    public @Nullable BlockPos getDrainPos() {
        return drainPos;
    }

    public int getDrainColor() {
        return drainColor;
    }

    public BlockEntityRechargePedestal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RECHARGE_PEDESTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityRechargePedestal pedestal) {
        if (level instanceof ServerLevel serverLevel) {
            pedestal.tickServer(serverLevel);
        }
    }

    private void tickServer(ServerLevel level) {
        counter++;
        if (drainTicks > 0 && --drainTicks == 0) {
            drainPos = null;
            setChanged();
            syncToClient();
        }
        if (getItem().isEmpty()) {
            return;
        }
        if (getItem().getItem() instanceof ItemWand) {
            if (counter % NODE_DRAW_INTERVAL_TICKS == 0 && (drawFromNodes(level) || drawFromRelay(level))) {
                setChanged();
                syncToClient();
                sendSparkle(level);
            }
            return;
        }
        if (counter % RECHARGE_INTERVAL_TICKS != 0) {
            return;
        }
        if (RechargeAccess.rechargeItem(level, getItem(), worldPosition, null, RECHARGE_AMOUNT) > 0.0F) {
            setChanged();
            syncToClient();
            sendSparkle(level);
        }
    }

    private boolean drawFromRelay(ServerLevel level) {
        ItemStack wand = getItem();
        BlockEntityVisRelay relay = VisRelayNetwork.findRelayNear(level, worldPosition);
        if (relay == null) {
            return false;
        }
        BlockEntityNode source = relay.resolveSource(level);
        if (source == null) {
            return false;
        }
        for (ResourceKey<IAspect> key : TCAspects.PRIMALS) {
            int needCv = WandVisHelper.getMaxVis(wand) - WandVisHelper.getVis(wand, key);
            if (needCv <= 0) {
                continue;
            }
            Holder<IAspect> aspect = Aspects.resolve(level.registryAccess(), key);
            if (aspect == null) {
                continue;
            }
            int drained = source.drainCentivis(aspect, Math.min(RELAY_DRAW_CV, needCv));
            if (drained <= 0) {
                continue;
            }
            WandVisHelper.addRealVis(wand, key, drained, true);
            drainPos = source.getBlockPos();
            drainColor = aspect.value().color();
            drainTicks = DRAIN_LINGER_TICKS;
            return true;
        }
        return false;
    }

    private boolean drawFromNodes(ServerLevel level) {
        ItemStack wand = getItem();
        WandParts parts = WandVisHelper.getParts(wand);
        int min = parts.rod() == TCWandParts.ROD_WOOD.get() || parts.cap() == TCWandParts.CAP_IRON.get() ? 0 : 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -NODE_DRAW_RANGE; xx <= NODE_DRAW_RANGE; xx++) {
            for (int yy = -NODE_DRAW_RANGE; yy <= NODE_DRAW_RANGE; yy++) {
                for (int zz = -NODE_DRAW_RANGE; zz <= NODE_DRAW_RANGE; zz++) {
                    cursor.setWithOffset(worldPosition, xx, yy, zz);
                    if (!(level.getBlockEntity(cursor) instanceof BlockEntityNode node) || node instanceof BlockEntityJarNode) {
                        continue;
                    }
                    for (AspectInstance entry : node.getAspects().entries()) {
                        if (!entry.aspect().value().isPrimal() || entry.amount() <= min) {
                            continue;
                        }
                        ResourceKey<IAspect> key = entry.aspect().unwrapKey().orElseThrow();
                        if (WandVisHelper.getVis(wand, key) >= WandVisHelper.getMaxVis(wand)) {
                            continue;
                        }
                        if (node.takeFromContainer(entry.aspect(), 1)) {
                            WandVisHelper.addVis(wand, key, 1, true);
                            node.setChanged();
                            level.sendBlockUpdated(cursor.immutable(), node.getBlockState(), node.getBlockState(), 3);
                            drainPos = cursor.immutable();
                            drainColor = entry.aspect().value().color();
                            drainTicks = DRAIN_LINGER_TICKS;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (drainPos != null) {
            output.store("DrainPos", BlockPos.CODEC, drainPos);
            output.putInt("DrainColor", drainColor);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        drainPos = input.read("DrainPos", BlockPos.CODEC).orElse(null);
        drainColor = input.getIntOr("DrainColor", 0xFFFFFF);
    }

    private void sendSparkle(ServerLevel level) {
        RandomSource rand = level.getRandom();
        List<Holder.Reference<IAspect>> primals = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(holder -> holder.value().isPrimal()).toList();
        int color = primals.isEmpty() ? 0xFFFFFF : primals.get(rand.nextInt(primals.size())).value().color();
        Vec3 from = new Vec3(worldPosition.getX() + rand.nextInt(SPARKLE_SPREAD) - rand.nextInt(SPARKLE_SPREAD) + rand.nextFloat(),
                worldPosition.getY() + 1 + rand.nextInt(SPARKLE_SPREAD) + rand.nextFloat(), worldPosition.getZ() + rand.nextInt(SPARKLE_SPREAD) - rand.nextInt(SPARKLE_SPREAD) + rand.nextFloat());
        Vec3 to = new Vec3(worldPosition.getX() + 0.4 + rand.nextFloat() * 0.2F, worldPosition.getY() + 1 + 0.4 + rand.nextFloat() * 0.2F, worldPosition.getZ() + 0.4 + rand.nextFloat() * 0.2F);
        EffectDispatch.spawnVisSparkle(level, from, to, color);
    }
}
