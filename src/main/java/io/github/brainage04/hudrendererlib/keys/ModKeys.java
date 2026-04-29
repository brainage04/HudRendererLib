package io.github.brainage04.hudrendererlib.keys;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.util.ScreenUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class ModKeys {
    public static final Map<KeyMapping, Class<? extends ConfigData>> openConfigKeyMap = new HashMap<>();
    public static final KeyMapping openElementEditor = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.%s.openElementEditor".formatted(HudRendererLib.MOD_ID),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            HudRendererLib.KEY_CATEGORY
    ));

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (KeyMapping key : openConfigKeyMap.keySet()) {
                if (key.isDown()) {
                    client.setScreen(
                            AutoConfigClient.getConfigScreen(openConfigKeyMap.get(key), client.screen).get()
                    );
                }
            }

            if (openElementEditor.isDown()) {
                ScreenUtils.openElementEditor(client);
            }
        });
    }
}
