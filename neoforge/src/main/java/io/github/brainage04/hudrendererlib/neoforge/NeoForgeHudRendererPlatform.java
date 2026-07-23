package io.github.brainage04.hudrendererlib.neoforge;

import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.platform.HudRendererPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
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
			Identifier anchor,
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
			if (registration.before()) {
				event.registerBelow(
						registration.anchor(),
						registration.id(),
						registration.element()::extractRenderState
				);
			} else {
				event.registerAbove(
						registration.anchor(),
						registration.id(),
						registration.element()::extractRenderState
				);
			}
		}
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
			Identifier anchor,
			boolean before,
			Identifier id,
			CoreHudElement<?> element
	) {
	}
}
