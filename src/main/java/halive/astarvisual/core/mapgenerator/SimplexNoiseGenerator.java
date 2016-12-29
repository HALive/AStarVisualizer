package halive.astarvisual.core.mapgenerator;

import halive.astarvisual.AStarVisualizer;

import java.awt.Point;
import java.util.Random;

/**
 * Generation Parts Copied from:
 * http://staffwww.itn.liu.se/~stegu/simplexnoise/simplexnoise.pdf
 */
public class SimplexNoiseGenerator extends MapGenerator {

    private static int grad3[][] = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
            {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    private static int p[] = new int[256];
    private static int perm[] = new int[512];

    private ValueConverter converter;

    private boolean[][] map;

    public SimplexNoiseGenerator(Random rnd, ValueConverter c) {
        super(rnd);
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        shuffle(rnd, p, rnd.nextInt(256) + 1);
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
        this.rnd = rnd;
        this.converter = c;
    }

    @Override
    public boolean[][] generateMap(int w, int h) {
        int[][] intmap = generateMap(w, h, (int) (AStarVisualizer.SQUARE_SIZE), 2);
        boolean[][] boolmap = new boolean[w][h];
        for (int x = 0; x < intmap.length; x++) {
            for (int y = 0; y < intmap[x].length; y++) {
                boolmap[x][y] = intmap[x][y] == 0;
            }
        }
        map = boolmap;
        return boolmap;
    }

    private Point getRandomPos() {
        int x = -1;
        int y = -1;

        do {
            x = rnd.nextInt(map.length);
            y = rnd.nextInt(map[x].length);
        } while (!map[x][y]);

        return new Point(x, y);
    }

    @Override
    public Point getStartPos() {
        return getRandomPos();
    }

    @Override
    public Point getEndPos() {
        return getRandomPos();
    }

    @Override
    public void reset() {
        map = null;
    }

    public int[][] generateMap(int width, int height, int rectSize, int max) {
        int[][] map = new int[width][height];

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                double x = (i / ((double) rectSize));
                double y = (j / ((double) rectSize));
                double val = noise(x, y);
                map[i][j] = converter.convert(val, max);
            }
        }

        return map;
    }

    private int fastfloor(double x) {
        return x > 0 ? (int) x : (int) x - 1;
    }

    private double dot(int g[], double x, double y) {
        return g[0] * x + g[1] * y;
    }

    public double noise(double xin, double yin) {
        double n0, n1, n2;
        final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
        double s = (xin + yin) * F2;
        int i = fastfloor(xin + s);
        int j = fastfloor(yin + s);
        final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        }
        else {
            i1 = 0;
            j1 = 1;
        }
        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii + perm[jj]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1]] % 12;
        int gi2 = perm[ii + 1 + perm[jj + 1]] % 12;
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0);
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
        }
        return 70.0 * (n0 + n1 + n2);
    }

    private void shuffle(Random rnd, int[] perm, int cycles) {
        for (int c = 0; c < cycles; c++) {
            for (int i = 0; i < perm.length; i++) {
                int j = -1;
                do {
                    j = rnd.nextInt(perm.length);
                } while (j == i);
                swap(perm, i, j);
            }
        }
    }

    private void swap(int[] d, int i, int j) {
        int s = d[i];
        d[i] = d[j];
        d[j] = s;
    }

    public enum ValueConverter {
        ABSOULTE((v, m) -> (int) (Math.abs(v) * m)),
        ABSOULTE_INVERSE((v, m) -> (int) ((m - 1) - Math.abs(v) * m)),
        SUBTRACT_FROM_1((v, m) -> (int) ((1 - v) * (m / 2))),
        SUBTRACT_FROM_1_INVERSE((v, m) -> (m - 1) - ((int) ((1 - v) * (m / 2))));

        private IConverter converter;

        ValueConverter(IConverter converter) {
            this.converter = converter;
        }

        public int convert(double value, int max) {
            return converter.convert(value, max);
        }

        private interface IConverter {

            int convert(double value, int max);
        }
    }
}

