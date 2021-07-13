package com.astroblaze;

/**
 * Represents an upgrade entry for one of the upgrade types
 */
public class UpgradeEntry {
    public final int maxTier;
    public final float multiplier;
    public final float price;
    public final UpgradeEntryType type;
    public final float multiplierExtra;
    public final float priceExtra;
    public int currentTier;

    public UpgradeEntry(UpgradeEntryType type, int currentTier, int maxTier, float multiplier, float price,
                        float multiplierExtra, float priceExtra) {
        this.currentTier = currentTier;
        this.maxTier = maxTier;
        this.multiplier = multiplier;
        this.price = price;
        this.type = type;
        this.multiplierExtra = multiplierExtra;
        this.priceExtra = priceExtra;
    }

    public float getUpgradePrice() {
        return currentTier < maxTier ? price : priceExtra;
    }

    public float getNextMultiplier() {
        return currentTier < maxTier ? multiplier : multiplierExtra;
    }

    public float getCurrentMultiplier() {
        float simpleUpgrade = multiplier * Math.min(currentTier, maxTier);
        float extraUpgrade = currentTier <= maxTier ? 0f : multiplierExtra * (currentTier - maxTier);
        return 1f + simpleUpgrade + extraUpgrade;
    }
}
