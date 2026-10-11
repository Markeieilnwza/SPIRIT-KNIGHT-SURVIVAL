package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import sprite.SpriteLoader;
import world.Camera;

public final class Bullet extends Entity {
    private static final double MAX_LIFETIME = 5.0;
    private final double velocity;
    private final double damage;
    private final Enemy target;
    private final String spriteFile;
    private double directionX;
    private double directionY;
    private double lifetime;

    public Bullet(double x, double y, Enemy target, double velocity, double damage,
            String spriteFile) {
        super(x, y, 6.0, 1.0);
        this.target = target;
        this.velocity = velocity;
        this.damage = damage;
        this.spriteFile = spriteFile;
        double distance = Math.hypot(target.getX() - x, target.getY() - y);
        directionX = distance == 0.0 ? 1.0 : (target.getX() - x) / distance;
        directionY = distance == 0.0 ? 0.0 : (target.getY() - y) / distance;
    }

    public void update(double dt) {
        lifetime += dt;
        if (lifetime >= MAX_LIFETIME || !target.isAlive()) {
            alive = false;
            return;
        }
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double distance = Math.hypot(dx, dy);
        if (distance > 0.0) {
            directionX = dx / distance;
            directionY = dy / distance;
        }
        x += directionX * velocity * dt;
        y += directionY * velocity * dt;
    }

    public double getDamage() {
        return damage;
    }

    public void consume() {
        alive = false;
    }

    public void render(Graphics2D g, Camera camera) {
        int size = 50;
        int screenX = (int) camera.worldToScreenX(x) - size / 2;
        int screenY = (int) camera.worldToScreenY(y) - size / 2;
        if (!SpriteLoader.drawIfAvailable(g, spriteFile, screenX, screenY, size)) {
            g.setColor(new Color(125, 205, 255));
            g.fillOval(screenX, screenY, size, size);
        }
    }
}
