package halive.astarvisual.core.mapgenerator;

import java.awt.Point;
import java.util.Random;

public class WhiteNoiseGenerator extends MapGenerator {

    private boolean[][] map;

    public WhiteNoiseGenerator(Random rnd) {
        super(rnd);
    }

    @Override
    public boolean[][] generateMap(int w, int h) {
        boolean[][] map = new boolean[w][h];

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                map[x][y] = !(rnd.nextInt(100) > 70);
            }
        }
        this.map = map;
        return map;
    }

    private Point getRandomWalkablePos() {
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
        return getRandomWalkablePos();
    }

    @Override
    public Point getEndPos() {
        return getRandomWalkablePos();
    }

    @Override
    public void reset() {
        map = null;
    }
}
