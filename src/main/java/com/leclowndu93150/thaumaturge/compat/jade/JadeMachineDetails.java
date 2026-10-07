package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.spa.BlockEntitySpa;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

final class JadeMachineDetails {
    private JadeMachineDetails() {}
    static void relay(BlockEntityVisRelay relay, JadeDetailBuilder data) {
        data.summary(relay.isLinked() ? "jade.thaumaturge.relay.linked" : "jade.thaumaturge.relay.unlinked");
        if (relay.isLinked()) {
            if (relay.depth() == 1)
                linkedSource(relay, data);
            else
                data.detail("jade.thaumaturge.relay.linked_relay", relay.depth() - 1);
        }
    }
    private static void linkedSource(BlockEntityVisRelay relay, JadeDetailBuilder data) {
        if (relay.getLevel() == null || relay.parentPos() == null || relay.getLevel().getBlockEntity(relay.parentPos()) instanceof BlockEntityNode)
            data.detail("jade.thaumaturge.relay.linked_node");
        else
            data.detail("jade.thaumaturge.relay.linked_source", relay.getLevel().getBlockState(relay.parentPos()).getBlock().getName());
    }
    static void transducer(BlockEntityNodeTransducer machine, JadeDetailBuilder data) {
        data.summary("jade.thaumaturge.transducer.status." + machine.getStatus());
        if (machine.getStatus() != 0)
            data.summary("jade.thaumaturge.transducer.charge", machine.getCount() * 100 / BlockEntityNodeTransducer.CHARGE_TARGET);
    }
    static void smelter(BlockEntitySmelter machine, JadeDetailBuilder data) {
        int progress = machine.getCookProgressScaled(100);
        int burn = machine.getBurnTimeRemainingScaled(100);
        data.summary(progress > 0 || burn > 0 ? "jade.thaumaturge.state.processing" : "jade.thaumaturge.state.idle");
        if (progress > 0)
            data.detail("jade.thaumaturge.machine.progress", progress);
        if (burn > 0)
            data.detail("jade.thaumaturge.machine.heat", burn);
    }
    static void advancedFurnace(BlockEntityAdvancedAlchemicalFurnace machine, JadeDetailBuilder data) {
        data.title(Component.translatable("block.thaumaturge.advanced_alchemical_furnace"));
        int cost = machine.inputCost();
        data.summary("jade.thaumaturge.advanced_furnace.status." + advancedFurnaceStatus(machine, cost));
        progress(data, machine.cycleDuration() - machine.cooldown(), machine.cycleDuration());
        data.storage(machine.aspects(), 0, false);
        if (!machine.input().isEmpty()) {
            data.detail("jade.thaumaturge.advanced_furnace.input", machine.input().getHoverName());
            data.detail("jade.thaumaturge.advanced_furnace.input_cost", cost * BlockEntityAdvancedAlchemicalFurnace.HEAT_PER_ASPECT, cost, cost);
        }
        data.detail("jade.thaumaturge.advanced_furnace.ignis", machine.heat(), BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
        data.detail("jade.thaumaturge.advanced_furnace.perditio", machine.perditio(), BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
        data.detail("jade.thaumaturge.advanced_furnace.aqua", machine.aqua(), BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
    }
    private static String advancedFurnaceStatus(BlockEntityAdvancedAlchemicalFurnace machine, int cost) {
        if (!machine.isAssembled())
            return "unformed";
        if (machine.cooldown() > 0)
            return "processing";
        int stored = machine.aspects().totalAmount();
        if (stored >= BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA || stored + cost > BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA)
            return "storage_full";
        if (cost > 0 && (machine.heat() < cost * BlockEntityAdvancedAlchemicalFurnace.HEAT_PER_ASPECT || machine.perditio() < cost || machine.aqua() < cost))
            return "waiting_vis";
        return "ready";
    }
    static void golemBuilder(BlockEntityGolemBuilder machine, JadeDetailBuilder data) {
        data.title(Component.translatable("block.thaumaturge.golem_builder"));
        data.summary(machine.maxCost() > 0 ? "jade.thaumaturge.state.processing" : "jade.thaumaturge.state.idle");
        progress(data, machine.maxCost() - machine.cost(), machine.maxCost());
        int output = machine.output().getAmountAsInt(BlockEntityGolemBuilder.SLOT_OUTPUT);
        if (output > 0)
            data.summary("jade.thaumaturge.machine.output", output);
    }
    static void fluxScrubber(BlockEntityFluxScrubber machine, JadeDetailBuilder data) {
        data.summary("jade.thaumaturge.flux_scrubber.charges", machine.charges());
        data.detail("jade.thaumaturge.flux_scrubber.power", Math.min(100, machine.power() * 100 / BlockEntityFluxScrubber.WORK_POWER));
    }
    static void siphon(BlockEntityVoidSiphon machine, JadeDetailBuilder data) {
        progress(data, machine.progress(), BlockEntityVoidSiphon.PROGRESS_REQUIRED);
        data.summary("jade.thaumaturge.machine.output", machine.output().getAmountAsInt(0));
    }
    static void deconstruction(BlockEntityDeconstructionTable machine, JadeDetailBuilder data) {
        if (machine.items().getResource(BlockEntityDeconstructionTable.SLOT_INPUT).isEmpty())
            data.summary("jade.thaumaturge.state.idle");
        else
            progress(data, BlockEntityDeconstructionTable.BREAK_TIME_TICKS - machine.breakTime(), BlockEntityDeconstructionTable.BREAK_TIME_TICKS);
        if (machine.resultAspect() != null)
            data.summary("jade.thaumaturge.deconstruction.result",
                    data.aspectName(machine.getLevel().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(ResourceKey.create(IAspect.REGISTRY_KEY, machine.resultAspect()))));
    }
    static void spa(BlockEntitySpa machine, JadeDetailBuilder data) {
        data.detail("jade.thaumaturge.machine.fluid", machine.getTank().getAmountAsInt(0), BlockEntitySpa.TANK_CAPACITY);
        data.summary(machine.getMix() ? "jade.thaumaturge.spa.mix" : "jade.thaumaturge.spa.water");
    }
    static void furnace(BlockEntityInfernalFurnace machine, JadeDetailBuilder data) {
        data.title(Component.translatable("block.thaumaturge.infernal_furnace"));
        progress(data, machine.furnaceCookTime, machine.furnaceMaxCookTime);
        int stored = 0;
        for (int i = 0; i < machine.inventory().size(); i++)
            stored += machine.inventory().getAmountAsInt(i);
        data.summary("jade.thaumaturge.machine.stored_items", stored);
    }
    static void focal(BlockEntityFocalManipulator machine, JadeDetailBuilder data) {
        data.detail("jade.thaumaturge.focal.vis", Math.round(machine.vis()));
        if (machine.focusStack().isEmpty())
            data.summary("jade.thaumaturge.focal.no_focus");
        else if (machine.name().isEmpty())
            data.summary("jade.thaumaturge.focal.focus_inserted");
        else
            data.summary("jade.thaumaturge.focal.focus", machine.name());
    }
    private static void progress(JadeDetailBuilder data, int value, int maximum) {
        if (maximum > 0)
            data.detail("jade.thaumaturge.machine.progress", Math.clamp(value * 100 / maximum, 0, 100));
    }
}
