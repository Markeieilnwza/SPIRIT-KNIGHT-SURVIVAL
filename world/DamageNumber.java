package world;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public final class DamageNumber {
    private static final double DURATION = 0.8;
    private final double x;
    private final double y;
    private final int amount;
    private double age;

    public DamageNumber(double x, double y, double amount) {
        this.x = x;
        this.y = y;
        this.amount = (int) Math.round(amount);
    }

    public void update(double dt) {
        age += dt;
    }

    public boolean isAlive() {
        return age < DURATION;
    }

    public void render(Graphics2D g, Camera camera) {
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y - age * 36.0);
        String text = Integer.toString(amount);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        int textX = screenX - g.getFontMetrics().stringWidth(text) / 2;
        g.setColor(new Color(25, 20, 20, 190));
        g.drawString(text, textX + 1, screenY + 1);
        g.setColor(new Color(255, 238, 175));
        g.drawString(text, textX, screenY);
    }
}
