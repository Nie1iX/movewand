# MoveWand

MoveWand is a Minecraft mod for Fabric and NeoForge that adds a survival-friendly tool for safely moving and rotating existing structures.

It is not a WorldEdit replacement and it does not copy or paste buildings. Use the wand to correct a mechanism's position, free some space, or rotate part of a build without dismantling and rebuilding it by hand.

## Requirements

Choose the MoveWand JAR that matches the Minecraft version and loader in the
instance. The complete downloadable list is on the
[GitHub Releases page](https://github.com/Nie1iX/movewand/releases).

| Minecraft versions | Java | Loaders |
| --- | --- | --- |
| `1.21`, `1.21.1`–`1.21.11` | 21 | Fabric and NeoForge |
| `26.1`, `26.1.1`, `26.1.2`, `26.2` | 25 | Fabric and NeoForge |

Fabric installations also require Fabric API for the matching Minecraft
version. Install only one MoveWand loader variant in an instance.

## Installation

1. Install either Fabric Loader with Fabric API, or NeoForge, for the chosen Minecraft version.
2. Place the matching MoveWand JAR in the instance's `mods` directory.
3. Launch the game with the Java version from the table. Do not install both loader variants.

The Oritech `1.2.9` addon remains on the [`1.21.1` maintenance branch](https://github.com/Nie1iX/movewand/tree/1.21.1); it is not released for the `26.*` lines.

## Building

Fabric and NeoForge are subprojects of one root Gradle build. Their shared source code is in `common/src`.

```bash
./gradlew build
```

This builds only MoveWand. Build every optional integration with:

```bash
./gradlew buildAddons
```

MoveWand JARs are written to `build/mods/`:

- `movewand-fabric-<mod-version>+mc<minecraft-version>.jar`
- `movewand-neoforge-<mod-version>+mc<minecraft-version>.jar`

Addon JARs are written to `build/addons/`:

- `movewand-oritech-fabric-<addon-version>+mc<minecraft-version>.jar`
- `movewand-oritech-neoforge-<addon-version>+mc<minecraft-version>.jar`

Addons have their own version because they can be released independently of MoveWand. Each addon declares the compatible MoveWand version range in its loader metadata.

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

MoveWand preserves `BlockState` and transfers `BlockEntity` data through NBT. This is best effort for third-party blocks: a mod that stores external references to old coordinates can require a dedicated integration. Integrations are separate loader-specific addons and do not add a runtime dependency to the base mod.

## Scope

- No copying, pasting, deleting, destination replacement, or mass building.
- Only 90-degree rotations around the vertical `Y` axis.
- No Undo command in the current release scope.
- Every supported Minecraft version is released as separate Fabric and NeoForge artifacts.

See the [functional specification](docs/functional-spec.md) and [compatibility matrix](docs/compatibility-matrix.md) for the detailed contract.
