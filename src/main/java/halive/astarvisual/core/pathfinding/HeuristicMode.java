package halive.astarvisual.core.pathfinding;

import java.awt.Point;

public enum HeuristicMode {
    MANHATTEN("Manhatten Heuristik", new HeurHandler() {
        @Override
        public double getH(Point tPos, Point endPos) {
            Point p = new Point(tPos.x-endPos.x, tPos.y-endPos.y);
            return Math.abs(p.x)+Math.abs(p.y);
        }
    }),
    EUCLID("Euklidsche Heuristik", new HeurHandler() {
        @Override
        public double getH(Point tPos, Point endPos) {
            Point p = new Point(tPos.x-endPos.x, tPos.y-endPos.y);
            return Math.sqrt(Math.pow(p.x, 2)+Math.pow(p.y, 2));
        }
    }),
    EUCLIDSQ("Quadrierte Euklidsche Heuristik", new HeurHandler() {
        @Override
        public double getH(Point tPos, Point endPos) {
            Point p = new Point(tPos.x-endPos.x, tPos.y-endPos.y);
            return Math.pow(p.x, 2)+Math.pow(p.y, 2);
        }
    }),
    OCTILE("Oktile heuristik", new HeurHandler() {
        @Override
        public double getH(Point tPos, Point endPos) {
            Point p = new Point(tPos.x-endPos.x, tPos.y-endPos.y);
            double f = Math.sqrt(2)-1;
            return (p.x < p.y) ?f*p.x+p.y : f*p.y+p.x;
        }
    }),
    CHEBYSHEW("Chebyshev Heuristik", new HeurHandler() {
        @Override
        public double getH(Point tPos, Point endPos) {
            Point p = new Point(tPos.x-endPos.x, tPos.y-endPos.y);
            return Math.max(p.x, p.y);
        }
    }),
    NONE("Keine Heuristik", new HeurHandler() {
        @Override
        public double getH(Point tPos, Point endPos) {
            return 0;
        }
    });

    private String name;
    private HeurHandler handler;


    @Override
    public String toString() {
        return name;
    }

    public double calculateH(Point pos, Point endPos)  {
        return handler.getH(pos, endPos);
    }

    HeuristicMode(String name, HeurHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    private static interface HeurHandler {
        double getH(Point tPos, Point endPos);
    }
}
