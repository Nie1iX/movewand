package io.github.nie1ix.movewand.platform;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeMetadataTest {
    @Test
    void declaresMoveWandWithItsPixelIcon() throws IOException {
        InputStream input = getClass().getResourceAsStream("/META-INF/neoforge.mods.toml");
        assertNotNull(input);
        String metadata = new String(input.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(metadata.contains("modId=\"movewand\""));
        assertTrue(metadata.contains("license=\"MIT\""));
        assertTrue(metadata.contains("logoFile=\"assets/movewand/icon.png\""));
        assertTrue(metadata.contains("logoBlur=false"));
    }
}
