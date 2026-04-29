package io.github.brainage04.hudrendererlib;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.hudrendererlib.command.ModCommands;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.CoreSettingsIdAssigner;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.keys.ModKeys;
import io.github.brainage04.hudrendererlib.util.ConfigUtils;
import io.github.brainage04.hudrendererlib.util.ScreenUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HudRendererLib implements ClientModInitializer {
    public static final String MOD_ID = "hudrendererlib";
    public static final String MOD_NAME = "HudRendererLib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keys"));

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

        CoreSettingsIdAssigner.assignElementIds(AutoConfig.getConfigHolder(configClass).getConfig());

        AutoConfig.getConfigHolder(configClass).registerSaveListener(ConfigUtils::saveLoad);
        AutoConfig.getConfigHolder(configClass).registerLoadListener(ConfigUtils::saveLoad);
    }

    @SuppressWarnings("unused")
    public static void registerConfigCommand(Class<? extends ConfigData> configClass, String modId) {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal(modId + "config")
                        .executes(context -> {
                            Minecraft.getInstance().schedule(() -> ScreenUtils.openConfig(context.getSource().getClient(), configClass));

                            return 1;
                        })
                )
        ));
    }

    @SuppressWarnings("unused")
    public static void registerConfigKey(Class<? extends ConfigData> configClass, int keycode, String modId, String modName) {
        ModKeys.openConfigKeyMap.put(
                KeyMappingHelper.registerKeyMapping(new KeyMapping(
                        "key.%s.openConfig".formatted(modId),
                        InputConstants.Type.KEYSYM,
                        keycode,
                        KEY_CATEGORY
                )),
                configClass
        );
    }

    @SuppressWarnings("unused")
    public static void registerHudElement(CoreHudElement<? extends ICoreSettingsContainer> coreHudElement) {
        Identifier layerId = Identifier.fromNamespaceAndPath(MOD_ID, "hud-layer-%d".formatted(HudRenderer.REGISTERED_ELEMENTS.size()));

        HudRenderer.REGISTERED_ELEMENTS.add(coreHudElement);

        TriConsumer<Identifier, Identifier, HudElement> attachAction = coreHudElement.getLayerInfo().before()
                ? HudElementRegistry::attachElementBefore
                : HudElementRegistry::attachElementAfter;

        attachAction.accept(
                coreHudElement.getLayerInfo().layer(),
                layerId,
                coreHudElement
        );
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
            return 0xFF_00_00_00 + coreSettings.elementOverrides.textColour.value;
        } else {
            return 0xFF_00_00_00 + ConfigUtils.getConfig().textColour;
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
