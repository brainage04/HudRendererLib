package io.github.brainage04.hudrendererlib.fabric;

import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.platform.HudRendererPlatform;
import io.github.brainage04.hudrendererlib.util.VanillaHudLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public final class FabricHudRendererPlatform implements HudRendererPlatform {
	@Override
	public void registerClientCommand(String literal, Consumer<Minecraft> action) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal(literal).executes(context -> {
					Minecraft client = context.getSource().getClient();
					client.schedule(() -> action.accept(client));
					return 1;
				}))
		);
	}

	@Override
	public void registerKeyCategory(KeyMapping.Category category) {
		// Fabric registers custom categories through KeyMapping.Category.register.
	}

	@Override
	public void registerKeyMapping(KeyMapping keyMapping) {
		KeyMappingHelper.registerKeyMapping(keyMapping);
	}

	@Override
	public void registerEndClientTick(Consumer<Minecraft> listener) {
		ClientTickEvents.END_CLIENT_TICK.register(listener::accept);
	}

	@Override
	public void registerHudElement(
			VanillaHudLayer anchor,
			boolean before,
			Identifier id,
			CoreHudElement<?> element
	) {
		// Attached elements are drawn where the game draws the anchor, so they share its conditions.
		HudElement hudElement = element::extractRenderState;
		if (before) {
			HudElementRegistry.attachElementBefore(layerId(anchor), id, hudElement);
		} else {
			HudElementRegistry.attachElementAfter(layerId(anchor), id, hudElement);
		}
	}

	private static Identifier layerId(VanillaHudLayer layer) {
		return switch (layer) {
			case CAMERA_OVERLAYS -> VanillaHudElements.MISC_OVERLAYS;
			case CROSSHAIR -> VanillaHudElements.CROSSHAIR;
			case SPECTATOR_MENU -> VanillaHudElements.SPECTATOR_MENU;
			case HOTBAR -> VanillaHudElements.HOTBAR;
			case ARMOR_BAR -> VanillaHudElements.ARMOR_BAR;
			case HEALTH_BAR -> VanillaHudElements.HEALTH_BAR;
			case FOOD_BAR -> VanillaHudElements.FOOD_BAR;
			case AIR_BAR -> VanillaHudElements.AIR_BAR;
			case MOUNT_HEALTH -> VanillaHudElements.MOUNT_HEALTH;
			case INFO_BAR -> VanillaHudElements.INFO_BAR;
			case EXPERIENCE_LEVEL -> VanillaHudElements.EXPERIENCE_LEVEL;
			case HELD_ITEM_TOOLTIP -> VanillaHudElements.HELD_ITEM_TOOLTIP;
			case SPECTATOR_TOOLTIP -> VanillaHudElements.SPECTATOR_TOOLTIP;
			case STATUS_EFFECTS -> VanillaHudElements.MOB_EFFECTS;
			case BOSS_BAR -> VanillaHudElements.BOSS_BAR;
			case SLEEP -> VanillaHudElements.SLEEP;
			case DEMO_TIMER -> VanillaHudElements.DEMO_TIMER;
			case SCOREBOARD -> VanillaHudElements.SCOREBOARD;
			case OVERLAY_MESSAGE -> VanillaHudElements.OVERLAY_MESSAGE;
			case TITLE -> VanillaHudElements.TITLE_AND_SUBTITLE;
			case CHAT -> VanillaHudElements.CHAT;
			case PLAYER_LIST -> VanillaHudElements.PLAYER_LIST;
			case SUBTITLES -> VanillaHudElements.SUBTITLES;
		};
	}
}
