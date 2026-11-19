package com.thrt.mtrdriver.mixin.client;

import com.thrt.mtrdriver.client.OwnerClientCache;
import com.thrt.mtrdriver.client.OwnerSelectionScreen;
import com.thrt.mtrdriver.owner.OwnerChannels;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.mtr.core.data.NameColorDataBase;
import org.mtr.mapping.holder.ButtonWidget;
import org.mtr.mapping.holder.ClickableWidget;
import org.mtr.mapping.holder.Screen;
import org.mtr.mapping.mapper.ButtonWidgetExtension;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mod.screen.EditNameColorScreenBase;
import org.mtr.mod.screen.EditRouteScreen;
import org.mtr.mod.screen.EditStationScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditNameColorScreenBase.class)
public class EditNameColorScreenBaseMixin {
	@Unique
	private ButtonWidgetExtension mtd$ownerButton;

	@Inject(method = "setPositionsAndInit", at = @At("TAIL"), remap = false)
	private void mtd$addOwnerButton(int nameStart, int colorStart, int colorEnd, CallbackInfo ci) {
		if (mtd$getType() < 0) {
			return;
		}
		final ScreenExtension screen = (ScreenExtension) (Object) this;
		final int x = Math.max(2, screen.getWidthMapped() - 110);
		mtd$ownerButton = new ButtonWidgetExtension(x, 2, 108, 20, TextHelper.literal(""), this::mtd$openOwnerScreen);
		screen.addChild(new ClickableWidget(mtd$ownerButton));
		mtd$updateOwnerButton();
	}

	@Inject(method = "tick2", at = @At("TAIL"), remap = false)
	private void mtd$tickOwnerButton(CallbackInfo ci) {
		mtd$updateOwnerButton();
	}

	@Unique
	private void mtd$updateOwnerButton() {
		if (mtd$ownerButton == null) {
			return;
		}
		final byte type = mtd$getType();
		final NameColorDataBase data = ((EditNameColorScreenAccessor) (Object) this).mtd$getData();
		if (type < 0 || data == null) {
			return;
		}
		final String team = OwnerClientCache.get(type, data.getId());
		final String owner = team == null || team.isEmpty() ? TextHelper.translatable("gui.mtrdriver.owner_none").getString() : team;
		mtd$ownerButton.setMessage2(org.mtr.mapping.holder.Text.cast(TextHelper.translatable("gui.mtrdriver.owner_button", owner)));
	}

	@Unique
	private void mtd$openOwnerScreen(ButtonWidget button) {
		final MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && !client.player.hasPermissionLevel(2)) {
			client.player.sendMessage(net.minecraft.text.Text.translatable("message.mtrdriver.owner.no_permission"), true);
			return;
		}
		try {
			if (!ClientPlayNetworking.canSend(OwnerChannels.SET)) {
				if (client.player != null) {
					client.player.sendMessage(net.minecraft.text.Text.translatable("message.mtrdriver.owner.server_missing"), true);
				}
				return;
			}
		} catch (Exception ignored) {
			return;
		}

		final EditNameColorScreenAccessor accessor = (EditNameColorScreenAccessor) (Object) this;
		accessor.mtd$saveData();
		final NameColorDataBase data = accessor.mtd$getData();
		if (data == null) {
			return;
		}
		((ScreenExtension) (Object) this).getClientMapped().openScreen(new Screen(new OwnerSelectionScreen(data.getId(), mtd$getType(), data.getName(), (ScreenExtension) (Object) this)));
	}

	@Unique
	private byte mtd$getType() {
		final Object self = this;
		if (self instanceof EditStationScreen) {
			return OwnerChannels.TYPE_STATION;
		}
		if (self instanceof EditRouteScreen) {
			return OwnerChannels.TYPE_ROUTE;
		}
		return -1;
	}
}
