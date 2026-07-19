package io.github.nie1ix.movewand.ae2;

import appeng.api.orientation.BlockOrientation;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AE2MoveIntegrationTest {
    @Test
    void rotatesAe2OrientationClockwiseAroundY() {
        assertEquals(BlockOrientation.EAST_UP, AE2MoveIntegration.rotateY(BlockOrientation.NORTH_UP, 1));
        assertEquals(BlockOrientation.SOUTH_UP, AE2MoveIntegration.rotateY(BlockOrientation.NORTH_UP, 2));
    }

    @Test
    void rotatesCableBusPartsAndFacadesToTheirNewSides() {
        CompoundTag data = new CompoundTag();
        data.put("north", part("ae2:terminal"));
        data.put("west", part("ae2:import_bus"));
        data.put("facadeSouth", new CompoundTag());

        AE2MoveIntegration.rotateCableBusSides(data, 1);

        assertEquals("ae2:terminal", data.getCompound("east").getString("id"));
        assertEquals("ae2:import_bus", data.getCompound("north").getString("id"));
        assertFalse(data.contains("west"));
        assertFalse(data.contains("facadeSouth"));
        assertEquals(CompoundTag.class, data.get("facadeWest").getClass());
    }

    private static CompoundTag part(String id) {
        CompoundTag part = new CompoundTag();
        part.putString("id", id);
        return part;
    }
}
