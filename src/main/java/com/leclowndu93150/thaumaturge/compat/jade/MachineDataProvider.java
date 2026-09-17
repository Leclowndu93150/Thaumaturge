package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.content.casters.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityEverfullUrn;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.spa.BlockEntitySpa;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum MachineDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = TCIds.rl("machine");
    static final int PERCENT = 100;
    static final String AREA = "MachineArea";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BlockEntity machine = resolveMachine(accessor);
        if (machine instanceof BlockEntityAdvancedAlchemicalFurnace furnace) {
            tag.putString(AREA, "advanced_alchemical_furnace");
            tag.putInt("Heat", furnace.heat());
            tag.putInt("Perditio", furnace.perditio());
            tag.putInt("Aqua", furnace.aqua());
            tag.putInt("PowerMax", BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
            tag.putInt("Cooldown", furnace.cooldown());
            tag.putInt("CycleDuration", furnace.cycleDuration());

            boolean hasInput = !furnace.input().isEmpty();
            int aspectCost = 0;
            if (hasInput) {
                tag.put("Input", furnace.input().save(accessor.getLevel().registryAccess()));
                aspectCost =
                        AspectIndexAccess.index().of(furnace.input().copy()).totalAmount();
                tag.putInt("InputAspectCost", aspectCost);
            }

            String status;
            if (!furnace.assembled()) {
                status = "unformed";
            } else if (furnace.cooldown() > 0) {
                status = "processing";
            } else if (furnace.aspects().totalAmount() >= BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA
                    || (hasInput
                            && furnace.aspects().totalAmount() + aspectCost
                                    > BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA)) {
                status = "storage_full";
            } else if (!hasInput) {
                status = "ready";
            } else if (furnace.heat() < aspectCost * 2
                    || furnace.perditio() < aspectCost
                    || furnace.aqua() < aspectCost) {
                status = "waiting_vis";
            } else {
                status = "ready";
            }
            tag.putString("Status", status);
        } else if (machine instanceof BlockEntitySmelter smelter) {
            tag.putString(AREA, "smelter");
            tag.putInt("SmeltProgress", smelter.getCookProgressScaled(PERCENT));
            tag.putInt("BurnRemaining", smelter.getBurnTimeRemainingScaled(PERCENT));
        } else if (machine instanceof BlockEntityGolemBuilder builder) {
            tag.putString(AREA, "golem_builder");
            tag.putInt("Progress", builder.cost());
            tag.putInt("ProgressMax", builder.maxCost());
            tag.putInt(
                    "OutputCount",
                    builder.output()
                            .getStackInSlot(BlockEntityGolemBuilder.SLOT_OUTPUT)
                            .getCount());
        } else if (machine instanceof BlockEntityVoidSiphon siphon) {
            tag.putString(AREA, "void_siphon");
            tag.putInt("Progress", siphon.progress());
            tag.putInt("ProgressMax", BlockEntityVoidSiphon.PROGRESS_REQUIRED);
            tag.putInt("OutputCount", siphon.output().getStackInSlot(0).getCount());
        } else if (machine instanceof BlockEntityDeconstructionTable table) {
            tag.putString(AREA, "deconstruction_table");
            tag.putBoolean(
                    "HasInput",
                    !table.items()
                            .getStackInSlot(BlockEntityDeconstructionTable.SLOT_INPUT)
                            .isEmpty());
            tag.putInt("Progress", BlockEntityDeconstructionTable.BREAK_TIME_TICKS - table.breakTime());
            tag.putInt("ProgressMax", BlockEntityDeconstructionTable.BREAK_TIME_TICKS);
            if (table.resultAspect() != null)
                tag.putString("ResultAspect", table.resultAspect().toString());
        } else if (machine instanceof BlockEntitySpa spa) {
            tag.putString(AREA, "spa");
            tag.putInt("Fluid", spa.getTank().getFluidAmount());
            tag.putInt("FluidMax", BlockEntitySpa.TANK_CAPACITY);
            tag.putBoolean("Mix", spa.getMix());
        } else if (machine instanceof BlockEntityEverfullUrn) {
            tag.putString(AREA, "everfull_urn");
        } else if (machine instanceof BlockEntityInfernalFurnace furnace) {
            tag.putString(AREA, "infernal_furnace");
            tag.putInt("Progress", furnace.furnaceCookTime);
            tag.putInt("ProgressMax", furnace.furnaceMaxCookTime);
            int stored = 0;
            for (int slot = 0; slot < furnace.inventory().getSlots(); slot++) {
                stored += furnace.inventory().getStackInSlot(slot).getCount();
            }
            tag.putInt("StoredItems", stored);
        } else if (machine instanceof BlockEntityFocalManipulator manipulator) {
            tag.putString(AREA, "focal_manipulator");
            tag.putFloat("VisRemaining", manipulator.vis);
            tag.putBoolean("HasFocus", !manipulator.focusStack().isEmpty());
            tag.putString("FocusName", manipulator.focusName);
        }
    }

    private static BlockEntity resolveMachine(BlockAccessor accessor) {
        BlockEntityAdvancedAlchemicalFurnace advancedFurnace = AdvancedFurnaceJadeAccess.resolve(accessor);
        if (advancedFurnace != null) return advancedFurnace;

        BlockEntity direct = accessor.getBlockEntity();
        if (direct != null) return direct;

        BlockState state = accessor.getBlockState();
        Class<? extends BlockEntity> controllerType;
        if (state.is(TCBlocks.PLACEHOLDER_IRON_BARS)
                || state.is(TCBlocks.PLACEHOLDER_ANVIL)
                || state.is(TCBlocks.PLACEHOLDER_CAULDRON)
                || state.is(TCBlocks.PLACEHOLDER_TABLE)) {
            controllerType = BlockEntityGolemBuilder.class;
        } else if (state.is(TCBlocks.NETHER_BRICKS_PLACEHOLDER) || state.is(TCBlocks.OBSIDIAN_PLACEHOLDER)) {
            controllerType = BlockEntityInfernalFurnace.class;
        } else {
            return null;
        }

        BlockPos origin = accessor.getPosition();
        for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            BlockEntity nearby = accessor.getLevel().getBlockEntity(candidate);
            if (controllerType.isInstance(nearby)) return nearby;
        }
        return null;
    }
}
