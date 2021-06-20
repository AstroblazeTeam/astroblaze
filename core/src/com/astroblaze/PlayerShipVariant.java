package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

import java.util.ArrayList;

public enum PlayerShipVariant {
    Scout(0, Assets.spaceShipAssets.get(0), 15f, 100f, 0f, 2, 3),
    Cruiser(1, Assets.spaceShipAssets.get(1), 15f, 150f, 10000f, 4, 4),
    Destroyer(2, Assets.spaceShipAssets.get(2), 15f, 200f, 200000f, 5, 8);

    public final int id;
    public final AssetDescriptor<Model> modelDescriptor;
    public final float modelScale;
    public final float price;
    public final int gunPorts;
    public final int missilePorts;
    private final float maxHp;

    PlayerShipVariant(int id, AssetDescriptor<Model> modelDescriptor, float modelScale, float maxHp,
                      float price, int gunPorts, int missilePorts) {
        this.id = id;
        this.modelDescriptor = modelDescriptor;
        this.modelScale = modelScale;
        this.maxHp = maxHp;
        this.price = price;
        this.gunPorts = gunPorts;
        this.missilePorts = missilePorts;
    }

    public float getUpgradeModifier(PlayerState state, UpgradeEntryType upgradeType) {
        float modifier = 1.0f;
        ArrayList<UpgradeEntry> upgrades = state.getUpgrades(this.id);
        if (upgrades == null)
            return modifier;
        for (UpgradeEntry upgrade : upgrades) {
            if (upgrade.type == upgradeType) {
                modifier += upgrade.multiplier * upgrade.currentTier;
            }
        }
        return modifier;
    }

    public float getMaxHp(PlayerState state) {
        return this.maxHp * getUpgradeModifier(state, UpgradeEntryType.ShieldUpgrade);
    }

    public float getDamage(PlayerState state) {
        return 3f * getUpgradeModifier(state, UpgradeEntryType.DamageUpgrade);
    }

    public float getSpeed(PlayerState state) {
        return 50f * getUpgradeModifier(state, UpgradeEntryType.SpeedUpgrade);
    }
}
