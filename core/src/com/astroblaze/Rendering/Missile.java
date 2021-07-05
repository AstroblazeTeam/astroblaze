package com.astroblaze.Rendering;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Missile extends Renderable {
    public final static float unpoweredSpeed = 150f;
    public final static float maxUnpoweredTime = 0.25f;
    private final float baseDamage = 100f;

    protected ParticleEffect effect;

    private final Vector3 unpoweredDir = new Vector3();
    private final Vector3 moveVector = new Vector3();
    private final float moveSpeed = 80f;
    private float unpoweredTime = 0.5f;
    private float damage = baseDamage;

    public Missile(AssetDescriptor<Model> model) {
        setModel(model);
        reset();
    }

    public void setUnpoweredDir(float x, float y, float z) {
        this.unpoweredDir.set(x, y, z);
    }

    public void reset() {
        this.unpoweredTime = maxUnpoweredTime;
        this.unpoweredDir.set(MathUtils.random(-0.5f, 0.5f) * unpoweredSpeed, 0f, 0f);
        this.setScale(0.75f);
    }

    public void setTargetVector(Vector3 moveVector) {
        this.moveVector.set(moveVector);
    }

    public Vector3 getVelocity() {
        return this.moveVector.cpy().nor().scl(moveSpeed);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (unpoweredTime > 0f) {
            unpoweredTime = Math.max(0f, unpoweredTime - delta);
            final float fakeDeceleration = unpoweredTime / maxUnpoweredTime;
            getPosition().mulAdd(unpoweredDir, fakeDeceleration * delta);
            addRotation(new Quaternion(Vector3.Z, delta * 720f * fakeDeceleration));
            applyTRS();
        } else if (unpoweredTime == 0f) {
            effect.start();
            unpoweredTime = -1f;
        } else {
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
            effect.setTransform(getTransform().cpy());
        }
    }

    public void setDamageModifier(float damageModifier) {
        this.damage = baseDamage * damageModifier;
    }

    public float getDamage() {
        return this.damage;
    }
}
