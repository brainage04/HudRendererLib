package io.github.brainage04.hudrendererlib.config.core;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
@Config(name = HudRendererLib.MOD_ID)
public class HudRendererLibConfig implements ConfigData {
    public int screenMargin = 5;

    // global element settings
    public int elementPadding = 2;
    public boolean textShadows = true;
    @ConfigEntry.ColorPicker
    public int textColour = 0xffffff;
    @ConfigEntry.BoundedDiscrete(min = 0, max = 255)
    public int backdropOpacity = 0;

    // quality of life settings
    @ConfigEntry.Gui.Tooltip()
    public boolean adjustTopRightElementsWithStatusEffects = true;
    public int adjustTopRightElementsWithStatusEffectsAmount = 21;
}
