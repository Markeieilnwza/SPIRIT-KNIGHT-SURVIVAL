package core;

import charecter.CharacterDef;
import entity.Boss;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import skill.Skill;
import world.Camera;
import world.GameWorld;
import world.Levels;

public final class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final transient Input input = new Input();
    private final transient GameLoop loop = new GameLoop(this);
    private GameState state = GameState.SELECT;
    private transient GameWorld world;
    private GameMode selectedMode = GameMode.TIMED;

    public GamePanel() {
        setFocusable(true);
        setBackground(new Color(17, 20, 29));
        addKeyListener(input);
    }

    public void start() {
        loop.start();
    }

    public void updateGame(double dt) {
        if (state == GameState.SELECT) {
            if (input.consumePressed(KeyEvent.VK_1)) {
                selectedMode = GameMode.TIMED;
                state = GameState.CHARACTER_SELECT;
            } else if (input.consumePressed(KeyEvent.VK_2)) {
                selectedMode = GameMode.ENDLESS;
                state = GameState.CHARACTER_SELECT;
            }
            return;
        }
        if (state == GameState.CHARACTER_SELECT) {
            for (int i = 0; i < CharacterDef.ALL.size(); i++) {
                if (input.consumePressed(KeyEvent.VK_1 + i)) {
                    startGame(CharacterDef.ALL.get(i));
                    return;
                }
            }
            return;
        }
        if (state == GameState.PLAY) {
            world.update(dt, input);
            if (world.getPlayer().getHp() <= 0.0) {
                state = GameState.DEAD;
                if (world.getMode() == GameMode.ENDLESS) {
                    EndlessHighScore.saveIfBetter(world.getElapsedTime(),
                            world.getBossKills(), world.getKills());
                }
            } else if (world.isVictory()) {
                state = GameState.VICTORY;
            } else if (world.getLevels().hasPendingLevelUp()) {
                state = GameState.LEVELUP;
            }
            return;
        }
        if (state == GameState.LEVELUP) {
            for (int i = 0; i < 3; i++) {
                if (input.consumePressed(KeyEvent.VK_1 + i)) {
                    applyUpgrade(i);
                    return;
                }
            }
        } else if ((state == GameState.DEAD || state == GameState.VICTORY)
                && input.consumePressed(KeyEvent.VK_R)) {
            state = GameState.SELECT;
            world = null;
        }
    }

    private void startGame(CharacterDef character) {
        world = new GameWorld(character, selectedMode);
        state = GameState.PLAY;
    }

    private void applyUpgrade(int choice) {
        if (choice == 0) {
            world.getPlayer().getSkill().increaseLevel();
        } else if (choice == 1) {
            world.getPlayer().increaseSpeed(1.1);
        } else {
            world.getPlayer().increaseMaxHp(20.0);
        }
        world.getLevels().consumePendingLevelUp();
        state = world.getLevels().hasPendingLevelUp() ? GameState.LEVELUP : GameState.PLAY;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (state == GameState.SELECT) {
            drawModeSelection(g);
        } else if (state == GameState.CHARACTER_SELECT) {
            drawCharacterSelection(g);
        } else if (world != null) {
            Camera camera = new Camera(world.getPlayer(), getWidth(), getHeight());
            world.render(g, camera, getWidth(), getHeight());
            drawHud(g);
            if (state == GameState.LEVELUP) {
                drawLevelUp(g);
            } else if (state == GameState.DEAD) {
                drawGameOver(g);
            } else if (state == GameState.VICTORY) {
                drawVictory(g);
            }
        }
        g.dispose();
    }

    private void drawModeSelection(Graphics2D g) {
        drawCentered(g, "SPIRIT KNIGHT SURVIVAL", 90, 42, Color.WHITE);
        drawCentered(g, "SELECT GAME MODE", 150, 24, new Color(220, 220, 230));
        drawCentered(g, "1. TIMED - Defeat the final boss before it enrages",
                230, 20, new Color(220, 230, 245));
        drawCentered(g, "2. ENDLESS - Survive, fight recurring bosses, and set a high score",
                275, 20, new Color(220, 230, 245));
        double bestTime = EndlessHighScore.getBestTimeSeconds();
        drawCentered(g, "ENDLESS BEST  " + formatTime(bestTime)
                        + "  |  BOSSES " + EndlessHighScore.getBestBossKills()
                        + "  |  KILLS " + EndlessHighScore.getBestKills(),
                340, 16, new Color(255, 210, 105));
        drawCentered(g, "Press 1 or 2 to continue", getHeight() - 48, 16,
                new Color(180, 190, 205));
    }

    private void drawCharacterSelection(Graphics2D g) {
        drawCentered(g, "SELECT A CHARACTER  -  "
                + (selectedMode == GameMode.TIMED ? "TIMED MODE" : "ENDLESS MODE"),
                150, 24, new Color(220, 220, 230));
        for (int i = 0; i < CharacterDef.ALL.size(); i++) {
            CharacterDef character = CharacterDef.ALL.get(i);
            int y = 240 + i * 72;
            g.setColor(new Color(35, 42, 57));
            g.fillRoundRect(getWidth() / 2 - 210, y - 32, 420, 54, 14, 14);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            g.drawString((i + 1) + ". " + character.getName(), getWidth() / 2 - 175, y + 2);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            g.setColor(new Color(180, 190, 205));
            g.drawString(character.getSkillDescription(), getWidth() / 2 - 20, y + 2);
        }
        drawCentered(g, "Press 1-3 to choose  |  Move with WASD or Arrow Keys",
                getHeight() - 48, 16, new Color(180, 190, 205));
    }

    private void drawHud(Graphics2D g) {
        Levels levels = world.getLevels();
        int margin = 18;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(margin, margin, 260, 24, 10, 10);
        g.setColor(new Color(194, 58, 72));
        g.fillRoundRect(margin, margin, (int) (260 * world.getPlayer().getHp()
                / world.getPlayer().getMaxHp()), 24, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.drawString("HP " + (int) world.getPlayer().getHp() + " / "
                + (int) world.getPlayer().getMaxHp(), margin + 8, margin + 17);

        int xpY = getHeight() - 25;
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, xpY, getWidth(), 25);
        g.setColor(new Color(85, 190, 255));
        int xpWidth = (int) Math.round(Math.min(getWidth(),
                getWidth() * levels.getXpProgress()));
        g.fillRect(0, xpY, xpWidth, 25);
        g.setColor(Color.WHITE);
        g.drawString("LV " + levels.getLevel() + "   XP " + levels.getXp()
                + " / " + levels.getXpToNextLevel(), 12, xpY - 7);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString(formatTime(world.getElapsedTime()), getWidth() / 2 - 30, 34);
        g.drawString("KILLS " + world.getKills(), getWidth() - 120, 34);
        g.drawString("WAVE " + world.getWave(), getWidth() / 2 - 34, 58);
        g.drawString("GOLD " + world.getGoldEarned(), getWidth() - 120, 58);
        Skill skill = world.getPlayer().getSkill();
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString(world.getPlayer().getCharacter().getName() + "  |  "
                + skill.getName() + " Lv." + skill.getLevel() + "  |  "
                + (world.getMode() == GameMode.TIMED ? "TIMED" : "ENDLESS"), 18, 62);
        if (world.isBossWarning()) {
            drawCentered(g, "Boss approaching", 105, 28, new Color(255, 105, 85));
        } else if (world.getMode() == GameMode.ENDLESS) {
            Boss boss = world.getBoss();
            String status = boss == null
                    ? "NEXT BOSS IN " + formatTime(Math.max(0.0,
                            world.getNextBossTime() - world.getElapsedTime()))
                    : "BOSS " + boss.getName();
            drawCentered(g, status, 92, 18, new Color(255, 200, 110));
        }
    }

    private void drawLevelUp(Graphics2D g) {
        g.setColor(new Color(5, 8, 15, 205));
        g.fillRect(0, 0, getWidth(), getHeight());
        drawCentered(g, "LEVEL UP!", getHeight() / 2 - 90, 34, Color.WHITE);
        String[] choices = {
            "1. Upgrade your character skill",
            "2. Increase movement speed by 10%",
            "3. Increase max HP by 20 and fully heal"
        };
        for (int i = 0; i < choices.length; i++) {
            drawCentered(g, choices[i], getHeight() / 2 - 25 + i * 42, 20,
                    new Color(220, 230, 245));
        }
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(5, 8, 15, 205));
        g.fillRect(0, 0, getWidth(), getHeight());
        drawCentered(g, "GAME OVER", getHeight() / 2 - 90, 38, Color.WHITE);
        drawRunSummary(g, getHeight() / 2 - 30);
        drawCentered(g, "Press R to return to mode select", getHeight() / 2 + 112, 20,
                new Color(220, 230, 245));
    }

    private void drawVictory(Graphics2D g) {
        g.setColor(new Color(5, 8, 15, 205));
        g.fillRect(0, 0, getWidth(), getHeight());
        drawCentered(g, "VICTORY!", getHeight() / 2 - 90, 42,
                new Color(255, 220, 100));
        drawRunSummary(g, getHeight() / 2 - 25);
        drawCentered(g, "Press R to return to mode select", getHeight() / 2 + 112, 20,
                new Color(220, 230, 245));
    }

    private void drawRunSummary(Graphics2D g, int startY) {
        drawCentered(g, "TIME " + formatTime(world.getElapsedTime())
                        + "  |  KILLS " + world.getKills(),
                startY, 20, Color.WHITE);
        drawCentered(g, "LEVEL " + world.getLevels().getLevel()
                        + "  |  GOLD EARNED " + world.getGoldEarned()
                        + "  |  BOSSES " + world.getBossKills(),
                startY + 38, 20, new Color(220, 230, 245));
    }

    private void drawCentered(Graphics2D g, String text, int y, int size, Color color) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
        g.setColor(color);
        g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

    private String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}
