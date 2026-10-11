package world;

import entity.Player;

public final class Camera {
    private final double x;
    private final double y;
    private final int width;
    private final int height;

    public Camera(Player target, int width, int height) {
        x = target.getX();
        y = target.getY();
        this.width = width;
        this.height = height;
    }

    public int getCenterX() {
        return width / 2;
    }

    public int getCenterY() {
        return height / 2;
    }

    public double worldToScreenX(double worldX) {
        return worldX - x + width / 2.0;
    }

    public double worldToScreenY(double worldY) {
        return worldY - y + height / 2.0;
    }
}
