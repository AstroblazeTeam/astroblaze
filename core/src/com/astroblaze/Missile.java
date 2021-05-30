package com.astroblaze;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Missile extends Renderable {
    private final float moveSpeed = 80f;
    private final Vector3 moveVector = new Vector3();
    protected ParticleEffect effect;
    protected static ParticlePool particlePool;

    public Missile(Scene3D scene, Model model) {
        super(scene, model);

        if (particlePool == null) {
            particlePool = new ParticlePool(Assets.asset(Assets.flame).copy());
        }
    }

    @Override
    public void show(Scene3D scene) {
        super.show(scene);
        this.effect = particlePool.obtain();
        //this.effect.init();
        this.effect.start();
        this.scene.getParticles().add(effect);
    }

    @Override
    public void hide(Scene3D scene) {
        super.hide(scene);
        this.scene.getParticles().remove(effect);
        particlePool.free(this.effect);
        this.effect = null;
    }

    public void setTargetVector(Vector3 moveVector) {
        this.moveVector.set(moveVector);
        this.setScale(0.75f);
    }

    protected void missileMovement(float delta) {
        Vector3 currentPos = getPosition();
        Vector3 diff = moveVector.cpy().sub(currentPos);
        float travelDist = moveSpeed * delta;

        if (diff.len() > travelDist) {
            currentPos.mulAdd(diff.nor(), travelDist);
        } else {
            currentPos = moveVector.cpy();
        }

        setPosition(currentPos);
        addRotation(new Quaternion(Vector3.Z, delta * 360f));
        applyTRS();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        this.missileMovement(delta);
        this.effect.setTransform(getTransform().cpy());
    }
}
