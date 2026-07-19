package io.github.nie1ix.movewand.platform;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeMetadataTest {
    private static final Path METADATA = Path.of("neoforge/src/main/resources/META-INF/neoforge.mods.toml");

    @Test
    void declaresMoveWandWithItsPixelIcon() throws IOException {
        String metadata = Files.readString(METADATA);

        assertTrue(metadata.contains("modId=\"movewand\""));
        assertTrue(metadata.contains("logoFile=\"assets/movewand/icon.png\""));
        assertTrue(metadata.contains("logoBlur=false"));
    }
}
