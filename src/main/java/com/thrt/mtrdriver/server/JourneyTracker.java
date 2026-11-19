package com.thrt.mtrdriver.server;

import com.thrt.mtrdriver.owner.OwnerChannels;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.mtr.mapping.holder.PlayerEntity;
import org.mtr.mapping.holder.Team;
import org.mtr.mapping.holder.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class JourneyTracker {
	private static final Map<UUID, LinkedHashSet<Long>> ROUTES = new HashMap<>();
	private static final Map<UUID, Integer> BALANCE_BEFORE = new HashMap<>();

	private JourneyTracker() {
	}

	public static void addRoute(UUID uuid, long routeId) {
		if (uuid == null || routeId == 0) {
			return;
		}
		final LinkedHashSet<Long> routes = ROUTES.computeIfAbsent(uuid, ignored -> new LinkedHashSet<>());
		routes.remove(routeId);
		routes.add(routeId);
	}

	public static void setBalanceBefore(UUID uuid, int balance) {
		if (uuid != null) {
			BALANCE_BEFORE.put(uuid, balance);
		}
	}

	public static int getBalanceBefore(UUID uuid) {
		return BALANCE_BEFORE.getOrDefault(uuid, 0);
	}

	public static void clear(UUID uuid) {
		if (uuid != null) {
			ROUTES.remove(uuid);
			BALANCE_BEFORE.remove(uuid);
		}
	}

	public static void settle(World world, PlayerEntity rider, int fare) {
		final UUID uuid = rider.getUuid();
		final List<Long> routes = new ArrayList<>(ROUTES.getOrDefault(uuid, new LinkedHashSet<>()));
		clear(uuid);
		if (world == null || world.isClient() || fare <= 0 || routes.isEmpty()) {
			return;
		}

		final org.mtr.mapping.holder.MinecraftServer mappedServer = world.getServer();
		if (mappedServer == null) {
			return;
		}
		final net.minecraft.server.MinecraftServer server = mappedServer.data;
		long ownerRouteId = 0;
		String ownerTeam = "";
		for (long routeId : routes) {
			final String team = OwnerManager.getRouteOwner(server, routeId);
			if (team != null && !team.isEmpty()) {
				ownerTeam = team;
				ownerRouteId = routeId;
			}
		}

		if (ownerTeam.isEmpty()) {
			return;
		}

		final Team team = world.getScoreboard().getTeam(ownerTeam);
		if (team == null) {
			return;
		}

		final String riderName = rider.getGameProfile().getName();
		String routeName = OwnerManager.getRouteName(server, ownerRouteId);
		if (routeName == null || routeName.isEmpty()) {
			routeName = Long.toString(ownerRouteId);
		}

		for (String member : team.getPlayerList()) {
			BalanceHelper.addBalance(world, member, fare);
		}

		for (String member : team.getPlayerList()) {
			final net.minecraft.server.network.ServerPlayerEntity onlinePlayer = server.getPlayerManager().getPlayer(member);
			if (onlinePlayer != null && ServerPlayNetworking.canSend(onlinePlayer, OwnerChannels.SYNC)) {
				onlinePlayer.sendMessage(net.minecraft.text.Text.translatable("message.mtrdriver.team.balance", ownerTeam, fare, riderName, routeName), false);
			}
		}

		if (!team.getPlayerList().contains(riderName)) {
			final net.minecraft.server.network.ServerPlayerEntity onlineRider = server.getPlayerManager().getPlayer(uuid);
			if (onlineRider != null && ServerPlayNetworking.canSend(onlineRider, OwnerChannels.SYNC)) {
				onlineRider.sendMessage(net.minecraft.text.Text.translatable("message.mtrdriver.rider.balance", fare, routeName, ownerTeam), false);
			}
		}
	}
}
