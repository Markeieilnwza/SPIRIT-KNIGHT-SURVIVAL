package skill;

import entity.Enemy;
import entity.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.IdentityHashMap;
import java.util.Map;
import world.Camera;
import world.GameWorld;

public final class OrbitSkill extends Skill {
    private static final String ORBIT_TEXTURE = "sword02.png";
    private double angle;
    private final Map<Enemy, Double> hitCooldowns = new IdentityHashMap<>();

    public OrbitSkill() {
        super("Orbit");
    }

    @Override
    public void update(double dt, Player owner, GameWorld world) {
        angle += dt * (1.8 + getLevel() * 0.12);
        int swords = 1 + (getLevel() - 1) / 2;
        double orbitRadius = 60.0;
        hitCooldowns.replaceAll((enemy, remaining) -> remaining - dt);
        hitCooldowns.entrySet().removeIf(entry -> !entry.getKey().isAlive()
                || entry.getValue() <= 0.0);
        for (Enemy enemy : world.getEnemies()) {
            if (hitCooldowns.containsKey(enemy)) {
                continue;
            }
            for (int i = 0; i < swords; i++) {
                double swordAngle = angle + Math.PI * 2.0 * i / swords;
                double swordX = owner.getX() + Math.cos(swordAngle) * orbitRadius;
                double swordY = owner.getY() + Math.sin(swordAngle) * orbitRadius;
                if (Math.hypot(enemy.getX() - swordX, enemy.getY() - swordY)
                        < enemy.getRadius() + 11.0) {
                    world.damageEnemy(enemy, 12.0 + getLevel() * 6.0);
                    hitCooldowns.put(enemy, 0.35);
                    break;
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        int swords = 1 + (getLevel() - 1) / 2;
        int size = 50;
        for (int i = 0; i < swords; i++) {
            double swordAngle = angle + Math.PI * 2.0 * i / swords;
            Graphics2D swordGraphics = (Graphics2D) g.create();
            swordGraphics.translate(
                    camera.getCenterX() + Math.cos(swordAngle) * 60.0,
                    camera.getCenterY() + Math.sin(swordAngle) * 60.0);
            swordGraphics.rotate(swordAngle + Math.PI / 2.0);
            sprite.SpriteLoader.draw(swordGraphics, ORBIT_TEXTURE,
                    -size / 2, -size / 2, size, new Color(255, 255, 255, 128));
            swordGraphics.dispose();
        }
    }
}
