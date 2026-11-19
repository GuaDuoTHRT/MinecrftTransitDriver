package com.thrt.mtrdriver.client;

import com.thrt.mtrdriver.owner.OwnerChannels;

import java.util.HashMap;
import java.util.Map;

public final class OwnerClientCache {
	private static final Map<Long, String> STATIONS = new HashMap<>();
	private static final Map<Long, String> ROUTES = new HashMap<>();

	private OwnerClientCache() {
	}

	public static void setAll(Map<Long, String> stations, Map<Long, String> routes) {
		STATIONS.clear();
		STATIONS.putAll(stations);
		ROUTES.clear();
		ROUTES.putAll(routes);
	}

	public static String get(byte type, long id) {
		return mtd$map(type).getOrDefault(id, "");
	}

	public static void set(byte type, long id, String team) {
		if (team == null || team.isEmpty()) {
			mtd$map(type).remove(id);
		} else {
			mtd$map(type).put(id, team);
		}
	}

	public static void clear() {
		STATIONS.clear();
		ROUTES.clear();
	}

	private static Map<Long, String> mtd$map(byte type) {
		return type == OwnerChannels.TYPE_STATION ? STATIONS : ROUTES;
	}
}
