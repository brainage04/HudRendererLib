package io.github.brainage04.hudrendererlib.util;

import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The vanilla HUD layers an element can be anchored to, named the same on every loader. Fabric and
 * NeoForge give most of these layers different identifiers (Fabric {@code scoreboard} is NeoForge
 * {@code scoreboard_sidebar}, Fabric {@code mob_effects} is NeoForge {@code effects}, ...); each loader
 * module maps these constants to its own layer. {@link #fromId} accepts either loader's identifier.
 *
 * <p>An element anchored to a layer is drawn under the same conditions as that layer on both loaders:
 * hidden with the rest of the HUD by F1 (except {@link #SLEEP} and {@link #SUBTITLES}, which the game
 * keeps drawing), and only when the game draws the layer itself, e.g. {@link #HEALTH_BAR} only in
 * survival/adventure and {@link #HOTBAR} only outside spectator mode.
 */
public enum VanillaHudLayer {
	CAMERA_OVERLAYS("misc_overlays", "camera_overlays"),
	CROSSHAIR("crosshair"),
	/** The spectator mode hotbar; drawn instead of {@link #HOTBAR} in spectator mode. */
	SPECTATOR_MENU("spectator_menu"),
	HOTBAR("hotbar"),
	ARMOR_BAR("armor_bar", "armor_level"),
	HEALTH_BAR("health_bar", "player_health"),
	FOOD_BAR("food_bar", "food_level"),
	AIR_BAR("air_bar", "air_level"),
	MOUNT_HEALTH("mount_health", "vehicle_health"),
	/** The contextual info bar background (experience, locator or jump bar). */
	INFO_BAR("info_bar", "contextual_info_bar_background"),
	EXPERIENCE_LEVEL("experience_level"),
	HELD_ITEM_TOOLTIP("held_item_tooltip", "selected_item_name"),
	SPECTATOR_TOOLTIP("spectator_tooltip"),
	STATUS_EFFECTS("mob_effects", "effects", "status_effects"),
	BOSS_BAR("boss_bar", "boss_overlay"),
	SLEEP("sleep", "sleep_overlay"),
	DEMO_TIMER("demo_timer", "demo_overlay"),
	SCOREBOARD("scoreboard", "scoreboard_sidebar"),
	OVERLAY_MESSAGE("overlay_message"),
	TITLE("title_and_subtitle", "title"),
	CHAT("chat"),
	PLAYER_LIST("player_list", "tab_list"),
	SUBTITLES("subtitles", "subtitle_overlay");

	private final List<String> paths;

	VanillaHudLayer(String... paths) {
		this.paths = List.of(paths);
	}

	/**
	 * The layer with the Fabric or NeoForge identifier {@code id} (namespace {@code minecraft}).
	 *
	 * @throws IllegalArgumentException if {@code id} names no vanilla HUD layer
	 */
	public static VanillaHudLayer fromId(Identifier id) {
		if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			for (VanillaHudLayer layer : values()) {
				if (layer.paths.contains(id.getPath())) return layer;
			}
		}
		throw new IllegalArgumentException("%s is not a vanilla HUD layer; use one of %s".formatted(
				id,
				Arrays.stream(values())
						.flatMap(layer -> layer.paths.stream())
						.map(path -> Identifier.DEFAULT_NAMESPACE + ":" + path)
						.collect(Collectors.joining(", "))
		));
	}
}
