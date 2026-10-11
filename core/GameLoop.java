package core;

import javax.swing.Timer;

public final class GameLoop {
    private final GamePanel panel;
    private final Timer timer;
    private long previousTime;

    public GameLoop(GamePanel panel) {
        this.panel = panel;
        timer = new Timer(16, event -> tick());
    }

    public void start() {
        previousTime = System.nanoTime();
        timer.start();
    }

    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min((now - previousTime) / 1_000_000_000.0, 0.05);
        previousTime = now;
        panel.updateGame(dt);
        panel.repaint();
    }
}
