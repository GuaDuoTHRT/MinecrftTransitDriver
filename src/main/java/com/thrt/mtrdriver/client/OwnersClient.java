package com.thrt.mtrdriver.client;

import com.thrt.mtrdriver.owner.OwnerChannels;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.HashMap;
import java.util.Map;

public final class OwnersClient {
	private OwnersClient() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(OwnerChannels.SYNC, (client, handler, buf, responseSender) -> {
			final int count = buf.readInt();
			final Map<Long, String> stations = new HashMap<>();
			final Map<Long, String> routes = new HashMap<>();
			for (int i = 0; i < count; i++) {
				final byte type = buf.readByte();
				final long id = buf.readLong();
				final String team = buf.readString(32767);
				if (type == OwnerChannels.TYPE_STATION) {
					stations.put(id, team);
				} else if (type == OwnerChannels.TYPE_ROUTE) {
					routes.put(id, team);
				}
			}
			client.execute(() -> OwnerClientCache.setAll(stations, routes));
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			try {
				if (ClientPlayNetworking.canSend(OwnerChannels.REQUEST)) {
					ClientPlayNetworking.send(OwnerChannels.REQUEST, PacketByteBufs.empty());
				}
			} catch (Exception ignored) {
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> OwnerClientCache.clear());
	}
}
