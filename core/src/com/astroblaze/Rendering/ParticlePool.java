package com.astroblaze.Rendering;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

/**
 * This provides a pool for ParticleEffect entities to reduce GC load
 */
public class ParticlePool extends Pool<ParticleEffect> {
    private final ParticleSystem system;
    private ParticleEffect effect;

    public ParticlePool(ParticleSystem system, ParticleEffect effect) {
        super(0, 512);
        this.system = system;
        this.setEffect(effect);
    }

    public ParticlePool(ParticleSystem system) {
        this(system, null);
    }

    public void setEffect(ParticleEffect effect) {
        this.effect = effect != null ? effect.copy() : null;
    }

    @Override
    protected void reset(ParticleEffect pfx) {
        // super.reset(pfx); - no need, default implementation only handles Poolable

        pfx.end();

        // hide starter particles out of the way
        pfx.translate(new Vector3(1000f, 0f, 0f));
    }

    @Override
    protected ParticleEffect newObject() {
        ParticleEffect pfx = effect.copy();
        pfx.init();

        // hide starter particles out of the way
        pfx.translate(new Vector3(1000f, 0f, 0f));

        system.add(pfx);
        return pfx;
    }

    @Override
    protected void discard(ParticleEffect pfx) {
        system.remove(pfx);
        super.discard(pfx);
    }
}