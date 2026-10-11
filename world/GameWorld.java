package world;

import charecter.CharacterDef;
import core.GameMode;
import core.Input;
import entity.Boss;
import entity.Bullet;
import entity.Enemy;
import entity.Gem;
import entity.HostileProjectile;
import entity.Player;
import entity.TreasureChest;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class GameWorld {
    private static final double TIMED_BOSS_TIME = 13.0 * 60.0;
    private static final double TIMED_ENRAGE_TIME = 15.0 * 60.0;
    private static final double ENDLESS_BOSS_INTERVAL = 10.0 * 60.0;

    private final Player player;
    private final GameMode mode;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<HostileProjectile> hostileProjectiles = new ArrayList<>();
    private final List<Gem> gems = new ArrayList<>();
    private final List<TreasureChest> treasureChests = new ArrayList<>();
    private final List<DamageNumber> damageNumbers = new ArrayList<>();
    private final Random spriteRandom = new Random();
    private final Map<Integer, String> timedWaveSprites = new HashMap<>();
    private final Spawner spawner = new Spawner();
    private final Levels levels = new Levels();
    private double elapsedTime;
    private double contactCooldown;
    private double nextEndlessBossTime = ENDLESS_BOSS_INTERVAL;
    private int wave = 1;
    private int kills;
    private int bossKills;
    private int bossesSpawned;
    private int goldEarned;
    private boolean timedBossSpawned;
    private boolean endlessBossPending;
    private boolean victory;

    public GameWorld(CharacterDef character, GameMode mode) {
        player = new Player(character, 0.0, 0.0);
        this.mode = mode;
    }

    public GameWorld(CharacterDef character) {
        this(character, GameMode.ENDLESS);
    }

    public void update(double dt, Input input) {
        if (victory) {
            return;
        }
        elapsedTime += dt;
        wave = 1 + (int) (elapsedTime / 30.0);
        updateBossSchedule();
        contactCooldown = Math.max(0.0, contactCooldown - dt);
        player.update(dt, input, this);
        spawner.update(dt, this);

        for (Enemy enemy : new ArrayList<>(enemies)) {
            enemy.update(dt, player, this);
            double collisionDistance = enemy.getRadius() + player.getRadius();
            if (contactCooldown <= 0.0
                    && Math.hypot(enemy.getX() - player.getX(), enemy.getY() - player.getY())
                    < collisionDistance) {
                double damage = enemy.getContactDamage(
                        (12.0 + (wave - 1) * 2.0) * getDifficultyScale());
                player.damage(damage);
                contactCooldown = 0.5;
            }
        }

        for (HostileProjectile projectile : new ArrayList<>(hostileProjectiles)) {
            projectile.update(dt);
            if (projectile.isAlive()
                    && Math.hypot(projectile.getX() - player.getX(),
                            projectile.getY() - player.getY())
                            < projectile.getRadius() + player.getRadius()) {
                player.damage(projectile.getDamage());
                projectile.consume();
            }
        }

        for (Bullet bullet : new ArrayList<>(bullets)) {
            bullet.update(dt);
            if (!bullet.isAlive()) {
                continue;
            }
            for (Enemy enemy : new ArrayList<>(enemies)) {
                if (Math.hypot(enemy.getX() - bullet.getX(), enemy.getY() - bullet.getY())
                        < enemy.getRadius() + bullet.getRadius()) {
                    damageEnemy(enemy, bullet.getDamage());
                    bullet.consume();
                    break;
                }
            }
        }

        for (DamageNumber damageNumber : damageNumbers) {
            damageNumber.update(dt);
        }
        damageNumbers.removeIf(damageNumber -> !damageNumber.isAlive());

        Iterator<Gem> gemIterator = gems.iterator();
        while (gemIterator.hasNext()) {
            Gem gem = gemIterator.next();
            if (Math.hypot(gem.getX() - player.getX(), gem.getY() - player.getY())
                    < gem.getRadius() + player.getRadius() + 5.0) {
                levels.addXp(gem.getValue());
                gemIterator.remove();
            }
        }

        Iterator<TreasureChest> chestIterator = treasureChests.iterator();
        while (chestIterator.hasNext()) {
            TreasureChest chest = chestIterator.next();
            if (Math.hypot(chest.getX() - player.getX(), chest.getY() - player.getY())
                    < chest.getRadius() + player.getRadius()) {
                grantChestReward();
                chestIterator.remove();
            }
        }

        enemies.removeIf(enemy -> !enemy.isAlive());
        bullets.removeIf(bullet -> !bullet.isAlive());
        hostileProjectiles.removeIf(projectile -> !projectile.isAlive());
    }

    private void updateBossSchedule() {
        if (mode == GameMode.TIMED) {
            if (!timedBossSpawned && elapsedTime >= TIMED_BOSS_TIME) {
                timedBossSpawned = true;
                spawnBoss(0, 1200.0, 52.0, 30.0, 3);
            }
            if (elapsedTime >= TIMED_ENRAGE_TIME) {
                Boss boss = getBoss();
                if (boss != null && boss.isAlive()) {
                    boss.enrage();
                }
            }
            return;
        }

        if (elapsedTime >= nextEndlessBossTime) {
            endlessBossPending = true;
        }
        if (endlessBossPending && getBoss() == null) {
            int bossNumber = bossesSpawned;
            double scale = Math.pow(1.7, bossNumber);
            spawnBoss(bossNumber, Math.min(1.0e300, 1000.0 * scale),
                    42.0 + bossNumber * 2.0,
                    Math.min(1.0e100, 25.0 * Math.pow(1.3, bossNumber)),
                    Math.min(3, 2 + bossNumber / 2));
            endlessBossPending = false;
            do {
                nextEndlessBossTime += ENDLESS_BOSS_INTERVAL;
            } while (nextEndlessBossTime <= elapsedTime);
        }
    }

    private void spawnBoss(int bossNumber, double hp, double speed, double damage,
            int unlockedPatterns) {
        double angle = Math.atan2(player.getY() + 1.0, player.getX() + 1.0);
        enemies.add(new Boss(player.getX() + Math.cos(angle) * 560.0,
                player.getY() + Math.sin(angle) * 560.0, bossNumber, hp, speed,
                damage, unlockedPatterns));
        bossesSpawned++;
    }

    private void grantChestReward() {
        goldEarned += 50;
        if (player.getSkill().getLevel() < 10) {
            player.getSkill().increaseLevel();
        } else {
            player.increaseMaxHp(20.0);
        }
    }

    public void render(Graphics2D g, Camera camera, int width, int height) {
        GridBackground.render(g, camera, width, height);
        for (TreasureChest chest : treasureChests) {
            chest.render(g, camera);
        }
        for (Gem gem : gems) {
            gem.render(g, camera);
        }
        for (Enemy enemy : enemies) {
            enemy.render(g, camera);
        }
        for (HostileProjectile projectile : hostileProjectiles) {
            projectile.render(g, camera);
        }
        for (Bullet bullet : bullets) {
            bullet.render(g, camera);
        }
        for (DamageNumber damageNumber : damageNumbers) {
            damageNumber.render(g, camera);
        }
        player.render(g, camera);
        player.getSkill().render(g, camera);
    }

    public void damageEnemy(Enemy enemy, double amount) {
        if (!enemy.isAlive()) {
            return;
        }
        double dealtDamage = Math.min(enemy.getHp(), amount);
        enemy.damage(amount);
        if (dealtDamage > 0.0) {
            damageNumbers.add(new DamageNumber(enemy.getX(), enemy.getY(), dealtDamage));
        }
        if (!enemy.isAlive()) {
            kills++;
            if (enemy.isBoss()) {
                bossKills++;
                goldEarned += 100;
                if (mode == GameMode.ENDLESS) {
                    treasureChests.add(new TreasureChest(enemy.getX(), enemy.getY()));
                } else {
                    victory = true;
                }
            } else {
                goldEarned += enemy.isElite() ? 5 : 1;
            }
            gems.add(new Gem(enemy.getX(), enemy.getY(), 10, wave));
        }
    }

    public Enemy findNearestEnemy(double x, double y) {
        Enemy nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (Enemy enemy : enemies) {
            double distance = Math.hypot(enemy.getX() - x, enemy.getY() - y);
            if (distance < nearestDistance) {
                nearest = enemy;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public void addEnemy(double x, double y, double hp, double speed, String spriteFile) {
        String selectedSprite = spriteFile == null || spriteFile.isBlank()
                ? getEnemySprite() : spriteFile;
        enemies.add(new Enemy(x, y, hp, speed, selectedSprite));
    }

    public void addEliteEnemy(double x, double y, double hp, double speed, String spriteFile) {
        String selectedSprite = spriteFile == null || spriteFile.isBlank()
                ? getEnemySprite() : spriteFile;
        enemies.add(Enemy.elite(x, y, hp, speed, selectedSprite));
    }

    private String getEnemySprite() {
        if (mode != GameMode.TIMED) {
            return Enemy.selectSprite(mode, wave, spriteRandom);
        }
        int waveGroup = Math.floorDiv(Math.max(1, wave) - 1, 5);
        return timedWaveSprites.computeIfAbsent(waveGroup,
                group -> Enemy.selectSprite(mode, group * 5 + 1, spriteRandom));
    }

    public void addBullet(double x, double y, Enemy target, double speed, double damage,
            String spriteFile) {
        bullets.add(new Bullet(x, y, target, speed, damage, spriteFile));
    }

    public void addHostileProjectile(double x, double y, double angle, double speed,
            double damage) {
        hostileProjectiles.add(new HostileProjectile(x, y, angle, speed, damage));
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public Levels getLevels() {
        return levels;
    }

    public GameMode getMode() {
        return mode;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public int getWave() {
        return wave;
    }

    public int getKills() {
        return kills;
    }

    public int getBossKills() {
        return bossKills;
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public boolean isVictory() {
        return victory;
    }

    public boolean isBossWarning() {
        return mode == GameMode.TIMED && elapsedTime >= 12.0 * 60.0
                && elapsedTime < TIMED_BOSS_TIME;
    }

    public Boss getBoss() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss && enemy.isAlive()) {
                return (Boss) enemy;
            }
        }
        return null;
    }

    public double getDifficultyScale() {
        if (mode == GameMode.TIMED) {
            return 1.0 + 2.0 * Math.min(elapsedTime, 12.0 * 60.0)
                    / (12.0 * 60.0);
        }
        if (elapsedTime <= 20.0 * 60.0) {
            return 1.0 + elapsedTime / 400.0;
        }
        return 4.0 * Math.pow(1.08, (elapsedTime - 20.0 * 60.0) / 60.0);
    }

    public double getNormalSpawnMultiplier() {
        if (mode != GameMode.TIMED || elapsedTime <= TIMED_BOSS_TIME) {
            return 1.0;
        }
        return Math.max(0.2, 1.0 - (elapsedTime - TIMED_BOSS_TIME) / 120.0 * 0.8);
    }

    public boolean canSpawnElite() {
        return mode == GameMode.ENDLESS || elapsedTime < 12.0 * 60.0;
    }

    public double getNextBossTime() {
        return mode == GameMode.TIMED ? TIMED_BOSS_TIME : nextEndlessBossTime;
    }
}
