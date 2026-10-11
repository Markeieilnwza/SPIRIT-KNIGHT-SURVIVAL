package entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import world.Camera;
import world.GameWorld;

public final class Boss extends Enemy {
    private static final String[] NAMES = {"The Warden", "Ashen Tyrant", "Void Stalker"};
    private static final Color[] COLORS = {
        new Color(210, 75, 95), new Color(235, 130, 45), new Color(155, 90, 220)
    };
    private final String name;
    private final int unlockedPatterns;
    private final double contactDamage;
    private final Color color;
    private double chargeCooldown = 3.0;
    private double chargeRemaining;
    private double chargeX;
    private double chargeY;
    private double spreadCooldown = 2.0;
    private double summonCooldown = 8.0;
    private double damageMultiplier = 1.0;
    private boolean enraged;

    public Boss(double x, double y, int bossNumber, double hp, double speed,
            double damage, int unlockedPatterns) {
        super(x, y, 42.0, hp, speed, null, false);
        int type = Math.floorMod(bossNumber, NAMES.length);
        name = NAMES[type];
        color = COLORS[type];
        contactDamage = damage;
        this.unlockedPatterns = unlockedPatterns;
    }

    @Override
    public void update(double dt, Player player, GameWorld world) {
        chargeCooldown -= dt;
        spreadCooldown -= dt;
        if (unlockedPatterns >= 3) {
            summonCooldown -= dt;
        }

        if (chargeRemaining > 0.0) {
            double movement = (speed * 2.8) * dt;
            x += chargeX * movement;
            y += chargeY * movement;
            chargeRemaining = Math.max(0.0, chargeRemaining - dt);
        } else {
            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double distance = Math.hypot(dx, dy);
            if (distance > 0.0) {
                x += dx / distance * speed * dt;
                y += dy / distance * speed * dt;
            }
            if (chargeCooldown <= 0.0) {
                chargeX = distance == 0.0 ? 1.0 : dx / distance;
                chargeY = distance == 0.0 ? 0.0 : dy / distance;
                chargeRemaining = 1.1;
                chargeCooldown = 5.0;
            }
        }

        if (spreadCooldown <= 0.0) {
            double baseAngle = Math.atan2(player.getY() - y, player.getX() - x);
            for (int i = -3; i <= 3; i++) {
                world.addHostileProjectile(x, y, baseAngle + i * 0.22,
                        210.0, contactDamage * 0.6 * damageMultiplier);
            }
            spreadCooldown = 3.4;
        }

        if (unlockedPatterns >= 3 && summonCooldown <= 0.0) {
            for (int i = 0; i < 3; i++) {
                double angle = i * Math.PI * 2.0 / 3.0;
                world.addEnemy(x + Math.cos(angle) * 55.0,
                        y + Math.sin(angle) * 55.0, getMaxHp() * 0.025,
                        speed * 1.15, null);
            }
            summonCooldown = 10.0;
        }
    }

    public void enrage() {
        if (!enraged) {
            enraged = true;
            speed *= 1.5;
            damageMultiplier *= 1.5;
        }
    }

    public double getContactDamage() {
        return contactDamage * damageMultiplier;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public double getContactDamage(double standardDamage) {
        return getContactDamage();
    }

    public String getName() {
        return name;
    }

    public boolean isEnraged() {
        return enraged;
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 65));
        g.fillOval(screenX - 53, screenY - 53, 106, 106);
        g.setColor(color);
        g.fillOval(screenX - 42, screenY - 42, 84, 84);
        g.setColor(new Color(255, 235, 205));
        g.fillOval(screenX - 15, screenY - 15, 30, 30);

        int barWidth = 180;
        int barX = screenX - barWidth / 2;
        int barY = screenY - 66;
        g.setColor(new Color(20, 15, 20, 220));
        g.fillRoundRect(barX, barY, barWidth, 10, 6, 6);
        g.setColor(enraged ? new Color(255, 80, 40) : color);
        g.fillRoundRect(barX, barY,
                (int) (barWidth * Math.max(0.0, getHp() / getMaxHp())), 10, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        String label = name + (enraged ? " - ENRAGED" : "");
        g.drawString(label, screenX - g.getFontMetrics().stringWidth(label) / 2, barY - 6);
    }
}
