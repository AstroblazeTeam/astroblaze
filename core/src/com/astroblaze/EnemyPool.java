package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pool;

public class EnemyPool extends Pool<Enemy> {
    private final Scene3D scene;
    private Model model;
    private ParticlePool particles;

    public EnemyPool(Scene3D scene) {
        super(0, 256);
        this.scene = scene;
    }

    public void setAssets(ParticlePool particles, Model model) {
        this.particles = particles;
        this.model = model;
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
        scene.addActors.add(enemy);
        //enemy.effect = particles.obtain();
        return enemy;
    }

    @Override
    public void free(Enemy enemy) {
        super.free(enemy);
        scene.removeActors.add(enemy);
        //particles.free(enemy.effect);
        //enemy.effect = null;
    }

    @Override
    protected Enemy newObject() {
        Enemy enemy = new Enemy(scene, model);
        reset(enemy);
        return enemy;
    }
}