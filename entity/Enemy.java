package entity;

import core.GameMode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import sprite.SpriteLoader;
import world.Camera;
import world.GameWorld;

public class Enemy extends Entity {
    private static final String[] DEFAULT_SPRITES = {
        "slime_0001.png", "slime_0008.png",
        "enemy.png", "enemy_ghoul.png",
        "enemy_ghost.png", "enemy_bat.png"
    };
    protected double speed;
    private final String spriteFile;
    private final boolean elite;

    public Enemy(double x, double y, double hp, double speed, String spriteFile) {
        this(x, y, 14.0, hp, speed, spriteFile, false);
    }

    protected Enemy(double x, double y, double radius, double hp, double speed,
            String spriteFile, boolean elite) {
        super(x, y, radius, hp);
        this.speed = speed;
        this.spriteFile = spriteFile;
        this.elite = elite;
    }

    public static Enemy elite(double x, double y, double hp, double speed, String spriteFile) {
        return new Enemy(x, y, 22.0, hp, speed, spriteFile, true);
    }

    public static String selectSprite(GameMode mode, int wave, Random random) {
        if (mode == GameMode.TIMED) {
            return DEFAULT_SPRITES[random.nextInt(DEFAULT_SPRITES.length)];
        }
        int spriteIndex = Math.floorDiv(Math.max(1, wave) - 1, 10)
                % DEFAULT_SPRITES.length;
        return DEFAULT_SPRITES[spriteIndex];
    }

    public void update(double dt, Player player) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distance = Math.hypot(dx, dy);
        if (distance > 0.0) {
            x += dx / distance * speed * dt;
            y += dy / distance * speed * dt;
        }
    }

    public void update(double dt, Player player, GameWorld world) {
        update(dt, player);
    }

    public boolean isElite() {
        return elite;
    }

    public boolean isBoss() {
        return false;
    }

    public double getContactDamage(double standardDamage) {
        return standardDamage;
    }

    public void render(Graphics2D g, Camera camera) {
        int size = elite ? 65 : 35;
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);
        String fileName = spriteFile;
        if (fileName == null || fileName.isBlank()) {
            fileName = DEFAULT_SPRITES[0];
        }
        Color tint = elite ? new Color(255, 145, 60) : new Color(70, 235, 175);
        SpriteLoader.draw(g, fileName, screenX - size / 2, screenY - size / 2, size, tint);
    }
}
