package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

import java.util.ArrayList;

public enum PlayerShipVariant {
    Scout(0, 0, 15f, 100f, 0f, 1, 1, 2),
    Destroyer(1, 1, 15f, 150f, 10000f, 2, 1, 3),
    Cruiser(2, 2, 15f, 200f, 50000f, 3, 2, 4),
    Battlecruiser(3, 3, 15f, 250f, 100000f, 4, 2, 5),
    Battleship(4, 9, 15f, 300f, 1000000f, 5, 3, 6);

    public final int id;
    public final int modelDescriptorId;
    public final float modelScale;
    public final float price;
    public final int gunPorts;
    public final int turretPorts;
    public final int missilePorts;
    private final float maxHp;

    PlayerShipVariant(int id, int modelDescriptorId, float modelScale, float maxHp,
                      float price, int gunPorts, int turretPorts, int missilePorts) {
        this.id = id;
        this.modelDescriptorId = modelDescriptorId;
        this.modelScale = modelScale;
        this.maxHp = maxHp;
        this.price = price;
        this.gunPorts = gunPorts;
        this.turretPorts = turretPorts;
        this.missilePorts = missilePorts;
    }

    public float getUpgradeModifier(PlayerState state, UpgradeEntryType upgradeType) {
        float modifier = 1.0f;
        ArrayList<UpgradeEntry> upgrades = state.getUpgrades(this.id);
        if (upgrades == null)
            return modifier;
        for (UpgradeEntry upgrade : upgrades) {
            if (upgrade.type == upgradeType) {
                modifier += upgrade.getCurrentMultiplier();
            }
        }
        return modifier;
    }

    public float getMaxHp(PlayerState state) {
        return this.maxHp * getUpgradeModifier(state, UpgradeEntryType.ShieldUpgrade);
    }

    public float getDamage(PlayerState state) {
        return 6f * getUpgradeModifier(state, UpgradeEntryType.DamageUpgrade);
    }

    public float getSpeed(PlayerState state) {
        return 50f * getUpgradeModifier(state, UpgradeEntryType.SpeedUpgrade);
    }

    public AssetDescriptor<Model> getVariantAssetModel() {
        return Assets.spaceShipAssets.get(modelDescriptorId);
    }
}
