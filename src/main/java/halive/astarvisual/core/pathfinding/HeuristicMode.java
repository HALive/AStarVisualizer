package halive.astarvisual.core.pathfinding;

import java.awt.Point;

public enum HeuristicMode {
    MANHATTEN("Manhatten Heuristik", (a, b) -> {
        Point p = new Point(a.x - b.x, a.y - b.y);
        return Math.abs(p.x) + Math.abs(p.y);
    }),
    EUCLID("Euklidsche Heuristik", (a, b) -> {
        Point p = new Point(a.x - b.x, a.y - b.y);
        return Math.sqrt(Math.pow(p.x, 2) + Math.pow(p.y, 2));
    }),
    EUCLIDSQ("Quadrierte Euklidsche Heuristik", (a, b) -> {
        Point p = new Point(a.x - b.x, a.y - b.y);
        return Math.pow(p.x, 2) + Math.pow(p.y, 2);
    }),
    OCTILE("Oktile heuristik", (a, b) -> {
        Point p = new Point(a.x - b.x, a.y - b.y);
        double f = Math.sqrt(2) - 1;
        return (p.x < p.y) ? f * p.x + p.y : f * p.y + p.x;
    }),
    CHEBYSHEW("Chebyshev Heuristik", (a, b) -> {
        Point p = new Point(a.x - b.x, a.y - b.y);
        return Math.max(p.x, p.y);
    }),
    NONE("Keine Heuristik", (a, b) -> 0);

    private String name;
    private IHeuristicHandler handler;


    HeuristicMode(String name, IHeuristicHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    @Override
    public String toString() {
        return name;
    }

    public double calculateH(Point pos, Point endPos) {
        return handler.getH(pos, endPos);
    }

    private interface IHeuristicHandler {

        double getH(Point a, Point b);
    }
}
