package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public enum EssentiaComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = TCIds.rl("essentia");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public int getDefaultPriority() {
        return 1100;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBoolean(EssentiaDataProvider.PRESENT)) return;
        String area = data.getString(EssentiaDataProvider.AREA);
        ResourceLocation option = optionFor(area);
        if (option == null) return;
        boolean crucible = "crucible".equals(area);
        boolean show = JadeConfig.shouldShow(config, option, accessor);
        if (!show) {
            if (crucible) tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
            return;
        }

        String tubeKind = data.getString(EssentiaDataProvider.TUBE_KIND);
        if (crucible) {
            appendCrucible(tooltip, accessor, data);
        } else {
            if (tubeKind.isEmpty() || accessor.showDetails()) appendEssentia(tooltip, accessor, data);
            if (!tubeKind.isEmpty()) appendTubeState(tooltip, accessor, data, tubeKind);
        }

        if (data.getBoolean(EssentiaDataProvider.HAS_FILTER)) {
            String filter = data.getString(EssentiaDataProvider.FILTER);
            tooltip.add(
                    filter.isEmpty()
                            ? Component.translatable("jade.thaumaturge.essentia.unfiltered")
                            : Component.translatable(
                                    "jade.thaumaturge.essentia.filter",
                                    JadeComponents.aspectName(
                                            filter, accessor.getLevel().registryAccess())));
        }

        if (data.contains(EssentiaDataProvider.RECIPE_CAPACITY)) {
            tooltip.add(Component.translatable(
                    "jade.thaumaturge.thaumatorium.recipes",
                    data.getInt(EssentiaDataProvider.RECIPE_COUNT),
                    data.getInt(EssentiaDataProvider.RECIPE_CAPACITY)));
        }

        if ("centrifuge".equals(area)) {
            tooltip.add(Component.translatable(
                    "jade.thaumaturge.centrifuge.state",
                    Component.translatable(
                            data.getBoolean(EssentiaDataProvider.ACTIVE)
                                    ? "jade.thaumaturge.state.processing"
                                    : "jade.thaumaturge.state.idle")));
        }
    }

    private static void appendEssentia(ITooltip tooltip, BlockAccessor accessor, CompoundTag data) {
        AspectList contents = JadeComponents.decodeAspects(
                data.getList(EssentiaDataProvider.CONTENTS, Tag.TAG_COMPOUND),
                accessor.getLevel().registryAccess());
        int capacity = data.getInt(EssentiaDataProvider.CAPACITY);
        boolean singleCapacityStorage = capacity == 1;
        if (contents.isEmpty()) {
            tooltip.add(
                    singleCapacityStorage
                            ? Component.translatable("jade.thaumaturge.essentia.none")
                            : capacity > 0
                                    ? Component.translatable("jade.thaumaturge.essentia.empty_capacity", capacity)
                                    : Component.translatable("jade.thaumaturge.essentia.empty"));
        } else {
            if (contents.entries().size() == 1 && capacity > 0 && !singleCapacityStorage) {
                AspectInstance entry = contents.entries().get(0);
                tooltip.add(
                        Component.translatable(
                                "jade.thaumaturge.essentia.combined",
                                AspectComponents.name(entry.aspect()),
                                entry.amount(),
                                capacity));
            } else {
                JadeComponents.addAspectLines(tooltip, "jade.thaumaturge.essentia.contents", contents);
                if (capacity > 0 && !singleCapacityStorage) {
                    tooltip.add(
                            Component.translatable(
                                    "jade.thaumaturge.essentia.amount",
                                    contents.totalAmount(),
                                    capacity));
                }
            }
        }
    }

    private static void appendCrucible(ITooltip tooltip, BlockAccessor accessor, CompoundTag data) {
        int heat = data.getInt(EssentiaDataProvider.CRUCIBLE_HEAT);
        String state = heat > 150 ? "boiling" : heat > 0 ? "heating" : "cold";
        tooltip.add(Component.translatable("jade.thaumaturge.crucible.state." + state));

        AspectList contents = JadeComponents.decodeAspects(
                data.getList(EssentiaDataProvider.CONTENTS, Tag.TAG_COMPOUND),
                accessor.getLevel().registryAccess());
        if (!contents.isEmpty()) {
            JadeComponents.addAspectLines(tooltip, "jade.thaumaturge.essentia.contents", contents);
        }
    }

    private static void appendTubeState(ITooltip tooltip, BlockAccessor accessor, CompoundTag data, String tubeKind) {
        if ("valve".equals(tubeKind)) {
            boolean open = data.getBoolean(EssentiaDataProvider.VALVE_OPEN);
            tooltip.add(Component.translatable("jade.thaumaturge.tube.valve." + (open ? "open" : "closed"))
                    .withStyle(open ? ChatFormatting.GREEN : ChatFormatting.RED));
        } else if ("oneway".equals(tubeKind)) {
            tooltip.add(Component.translatable(
                    "jade.thaumaturge.tube.direction",
                    JadeComponents.direction(data.getString(EssentiaDataProvider.FACING))));
        } else if ("restrict".equals(tubeKind)) {
            tooltip.add(Component.translatable("jade.thaumaturge.tube.restricted"));
        }

        if (!accessor.showDetails()) return;

        int suction = data.getInt(EssentiaDataProvider.SUCTION);
        if (suction > 0) {
            String suctionAspect = data.getString(EssentiaDataProvider.SUCTION_ASPECT);
            Component type = suctionAspect.isEmpty()
                    ? Component.translatable("jade.thaumaturge.tube.any_aspect")
                    : JadeComponents.aspectName(
                            suctionAspect, accessor.getLevel().registryAccess());
            tooltip.add(Component.translatable("jade.thaumaturge.tube.suction", type, suction));
        }

        int closed = data.getInt(EssentiaDataProvider.CLOSED_SIDES);
        if (closed != 0) {
            tooltip.add(
                    Component.translatable("jade.thaumaturge.tube.closed_sides", JadeComponents.directions(closed)));
        }

        if ("buffer".equals(tubeKind)) {
            appendChokedSides(tooltip, data.getByteArray(EssentiaDataProvider.CHOKED_SIDES));
        }
    }

    private static void appendChokedSides(ITooltip tooltip, byte[] choked) {
        int partial = 0;
        int blocked = 0;
        for (int i = 0; i < Math.min(choked.length, 6); i++) {
            if (choked[i] == 1) partial |= 1 << i;
            if (choked[i] >= 2) blocked |= 1 << i;
        }
        if (partial != 0) {
            tooltip.add(Component.translatable(
                    "jade.thaumaturge.tube.choked",
                    JadeComponents.directions(partial),
                    Component.translatable("jade.thaumaturge.tube.choke.reduced")));
        }
        if (blocked != 0) {
            tooltip.add(Component.translatable(
                    "jade.thaumaturge.tube.choked",
                    JadeComponents.directions(blocked),
                    Component.translatable("jade.thaumaturge.tube.choke.blocked")));
        }
    }

    private static ResourceLocation optionFor(String area) {
        return switch (area) {
            case "jar" -> JadeConfig.JARS;
            case "alembic" -> JadeConfig.ALEMBICS;
            case "crucible" -> JadeConfig.CRUCIBLES;
            case "tube" -> JadeConfig.TUBES;
            case "valve" -> JadeConfig.VALVES;
            case "restrict" -> JadeConfig.RESTRICTED_TUBES;
            case "filter" -> JadeConfig.FILTER_TUBES;
            case "oneway" -> JadeConfig.ONE_WAY_TUBES;
            case "buffer" -> JadeConfig.BUFFERS;
            case "thaumatorium" -> JadeConfig.THAUMATORIUMS;
            case "centrifuge" -> JadeConfig.CENTRIFUGES;
            default -> null;
        };
    }
}
