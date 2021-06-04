package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Ship extends Renderable {
    private final float bankSpeed = 90f;
    private final float moveSpeed = 80f;
    private float currentBank;
    private final Vector3 moveVector = new Vector3();
    private float fireInterval = 1f / 2f;
    private float fireClock = 0f;
    private boolean isControlled = false;

    public Ship(Scene3D scene, Model model) {
        super(scene, model);
        reset();
    }

    public void setMoveVector(Vector3 moveVector) {
        this.moveVector.set(moveVector);
    }

    public void reset() {
        moveVector.setZero();
        setPosition(new Vector3(0f, 0f, 0f));
        setRotation(new Quaternion());
        setScale(0.5f);
        applyTRS();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector3 currentPos = getPosition().cpy();
        Vector3 diff = getPosition().cpy().sub(currentPos);
        if (!isControlled) {
            diff.setZero();
        }

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
        if (isControlled && fireClock < 0f) {
            fireClock = fireInterval;

            Missile missile = scene.getMissilesPool().obtain();
            Vector3 pos = this.getPosition().cpy();
            missile.setPosition(pos);
            missile.setTargetVector(pos.cpy().add(0, 0, 1000f));
            missile.applyTRS();
        }
    }

    public boolean getControlled() {
        return this.isControlled;
    }

    public void setControlled(boolean isControlled) {
        this.isControlled = isControlled;
    }
}
