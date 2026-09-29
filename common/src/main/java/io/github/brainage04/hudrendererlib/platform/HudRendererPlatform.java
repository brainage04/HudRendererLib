package io.github.brainage04.hudrendererlib.platform;

import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.util.VanillaHudLayer;
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

	/**
	 * Draws {@code element} just before or after {@code anchor}, under the same conditions the game
	 * draws {@code anchor} itself (F1, game mode, ...).
	 */
	void registerHudElement(
			VanillaHudLayer anchor,
			boolean before,
			Identifier id,
			CoreHudElement<?> element
	);
}
