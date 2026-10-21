package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.content.recipe.workbench.JarUpgradeTests;
import com.leclowndu93150.thaumaturge.api.research.scan.InventoryScanTests;
import com.leclowndu93150.thaumaturge.content.aura.AuraRetrogenTests;
import com.leclowndu93150.thaumaturge.content.aura.AuraBaselineTests;
import com.leclowndu93150.thaumaturge.content.aspect.AspectSupplementTests;
import com.leclowndu93150.thaumaturge.content.golem.GolemDutyTests;
import com.leclowndu93150.thaumaturge.content.entity.RiftTransferTests;
import com.leclowndu93150.thaumaturge.content.entity.RiftPersistenceTests;
import com.leclowndu93150.thaumaturge.content.entity.RiftAuthorizationTests;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionPersistenceTests;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellAuthorityTests;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellCarrierAuthorityTests;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeTransferTests;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class TTGameTestRegistration {
    private TTGameTestRegistration() {}

    public static void registerTests(RegisterGameTestsEvent event) {
        TTTestRegistrar r = new TTTestRegistrar(event);
        DataValidationTests.register(r);
        AspectSupplementTests.register(r);
        AuraTests.register(r);
        AuraRetrogenTests.register(r);
        AuraBaselineTests.register(r);
        RiftApiTests.register(r);
        RiftTransferTests.register(r);
        RiftPersistenceTests.register(r);
        RiftAuthorizationTests.register(r);
        NodeTests.register(r);
        NodeApiTests.register(r);
        NodeBehaviorTests.register(r);
        NodeTransferTests.register(r);
        TransducerTests.register(r);
        RelayTests.register(r);
        TaintTests.register(r);
        TaintApiTests.register(r);
        RevealingApiTests.register(r);
        WarpStrengthApiTests.register(r);
        WarpStrengthTests.register(r);
        SpellAuthorityTests.register(r);
        SpellAuthorityEngineTests.register(r);
        SpellCarrierAuthorityTests.register(r);
        ResearchGateTests.register(r);
        ResearchNoteApiTests.register(r);
        VisDiscountTests.register(r);
        AspectOrbTests.register(r);
        WandAssemblyApiTests.register(r);
        InfusionAutomationTests.register(r);
        InfusionDelegationTests.register(r);
        DynamicInfusionTests.register(r);
        InfusionPersistenceTests.register(r);
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
