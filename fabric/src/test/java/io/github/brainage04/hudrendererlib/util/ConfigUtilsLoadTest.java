package io.github.brainage04.hudrendererlib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import me.shedaniel.autoconfig.ConfigData;
import org.junit.jupiter.api.Test;

class ConfigUtilsLoadTest {
    // The Gson that Cloth Config's GsonConfigSerializer(Config, Class) builds (cloth-config
    // 26.3.159).
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    void aValidFileLoadsUnchanged() {
        String saved =
                """
                {
                  "keys": {
                    "coreSettings": {
                      "elementName": "Keys",
                      "enabled": false,
                      "x": -12,
                      "y": 30,
                      "elementAnchor": "BOTTOM_LEFT",
                      "elementOverrides": {
                        "padding": { "enabled": true, "value": 4 },
                        "maxWidth": { "enabled": true, "value": 120 },
                        "textShadows": { "enabled": true, "value": false },
                        "textColour": { "enabled": true, "value": 255 },
                        "backdropOpacity": { "enabled": true, "value": 50 }
                      }
                    }
                  }
                }
                """;

        TestConfig config = load(saved);

        CoreSettings settings = config.keys.coreSettings;
        assertFalse(settings.enabled);
        assertEquals(-12, settings.x);
        assertEquals(30, settings.y);
        assertEquals(ElementAnchor.BOTTOM_LEFT, settings.elementAnchor);
        assertTrue(settings.elementOverrides.padding.enabled);
        assertEquals(4, settings.elementOverrides.padding.value);
        assertEquals(120, settings.elementOverrides.maxWidth.value);
        assertFalse(settings.elementOverrides.textShadows.value);
        assertEquals(255, settings.elementOverrides.textColour.value);
        assertEquals(50, settings.elementOverrides.backdropOpacity.value);
    }

    @Test
    void aMisspeltAnchorFallsBackToTheElementDefault() {
        // Gson reads an enum name it does not know as null, and every frame switches on the anchor.
        TestConfig config =
                load(
                        """
                        { "keys": { "coreSettings": {
                          "elementName": "Keys", "enabled": true, "x": 3, "y": 4,
                          "elementAnchor": "TOP_Left"
                        } } }
                        """);

        assertEquals(ElementAnchor.TOP_RIGHT, config.keys.coreSettings.elementAnchor);
        assertEquals(3, config.keys.coreSettings.x);
        assertEquals(4, config.keys.coreSettings.y);
    }

    @Test
    void nullOverridesFallBackToTheDefaults() {
        TestConfig config =
                load(
                        """
                        { "keys": { "coreSettings": {
                          "elementName": "Keys", "enabled": true, "x": 3, "y": 4,
                          "elementAnchor": "TOP_LEFT", "elementOverrides": null
                        } } }
                        """);
        assertNotNull(config.keys.coreSettings.elementOverrides);
        assertNotNull(config.keys.coreSettings.elementOverrides.padding);

        config =
                load(
                        """
                        { "keys": { "coreSettings": {
                          "elementName": "Keys", "enabled": true, "x": 3, "y": 4,
                          "elementAnchor": "TOP_LEFT",
                          "elementOverrides": {
                            "padding": null,
                            "maxWidth": { "enabled": true, "value": 90 }
                          }
                        } } }
                        """);
        assertNotNull(config.keys.coreSettings.elementOverrides.padding);
        assertEquals(90, config.keys.coreSettings.elementOverrides.maxWidth.value);
    }

    @Test
    void nullElementSettingsFallBackToTheDefaults() {
        TestConfig config = load("{ \"keys\": { \"coreSettings\": null } }");
        assertNotNull(config.keys.coreSettings);
        assertEquals(ElementAnchor.TOP_RIGHT, config.keys.coreSettings.elementAnchor);

        config = load("{ \"keys\": null }");
        assertNotNull(config.keys);
        assertNotNull(config.keys.coreSettings);
    }

    @Test
    void aMissingNameFallsBackToTheElementDefaultAndKeepsTheSettings() {
        // The editor shows the name as Component.literal(name) when the element is hovered.
        TestConfig config =
                load(
                        """
                        { "keys": { "coreSettings": {
                          "enabled": false, "x": 3, "y": 4, "elementAnchor": "BOTTOM",
                          "elementOverrides": { "padding": { "enabled": true, "value": 6 } }
                        } } }
                        """);

        CoreSettings settings = config.keys.coreSettings;
        assertEquals("Keys", settings.elementName);
        assertFalse(settings.enabled);
        assertEquals(3, settings.x);
        assertEquals(4, settings.y);
        assertEquals(ElementAnchor.BOTTOM, settings.elementAnchor);
        assertTrue(settings.elementOverrides.padding.enabled);
        assertEquals(6, settings.elementOverrides.padding.value);
        assertNotNull(settings.elementOverrides.maxWidth);
    }

    private static TestConfig load(String json) {
        TestConfig config = GSON.fromJson(json, TestConfig.class);
        ConfigUtils.onLoad(null, config);
        return config;
    }

    private static final class TestConfig implements ConfigData {
        TestElement keys = new TestElement();
    }

    private static final class TestElement implements ICoreSettingsContainer {
        CoreSettings coreSettings = new CoreSettings("Keys", true, 5, 5, ElementAnchor.TOP_RIGHT);

        @Override
        public CoreSettings getCoreSettings() {
            return coreSettings;
        }

        @Override
        public void setCoreSettings(CoreSettings coreSettings) {
            this.coreSettings = coreSettings;
        }
    }
}
