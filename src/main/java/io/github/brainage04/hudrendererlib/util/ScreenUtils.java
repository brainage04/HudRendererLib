package io.github.brainage04.hudrendererlib.util;

import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.MinecraftClient;

public class ScreenUtils {
    public static void openConfig(MinecraftClient client, Class<? extends ConfigData> configClass) {
        client.setScreen(
                AutoConfig.getConfigScreen(configClass, client.currentScreen).get()
        );
    }

    public static void openElementEditor(MinecraftClient client) {
        client.setScreen(
                new HudElementEditor()
        );
    }
}
