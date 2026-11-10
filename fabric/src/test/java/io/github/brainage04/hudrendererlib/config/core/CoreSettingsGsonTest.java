package io.github.brainage04.hudrendererlib.config.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

class CoreSettingsGsonTest {
    // The Gson that Cloth Config's GsonConfigSerializer(Config, Class) builds (cloth-config
    // 26.3.159).
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    void aCoreSettingsWithoutElementOverridesLoadsWithTheDefaults() {
        // A hand-edited config file can leave elementOverrides out, and HudRendererLib.getPadding
        // reads elementOverrides.padding for every element on every frame.
        String saved =
                """
                {
                  "elementName": "Fishing",
                  "enabled": true,
                  "x": 4,
                  "y": 4,
                  "elementAnchor": "TOP_LEFT"
                }
                """;

        CoreSettings coreSettings = GSON.fromJson(saved, CoreSettings.class);

        assertNotNull(coreSettings.elementOverrides);
    }
}
