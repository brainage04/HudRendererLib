package io.github.brainage04.hudrendererlib.config.core;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.overrides.core.ElementOverrides;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts the code defaults back into a loaded config wherever its file left an element's settings
 * null. Gson reads an enum name it does not know (a hand-edited {@code "TOP_Left"}) and an explicit
 * {@code null} as null, AutoConfig keeps that, and every HUD frame reads these values.
 */
public final class CoreSettingsDefaults {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudRendererLib.MOD_ID);

    private CoreSettingsDefaults() {}

    public static <T extends ConfigData> void applyTo(T config) {
        Object defaults = Utils.constructUnsafely(config.getClass());
        for (Class<?> type = config.getClass(); type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!(Utils.getUnsafely(field, defaults)
                        instanceof ICoreSettingsContainer defaultContainer)) continue;

                String where = type.getSimpleName() + "." + field.getName();
                Object value = Utils.getUnsafely(field, config);
                if (value instanceof ICoreSettingsContainer container) {
                    applyTo(container, defaultContainer.getCoreSettings(), where);
                } else if (value == null) {
                    warn(where, "is missing");
                    Utils.setUnsafely(field, config, defaultContainer);
                }
            }
        }
    }

    private static void applyTo(
            ICoreSettingsContainer container, CoreSettings defaults, String where) {
        CoreSettings settings = container.getCoreSettings();
        if (settings == null) {
            warn(where + ".coreSettings", "is missing");
            container.setCoreSettings(defaults);
            return;
        }
        if (settings.elementName == null) {
            warn(where + ".coreSettings.elementName", "is missing");
            settings = withName(settings, defaults.elementName);
            container.setCoreSettings(settings);
        }
        if (settings.elementAnchor == null) {
            warn(where + ".coreSettings.elementAnchor", "is missing or not a known anchor");
            settings.elementAnchor = defaults.elementAnchor;
        }
        if (settings.elementOverrides == null) {
            warn(where + ".coreSettings.elementOverrides", "is missing");
            settings.elementOverrides = defaults.elementOverrides;
            return;
        }
        for (Field field : ElementOverrides.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Utils.getUnsafely(field, settings.elementOverrides) == null) {
                warn(where + ".coreSettings.elementOverrides." + field.getName(), "is missing");
                Utils.setUnsafely(
                        field,
                        settings.elementOverrides,
                        Utils.getUnsafely(field, defaults.elementOverrides));
            }
        }
    }

    private static CoreSettings withName(CoreSettings settings, String elementName) {
        CoreSettings named =
                new CoreSettings(
                        elementName,
                        settings.enabled,
                        settings.x,
                        settings.y,
                        settings.elementAnchor);
        named.elementOverrides = settings.elementOverrides;
        return named;
    }

    private static void warn(String where, String problem) {
        LOGGER.warn("Config value {} {}, using the default", where, problem);
    }
}
