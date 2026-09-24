package io.github.brainage04.hudrendererlib.util;

import io.github.brainage04.hudrendererlib.config.core.CoreSettingsIdAssigner;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.world.InteractionResult;
import java.util.HashSet;
import java.util.Set;

public class ConfigUtils {
    private static final Set<Class<? extends ConfigData>> configClasses = new HashSet<>();

    public static void addConfigClass(Class<? extends ConfigData> configClass) {
        configClasses.add(configClass);
    }

    @SuppressWarnings({"SameReturnValue", "unused"})
    public static <T extends ConfigData> InteractionResult onSave(ConfigHolder<T> configHolder, T config) {
        CoreSettingsIdAssigner.assignElementIds(config);
        HudElementEditor.populateCoreSettingsElements();

        return InteractionResult.SUCCESS;
    }

    /**
     * Gives the freshly loaded config's elements their IDs. The editor's element map is not
     * rebuilt here: AutoConfig calls load listeners before it replaces the holder's config, so
     * the registered elements would still report the old config's settings. Callers of
     * {@code load()} rebuild the map afterwards (see {@link #loadConfigClasses()}).
     */
    @SuppressWarnings({"SameReturnValue", "unused"})
    public static <T extends ConfigData> InteractionResult onLoad(ConfigHolder<T> configHolder, T config) {
        CoreSettingsIdAssigner.assignElementIds(config);

        return InteractionResult.SUCCESS;
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
        HudElementEditor.populateCoreSettingsElements();
    }
}
