package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.utils.Pool;

public class ParticlePool extends Pool<ParticleEffect> {
    private final ParticleEffect sourceEffect;

    public ParticlePool(ParticleEffect sourceEffect) {
        this.sourceEffect = sourceEffect;
    }

    @Override
    public void free(ParticleEffect pfx) {
        pfx.reset();
        super.free(pfx);
    }

    @Override
    protected ParticleEffect newObject() {
        ParticleEffect eff = sourceEffect.copy();
        eff.init();
        return eff;
    }
}