package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEntityEssentiaCrystalizer;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEntityEssentiaReservoir;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeOneway;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeRestrict;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

final class JadeEssentiaDetails {
    private JadeEssentiaDetails() {}

    static void jar(BlockEntityJar jar, JadeDetailBuilder data) {
        data.storage(jar.getContents(jar.getLevel().registryAccess()), jar.capacity(), false);
        filter(data, jar, jar.aspectFilterKey());
    }

    static void alembic(BlockEntityAlembic alembic, JadeDetailBuilder data) {
        data.storage(alembic.getAspects(), BlockEntityAlembic.CAPACITY, false);
        filter(data, alembic, alembic.aspectFilterKey());
    }

    static void crucible(BlockEntityCrucible crucible, JadeDetailBuilder data) {
        data.hideFluid();
        int heat = crucible.getHeat();
        data.summary(
                heat > 150
                        ? "jade.thaumaturge.crucible.state.boiling"
                        : heat > 0
                                ? "jade.thaumaturge.crucible.state.heating"
                                : "jade.thaumaturge.crucible.state.cold");
        if (!crucible.getAspects().isEmpty()) data.storage(crucible.getAspects(), 0, false);
    }

    static void thaumatorium(BlockEntityThaumatorium machine, JadeDetailBuilder data) {
        data.storage(machine.essentia(), 0, false);
        data.summary("jade.thaumaturge.thaumatorium.recipes", machine.queue().size(), machine.maxRecipes());
    }

    static void centrifuge(BlockEntityCentrifuge machine, JadeDetailBuilder data) {
        AspectList contents = AspectList.EMPTY;
        if (machine.getEssentiaType(Direction.UP) != null && machine.getEssentiaAmount(Direction.UP) > 0) {
            contents = contents.add(machine.getEssentiaType(Direction.UP), machine.getEssentiaAmount(Direction.UP));
        }
        data.storage(contents, 1, false);
        data.summary(
                "jade.thaumaturge.centrifuge.state",
                Component.translatable(
                        machine.isSpinning() ? "jade.thaumaturge.state.processing" : "jade.thaumaturge.state.idle"));
    }

    static void crystalizer(BlockEntityEssentiaCrystalizer machine, JadeDetailBuilder data) {
        AspectList contents = AspectList.EMPTY;
        if (machine.getEssentiaType(Direction.UP) != null) {
            contents = contents.add(machine.getEssentiaType(Direction.UP), 1);
        }
        data.summary(contents.isEmpty() ? "jade.thaumaturge.state.idle" : "jade.thaumaturge.state.processing");
        data.storage(contents, 1, false);
        if (!contents.isEmpty())
            data.detail(
                    "jade.thaumaturge.machine.progress",
                    Math.min(100, machine.progress() * 100 / BlockEntityEssentiaCrystalizer.TARGET_PROGRESS));
    }

    static void reservoir(BlockEntityEssentiaReservoir reservoir, JadeDetailBuilder data) {
        data.storage(reservoir.contents(), BlockEntityEssentiaReservoir.CAPACITY, false);
    }

    static void tube(BlockEntityTube tube, JadeDetailBuilder data) {
        AspectList contents = AspectList.EMPTY;
        if (tube.essentiaKey() != null && tube.essentiaAmountRaw() > 0) {
            contents = contents.add(
                    tube.getLevel()
                            .registryAccess()
                            .lookupOrThrow(IAspect.REGISTRY_KEY)
                            .getOrThrow(tube.essentiaKey()),
                    tube.essentiaAmountRaw());
        }
        data.storage(contents, 1, true);
        tubeState(tube, data);
    }

    static void buffer(BlockEntityTubeBuffer tube, JadeDetailBuilder data) {
        data.storage(tube.contents(), 0, true);
        data.detail("jade.thaumaturge.essentia.amount", tube.visSize(), BlockEntityTubeBuffer.MAX_AMOUNT);
        closedSides(tube::isSideOpen, data);
        int reduced = 0;
        int blocked = 0;
        for (Direction face : Direction.values()) {
            int choke = tube.chokedSide(face);
            if (choke == 1) reduced |= 1 << face.ordinal();
            if (choke >= 2) blocked |= 1 << face.ordinal();
        }
        if (reduced != 0)
            data.detail(
                    "jade.thaumaturge.tube.choked",
                    directions(reduced),
                    Component.translatable("jade.thaumaturge.tube.choke.reduced"));
        if (blocked != 0)
            data.detail(
                    "jade.thaumaturge.tube.choked",
                    directions(blocked),
                    Component.translatable("jade.thaumaturge.tube.choke.blocked"));
    }

    static void valve(BlockEntityTubeValve tube, JadeDetailBuilder data) {
        tube(tube, data);
        data.summary(tube.allowFlow() ? "jade.thaumaturge.tube.valve.open" : "jade.thaumaturge.tube.valve.closed");
    }

    static void filterTube(BlockEntityTubeFilter tube, JadeDetailBuilder data) {
        tube(tube, data);
        AspectList filter = tube.queryAspects();
        filter(
                data,
                tube,
                filter.isEmpty()
                        ? null
                        : filter.entries().getFirst().aspect().unwrapKey().orElse(null));
    }

    static void oneway(BlockEntityTubeOneway tube, JadeDetailBuilder data) {
        tube(tube, data);
        data.summary("jade.thaumaturge.tube.direction", direction(tube.facing().getOpposite()));
    }

    static void restricted(BlockEntityTubeRestrict tube, JadeDetailBuilder data) {
        tube(tube, data);
        data.summary("jade.thaumaturge.tube.restricted");
    }

    private static void tubeState(BlockEntityTube tube, JadeDetailBuilder data) {
        if (tube.suctionRaw() > 0)
            data.detail("jade.thaumaturge.tube.suction", aspectName(data, tube, tube.suctionKey()), tube.suctionRaw());
        closedSides(tube::isSideOpen, data);
    }

    private static void closedSides(Predicate<Direction> isOpen, JadeDetailBuilder data) {
        int closed = 0;
        for (Direction face : Direction.values()) if (!isOpen.test(face)) closed |= 1 << face.ordinal();
        if (closed != 0) data.detail("jade.thaumaturge.tube.closed_sides", directions(closed));
    }

    private static void filter(JadeDetailBuilder data, BlockEntity entity, @Nullable ResourceKey<IAspect> aspect) {
        if (aspect == null) data.summary("jade.thaumaturge.essentia.unfiltered");
        else data.summary("jade.thaumaturge.essentia.filter", aspectName(data, entity, aspect));
    }

    private static Component aspectName(
            JadeDetailBuilder data, BlockEntity entity, @Nullable ResourceKey<IAspect> aspect) {
        return aspect == null
                ? Component.translatable("jade.thaumaturge.tube.any_aspect")
                : data.aspectName(entity.getLevel()
                        .registryAccess()
                        .lookupOrThrow(IAspect.REGISTRY_KEY)
                        .getOrThrow(aspect));
    }

    private static Component direction(Direction face) {
        return Component.translatable("jade.thaumaturge.direction." + face.getSerializedName());
    }

    private static Component directions(int mask) {
        MutableComponent text = Component.empty();
        boolean first = true;
        for (Direction face : Direction.values()) {
            if ((mask & 1 << face.ordinal()) == 0) continue;
            if (!first) text.append(", ");
            text.append(direction(face));
            first = false;
        }
        return text;
    }
}
