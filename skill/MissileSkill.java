package skill;

import entity.Enemy;
import entity.Player;
import java.awt.Graphics2D;
import world.Camera;
import world.GameWorld;

public final class MissileSkill extends Skill {
    private static final String MISSILE_TEXTURE = "i_want_fire_ball.png";
    private double cooldown;

    public MissileSkill() {
        super("Missile");
    }

    @Override
    public void update(double dt, Player owner, GameWorld world) {
        cooldown -= dt;
        if (cooldown > 0.0) {
            return;
        }
        Enemy target = world.findNearestEnemy(owner.getX(), owner.getY());
        if (target == null) {
            return;
        }
        int shots = 1 + (getLevel() - 1) / 3;
        double damage = 30.0 + getLevel() * 7.5;
        for (int i = 0; i < shots; i++) {
            world.addBullet(owner.getX(), owner.getY(), target,
                    360.0 + getLevel() * 20.0, damage, MISSILE_TEXTURE);
        }
        cooldown = Math.max(0.22, 1.0 - (getLevel() - 1) * 0.09);
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        // Missile skill has no persistent visual attached to the player.
    }
}
