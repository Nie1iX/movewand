# Maintaining Minecraft Version Lines

MoveWand keeps one branch per supported Minecraft version. `main` is the
current core line; maintenance branches are named only after their Minecraft
version, for example `1.21.5` or `26.1.2`.

## Adding a patch line

1. Branch from the nearest maintained version line.
2. Update `minecraft_version`, its range, Fabric API, and NeoForge coordinates
   in `gradle.properties`.
3. Update the branch filters and artifact name in `.github/workflows/build.yml`.
4. Run `./gradlew build --no-daemon --max-workers=1 --console=plain` with the
   Java version required by that line.
5. Push the branch and confirm its Build workflow succeeds.
6. Create a tag named `mc<minecraft-version>-<mod-version>`, then push that tag
   by itself. GitHub Actions does not create individual push events when too
   many tags are pushed together.
7. Verify the GitHub Release contains exactly the Fabric and NeoForge JARs.

## Release publication

The tag workflow creates the GitHub Release and, when the relevant repository
secrets are configured, uploads separate Fabric and NeoForge versions to
Modrinth and CurseForge. The Fabric Modrinth upload declares Fabric API as a
required dependency; the NeoForge upload has no Fabric API dependency.

`MODRINTH_TOKEN` needs the Modrinth `VERSION_CREATE` scope. `CURSEFORGE_TOKEN`
is an API token created in the CurseForge author dashboard. A missing token
skips only that platform's publishing step, so GitHub Releases remain usable
during local development or before either secret is configured.
