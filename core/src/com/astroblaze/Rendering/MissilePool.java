package com.astroblaze.Rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class MissilePool extends Pool<Missile> {
    private final Scene3D scene;
    private AssetDescriptor<Model> model;
    private ParticlePool particles;
    private Vector3 offscreenPosition = new Vector3(6000f, 0f, 0f);

    public MissilePool(Scene3D scene) {
        super(0, 512);
        this.scene = scene;
    }

    public void setAssets(ParticlePool particles, AssetDescriptor<Model> model) {
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
        scene.addActor(missile);
        return missile;
    }

    @Override
    public void free(Missile missile) {
        super.free(missile);
        if (missile.effect == null) {
            Gdx.app.debug("MissilePool", "free() on missile with null effect");
        } else {
            particles.free(missile.effect);
            missile.effect.translate(offscreenPosition);
            missile.effect = null;
        }
        scene.removeActor(missile);
    }

    @Override
    protected Missile newObject() {
        return new Missile(model);
    }
}