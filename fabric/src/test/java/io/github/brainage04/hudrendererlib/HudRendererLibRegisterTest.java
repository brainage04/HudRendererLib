package io.github.brainage04.hudrendererlib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import org.junit.jupiter.api.Test;

class HudRendererLibRegisterTest {
    @Config(name = "hudrendererlib-register-test")
    static class EmptyFileConfig implements ConfigData {
        int value = 7;
    }

    @Test
    void anEmptyConfigFileLoadsTheDefaults() {
        // Reads the file as GsonConfigSerializer.deserialize does (cloth-config 26.3.159), after a
        // crash during a save left it empty.
        HudRendererLib.register(
                EmptyFileConfig.class,
                (definition, configClass) ->
                        new ConfigSerializer<>() {
                            @Override
                            public void serialize(EmptyFileConfig config) {}

                            @Override
                            public EmptyFileConfig deserialize() {
                                return new Gson().fromJson("", configClass);
                            }

                            @Override
                            public EmptyFileConfig createDefault() {
                                return Utils.constructUnsafely(configClass);
                            }
                        });

        assertEquals(7, AutoConfig.getConfigHolder(EmptyFileConfig.class).getConfig().value);
    }
}
