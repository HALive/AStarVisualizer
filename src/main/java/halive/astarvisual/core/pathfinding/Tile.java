package halive.astarvisual.core.pathfinding;

import halive.astarvisual.AStarVisualizer;
import halive.astarvisual.core.ui.GridRenderCanvas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Tile {

    private int width = AStarVisualizer.SQUARE_SIZE;
    private int height = AStarVisualizer.SQUARE_SIZE;

    private int x;
    private int y;

    private NodeState state;

    private Point position;
    private Point endPos;

    private Tile prior;

    private double g;
    private double h;

    private GridRenderCanvas canvas;

    private boolean aStarDataSet = false;

    public Tile(int x, int y, NodeState state, GridRenderCanvas canvas) {
        this.x = x;
        this.y = y;
        this.position = new Point(x / width, y / height);
        this.state = state;
        this.canvas = canvas;
    }

    public void render(Graphics g, float xScale, float yScale) {
        g.setColor(state.renderColor);
        g.fillRect((int) (x * xScale), (int) (y * yScale), (int) (width * xScale), (int) (height * yScale));
        if (AStarVisualizer.DRAW_GRID) {
            g.setColor(state.borderColor);
            g.drawRect((int) ((x) * xScale), (int) ((y) * yScale), (int) ((width) * xScale), (int) ((height) * yScale));
        }
    }

    public boolean isWalkable() {
        return state != NodeState.WALL;
    }

    public boolean isaStarDataSet() {
        return aStarDataSet;
    }

    public void setAStarValues(Tile prior, Point endPos, double g) {
        setPrior(prior);
        this.endPos = endPos;
        this.g = g;
        if (endPos != null) {
            this.h = calculateH();
        } else {
            this.h = 0;
        }
        aStarDataSet = true;
    }

    public void clearAStarValues() {
        setAStarValues(null, null, 0);
        aStarDataSet = false;
    }

    public Tile getPrior() {
        return prior;
    }

    public void setPrior(Tile prior) {
        this.prior = prior;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return g + h;
    }

    public boolean isEndNode() {
        return position.equals(endPos);
    }

    private double calculateH() {
        return canvas.getMode().calculateH(position, endPos);
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point getPosition() {
        return position;
    }

    public boolean isStateChangeAllowed() {
        return !(state == NodeState.START_POINT || state == NodeState.STOP_POINT);
    }

    public enum NodeState {
        START_POINT(Color.green, "Start"),
        STOP_POINT(Color.red, "Ziel"),
        WALL(Color.black, "Mauer"),
        UNKNOWN(Color.lightGray, "Unbekannt"),
        DISCOVERED(Color.orange, "Gesischtet"),
        CURRENT_PATH(Color.magenta, "Aktueller Pfad"),
        CLOSED(Color.yellow, "Erforscht");

        private Color renderColor;
        private Color borderColor;
        private String name;

        NodeState(Color renderColor, String name) {
            this.renderColor = renderColor;
            borderColor = new Color(renderColor.getRGB() ^ 0xFFFFFF);
            this.name = name;
        }

        public Color getBorderColor() {
            return borderColor;
        }
    }
}
