package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCSeals;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class GolemBindings implements GolemHelper.Bindings {
    @Override
    public @Nullable ISeal createSeal(Identifier key) {
        return TCSeals.registry().getOptional(key).map(type -> (ISeal) type.factory().get()).orElse(null);
    }

    @Override
    public ItemStack getSealStack(Identifier key) {
        return TCSeals.registry().getOptional(key).map(type -> new ItemStack(type.placerItem().get())).orElse(ItemStack.EMPTY);
    }

    @Override
    public @Nullable ISealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        return SealHandler.getSealEntity(level, pos);
    }

    @Override
    public void addGolemTask(Level level, Task task) {
        TaskHandler.addTask(level, task);
    }

    @Override
    public List<ProvisionRequest> getProvisionRequests(Level level) {
        return level.getData(TCAttachments.GOLEM_TASKS).provisionRequests();
    }
}
