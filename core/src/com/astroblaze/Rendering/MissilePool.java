package com.astroblaze.Rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;

public class MissilePool extends Pool<Missile> {
    private final Scene3D scene;
    private final Vector3 offscreenPosition = new Vector3(6000f, 0f, 0f);
    private final ArrayList<Missile> activeMissiles = new ArrayList<>(256);
    private AssetDescriptor<Model> model;
    private ParticlePool particles;

    public ArrayList<Missile> getActiveMissiles() {
        return this.activeMissiles;
    }

    public MissilePool(Scene3D scene) {
        super(64, 512);
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
        activeMissiles.add(missile);
        return missile;
    }

    @Override
    public void free(Missile missile) {
        super.free(missile);
        if (missile.effect == null) {
            Gdx.app.debug("MissilePool", "free() on missile with null effect");
        } else {
            missile.effect.translate(offscreenPosition);
            particles.free(missile.effect);
            missile.effect = null;
        }
        activeMissiles.remove(missile);
    }

    @Override
    protected Missile newObject() {
        return new Missile(model);
    }
}