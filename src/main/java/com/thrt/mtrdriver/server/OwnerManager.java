package com.thrt.mtrdriver.server;

import com.thrt.mtrdriver.owner.OwnerChannels;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.mtr.mapping.mapper.PersistenceStateExtension;

import java.util.Map;

public final class OwnerManager {
	private static final String STATE_ID = "mtrdriver_owners";

	private OwnerManager() {
	}

	public static OwnerState getState(MinecraftServer server) {
		final org.mtr.mapping.holder.MinecraftServer mappedServer = new org.mtr.mapping.holder.MinecraftServer(server);
		return (OwnerState) PersistenceStateExtension.register(mappedServer.getOverworld(), OwnerState::new, STATE_ID);
	}

	public static String getStationOwner(MinecraftServer server, long id) {
		return getState(server).getStationOwner(id);
	}

	public static String getRouteOwner(MinecraftServer server, long id) {
		return getState(server).getRouteOwner(id);
	}

	public static String getStationName(MinecraftServer server, long id) {
		return getState(server).getStationName(id);
	}

	public static String getRouteName(MinecraftServer server, long id) {
		return getState(server).getRouteName(id);
	}

	public static void setOwner(MinecraftServer server, ServerPlayerEntity player, byte type, long id, String team, String name) {
		if (!player.hasPermissionLevel(2)) {
			player.sendMessage(Text.translatable("message.mtrdriver.owner.no_permission"), false);
			sendSnapshot(server, player);
			return;
		}

		final String safeTeam = team == null ? "" : team.trim();
		final String safeName = name == null || name.trim().isEmpty() ? "#" + id : name.trim();
		if (!safeTeam.isEmpty() && server.getScoreboard().getTeam(safeTeam) == null) {
			player.sendMessage(Text.translatable("message.mtrdriver.owner.team_not_found", safeTeam), false);
			sendSnapshot(server, player);
			return;
		}

		final OwnerState state = getState(server);
		if (type == OwnerChannels.TYPE_STATION) {
			state.setStationOwner(id, safeTeam, safeName);
			player.sendMessage(Text.translatable(safeTeam.isEmpty() ? "message.mtrdriver.owner.station.cleared" : "message.mtrdriver.owner.station.updated", safeName, safeTeam), false);
		} else if (type == OwnerChannels.TYPE_ROUTE) {
			state.setRouteOwner(id, safeTeam, safeName);
			player.sendMessage(Text.translatable(safeTeam.isEmpty() ? "message.mtrdriver.owner.route.cleared" : "message.mtrdriver.owner.route.updated", safeName, safeTeam), false);
		} else {
			sendSnapshot(server, player);
			return;
		}

		broadcast(server);
	}

	public static void sendSnapshot(MinecraftServer server, ServerPlayerEntity player) {
		if (ServerPlayNetworking.canSend(player, OwnerChannels.SYNC)) {
			ServerPlayNetworking.send(player, OwnerChannels.SYNC, createSnapshot(server));
		}
	}

	public static void broadcast(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (ServerPlayNetworking.canSend(player, OwnerChannels.SYNC)) {
				ServerPlayNetworking.send(player, OwnerChannels.SYNC, createSnapshot(server));
			}
		}
	}

	private static PacketByteBuf createSnapshot(MinecraftServer server) {
		final OwnerState state = getState(server);
		final Map<Long, String> stations = state.getStationOwners();
		final Map<Long, String> routes = state.getRouteOwners();
		final PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(stations.size() + routes.size());
		stations.forEach((id, team) -> {
			buf.writeByte(OwnerChannels.TYPE_STATION);
			buf.writeLong(id);
			buf.writeString(team);
		});
		routes.forEach((id, team) -> {
			buf.writeByte(OwnerChannels.TYPE_ROUTE);
			buf.writeLong(id);
			buf.writeString(team);
		});
		return buf;
	}
}
