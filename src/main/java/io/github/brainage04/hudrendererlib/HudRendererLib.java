package io.github.brainage04.hudrendererlib;

import io.github.brainage04.hudrendererlib.command.ModCommands;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.hud.core.HudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.keys.ModKeys;
import io.github.brainage04.hudrendererlib.util.ConfigUtils;
import io.github.brainage04.hudrendererlib.util.ScreenUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HudRendererLib implements ClientModInitializer {
    public static final String MOD_ID = "hudrendererlib";
    public static final String MOD_NAME = "HudRendererLib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info(MOD_NAME + " initialising...");

        // config
        AutoConfig.register(HudRendererLibConfig.class, GsonConfigSerializer::new);

        // commands
        ModCommands.initialize();
        registerConfigCommand(HudRendererLibConfig.class, MOD_ID);

        // keys
        ModKeys.initialize();
        registerConfigKey(HudRendererLibConfig.class, GLFW.GLFW_KEY_KP_ENTER, MOD_ID, MOD_NAME);

        LOGGER.info(MOD_NAME + " initialized.");
    }

    @SuppressWarnings("unused")
    public static <T extends ConfigData> void register(Class<T> configClass, ConfigSerializer.Factory<T> serializerFactory) {
        AutoConfig.register(configClass, serializerFactory);
        ConfigUtils.addConfigClass(configClass);

        AutoConfig.getConfigHolder(configClass).registerSaveListener(ConfigUtils::saveLoad);
        AutoConfig.getConfigHolder(configClass).registerLoadListener(ConfigUtils::saveLoad);
    }

    @SuppressWarnings("unused")
    public static void registerConfigCommand(Class<? extends ConfigData> configClass, String modId) {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(modId + "config")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> ScreenUtils.openConfig(context.getSource().getClient(), configClass));

                            return 1;
                        })
                )
        ));
    }

    @SuppressWarnings("unused")
    public static void registerConfigKey(Class<? extends ConfigData> configClass, int keycode, String modId, String modName) {
        ModKeys.openConfigKeyMap.put(
                KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.%s.openConfig".formatted(modId),
                        InputUtil.Type.KEYSYM,
                        keycode,
                        modName
                )),
                configClass
        );
    }

    @SuppressWarnings("unused")
    public static void registerHudElement(HudElement<? extends ICoreSettingsContainer> hudElement) {
        Identifier layerId = Identifier.of(MOD_ID, "hud-layer-%d".formatted(HudRenderer.REGISTERED_ELEMENTS.size()));

        HudRenderer.REGISTERED_ELEMENTS.add(hudElement);

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
            TriConsumer<Identifier, Identifier, LayeredDrawer.Layer> attachAction = hudElement.getLayerInfo().before()
                    ? layeredDrawer::attachLayerBefore
                    : layeredDrawer::attachLayerAfter;

            attachAction.accept(
                    hudElement.getLayerInfo().layer(),
                    layerId,
                    (context, tickCounter) ->
                            HudRenderer.render(context, hudElement)
            );
        });
    }

    // override util methods
    public static int getPadding(CoreSettings coreSettings) {
        if (coreSettings.elementOverrides.padding.enabled) {
            return coreSettings.elementOverrides.padding.value;
        } else {
            return ConfigUtils.getConfig().elementPadding;
        }
    }

    public static boolean getTextShadows(CoreSettings coreSettings) {
        if (coreSettings.elementOverrides.textShadows.enabled) {
            return coreSettings.elementOverrides.textShadows.value;
        } else {
            return ConfigUtils.getConfig().textShadows;
        }
    }

    public static int getTextColour(CoreSettings coreSettings) {
        if (coreSettings.elementOverrides.textColour.enabled) {
            return coreSettings.elementOverrides.textColour.value;
        } else {
            return ConfigUtils.getConfig().textColour;
        }
    }

    public static int getOpacity(CoreSettings coreSettings) {
        if (coreSettings.elementOverrides.backdropOpacity.enabled) {
            return coreSettings.elementOverrides.backdropOpacity.value;
        } else {
            return ConfigUtils.getConfig().backdropOpacity;
        }
    }
}
