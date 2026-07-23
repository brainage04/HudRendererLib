package io.github.brainage04.hudrendererlib.platform;

import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

/** Loader-specific registrations required by HudRendererLib's shared client implementation. */
public interface HudRendererPlatform {
	void registerClientCommand(String literal, Consumer<Minecraft> action);

	void registerKeyCategory(KeyMapping.Category category);

	void registerKeyMapping(KeyMapping keyMapping);

	void registerEndClientTick(Consumer<Minecraft> listener);

	void registerHudElement(
			Identifier anchor,
			boolean before,
			Identifier id,
			CoreHudElement<?> element
	);
}
