package com.astroblaze;

/**
 * Represents a single hiscores entry
 */
public class HiscoresEntry {
    public final int rank;
    public final String id;
    public final String name;
    public final float score;
    public final int maxLevel;

    public HiscoresEntry(int rank, String id, String name, float score, int maxLevel) {
        this.rank = rank;
        this.id = id;
        this.name = name;
        this.score = score;
        this.maxLevel = maxLevel;
    }
}
