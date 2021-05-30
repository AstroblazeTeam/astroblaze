package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Ship extends Renderable {
    private final float bankSpeed = 90f;
    private final float moveSpeed = 60f;
    private float currentBank;
    private final Vector3 moveVector = new Vector3();
    private float fireInterval = 1f / 8f;
    private float fireClock = 0f;

    public Ship(Scene3D scene, Model model) {
        super(scene, model);
        setScale(0.5f);
        applyTRS();
    }

    public void setMoveVector(Vector3 moveVector) {
        this.moveVector.set(moveVector);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector3 currentPos = getPosition();
        Vector3 diff = moveVector.cpy().sub(currentPos);
        float travelDist = moveSpeed * delta;
        if (MathUtils.isEqual(currentBank, 0f, 0.1f) && MathUtils.isEqual(diff.x, 0f, 0.1f)) {
            currentBank = 0f;
        } else {
            currentBank -= Math.signum(diff.x != 0f ? diff.x : currentBank) * bankSpeed * delta;
            currentBank = MathUtils.clamp(currentBank, -15f, +15f);
        }

        if (diff.len() > travelDist) {
            currentPos.mulAdd(diff.nor(), travelDist);
        } else {
            currentPos = moveVector.cpy();
        }

        setPosition(currentPos);
        setRotation(new Quaternion(Vector3.Z, currentBank));
        applyTRS();

        fireClock -= delta;
        if (fireClock < 0f) {
            fireClock = fireInterval;

            Missile missile = scene.getMissilesPool().obtain();
            Vector3 pos = this.getPosition().cpy();
            missile.setPosition(pos);
            missile.setTargetVector(pos.cpy().add(0, 0, 1000f));
            missile.applyTRS();
        }
    }
}
