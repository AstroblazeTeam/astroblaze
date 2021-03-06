package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

import java.util.ArrayList;

/**
 * This enum represents the player base ships before upgrades
 * It also provides methods to get the actual stats including
 * the upgrades player owns.
 */
public enum PlayerShipVariant {
    Shuttle(0, 0, 10f, 80f, 100f, 5, 2f, 0f, 1, 1, 2),
    Yacht(1, 1, 12f, 150f, 80f, 10, 3f, 20000f, 1, 2, 2),
    Destroyer(2, 2, 15f, 300f, 50f, 20, 10f, 70000f, 4, 2, 3),
    Sentinel(3, 3, 15f, 200f, 50f, 20, 5f, 75000f, 2, 4, 2),
    Archer(4, 9, 15f, 250f, 50f, 50, 5f, 60000f, 2, 2, 8);

    public final int id;
    public final int modelDescriptorId;
    public final float modelScale;
    public final float price;

    public final int gunPorts;
    public final int turretPorts;
    public final int missilePorts;

    public final float baseHp;
    public final float baseSpeed;
    public final int maxMissiles;
    public final float maxLaser;

    // constants
    public final float baseGunDamage = 5f;
    public final float baseTurretDamage = 4f;
    public final float baseMissileDamage = 100f;
    public final float baseLaserDamage = 500f;

    PlayerShipVariant(int id, int modelDescriptorId, float modelScale,
                      float baseHp, float baseSpeed, int maxMissiles, float maxLaser,
                      float price, int gunPorts, int turretPorts, int missilePorts) {
        this.id = id;
        this.modelDescriptorId = modelDescriptorId;
        this.modelScale = modelScale;

        this.baseHp = baseHp;
        this.baseSpeed = baseSpeed;
        this.maxMissiles = maxMissiles;
        this.maxLaser = maxLaser;

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

    public int getMaxMissiles(PlayerState playerState) {
        return (int) (maxMissiles * getUpgradeModifier(playerState, UpgradeEntryType.MaxMissiles));
    }

    public float getLaserCapacity(PlayerState playerState) {
        return maxLaser * getUpgradeModifier(playerState, UpgradeEntryType.LaserCapacity);
    }

    public float getTurretSpeed(PlayerState playerState) {
        return 120f * getUpgradeModifier(playerState, UpgradeEntryType.TurretSpeed);
    }
}
