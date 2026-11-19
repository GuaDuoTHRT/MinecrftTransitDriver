package com.thrt.mtrdriver.owner;

import net.minecraft.util.Identifier;

public final class OwnerChannels {
	public static final Identifier SYNC = new Identifier("mtrdriver", "owners_sync");
	public static final Identifier SET = new Identifier("mtrdriver", "set_owner");
	public static final Identifier REQUEST = new Identifier("mtrdriver", "request_owners");
	public static final byte TYPE_STATION = 0;
	public static final byte TYPE_ROUTE = 1;

	private OwnerChannels() {
	}
}
