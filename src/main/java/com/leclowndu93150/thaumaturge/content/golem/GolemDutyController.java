package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.duty.GolemDuties;
import com.leclowndu93150.thaumaturge.api.golems.duty.GolemDuty;
import com.leclowndu93150.thaumaturge.api.golems.duty.GolemDuty.StopReason;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

final class GolemDutyController {
    private static final double OWNER_RANGE_SQR = 32 * 32;
    private static final double INTERACTION_RANGE_SQR = 8 * 8;
    private final EntityThaumaturgeGolem golem;
    private @Nullable GolemDuty active;
    private CompoundTag state = new CompoundTag();
    private @Nullable UUID owner;
    private BlockPos home = BlockPos.ZERO;
    private int homeRadius;
    private boolean hadHome;
    private boolean following;

    GolemDutyController(EntityThaumaturgeGolem golem) {
        this.golem = golem;
    }

    private void requireServer() {
        if (!(golem.level() instanceof ServerLevel level) || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Golem duties require the server thread");
        }
    }

    boolean authorized(ServerPlayer actor) {
        requireServer();
        return actor.isAlive() && !actor.isSpectator() && golem.isAlive() && actor.level() == golem.level() && golem.isOwner(actor) && golem.distanceToSqr(actor) <= INTERACTION_RANGE_SQR;
    }

    boolean enter(ServerPlayer actor, Identifier id, CompoundTag initialState) {
        if (!authorized(actor) || active != null) {
            return false;
        }
        GolemDuty duty = GolemDuties.get(id).orElse(null);
        if (duty == null || !equipped(duty)) {
            return false;
        }
        CompoundTag copied = initialState.copy();
        home = golem.getHomePosition().immutable();
        homeRadius = golem.getHomeRadius();
        hadHome = golem.hasHome();
        following = golem.isTrailingOwner();
        owner = actor.getUUID();
        state = copied;
        active = duty;
        TaskBoard.of(golem.level()).cancelFor((ServerLevel) golem.level(), golem);
        golem.releaseTask();
        golem.setTarget(null);
        golem.setDutyPresentation(duty.id());
        golem.recomputeStats();
        golem.clearHome();
        return true;
    }

    boolean exit(ServerPlayer actor) {
        if (!authorized(actor) || active == null) {
            return false;
        }
        leave(StopReason.REQUESTED);
        return true;
    }

    private boolean equipped(GolemDuty duty) {
        return golem.getAccessories().stream().anyMatch(accessory -> accessory.id().equals(duty.accessory()));
    }

    void leave(StopReason reason) {
        if (active == null) {
            return;
        }
        GolemDuty previous = active;
        CompoundTag previousState = state;
        active = null;
        state = new CompoundTag();
        restore();
        try {
            previous.stopped(golem, previousState, reason);
        } catch (RuntimeException exception) {
            Thaumaturge.LOGGER.error("Golem duty {} failed to stop", previous.id(), exception);
        }
    }

    private void restore() {
        golem.setDutyPresentation(null);
        golem.setTarget(null);
        golem.getNavigation().stop();
        golem.setTrailingOwner(following);
        golem.recomputeStats();
        if (hadHome) {
            golem.setHomeTo(home, homeRadius);
        } else {
            golem.clearHome();
        }
    }

