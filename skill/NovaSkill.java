package skill;

import entity.Enemy;
import entity.Player;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import world.Camera;
import world.GameWorld;

public final class NovaSkill extends Skill {
    private final static String NOVA_TEXTURE = "frostskill.png";
    private double cooldown;
    private double pulseTime;
    private double pulseRadius = 20.0;

    public NovaSkill() {
        super("Nova");
    }

    @Override
    public void update(double dt, Player owner, GameWorld world) {
        cooldown -= dt;
        pulseTime = Math.max(0.0, pulseTime - dt);
        if (cooldown > 0.0) {
            return;
        }
        double radius = 105.0 + (getLevel() - 1) * 20.0;
        double damage = 30.0 + getLevel() * 9.0;
        for (Enemy enemy : world.getEnemies()) {
            if (Math.hypot(enemy.getX() - owner.getX(), enemy.getY() - owner.getY())
                    <= radius + enemy.getRadius()) {
                world.damageEnemy(enemy, damage);
            }
        }
        pulseRadius = radius;
        pulseTime = 0.25;
        cooldown = Math.max(0.45, 2.0 - (getLevel() - 1) * 0.18);
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (pulseTime > 0.0) {
            int diameter = (int) Math.round(pulseRadius * 2.0);
            int screenX = camera.getCenterX() - diameter / 2;
            int screenY = camera.getCenterY() - diameter / 2;
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            if (!sprite.SpriteLoader.drawIfAvailable(g, NOVA_TEXTURE,
                    screenX, screenY, diameter)) {
                g.setColor(new Color(150, 220, 255, 120));
                g.fillOval(screenX, screenY, diameter, diameter);
            }
            g.setComposite(oldComposite);
        }
    }
}
