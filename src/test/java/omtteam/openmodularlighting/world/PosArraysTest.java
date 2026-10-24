package omtteam.openmodularlighting.world;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PosArraysTest {
    @Test
    void positionsSurviveTheRoundTrip() {
        List<BlockPos> positions = Arrays.asList(new BlockPos(1, 2, 3), new BlockPos(-30000000, 0, 29999999),
                new BlockPos(0, 255, -1));
        assertEquals(positions, PosArrays.read(PosArrays.write(positions)));
    }

    @Test
    void writesXyzTriples() {
        assertArrayEquals(new int[]{1, 2, 3, -4, 5, -6},
                PosArrays.write(Arrays.asList(new BlockPos(1, 2, 3), new BlockPos(-4, 5, -6))));
    }

    @Test
    void aTrailingIncompletePositionIsIgnored() {
        assertEquals(Collections.singletonList(new BlockPos(1, 2, 3)), PosArrays.read(new int[]{1, 2, 3, 4, 5}));
    }

    @Test
    void emptyStaysEmpty() {
        assertEquals(0, PosArrays.write(Collections.emptyList()).length);
        assertTrue(PosArrays.read(new int[0]).isEmpty());
    }
}
