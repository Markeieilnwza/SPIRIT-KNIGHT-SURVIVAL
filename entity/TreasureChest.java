package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import world.Camera;

public final class TreasureChest {
    private final double x;
    private final double y;

    public TreasureChest(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return 17.0;
    }

    public void render(Graphics2D g, Camera camera) {
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);
        g.setColor(new Color(255, 220, 90));
        g.fillRoundRect(screenX - 17, screenY - 12, 34, 26, 6, 6);
        g.setColor(new Color(130, 75, 30));
        g.fillRect(screenX - 17, screenY - 3, 34, 5);
        g.fillRect(screenX - 3, screenY - 2, 6, 10);
    }
}
