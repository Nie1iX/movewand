# MoveWand

MoveWand is a Minecraft mod for Fabric and NeoForge that adds a survival-friendly tool for safely moving and rotating existing structures.

It is not a WorldEdit replacement and it does not copy or paste buildings. Use the wand to correct a mechanism's position, free some space, or rotate part of a build without dismantling and rebuilding it by hand.

## Requirements

- Minecraft `1.21.1`
- Java 21
- One supported loader:
  - Fabric Loader `0.16` or newer and Fabric API for Minecraft `1.21.1`
  - NeoForge `21.1.235`

## Installation

1. Install either Fabric Loader with Fabric API, or NeoForge, for Minecraft `1.21.1`.
2. Place the matching MoveWand JAR in the instance's `mods` directory.
3. Launch the game with Java 21. Do not install both loader variants in one instance.

## Building

Fabric and NeoForge are independent Gradle projects that share the code in `src/main`.

```bash
./gradlew -p fabric build
./gradlew -p neoforge build
```

The resulting JARs are written to `fabric/build/libs/` and `neoforge/build/libs/` respectively:

- `movewand-fabric-<mod-version>+mc<minecraft-version>.jar`
- `movewand-neoforge-<mod-version>+mc<minecraft-version>.jar`

## Crafting

The MoveWand recipe uses one stick, two iron ingots, and one diamond. Transformations do not consume durability or hunger.

## Using the wand

1. Hold MoveWand in the main hand.
2. Right-click two blocks to select a box, or use `Shift` + right-click to add or remove individual blocks.
3. Press a movement or rotation key to open the transform preview.
4. Press `Enter` to apply the transform, or `Backspace` / `Esc` to cancel it.

Starting an individual-block selection clears a pending first box corner. The selection overlay is visible only while MoveWand is held in the main hand; press `Delete` to clear it explicitly.

| Default key | Action |
| --- | --- |
| Arrow keys | Move forward, backward, left, or right relative to the player's horizontal view |
| `R` / `V` | Move up / down along world axis `Y` |
| `[` / `]` | Rotate the whole selection around axis `Y` |
| `Enter` | Apply the previewed transform |
| `Backspace` or `Esc` | Cancel the active preview |
| `Delete` | Clear the selection |

All bindings can be changed in Minecraft Controls. With Mod Menu installed, MoveWand's Configure button opens its key-binding settings.

## Safety and limits

- A selection may contain up to 512 blocks.
- The pivot may move up to 16 blocks from its original position.
- The server validates every operation before changing the world. A conflicting destination rejects the whole transform; no partial move is applied.
- A destination may be empty, part of the same moving selection, or a non-source fluid position. Source fluids are blocked.
- Blocks that require support, such as short grass, flowers, torches, and dripstone, must be able to survive at their destination after the complete transform.
- Doors and beds are expanded to include their companion part. A double chest must include both halves; moving only one half is rejected with an explanation.

The first box corner, or the first individually selected block, becomes the pivot. After a successful operation the server updates the selection and pivot to the structure's new coordinates, so the next operation continues from the moved group.

MoveWand preserves `BlockState` and transfers `BlockEntity` data through NBT. This is best effort for third-party blocks: a mod that stores external references to old coordinates can require a dedicated integration.

## Scope

- No copying, pasting, deleting, destination replacement, or mass building.
- Only 90-degree rotations around the vertical `Y` axis.
- No Undo command in the current release scope.
- The supported runtimes are Fabric and NeoForge for Minecraft `1.21.1` on Java 21. Other Minecraft versions, if supported, are released as separate artifacts.

See the [functional specification](docs/functional-spec.md) and [compatibility matrix](docs/compatibility-matrix.md) for the detailed contract.
