package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import world.Camera;

public final class HostileProjectile {
    private double x;
    private double y;
    private final double velocityX;
    private final double velocityY;
    private final double damage;
    private double lifetime = 4.0;
    private boolean alive = true;

    public HostileProjectile(double x, double y, double angle, double speed, double damage) {
        this.x = x;
        this.y = y;
        velocityX = Math.cos(angle) * speed;
        velocityY = Math.sin(angle) * speed;
        this.damage = damage;
    }

    public void update(double dt) {
        x += velocityX * dt;
        y += velocityY * dt;
        lifetime -= dt;
        alive = lifetime > 0.0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return 10.0;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isAlive() {
        return alive;
    }

    public void consume() {
        alive = false;
    }

    public void render(Graphics2D g, Camera camera) {
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);
        g.setColor(new Color(255, 100, 75));
        g.fillOval(screenX - 9, screenY - 9, 18, 18);
    }
}
