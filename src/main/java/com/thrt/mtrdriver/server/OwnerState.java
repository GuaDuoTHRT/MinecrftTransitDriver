package com.thrt.mtrdriver.server;

import org.mtr.libraries.com.google.gson.JsonElement;
import org.mtr.libraries.com.google.gson.JsonObject;
import org.mtr.libraries.com.google.gson.JsonParser;
import org.mtr.mapping.holder.CompoundTag;
import org.mtr.mapping.mapper.PersistenceStateExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class OwnerState extends PersistenceStateExtension {
	private static final String KEY_STATIONS = "station_owners";
	private static final String KEY_ROUTES = "route_owners";
	private static final String KEY_STATION_NAMES = "station_names";
	private static final String KEY_ROUTE_NAMES = "route_names";
	private final Map<Long, String> stationOwners = new HashMap<>();
	private final Map<Long, String> routeOwners = new HashMap<>();
	private final Map<Long, String> stationNames = new HashMap<>();
	private final Map<Long, String> routeNames = new HashMap<>();

	public OwnerState() {
		super("mtrdriver_owners");
	}

	@Override
	public void readNbt(CompoundTag tag) {
		stationOwners.clear();
		routeOwners.clear();
		stationNames.clear();
		routeNames.clear();
		mtd$readMap(tag.getString(KEY_STATIONS), stationOwners);
		mtd$readMap(tag.getString(KEY_ROUTES), routeOwners);
		mtd$readMap(tag.getString(KEY_STATION_NAMES), stationNames);
		mtd$readMap(tag.getString(KEY_ROUTE_NAMES), routeNames);
	}

	@Override
	public CompoundTag writeNbt2(CompoundTag tag) {
		tag.putString(KEY_STATIONS, mtd$writeMap(stationOwners));
		tag.putString(KEY_ROUTES, mtd$writeMap(routeOwners));
		tag.putString(KEY_STATION_NAMES, mtd$writeMap(stationNames));
		tag.putString(KEY_ROUTE_NAMES, mtd$writeMap(routeNames));
		return tag;
	}

	public String getStationOwner(long id) {
		return stationOwners.getOrDefault(id, "");
	}

	public String getRouteOwner(long id) {
		return routeOwners.getOrDefault(id, "");
	}

	public String getStationName(long id) {
		return stationNames.getOrDefault(id, "");
	}

	public String getRouteName(long id) {
		return routeNames.getOrDefault(id, "");
	}

	public void setStationOwner(long id, String team, String name) {
		mtd$set(stationOwners, stationNames, id, team, name);
	}

	public void setRouteOwner(long id, String team, String name) {
		mtd$set(routeOwners, routeNames, id, team, name);
	}

	public Map<Long, String> getStationOwners() {
		return Collections.unmodifiableMap(stationOwners);
	}

	public Map<Long, String> getRouteOwners() {
		return Collections.unmodifiableMap(routeOwners);
	}

	private void mtd$set(Map<Long, String> owners, Map<Long, String> names, long id, String team, String name) {
		if (team == null || team.isEmpty()) {
			owners.remove(id);
			names.remove(id);
		} else {
			owners.put(id, team);
			if (name == null || name.isEmpty()) {
				names.remove(id);
			} else {
				names.put(id, name);
			}
		}
		markDirty2();
	}

	private static void mtd$readMap(String json, Map<Long, String> target) {
		if (json == null || json.isEmpty()) {
			return;
		}
		try {
			final JsonObject object = JsonParser.parseString(json).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				target.put(Long.parseLong(entry.getKey()), entry.getValue().getAsString());
			}
		} catch (Exception ignored) {
		}
	}

	private static String mtd$writeMap(Map<Long, String> source) {
		final JsonObject object = new JsonObject();
		source.forEach((id, value) -> object.addProperty(Long.toString(id), value));
		return object.toString();
	}
}
