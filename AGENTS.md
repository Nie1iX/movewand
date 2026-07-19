# MoveWand Project Instructions

## Communication and Git

- Keep code identifiers, commands, framework names, and other technical terms verbatim.
- Establish claims about the repository, builds, tests, CI, and releases from live evidence. Distinguish an executed check from an inspection or an assumption.
- Work directly on a maintenance branch only when the task targets that version line. Do not create a feature branch unless requested; if one is requested, use `feat/*` without a username prefix.
- Maintenance branches use only the Minecraft version: `26.2`, `1.21.1`. Do not prefix branch names with `mc`.
- Keep commit messages factual and decision-oriented. Do not add `Tested:` trailers or any `Co-authored-by` attribution. Report verification in the handoff instead.

## Version Lines and Layout

- The core MoveWand mod is the primary product. Loader-specific integrations and third-party support are secondary optional add-ons.
- `main` is the current core line for Minecraft 26.2. `1.21.1` is a maintained line for Minecraft 1.21.1 and the Oritech add-on.
- Keep shared code in `common/`, Fabric-only code and resources in `fabric/`, and NeoForge-only code and resources in `neoforge/`. Do not put loader-specific code back into a shared source tree.
- Keep optional integrations below `addons/<name>/`, with separate Fabric and NeoForge modules where needed. Put integration logic in dedicated classes rather than mixing it into core move code.
- Core builds must not build add-ons by default. Core artifacts go to `build/mods/`; add-on artifacts go to `build/addons/`.
- Keep an add-on's version and compatible MoveWand range in that add-on's own configuration, never in root `gradle.properties`.

## Versions, Artifacts, and Releases

- Include the Minecraft version in the mod version, for example `1.0.0+mc26.2`.
- Name loader artifacts explicitly, for example `movewand-fabric-1.0.0+mc26.2.jar` and `movewand-neoforge-1.0.0+mc26.2.jar`.
- Use release tags in the form `mc<minecraft-version>-<mod-version>`, for example `mc26.2-1.0.0`.
- A matching tag triggers the release workflow, which publishes the two non-source JARs to GitHub Releases. Do not introduce GitHub Packages for ordinary mod releases.

## CI and Verification

- Keep CI with the code line it tests. `main` builds the 26.2 core with Java 25. `1.21.1` builds its core and Oritech add-on with Java 21.
- Do not leave an add-on workflow on `main` after the add-on has been removed from that Gradle build. The Oritech workflow belongs to `1.21.1`.
- Before a release, build both Fabric and NeoForge variants. Run the narrowest relevant automated checks first, then the full Gradle build when the change crosses loaders or affects packaging.
- For movement changes, use the manual QA checklist in `docs/compatibility-matrix.md` in addition to automated tests. Test both loaders, translation, Y rotation, overlap, reconnect, multiplayer observation, and relevant third-party mods.

## Product Constraints

- A selected door, bed, or two-block plant must include all of its parts. A double chest is valid only when both halves are selected.
- Unsupported or immovable blocks must be visibly marked in the selection and identified by name in rejection feedback.
- Support-dependent and redstone blocks (including carpets, dust, repeaters, and comparators) must not break, drop, or duplicate when a valid structure is moved. Cover rotations and multi-block moves when changing this logic.
- Treat third-party multiblock compatibility as an add-on concern. Do not claim generic NBT transfer makes a mod compatible; use a dedicated integration or denylist unsupported blocks.
- Keep Mod Menu links to the Modrinth website and GitHub issues. Do not expose keybinding configuration through Mod Menu while its rendering defect remains unresolved.
- Do not add speculative projection fixes for beds, chests, or Ender chests. Validate projection changes visually on both loaders instead.
