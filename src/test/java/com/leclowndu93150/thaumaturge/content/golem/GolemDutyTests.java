package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.duty.GolemDuties;
import com.leclowndu93150.thaumaturge.api.golems.duty.GolemDuty;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class GolemDutyTests {
    private static final Identifier DUTY_ID = Identifier.fromNamespaceAndPath("thaumaturge", "test_duty");
    private static final GolemAccessory ACCESSORY = GolemAccessories.register(new GolemAccessory(DUTY_ID, GolemAccessory.Group.NONE, 0, 1, 1, 1, 0, false));
    private static final GolemDuty DUTY = GolemDuties.register(new GolemDuty() {
        @Override
        public Identifier id() {
            return DUTY_ID;
        }

        @Override
        public Identifier accessory() {
            return ACCESSORY.id();
        }

        @Override
        public int version() {
            return 1;
        }

        @Override
        public void tick(Context context, CompoundTag state) {
            context.stop();
        }
    });

    private GolemDutyTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("golem/duty_ownership_and_conflict", 20, helper -> {
            ServerPlayer owner = player(helper, "duty_owner");
            EntityThaumaturgeGolem golem = golem(helper, owner);
            require(helper, golem.duty().isEmpty() && golem.hasHome(), "An ordinary golem incorrectly has an exclusive duty");
            ServerPlayer stranger = player(helper, "duty_stranger");
            require(helper, !golem.tryEnterDuty(stranger, DUTY.id(), new CompoundTag()), "A stranger entered the duty");
            require(helper, golem.tryEnterDuty(owner, DUTY.id(), new CompoundTag()), "The owner could not enter the duty");
            require(helper, !golem.tryEnterDuty(owner, DUTY.id(), new CompoundTag()), "A competing entry replaced the duty");
            require(helper, !golem.exitDuty(stranger), "A stranger stopped the duty");
            require(helper, golem.exitDuty(owner), "The owner could not leave the duty");
            helper.succeed();
        });
        registrar.add("golem/duty_releases_tasks_preserves_items", 20, helper -> {
            ServerPlayer owner = player(helper, "duty_carrier");
            EntityThaumaturgeGolem golem = golem(helper, owner);
            BlockPos home = golem.getHomePosition();
            Task task = Task.atBlock(null, home);
            task.assignTo(golem.getUUID());
            task.claim();
            TaskBoard.of(helper.getLevel()).post(task);
            golem.assignJob(task);
            golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND, 3));
            require(helper, golem.tryEnterDuty(owner, DUTY.id(), new CompoundTag()), "Duty entry failed");
            require(helper, !task.isClaimed() && task.isEnded() && golem.activeJob() == null, "The former task retained its claim");
            require(helper, !golem.hasHome(), "The old home still constrains the duty");
            require(helper, golem.getMainHandItem().getCount() == 3, "Duty entry changed carried items");
            require(helper, golem.exitDuty(owner), "Duty exit failed");
            require(helper, golem.hasHome() && golem.getHomePosition().equals(home), "Duty exit did not restore the home");
            require(helper, golem.getMainHandItem().getCount() == 3, "Duty exit changed carried items");
            helper.succeed();
        });
        registrar.add("golem/duty_owner_change_and_accessory_removal", 20, helper -> {
            ServerPlayer owner = player(helper, "duty_change_owner");
            EntityThaumaturgeGolem golem = golem(helper, owner);
            require(helper, golem.tryEnterDuty(owner, DUTY.id(), new CompoundTag()), "Duty entry failed");
            golem.setOwner(player(helper, "duty_next_owner"));
            require(helper, golem.duty().isEmpty() && golem.hasHome(), "Owner change retained the old duty");
            golem.setOwner(owner);
            require(helper, golem.tryEnterDuty(owner, DUTY.id(), new CompoundTag()), "Duty reentry failed");
            golem.removeAccessory(ACCESSORY);
            require(helper, golem.duty().isEmpty() && golem.hasHome(), "Accessory removal retained its duty");
            helper.succeed();
        });
        registrar.add("golem/duty_missing_addon_restores_home", 20, helper -> {
            ServerPlayer owner = player(helper, "duty_reload_owner");
            EntityThaumaturgeGolem golem = golem(helper, owner);
            BlockPos home = golem.getHomePosition();
            int radius = golem.getHomeRadius();
            require(helper, golem.tryEnterDuty(owner, DUTY.id(), new CompoundTag()), "Duty entry failed");
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
            golem.addAdditionalSaveData(output);
            CompoundTag saved = output.buildResult();
            EntityThaumaturgeGolem reloaded = new EntityThaumaturgeGolem(TTEntities.THAUMATURGE_GOLEM.get(), helper.getLevel());
            reloaded.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            require(helper, reloaded.duty().filter(DUTY.id()::equals).isPresent() && !reloaded.hasHome(), "A registered duty did not survive reload");
            CompoundTag duty = saved.getCompoundOrEmpty("duty");
            duty.putString("id", "missing:duty");
            saved.put("duty", duty);
            EntityThaumaturgeGolem restored = new EntityThaumaturgeGolem(TTEntities.THAUMATURGE_GOLEM.get(), helper.getLevel());
            restored.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            require(helper, restored.duty().isEmpty(), "A missing addon retained an active duty");
            require(helper, restored.hasHome() && restored.getHomePosition().equals(home) && restored.getHomeRadius() == radius, "A missing addon lost the former home");
            helper.succeed();
        });
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name));
        BlockPos position = helper.absolutePos(new BlockPos(2, 1, 2));
        player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        return player;
    }

    private static EntityThaumaturgeGolem golem(GameTestHelper helper, ServerPlayer owner) {
        EntityThaumaturgeGolem golem = new EntityThaumaturgeGolem(TTEntities.THAUMATURGE_GOLEM.get(), helper.getLevel());
        golem.setPos(owner.position());
        golem.setOwner(owner);
        golem.setValidSpawn();
        golem.setHomeTo(owner.blockPosition(), 16);
        golem.fitAccessory(ACCESSORY, ItemStack.EMPTY);
        return golem;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }
}
