package io.github.illuminatijoe.voxelgame.util;

public class VoxelUtils {
    public static int manhattanDistance(int xs, int ys, int zs, int xe, int ye, int ze) {
        int xDis = Math.abs(xs - xe);
        int yDis = Math.abs(ys - ye);
        int zDis = Math.abs(zs - ze);

        return xDis + yDis + zDis;
    }
}
