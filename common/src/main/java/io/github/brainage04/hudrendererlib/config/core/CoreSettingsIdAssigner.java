package io.github.brainage04.hudrendererlib.config.core;

import me.shedaniel.autoconfig.ConfigData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CoreSettingsIdAssigner {
    public static final int INVALID_ID = 0;
    private static final int FIRST_ID = 1;
    private static int nextId = FIRST_ID;

    public static synchronized int assignIdIfUnset(int current) {
        return current == INVALID_ID ? nextId++ : current;
    }


    public static <T extends ConfigData> void assignElementIds(T config) {
        for (Class<?> type = config.getClass(); type != Object.class; type = type.getSuperclass()) {
            assignDeclaredElementIds(config, type);
        }
    }

    private static void assignDeclaredElementIds(ConfigData config, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);

            try {
                Object value = field.get(config);
                if (value instanceof ICoreSettingsContainer coreSettingsContainer) {
                    CoreSettings coreSettings = coreSettingsContainer.getCoreSettings();
                    coreSettings.elementId = assignIdIfUnset(coreSettingsContainer.getCoreSettings().elementId);
                    coreSettingsContainer.setCoreSettings(coreSettings);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to assign ID to " + field.getName(), e);
            }
        }
    }
}
