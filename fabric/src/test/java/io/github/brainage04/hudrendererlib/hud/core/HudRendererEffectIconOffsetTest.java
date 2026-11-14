package io.github.brainage04.hudrendererlib.hud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HudRendererEffectIconOffsetTest {
    @Test
    void demoModeMovesBothIconRowsLower() {
        // No GameTest can start in demo mode, where the game draws both rows 15 px lower.
        assertEquals(0, HudRenderer.getEffectIconOffset(21, false, false, true));
        assertEquals(21, HudRenderer.getEffectIconOffset(21, true, false, false));
        assertEquals(21 + 15, HudRenderer.getEffectIconOffset(21, true, false, true));
        assertEquals(21 + 26, HudRenderer.getEffectIconOffset(21, true, true, false));
        assertEquals(21 + 26 + 15, HudRenderer.getEffectIconOffset(21, true, true, true));
    }
}
