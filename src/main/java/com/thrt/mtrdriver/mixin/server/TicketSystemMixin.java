package com.thrt.mtrdriver.mixin.server;

import com.thrt.mtrdriver.server.JourneyTracker;
import org.mtr.core.data.Station;
import org.mtr.mapping.holder.PlayerEntity;
import org.mtr.mapping.holder.World;
import org.mtr.mod.data.TicketSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(TicketSystem.class)
public class TicketSystemMixin {
	@Inject(method = "onEnter", at = @At("RETURN"), remap = false)
	private static void mtd$clearJourneyOnEnter(World world, Station station, PlayerEntity player, boolean showMessage, CallbackInfoReturnable<Boolean> cir) {
		if (world != null && !world.isClient() && cir.getReturnValueZ()) {
			JourneyTracker.clear(player.getUuid());
		}
	}

	@Inject(method = "onExit", at = @At("HEAD"), remap = false)
	private static void mtd$rememberBalance(World world, Station station, PlayerEntity player, boolean showMessage, CallbackInfoReturnable<Boolean> cir) {
		if (world != null && !world.isClient()) {
			JourneyTracker.setBalanceBefore(player.getUuid(), TicketSystem.getBalance(world, player));
		}
	}

	@Inject(method = "onExit", at = @At("RETURN"), remap = false)
	private static void mtd$settleJourney(World world, Station station, PlayerEntity player, boolean showMessage, CallbackInfoReturnable<Boolean> cir) {
		if (world == null || world.isClient()) {
			return;
		}
		final UUID uuid = player.getUuid();
		if (cir.getReturnValueZ()) {
			final int fare = Math.max(0, JourneyTracker.getBalanceBefore(uuid) - TicketSystem.getBalance(world, player));
			JourneyTracker.settle(world, player, fare);
		} else {
			JourneyTracker.clear(uuid);
		}
	}
}
