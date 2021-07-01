package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

public enum EnemyType {
    Simple(Assets.spaceShipAssets.get(4), 15f, 100f, 100f, 2f, 2, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 30f), // simple forward shooting enemy
    TrainingDummy(Assets.spaceShipAssets.get(5), 15f, 300f, 100f, 0f, 0, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 0f), // simple training dummy for tutorial
    SineWave(Assets.spaceShipAssets.get(6), 15f, 100f, 100f, 2f, 3, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 30f), // enemy moving in sine wave pattern
    Rammer(Assets.spaceShipAssets.get(7), 15f, 50f, 10000f, 1000f, 1, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 30f), // enemy moving in sine wave pattern
    MoneyDrop(Assets.spaceShipAssets.get(8), 15f, 500f, 200f, 2f, 0, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 60f), // enemy drops money
    Boss(Assets.spaceShipAssets.get(9), 25f, 3000f, 1000f, 300f, 0, 3f, 1f, 30f, 3, 3f, 1 / 4f, 70f, 5f, 40f), // boss dummy
    MiniBoss1(Assets.spaceShipAssets.get(12), 20f, 500f, 350f, 100f, 2, 3f, 1f, 30f, 2, 3f, 1 / 4f, 60f, 5f, 50f); // miniboss1

    public final AssetDescriptor<Model> modelDescriptor;
    public final float modelScale;
    public final float value;
    public final float baseHp;
    public final float hpPerLevel;
    public final int guns;
    public final float gunDamage;
    public final float gunFireInterval;
    public final float gunBulletSpeed;
    public final int turrets;
    public final float turretDamage;
    public final float turretFireInterval;
    public final float turretBulletSpeed;
    public final float aiMoveDecisionTime;
    public final float speed;

    EnemyType(AssetDescriptor<Model> modelDescriptor, float modelScale, float value,
              float baseHp, float hpPerLevel,
              int guns, float gunDamage, float gunFireInterval, float gunBulletSpeed,
              int turrets, float turretDamage, float turretFireInterval, float turretBulletSpeed,
              float aiMoveDecisionTime,
              float speed) {
        this.modelDescriptor = modelDescriptor;
        this.value = value;
        this.baseHp = baseHp;
        this.hpPerLevel = hpPerLevel;
        this.guns = guns;
        this.modelScale = modelScale;
        this.gunDamage = gunDamage;
        this.gunFireInterval = gunFireInterval;
        this.gunBulletSpeed = gunBulletSpeed;
        this.turrets = turrets;
        this.turretDamage = turretDamage;
        this.turretFireInterval = turretFireInterval;
        this.turretBulletSpeed = turretBulletSpeed;
        this.aiMoveDecisionTime = aiMoveDecisionTime;
        this.speed = speed;
    }
}
