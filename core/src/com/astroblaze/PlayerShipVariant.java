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

    public final float baseHp;
    public final float baseGunDamage = 5f;
    public final float baseSpeed = 50f;
    public final float baseTurretDamage = 4f;
    public final float baseMissileDamage = 100f;
    public final float baseLaserDamage = 500f;

    PlayerShipVariant(int id, int modelDescriptorId, float modelScale, float baseHp,
                      float price, int gunPorts, int turretPorts, int missilePorts) {
        this.id = id;
        this.modelDescriptorId = modelDescriptorId;
        this.modelScale = modelScale;
        this.baseHp = baseHp;
        this.price = price;
        this.gunPorts = gunPorts;
        this.turretPorts = turretPorts;
        this.missilePorts = missilePorts;
    }

    public float getUpgradeModifier(PlayerState state, UpgradeEntryType upgradeType) {
        float modifier = 1f;
        ArrayList<UpgradeEntry> upgrades = state.getUpgrades(this.id);
        if (upgrades == null)
            return modifier;
        for (UpgradeEntry upgrade : upgrades) {
            if (upgrade.type == upgradeType) {
                modifier *= upgrade.getCurrentMultiplier();
            }
        }
        return modifier;
    }

    public float getMaxHp(PlayerState state) {
        return baseHp * getUpgradeModifier(state, UpgradeEntryType.ShieldUpgrade);
    }

    public float getGunDamage(PlayerState state) {
        return baseGunDamage * getDamageModifier(state);
    }

    public float getTurretDamage(PlayerState state) {
        return baseTurretDamage * getDamageModifier(state);
    }

    public float getMissileDamage(PlayerState state) {
        return baseMissileDamage * getDamageModifier(state);
    }

    public float getLaserDamage(PlayerState state) {
        return baseLaserDamage * getDamageModifier(state);
    }

    public float getDamageModifier(PlayerState state) {
        return getUpgradeModifier(state, UpgradeEntryType.DamageUpgrade);
    }

    public float getSpeed(PlayerState state) {
        return baseSpeed * getSpeedModifier(state);
    }

    public float getSpeedModifier(PlayerState state) {
        return getUpgradeModifier(state, UpgradeEntryType.SpeedUpgrade);
    }

    public AssetDescriptor<Model> getVariantAssetModel() {
        return Assets.spaceShipAssets.get(modelDescriptorId);
    }
}
