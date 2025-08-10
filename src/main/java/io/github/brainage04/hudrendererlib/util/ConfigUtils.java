package io.github.brainage04.hudrendererlib.util;

import io.github.brainage04.hudrendererlib.config.core.CoreSettingsIdAssigner;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.util.ActionResult;

import java.util.HashSet;
import java.util.Set;

public class ConfigUtils {
    private static final Set<Class<? extends ConfigData>> configClasses = new HashSet<>();

    public static void addConfigClass(Class<? extends ConfigData> configClass) {
        configClasses.add(configClass);
    }

    @SuppressWarnings({"SameReturnValue", "unused"})
    public static <T extends ConfigData> ActionResult saveLoad(ConfigHolder<T> configHolder, T config) {
        // refresh element IDs
        CoreSettingsIdAssigner.assignElementIds(config);

        HudElementEditor.populateCoreSettingsElements();

        return ActionResult.SUCCESS;
    }

    public static HudRendererLibConfig getConfig() {
        return AutoConfig.getConfigHolder(HudRendererLibConfig.class).getConfig();
    }

    public static void saveConfigClasses() {
        for (Class<? extends ConfigData> configClass : configClasses) {
            AutoConfig.getConfigHolder(configClass).save();
        }
    }

    public static void loadConfigClasses() {
        for (Class<? extends ConfigData> configClass : configClasses) {
            AutoConfig.getConfigHolder(configClass).load();
        }
    }
}
