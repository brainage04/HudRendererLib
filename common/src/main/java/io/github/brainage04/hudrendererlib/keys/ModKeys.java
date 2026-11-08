package io.github.brainage04.hudrendererlib.keys;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.util.ScreenUtils;
import java.util.HashMap;
import java.util.Map;
import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.KeyMapping;
import org.lwjgl.sdl.SDLScancode;

public class ModKeys {
    public static final Map<KeyMapping, Class<? extends ConfigData>> openConfigKeyMap =
            new HashMap<>();
    public static final KeyMapping openElementEditor =
            new KeyMapping(
                    "key.%s.openElementEditor".formatted(HudRendererLib.MOD_ID),
                    InputConstants.Type.KEYBOARD,
                    SDLScancode.SDL_SCANCODE_KP_PLUS,
                    HudRendererLib.KEY_CATEGORY);

    public static void initialize() {
        HudRendererLib.platform().registerKeyCategory(HudRendererLib.KEY_CATEGORY);
        HudRendererLib.platform().registerKeyMapping(openElementEditor);
        HudRendererLib.platform()
                .registerEndClientTick(
                        client -> {
                            for (KeyMapping key : openConfigKeyMap.keySet()) {
                                if (key.consumeClick()) {
                                    ScreenUtils.openConfig(client, openConfigKeyMap.get(key));
                                }
                            }

                            if (openElementEditor.consumeClick()) {
                                ScreenUtils.openElementEditor(client);
                            }
                        });
    }
}
