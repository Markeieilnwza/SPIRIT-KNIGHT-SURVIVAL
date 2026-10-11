package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import sprite.SpriteLoader;
import world.Camera;

public final class Gem extends Entity {
    private static final String[] SPRITE = {
        "green.png","blue.png","red.png","purple.png"
    };
    private final int value;
    private final int wave;

    public Gem(double x, double y, int value) {
        this(x, y, value, 1);
    }

    public Gem(double x, double y, int value, int wave) {
        super(x, y, 8.0, 1.0);
        this.wave = Math.max(1, wave);
        int spriteIndex = Math.min(3, Math.floorDiv(this.wave - 1, 5));
        this.value = value * (spriteIndex + 1);
    }

    public int getValue() {
        return value;
    }

    public void render(Graphics2D g, Camera camera) {
        int size = 20;
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);
        int spriteIndex = Math.min(3, Math.floorDiv(wave - 1, 5));
        SpriteLoader.draw(g, SPRITE[spriteIndex], screenX - size / 2,
                screenY - size / 2, size, new Color(255, 255, 255));
    }
}
