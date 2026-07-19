package io.github.nie1ix.movewand.platform;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMetadataTest {
    @Test
    void declaresSharedModMetadata() throws IOException {
        InputStream input = getClass().getResourceAsStream("/fabric.mod.json");
        assertNotNull(input);
        String metadata = new String(input.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(metadata.contains("\"id\": \"movewand\""));
        assertTrue(metadata.contains("\"name\": \"MoveWand\""));
        assertTrue(metadata.contains("\"license\": \"All Rights Reserved\""));
        assertTrue(metadata.contains("\"icon\": \"assets/movewand/icon.png\""));
    }
}
