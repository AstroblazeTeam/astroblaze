package com.astroblaze;

import com.astroblaze.Rendering.EnemyType;

import java.util.HashMap;

public class LevelStatTracker {
    private final PlayerState state;
    private final HashMap<EnemyType, Integer> killsMap = new HashMap<>(64);
    private float damageDone;
    private float damageTaken;
    private float startMoney;
    private float startScore;

    public LevelStatTracker() {
        state = AstroblazeGame.getPlayerState();
    }

    public void addKill(EnemyType typeId) {
        int kills = killsMap.containsKey(typeId) ? killsMap.get(typeId) : 0;
        killsMap.put(typeId, kills + 1);
    }

    public void addDamageDone(float damage) {
        damageDone += damage;
    }

    public void addDamageTaken(float damage) {
        damageTaken -= damage;
    }

    public float getDamageDone() {
        return damageDone;
    }

    public float getDamageTaken() {
        return damageTaken;
    }

    public HashMap<EnemyType, Integer> getKills() {
        return killsMap;
    }

    public float getScore() {
        return state.getPlayerScore() - startScore;
    }

    public float getMoney() {
        return state.getPlayerMoney() - startMoney;
    }

    public void reset() {
        startScore = state.getPlayerScore();
        startMoney = state.getPlayerMoney();
        killsMap.clear();
        damageDone = 0f;
        damageTaken = 0f;
    }
}
