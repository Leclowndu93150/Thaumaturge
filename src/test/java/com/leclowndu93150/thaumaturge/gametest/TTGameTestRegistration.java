package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.content.recipe.workbench.JarUpgradeTests;
import com.leclowndu93150.thaumaturge.api.research.scan.InventoryScanTests;
import com.leclowndu93150.thaumaturge.content.aura.AuraRetrogenTests;
import com.leclowndu93150.thaumaturge.content.golem.GolemDutyTests;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeTransferTests;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class TTGameTestRegistration {
    private TTGameTestRegistration() {}

    public static void registerTests(RegisterGameTestsEvent event) {
        TTTestRegistrar r = new TTTestRegistrar(event);
        DataValidationTests.register(r);
        AuraTests.register(r);
        AuraRetrogenTests.register(r);
        NodeTests.register(r);
        NodeApiTests.register(r);
        NodeBehaviorTests.register(r);
        NodeTransferTests.register(r);
        TransducerTests.register(r);
        RelayTests.register(r);
        TaintTests.register(r);
        TaintApiTests.register(r);
        RevealingApiTests.register(r);
        ResearchGateTests.register(r);
        ResearchNoteApiTests.register(r);
        VisDiscountTests.register(r);
        AspectOrbTests.register(r);
        WandAssemblyApiTests.register(r);
        InfusionAutomationTests.register(r);
        InfusionDelegationTests.register(r);
        DynamicInfusionTests.register(r);
        EssentiaTests.register(r);
        ResearchTests.register(r);
        GolemSealTests.register(r);
        GolemDutyTests.register(r);
        GolemPressTests.register(r);
        InfernalFurnaceTests.register(r);
        RunicShieldingTests.register(r);
        ManaBeanTests.register(r);
        WandTests.register(r);
        WandTransactionTests.register(r);
        WandStorageTests.register(r);
        JarUpgradeTests.register(r);
        ItemGrateTests.register(r);
        HungryNodeWardTests.register(r);
        InventoryScanTests.register(r);
    }
}
