package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MachineComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = TCIds.rl("machine");

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
        if (accessor.getBlockEntity() instanceof BlockEntityVisRelay relay) {
            if (!JadeConfig.shouldShow(config, JadeConfig.VIS_RELAYS, accessor)) return;
            if (!relay.isLinked()) {
                tooltip.add(Component.translatable("jade.thaumaturge.relay.unlinked"));
            } else if (!accessor.showDetails()) {
                tooltip.add(Component.translatable("jade.thaumaturge.relay.linked"));
            } else if (relay.depth() == 1) {
                tooltip.add(Component.translatable("jade.thaumaturge.relay.linked_node"));
            } else {
                tooltip.add(Component.translatable("jade.thaumaturge.relay.linked_relay", relay.depth() - 1));
            }
            return;
        }
        if (accessor.getBlockEntity() instanceof BlockEntityNodeTransducer transducer) {
            if (!JadeConfig.shouldShow(config, JadeConfig.NODE_TRANSDUCERS, accessor)) return;
            tooltip.add(Component.translatable("jade.thaumaturge.transducer.status." + transducer.getStatus()));
            if (transducer.getStatus() != 0 && accessor.showDetails()) {
                tooltip.add(Component.translatable(
                        "jade.thaumaturge.transducer.charge",
                        transducer.getCount() * 100 / BlockEntityNodeTransducer.CHARGE_TARGET));
            }
            return;
        }
        CompoundTag data = accessor.getServerData();
        String area = data.getString(MachineDataProvider.AREA);
        ResourceLocation option = machineOption(area);
        if (option == null) return;
        boolean show = JadeConfig.shouldShow(config, option, accessor);
        if ("everfull_urn".equals(area)) {
            if (!show) tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
            return;
        }
        if (!show) return;

        if ("golem_builder".equals(area)) {
            tooltip.replace(
                    JadeIds.CORE_OBJECT_NAME,
                    IThemeHelper.get().title(Component.translatable("block.thaumaturge.golem_builder")));
        } else if ("infernal_furnace".equals(area)) {
            tooltip.replace(
                    JadeIds.CORE_OBJECT_NAME,
                    IThemeHelper.get().title(Component.translatable("block.thaumaturge.infernal_furnace")));
        }

        if (!"smelter".equals(area)) {
            appendMachineState(tooltip, accessor, data, area, accessor.showDetails());
            return;
        }
        int progress = data.getInt("SmeltProgress");
        int burn = data.getInt("BurnRemaining");
        tooltip.add(Component.translatable(
                progress > 0 || burn > 0 ? "jade.thaumaturge.state.processing" : "jade.thaumaturge.state.idle"));
        if (!accessor.showDetails()) return;
        if (progress > 0) {
            tooltip.add(Component.translatable("jade.thaumaturge.machine.progress", progress));
        }
        if (burn > 0) {
            tooltip.add(Component.translatable("jade.thaumaturge.machine.heat", burn));
        }
    }

    private static void appendMachineState(
            ITooltip tooltip, BlockAccessor accessor, CompoundTag data, String area, boolean detailed) {
        int progress = data.getInt("Progress");
        int maximum = data.getInt("ProgressMax");
        if ("golem_builder".equals(area)) {
            tooltip.add(Component.translatable(
                    maximum > 0 ? "jade.thaumaturge.state.processing" : "jade.thaumaturge.state.idle"));
            if (detailed && maximum > 0) {
                int percent = Math.min(100, Math.max(0, (maximum - progress) * 100 / maximum));
                tooltip.add(Component.translatable("jade.thaumaturge.machine.progress", percent));
            }
            int output = data.getInt("OutputCount");
            if (output > 0) {
                tooltip.add(Component.translatable("jade.thaumaturge.machine.output", output));
            }
            return;
        }
        if (detailed && maximum > 0 && (!"deconstruction_table".equals(area) || data.getBoolean("HasInput"))) {
            int percent = Math.min(100, Math.max(0, progress * 100 / maximum));
            tooltip.add(Component.translatable("jade.thaumaturge.machine.progress", percent));
        }

        if ("void_siphon".equals(area)) {
            tooltip.add(Component.translatable("jade.thaumaturge.machine.output", data.getInt("OutputCount")));
        } else if ("deconstruction_table".equals(area)) {
            if (!data.getBoolean("HasInput")) {
                tooltip.add(Component.translatable("jade.thaumaturge.state.idle"));
            }
            String result = data.getString("ResultAspect");
            if (!result.isEmpty()) {
                tooltip.add(Component.translatable(
                        "jade.thaumaturge.deconstruction.result",
                        JadeComponents.aspectName(result, accessor.getLevel().registryAccess())));
            }
        } else if ("spa".equals(area)) {
            if (detailed) {
                tooltip.add(Component.translatable(
                        "jade.thaumaturge.machine.fluid", data.getInt("Fluid"), data.getInt("FluidMax")));
            }
            tooltip.add(Component.translatable(
                    data.getBoolean("Mix") ? "jade.thaumaturge.spa.mix" : "jade.thaumaturge.spa.water"));
        } else if ("infernal_furnace".equals(area)) {
            tooltip.add(Component.translatable("jade.thaumaturge.machine.stored_items", data.getInt("StoredItems")));
        } else if ("focal_manipulator".equals(area)) {
            if (detailed) {
                tooltip.add(Component.translatable(
                        "jade.thaumaturge.focal.vis", Math.round(data.getFloat("VisRemaining"))));
            }
            if (data.getBoolean("HasFocus")) {
                String focusName = data.getString("FocusName");
                tooltip.add(
                        focusName.isEmpty()
                                ? Component.translatable("jade.thaumaturge.focal.focus_inserted")
                                : Component.translatable("jade.thaumaturge.focal.focus", focusName));
            } else {
                tooltip.add(Component.translatable("jade.thaumaturge.focal.no_focus"));
            }
        }
    }

    private static ResourceLocation machineOption(String area) {
        return switch (area) {
            case "smelter" -> JadeConfig.SMELTERS;
            case "golem_builder" -> JadeConfig.GOLEM_BUILDERS;
            case "void_siphon" -> JadeConfig.VOID_SIPHONS;
            case "deconstruction_table" -> JadeConfig.DECONSTRUCTION_TABLES;
            case "spa" -> JadeConfig.SPAS;
            case "everfull_urn" -> JadeConfig.EVERFULL_URNS;
            case "infernal_furnace" -> JadeConfig.INFERNAL_FURNACES;
            case "focal_manipulator" -> JadeConfig.FOCAL_MANIPULATORS;
            default -> null;
        };
    }
}
