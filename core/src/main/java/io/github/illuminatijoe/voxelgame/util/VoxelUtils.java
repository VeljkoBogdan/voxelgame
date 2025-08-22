package io.github.illuminatijoe.voxelgame.util;

import com.badlogic.gdx.math.Vector3;

public class VoxelUtils {
    public static int manhattanDistance(int xs, int ys, int zs, int xe, int ye, int ze) {
        int xDis = Math.abs(xs - xe);
        int yDis = Math.abs(ys - ye);
        int zDis = Math.abs(zs - ze);

        return xDis + yDis + zDis;
    }

    public static int squareDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        int dz = Math.abs(z1 - z2);

        return Math.max(Math.max(dx, dy), dz);
    }

}
