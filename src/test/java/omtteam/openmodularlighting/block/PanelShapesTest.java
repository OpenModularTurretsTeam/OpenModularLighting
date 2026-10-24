package omtteam.openmodularlighting.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelShapesTest {
    private static final double EPSILON = 1E-9;
    /** The grow light: a full panel, 3 px thick. */
    private static final Map<EnumFacing, AxisAlignedBB> PANEL = PanelShapes.of(0, 0, 0, 16, 3, 16);

    /** Asserts a box given in pixels. */
    private static void assertBox(AxisAlignedBB box, double minX, double minY, double minZ, double maxX, double maxY,
                                  double maxZ) {
        AxisAlignedBB expected = new AxisAlignedBB(minX / 16, minY / 16, minZ / 16, maxX / 16, maxY / 16, maxZ / 16);
        assertEquals(expected.minX, box.minX, EPSILON, box + " minX");
        assertEquals(expected.minY, box.minY, EPSILON, box + " minY");
        assertEquals(expected.minZ, box.minZ, EPSILON, box + " minZ");
        assertEquals(expected.maxX, box.maxX, EPSILON, box + " maxX");
        assertEquals(expected.maxY, box.maxY, EPSILON, box + " maxY");
        assertEquals(expected.maxZ, box.maxZ, EPSILON, box + " maxZ");
    }

    @Test
    void thePanelSitsOnTheFaceOppositeItsFacing() {
        assertBox(PANEL.get(EnumFacing.UP), 0, 0, 0, 16, 3, 16);
        assertBox(PANEL.get(EnumFacing.DOWN), 0, 13, 0, 16, 16, 16);
        assertBox(PANEL.get(EnumFacing.NORTH), 0, 0, 13, 16, 16, 16);
        assertBox(PANEL.get(EnumFacing.SOUTH), 0, 0, 0, 16, 16, 3);
        assertBox(PANEL.get(EnumFacing.WEST), 13, 0, 0, 16, 16, 16);
        assertBox(PANEL.get(EnumFacing.EAST), 0, 0, 0, 3, 16, 16);
    }

    @Test
    void aStripTurnsWithTheBlockstateRotation() {
        // the small fluorescent light: a strip along x when lying on the floor
        Map<EnumFacing, AxisAlignedBB> strip = PanelShapes.of(0, 0, 5, 16, 3, 11);
        assertBox(strip.get(EnumFacing.UP), 0, 0, 5, 16, 3, 11);
        assertBox(strip.get(EnumFacing.NORTH), 0, 5, 13, 16, 11, 16);
        assertBox(strip.get(EnumFacing.EAST), 0, 5, 0, 3, 11, 16);
    }

    @Test
    void everyShapeStaysInsideTheBlock() {
        assertEquals(6, PANEL.size());
        AxisAlignedBB block = new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(EPSILON);
        for (Map.Entry<EnumFacing, AxisAlignedBB> shape : PanelShapes.of(5, 0, 0, 11, 3, 16).entrySet()) {
            AxisAlignedBB box = shape.getValue();
            assertTrue(block.contains(new Vec3d(box.minX, box.minY, box.minZ))
                    && block.contains(new Vec3d(box.maxX, box.maxY, box.maxZ)),
                    shape.getKey() + ": " + box);
        }
    }
}
