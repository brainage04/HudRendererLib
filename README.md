# About
HudRendererLib is a library for rendering Heads-Up Display (HUD) elements in Fabric mods for Minecraft.

This mod integrates with Cloth Config's AutoConfig system, and uses annotations from it.

I created this library because I found myself copying and pasting a lot of the same rendering code
I was using for rendering HUD elements, and didn't want to have to maintain all the mods that use
said code each time I found a way to improve said code.

# Setup
To add this library to your mod, add the following dependency to your `build.gradle` file:
```groovy
repositories {
    // HudRendererLib
    maven {
        url = "https://maven.pkg.github.com/brainage04/HudRendererLib"
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // HudRendererLib
    modImplementation "io.github.brainage04:hudrendererlib:1.0.0-1.21.4"
}
```

You will also need a "Personal access token (classic)" token
from [this](https://github.com/settings/tokens) page
with the `read:packages` permission in order to download this package.

Once you have generated the token,
you can export your username and token
as environment variables like so:

Linux/macOS:
```bash
export GITHUB_USERNAME=<github_username>
export GITHUB_TOKEN=<github_token>
```

Windows:
```bash
setx GITHUB_USERNAME <github_username>
setx GITHUB_TOKEN <github_token>
```

Where `<github_username>` is your GitHub username,
and `<github_token>` is the GitHub token that you created.

Note: The Linux/macOS command is not persistent in between terminal instances.
To achieve persistence, you should append this command to the end of the
`/etc/environment` file and then reload the changes by logging out and back in. 

Supported Minecraft versions include 1.21.4 and 1.21.5 (`1.21.4` is used for both).
Each library version may support several different Minecraft versions.
When changes are made to the Minecraft and/or Fabric rendering APIs,
the Minecraft version in the library version will be updated.
This means that each library version supports a range of Minecraft versions,
from the Minecraft version specified in the library version (such as `1.21.4`)
to the Minecraft version before the one specified in the next library version. 

The first thing you need to do is register your config class with HudRendererLib in your `onInitializeClient` method like so:

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