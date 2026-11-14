package io.github.brainage04.hudrendererlib;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;

/**
 * Loads the defaults from a config file that is empty or holds only {@code null}. Gson reads either
 * as a null config, GsonConfigSerializer returns it as it is, and AutoConfig's ConfigManager.load
 * then calls validatePostLoad on null and fails the mod's initialisation (cloth-config 26.3.159).
 * Saving truncates the file before writing it, so an interrupted save leaves it empty.
 */
final class EmptyFileDefaultsSerializer<T extends ConfigData> implements ConfigSerializer<T> {
    private final ConfigSerializer<T> serializer;
    private final Class<T> configClass;

    private EmptyFileDefaultsSerializer(ConfigSerializer<T> serializer, Class<T> configClass) {
        this.serializer = serializer;
        this.configClass = configClass;
    }

    static <T extends ConfigData> ConfigSerializer.Factory<T> wrap(
            ConfigSerializer.Factory<T> factory) {
        return (definition, configClass) ->
                new EmptyFileDefaultsSerializer<>(
                        factory.create(definition, configClass), configClass);
    }

    @Override
    public void serialize(T config) throws SerializationException {
        serializer.serialize(config);
    }

    @Override
    public T deserialize() throws SerializationException {
        T config = serializer.deserialize();
        if (config != null) return config;

        HudRendererLib.LOGGER.warn(
                "Config file of {} is empty, using the defaults", configClass.getSimpleName());
        return serializer.createDefault();
    }

    @Override
    public T createDefault() {
        return serializer.createDefault();
    }
}
