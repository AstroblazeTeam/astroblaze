package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.utils.Pool;

public class MissilePool extends Pool<Missile> {
    private final Scene3D scene;
    private Model model;
    private ParticlePool particles;

    public MissilePool(Scene3D scene) {
        super(0, 256);
        this.scene = scene;
    }

    public void setAssets(ParticlePool particles, Model model) {
        this.particles = particles;
        this.model = model;
    }

    @Override
    protected void reset(Missile missile) {
        missile.reset();
        // super.reset(pfx); - no need, default implementation only handles Poolable
    }

    @Override
    public Missile obtain() {
        Missile missile = super.obtain();
        missile.effect = particles.obtain();
        scene.addActors.add(missile);
        return missile;
    }

    @Override
    public void free(Missile missile) {
        super.free(missile);
        if (missile.effect == null) {
            Gdx.app.debug("MissilePool", "free() on missile with null effect");
        } else {
            particles.free(missile.effect);
            missile.effect = null;
        }
        scene.removeActors.add(missile);
    }

    @Override
    protected Missile newObject() {
        return new Missile(scene, model);
    }
}