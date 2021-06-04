package com.astroblaze;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;

public class EnemySpawner extends Actor {
    private final float spawnInterval;
    private float spawnTimer = 10f;
    private final Scene3D scene;

    public EnemySpawner(Scene3D scene, float spawnInterval) {
        this.scene = scene;
        this.spawnInterval = spawnInterval;
    }

    @Override
    public void act(float delta) {
        spawnTimer -= delta * scene.getTimeScale();
        if (spawnTimer < 0f) {
            spawnTimer = spawnInterval;
            scene.enemyPool.obtain();
        }
    }
}
