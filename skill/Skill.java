package skill;

import entity.Player;
import java.awt.Graphics2D;
import world.Camera;
import world.GameWorld;


public abstract class Skill {
    private final String name;
    private int level = 1;

    protected Skill(String name) {
        this.name = name;
    }

    public abstract void update(double dt, Player owner, GameWorld world);

    public abstract void render(Graphics2D g, Camera camera);

    protected final void levelUp() {
        if (level < 10) {
            level++;
        }
    }

    public final void increaseLevel() {
        levelUp();
    }

    public final int getLevel() {
        return level;
    }

    public final String getName() {
        return name;
    }
}
