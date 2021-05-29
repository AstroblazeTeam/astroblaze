package com.astroblaze;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Ship extends Renderable {
    private final float bankSpeed = 25f;
    private final float moveSpeed = 50f;
    private float currentBank;
    private final Vector3 moveVector = new Vector3();

    public void setMoveVector(Vector3 moveVector) {
        this.moveVector.set(moveVector);
    }

    public Ship(Scene3D scene, Model model) {
        super(scene, model);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector3 currentPos = getTransform().getTranslation(new Vector3());
        Vector3 diff = moveVector.cpy().sub(currentPos);
        float travelDist = moveSpeed * delta;
        if(MathUtils.isEqual(currentBank, 0f, 0.1f) && MathUtils.isEqual(diff.x, 0f, 0.1f)) {
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

        getTransform().set(currentPos,
                new Quaternion(Vector3.Z, currentBank),
                new Vector3(0.5f, 0.5f, 0.5f));
    }
}
