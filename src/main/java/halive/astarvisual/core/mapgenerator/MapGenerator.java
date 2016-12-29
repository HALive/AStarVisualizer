package halive.astarvisual.core.mapgenerator;

import java.awt.Point;
import java.util.Random;

public abstract class MapGenerator {

    protected Random rnd;

    public MapGenerator(Random rnd) {
        this.rnd = rnd;
    }

    public abstract boolean[][] generateMap(int w, int h);

    public abstract Point getStartPos();

    public abstract Point getEndPos();

    public abstract void reset();
}
