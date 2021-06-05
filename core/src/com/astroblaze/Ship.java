package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Ship extends Renderable {
    private final float bankSpeed = 90f;
    private final float moveSpeed = 80f;
    private final float respawnNoControlTime = 1f;
    private float currentBank;
    private final Vector3 moveVector = new Vector3();
    private float fireInterval = 1f / 2f;
    private float fireIntervalGun = 1f / 20f;
    private float fireClock = 0f;
    private float fireClockGun = 0f;
    private float gunDamage = 10f;
    private float noControlTimer;

    public Ship(Scene3D scene, Model model) {
        super(scene, model);
        reset();
    }

    public void modGunDamage(float mod) {
        this.gunDamage = MathUtils.clamp(this.gunDamage + mod, 10f, 100f);
    }

    public void setMoveVector(Vector3 moveVector) {
        if (noControlTimer > 0f)
            return;
        this.moveVector.set(moveVector);
    }

    public void reset() {
        noControlTimer = respawnNoControlTime;
        moveVector.setZero();
        // set to slightly closer than destroy bounds
        setPosition(new Vector3(0f, 0f, scene.destroyBounds.min.z + 5f));
        setRotation(new Quaternion());
        setScale(0.5f);
        applyTRS();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        noControlTimer -= delta;
        Vector3 currentPos = getPosition().cpy();
        Vector3 diff = moveVector.cpy().sub(currentPos);

        if (MathUtils.isEqual(currentBank, 0f, 0.1f) && MathUtils.isEqual(diff.x, 0f, 0.1f)) {
            currentBank = 0f;
        } else {
            currentBank = MathHelper.moveTowards(currentBank, -diff.x, bankSpeed * delta);
            currentBank = MathUtils.clamp(currentBank, -15f, +15f);
        }

        MathHelper.moveTowards(currentPos, moveVector, moveSpeed * delta);
        setPosition(currentPos);
        setRotation(new Quaternion(Vector3.Z, currentBank));
        applyTRS();

        fireClock -= delta;
        if (isControlled() && fireClock < 0f) {
            fireClock = fireInterval;

            Missile missile = scene.getMissilesPool().obtain();
            Vector3 pos = this.getPosition().cpy();
            missile.setPosition(pos);
            missile.setTargetVector(pos.cpy().add(0, 0, 1000f));
            missile.applyTRS();
        }

        fireClockGun -= delta;
        if (isControlled() && fireClockGun < 0f) {
            fireClockGun = fireIntervalGun;

            final Vector3 vel = new Vector3(0, 0, 3f * moveSpeed);
            scene.decals.addBullet(this.getPosition().cpy().add(+5f, 0f, 3f), vel, 0.1f, gunDamage);
            scene.decals.addBullet(this.getPosition().cpy().add(-5f, 0f, 3f), vel, 0.1f, gunDamage);
        }
    }

    public Vector3 getVelocity() {
        return moveVector.cpy().sub(getPosition()).nor().scl(moveSpeed);
    }

    public boolean isControlled() {
        return noControlTimer <= 0f;
    }

    public void setNoControlTime(float time) {
        this.noControlTimer = time;
    }
}
