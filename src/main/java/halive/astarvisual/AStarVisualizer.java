package halive.astarvisual;


import halive.astarvisual.core.ui.GridRenderCanvas;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class AStarVisualizer {

    public static final int SQUARE_SIZE = 10;
    public static final int DELAY_MS = 10;
    public static final boolean DRAW_GRID = false;
    public static int X_OFFSET;
    public static int Y_OFFSET;
    public static final Dimension DEFAULT_SIZE = new Dimension(800 + X_OFFSET, 600 + Y_OFFSET);
    static {
        String osName = System.getProperty("os.name").toLowerCase().replace(" ", "");
        ScreenSizeOffset offset = ScreenSizeOffset.OTHER;
        for (int i = 0; i < ScreenSizeOffset.values().length; i++) {
            ScreenSizeOffset offset1 = ScreenSizeOffset.values()[i];
            if (osName.contains(offset1.lookupName)) {
                offset = offset1;
                break;
            }
        }
        X_OFFSET = offset.xOffset;
        Y_OFFSET = offset.yOffset;
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("A* 2D Grid");
        frame.setSize(DEFAULT_SIZE);
        final GridRenderCanvas c = new GridRenderCanvas(frame);
        frame.add(c);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newX = e.getComponent().getSize().width - X_OFFSET;
                int newY = e.getComponent().getSize().height - Y_OFFSET;
                c.updateScale(newX, newY);
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
                c.init();
            }
        });
    }

    private enum ScreenSizeOffset {
        WINDOWS(17, 90, "windows"),
        MACOS(2, 73, "mac"),
        OTHER(2, 73, "HELLO WORLD");

        private int xOffset;
        private int yOffset;
        private String lookupName;

        ScreenSizeOffset(int xOffset, int yOffset, String lookupName) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.lookupName = lookupName;
        }
    }
}
