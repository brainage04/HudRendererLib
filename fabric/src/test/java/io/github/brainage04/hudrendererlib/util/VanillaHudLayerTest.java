package io.github.brainage04.hudrendererlib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class VanillaHudLayerTest {
    @Test
    void resolvesFabricAndNeoForgeIdsToTheSameLayer() {
        assertEquals(
                VanillaHudLayer.SCOREBOARD,
                VanillaHudLayer.fromId(Identifier.withDefaultNamespace("scoreboard")));
        assertEquals(
                VanillaHudLayer.SCOREBOARD,
                VanillaHudLayer.fromId(Identifier.withDefaultNamespace("scoreboard_sidebar")));
        assertEquals(
                VanillaHudLayer.STATUS_EFFECTS,
                VanillaHudLayer.fromId(Identifier.withDefaultNamespace("mob_effects")));
        assertEquals(
                VanillaHudLayer.STATUS_EFFECTS,
                VanillaHudLayer.fromId(Identifier.withDefaultNamespace("effects")));
        assertEquals(
                VanillaHudLayer.PLAYER_LIST,
                VanillaHudLayer.fromId(Identifier.withDefaultNamespace("tab_list")));
        assertEquals(
                VanillaHudLayer.INFO_BAR_FOREGROUND,
                VanillaHudLayer.fromId(Identifier.withDefaultNamespace("info_bar_foreground")));
        assertEquals(
                VanillaHudLayer.INFO_BAR_FOREGROUND,
                VanillaHudLayer.fromId(Identifier.withDefaultNamespace("contextual_info_bar")));
    }

    @Test
    void rejectsUnknownAndNonVanillaIdsNamingTheId() {
        IllegalArgumentException unknown =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new LayerInfo(
                                        Identifier.withDefaultNamespace("scoreboard_side"), true));
        assertTrue(
                unknown.getMessage().contains("minecraft:scoreboard_side"), unknown.getMessage());

        assertThrows(
                IllegalArgumentException.class,
                () -> VanillaHudLayer.fromId(Identifier.fromNamespaceAndPath("othermod", "chat")));
    }
}
