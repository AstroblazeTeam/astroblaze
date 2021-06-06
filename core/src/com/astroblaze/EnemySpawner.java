package com.astroblaze;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class EnemySpawner extends Actor {
    private final float spawnInterval;
    private float spawnTimer = 3f;
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
            Enemy enemy = scene.enemyPool.obtain();
            enemy.setType(MathUtils.random(0, 2));
        }
    }
}
