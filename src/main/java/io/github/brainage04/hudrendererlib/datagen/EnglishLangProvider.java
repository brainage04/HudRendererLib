package io.github.brainage04.hudrendererlib.datagen;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.hudrendererlib.util.StringUtils;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

public class EnglishLangProvider extends FabricLanguageProvider {
    public EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    private void addAutoConfigTranslations(String[] keys, String[] values, TranslationBuilder translationBuilder) {
        for (int i = 0; i < keys.length; i++) {
            translationBuilder.add("%s.%s".formatted(autoConfigPrefix, keys[i]), values[i]);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void addAutomaticTranslations(String[] keys, String packageName, TranslationBuilder translationBuilder) {
        for (String key : keys) {
            translationBuilder.add("%s.%s.%s".formatted(packageName, HudRendererLib.MOD_ID, key), StringUtils.pascalCaseToHumanReadable(key));
        }
    }

    private final String autoConfigPrefix = "text.autoconfig.%s.option".formatted(HudRendererLib.MOD_ID);

    private void generateReflectedTranslations(Class<?> clazz, String baseKey, TranslationBuilder translationBuilder) {
        for (Field field : clazz.getFields()) {
            String newBaseKey = "%s.%s".formatted(baseKey, field.getName());

            translationBuilder.add(newBaseKey, StringUtils.pascalCaseToHumanReadable(field.getName()));

            if (field.getType().isPrimitive()) continue;
            if (field.getType().isEnum()) continue;
            if (field.getType() == String.class) continue;

            generateReflectedTranslations(field.getType(), newBaseKey, translationBuilder);
        }
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        // element editor
        translationBuilder.add(
                "text.autoconfig.%s.title".formatted(HudRendererLib.MOD_ID),
                "%s Config Editor".formatted(HudRendererLib.MOD_NAME)
        );

        // keybinds
        addAutomaticTranslations(
                new String[]{
                        "openConfig",
                        "openElementEditor"
                },
                "keybind",
                translationBuilder
        );

        // config
        generateReflectedTranslations(HudRendererLibConfig.class, autoConfigPrefix, translationBuilder);

        addAutoConfigTranslations(
                new String[]{
                        "adjustTopRightElementsWithStatusEffects.@Tooltip"
                },
                new String[]{
                        "Shifts elements with the \"%s\" alignment down by the specified amount of pixels below if potion effects are being overlayed in the top right.".formatted(ElementAnchor.TOP_RIGHT)
                },
                translationBuilder
        );
    }
}
