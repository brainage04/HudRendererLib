package io.github.brainage04.hudrendererlib.util;

import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.Minecraft;

public class ScreenUtils {
    public static void openConfig(Minecraft client, Class<? extends ConfigData> configClass) {
        client.setScreen(
                AutoConfigClient.getConfigScreen(configClass, client.screen).get()
        );
    }

    public static void openElementEditor(Minecraft client) {
        client.setScreen(
                new HudElementEditor()
        );
    }
}
