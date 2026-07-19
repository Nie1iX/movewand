# Add-ons

Optional mod integrations live here. Each integration owns its loader metadata,
compatibility range, version, and tests; the base MoveWand build remains independent.

## Layout

```text
addons/
  <mod>/
    build.gradle              shared add-on version and buildAddon task
    src/                      loader-neutral integration and tests
    fabric/                   Fabric entrypoint and metadata
    neoforge/                 NeoForge entrypoint and metadata
```

The loader-neutral code must use `MoveIntegration` from `common`. Do not add
third-party mod dependencies to the base projects.

## Build

- `./gradlew build` builds only MoveWand for Fabric and NeoForge.
- `./gradlew buildAddons` builds every optional add-on.
- `./gradlew :addons:<mod>:buildAddon` builds one integration.
- `./gradlew :addons:oritech:fabric:runGameTest` runs Oritech's runtime GameTest.

Built add-on JARs are written to `build/addons/`. The `addons` aggregate project
automatically invokes each immediate add-on's `buildAddon` task.

## Adding an integration

1. Add its parent and loader projects to `settings.gradle`.
2. Define the add-on version and compatible MoveWand range in
   `addons/<mod>/build.gradle`, then expose a `buildAddon` task.
3. Keep the implementation under `addons/<mod>/src/` and register it from each
   loader entrypoint.
4. Add loader metadata with the supported game, loader, base-mod, and third-party
   mod versions.
5. Cover NBT/selection rules with unit tests and, when the target mod can run in
   the test environment, an integration GameTest.
