package io.github.brainage04.hudrendererlib.command;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.util.ScreenUtils;

public class ModCommands {
    public static void initialize() {
        HudRendererLib.platform().registerClientCommand("hudelementeditor", ScreenUtils::openElementEditor);
    }
}
