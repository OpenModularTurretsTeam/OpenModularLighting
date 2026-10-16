package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.math.Facing;
import omtteam.openmodularlighting.core.math.Pos;

import java.util.HashSet;
import java.util.Set;

/**
 * Reference implementation: the 1.7.10 beam loops, copied as literally as possible from
 * {@code oldmod/.../tileentity/TileEntityMetaFloodlight}, {@code TileEntitySmallFloodlight},
 * {@code TileEntityGrowLight} and {@code util/MathUtil}. Only the world access is replaced; placing a phantom
 * light becomes adding the position to the result. {@link BeamCaster} must produce the same positions.
 */
final class Legacy1710Beams {
    private Legacy1710Beams() {
    }

    static Set<Pos> straightSource(Pos o, Facing orientation, int rangeStraightFloodlight, LightWorld world) {
        Set<Pos> lit = new HashSet<>();
        for (int i = 1; i <= rangeStraightFloodlight; i++) {
            int x = o.x() + orientation.dx() * i;
            int y = o.y() + orientation.dy() * i;
            int z = o.z() + orientation.dz() * i;
            if (!setLightChecked(world, lit, x, y, z)) {
                break;
            }
        }
        return lit;
    }

    static Set<Pos> wideConeSource(Pos o, Facing orientation, int rangeConeFloodlight, LightWorld world) {
        Set<Pos> lit = new HashSet<>();
        boolean[] failedBeams = new boolean[9];
        // 1.7.10: getBlock(...).isOpaqueCube(); an unloaded block read as air there
        if (world.cellAt(new Pos(o.x() + orientation.dx(), o.y() + orientation.dy(), o.z() + orientation.dz()))
                == Cell.OPAQUE) {
            return lit;
        }
        for (int j = 0; j <= 16; j++) {
            if (j <= 8) {
                for (int i = 1; i <= rangeConeFloodlight / 4; i++) {
                    int b = 0;
                    int c = 0;
                    switch (j) {
                        case 0: b += i; break;
                        case 1: b -= i; break;
                        case 2: c += i; break;
                        case 3: c -= i; break;
                        case 4: b += i; c += i; break;
                        case 5: b += i; c -= i; break;
                        case 6: b -= i; c += i; break;
                        case 7: b -= i; c -= i; break;
                        default: break;
                    }
                    int[] rotatedCoords = rotate(i, b, c, orientation);
                    int x = o.x() + rotatedCoords[0];
                    int y = o.y() + rotatedCoords[1];
                    int z = o.z() + rotatedCoords[2];
                    if (!setLightChecked(world, lit, x, y, z)) {
                        if (i < 4) {
                            failedBeams[j] = true;
                        }
                        break;
                    }
                }
            } else if (!failedBeams[j - 9]) {
                for (int i = 4; i <= rangeConeFloodlight / 4; i++) {
                    int b = 0;
                    int c = 0;
                    switch (j) {
                        case 9: b += i / 2; break;
                        case 10: b -= i / 2; break;
                        case 11: c += i / 2; break;
                        case 12: c -= i / 2; break;
                        case 13: b += i / 2; c += i / 2; break;
                        case 14: b += i / 2; c -= i / 2; break;
                        case 15: b -= i / 2; c += i / 2; break;
                        case 16: b -= i / 2; c -= i / 2; break;
                        default: break;
                    }
                    int[] rotatedCoords = rotate(i, b, c, orientation);
                    int x = o.x() + rotatedCoords[0];
                    int y = o.y() + rotatedCoords[1];
                    int z = o.z() + rotatedCoords[2];
                    if (!setLightChecked(world, lit, x, y, z)) {
                        break;
                    }
                }
            }
        }
        return lit;
    }

