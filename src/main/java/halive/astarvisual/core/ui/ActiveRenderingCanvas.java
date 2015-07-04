package halive.astarvisual.core.ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

public abstract class ActiveRenderingCanvas extends Canvas implements Runnable {
    protected Container parent;
    protected Thread renderThread = new Thread(this, "Rendering Canvas");

    private boolean running = true;
    private boolean debug = false;

    private long lastTime = 0;

    private Color bgColor = Color.white;

    public ActiveRenderingCanvas(Container frame) {
        this.parent = frame;
        renderThread.setDaemon(true);
        this.parent.setIgnoreRepaint(true);
        Container j = this.parent;
        while (j != null) {
            j.setIgnoreRepaint(true);
            j = j.getParent();
        }
    }

    @Override
    public void run() {
        BufferStrategy buf = this.getBufferStrategy();
        while (running) {
            Graphics g = buf.getDrawGraphics();
            g.setColor(bgColor);
            g.fillRect(0, 0, getSize().width, getSize().height);
            draw(g);
            if (debug) {
                double fps = 1/((System.currentTimeMillis() - lastTime) / 1000D);
                String fpsDisp = (Math.round(fps*100)/100D)+" FPS";
                int textW = g.getFontMetrics(new Font("Arial",Font.BOLD,12)).stringWidth(fpsDisp);
                g.setColor(Color.BLACK);
                g.drawString(fpsDisp, getWidth()-(textW+30), 20);
                lastTime = System.currentTimeMillis();
            }
            if (!buf.contentsLost())
                buf.show();
            Thread.yield();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {}
        }
    }

    public void init() {
        running = true;
        this.createBufferStrategy(2);
        this.renderThread.start();
        lastTime = System.currentTimeMillis();
    }

    public void toggleShutdown() {
        running = false;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public abstract void draw(Graphics g);
}
