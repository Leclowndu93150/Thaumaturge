# 1.21.1 vs 26.1.2 Parity

## Not Implemented / Partial

| Feature | Current State | Note |
| --- | --- | --- |
| Gametest suite | NYI (also dev only) | Planned - Low Prio |
| JEI live search-index refresh | Partially Implemented | only the live refresh is missing |

## Codebase Differences

| 26.1 | 1.21 |
| --- | --- |
| `hurtServer` / `hurtClient` | `content/entity/ISidedHurt` |
| `VoidGearItem` covers tools and armour | armour base only. Void tools are `VoidSwordItem`, `VoidPickaxeItem`, `VoidAxeItem`, `VoidShovelItem`; `PrimalCrusherItem` is standalone |
| `JarFill` record feeding the jar item renderer | `JarItemSpecialRenderer` reads capacity off the `BlockItem`'s block -> a custom jar only needs `IEssentiaJar` |
| item variant JSON (`assets/.../items/*.json`) | caster, focus and celestial notes variants are model overrides plus `ItemColor` handlers in `client/color` |
| Ghast movement classes reused from vanilla | local ports at `content/entity/ai/GhastLike{Flight,LookGoal,MoveControl}`, used by Wisp and Fire Bat |
| taint overlay: `ModelUvLayout` reads cube polygons off `Model#root()`, and `LivingEntityRendererMixin` puts the overlay flag on the render state | `ModelPart` polygons are package-private, so `ModelUvLayout` compiles each cube into `ModelPolygonCollector`. Model parts come from `ModelRootParts`, which mixins add to `HierarchicalModel`, `AgeableListModel` and `ListModel`; any other model gets a full-canvas overlay. `TaintOverlayLayer` checks the entity itself |