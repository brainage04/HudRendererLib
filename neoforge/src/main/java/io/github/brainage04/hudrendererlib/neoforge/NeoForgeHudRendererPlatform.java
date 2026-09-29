package io.github.brainage04.hudrendererlib.neoforge;

import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.platform.HudRendererPlatform;
import io.github.brainage04.hudrendererlib.util.VanillaHudLayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NeoForgeHudRendererPlatform implements HudRendererPlatform {
	private final List<ClientCommandRegistration> clientCommands = new ArrayList<>();
	private final List<KeyMapping.Category> keyCategories = new ArrayList<>();
	private final List<KeyMapping> keyMappings = new ArrayList<>();
	private final List<Consumer<Minecraft>> endClientTickListeners = new ArrayList<>();
	private final List<HudElementRegistration> hudElements = new ArrayList<>();

	public NeoForgeHudRendererPlatform(IEventBus modBus) {
		modBus.addListener(this::onRegisterKeyMappings);
		modBus.addListener(this::onRegisterGuiLayers);
		NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
		NeoForge.EVENT_BUS.addListener(this::onEndClientTick);
	}

	@Override
	public void registerClientCommand(String literal, Consumer<Minecraft> action) {
		clientCommands.add(new ClientCommandRegistration(literal, action));
	}

	@Override
	public void registerKeyCategory(KeyMapping.Category category) {
		keyCategories.add(category);
	}

	@Override
	public void registerKeyMapping(KeyMapping keyMapping) {
		keyMappings.add(keyMapping);
	}

	@Override
	public void registerEndClientTick(Consumer<Minecraft> listener) {
		endClientTickListeners.add(listener);
	}

	@Override
	public void registerHudElement(
			VanillaHudLayer anchor,
			boolean before,
			Identifier id,
			CoreHudElement<?> element
	) {
		hudElements.add(new HudElementRegistration(anchor, before, id, element));
	}

	private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		keyCategories.forEach(event::registerCategory);
		keyMappings.forEach(event::register);
	}

	private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		for (HudElementRegistration registration : hudElements) {
			VanillaHudLayer anchor = registration.anchor();
			CoreHudElement<?> element = registration.element();
			// Unlike Fabric's attached elements, NeoForge layers do not inherit their neighbour's
			// condition, so apply the anchor's own condition here.
			GuiLayer layer = (graphics, deltaTracker) -> {
				if (isDrawn(anchor, Minecraft.getInstance())) element.extractRenderState(graphics, deltaTracker);
			};
			if (registration.before()) {
				event.registerBelow(layerId(anchor), registration.id(), layer);
			} else {
				event.registerAbove(layerId(anchor), registration.id(), layer);
			}
		}
	}

	private static Identifier layerId(VanillaHudLayer layer) {
		return switch (layer) {
			case CAMERA_OVERLAYS -> VanillaGuiLayers.CAMERA_OVERLAYS;
			case CROSSHAIR -> VanillaGuiLayers.CROSSHAIR;
			// NeoForge draws the spectator menu in its hotbar layer.
			case SPECTATOR_MENU, HOTBAR -> VanillaGuiLayers.HOTBAR;
			case ARMOR_BAR -> VanillaGuiLayers.ARMOR_LEVEL;
			case HEALTH_BAR -> VanillaGuiLayers.PLAYER_HEALTH;
			case FOOD_BAR -> VanillaGuiLayers.FOOD_LEVEL;
			case AIR_BAR -> VanillaGuiLayers.AIR_LEVEL;
			case MOUNT_HEALTH -> VanillaGuiLayers.VEHICLE_HEALTH;
			case INFO_BAR -> VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND;
			case EXPERIENCE_LEVEL -> VanillaGuiLayers.EXPERIENCE_LEVEL;
			case HELD_ITEM_TOOLTIP -> VanillaGuiLayers.SELECTED_ITEM_NAME;
			case SPECTATOR_TOOLTIP -> VanillaGuiLayers.SPECTATOR_TOOLTIP;
			case STATUS_EFFECTS -> VanillaGuiLayers.EFFECTS;
			case BOSS_BAR -> VanillaGuiLayers.BOSS_OVERLAY;
			case SLEEP -> VanillaGuiLayers.SLEEP_OVERLAY;
			case DEMO_TIMER -> VanillaGuiLayers.DEMO_OVERLAY;
			case SCOREBOARD -> VanillaGuiLayers.SCOREBOARD_SIDEBAR;
			case OVERLAY_MESSAGE -> VanillaGuiLayers.OVERLAY_MESSAGE;
			case TITLE -> VanillaGuiLayers.TITLE;
			case CHAT -> VanillaGuiLayers.CHAT;
			case PLAYER_LIST -> VanillaGuiLayers.TAB_LIST;
			case SUBTITLES -> VanillaGuiLayers.SUBTITLE_OVERLAY;
		};
	}

	/**
	 * Whether the game draws {@code layer} this frame: the guards around each call in vanilla
	 * {@code Hud.extractRenderState}/{@code extractHotbarAndDecorations}/{@code extractPlayerHealth},
	 * which is where Fabric draws the elements attached to it.
	 */
	private static boolean isDrawn(VanillaHudLayer layer, Minecraft minecraft) {
		boolean hudHidden = minecraft.gui.hud.isHidden();
		if (layer == VanillaHudLayer.SLEEP) return true;
		if (layer == VanillaHudLayer.SUBTITLES) {
			Screen screen = minecraft.gui.screen();
			return !hudHidden || screen != null && screen.isInGameUi();
		}
		if (hudHidden) return false;

		MultiPlayerGameMode gameMode = minecraft.gameMode;
		boolean spectatorMode = gameMode != null && gameMode.getPlayerMode() == GameType.SPECTATOR;
		return switch (layer) {
			case SPECTATOR_MENU -> spectatorMode;
			case HOTBAR, HELD_ITEM_TOOLTIP -> !spectatorMode;
			case SPECTATOR_TOOLTIP -> spectatorMode && minecraft.player != null && minecraft.player.isSpectator();
			case ARMOR_BAR, HEALTH_BAR, AIR_BAR -> playerHealthDrawn(minecraft);
			case FOOD_BAR -> playerHealthDrawn(minecraft) && vehicleMaxHearts(minecraft) == 0;
			case EXPERIENCE_LEVEL -> gameMode != null && gameMode.hasExperience()
					&& minecraft.player != null && minecraft.player.experienceLevel > 0;
			default -> true;
		};
	}

	private static boolean playerHealthDrawn(Minecraft minecraft) {
		return minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer()
				&& minecraft.getCameraEntity() instanceof Player;
	}

	private static int vehicleMaxHearts(Minecraft minecraft) {
		if (!(minecraft.getCameraEntity() instanceof Player player)) return 0;
		if (!(player.getVehicle() instanceof LivingEntity vehicle) || !vehicle.showVehicleHealth()) return 0;
		return Math.min((int) (vehicle.getMaxHealth() + 0.5F) / 2, 30);
	}

	private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		for (ClientCommandRegistration registration : clientCommands) {
			event.getDispatcher().register(Commands.literal(registration.literal()).executes(context -> {
				Minecraft client = Minecraft.getInstance();
				client.schedule(() -> registration.action().accept(client));
				return 1;
			}));
		}
	}

	private void onEndClientTick(ClientTickEvent.Post event) {
		Minecraft client = Minecraft.getInstance();
		for (Consumer<Minecraft> listener : endClientTickListeners) {
			listener.accept(client);
		}
	}

	private record ClientCommandRegistration(String literal, Consumer<Minecraft> action) {
	}

	private record HudElementRegistration(
			VanillaHudLayer anchor,
			boolean before,
			Identifier id,
			CoreHudElement<?> element
	) {
	}
}
