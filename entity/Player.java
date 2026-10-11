package entity;

import charecter.CharacterDef;
import core.Input;
import java.awt.Color;
import java.awt.Graphics2D;
import skill.Skill;
import sprite.SpriteLoader;
import world.Camera;
import world.GridBackground;
import world.GameWorld;

public final class Player extends Entity {
    private final CharacterDef character;
    private final Skill skill;
    private double speed;
    private boolean facingRight = true;

    public Player(CharacterDef character, double x, double y) {
        super(x, y, 17.0, character.getMaxHp());
        this.character = character;
        speed = character.getSpeed();
        skill = character.createSkill();
    }

    public void update(double dt, Input input, GameWorld world) {
        double dx = 0.0;
        double dy = 0.0;
        if (input.isDown(java.awt.event.KeyEvent.VK_A)
                || input.isDown(java.awt.event.KeyEvent.VK_LEFT)) {
            dx -= 1.0;
            
        }
        if (input.isDown(java.awt.event.KeyEvent.VK_D)
                || input.isDown(java.awt.event.KeyEvent.VK_RIGHT)) {
            dx += 1.0;
        }
        if (dx != 0.0) {
            facingRight = dx > 0.0;
        }
        if (input.isDown(java.awt.event.KeyEvent.VK_W)
                || input.isDown(java.awt.event.KeyEvent.VK_UP)) {
            dy -= 1.0;
        }
        if (input.isDown(java.awt.event.KeyEvent.VK_S)
                || input.isDown(java.awt.event.KeyEvent.VK_DOWN)) {
            dy += 1.0;
        }
        double length = Math.hypot(dx, dy);
        if (length > 0.0) {
            double movementX = dx / length * speed * dt;
            double movementY = dy / length * speed * dt;
            double[] resolved = GridBackground.resolve(x + movementX, y, radius);
            x = resolved[0];
            y = resolved[1];
            resolved = GridBackground.resolve(x, y + movementY, radius);
            x = resolved[0];
            y = resolved[1];
        }
        skill.update(dt, this, world);
    }

    public void increaseSpeed(double multiplier) {
        speed *= multiplier;
    }

    public void increaseMaxHp(double amount) {
        maxHp += amount;
        hp = maxHp;
    }

    public CharacterDef getCharacter() {
        return character;
    }

    public Skill getSkill() {
        return skill;
    }

    public void render(Graphics2D g, Camera camera) {
        int size = 90;
        int screenX = (int) camera.worldToScreenX(x) - size / 2;
        int screenY = (int) camera.worldToScreenY(y) - size / 2;
        if (facingRight) {
            SpriteLoader.draw(g, character.getSpriteFile(), screenX, screenY, size,
                    new Color(90, 180, 245));
            return;
        }
        Graphics2D flippedGraphics = (Graphics2D) g.create();
        flippedGraphics.translate(screenX + size, screenY);
        flippedGraphics.scale(-1.0, 1.0);
        SpriteLoader.draw(flippedGraphics, character.getSpriteFile(), 0, 0, size,
                new Color(90, 180, 245));
        flippedGraphics.dispose();
    }
}
