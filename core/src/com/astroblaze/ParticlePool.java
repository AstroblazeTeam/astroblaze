package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.utils.Pool;

public class ParticlePool extends Pool<ParticleEffect> {
    private final ParticleSystem system;
    private ParticleEffect effect;

    public ParticlePool(ParticleSystem system, ParticleEffect effect) {
        super(0, 256);
        this.system = system;
        setEffect(effect);
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
        pfx.reset();
    }

    @Override
    protected ParticleEffect newObject() {
        ParticleEffect pfx = effect.copy();
        pfx.init();
        system.add(pfx);
        return pfx;
    }

    @Override
    protected void discard(ParticleEffect pfx) {
        system.remove(pfx);
        super.discard(pfx);
    }
}