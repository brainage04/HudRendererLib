package io.github.brainage04.hudrendererlib.datagen;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import io.github.brainage04.hudrendererlib.util.StringUtils;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class EnglishLangProvider extends FabricLanguageProvider {
    public EnglishLangProvider(
            FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    private void addAutoConfigTranslations(
            String[] keys, String[] values, TranslationBuilder translationBuilder) {
        for (int i = 0; i < keys.length; i++) {
            translationBuilder.add("%s.%s".formatted(autoConfigPrefix, keys[i]), values[i]);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void addAutomaticTranslations(
            String[] keys, String packageName, TranslationBuilder translationBuilder) {
        for (String key : keys) {
            translationBuilder.add(
                    "%s.%s.%s".formatted(packageName, HudRendererLib.MOD_ID, key),
                    StringUtils.pascalCaseToHumanReadable(key));
        }
    }

    private final String autoConfigPrefix =
            "text.autoconfig.%s.option".formatted(HudRendererLib.MOD_ID);

    private void generateReflectedTranslations(
            Class<?> clazz, String baseKey, TranslationBuilder translationBuilder) {
        for (Field field : clazz.getFields()) {
            String newBaseKey = "%s.%s".formatted(baseKey, field.getName());

            translationBuilder.add(
                    newBaseKey, StringUtils.pascalCaseToHumanReadable(field.getName()));

            if (field.getType().isPrimitive()) continue;
            if (field.getType().isEnum()) continue;
            if (field.getType() == String.class) continue;

            generateReflectedTranslations(field.getType(), newBaseKey, translationBuilder);
        }
    }

    @Override
    public void generateTranslations(
            HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(
                "text.autoconfig.%s.title".formatted(HudRendererLib.MOD_ID),
                "%s Config Editor".formatted(HudRendererLib.MOD_NAME));

        addAutomaticTranslations(
                new String[] {"openConfig", "openElementEditor"}, "key", translationBuilder);
        translationBuilder.add(
                "key.category.%s.keys".formatted(HudRendererLib.MOD_ID), HudRendererLib.MOD_NAME);

        generateReflectedTranslations(
                HudRendererLibConfig.class, autoConfigPrefix, translationBuilder);

        addAutoConfigTranslations(
                new String[] {
                    "maxElementWidth.@Tooltip",
                    "showVanillaStatusEffects.@Tooltip",
                    "adjustTopRightElementsWithStatusEffects.@Tooltip"
                },
                new String[] {
                    "Wraps every element's text at this width in GUI pixels, between words and never inside one. At 0 each element uses its own default width, and most elements have none. An element's own Max Width override, also set in the HUD Element Editor, takes precedence.",
                    "Show the game's status effect icons in the top right of the screen.",
                    "Shifts elements with the \"%s\" alignment down by the specified amount of pixels below if potion effects are being overlayed in the top right. Only applies while Show Vanilla Status Effects is on."
                            .formatted(ElementAnchor.TOP_RIGHT)
                },
                translationBuilder);
    }
}