    void validate() {
        if (active == null) {
            return;
        }
        if (!golem.ownerIdentity().filter(identity -> identity.equals(owner)).isPresent()) {
            leave(StopReason.OWNER_CHANGED);
        } else if (!equipped(active)) {
            leave(StopReason.ACCESSORY_REMOVED);
        } else if (!(golem.getOwner() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator() || player.level() != golem.level()
                || golem.distanceToSqr(player) > OWNER_RANGE_SQR) {
            leave(StopReason.OWNER_UNAVAILABLE);
        } else if (golem.getTarget() != null) {
            Context context = new Context(player, active);
            try {
                if (!context.attack(golem.getTarget())) {
                    golem.setTarget(null);
                    golem.getNavigation().stop();
                }
            } finally {
                context.valid = false;
            }
        }
    }

    void tick() {
        validate();
        if (active == null || golem.isFettered() || !(golem.getOwner() instanceof ServerPlayer player)) {
            return;
        }
        GolemDuty duty = active;
        Context context = new Context(player, duty);
        try {
            duty.tick(context, state);
        } catch (RuntimeException exception) {
            Thaumaturge.LOGGER.error("Golem duty {} failed to tick", duty.id(), exception);
            leave(StopReason.CALLBACK_FAILED);
        } finally {
            context.valid = false;
        }
    }

    void save(ValueOutput output) {
        if (active == null) {
            return;
        }
        ValueOutput saved = output.child("duty");
        saved.putString("id", active.id().toString());
        saved.putInt("version", active.version());
        saved.store("state", CompoundTag.CODEC, state);
        saved.store("home", BlockPos.CODEC, home);
        saved.putInt("home_radius", homeRadius);
        saved.putBoolean("had_home", hadHome);
        saved.putBoolean("following", following);
        saved.putString("owner", owner.toString());
    }

    void load(ValueInput input) {
        ValueInput saved = input.childOrEmpty("duty");
        String id = saved.getStringOr("id", "");
        if (id.isEmpty()) {
            return;
        }
        home = saved.read("home", BlockPos.CODEC).orElse(golem.blockPosition());
        homeRadius = Math.max(0, saved.getIntOr("home_radius", GolemStatSheet.homeRadius(golem)));
        hadHome = saved.getBooleanOr("had_home", true);
        following = saved.getBooleanOr("following", false);
        Identifier parsed = Identifier.tryParse(id);
        GolemDuty duty = parsed == null ? null : GolemDuties.get(parsed).orElse(null);
        try {
            owner = UUID.fromString(saved.getStringOr("owner", ""));
        } catch (IllegalArgumentException exception) {
            restore();
            return;
        }
        if (duty == null || duty.version() != saved.getIntOr("version", -1) || !equipped(duty)) {
            restore();
            return;
        }
        active = duty;
        state = saved.read("state", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        golem.setDutyPresentation(duty.id());
        golem.recomputeStats();
        golem.clearHome();
    }

    private final class Context implements GolemDuty.Context {
        private final ServerPlayer player;
        private final GolemDuty duty;
        private boolean valid = true;

        private Context(ServerPlayer player, GolemDuty duty) {
            this.player = player;
            this.duty = duty;
        }

        @Override
        public IGolemAPI golem() {
            return golem;
        }

        @Override
        public ServerPlayer owner() {
            return player;
        }

        private boolean allowed(BlockPos position) {
            requireServer();
            ServerLevel level = (ServerLevel) golem.level();
            return valid && active == duty && player.level() == level && player.isAlive() && !player.isSpectator() && player.blockPosition().distSqr(position) <= OWNER_RANGE_SQR
                    && level.hasChunkAt(position) && level.getWorldBorder().isWithinBounds(position) && level.mayInteract(player, position);
        }

        @Override
        public boolean moveTo(BlockPos position) {
            return allowed(position) && golem.getNavigation().moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5, golem.travelSpeed());
        }

        @Override
        public boolean attack(LivingEntity target) {
            if (target == golem || target.level() != golem.level() || !target.isAlive() || target.isInvulnerable() || target.isSpectator() || golem.isAlliedTo(target) || target == player
                    || !allowed(target.blockPosition()) || target instanceof Player victim && (!((ServerLevel) golem.level()).isPvpAllowed() || !player.canHarmPlayer(victim))) {
                return false;
            }
            golem.setTarget(target);
            return true;
        }

        @Override
        public void stop() {
            requireServer();
            if (valid && active == duty) {
                golem.getNavigation().stop();
                golem.setTarget(null);
            }
        }
    }
}
