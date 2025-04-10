package io.github.brainage04.hudrendererlib.keys;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.util.ScreenUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class ModKeys {
    public static final Map<KeyBinding, Class<? extends ConfigData>> openConfigKeyMap = new HashMap<>();
    public static final KeyBinding openElementEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.%s.openElementEditor".formatted(HudRendererLib.MOD_ID),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            HudRendererLib.MOD_NAME
    ));

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (KeyBinding key : openConfigKeyMap.keySet()) {
                if (key.isPressed()) {
                    client.setScreen(
                            AutoConfig.getConfigScreen(openConfigKeyMap.get(key), client.currentScreen).get()
                    );
                }
            }

            if (openElementEditor.isPressed()) {
                ScreenUtils.openElementEditor(client);
            }
        });
    }
}
