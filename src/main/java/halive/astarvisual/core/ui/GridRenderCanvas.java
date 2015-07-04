package halive.astarvisual.core.ui;

import halive.astarvisual.AStarVisualizer;
import halive.astarvisual.core.pathfinding.HeuristicMode;
import halive.astarvisual.core.pathfinding.PathfindingHandler;
import halive.astarvisual.core.pathfinding.Tile;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

public class GridRenderCanvas extends ActiveRenderingCanvas {

    private float xScale=1;
    private float yScale=1;

    private Tile[][] tiles;

    private Point startPos = null;
    private Point endPos = null;

    private boolean allowDrawing = true;
    //Possible Numbers:
    //SetMode = 0 <- Draws Walls
    //SetMode = 1 <- Sets StartPoint
    //SetMode = 2 <- Sets EndPoint
    private int setMode = 0;

    private PathfindingHandler pHandler;
    private Thread pHandlerThread;

    private HeuristicMode mode = HeuristicMode.values()[0];
    private int heurPos = 0;

    private String status = "";

    public GridRenderCanvas(Container frame) {
        super(frame);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseClicked(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        });
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        tiles = new Tile[(AStarVisualizer.DEFAULT_SIZE.width-AStarVisualizer.X_OFFSET)/AStarVisualizer.SQUARE_SIZE]
                [(AStarVisualizer.DEFAULT_SIZE.height-AStarVisualizer.Y_OFFSET)/AStarVisualizer.SQUARE_SIZE];
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                tiles[x][y] = new Tile(x*AStarVisualizer.SQUARE_SIZE, y*AStarVisualizer.SQUARE_SIZE, Tile.NodeState.UNKNOWN, this);
            }
        }
        clear();
    }

    public void updateScale(int x, int y) {
        xScale = ((float) x)/( AStarVisualizer.DEFAULT_SIZE.width-AStarVisualizer.X_OFFSET);
        yScale = ((float) y)/( AStarVisualizer.DEFAULT_SIZE.height-AStarVisualizer.Y_OFFSET);
    }

    @Override
    public void draw(Graphics g) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                tiles[x][y].render(g,xScale,yScale);
            }
        }
        g.setColor(Color.black);
        g.drawString("Heuristik: "+ mode.toString(), 5, (int) (15+(tiles[0].length*AStarVisualizer.SQUARE_SIZE*yScale)));
        g.drawString("Tasten: H = Heuristik ändern; S = Startknoten setzen; E = Endknoten Setzen; C = Fläche bereinigen", 5, (int) (30+(tiles[0].length*AStarVisualizer.SQUARE_SIZE*yScale)));
        g.drawString(status, 5, (int) (45+(tiles[0].length*AStarVisualizer.SQUARE_SIZE*yScale)));
    }

    private void handleKeyPress(KeyEvent e) {
        switch(e.getKeyCode()) {
            case(KeyEvent.VK_S):
                setMode = 1;
                break;
            case(KeyEvent.VK_E):
                setMode = 2;
                break;
            case(KeyEvent.VK_C):
                clear();
                break;
            case(KeyEvent.VK_H):
                changeHeur();
                break;
            case(KeyEvent.VK_SPACE):
                startSearch();
                break;
            case(KeyEvent.VK_R):
                generateRandomMap();
                break;
            default:
                break;
        }
    }

    private void generateRandomMap() {
        if(allowDrawing) {
            int xMax = tiles.length;
            int yMax = tiles[0].length;
            clear();
            Random r = new Random();
            setTileModeAtPos(startPos, Tile.NodeState.UNKNOWN);
            setTileModeAtPos(endPos, Tile.NodeState.UNKNOWN);
            for (int x = 0; x < xMax; x++) {
                for (int y = 0; y < yMax; y++) {
                    Tile.NodeState s = r.nextInt(100) > 70 ? Tile.NodeState.WALL : Tile.NodeState.UNKNOWN;
                    Point p = new Point(x,y);
                    setTileModeAtPos(p, s);
                }
            }
            double dist = 0;
            do {
                startPos = new Point(r.nextInt(xMax), r.nextInt(yMax));
                endPos = new Point(r.nextInt(xMax), r.nextInt(yMax));
                dist = startPos.distance(endPos);
            } while(startPos.equals(endPos) || dist < 70);
            setTileModeAtPos(startPos, Tile.NodeState.START_POINT);
            setTileModeAtPos(endPos, Tile.NodeState.STOP_POINT);
        }
    }

    private void changeHeur() {
        if(allowDrawing) {
            heurPos++;
            heurPos = heurPos%HeuristicMode.values().length;
            mode = HeuristicMode.values()[heurPos];
        }
    }

    private void clear() {
        status = "Leertaste zum Starten drücken...";
        if(allowDrawing) {
            startPos = null;
            endPos = null;
            for (int x = 0; x < tiles.length; x++) {
                for (int y = 0; y < tiles[x].length; y++) {
                    Tile t = tiles[x][y];
                    t.clearAStarValues();
                    t.setState(Tile.NodeState.UNKNOWN);
                }
            }
        }
    }

    private void startSearch() {
        if(startPos == null || endPos == null) {
            JOptionPane.showMessageDialog(this, "Suche konnte nicht gestartet werden,\n" +
                            "da kein Start- und/oder Endpunkt festgelegt wurde.",
                    "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setMode = 0;
        if(allowDrawing) {
            pHandler = new PathfindingHandler(this);
            pHandlerThread = new Thread(pHandler);
            pHandlerThread.start();
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTileModeAtPos(Point p, Tile.NodeState s) {
        if (p != null) {
            tiles[p.x][p.y].setState(s);
        }
    }

    private void handleMouseClicked(MouseEvent e) {
        if(allowDrawing) {
            if(e.getButton() == 2) {
                return;
            }
            Tile.NodeState state = SwingUtilities.isRightMouseButton(e)? Tile.NodeState.UNKNOWN : Tile.NodeState.WALL;
            int x = e.getX()/(int) (AStarVisualizer.SQUARE_SIZE*xScale);
            int y = e.getY()/(int) (AStarVisualizer.SQUARE_SIZE*yScale);
            if(x < 0 || y < 0 || x >= tiles.length||y >= tiles[0].length) {
                return;
            }
            if (tiles[x][y].isStateChangeAllowed()) {
                if(setMode >= 1) {
                    state = setMode == 1 ? Tile.NodeState.START_POINT : Tile.NodeState.STOP_POINT;
                    Point p = setMode == 1 ? startPos : endPos;
                    if (p != null) {
                        tiles[p.x][p.y].setState(Tile.NodeState.UNKNOWN);
                    }
                    p = new Point(x,y);
                    if(setMode == 1)
                        startPos = p;
                    else
                        endPos = p;
                    setMode = 0;
                }
                tiles[x][y].setState(state);
            }
        }
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public Tile getStartTile() {
        return tiles[startPos.x][startPos.y];
    }

    public Tile getEndTile() {
        return tiles[endPos.x][endPos.y];
    }

    public Point getStartPos() {
        return startPos;
    }

    public Point getEndPos() {
        return endPos;
    }

    public void setAllowDrawing(boolean allowDrawing) {
        this.allowDrawing = allowDrawing;
    }

    public HeuristicMode getMode() {
        return mode;
    }
}