    static Set<Pos> narrowConeSource(Pos o, Facing orientation, int rangeConeFloodlight, LightWorld world) {
        Set<Pos> lit = new HashSet<>();
        boolean[] failedBeams = new boolean[9];
        for (int j = 0; j <= 16; j++) {
            if (j <= 8) {
                for (int i = 1; i <= rangeConeFloodlight; i++) {
                    if (i == 1) {
                        int x = o.x() + orientation.dx();
                        int y = o.y() + orientation.dy();
                        int z = o.z() + orientation.dz();
                        if (!setLightChecked(world, lit, x, y, z)) {
                            return lit;
                        }
                    }
                    int a = i;
                    int b = 0;
                    int c = 0;
                    switch (j) {
                        case 0: b += i / 2; break;
                        case 1: b -= i / 2; break;
                        case 2: c += i / 2; break;
                        case 3: c -= i / 2; break;
                        case 4: b += i / 2; c += i / 2; break;
                        case 5: b += i / 2; c -= i / 2; break;
                        case 6: b -= i / 2; c += i / 2; break;
                        case 7: b -= i / 2; c -= i / 2; break;
                        default: break;
                    }
                    int[] rotatedCoords = rotate(a, b, c, orientation);
                    int x = o.x() + rotatedCoords[0];
                    int y = o.y() + rotatedCoords[1];
                    int z = o.z() + rotatedCoords[2];
                    if (!setLightChecked(world, lit, x, y, z)) {
                        if (i < 8) {
                            failedBeams[j] = true;
                        }
                        break;
                    }
                }
            } else if (!failedBeams[j - 9]) {
                for (int i = 8; i <= rangeConeFloodlight; i++) {
                    int a = i;
                    int b = 0;
                    int c = 0;
                    switch (j) {
                        case 9: b += i / 4; break;
                        case 10: b -= i / 4; break;
                        case 11: c += i / 4; break;
                        case 12: c -= i / 4; break;
                        case 13: b += i / 4; c += i / 4; break;
                        case 14: b += i / 4; c -= i / 4; break;
                        case 15: b -= i / 4; c += i / 4; break;
                        case 16: b -= i / 4; c -= i / 4; break;
                        default: break;
                    }
                    int[] rotatedCoords = rotate(a, b, c, orientation);
                    int x = o.x() + rotatedCoords[0];
                    int y = o.y() + rotatedCoords[1];
                    int z = o.z() + rotatedCoords[2];
                    if (!setLightChecked(world, lit, x, y, z)) {
                        break;
                    }
                }
            }
        }
        return lit;
    }

    static Set<Pos> smallSource(Pos o, Facing orientation, LightWorld world) {
        Set<Pos> lit = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            int a = 0;
            int b = 0;
            int c = 0;
            if (i == 0) {
                a = 1;
            } else if (i == 1) {
                b = 1;
            } else if (i == 2) {
                b = -1;
            } else if (i == 3) {
                c = 1;
            } else {
                c = -1;
            }
            int[] rotatedCoords = rotate(a, b, c, orientation);
            setLightChecked(world, lit, o.x() + rotatedCoords[0], o.y() + rotatedCoords[1], o.z() + rotatedCoords[2]);
        }
        return lit;
    }

    static Set<Pos> growSource(Pos o, Facing orientation, LightWorld world) {
        Set<Pos> lit = new HashSet<>();
        int[] rotatedCoords = rotate(1, 0, 0, orientation);
        setLightChecked(world, lit, o.x() + rotatedCoords[0], o.y() + rotatedCoords[1], o.z() + rotatedCoords[2]);
        return lit;
    }

    /** {@code setLightChecked(x, y, z, remove = false)}. */
    private static boolean setLightChecked(LightWorld world, Set<Pos> lit, int x, int y, int z) {
        Pos pos = new Pos(x, y, z);
        Cell cell = world.cellAt(pos);
        if (cell == Cell.UNLOADED) { // !worldObj.blockExists(x, y, z)
            return false;
        }
        if (cell == Cell.AIR) { // setLight: place a phantom light
            lit.add(pos);
        } else if (cell == Cell.PHANTOM_LIGHT) { // addSource
            lit.add(pos);
        } else if (cell == Cell.OPAQUE) {
            return false;
        }
        return true;
    }

    /** {@code MathUtil.rotate}. */
    static int[] rotate(int x, int y, int z, Facing direction) {
        int[] result = new int[3];
        switch (direction) {
            case DOWN:
                result[0] = y;
                result[1] = -x;
                result[2] = z;
                break;
            case UP:
                result[0] = -y;
                result[1] = x;
                result[2] = z;
                break;
            case NORTH:
                result[0] = z;
                result[1] = y;
                result[2] = -x;
                break;
            case SOUTH:
                result[0] = -z;
                result[1] = y;
                result[2] = x;
                break;
            case WEST:
                result[0] = -x;
                result[1] = y;
                result[2] = z;
                break;
            case EAST:
                result[0] = x;
                result[1] = y;
                result[2] = z;
                break;
        }
        return result;
    }
}
