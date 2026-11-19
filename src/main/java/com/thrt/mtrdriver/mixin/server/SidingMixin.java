package com.thrt.mtrdriver.mixin.server;

import com.thrt.mtrdriver.server.JourneyTracker;
import org.mtr.core.data.Siding;
import org.mtr.core.data.Vehicle;
import org.mtr.core.data.VehicleRidingEntity;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Siding.class)
public class SidingMixin {
	@Shadow(remap = false)
	private ObjectArraySet<Vehicle> vehicles;

	@Inject(method = "updateVehicleRidingEntities", at = @At("HEAD"), remap = false)
	private void mtd$trackRoutes(long vehicleId, ObjectArrayList<VehicleRidingEntity> ridingEntities, CallbackInfo ci) {
		long routeId = 0;
		for (Vehicle vehicle : vehicles) {
			if (vehicle.getId() == vehicleId) {
				routeId = vehicle.vehicleExtraData.getThisRouteId();
				break;
			}
		}
		if (routeId == 0 || ridingEntities == null) {
			return;
		}
		for (VehicleRidingEntity entity : ridingEntities) {
			if (entity.isOnVehicle()) {
				JourneyTracker.addRoute(entity.uuid, routeId);
			}
		}
	}
}
