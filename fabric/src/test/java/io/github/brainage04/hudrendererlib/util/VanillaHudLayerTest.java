package io.github.brainage04.hudrendererlib.util;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaHudLayerTest {
	@Test
	void resolvesFabricAndNeoForgeIdsToTheSameLayer() {
		assertEquals(VanillaHudLayer.SCOREBOARD, VanillaHudLayer.fromId(Identifier.withDefaultNamespace("scoreboard")));
		assertEquals(VanillaHudLayer.SCOREBOARD, VanillaHudLayer.fromId(Identifier.withDefaultNamespace("scoreboard_sidebar")));
		assertEquals(VanillaHudLayer.STATUS_EFFECTS, VanillaHudLayer.fromId(Identifier.withDefaultNamespace("mob_effects")));
		assertEquals(VanillaHudLayer.STATUS_EFFECTS, VanillaHudLayer.fromId(Identifier.withDefaultNamespace("effects")));
		assertEquals(VanillaHudLayer.PLAYER_LIST, VanillaHudLayer.fromId(Identifier.withDefaultNamespace("tab_list")));
	}

	@Test
	void rejectsUnknownAndNonVanillaIdsNamingTheId() {
		IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class,
				() -> new LayerInfo(Identifier.withDefaultNamespace("scoreboard_side"), true));
		assertTrue(unknown.getMessage().contains("minecraft:scoreboard_side"), unknown.getMessage());

		assertThrows(IllegalArgumentException.class,
				() -> VanillaHudLayer.fromId(Identifier.fromNamespaceAndPath("othermod", "chat")));
	}
}
