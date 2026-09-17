package io.github.brainage04.hudrendererlib.config.core;

import me.shedaniel.autoconfig.ConfigData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CoreSettingsIdAssignerTest {
    @Test
    void assignsIdsGloballyAcrossConfigClasses() {
        TestConfig first = new TestConfig("first");
        TestConfig second = new TestConfig("second");
        first.element.coreSettings.elementId = CoreSettingsIdAssigner.INVALID_ID;
        second.element.coreSettings.elementId = CoreSettingsIdAssigner.INVALID_ID;

        CoreSettingsIdAssigner.assignElementIds(first);
        CoreSettingsIdAssigner.assignElementIds(second);

        assertNotEquals(first.element.coreSettings.elementId, second.element.coreSettings.elementId);
    }

    private static final class TestConfig implements ConfigData {
        private final TestElement element;

        private TestConfig(String name) {
            element = new TestElement(name);
        }
    }

    private static final class TestElement implements ICoreSettingsContainer {
        private CoreSettings coreSettings;

        private TestElement(String name) {
            coreSettings = new CoreSettings(name, true, 0, 0, ElementAnchor.TOP_LEFT);
        }

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
