package io.github.brainage04.hudrendererlib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.brainage04.hudrendererlib.config.overrides.core.IntegerOverride;
import org.junit.jupiter.api.Test;

class HudRendererLibMaxWidthTest {
    private static final int DEFAULT = 300;

    @Test
    void theElementsDefaultAppliesWhileNeitherOverrideNorGlobalIsSet() {
        assertEquals(
                DEFAULT, HudRendererLib.getMaxWidth(new IntegerOverride(false, 0), 0, DEFAULT));
        assertEquals(0, HudRendererLib.getMaxWidth(new IntegerOverride(false, 0), 0, 0));
    }

    @Test
    void theGlobalMaxElementWidthReplacesTheDefault() {
        assertEquals(450, HudRendererLib.getMaxWidth(new IntegerOverride(false, 0), 450, DEFAULT));
        assertEquals(120, HudRendererLib.getMaxWidth(new IntegerOverride(false, 0), 120, DEFAULT));
    }

    @Test
    void anEnabledOverrideWinsAndZeroTurnsWrappingOff() {
        assertEquals(200, HudRendererLib.getMaxWidth(new IntegerOverride(true, 200), 450, DEFAULT));
        assertEquals(0, HudRendererLib.getMaxWidth(new IntegerOverride(true, 0), 450, DEFAULT));
        // a disabled override's value is ignored
        assertEquals(
                DEFAULT, HudRendererLib.getMaxWidth(new IntegerOverride(false, 80), 0, DEFAULT));
    }
}
