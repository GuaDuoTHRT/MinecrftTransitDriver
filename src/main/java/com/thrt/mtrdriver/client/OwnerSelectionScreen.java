package com.thrt.mtrdriver.client;

import com.thrt.mtrdriver.owner.OwnerChannels;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.mtr.mapping.holder.ClickableWidget;
import org.mtr.mapping.mapper.ButtonWidgetExtension;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mod.screen.MTRScreenBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OwnerSelectionScreen extends MTRScreenBase {
	private static final int PER_PAGE = 15;
	private static final int COLUMNS = 3;
	private final long dataId;
	private final byte type;
	private final String dataName;
	private int page;
	private int totalPages = 1;

	public OwnerSelectionScreen(long dataId, byte type, String dataName, ScreenExtension previousScreen) {
		super(previousScreen);
		this.dataId = dataId;
		this.type = type;
		this.dataName = dataName == null ? "" : dataName;
	}

	@Override
	protected void init2() {
		super.init2();
		final int width = getWidthMapped();
		final int height = getHeightMapped();
		final List<String> teams = new ArrayList<>();
		final MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null) {
			teams.addAll(client.world.getScoreboard().getTeamNames());
		}
		Collections.sort(teams);
		totalPages = Math.max(1, (teams.size() + PER_PAGE - 1) / PER_PAGE);
		page = Math.max(0, Math.min(page, totalPages - 1));

		mtd$addButton(TextHelper.translatable("gui.mtrdriver.owner_none_option").getString(), 20, 30, width - 40, 20, "");
		final int gap = 4;
		final int buttonWidth = Math.max(40, (width - 40 - gap * (COLUMNS - 1)) / COLUMNS);
		final int startIndex = page * PER_PAGE;
		final int endIndex = Math.min(teams.size(), startIndex + PER_PAGE);
		for (int i = startIndex; i < endIndex; i++) {
			final int index = i - startIndex;
			final int column = index % COLUMNS;
			final int row = index / COLUMNS;
			final int x = 20 + column * (buttonWidth + gap);
			final int y = 58 + row * 24;
			final String team = teams.get(i);
			mtd$addButton(team, x, y, buttonWidth, 20, team);
		}

		final int bottomY = height - 30;
		if (totalPages > 1) {
			mtd$addActionButton(TextHelper.translatable("gui.mtrdriver.owner_prev").getString(), 20, bottomY, 80, 20, () -> mtd$changePage(-1));
			mtd$addActionButton(TextHelper.translatable("gui.mtrdriver.owner_next").getString(), 104, bottomY, 80, 20, () -> mtd$changePage(1));
		}
		mtd$addActionButton(TextHelper.translatable("gui.mtrdriver.owner_cancel").getString(), width - 100, bottomY, 80, 20, this::onClose2);
	}

	private void mtd$addButton(String label, int x, int y, int width, int height, String value) {
		addChild(new ClickableWidget(new ButtonWidgetExtension(x, y, width, height, TextHelper.literal(label), button -> mtd$select(value))));
	}

	private void mtd$addActionButton(String label, int x, int y, int width, int height, Runnable action) {
		addChild(new ClickableWidget(new ButtonWidgetExtension(x, y, width, height, TextHelper.literal(label), button -> action.run())));
	}

	private void mtd$changePage(int delta) {
		page = Math.max(0, Math.min(totalPages - 1, page + delta));
		clearChildren();
		init2();
	}

	private void mtd$select(String team) {
		try {
			if (!ClientPlayNetworking.canSend(OwnerChannels.SET)) {
				onClose2();
				return;
			}
		} catch (Exception ignored) {
			onClose2();
			return;
		}
		OwnerClientCache.set(type, dataId, team);
		final PacketByteBuf buf = PacketByteBufs.create();
		buf.writeByte(type);
		buf.writeLong(dataId);
		buf.writeString(team);
		buf.writeString(dataName);
		ClientPlayNetworking.send(OwnerChannels.SET, buf);
		onClose2();
	}
}
