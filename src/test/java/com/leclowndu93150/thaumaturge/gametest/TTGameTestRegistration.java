package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class TTGameTestRegistration {
    private TTGameTestRegistration() {}

    public static void registerTests(RegisterGameTestsEvent event) {
        TTTestRegistrar r = new TTTestRegistrar(event);
        DataValidationTests.register(r);
        AuraTests.register(r);
        NodeTests.register(r);
        TransducerTests.register(r);
        RelayTests.register(r);
        TaintTests.register(r);
        EssentiaTests.register(r);
        ResearchTests.register(r);
        GolemSealTests.register(r);
        GolemPressTests.register(r);
        InfernalFurnaceTests.register(r);
        RunicShieldingTests.register(r);
        ManaBeanTests.register(r);
        WandTests.register(r);
    }
}
