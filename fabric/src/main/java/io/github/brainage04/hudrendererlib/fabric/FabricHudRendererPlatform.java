package io.github.brainage04.hudrendererlib.fabric;

import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.platform.HudRendererPlatform;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
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
			Identifier anchor,
			boolean before,
			Identifier id,
			CoreHudElement<?> element
	) {
		HudElement hudElement = element::extractRenderState;
		if (before) {
			HudElementRegistry.attachElementBefore(anchor, id, hudElement);
		} else {
			HudElementRegistry.attachElementAfter(anchor, id, hudElement);
		}
	}
}
