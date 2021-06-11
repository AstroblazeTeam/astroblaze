package com.astroblaze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;

public enum EnemyType {
    Idle(Assets.spaceShip1, 1f, 0f, 100f, 0, 0f), // for static enemies with no hitbox or behaviour
    Simple(Assets.spaceShip2, 0.25f, 100f, 100f, 2, 30f), // simple forward shooting enemy
    TrainingDummy(Assets.spaceShip2, 0.5f, 300f, 100f, 0, 0f), // simple training dummy for tutorial
    SineWave(Assets.spaceShip3, 0.25f, 100f, 100f, 3, 30f), // enemy moving in sine wave pattern
    Rammer(Assets.spaceShip1, 0.5f, 50f, 10000f, 1, 30f), // enemy moving in sine wave pattern
    MoneyDrop(Assets.spaceShip1, 0.5f, 500f, 200f, 0, 60f), // enemy drops money
    Boss(Assets.spaceShip1, 3f, 3000f, 1000f, 0, 0f); // boss dummy

    public final AssetDescriptor<Model> modelDescriptor;
    public final float value;
    public final float hp;
    public final int gunPellets;
    public final float modelScale;
    public final float speed;

    EnemyType(AssetDescriptor<Model> modelDescriptor, float modelScale, float value, float hp, int gunPellets, float speed) {
        this.modelDescriptor = modelDescriptor;
        this.value = value;
        this.hp = hp;
        this.gunPellets = gunPellets;
        this.modelScale = modelScale;
        this.speed = speed;
    }
}
