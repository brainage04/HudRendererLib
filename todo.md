# HudRendererLib todo

The loader-neutral HUD contract is in place: `CoreHudElement`, `LayerInfo` and `HudRendererPlatform` live in `common`, with Fabric (`HudElementRegistry`) and NeoForge (`RegisterGuiLayersEvent`) adapters, and both loaders publish 1.0.11. The loaders still differ in these ways:

- [ ] Hide elements with F1 on NeoForge. Fabric elements inherit the anchor layer's hide-GUI condition; NeoForge layers have none, and `HudRenderer` never checks `hideGui`.
- [ ] Add a NeoForge config screen (`IConfigScreenFactory`); only Fabric has one, through Mod Menu.
- [ ] Map layer anchors between loaders. `LayerInfo` takes a raw vanilla id, and only ids both loaders share (such as `minecraft:chat`) work on both; Fabric's `status_effects` is NeoForge's `effects`.
- [ ] Reject or queue `registerHudElement` calls made after `RegisterGuiLayersEvent`. NeoForge silently drops them, while Fabric accepts them, and the Fabric GameTest relies on that.
- [ ] Add NeoForge GameTests. The only unit test covers element id assignment.
- [ ] Update the README, which still says 1.0.7.
