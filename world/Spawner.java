package world;

import java.util.Random;

public final class Spawner {
    private static final int MAX_ENEMIES = 400;
    private final Random random = new Random();
    private double cooldown;
    private double eliteCooldown = 180.0;

    public void update(double dt, GameWorld world) {
        cooldown -= dt;
        eliteCooldown -= dt;
        if (world.canSpawnElite() && eliteCooldown <= 0.0
                && world.getEnemies().size() < MAX_ENEMIES) {
            spawnElite(world);
            eliteCooldown = world.getMode() == core.GameMode.TIMED ? 35.0 : 30.0;
        }
        if (cooldown > 0.0 || world.getEnemies().size() >= MAX_ENEMIES) {
            return;
        }

        int wave = world.getWave();
        int burst = world.getMode() == core.GameMode.TIMED
                ? Math.min(3, 1 + (wave - 1) / 4)
                : Math.min(5, 1 + (wave - 1) / 3);
        double hp;
        double speed;
        if (world.getMode() == core.GameMode.TIMED) {
            double progress = Math.min(1.0, world.getElapsedTime() / (12.0 * 60.0));
            hp = 35.0 + progress * 360.0;
            speed = 30.0 + progress * 55.0 + random.nextDouble() * 12.0;
        } else {
            double lateRunScale = world.getElapsedTime() <= 20.0 * 60.0 ? 1.0
                    : Math.pow(1.08, (world.getElapsedTime() - 20.0 * 60.0) / 60.0);
            hp = (35.0 + (wave - 1) * 18.0) * lateRunScale;
            speed = (30.0 + Math.min(95.0, (wave - 1) * 5.0)) * lateRunScale
                    + random.nextDouble() * 12.0;
        }
        for (int i = 0; i < burst && world.getEnemies().size() < MAX_ENEMIES; i++) {
            spawn(world, hp, speed, false);
        }

        double interval = Math.max(0.22, 1.0 - Math.min(0.55, world.getElapsedTime() / 1500.0));
        interval /= world.getNormalSpawnMultiplier();
        if (world.getMode() == core.GameMode.ENDLESS && world.getElapsedTime() > 20.0 * 60.0) {
            interval /= Math.pow(1.04, (world.getElapsedTime() - 20.0 * 60.0) / 60.0);
        }
        cooldown = Math.max(0.16, interval);
    }

    private void spawnElite(GameWorld world) {
        double hp = 120.0 * world.getDifficultyScale();
        double speed = 42.0 + Math.min(35.0, world.getWave() * 1.5);
        spawn(world, hp, speed, true);
    }

    private void spawn(GameWorld world, double hp, double speed, boolean elite) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 460.0 + random.nextDouble() * 100.0;
        double x = world.getPlayer().getX() + Math.cos(angle) * distance;
        double y = world.getPlayer().getY() + Math.sin(angle) * distance;
        if (elite) {
            world.addEliteEnemy(x, y, hp, speed, null);
        } else {
            world.addEnemy(x, y, hp, speed, null);
        }
    }
}
