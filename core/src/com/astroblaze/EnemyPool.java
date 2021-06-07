package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pool;

public class EnemyPool extends Pool<Enemy> {
    private final Scene3D scene;
    private ParticlePool particles;

    public EnemyPool(Scene3D scene) {
        super(0, 256);
        this.scene = scene;
    }

    public void setAssets(ParticlePool particles) {
        this.particles = particles;
    }

    @Override
    protected void reset(Enemy enemy) {
        // super.reset(pfx); - no need, default implementation only handles Poolable
        BoundingBox bb = scene.gameBounds;

        enemy.reset(bb);
    }

    @Override
    public Enemy obtain() {
        Enemy enemy = super.obtain();
        Gdx.app.debug("EnemyPool", "Added enemy to scene");
        scene.addActors.add(enemy);
        return enemy;
    }

    @Override
    public void free(Enemy enemy) {
        super.free(enemy);
        Gdx.app.debug("EnemyPool", "Removed enemy from scene");
        scene.removeActors.add(enemy);
    }

    @Override
    protected Enemy newObject() {
        Enemy enemy = new Enemy(scene, EnemyType.Idle);
        reset(enemy);
        return enemy;
    }
}