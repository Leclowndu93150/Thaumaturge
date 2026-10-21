package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public final class RiftPersistenceTests {
    private RiftPersistenceTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("rift_api/persisted_revision_avoids_aba", 20, helper -> {
            EntityFluxRift original = new EntityFluxRift(TTEntities.FLUX_RIFT.get(), helper.getLevel());
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
            original.addAdditionalSaveData(output);
            output.putLong("api_revision", 1_000_000_000L);
            var saved = output.buildResult();
            EntityFluxRift first = new EntityFluxRift(TTEntities.FLUX_RIFT.get(), helper.getLevel());
            first.setUUID(original.getUUID());
            first.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            EntityFluxRift second = new EntityFluxRift(TTEntities.FLUX_RIFT.get(), helper.getLevel());
            second.setUUID(original.getUUID());
            second.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            helper.assertTrue(first.snapshot().revision() > 1_000_000_000L, "Reload did not advance persisted revision");
            helper.assertTrue(second.snapshot().revision() > first.snapshot().revision(), "Reloading the same persisted identity reused a revision");
            helper.succeed();
        });
    }
}
