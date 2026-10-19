package io.github.brainage04.hudrendererlib.config.core;

import me.shedaniel.autoconfig.ConfigData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void assignsInheritedDeserializedSettingsWithoutChangingExistingIds() {
        ChildConfig config = new ChildConfig();
        config.inherited.coreSettings.elementId = CoreSettingsIdAssigner.INVALID_ID;
        config.child.coreSettings.elementId = CoreSettingsIdAssigner.INVALID_ID;
        CoreSettingsIdAssigner.assignElementIds(config);
        assertNotEquals(CoreSettingsIdAssigner.INVALID_ID, config.inherited.coreSettings.elementId);
        assertNotEquals(config.inherited.coreSettings.elementId, config.child.coreSettings.elementId);
        int inheritedId = config.inherited.coreSettings.elementId;
        int childId = config.child.coreSettings.elementId;
        CoreSettingsIdAssigner.assignElementIds(config);
        assertEquals(inheritedId, config.inherited.coreSettings.elementId);
        assertEquals(childId, config.child.coreSettings.elementId);
    }

    @Test
    void doesNotAssignIdsToStaticConfigFields() {
        int previous = StaticConfig.SHARED.coreSettings.elementId;
        try {
            StaticConfig.SHARED.coreSettings.elementId = CoreSettingsIdAssigner.INVALID_ID;
            CoreSettingsIdAssigner.assignElementIds(new StaticConfig());
            assertEquals(CoreSettingsIdAssigner.INVALID_ID, StaticConfig.SHARED.coreSettings.elementId);
        } finally {
            StaticConfig.SHARED.coreSettings.elementId = previous;
        }
    }

    private static class ParentConfig implements ConfigData {
        final TestElement inherited = new TestElement("inherited");
    }

    private static final class ChildConfig extends ParentConfig {
        final TestElement child = new TestElement("child");
    }

    private static final class StaticConfig implements ConfigData {
        static final TestElement SHARED = new TestElement("static");
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
