package io.github.brainage04.hudrendererlib.util;

import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * Where an element is drawn: just before ({@code before = true}) or just after the vanilla HUD layer
 * {@code layer}.
 */
public record LayerInfo(VanillaHudLayer layer, boolean before) {
	public LayerInfo {
		Objects.requireNonNull(layer, "layer");
	}

	/**
	 * Anchors to the vanilla layer with the Fabric or NeoForge identifier {@code layer}, e.g.
	 * {@code minecraft:scoreboard} or {@code minecraft:scoreboard_sidebar}; prefer
	 * {@link #LayerInfo(VanillaHudLayer, boolean)}.
	 *
	 * @throws IllegalArgumentException if {@code layer} names no vanilla HUD layer
	 */
	public LayerInfo(Identifier layer, boolean before) {
		this(VanillaHudLayer.fromId(layer), before);
	}
}
