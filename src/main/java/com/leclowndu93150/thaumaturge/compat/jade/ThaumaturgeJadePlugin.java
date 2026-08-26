package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class ThaumaturgeJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EssentiaDataProvider.INSTANCE, Block.class);
        registration.registerBlockDataProvider(MachineDataProvider.INSTANCE, Block.class);
        registration.registerEntityDataProvider(GolemDataProvider.INSTANCE, EntityThaumaturgeGolem.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        JadeConfig.register(registration);
        registration.registerBlockComponent(NodeComponentProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(EssentiaComponentProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(MachineComponentProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(GolemComponentProvider.INSTANCE, EntityThaumaturgeGolem.class);
    }
}
