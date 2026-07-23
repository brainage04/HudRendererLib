# About
HudRendererLib is a library for rendering Heads-Up Display (HUD) elements in Fabric and NeoForge mods for Minecraft.

This mod integrates with Cloth Config's AutoConfig system, and uses annotations from it.

I created this library because I found myself copying and pasting a lot of the same rendering code
I was using for rendering HUD elements, and didn't want to have to maintain all the mods that use
said code each time I found a way to improve said code.

# Setup

HudRendererLib 1.0.6 targets Minecraft 26.2 on both loaders.

## Fabric Loom

HudRendererLib 1.0.6 is published on Maven Central:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    modImplementation "io.github.brainage04:hudrendererlib:1.0.6"
}
```

## NeoForge with Architectury Loom

The NeoForge artifact is also published on Maven Central:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.brainage04:hudrendererlib-neoforge:1.0.6"
}
```

For local workspace development, publish both loader artifacts to the sibling Maven repository with:

```bash
./gradlew :fabric:publishAllPublicationsToLocalRepository :neoforge:publishAllPublicationsToLocalRepository
```

The library version and supported Minecraft version are tracked separately in `gradle.properties`, `fabric.mod.json`, and `neoforge.mods.toml`. Check the loader-specific release metadata before using HudRendererLib with another Minecraft version.

For Fabric, register your config class in `onInitializeClient`; on NeoForge, make the same call from your client-only mod initialization path:

```java
@Override
public void onInitializeClient() {
    // ...

    HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);

    // ...
}
```

Where `ModConfig` is the name of your mod's config class.

This will automatically register the config class with both AutoConfig and HudRendererLib
and ensure the HUD Element Editor works properly after saving/loading.

See the [AutoConfig wiki](https://shedaniel.gitbook.io/cloth-config/auto-config/creating-a-config-class) for details on how to create a config class.

To ensure that the config screen works properly, make sure to annotate all `CoreSettings` fields
in classes that implement `ICoreSettingsContainer` with `@ConfigEntry.Gui.CollapsibleObject`,
otherwise it will not appear in the config GUI! Here is an example of how to do this:
```java
public class ExampleHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;

    public ExampleHudConfig() {
        this.coreSettings = new CoreSettings(0, "Example HUD", true, 5, 5, ElementAnchor.TOP_LEFT);
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
```

Each time that you create a new HUD element, you should increment the `elementId`
you use when initialising your `CoreSettings` field (first argument in `CoreSettings` constructor).

This annoying requirement is something I am planning on fixing in the future,
when I figure out a better way to do it.

Once you have registered a config object, you can register a basic (text-only) HUD element like so:
```java
public class ExampleHud implements BasicHudElement<ExampleHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        // add stuff to lines here

        return lines;
    }

    @Override
    public ExampleHudConfig getElementConfig() {
        return getConfig().exampleHudConfig;
    }
}
```

or a custom HUD element like so:
```java
public class ExampleHud implements CustomHudElement<ExampleHudConfig> {
    @Override
    public void render(TextRenderer textRenderer, DrawContext drawContext) {
        // render element here
    }

    @Override
    public ExampleHudConfig getElementConfig() {
        return getConfig().exampleHudConfig;
    }
}
```

If you wish to change the layer that a given element (basic or custom) is rendered with:
```java
    @Override
    public LayerInfo getLayerInfo() {
        return new LayerInfo(IdentifiedLayer.CHAT, true);
    }
```

The first argument should be an officially supported HUD layer
(`Identifiers` from `IdentifiedLayer`), and the second argument
should be whether to render the element before or after the
specified layer.

Once you have created a config and HUD class for your element, register the element in your `onInitializeClient` method like so:

```java
@Override
public void onInitializeClient() {
    // ...

    // register your config class first
    HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);

    // THEN register your HUD elements
    HudRendererLib.registerHudElement(new ExampleHud());

    // ...
}
```

Once you have done this, you may want to add keybinds and/or commands that open the config GUI.
You can use the built-in methods like so AFTER registering your main config class:
```java
@Override
public void onInitializeClient() {
    // ...

    // register your config class first
    HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);
    
    // THEN register your commands/keys
    // HUD elements can be registered before/after commands/keys as long as they are registered after the main config class
    HudRendererLib.registerConfigCommand(ModConfig.class, MOD_ID);
    HudRendererLib.registerConfigKey(ModConfig.class, MOD_NAME);

    // ...
}
```

Where `MOD_ID` is the ID of your mod and `MOD_NAME` is the name of your mod.

The HUD Element Editor can be accessed with `/hudrendererlibconfig`, or the Numpad Plus key.
The HudRendererLib Config Editor can be accessed with `/hudelementeditor`, or the Numpad Enter key.

For more examples, please see my mods that use this library:
- [BrainageHUD](https://github.com/brainage04/BrainageHUD/tree/master/src/main/java/com/github/brainage04/brainagehud)
- [TwitchPlaysMinecraft](https://github.com/brainage04/TwitchPlaysMinecraft/tree/master/src/main/java/io/github/brainage04/twitchplaysminecraft)
