package core;

import java.util.prefs.Preferences;

public final class EndlessHighScore {
    private static final Preferences PREFERENCES =
            Preferences.userNodeForPackage(EndlessHighScore.class);
    private static final String TIME_KEY = "endless.highScore.timeMillis";
    private static final String BOSSES_KEY = "endless.highScore.bossKills";
    private static final String KILLS_KEY = "endless.highScore.kills";

    private EndlessHighScore() {
    }

    public static void saveIfBetter(double survivalTime, int bossKills, int kills) {
        long timeMillis = Math.max(0L, Math.round(survivalTime * 1000.0));
        long bestTimeMillis = PREFERENCES.getLong(TIME_KEY, 0L);
        int bestBossKills = PREFERENCES.getInt(BOSSES_KEY, 0);
        int bestKills = PREFERENCES.getInt(KILLS_KEY, 0);
        if (timeMillis > bestTimeMillis
                || (timeMillis == bestTimeMillis && bossKills > bestBossKills)
                || (timeMillis == bestTimeMillis && bossKills == bestBossKills
                        && kills > bestKills)) {
            PREFERENCES.putLong(TIME_KEY, timeMillis);
            PREFERENCES.putInt(BOSSES_KEY, bossKills);
            PREFERENCES.putInt(KILLS_KEY, kills);
        }
    }

    public static double getBestTimeSeconds() {
        return PREFERENCES.getLong(TIME_KEY, 0L) / 1000.0;
    }

    public static int getBestBossKills() {
        return PREFERENCES.getInt(BOSSES_KEY, 0);
    }

    public static int getBestKills() {
        return PREFERENCES.getInt(KILLS_KEY, 0);
    }
}
