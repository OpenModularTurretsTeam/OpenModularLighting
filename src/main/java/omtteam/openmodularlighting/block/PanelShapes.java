package omtteam.openmodularlighting.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Outline shapes of the panel lights (fluorescent and grow lights) for each facing. The panel is modelled facing up
 * (lying on the floor, shining up) and turned like the block models: x rotation first, then y rotation, the same
 * table as vanilla's {@code end_rod} blockstate (up: none, down: x 180, north: x 90, south: x 90 y 180, west: x 90 y
 * 270, east: x 90 y 90).
 */
public final class PanelShapes {
    private PanelShapes() {
    }

    /**
     * @param box the panel facing up, in pixels ({@code minX, minY, minZ, maxX, maxY, maxZ})
     */
    public static Map<Direction, VoxelShape> of(double... box) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            shapes.put(facing, rotate(box, facing));
        }
        return shapes;
    }

    private static VoxelShape rotate(double[] box, Direction facing) {
        int[] rotation = rotation(facing);
        double[] a = turn(new double[]{box[0], box[1], box[2]}, rotation);
        double[] b = turn(new double[]{box[3], box[4], box[5]}, rotation);
        return Block.box(Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                Math.max(a[0], b[0]), Math.max(a[1], b[1]), Math.max(a[2], b[2]));
    }

    /** Blockstate rotation {x, y} in degrees for a model facing up. */
    static int[] rotation(Direction facing) {
        return switch (facing) {
            case UP -> new int[]{0, 0};
            case DOWN -> new int[]{180, 0};
            case NORTH -> new int[]{90, 0};
            case SOUTH -> new int[]{90, 180};
            case WEST -> new int[]{90, 270};
            case EAST -> new int[]{90, 90};
        };
    }

    /**
     * Turns a point (in pixels) around the block centre like a blockstate rotation: per 90° of x, up → north
     * ({@code (x, y, z) → (x, z, -y)}); per 90° of y, north → east ({@code (x, y, z) → (-z, y, x)}).
     */
    private static double[] turn(double[] point, int[] rotation) {
        double x = point[0] - 8;
        double y = point[1] - 8;
        double z = point[2] - 8;
        for (int i = 0; i < rotation[0] / 90; i++) {
            double newY = z;
            z = -y;
            y = newY;
        }
        for (int i = 0; i < rotation[1] / 90; i++) {
            double newX = -z;
            z = x;
            x = newX;
        }
        return new double[]{x + 8, y + 8, z + 8};
    }
}
