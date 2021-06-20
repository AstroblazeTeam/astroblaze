package com.astroblaze;

public class UpgradeEntry {
    public final String name;
    public final float base;
    public final int maxTier;
    public final float multiplier;
    public final float price;
    public final UpgradeEntryType type;
    public int currentTier;

    public UpgradeEntry(String name, float base, int currentTier, int maxTier, float multiplier, float price, UpgradeEntryType type) {
        this.name = name;
        this.base = base;
        this.currentTier = currentTier;
        this.maxTier = maxTier;
        this.multiplier = multiplier;
        this.price = price;
        this.type = type;
    }

    public float getCurrent() {
        return base + multiplier * currentTier;
    }
}
