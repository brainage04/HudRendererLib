# HudRendererLib todo

The loader-neutral HUD contract is in place: `CoreHudElement`, `LayerInfo` and `HudRendererPlatform` live in `common`, with Fabric (`HudElementRegistry`) and NeoForge (`RegisterGuiLayersEvent`) adapters, and both loaders publish 1.0.12. The loaders still differ in these ways:

- [x] Hide elements with F1 on NeoForge. Fixed in 1.0.12: the NeoForge adapter draws each element only when vanilla draws its anchor (F1, game mode, spectator, experience, vehicle), the same call-site conditions Fabric's attached elements inherit.
- [x] Add a NeoForge config screen. NeoForge registers an `IConfigScreenFactory` that opens the same Cloth Config screen Mod Menu opens on Fabric.
- [x] Map layer anchors between loaders. Fixed in 1.0.12: `LayerInfo` takes a loader-neutral `VanillaHudLayer` (every layer both loaders have, mapped to `VanillaHudElements`/`VanillaGuiLayers`); the `Identifier` constructor accepts either loader's id and an unknown id fails at registration naming the element and id.
- [ ] Reject or queue `registerHudElement` calls made after `RegisterGuiLayersEvent`. NeoForge silently drops them, while Fabric accepts them, and the Fabric GameTest relies on that.
- [ ] Add NeoForge GameTests. The only unit test covers element id assignment.
- [x] Update the README, which still says 1.0.7. Now says 1.0.12.
