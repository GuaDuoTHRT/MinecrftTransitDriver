package com.thrt.mtrdriver.server;

import com.thrt.mtrdriver.owner.OwnerChannels;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class OwnersServer {
	private OwnersServer() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(OwnerChannels.SET, (server, player, handler, buf, responseSender) -> {
			final byte type = buf.readByte();
			final long id = buf.readLong();
			final String team = buf.readString(32767);
			final String name = buf.readString(32767);
			server.execute(() -> OwnerManager.setOwner(server, player, type, id, team, name));
		});

		ServerPlayNetworking.registerGlobalReceiver(OwnerChannels.REQUEST, (server, player, handler, buf, responseSender) -> server.execute(() -> OwnerManager.sendSnapshot(server, player)));
	}
}
