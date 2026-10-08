package com.leclowndu93150.thaumaturge.content.essentia.flow;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class EssentiaIntake implements IEssentiaTransport {
    private final EssentiaIntakeHost host;
    private final ResourceKey<IAspect> aspect;
    private final int suction;
    private final int attemptInterval;
    private int cooldown;

    public EssentiaIntake(EssentiaIntakeHost host, ResourceKey<IAspect> aspect, int suction, int attemptInterval) {
        this.host = host;
        this.aspect = aspect;
        this.suction = suction;
        this.attemptInterval = attemptInterval;
    }

    public boolean pullOne() {
        if (--cooldown > 0) {
            return false;
        }
        cooldown = attemptInterval;
        Level level = host.getLevel();
        Holder<IAspect> wanted = Aspects.resolve(level, aspect);
        if (wanted == null) {
            return false;
        }
        Direction face = host.intakeFace();
        Direction remoteFace = face.getOpposite();
        IEssentiaTransport supplier =
                EssentiaAccess.transport(level, host.getBlockPos().relative(face), remoteFace);
        if (supplier == null
                || !supplier.canOutputTo(remoteFace)
                || supplier.getSuctionAmount(remoteFace) >= getSuctionAmount(face)) {
            return false;
        }
        return supplier.takeEssentia(wanted, 1, remoteFace) == 1;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == host.intakeFace();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return isConnectable(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return Aspects.resolve(host.getLevel(), aspect);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return isConnectable(face) && host.wantsEssentia() ? suction : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
