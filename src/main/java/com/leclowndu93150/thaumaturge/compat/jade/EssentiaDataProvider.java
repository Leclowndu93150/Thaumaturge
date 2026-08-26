package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeOneway;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeRestrict;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum EssentiaDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = TCIds.rl("essentia");

    static final String PRESENT = "ThaumaturgeEssentia";
    static final String AREA = "EssentiaArea";
    static final String CONTENTS = "EssentiaContents";
    static final String CAPACITY = "EssentiaCapacity";
    static final String HAS_FILTER = "HasEssentiaFilter";
    static final String FILTER = "EssentiaFilter";
    static final String TUBE_KIND = "TubeKind";
    static final String FACING = "TubeFacing";
    static final String CLOSED_SIDES = "TubeClosedSides";
    static final String CHOKED_SIDES = "TubeChokedSides";
    static final String VALVE_OPEN = "TubeValveOpen";
    static final String SUCTION = "TubeSuction";
    static final String SUCTION_ASPECT = "TubeSuctionAspect";
    static final String CRUCIBLE_HEAT = "CrucibleHeat";
    static final String RECIPE_COUNT = "RecipeCount";
    static final String RECIPE_CAPACITY = "RecipeCapacity";
    static final String ACTIVE = "EssentiaActive";

    static final String ASPECT_ID = "Id";
    static final String ASPECT_AMOUNT = "Amount";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof BlockEntityNode) return;

        if (blockEntity instanceof BlockEntityJar jar) {
            writeStorage(tag, jar.getContents(accessor.getLevel().registryAccess()), jar.capacity());
            tag.putString(AREA, "jar");
            return;
        }
        if (blockEntity instanceof BlockEntityTubeBuffer buffer) {
            writeStorage(tag, buffer.contents(), BlockEntityTubeBuffer.MAX_AMOUNT);
            writeBufferState(tag, buffer);
            return;
        }
        if (blockEntity instanceof BlockEntityTube tube) {
            writeTube(tag, tube);
            return;
        }
        if (blockEntity instanceof BlockEntityThaumatorium thaumatorium) {
            writeStorage(tag, thaumatorium.essentia(), 0);
            tag.putString(AREA, "thaumatorium");
            tag.putInt(RECIPE_COUNT, thaumatorium.queue().size());
            tag.putInt(RECIPE_CAPACITY, thaumatorium.maxRecipes());
            return;
        }
        if (blockEntity instanceof BlockEntityCentrifuge centrifuge) {
            tag.putBoolean(PRESENT, true);
            writeSingleAspect(
                    tag, centrifuge.getEssentiaType(Direction.UP), centrifuge.getEssentiaAmount(Direction.UP));
            tag.putInt(CAPACITY, 1);
            tag.putString(AREA, "centrifuge");
            tag.putBoolean(ACTIVE, centrifuge.isSpinning());
            return;
        }
        if (blockEntity instanceof IAspectContainer container) {
            int capacity = blockEntity instanceof BlockEntityAlembic ? BlockEntityAlembic.CAPACITY : 0;
            writeStorage(tag, container.getAspects(), capacity);
            if (blockEntity instanceof BlockEntityAlembic alembic) {
                tag.putString(AREA, "alembic");
                tag.putBoolean(HAS_FILTER, true);
                putAspect(tag, FILTER, alembic.aspectFilterKey());
            }
            if (blockEntity instanceof BlockEntityCrucible crucible) {
                tag.putString(AREA, "crucible");
                tag.putInt(CRUCIBLE_HEAT, crucible.getHeat());
            }
            return;
        }
        if (blockEntity instanceof IAspectQuery query) {
            tag.putBoolean(PRESENT, true);
            tag.putString(AREA, "filter");
            writeFilter(tag, query.queryAspects());
        }
    }

    private static void writeTube(CompoundTag tag, BlockEntityTube tube) {
        tag.putBoolean(PRESENT, true);
        writeSingleAspect(tag, tube.essentiaKey(), tube.essentiaAmountRaw());
        tag.putInt(CAPACITY, 1);
        String kind = tubeKind(tube);
        tag.putString(TUBE_KIND, kind);
        tag.putString(AREA, "normal".equals(kind) ? "tube" : kind);
        Direction displayDirection =
                tube instanceof BlockEntityTubeOneway ? tube.facing().getOpposite() : tube.facing();
        tag.putString(FACING, displayDirection.getSerializedName());
        tag.putInt(CLOSED_SIDES, closedMask(tube.openSides()));
        tag.putInt(SUCTION, tube.suctionRaw());
        putAspect(tag, SUCTION_ASPECT, tube.suctionKey());
        if (tube instanceof BlockEntityTubeValve valve) {
            tag.putBoolean(VALVE_OPEN, valve.allowFlow());
        }
        if (tube instanceof BlockEntityTubeFilter filter) {
            writeFilter(tag, filter.queryAspects());
        }
    }

    private static void writeBufferState(CompoundTag tag, BlockEntityTubeBuffer buffer) {
        tag.putString(TUBE_KIND, "buffer");
        tag.putString(AREA, "buffer");
        tag.putString(FACING, buffer.facing().getSerializedName());
        tag.putInt(CLOSED_SIDES, closedMask(buffer.openSides()));
        byte[] choked = new byte[Direction.values().length];
        for (Direction direction : Direction.values()) {
            choked[direction.ordinal()] = (byte) buffer.chokedSide(direction);
        }
        tag.putByteArray(CHOKED_SIDES, choked);
    }

    private static String tubeKind(BlockEntityTube tube) {
        if (tube instanceof BlockEntityTubeValve) return "valve";
        if (tube instanceof BlockEntityTubeRestrict) return "restrict";
        if (tube instanceof BlockEntityTubeFilter) return "filter";
        if (tube instanceof BlockEntityTubeOneway) return "oneway";
        return "normal";
    }

    private static int closedMask(boolean[] openSides) {
        int mask = 0;
        for (int i = 0; i < Math.min(openSides.length, Direction.values().length); i++) {
            if (!openSides[i]) mask |= 1 << i;
        }
        return mask;
    }

    private static void writeStorage(CompoundTag tag, AspectList contents, int capacity) {
        tag.putBoolean(PRESENT, true);
        writeAspects(tag, contents);
        if (capacity > 0) tag.putInt(CAPACITY, capacity);
    }

    private static void writeSingleAspect(CompoundTag tag, ResourceKey<IAspect> aspect, int amount) {
        if (aspect == null || amount <= 0) return;
        CompoundTag entry = new CompoundTag();
        entry.putString(ASPECT_ID, aspect.location().toString());
        entry.putInt(ASPECT_AMOUNT, amount);
        ListTag contents = new ListTag();
        contents.add(entry);
        tag.put(CONTENTS, contents);
    }

    private static void writeSingleAspect(CompoundTag tag, net.minecraft.core.Holder<IAspect> aspect, int amount) {
        if (aspect == null) return;
        writeSingleAspect(tag, aspect.unwrapKey().orElse(null), amount);
    }

    private static void writeAspects(CompoundTag tag, AspectList aspects) {
        ListTag contents = new ListTag();
        for (AspectInstance entry : aspects.entries()) {
            entry.aspect().unwrapKey().ifPresent(key -> {
                CompoundTag encoded = new CompoundTag();
                encoded.putString(ASPECT_ID, key.location().toString());
                encoded.putInt(ASPECT_AMOUNT, entry.amount());
                contents.add(encoded);
            });
        }
        tag.put(CONTENTS, contents);
    }

    private static void writeFilter(CompoundTag tag, AspectList filter) {
        tag.putBoolean(HAS_FILTER, true);
        if (filter.isEmpty()) return;
        filter.entries()
                .getFirst()
                .aspect()
                .unwrapKey()
                .ifPresent(key -> tag.putString(FILTER, key.location().toString()));
    }

    private static void putAspect(CompoundTag tag, String key, ResourceKey<IAspect> aspect) {
        if (aspect != null) tag.putString(key, aspect.location().toString());
    }
}
