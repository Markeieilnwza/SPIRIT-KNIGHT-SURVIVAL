package world;

public final class Levels {
    private int level = 1;
    private int xp;
    private int pendingLevelUps;

    public void addXp(int amount) {
        xp += amount;
        while (xp >= getXpToNextLevel()) {
            xp -= getXpToNextLevel();
            level++;
            pendingLevelUps++;
        }
    }

    public boolean hasPendingLevelUp() {
        return pendingLevelUps > 0;
    }

    public void consumePendingLevelUp() {
        if (pendingLevelUps > 0) {
            pendingLevelUps--;
        }
    }
    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getXpToNextLevel() {
        return 10 + (level - 1) * 25;
    }

    public double getXpProgress() {
        return Math.min(1.0, (double) xp / getXpToNextLevel());
    }
}
