package com.astroblaze.Rendering;

import com.astroblaze.*;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

public enum EnemyType {
    Idle(Assets.spaceShipAssets.get(3), 15f, 0f, 100f, 0, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 0f), // for static enemies with no hitbox or behaviour
    Simple(Assets.spaceShipAssets.get(4), 15f, 100f, 100f, 2, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 30f), // simple forward shooting enemy
    TrainingDummy(Assets.spaceShipAssets.get(5), 15f, 300f, 100f, 0, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 0f), // simple training dummy for tutorial
    SineWave(Assets.spaceShipAssets.get(6), 15f, 100f, 100f, 3, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 30f), // enemy moving in sine wave pattern
    Rammer(Assets.spaceShipAssets.get(7), 15f, 50f, 10000f, 1, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 30f), // enemy moving in sine wave pattern
    MoneyDrop(Assets.spaceShipAssets.get(8), 15f, 500f, 200f, 0, 3f, 1f, 30f, 0, 2.5f, 1 / 3f, 50f, 0f, 60f), // enemy drops money
    Boss(Assets.spaceShipAssets.get(9), 25f, 3000f, 10000f, 0, 3f, 1f, 30f, 3, 5f, 1 / 8f, 80f, 4f, 40f); // boss dummy

    public final AssetDescriptor<Model> modelDescriptor;
    public final float value;
    public final float hp;
    public final float modelScale;
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

    EnemyType(AssetDescriptor<Model> modelDescriptor, float modelScale, float value, float hp,
              int guns, float gunDamage, float gunFireInterval, float gunBulletSpeed,
              int turrets, float turretDamage, float turretFireInterval, float turretBulletSpeed,
              float aiMoveDecisionTime,
              float speed) {
        this.modelDescriptor = modelDescriptor;
        this.value = value;
        this.hp = hp;
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
