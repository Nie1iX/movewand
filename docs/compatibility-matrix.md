# MoveWand Compatibility Matrix

This document defines the support boundary for the first public alpha. `GameTest covered` means the named scenario has an automated Minecraft `GameTest`; it is not a substitute for manual multiplayer testing in a backed-up world.

The detailed matrix below is covered by the legacy GameTest suite on the `1.21.1`
maintenance line. The current `26.2` Fabric GameTest suite covers paired doors,
overlapping carpets, redstone rotation, wall-torch rotation, flowing-water
destinations, and spawner rejection. The remaining `26.*` cases keep unit-test
and manual-QA coverage until their GameTests are ported.

## Current contract

- `1.21.*` lines require Java 21; `26.*` lines require Java 25. Every release's loader and Minecraft coordinates are embedded in its JAR metadata.
- MoveWand transfers `BlockState` and `BlockEntity` NBT. Before loading NBT at the destination, it writes the destination `x`, `y`, and `z` coordinates.
- A destination may be empty, part of the source selection that is freed by the same operation, or a non-source fluid position. Source fluids are rejected.
- Bedrock, spawners, trial spawners, and blocks in `c:relocation_not_supported`, `forge:relocation_not_supported`, `create:non_movable`, or MoveWand's own denylist tags are rejected before the world changes. Missing tag namespaces do not add a dependency.
- A `BlockEntity` with an NBT `Lock` value is rejected before the operation.
- A selection is limited to 512 blocks, and its pivot may be displaced by at most 16 blocks.
- Generic NBT transfer alone is not a compatibility promise for a third-party mod.
- Optional integrations can expand a selection and rewrite captured `BlockEntity` NBT before it is restored. They are distributed as separate loader-specific addons.

## Matrix

| Area | Status | Automated coverage | Remaining manual checks |
| --- | --- | --- | --- |
| Ordinary vanilla blocks | GameTest covered | Translation, `BlockState` rotation, `facing`, and `axis` | Multiplayer and reconnect behavior |
| Doors and beds | GameTest covered | Companion-part expansion, translation, rotation, and invalid bed destination rejection | Multiplayer behavior |
| Flowers, short grass, torches, and dripstone | GameTest covered | Destination survival and unsupported-target rejection | Unusual modded support rules |
| Chest, barrel, and shulker box | GameTest covered | Inventory NBT is preserved | Custom names, locks, rotation, reopening, and double-chest full move |
| Furnace and brewing stand | GameTest covered | Inventory and active progress data are preserved | Smoker, blast furnace, redstone, and GUI behavior after reconnect |
| Hopper | GameTest covered | Inventory NBT is preserved | Neighbor inventories, redstone, and transfer timing |
| Sign and lectern | GameTest covered | Sign data and lectern book data are preserved | Interaction and rendering after reconnect |
| Item frames and paintings | GameTest covered | Item-frame translation and rotation; 1×1 painting translation | Glow item frames, map items, paintings of every size, and multiplayer rendering |
| Locked `BlockEntity` | GameTest covered | Rejection before world mutation | Mod-specific lock conventions |
| Denylist-tagged blocks | GameTest covered | Rejection for relocation opt-out tags | Mod-specific tag coverage |
| Oritech `1.2.9` multiblocks | `mc1.21.1` maintenance line only | GameTest: controller/core selection expansion and coordinate-reference NBT rewrite | Translation, rotation, GUI, recipes, reconnect, and multiplayer on Fabric and NeoForge |
| Create ordinary blocks | No integration claim | Generic denylist is recognized | Kinetic networks, storage, smart blocks, and rotation require a dedicated integration review |
| AE2 | No integration claim | None | Cables, machines, storage, and network reconnect require a dedicated integration review |

## Verification sequence

1. Back up the test world.
2. For each supported vanilla scenario, test translation, `Y` rotation, a second transform, leaving the world, and rejoining it.
3. Test multiplayer: one player performs the operation and another observes the resulting blocks and inventories after reconnecting.
4. Test every modded integration in an isolated world. If a block is unsafe, add its block id to a denylist data pack until a dedicated integration exists.

## Manual QA checklist

Run this checklist on both Fabric and NeoForge in a backed-up world. For every successful case, test translation, `Y` rotation, a second move, and source/destination overlap where it makes sense.

- [ ] Selection: a single stone block; a 2×2×2 structure; a selection near the 512-block limit; offsets on all three axes; a blocked destination; an unloaded destination; an unmovable block is highlighted and named in the rejection message.
- [ ] Block states: stairs, slabs, logs, glazed terracotta, observers, pistons, and redstone components keep their `facing`, `axis`, or other orientation after translation and rotation.
- [ ] Support-dependent blocks: flowers, short grass, carpets, pressure plates, torches, buttons, levers, rails, and pointed dripstone. Move them once with their support selected and once without it; the latter must follow vanilla breaking behavior without duplicating items.
- [ ] Redstone: a line containing dust, repeaters, comparators, lamps, observers, pistons, and a target block. Test a multi-block selection, overlap with the source, rotation, and that no duplicate drops appear.
- [ ] Multi-block structures: doors, beds, tall plants, double chests, and a chest adjacent to a double chest. Test selecting one part and every part where applicable.
- [ ] Block entities: chest, barrel, shulker box, ender chest, furnace, smoker, blast furnace, brewing stand, hopper, sign, lectern, and decorated pot. Check inventories, custom names, active progress, redstone, GUI opening, and reconnect behavior.
- [ ] Hanging entities: item frames, glow item frames, and paintings in several sizes. Select every supporting block, then verify the entity moves and rotates without a duplicate item or a delayed drop at the old position. Repeat with an item and a map inside a frame.
- [ ] Fluids and terrain: move into flowing water, near source water and lava, and across uneven terrain; source fluid positions must still be rejected.
- [ ] Persistence and multiplayer: reconnect after a move; have another player observe the operation; repeat with both players holding or viewing the same container and item frame.
- [ ] Modded blocks: test each installed mod in a separate world before using it in survival. A passed vanilla case does not imply third-party compatibility.
- [ ] Oritech `1.2.9` on the `mc1.21.1` maintenance line: with the matching MoveWand Oritech addon installed, select only one controller or core of each tested multiblock. Verify that all of its cores are added to the selection, then test translation, rotation, GUI interaction, active processing, reconnect, and multiplayer observation.

## External contracts used as references

- Movable Block Entities uses `c:relocation_not_supported` as an opt-out contract; MoveWand respects it.
- Create defines `create:non_movable` and uses `forge:relocation_not_supported` for blocks that must not move; MoveWand respects both. `create:safe_nbt` concerns schematic printing and is not a general relocation guarantee.
- AE2 exposes a move strategy based on `beginMove` / `completeMove`. Its default path creates a new `BlockEntity` at the target through `loadStatic`, which differs from MoveWand's generic NBT path. MoveWand has no AE2 dependency until an optional integration is verified.
- WorldEdit moves a selection only with explicit `-s`. MoveWand always moves its selection after a successful operation, because the wand is intended for sequential adjustments to the same group.
