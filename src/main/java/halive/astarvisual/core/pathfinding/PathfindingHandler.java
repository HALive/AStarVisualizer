package halive.astarvisual.core.pathfinding;

import halive.astarvisual.AStarVisualizer;
import halive.astarvisual.core.ui.GridRenderCanvas;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

public class PathfindingHandler implements Runnable {

    private static final Point[] ADD_MOVE_POSITIONS = {new Point(1, 0),
            new Point(0, 1), new Point(-1, 0), new Point(0, -1),
            new Point(1, 1), new Point(-1, -1), new Point(-1, 1), new Point(1, -1)};

    private GridRenderCanvas canvas;

    private List<Tile> openList;
    private List<Tile> closedList;

    private boolean running = false;

    public PathfindingHandler(GridRenderCanvas canvas) {
        this.canvas = canvas;
    }

    private void init() {
        openList = new LinkedList<Tile>();
        closedList = new LinkedList<Tile>();
        clearTiles();
        Tile start = canvas.getStartTile();
        start.setAStarValues(null, canvas.getEndPos(), 0);
        addSurroundingTilesToOpenList(start);
    }

    private void clearTiles() {
        Tile[][] ts = canvas.getTiles();
        for (int x = 0; x < ts.length; x++) {
            for (int y = 0; y < ts[x].length; y++) {
                Tile t = ts[x][y];
                if (t.getState() != null && t.getState() != Tile.NodeState.WALL) {
                    changeTileState(t, Tile.NodeState.UNKNOWN);
                }
                t.clearAStarValues();
            }
        }
    }

    @Override
    public void run() {
        canvas.setStatus("Initialisiere...");
        running = true;
        canvas.setAllowDrawing(false);
        init();
        try {
            searchPath();
        } catch (NullPointerException e) {
            canvas.setStatus("Kein Weg gefunden.");
        }
        running = false;
        canvas.setAllowDrawing(true);
    }

    private void searchPath() {
        Tile knot = null;
        long startTIme = System.currentTimeMillis();
        long endTime = -1;
        do {
            knot = pullMinTileFromList(openList);
            if (knot != null && knot.isEndNode()) {
                visualizePath(knot, Tile.NodeState.CURRENT_PATH);
                double pathLength = Math.round(knot.getG() * 100) / 100.0D;
                endTime = System.currentTimeMillis() - startTIme;
                canvas.setStatus("Pfad gefunden! Länge: " + pathLength + " Gebrauchte Zeit: " + endTime + " MS");
                return;
            }

            addKnotToClosedList(knot);

            addSurroundingTilesToOpenList(knot);
            if (knot != null) {
                canvas.setStatus("Betrachte (" + knot.getPosition().x + "|" + knot.getPosition().y + ")");
            }
            try {
                Thread.sleep(AStarVisualizer.DELAY_MS);
            } catch (InterruptedException e) {
            }
        } while (!openList.isEmpty());
        endTime = System.currentTimeMillis() - startTIme;
        canvas.setStatus("Kein Weg gefunden. Gebrauchte Zeit: " + endTime + " MS");
    }

    private void visualizePath(Tile knot, Tile.NodeState s) {
        Tile t = knot;
        Tile.NodeState nodeState = s;
        do {
            if (t != null) {
                if (s == null) {
                    if (closedList.contains(t)) {
                        nodeState = Tile.NodeState.CLOSED;
                    } else {
                        nodeState = Tile.NodeState.DISCOVERED;
                    }
                }
                changeTileState(t, nodeState);
                t = t.getPrior();
            }
        } while (t != null && !t.getPosition().equals(canvas.getStartPos()));
    }

    private void addSurroundingTilesToOpenList(Tile k) {
        Tile[][] ts = canvas.getTiles();
        Point p = k.getPosition();
        for (int i = 0; i < ADD_MOVE_POSITIONS.length; i++) {
            Point a = ADD_MOVE_POSITIONS[i];
            int x = p.x + a.x;
            int y = p.y + a.y;

            boolean add = true;
            double cost = 1;

            if (a.x != 0 && a.y != 0) {
                cost = Math.sqrt(2);
                if (((x - 1) >= 0 && (y - 1) >= 0) &&
                        ((x + 1) < ts.length && (y + 1) < ts[0].length)) {
                    Point prior = k.getPosition();
                    Point[] adjTiles = {new Point(x, p.y), new Point(p.x, y)};
                    int cntWalls = 0;
                    for (Point point : adjTiles) {
                        if (ts[point.x][point.y].getState() == Tile.NodeState.WALL) {
                            cntWalls++;
                        }
                    }
                    if (cntWalls > 1) {
                        add = false;
                    }
                }
            }
            if (add) {
                addTileToOpenList(x, y, p, k, cost, ts);
            }
        }
    }

    private void addTileToOpenList(int x, int y, Point p, Tile k, double cost, Tile[][] ts) {
        if (!(p.x == x && p.y == y) &&
                (x >= 0 && y >= 0) &&
                (x < ts.length && y < ts[0].length)) {
            Tile t = ts[x][y];
            if (t.isWalkable() && !openList.contains(t) && !closedList.contains(t)) {
                t.setAStarValues(k, canvas.getEndPos(), k.getG() + cost);
                changeTileState(t, Tile.NodeState.DISCOVERED);
                addKnotToOpenList(t);
            } else if (openList.contains(t)) {
                double gk = k.getG() + cost;
                double gt = t.getG();
                if (gk < gt) {
                    t.setG(gk);
                    t.setPrior(k);
                }
            }
        }
    }

    private void changeTileState(Tile t, Tile.NodeState s) {
        if (!(t.getState() == Tile.NodeState.START_POINT || t.getState() == Tile.NodeState.STOP_POINT)) {
            t.setState(s);
        }
    }

    private void addKnotToOpenList(Tile k) {
        if (k.isWalkable() && k.isaStarDataSet() && !closedList.contains(k)) {
            openList.add(k);
        }
    }

    private void addKnotToClosedList(Tile k) {
        if (k != null) {
            changeTileState(k, Tile.NodeState.CLOSED);
            closedList.add(k);
        }
    }

    private Tile pullMinTileFromList(List<Tile> l) {
        Tile t = null;
        if (l.size() > 0) {
            t = l.get(getMinFIdx(l));
        }
        if (t != null) {
            l.remove(t);
        }
        return t;
    }

    private int getMinFIdx(List<Tile> l) {
        int minIdx = 0;
        for (int i = 0; i < l.size(); i++) {
            Tile h1 = l.get(i);
            Tile h2 = l.get(minIdx);
            if (h1.getF() <= h2.getF()) {
                minIdx = i;
            }
        }
        return minIdx;
    }

    public boolean isRunning() {
        return running;
    }
}
